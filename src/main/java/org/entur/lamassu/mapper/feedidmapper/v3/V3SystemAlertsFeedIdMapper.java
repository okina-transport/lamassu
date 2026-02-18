package org.entur.lamassu.mapper.feedidmapper.v3;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.entur.lamassu.util.IdMappersUtil;
import org.mobilitydata.gbfs.v3_0.system_alerts.GBFSAlert;
import org.mobilitydata.gbfs.v3_0.system_alerts.GBFSSystemAlerts;
import org.springframework.stereotype.Component;

@Component
public class V3SystemAlertsFeedIdMapper implements FeedIdMapper<GBFSSystemAlerts> {

  private final IdMappingService idMappingService;

  public V3SystemAlertsFeedIdMapper(IdMappingService idMappingService) {
    this.idMappingService = idMappingService;
  }

  @Override
  public void mapIds(
    GBFSSystemAlerts mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (
      mapped == null ||
      mapped.getData() == null ||
      CollectionUtils.isEmpty(mapped.getData().getAlerts())
    ) {
      return;
    }

    Map<String, String> alertIdsMap = buildAlertIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );
    Map<String, String> regionIdsMap = buildRegionIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );
    Map<String, String> stationIdsMap = buildStationIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );

    for (var alert : mapped.getData().getAlerts()) {
      alert.setAlertId(IdMappersUtil.mapId(alert.getAlertId(), alertIdsMap));
      alert.setRegionIds(IdMappersUtil.mapIds(alert.getRegionIds(), regionIdsMap));
      alert.setStationIds(IdMappersUtil.mapIds(alert.getStationIds(), stationIdsMap));
    }
  }

  private Map<String, String> buildAlertIdsMap(
    GBFSSystemAlerts mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> alertIds = mapped
      .getData()
      .getAlerts()
      .stream()
      .map(GBFSAlert::getAlertId)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getAlertIdsSuperToOriginalMap(alertIds, feedProvider)
      : idMappingService.getAlertIdsOriginalToSuperMap(alertIds, feedProvider);
  }

  private Map<String, String> buildRegionIdsMap(
    GBFSSystemAlerts mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> regionIds = mapped
      .getData()
      .getAlerts()
      .stream()
      .map(GBFSAlert::getRegionIds)
      .filter(CollectionUtils::isNotEmpty)
      .flatMap(List::stream)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getRegionIdsSuperToOriginalMap(regionIds, feedProvider)
      : idMappingService.getRegionIdsOriginalToSuperMap(regionIds, feedProvider);
  }

  private Map<String, String> buildStationIdsMap(
    GBFSSystemAlerts mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> stationIds = mapped
      .getData()
      .getAlerts()
      .stream()
      .map(GBFSAlert::getStationIds)
      .filter(CollectionUtils::isNotEmpty)
      .flatMap(List::stream)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getStationIdsSuperToOriginalMap(stationIds, feedProvider)
      : idMappingService.getStationIdsOriginalToSuperMap(stationIds, feedProvider);
  }
}
