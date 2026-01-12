package org.entur.lamassu.mapper.feedidmapper.v2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.entur.lamassu.util.IdMappersUtil;
import org.mobilitydata.gbfs.v2_3.station_information.GBFSStation;
import org.mobilitydata.gbfs.v2_3.station_information.GBFSStationInformation;
import org.springframework.stereotype.Component;

@Component
public class StationInformationFeedIdMapper
  implements FeedIdMapper<GBFSStationInformation> {

  private final IdMappingService idMappingService;

  public StationInformationFeedIdMapper(IdMappingService idMappingService) {
    this.idMappingService = idMappingService;
  }

  @Override
  public void mapIds(
    GBFSStationInformation mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (
      mapped == null ||
      mapped.getData() == null ||
      CollectionUtils.isEmpty(mapped.getData().getStations())
    ) {
      return;
    }

    Map<String, String> stationIdsMap = buildStationIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );
    Map<String, String> regionIdsMap = buildRegionIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );
    Map<String, String> vehicleTypeIdsMap = buildVehicleTypeIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );

    for (var station : mapped.getData().getStations()) {
      station.setStationId(IdMappersUtil.mapId(station.getStationId(), stationIdsMap));
      station.setRegionId(IdMappersUtil.mapId(station.getRegionId(), regionIdsMap));
      if (station.getVehicleCapacity() != null) {
        Map<String, Double> mappedIds = IdMappersUtil.mapIdsMap(
          station.getVehicleCapacity().getAdditionalProperties(),
          vehicleTypeIdsMap
        );
        if (mappedIds != null) {
          station.getVehicleCapacity().getAdditionalProperties().clear();
          station.getVehicleCapacity().getAdditionalProperties().putAll(mappedIds);
        }
      }
      if (station.getVehicleTypeCapacity() != null) {
        Map<String, Double> mappedIds = IdMappersUtil.mapIdsMap(
          station.getVehicleTypeCapacity().getAdditionalProperties(),
          vehicleTypeIdsMap
        );
        if (mappedIds != null) {
          station.getVehicleTypeCapacity().getAdditionalProperties().clear();
          station.getVehicleTypeCapacity().getAdditionalProperties().putAll(mappedIds);
        }
      }
    }
  }

  private Map<String, String> buildStationIdsMap(
    GBFSStationInformation mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> stationIds = mapped
      .getData()
      .getStations()
      .stream()
      .map(GBFSStation::getStationId)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getStationIdsSuperToOriginalMap(stationIds, feedProvider)
      : idMappingService.getStationIdsOriginalToSuperMap(stationIds, feedProvider);
  }

  private Map<String, String> buildRegionIdsMap(
    GBFSStationInformation mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> regionIds = mapped
      .getData()
      .getStations()
      .stream()
      .map(GBFSStation::getRegionId)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getRegionIdsSuperToOriginalMap(regionIds, feedProvider)
      : idMappingService.getRegionIdsOriginalToSuperMap(regionIds, feedProvider);
  }

  private Map<String, String> buildVehicleTypeIdsMap(
    GBFSStationInformation mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> vehicleTypeIds = new HashSet<>();
    for (var station : mapped.getData().getStations()) {
      if (station.getVehicleCapacity() != null) {
        vehicleTypeIds.addAll(
          station.getVehicleCapacity().getAdditionalProperties().keySet()
        );
      }
      if (station.getVehicleTypeCapacity() != null) {
        vehicleTypeIds.addAll(
          station.getVehicleTypeCapacity().getAdditionalProperties().keySet()
        );
      }
    }
    return toOriginalId
      ? idMappingService.getVehicleTypeIdsSuperToOriginalMap(vehicleTypeIds, feedProvider)
      : idMappingService.getVehicleTypeIdsOriginalToSuperMap(
        vehicleTypeIds,
        feedProvider
      );
  }
}
