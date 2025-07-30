package org.entur.lamassu.leader;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.entur.gbfs.http.GBFSHttpClientEventHandler;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.SubscriptionMonitoringService;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisterIncomingDataEventHandler implements GBFSHttpClientEventHandler {

  private final MetricsService metricsService;
  private final SubscriptionMonitoringService subscriptionMonitoringService;
  private final FeedProviderConfig feedProviderConfig;
  private final Map<URI, URI> feedURItoDiscoveryURI = new HashMap<>();

  public RegisterIncomingDataEventHandler(
    MetricsService metricsService,
    SubscriptionMonitoringService subscriptionMonitoringService,
    FeedProviderConfig feedProviderConfig
  ) {
    this.metricsService = metricsService;
    this.subscriptionMonitoringService = subscriptionMonitoringService;
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

  @Override
  public void registerFeedUri(URI feedUri, URI discoveryUri) {
    feedURItoDiscoveryURI.put(feedUri, discoveryUri);
  }

  private Optional<FeedProvider> findFeedProviderByUrl(URI discoveryUri) {
    final String feedProviderURI = discoveryUri.toString();
    return this.feedProviderConfig.getProviders()
      .stream()
      .filter(fp -> fp.getUrl().equals(feedProviderURI))
      .findFirst();
  }

  private void onGetData(@Nullable Integer httpStatus, URI uri) {
    URI discoveryUri;
    URI feedUri;
    if (feedURItoDiscoveryURI.containsKey(uri)) {
      discoveryUri = feedURItoDiscoveryURI.get(uri);
      feedUri = uri;
    } else {
      feedUri = null;
      discoveryUri = uri;
    }
    if (discoveryUri == null) {
      log.error("Unable to find discovery URI for URI: {}", uri);
      return;
    }
    Optional<FeedProvider> feedProvider = this.findFeedProviderByUrl(discoveryUri);
    feedProvider.ifPresent(fp -> {
      metricsService.registerIncomingData(httpStatus, discoveryUri, fp.getOperatorName());
      subscriptionMonitoringService.sendSubscriptionMonitoringData(
        httpStatus,
        fp,
        discoveryUri
      );
      if (feedUri != null) {
        metricsService.registerIncomingData(httpStatus, feedUri, fp.getOperatorName());
        subscriptionMonitoringService.sendSubscriptionMonitoringData(
          httpStatus,
          fp,
          feedUri
        );
      }
      if (httpStatus != null && httpStatus >= 200 && httpStatus < 300) {
        fp.setLastSuccessfulProducerCall(Instant.now());
      } else {
        fp.setLastFailedProducerCall(Instant.now());
      }
    });
  }
}
