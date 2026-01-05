package org.entur.lamassu.service.globalfeed;

import java.util.Date;
import java.util.List;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs_versions.GBFSVersion;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSData;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSName;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSSystemInformation;

public class AggregateSystemInformationService extends AggregateFeedDataService {

  public AggregateSystemInformationService(
    FeedProviderService feedProviderService,
    GBFSV3FeedCache gbfsv3FeedCache,
    GlobalFeedConfiguration globalFeedConfiguration
  ) {
    super(feedProviderService, gbfsv3FeedCache, globalFeedConfiguration);
  }

  @Override
  public Object buildGlobalFeed() {
    List<FeedProvider> feedProviders = getFeedProviders();
    Date globalLastUpdated = null;
    GBFSSystemInformation systemInformation;
    for (FeedProvider feedProvider : feedProviders) {
      systemInformation =
        gbfsv3FeedCache.find(GBFSFeed.Name.SYSTEM_INFORMATION, feedProvider);
      if (
        systemInformation != null &&
        (
          globalLastUpdated == null ||
          globalLastUpdated.before(systemInformation.getLastUpdated())
        )
      ) {
        globalLastUpdated = systemInformation.getLastUpdated();
      }
    }
    GBFSData data = new GBFSData()
      .withEmail(globalFeedConfiguration.getEmail())
      .withFeedContactEmail(globalFeedConfiguration.getFeedContactEmail())
      .withLanguages(List.of(globalFeedConfiguration.getLanguage()))
      .withName(
        List.of(
          new GBFSName()
            .withLanguage(globalFeedConfiguration.getLanguage())
            .withText(getSystemName())
        )
      )
      .withOpeningHours(globalFeedConfiguration.getOpeningHours())
      .withSystemId(getSystemId())
      .withTimezone(GBFSData.Timezone.fromValue(globalFeedConfiguration.getTimezone()));
    return new GBFSSystemInformation()
      .withData(data)
      .withVersion(GBFSVersion.Version._3_0.value())
      .withTtl(globalFeedConfiguration.getTimeToLive())
      .withLastUpdated(ensureNonNullDate(globalLastUpdated));
  }
}
