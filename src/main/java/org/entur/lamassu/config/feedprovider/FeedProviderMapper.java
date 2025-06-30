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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FeedProviderMapper {

  private static final Logger log = LoggerFactory.getLogger(FeedProviderMapper.class);

  public FeedProvider mapFromApiResponse(Map<String, Object> providerData) {
    FeedProvider provider = new FeedProvider();
    provider.setSystemId((String) providerData.get("systemId"));
    provider.setOperatorId((String) providerData.get("operatorId"));
    provider.setOperatorName((String) providerData.get("operatorName"));
    provider.setCodespace(providerData.get("codespace").toString().toUpperCase());
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
      try {
        Map<String, Object> ppMap = (Map<String, Object>) providerData.get(
          "pricingPlans"
        );
        GBFSPlan pricingPlan = new GBFSPlan();

        setIfPresent(ppMap, "planId", v -> pricingPlan.setPlanId((String) v));
        setIfPresent(ppMap, "name", v -> pricingPlan.setName((String) v));
        setIfPresent(ppMap, "currency", v -> pricingPlan.setCurrency((String) v));

        setIfPresent(
          ppMap,
          "price",
          v -> {
            try {
              pricingPlan.setPrice(((Number) v).doubleValue());
            } catch (ClassCastException e) {
              log.warn(
                "Invalid 'price' format for pricing plan. Expected Number, got {}. Value: {}",
                v.getClass().getName(),
                v,
                e
              );
            }
          }
        );

        setIfPresent(ppMap, "isTaxable", v -> pricingPlan.setIsTaxable((Boolean) v));
        setIfPresent(ppMap, "description", v -> pricingPlan.setDescription((String) v));
        setIfPresent(ppMap, "url", v -> pricingPlan.setUrl((String) v));
        setIfPresent(
          ppMap,
          "surgePricing",
          v -> pricingPlan.setSurgePricing((Boolean) v)
        );

        if (ppMap.get("perKmPricing") != null) {
          try {
            pricingPlan.setPerKmPricing(
              mapPerKmPricing((List<Map<String, Object>>) ppMap.get("perKmPricing"))
            );
          } catch (ClassCastException e) {
            log.warn(
              "Invalid 'perKmPricing' format. Expected List<Map<String, Object>>, got {}. Value: {}",
              ppMap.get("perKmPricing").getClass().getName(),
              ppMap.get("perKmPricing"),
              e
            );
          }
        }

        if (ppMap.get("perMinPricing") != null) {
          try {
            pricingPlan.setPerMinPricing(
              mapPerMinPricing((List<Map<String, Object>>) ppMap.get("perMinPricing"))
            );
          } catch (ClassCastException e) {
            log.warn(
              "Invalid 'perMinPricing' format. Expected List<Map<String, Object>>, got {}. Value: {}",
              ppMap.get("perMinPricing").getClass().getName(),
              ppMap.get("perMinPricing"),
              e
            );
          }
        }

        List<GBFSPlan> plans = new ArrayList<>();
        plans.add(pricingPlan);
        provider.setPricingPlans(plans);
      } catch (Exception e) {
        log.error(
          "Error during pricing plans mapping for provider: {}",
          provider.getSystemId(),
          e
        );
      }
    }
  }

  private static void mapVehicleTypes(
    Map<String, Object> providerData,
    FeedProvider provider
  ) {
    if (providerData.get("vehicleTypes") instanceof Map<?, ?>) {
      try {
        Map<String, Object> vtMap = (Map<String, Object>) providerData.get(
          "vehicleTypes"
        );
        GBFSVehicleType vehicleType = new GBFSVehicleType();

        setIfPresent(
          vtMap,
          "vehicleTypeId",
          v -> vehicleType.setVehicleTypeId((String) v)
        );
        setIfPresent(
          vtMap,
          "formFactor",
          v -> {
            try {
              vehicleType.setFormFactor(GBFSVehicleType.FormFactor.fromValue((String) v));
            } catch (IllegalArgumentException e) {
              log.warn("Invalid 'formFactor' value for vehicle type. Value: {}", v, e);
            }
          }
        );
        setIfPresent(vtMap, "name", v -> vehicleType.setName((String) v));
        setIfPresent(
          vtMap,
          "propulsionType",
          v -> {
            try {
              vehicleType.setPropulsionType(
                GBFSVehicleType.PropulsionType.valueOf((String) v)
              );
            } catch (IllegalArgumentException e) {
              log.warn(
                "Invalid 'propulsionType' value for vehicle type. Value: {}",
                v,
                e
              );
            }
          }
        );
        setIfPresent(
          vtMap,
          "riderCapacity",
          v -> {
            try {
              vehicleType.setRiderCapacity(((Number) v).intValue());
            } catch (ClassCastException e) {
              log.warn(
                "Invalid 'riderCapacity' format for vehicle type. Expected Number, got {}. Value: {}",
                v.getClass().getName(),
                v,
                e
              );
            }
          }
        );
        setIfPresent(
          vtMap,
          "maxRangeMeters",
          v -> {
            try {
              vehicleType.setMaxRangeMeters(((Number) v).doubleValue());
            } catch (ClassCastException e) {
              log.warn(
                "Invalid 'maxRangeMeters' format for vehicle type. Expected Number, got {}. Value: {}",
                v.getClass().getName(),
                v,
                e
              );
            }
          }
        );
        setIfPresent(vtMap, "make", v -> vehicleType.setMake((String) v));
        setIfPresent(vtMap, "model", v -> vehicleType.setModel((String) v));
        setIfPresent(vtMap, "color", v -> vehicleType.setColor((String) v));
        setIfPresent(
          vtMap,
          "wheelCount",
          v -> {
            try {
              vehicleType.setWheelCount(((Number) v).intValue());
            } catch (ClassCastException e) {
              log.warn(
                "Invalid 'wheelCount' format for vehicle type. Expected Number, got {}. Value: {}",
                v.getClass().getName(),
                v,
                e
              );
            }
          }
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
          v -> {
            try {
              vehicleType.setPricingPlanIds((List<String>) v);
            } catch (ClassCastException e) {
              log.warn(
                "Invalid 'pricingPlanIds' format for vehicle type. Expected List<String>, got {}. Value: {}",
                v.getClass().getName(),
                v,
                e
              );
            }
          }
        );

        List<GBFSVehicleType> vehicleTypes = new ArrayList<>();
        vehicleTypes.add(vehicleType);
        provider.setVehicleTypes(vehicleTypes);
      } catch (Exception e) {
        log.error(
          "Error during vehicle types mapping for provider: {}",
          provider.getSystemId(),
          e
        );
      }
    }
  }

  private static void mapAuthentication(
    Map<String, Object> providerData,
    FeedProvider provider
  ) {
    if (providerData.get("authentication") != null) {
      try {
        Map<String, Object> authMap = (Map<String, Object>) providerData.get(
          "authentication"
        );
        Authentication authentication = new Authentication();

        String authType = (String) authMap.get("type");
        if (authType != null) {
          try {
            authentication.setScheme(AuthenticationScheme.valueOf(authType));
          } catch (IllegalArgumentException e) {
            log.warn("Invalid 'authentication.type' value. Value: {}", authType, e);
          }
        } else {
          log.warn(
            "'authentication.type' is null for provider: {}",
            provider.getSystemId()
          );
        }

        Map<String, String> properties = new HashMap<>();
        authMap.forEach((key, value) -> {
          if (!"type".equals(key) && value != null) {
            properties.put(key, value.toString());
          }
        });

        authentication.setProperties(properties);
        provider.setAuthentication(authentication);
      } catch (Exception e) {
        log.error(
          "Error during authentication mapping for provider: {}",
          provider.getSystemId(),
          e
        );
      }
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
