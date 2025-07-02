package org.entur.lamassu.leader;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import org.entur.gbfs.http.GBFSHttpClientEventHandler;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.provider.FeedProvider;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class RegisterIncomingDataEventHandler implements GBFSHttpClientEventHandler {

  private final MetricsService metricsService;
  private final FeedProviderConfig feedProviderConfig;

  public RegisterIncomingDataEventHandler(
    MetricsService metricsService,
    FeedProviderConfig feedProviderConfig
  ) {
    this.metricsService = metricsService;
    this.feedProviderConfig = feedProviderConfig;
  }

  @Override
  public void onGetDataSuccess(@Nullable Integer httpStatus, URI uri) {
    onGetData(httpStatus, uri);
  }

  @Override
  public void onGetDataFailure(URI uri) {
    onGetData(null, uri);
  }

  private Optional<FeedProvider> findFeedProviderByUrl(URI uri) {
    return this.feedProviderConfig.getProviders()
      .stream()
      .filter(fp -> fp.getUrl().equals(uri.toString()))
      .findFirst();
  }

  private void onGetData(@Nullable Integer httpStatus, URI uri) {
    Optional<FeedProvider> feedProvider = this.findFeedProviderByUrl(uri);
    feedProvider.ifPresent(fp -> {
      metricsService.registerIncomingData(httpStatus, uri, fp.getCodespace());
      if (httpStatus != null && httpStatus >= 200 && httpStatus < 300) {
        fp.setLastSuccessfulProducerCall(Instant.now());
      } else {
        fp.setLastFailedProducerCall(Instant.now());
      }
    });
  }
}
