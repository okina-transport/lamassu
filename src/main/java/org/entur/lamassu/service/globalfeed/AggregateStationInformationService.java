package org.entur.lamassu.service.globalfeed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSData;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStationInformation;

public class AggregateStationInformationService extends AggregateFeedDataService {

  public AggregateStationInformationService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    super(feedProviderService, gbfsv3FeedCache, globalFeedConfiguration);
  }

  @Override
  public Object buildGlobalFeed() {
    List<FeedProvider> feedProviders = getFeedProviders();
    GBFSStationInformation stationInformations;
    Date globalLastUpdated = null;
    List<GBFSStation> stationByFeed;
    List<GBFSStation> stationAggregate = new ArrayList<>();
    for (FeedProvider feedProvider : feedProviders) {
      stationInformations =
        gbfsv3FeedCache.find(GBFSFeed.Name.STATION_INFORMATION, feedProvider);
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
      if (StringUtils.isNotBlank(station.getRegionId())) {
        station.setRegionId(originalSystemId);
      }
      String originalStationId = StringUtils.defaultIfBlank(
        station.getStationId(),
        globalFeedConfiguration.getDefaultStationId()
      );
      station.setStationId(aggregateSystemId + ":" + originalStationId);
      updatedStations.add(station);
    }
    return updatedStations;
  }
}
