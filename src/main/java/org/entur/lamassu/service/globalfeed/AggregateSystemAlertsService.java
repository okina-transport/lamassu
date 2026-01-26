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
import org.mobilitydata.gbfs.v3_0.system_alerts.GBFSAlert;
import org.mobilitydata.gbfs.v3_0.system_alerts.GBFSData;
import org.mobilitydata.gbfs.v3_0.system_alerts.GBFSSystemAlerts;

public class AggregateSystemAlertsService extends AggregateFeedDataService {

  public AggregateSystemAlertsService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    super(feedProviderService, gbfsv3FeedCache, globalFeedConfiguration);
  }

  @Override
  public Object buildGlobalFeed() {
    List<FeedProvider> feedProviders = getFeedProviders();
    GBFSSystemAlerts systemAlerts;
    Date globalLastUpdated = null;
    List<GBFSAlert> alertByFeed;
    List<GBFSAlert> alertAggregate = new ArrayList<>();
    for (FeedProvider feedProvider : feedProviders) {
      systemAlerts = gbfsv3FeedCache.find(GBFSFeed.Name.SYSTEM_ALERTS, feedProvider);
      if (systemAlerts != null) {
        if (
          globalLastUpdated == null ||
          globalLastUpdated.before(systemAlerts.getLastUpdated())
        ) {
          globalLastUpdated = systemAlerts.getLastUpdated();
        }
        if (systemAlerts.getData() != null) {
          alertByFeed = systemAlerts.getData().getAlerts();
          if (CollectionUtils.isNotEmpty(alertByFeed)) {
            alertAggregate.addAll(alertByFeed);
          }
        }
      }
    }
    return new GBFSSystemAlerts()
      .withData(new GBFSData().withAlerts(alertAggregate))
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(ensureNonNullDate(globalLastUpdated));
  }
}
