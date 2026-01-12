package org.entur.lamassu.mapper.feedidmapper.v3;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.entur.lamassu.util.IdMappersUtil;
import org.mobilitydata.gbfs.v3_0.system_regions.GBFSRegion;
import org.mobilitydata.gbfs.v3_0.system_regions.GBFSSystemRegions;
import org.springframework.stereotype.Component;

@Component
public class V3SystemRegionsFeedIdMapper implements FeedIdMapper<GBFSSystemRegions> {

  private final IdMappingService idMappingService;

  public V3SystemRegionsFeedIdMapper(IdMappingService idMappingService) {
    this.idMappingService = idMappingService;
  }

  @Override
  public void mapIds(
    GBFSSystemRegions mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (
      mapped == null ||
      mapped.getData() == null ||
      CollectionUtils.isEmpty(mapped.getData().getRegions())
    ) {
      return;
    }

    Map<String, String> regionIdsMap = buildRegionIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );

    for (var region : mapped.getData().getRegions()) {
      region.setRegionId(IdMappersUtil.mapId(region.getRegionId(), regionIdsMap));
    }
  }

  private Map<String, String> buildRegionIdsMap(
    GBFSSystemRegions mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> regionIds = mapped
      .getData()
      .getRegions()
      .stream()
      .map(GBFSRegion::getRegionId)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());
    return toOriginalId
      ? idMappingService.getRegionIdsSuperToOriginalMap(regionIds, feedProvider)
      : idMappingService.getRegionIdsOriginalToSuperMap(regionIds, feedProvider);
  }
}
