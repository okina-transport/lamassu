package org.entur.lamassu.service.globalfeed;

import java.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.*;

public class AggregateGeofencingZonesService extends AggregateFeedDataService {

  public AggregateGeofencingZonesService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    super(feedProviderService, gbfsv3FeedCache, globalFeedConfiguration);
  }

  @Override
  public Object buildGlobalFeed() {
    List<FeedProvider> feedProviders = getFeedProviders();
    GBFSGeofencingZones geofencingZones;
    Date globalLastUpdated = null;
    List<GBFSGlobalRule> rulesAggregate = new ArrayList<>();
    List<GBFSFeature> featureAggregate = new ArrayList<>();
    for (FeedProvider feedProvider : feedProviders) {
      geofencingZones =
        gbfsv3FeedCache.find(GBFSFeed.Name.GEOFENCING_ZONES, feedProvider);
      if (geofencingZones != null) {
        if (
          globalLastUpdated == null ||
          globalLastUpdated.before(geofencingZones.getLastUpdated())
        ) {
          globalLastUpdated = geofencingZones.getLastUpdated();
        }
        readData(geofencingZones, rulesAggregate, featureAggregate);
      }
    }
    return new GBFSGeofencingZones()
      .withData(
        new GBFSData()
          .withGlobalRules(rulesAggregate)
          .withGeofencingZones(
            new GBFSGeofencingZones__1()
              .withFeatures(featureAggregate)
              .withType(GBFSGeofencingZones__1.Type.FEATURE_COLLECTION)
          )
      )
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(ensureNonNullDate(globalLastUpdated));
  }

  private static void readData(
    GBFSGeofencingZones geofencingZones,
    List<GBFSGlobalRule> rulesAggregate,
    List<GBFSFeature> featureAggregate
  ) {
    List<GBFSGlobalRule> rulesByFeed;
    if (geofencingZones.getData() != null) {
      rulesByFeed = geofencingZones.getData().getGlobalRules();
      if (CollectionUtils.isNotEmpty(rulesByFeed)) {
        rulesAggregate.addAll(rulesByFeed);
      }
      GBFSGeofencingZones__1 zoneContent = geofencingZones.getData().getGeofencingZones();
      if (zoneContent != null && CollectionUtils.isNotEmpty(zoneContent.getFeatures())) {
        featureAggregate.addAll(zoneContent.getFeatures());
      }
    }
  }
}
