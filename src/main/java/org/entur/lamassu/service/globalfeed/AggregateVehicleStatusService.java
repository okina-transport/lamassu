package org.entur.lamassu.service.globalfeed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSData;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicleStatus;

public class AggregateVehicleStatusService extends AggregateFeedDataService {

  protected AggregateVehicleStatusService(
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
    GBFSVehicleStatus vehicleStatus;
    Date globalLastUpdated = null;
    List<GBFSVehicle> vehicleByFeed;
    List<GBFSVehicle> vehicleAggregate = new ArrayList<>();
    for (FeedProvider feedProvider : feedProviders) {
      vehicleStatus = gbfsv3FeedCache.find(GBFSFeed.Name.VEHICLE_STATUS, feedProvider);
      if (useOriginalId) {
        vehicleStatus =
          (GBFSVehicleStatus) gbfsV3DeliveryMapper.mapSingleGbfsFeed(
            vehicleStatus,
            feedProvider,
            true
          );
      }
      if (vehicleStatus != null) {
        if (
          globalLastUpdated == null ||
          globalLastUpdated.before(vehicleStatus.getLastUpdated())
        ) {
          globalLastUpdated = vehicleStatus.getLastUpdated();
        }
        if (vehicleStatus.getData() != null) {
          vehicleByFeed = vehicleStatus.getData().getVehicles();
          if (CollectionUtils.isNotEmpty(vehicleByFeed)) {
            vehicleAggregate.addAll(vehicleByFeed);
          }
        }
      }
    }
    return new GBFSVehicleStatus()
      .withData(new GBFSData().withVehicles(vehicleAggregate))
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(ensureNonNullDate(globalLastUpdated));
  }
}
