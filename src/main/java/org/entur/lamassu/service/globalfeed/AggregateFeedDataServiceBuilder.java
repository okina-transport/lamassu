package org.entur.lamassu.service.globalfeed;

import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.model.provider.GbfsModality;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;

public class AggregateFeedDataServiceBuilder {

  private final AggregateFeedDataService instance;

  AggregateFeedDataServiceBuilder(
    GBFSFeed.Name gbfsFeed,
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    switch (gbfsFeed) {
      case GBFS_VERSIONS -> instance =
        new AggregateGBFSVersionService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      case SYSTEM_INFORMATION -> instance =
        new AggregateSystemInformationService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      case VEHICLE_TYPES -> instance =
        new AggregateVehicleTypesService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      case STATION_INFORMATION -> instance =
        new AggregateStationInformationService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      case STATION_STATUS -> instance =
        new AggregateStationStatusService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      case VEHICLE_STATUS -> instance =
        new AggregateVehicleStatusService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      case SYSTEM_ALERTS -> instance =
        new AggregateSystemAlertsService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      case SYSTEM_REGIONS -> instance =
        new AggregateSystemRegionsService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      case SYSTEM_PRICING_PLANS -> instance =
        new AggregateSystemPricingPlansService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      case GEOFENCING_ZONES -> instance =
        new AggregateGeofencingZonesService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
      default -> instance =
        new AggregateGBFSService(
          feedProviderService,
          gbfsv3FeedCache,
          globalFeedConfiguration
        );
    }
  }

  public static AggregateFeedDataServiceBuilder init(
    GBFSFeed.Name gbfsFeed,
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    return new AggregateFeedDataServiceBuilder(
      gbfsFeed,
      feedProviderService,
      gbfsv3FeedCache,
      globalFeedConfiguration
    );
  }

  public AggregateFeedDataServiceBuilder withModality(GbfsModality gbfsModality) {
    this.instance.setGbfsModality(gbfsModality);
    return this;
  }

  public AggregateFeedDataService build() {
    return instance;
  }
}
