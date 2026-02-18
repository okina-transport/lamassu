package org.entur.lamassu.mapper.feedidmapper.v2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.entur.lamassu.util.IdMappersUtil;
import org.mobilitydata.gbfs.v2_3.geofencing_zones.GBFSGeofencingZones;
import org.mobilitydata.gbfs.v2_3.geofencing_zones.GBFSRule;
import org.springframework.stereotype.Component;

@Component
public class GeofencingZonesFeedIdMapper implements FeedIdMapper<GBFSGeofencingZones> {

  private final IdMappingService idMappingService;

  public GeofencingZonesFeedIdMapper(IdMappingService idMappingService) {
    this.idMappingService = idMappingService;
  }

  @Override
  public void mapIds(
    GBFSGeofencingZones mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (
      mapped == null ||
      mapped.getData() == null ||
      mapped.getData().getGeofencingZones() == null ||
      CollectionUtils.isEmpty(mapped.getData().getGeofencingZones().getFeatures())
    ) {
      return;
    }
    List<GBFSRule> rules = mapped
      .getData()
      .getGeofencingZones()
      .getFeatures()
      .stream()
      .filter(feature ->
        feature.getProperties() != null &&
        CollectionUtils.isNotEmpty(feature.getProperties().getRules())
      )
      .map(feature -> feature.getProperties().getRules())
      .flatMap(List::stream)
      .toList();

    if (CollectionUtils.isEmpty(rules)) {
      return;
    }

    Set<String> vehicleTypeIds = rules
      .stream()
      .map(GBFSRule::getVehicleTypeId)
      .filter(CollectionUtils::isNotEmpty)
      .flatMap(List::stream)
      .collect(Collectors.toSet());

    if (CollectionUtils.isEmpty(vehicleTypeIds)) {
      return;
    }

    Map<String, String> vehicleTypeIdsMap = toOriginalId
      ? idMappingService.getVehicleTypeIdsSuperToOriginalMap(vehicleTypeIds, feedProvider)
      : idMappingService.getVehicleTypeIdsOriginalToSuperMap(
        vehicleTypeIds,
        feedProvider
      );

    for (var rule : rules) {
      rule.setVehicleTypeId(
        IdMappersUtil.mapIds(rule.getVehicleTypeId(), vehicleTypeIdsMap)
      );
    }
  }
}
