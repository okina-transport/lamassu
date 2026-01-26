package org.entur.lamassu.service.globalfeed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSData;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSGbfs;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;

public class AggregateGBFSService extends AggregateFeedDataService {

  public AggregateGBFSService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    super(feedProviderService, gbfsv3FeedCache, globalFeedConfiguration);
  }

  @Override
  public Object buildGlobalFeed() {
    List<GBFSFeed> data = new ArrayList<>();
    for (GBFSFeed.Name feed : GBFSFeed.Name.values()) {
      data.add(new GBFSFeed().withName(feed).withUrl(buildFeedUrl(feed)));
    }
    return new GBFSGbfs()
      .withData(new GBFSData().withFeeds(data))
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(new Date());
  }

  private String buildFeedUrl(GBFSFeed.Name feed) {
    return (
      globalFeedConfiguration.getHostUrl() +
      "/gbfs/v3/aggregate/" +
      gbfsModality.getValue() +
      "/" +
      feed
    );
  }
}
