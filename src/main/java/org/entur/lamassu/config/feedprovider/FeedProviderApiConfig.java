package org.entur.lamassu.config.feedprovider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.apache.commons.collections4.ListUtils;
import org.entur.lamassu.model.provider.Authentication;
import org.entur.lamassu.model.provider.AuthenticationScheme;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.TokenService;
import org.entur.lamassu.util.GbfsUtils;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPerKmPricing;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPerMinPricing;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v2_3.vehicle_types.GBFSVehicleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Primary
public class FeedProviderApiConfig implements FeedProviderConfig {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String GBFS_APIS_URI = "/gbfs-apis/for-lamassu";
    private final String ishtarBaseUrl;
    private final WebClient webClient;
    private final TokenService tokenService;

    // Cache des providers
    private List<FeedProvider> cachedProviders;
    private Instant lastUpdated;

    @Autowired
    public FeedProviderApiConfig(
            @Value("${ishtar.server.url}") String ishtarBaseUrl,
            WebClient webClient,
            TokenService tokenService
    ) {
        this.ishtarBaseUrl = ishtarBaseUrl;
        this.webClient = webClient;
        this.tokenService = tokenService;
    }

    @PostConstruct
    public void init() {
        // Initialise le cache au démarrage
        refreshProviders();
    }

