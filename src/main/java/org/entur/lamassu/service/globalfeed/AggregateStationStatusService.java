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
import org.mobilitydata.gbfs.v3_0.station_status.GBFSData;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStationStatus;

public class AggregateStationStatusService extends AggregateFeedDataService {

  protected AggregateStationStatusService(
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
    GBFSStationStatus stationStatus;
    Date globalLastUpdated = null;
    List<GBFSStation> stationStatusByFeed;
    List<GBFSStation> stationStatusAggregate = new ArrayList<>();
    for (FeedProvider feedProvider : feedProviders) {
      stationStatus = gbfsv3FeedCache.find(GBFSFeed.Name.STATION_STATUS, feedProvider);
      if (useOriginalId) {
        stationStatus =
          (GBFSStationStatus) gbfsV3DeliveryMapper.mapSingleGbfsFeed(
            stationStatus,
            feedProvider,
            true
          );
      }
      if (stationStatus != null) {
        if (
          globalLastUpdated == null ||
          globalLastUpdated.before(stationStatus.getLastUpdated())
        ) {
          globalLastUpdated = stationStatus.getLastUpdated();
        }
        if (stationStatus.getData() != null) {
          stationStatusByFeed = stationStatus.getData().getStations();
          if (CollectionUtils.isNotEmpty(stationStatusByFeed)) {
            stationStatusAggregate.addAll(stationStatusByFeed);
          }
        }
      }
    }
    return new GBFSStationStatus()
      .withData(new GBFSData().withStations(stationStatusAggregate))
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(ensureNonNullDate(globalLastUpdated));
  }
}
