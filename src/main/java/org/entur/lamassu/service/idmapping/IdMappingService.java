package org.entur.lamassu.service.idmapping;

import java.util.Map;
import java.util.Set;
import org.entur.lamassu.model.provider.FeedProvider;

public interface IdMappingService {
  Map<String, String> getAlertIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getAlertIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getBikeIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getBikeIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getPricingPlanIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getPricingPlanIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  String getSystemIdOriginalToSuper(String id, FeedProvider feedProvider);

  String getSystemIdSuperToOriginal(String id, FeedProvider feedProvider);

  Map<String, String> getRegionIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getRegionIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getStationIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getStationIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getVehicleTypeIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  );

  Map<String, String> getVehicleTypeIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  );
}
