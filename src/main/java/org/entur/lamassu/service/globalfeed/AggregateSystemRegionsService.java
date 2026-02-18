package org.entur.lamassu.service.globalfeed;

import java.util.Date;
import java.util.List;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;
import org.mobilitydata.gbfs.v3_0.system_regions.GBFSName;
import org.mobilitydata.gbfs.v3_0.system_regions.GBFSRegion;
import org.mobilitydata.gbfs.v3_0.system_regions.GBFSSystemRegions;

public class AggregateSystemRegionsService extends AggregateFeedDataService {

  protected AggregateSystemRegionsService(
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
    List<FeedProvider> feedProviders = getFeedProviders();
    String regionName = getSystemName();
    String language = globalFeedConfiguration.getLanguage();
    List<GBFSRegion> gbfsRegion = feedProviders
      .stream()
      .map(item ->
        new GBFSRegion()
          .withRegionId(item.getSystemId())
          .withName(List.of(new GBFSName().withLanguage(language).withText(regionName)))
      )
      .toList();
    org.mobilitydata.gbfs.v3_0.system_regions.GBFSData data =
      new org.mobilitydata.gbfs.v3_0.system_regions.GBFSData().withRegions(gbfsRegion);
    return new GBFSSystemRegions()
      .withData(data)
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(new Date());
  }
}
