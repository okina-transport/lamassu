package org.entur.lamassu.service.globalfeed;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.model.provider.GbfsModality;
import org.entur.lamassu.service.FeedProviderService;

public abstract class AggregateFeedDataService {

  protected final FeedProviderService feedProviderService;

  protected final GBFSV3FeedCache gbfsv3FeedCache;

  protected final GlobalFeedConfiguration globalFeedConfiguration;

  protected final GbfsV3DeliveryMapper gbfsV3DeliveryMapper;

  @Setter
  @Getter
  protected GbfsModality gbfsModality = GbfsModality.GLOBAL;

  protected AggregateFeedDataService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration,
    GbfsV3DeliveryMapper gbfsV3DeliveryMapper
  ) {
    this.feedProviderService = feedProviderService;
    this.gbfsv3FeedCache = gbfsv3FeedCache;
    this.globalFeedConfiguration = globalFeedConfiguration;
    this.gbfsV3DeliveryMapper = gbfsV3DeliveryMapper;
  }

  public abstract Object buildGlobalFeed(boolean useOriginalId);

  protected List<FeedProvider> getFeedProviders() {
    List<FeedProvider> feedProviders = feedProviderService.getFeedProviders();
    if (gbfsModality != GbfsModality.GLOBAL) {
      feedProviders =
        feedProviders
          .stream()
          .filter(feedProvider -> gbfsModality == feedProvider.getGbfsModality())
          .toList();
    }
    return feedProviders;
  }

  protected String getSystemId() {
    return (this.globalFeedConfiguration.getSystemIdPrefix() + gbfsModality.getValue());
  }

  protected String getSystemName() {
    String regionName = globalFeedConfiguration.getSystemName() + " agrégé";
    if (gbfsModality != GbfsModality.GLOBAL) {
      regionName += " par " + gbfsModality.getFrenchLabel();
    }
    return regionName;
  }

  protected Date ensureNonNullDate(Date input) {
    return Objects.requireNonNullElseGet(input, Date::new);
  }
}
