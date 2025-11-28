package org.entur.lamassu.service;

import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.model.provider.GbfsModality;
import org.entur.lamassu.service.globalfeed.AggregateFeedDataService;
import org.entur.lamassu.service.globalfeed.AggregateFeedDataServiceBuilder;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.springframework.stereotype.Service;

@Service
public class GlobalFeedProviderService {

  private final FeedProviderService feedProviderService;

  private final GBFSV3FeedCache gbfsv3FeedCache;

  private final GlobalFeedConfiguration globalFeedConfiguration;

  public GlobalFeedProviderService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    this.feedProviderService = feedProviderService;
    this.gbfsv3FeedCache = gbfsv3FeedCache;
    this.globalFeedConfiguration = globalFeedConfiguration;
  }

  public Object getGlobalFeed(GbfsModality gbfsModality, String feedName) {
    GBFSFeed.Name gbfsFeed = GBFSFeed.Name.fromValue(feedName);
    AggregateFeedDataService aggregateFeedServiceInstance =
      AggregateFeedDataServiceBuilder
        .init(gbfsFeed, feedProviderService, gbfsv3FeedCache, globalFeedConfiguration)
        .withModality(gbfsModality)
        .build();
    return aggregateFeedServiceInstance.buildGlobalFeed();
  }
}
