package org.entur.lamassu.mapper.feedidmapper.v3;

import io.micrometer.common.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.entur.lamassu.util.IdMappersUtil;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStationStatus;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSVehicleDocksAvailable;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSVehicleTypesAvailable;
import org.springframework.stereotype.Component;

@Component
public class V3StationStatusFeedIdMapper implements FeedIdMapper<GBFSStationStatus> {

  private final IdMappingService idMappingService;

  public V3StationStatusFeedIdMapper(IdMappingService idMappingService) {
    this.idMappingService = idMappingService;
  }

  @Override
  public void mapIds(
    GBFSStationStatus mapped,
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

    Map<String, String> vehicleTypeIdsMap = buildVehicleTypeIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );

    for (var station : mapped.getData().getStations()) {
      station.setStationId(IdMappersUtil.mapId(station.getStationId(), stationIdsMap));
      if (CollectionUtils.isNotEmpty(station.getVehicleTypesAvailable())) {
        station
          .getVehicleTypesAvailable()
          .forEach(vta ->
            vta.setVehicleTypeId(
              IdMappersUtil.mapId(vta.getVehicleTypeId(), vehicleTypeIdsMap)
            )
          );
      }
      if (CollectionUtils.isNotEmpty(station.getVehicleDocksAvailable())) {
        station
          .getVehicleDocksAvailable()
          .forEach(vda ->
            vda.setVehicleTypeIds(
              IdMappersUtil.mapIds(vda.getVehicleTypeIds(), vehicleTypeIdsMap)
            )
          );
      }
    }
  }

  private Map<String, String> buildStationIdsMap(
    GBFSStationStatus mapped,
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

  private Map<String, String> buildVehicleTypeIdsMap(
    GBFSStationStatus mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> vehicleTypeIds = mapped
      .getData()
      .getStations()
      .stream()
      .map(GBFSStation::getVehicleTypesAvailable)
      .filter(CollectionUtils::isNotEmpty)
      .flatMap(List::stream)
      .map(GBFSVehicleTypesAvailable::getVehicleTypeId)
      .collect(Collectors.toSet());

    vehicleTypeIds.addAll(
      mapped
        .getData()
        .getStations()
        .stream()
        .map(GBFSStation::getVehicleDocksAvailable)
        .filter(CollectionUtils::isNotEmpty)
        .flatMap(List::stream)
        .map(GBFSVehicleDocksAvailable::getVehicleTypeIds)
        .filter(CollectionUtils::isNotEmpty)
        .flatMap(List::stream)
        .collect(Collectors.toSet())
    );

    return toOriginalId
      ? idMappingService.getVehicleTypeIdsSuperToOriginalMap(vehicleTypeIds, feedProvider)
      : idMappingService.getVehicleTypeIdsOriginalToSuperMap(
        vehicleTypeIds,
        feedProvider
      );
  }
}