    @Override
    public List<FeedProvider> getProviders() {
        if (cachedProviders == null) {
            refreshProviders();
        }
        return cachedProviders != null ? cachedProviders : ListUtils.emptyIfNull(new ArrayList<>());
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void refreshProviders() {
        try {
            this.cachedProviders = fetchProvidersFromApi();
            this.lastUpdated = Instant.now();
        } catch (Exception e) {
            logger.error("Failed to refresh providers", e);
        }
    }

    private List<FeedProvider> fetchProvidersFromApi() {
        return ListUtils.emptyIfNull(
                getWebClient(GBFS_APIS_URI)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(this::extractProviders)
                        .onErrorReturn(new ArrayList<>(0))
                        .block()
        );
    }

    private List<FeedProvider> extractProviders(Map<String, Object> response) {
        Map<String, Object> lamassu = (Map<String, Object>) response.get("lamassu");
        List<Map<String, Object>> providersData = (List<Map<String, Object>>) lamassu.get("providers");

        return providersData
                .stream()
                .map(this::mapToFeedProvider)
                .collect(Collectors.toList());
    }

    private FeedProvider mapToFeedProvider(Map<String, Object> providerData) {
        FeedProvider provider = new FeedProvider();
        provider.setSystemId((String) providerData.get("systemId"));
        provider.setOperatorId((String) providerData.get("operatorId"));
        provider.setOperatorName((String) providerData.get("operatorName"));
        provider.setCodespace((String) providerData.get("codespace"));
        provider.setUrl((String) providerData.get("url"));
        provider.setLanguage((String) providerData.get("language"));
        provider.setExcludeFeeds(
                GbfsUtils.convertToGBFSFeedNameList(providerData.get("excludeFeeds"))
        );

        if (providerData.get("authentication") != null) {
            Map<String, Object> authMap = (Map<String, Object>) providerData.get("authentication");
            Authentication authentication = new Authentication();

            String authType = (String) authMap.get("type");
            authentication.setScheme(AuthenticationScheme.valueOf(authType));

            Map<String, String> properties = new HashMap<>();
            authMap.forEach((key, value) -> {
                if (!"type".equals(key) && value != null) {
                    properties.put(key, value.toString());
                }
            });

            authentication.setProperties(properties);
        }

        if (providerData.get("vehicleTypes") instanceof Map<?, ?>) {
            Map<String, Object> vtMap = (Map<String, Object>) providerData.get("vehicleTypes");
            GBFSVehicleType vehicleType = new GBFSVehicleType();

            setIfPresent(vtMap, "vehicleTypeId", v -> vehicleType.setVehicleTypeId((String) v));
            setIfPresent(vtMap, "formFactor", v -> vehicleType.setFormFactor(GBFSVehicleType.FormFactor.fromValue((String) v)));
            setIfPresent(vtMap, "name", v -> vehicleType.setName((String) v));
            setIfPresent(vtMap, "propulsionType", v -> vehicleType.setPropulsionType(GBFSVehicleType.PropulsionType.valueOf((String) v)));
            setIfPresent(vtMap, "riderCapacity", v -> vehicleType.setRiderCapacity(((Number) v).intValue()));
            setIfPresent(vtMap, "maxRangeMeters", v -> vehicleType.setMaxRangeMeters(((Number) v).doubleValue()));
            setIfPresent(vtMap, "make", v -> vehicleType.setMake((String) v));
            setIfPresent(vtMap, "model", v -> vehicleType.setModel((String) v));
            setIfPresent(vtMap, "color", v -> vehicleType.setColor((String) v));
            setIfPresent(vtMap, "wheelCount", v -> vehicleType.setWheelCount(((Number) v).intValue()));
            setIfPresent(vtMap, "vehicleImage", v -> vehicleType.setVehicleImage((String) v));
            setIfPresent(vtMap, "defaultPricingPlanId", v -> vehicleType.setDefaultPricingPlanId((String) v));
            setIfPresent(vtMap, "pricingPlanIds", v -> vehicleType.setPricingPlanIds((List<String>) v));

            List<GBFSVehicleType> vehicleTypes = new ArrayList<>();
            vehicleTypes.add(vehicleType);
            provider.setVehicleTypes(vehicleTypes);
        }


        if (providerData.get("pricingPlans") instanceof Map<?, ?>) {
            Map<String, Object> ppMap = (Map<String, Object>) providerData.get("pricingPlans");
            GBFSPlan pricingPlan = new GBFSPlan();

            setIfPresent(ppMap, "planId", v -> pricingPlan.setPlanId((String) v));
            setIfPresent(ppMap, "name", v -> pricingPlan.setName((String) v));
            setIfPresent(ppMap, "currency", v -> pricingPlan.setCurrency((String) v));
            setIfPresent(ppMap, "price", v -> pricingPlan.setPrice(((Number) v).doubleValue()));
            setIfPresent(ppMap, "isTaxable", v -> pricingPlan.setIsTaxable((Boolean) v));
            setIfPresent(ppMap, "description", v -> pricingPlan.setDescription((String) v));
            setIfPresent(ppMap, "url", v -> pricingPlan.setUrl((String) v));
            setIfPresent(ppMap, "surgePricing", v -> pricingPlan.setSurgePricing((Boolean) v));
            if (ppMap.get("perKmPricing") != null) {
                pricingPlan.setPerKmPricing(mapPerKmPricing((List<Map<String, Object>>) ppMap.get("perKmPricing")));
            }
            if (ppMap.get("perMinPricing") != null) {
                pricingPlan.setPerMinPricing(mapPerMinPricing((List<Map<String, Object>>) ppMap.get("perMinPricing")));
            }

            List<GBFSPlan> plans = new ArrayList<>();
            plans.add(pricingPlan);
            provider.setPricingPlans(plans);
        }

        return provider;
    }

    // Méthodes utilitaires pour les structures complexes
    private List<GBFSPerKmPricing> mapPerKmPricing(List<Map<String, Object>> perKmPricingList) {
        return perKmPricingList.stream().map(map -> {
            GBFSPerKmPricing pricing = new GBFSPerKmPricing();
            setIfPresent(map, "start", v -> pricing.setStart((Integer) v));
            setIfPresent(map, "rate", v -> pricing.setRate((Double) v));
            setIfPresent(map, "interval", v -> pricing.setInterval((Integer) v));
            setIfPresent(map, "end", v -> pricing.setEnd((Integer) v));
            return pricing;
        }).collect(Collectors.toList());
    }

    private List<GBFSPerMinPricing> mapPerMinPricing(List<Map<String, Object>> perMinPricingList) {
        return perMinPricingList.stream().map(map -> {
            GBFSPerMinPricing pricing = new GBFSPerMinPricing();
            setIfPresent(map, "start", v -> pricing.setStart((Integer) v));
            setIfPresent(map, "rate", v -> pricing.setRate((Double) v));
            setIfPresent(map, "interval", v -> pricing.setInterval((Integer) v));
            setIfPresent(map, "end", v -> pricing.setEnd((Integer) v));
            return pricing;
        }).collect(Collectors.toList());
    }

    private static <T> void setIfPresent(Map<String, Object> map, String key, Consumer<Object> setter) {
        if (map.containsKey(key) && map.get(key) != null) {
            setter.accept(map.get(key));
        }
    }


    private WebClient.RequestHeadersSpec<?> getWebClient(String uri) {
        return webClient
                .get()
                .uri(ishtarBaseUrl + uri)
                .headers(headers -> headers.setBearerAuth(tokenService.getToken()));
    }
}
