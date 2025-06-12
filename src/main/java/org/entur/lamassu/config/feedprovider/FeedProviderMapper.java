package org.entur.lamassu.config.feedprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.entur.lamassu.model.provider.Authentication;
import org.entur.lamassu.model.provider.AuthenticationScheme;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.GbfsUtils;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPerKmPricing;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPerMinPricing;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v2_3.vehicle_types.GBFSVehicleType;
import org.springframework.stereotype.Component;

@Component
public class FeedProviderMapper {

  public FeedProvider mapFromApiResponse(Map<String, Object> providerData) {
    FeedProvider provider = new FeedProvider();
    provider.setSystemId((String) providerData.get("systemId"));
    provider.setOperatorId((String) providerData.get("operatorId"));
    provider.setOperatorName((String) providerData.get("operatorName"));
    provider.setCodespace((String) providerData.get("codespace"));
    provider.setUrl((String) providerData.get("url"));
    provider.setLanguage((String) providerData.get("language"));
    provider.setVersion((String) providerData.get("version"));
    provider.setExcludeFeeds(
      GbfsUtils.convertToGBFSFeedNameList(providerData.get("excludeFeeds"))
    );

    mapAuthentication(providerData, provider);
    mapVehicleTypes(providerData, provider);
    mapPricingPlans(providerData, provider);

    return provider;
  }

  private static void mapPricingPlans(
    Map<String, Object> providerData,
    FeedProvider provider
  ) {
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
        pricingPlan.setPerKmPricing(
          mapPerKmPricing((List<Map<String, Object>>) ppMap.get("perKmPricing"))
        );
      }
      if (ppMap.get("perMinPricing") != null) {
        pricingPlan.setPerMinPricing(
          mapPerMinPricing((List<Map<String, Object>>) ppMap.get("perMinPricing"))
        );
      }

      List<GBFSPlan> plans = new ArrayList<>();
      plans.add(pricingPlan);
      provider.setPricingPlans(plans);
    }
  }

  private static void mapVehicleTypes(
    Map<String, Object> providerData,
    FeedProvider provider
  ) {
    if (providerData.get("vehicleTypes") instanceof Map<?, ?>) {
      Map<String, Object> vtMap = (Map<String, Object>) providerData.get("vehicleTypes");
      GBFSVehicleType vehicleType = new GBFSVehicleType();

      setIfPresent(vtMap, "vehicleTypeId", v -> vehicleType.setVehicleTypeId((String) v));
      setIfPresent(
        vtMap,
        "formFactor",
        v -> vehicleType.setFormFactor(GBFSVehicleType.FormFactor.fromValue((String) v))
      );
      setIfPresent(vtMap, "name", v -> vehicleType.setName((String) v));
      setIfPresent(
        vtMap,
        "propulsionType",
        v ->
          vehicleType.setPropulsionType(
            GBFSVehicleType.PropulsionType.valueOf((String) v)
          )
      );
      setIfPresent(
        vtMap,
        "riderCapacity",
        v -> vehicleType.setRiderCapacity(((Number) v).intValue())
      );
      setIfPresent(
        vtMap,
        "maxRangeMeters",
        v -> vehicleType.setMaxRangeMeters(((Number) v).doubleValue())
      );
      setIfPresent(vtMap, "make", v -> vehicleType.setMake((String) v));
      setIfPresent(vtMap, "model", v -> vehicleType.setModel((String) v));
      setIfPresent(vtMap, "color", v -> vehicleType.setColor((String) v));
      setIfPresent(
        vtMap,
        "wheelCount",
        v -> vehicleType.setWheelCount(((Number) v).intValue())
      );
      setIfPresent(vtMap, "vehicleImage", v -> vehicleType.setVehicleImage((String) v));
      setIfPresent(
        vtMap,
        "defaultPricingPlanId",
        v -> vehicleType.setDefaultPricingPlanId((String) v)
      );
      setIfPresent(
        vtMap,
        "pricingPlanIds",
        v -> vehicleType.setPricingPlanIds((List<String>) v)
      );

      List<GBFSVehicleType> vehicleTypes = new ArrayList<>();
      vehicleTypes.add(vehicleType);
      provider.setVehicleTypes(vehicleTypes);
    }
  }

  private static void mapAuthentication(
    Map<String, Object> providerData,
    FeedProvider provider
  ) {
    if (providerData.get("authentication") != null) {
      Map<String, Object> authMap = (Map<String, Object>) providerData.get(
        "authentication"
      );
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
      provider.setAuthentication(authentication);
    }
  }

  // Méthodes utilitaires pour les structures complexes
  public static List<GBFSPerKmPricing> mapPerKmPricing(
    List<Map<String, Object>> perKmPricingList
  ) {
    return perKmPricingList
      .stream()
      .map(map -> {
        GBFSPerKmPricing pricing = new GBFSPerKmPricing();
        setIfPresent(map, "start", v -> pricing.setStart((Integer) v));
        setIfPresent(map, "rate", v -> pricing.setRate((Double) v));
        setIfPresent(map, "interval", v -> pricing.setInterval((Integer) v));
        setIfPresent(map, "end", v -> pricing.setEnd((Integer) v));
        return pricing;
      })
      .collect(Collectors.toList());
  }

  public static List<GBFSPerMinPricing> mapPerMinPricing(
    List<Map<String, Object>> perMinPricingList
  ) {
    return perMinPricingList
      .stream()
      .map(map -> {
        GBFSPerMinPricing pricing = new GBFSPerMinPricing();
        setIfPresent(map, "start", v -> pricing.setStart((Integer) v));
        setIfPresent(map, "rate", v -> pricing.setRate((Double) v));
        setIfPresent(map, "interval", v -> pricing.setInterval((Integer) v));
        setIfPresent(map, "end", v -> pricing.setEnd((Integer) v));
        return pricing;
      })
      .collect(Collectors.toList());
  }

  public static <T> void setIfPresent(
    Map<String, Object> map,
    String key,
    Consumer<Object> setter
  ) {
    if (map.containsKey(key) && map.get(key) != null) {
      setter.accept(map.get(key));
    }
  }
}
