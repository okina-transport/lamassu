package org.entur.lamassu.service.globalfeed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSData;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSSystemPricingPlans;

public class AggregateSystemPricingPlansService extends AggregateFeedDataService {

  public AggregateSystemPricingPlansService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    super(feedProviderService, gbfsv3FeedCache, globalFeedConfiguration);
  }

  @Override
  public Object buildGlobalFeed() {
    List<FeedProvider> feedProviders = getFeedProviders();
    GBFSSystemPricingPlans systemPricingPlan;
    Date globalLastUpdated = null;
    List<GBFSPlan> planByFeed;
    List<GBFSPlan> planAggregate = new ArrayList<>();
    for (FeedProvider feedProvider : feedProviders) {
      systemPricingPlan =
        gbfsv3FeedCache.find(GBFSFeed.Name.SYSTEM_PRICING_PLANS, feedProvider);
      if (systemPricingPlan != null) {
        if (
          globalLastUpdated == null ||
          globalLastUpdated.before(systemPricingPlan.getLastUpdated())
        ) {
          globalLastUpdated = systemPricingPlan.getLastUpdated();
        }
        if (systemPricingPlan.getData() != null) {
          planByFeed = systemPricingPlan.getData().getPlans();
          if (CollectionUtils.isNotEmpty(planByFeed)) {
            planAggregate.addAll(planByFeed);
          }
        }
      }
    }
    return new GBFSSystemPricingPlans()
      .withData(new GBFSData().withPlans(planAggregate))
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(ensureNonNullDate(globalLastUpdated));
  }
}
