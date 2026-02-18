package org.entur.lamassu.service.globalfeed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSData;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSGbfs;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;

public class AggregateGBFSService extends AggregateFeedDataService {

  protected AggregateGBFSService(
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
    List<GBFSFeed> data = new ArrayList<>();
    for (GBFSFeed.Name feed : GBFSFeed.Name.values()) {
      data.add(new GBFSFeed().withName(feed).withUrl(buildFeedUrl(feed, useOriginalId)));
    }
    return new GBFSGbfs()
      .withData(new GBFSData().withFeeds(data))
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(new Date());
  }

  private String buildFeedUrl(GBFSFeed.Name feed, boolean useOriginalId) {
    String feedUrl =
      globalFeedConfiguration.getHostUrl() +
      "/gbfs/v3/aggregate/" +
      gbfsModality.getValue() +
      "/" +
      feed;
    if (useOriginalId) {
      feedUrl += "?useOriginalId=true";
    }
    return feedUrl;
  }
}
