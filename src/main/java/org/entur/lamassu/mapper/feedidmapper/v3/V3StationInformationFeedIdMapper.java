package org.entur.lamassu.mapper.feedidmapper.v3;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.entur.lamassu.util.IdMappersUtil;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStationInformation;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSVehicleDocksCapacity;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSVehicleTypesCapacity;
import org.springframework.stereotype.Component;

@Component
public class V3StationInformationFeedIdMapper
  implements FeedIdMapper<GBFSStationInformation> {

  private final IdMappingService idMappingService;

  public V3StationInformationFeedIdMapper(IdMappingService idMappingService) {
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
      if (CollectionUtils.isNotEmpty(station.getVehicleDocksCapacity())) {
        for (var vehicleDocksCapacity : station.getVehicleDocksCapacity()) {
          vehicleDocksCapacity.setVehicleTypeIds(
            IdMappersUtil.mapIds(
              vehicleDocksCapacity.getVehicleTypeIds(),
              vehicleTypeIdsMap
            )
          );
        }
      }
      if (CollectionUtils.isNotEmpty(station.getVehicleTypesCapacity())) {
        for (var vehicleTypesCapacity : station.getVehicleTypesCapacity()) {
          vehicleTypesCapacity.setVehicleTypeIds(
            IdMappersUtil.mapIds(
              vehicleTypesCapacity.getVehicleTypeIds(),
              vehicleTypeIdsMap
            )
          );
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
      if (CollectionUtils.isNotEmpty(station.getVehicleDocksCapacity())) {
        vehicleTypeIds.addAll(
          station
            .getVehicleDocksCapacity()
            .stream()
            .map(GBFSVehicleDocksCapacity::getVehicleTypeIds)
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(List::stream)
            .filter(StringUtils::isNotBlank)
            .toList()
        );
      }
      if (CollectionUtils.isNotEmpty(station.getVehicleTypesCapacity())) {
        vehicleTypeIds.addAll(
          station
            .getVehicleTypesCapacity()
            .stream()
            .map(GBFSVehicleTypesCapacity::getVehicleTypeIds)
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(List::stream)
            .filter(StringUtils::isNotBlank)
            .toList()
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
