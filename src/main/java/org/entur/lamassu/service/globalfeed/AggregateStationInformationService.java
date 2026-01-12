package org.entur.lamassu.service.globalfeed;

import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSData;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStationInformation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AggregateStationInformationService extends AggregateFeedDataService {

  protected AggregateStationInformationService(
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
    GBFSStationInformation stationInformations;
    Date globalLastUpdated = null;
    List<GBFSStation> stationByFeed;
    List<GBFSStation> stationAggregate = new ArrayList<>();
    for (FeedProvider feedProvider : feedProviders) {
      stationInformations =
        gbfsv3FeedCache.find(GBFSFeed.Name.STATION_INFORMATION, feedProvider);
      if (useOriginalId) {
        stationInformations =
          (GBFSStationInformation) gbfsV3DeliveryMapper.mapSingleGbfsFeed(
            stationInformations,
            feedProvider,
            true
          );
      }
      if (stationInformations != null) {
        if (
          globalLastUpdated == null ||
          globalLastUpdated.before(stationInformations.getLastUpdated())
        ) {
          globalLastUpdated = stationInformations.getLastUpdated();
        }
        if (CollectionUtils.isNotEmpty(stationInformations.getData().getStations())) {
          stationByFeed = stationInformations.getData().getStations();
          stationAggregate.addAll(
            updateRegionAndSystemId(feedProvider.getSystemId(), stationByFeed)
          );
        }
      }
    }
    return new GBFSStationInformation()
      .withData(new GBFSData().withStations(stationAggregate))
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(ensureNonNullDate(globalLastUpdated));
  }

  private List<GBFSStation> updateRegionAndSystemId(
    String originalSystemId,
    List<GBFSStation> stationByFeed
  ) {
    String aggregateSystemId = getSystemId();
    List<GBFSStation> updatedStations = new ArrayList<>(stationByFeed.size());
    for (GBFSStation station : stationByFeed) {
      station.setRegionId(originalSystemId);
      station.setStationId(aggregateSystemId + ":" + station.getStationId());
      updatedStations.add(station);
    }
    return updatedStations;
  }
}
