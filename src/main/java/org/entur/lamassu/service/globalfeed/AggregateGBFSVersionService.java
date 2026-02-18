package org.entur.lamassu.service.globalfeed;

import java.util.Date;
import java.util.List;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSData;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSGbfsVersions;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;

public class AggregateGBFSVersionService extends AggregateFeedDataService {

  protected AggregateGBFSVersionService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration,
    GbfsV3DeliveryMapper gbfsV3DeliveryMapper
  ) {
    super(
      feedProviderService,
      gbfsv3FeedCache,
      globalFeedConfiguration,
      gbfsV3DeliveryMapper
    );
  }

  @Override
  public Object buildGlobalFeed(boolean useOriginalId) {
    return new GBFSGbfsVersions()
      .withData(
        new GBFSData()
          .withVersions(
            List.of(
              new GBFSVersion()
                .withVersion(GBFSVersion.Version._3_0)
                .withUrl(globalFeedConfiguration.getHostUrl())
            )
          )
      )
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(new Date());
  }
}
