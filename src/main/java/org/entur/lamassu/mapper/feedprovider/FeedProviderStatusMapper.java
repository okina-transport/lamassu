package org.entur.lamassu.mapper.feedprovider;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.model.provider.FeedProviderStatus;
import org.springframework.stereotype.Component;

@Component
public class FeedProviderStatusMapper {

  private final Clock clock;

  public FeedProviderStatusMapper(Clock clock) {
    this.clock = clock;
  }

  public FeedProviderStatus mapFeedProvider(FeedProvider feedProvider) {
    FeedProviderStatus feedProviderStatus = new FeedProviderStatus();
    feedProviderStatus.setId(feedProvider.getSystemId());
    feedProviderStatus.setDataset(feedProvider.getCodespace());
    feedProviderStatus.setUrl(feedProvider.getUrl());
    FeedProviderStatus.DailyStatus dailyStatus = FeedProviderStatus.DailyStatus.GREEN;
    Instant todayAtMidnight = clock.instant().truncatedTo(ChronoUnit.DAYS);
    Instant lastSuccess = null;
    Instant lastFailure = null;
    if (feedProvider.getLastSuccessfulProducerCall() != null) {
      lastSuccess = feedProvider.getLastSuccessfulProducerCall();
    }
    if (feedProvider.getLastFailedProducerCall() != null) {
      lastFailure = feedProvider.getLastFailedProducerCall();
    }
    if (lastSuccess != null && lastFailure != null) {
      if (lastFailure.isAfter(lastSuccess) && lastFailure.isAfter(todayAtMidnight)) {
        dailyStatus = FeedProviderStatus.DailyStatus.RED;
      } else if (
        lastSuccess.isAfter(todayAtMidnight) && lastFailure.isAfter(todayAtMidnight)
      ) {
        dailyStatus = FeedProviderStatus.DailyStatus.ORANGE;
      }
    } else if (lastFailure != null && lastFailure.isAfter(todayAtMidnight)) {
      dailyStatus = FeedProviderStatus.DailyStatus.RED;
    } else if (lastSuccess == null && lastFailure == null) {
      dailyStatus = FeedProviderStatus.DailyStatus.UNKNOWN;
    }
    feedProviderStatus.setDailyStatus(dailyStatus);
    return feedProviderStatus;
  }
}
