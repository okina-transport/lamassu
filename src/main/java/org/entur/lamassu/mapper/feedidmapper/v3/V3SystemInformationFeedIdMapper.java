package org.entur.lamassu.mapper.feedidmapper.v3;

import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSSystemInformation;
import org.springframework.stereotype.Component;

@Component
public class V3SystemInformationFeedIdMapper
  implements FeedIdMapper<GBFSSystemInformation> {

  private final IdMappingService idMappingService;

  public V3SystemInformationFeedIdMapper(IdMappingService idMappingService) {
    this.idMappingService = idMappingService;
  }

  @Override
  public void mapIds(
    GBFSSystemInformation mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (mapped.getData() == null) {
      return;
    }
    if (toOriginalId) {
      mapped
        .getData()
        .setSystemId(
          idMappingService.getSystemIdSuperToOriginal(
            mapped.getData().getSystemId(),
            feedProvider
          )
        );
    } else {
      mapped
        .getData()
        .setSystemId(
          idMappingService.getSystemIdOriginalToSuper(
            mapped.getData().getSystemId(),
            feedProvider
          )
        );
    }
  }
}
