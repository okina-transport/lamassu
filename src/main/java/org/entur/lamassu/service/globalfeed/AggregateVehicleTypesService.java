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
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSData;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleType;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleTypes;

public class AggregateVehicleTypesService extends AggregateFeedDataService {

  public AggregateVehicleTypesService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    super(feedProviderService, gbfsv3FeedCache, globalFeedConfiguration);
  }

  @Override
  public Object buildGlobalFeed() {
    List<FeedProvider> feedProviders = getFeedProviders();
    GBFSVehicleTypes vehicleTypes;
    Date globalLastUpdated = null;
    List<GBFSVehicleType> vehicleTypesByFeed;
    List<GBFSVehicleType> vehicleTypesAggregate = new ArrayList<>();
    for (FeedProvider feedProvider : feedProviders) {
      vehicleTypes = gbfsv3FeedCache.find(GBFSFeed.Name.VEHICLE_TYPES, feedProvider);
      if (vehicleTypes != null) {
        if (
          globalLastUpdated == null ||
          globalLastUpdated.before(vehicleTypes.getLastUpdated())
        ) {
          globalLastUpdated = vehicleTypes.getLastUpdated();
        }
        if (vehicleTypes.getData() != null) {
          vehicleTypesByFeed = vehicleTypes.getData().getVehicleTypes();
          if (CollectionUtils.isNotEmpty(vehicleTypesByFeed)) {
            vehicleTypesAggregate.addAll(vehicleTypesByFeed);
          }
        }
      }
    }
    return new GBFSVehicleTypes()
      .withData(new GBFSData().withVehicleTypes(vehicleTypesAggregate))
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(ensureNonNullDate(globalLastUpdated));
  }
}
