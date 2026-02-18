package org.entur.lamassu.service.idmapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.IdMappersUtil;

public class BaseIdMappingService implements IdMappingService {

  @Override
  public Map<String, String> getAlertIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturOriginalToSuperIdsMapping(
      ids,
      feedProvider,
      IdMappersUtil.ALERT_ID_TYPE
    );
  }

  @Override
  public Map<String, String> getAlertIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturSuperToOriginalIdsMapping(ids);
  }

  @Override
  public Map<String, String> getBikeIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturOriginalToSuperIdsMapping(
      ids,
      feedProvider,
      IdMappersUtil.BIKE_ID_TYPE
    );
  }

  @Override
  public Map<String, String> getBikeIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturSuperToOriginalIdsMapping(ids);
  }

  @Override
  public Map<String, String> getPricingPlanIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturOriginalToSuperIdsMapping(
      ids,
      feedProvider,
      IdMappersUtil.PRICING_PLAN_ID_TYPE
    );
  }

  @Override
  public Map<String, String> getPricingPlanIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturSuperToOriginalIdsMapping(ids);
  }

  @Override
  public Map<String, String> getRegionIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturOriginalToSuperIdsMapping(
      ids,
      feedProvider,
      IdMappersUtil.REGION_ID_TYPE
    );
  }

  @Override
  public Map<String, String> getRegionIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturSuperToOriginalIdsMapping(ids);
  }

  @Override
  public Map<String, String> getStationIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturOriginalToSuperIdsMapping(
      ids,
      feedProvider,
      IdMappersUtil.STATION_ID_TYPE
    );
  }

  @Override
  public Map<String, String> getStationIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    return IdMappersUtil.enturSuperToOriginalIdsMapping(ids);
  }

  @Override
  public String getSystemIdOriginalToSuper(String id, FeedProvider feedProvider) {
    // unmapped by default
    return id;
  }

  @Override
  public String getSystemIdSuperToOriginal(String id, FeedProvider feedProvider) {
    // unmapped by default
    return id;
  }

  @Override
  public Map<String, String> getVehicleTypeIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    // do not map vehicle type ids to be consistent with SIRI FM useOriginalId
    if (CollectionUtils.isEmpty(ids)) {
      return new HashMap<>();
    }
    return ids
      .stream()
      .collect(Collectors.toMap(Function.identity(), Function.identity()));
  }

  @Override
  public Map<String, String> getVehicleTypeIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    // do not map vehicle type ids to be consistent with SIRI FM useOriginalId
    if (CollectionUtils.isEmpty(ids)) {
      return new HashMap<>();
    }
    return ids
      .stream()
      .collect(Collectors.toMap(Function.identity(), Function.identity()));
  }
}
