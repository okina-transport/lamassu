package org.entur.lamassu.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.GlobalFeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.junit.Before;
import org.junit.Test;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSData;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSGbfs;
import org.springframework.web.server.ResponseStatusException;

public class GBFSV3FeedControllerTest {

  public static final String KNOWN_SYSTEM_ID = "knownSystem";
  private GBFSV3FeedController feedController;
  private FeedProviderService mockedFeedProviderService;
  private GlobalFeedProviderService mockedGlobalFeedProviderService;
  private GlobalFeedConfiguration mockedGlobalFeedConfiguration;
  private GbfsV3DeliveryMapper mockedDeliveryMapper;

  private GBFSV3FeedCache mockedFeedCache;

  @Before
  public void before() {
    SystemDiscoveryService systemDiscoveryService = mock(SystemDiscoveryService.class);
    mockedFeedCache = mock(GBFSV3FeedCache.class);
    mockedFeedProviderService = mock(FeedProviderService.class);
    mockedGlobalFeedConfiguration = mock(GlobalFeedConfiguration.class);
    mockedGlobalFeedProviderService = mock(GlobalFeedProviderService.class);
    mockedDeliveryMapper = mock(GbfsV3DeliveryMapper.class);

    feedController =
      new GBFSV3FeedController(
        systemDiscoveryService,
        mockedFeedCache,
        mockedFeedProviderService,
        mockedGlobalFeedProviderService,
        mockedGlobalFeedConfiguration,
        mockedDeliveryMapper
      );
  }

  @Test
  public void throws400OnNonGBFSFeedRequest() {
    assertThrows(
      ResponseStatusException.class,
      () -> feedController.getV3Feed("anySystem", "no-gbfs-feed", false),
      "400 BAD_REQUEST"
    );
  }

  @Test
  public void throws404OnNonConfiguredSystemRequest() {
    assertThrows(
      ResponseStatusException.class,
      () -> feedController.getV3Feed("unknownSystem", "gbfs", false),
      "404 NOT_FOUND"
    );
  }

  @Test
  public void throws502OnConfiguredSystemButUnavailableFeedRequest() {
    FeedProvider feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);

    assertThrows(
      UpstreamFeedNotYetAvailableException.class,
      () -> feedController.getV3Feed(KNOWN_SYSTEM_ID, "gbfs", false)
    );
  }

  @Test
  public void throws404OnConfiguredSystemButUndeclaredFeedRequest() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    var gbfs = createDiscoveryFileWithFeed(GBFSFeed.Name.GBFS);

    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);
    when(mockedFeedCache.find(GBFSFeed.Name.GBFS, feedProvider)).thenReturn(gbfs);
    when(mockedFeedCache.find(GBFSFeed.Name.GEOFENCING_ZONES, feedProvider))
      .thenReturn(null);

    assertThrows(
      ResponseStatusException.class,
      () -> feedController.getV3Feed(KNOWN_SYSTEM_ID, "geofencing_zones", false),
      "404 NOT_FOUND"
    );
  }

  @Test
  public void throws502OnConfiguredSystemAndDeclaredFeedRequest() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    var gbfs = createDiscoveryFileWithFeed(GBFSFeed.Name.GEOFENCING_ZONES);

    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);
    when(mockedFeedCache.find(GBFSFeed.Name.GBFS, feedProvider)).thenReturn(gbfs);
    when(mockedFeedCache.find(GBFSFeed.Name.GEOFENCING_ZONES, feedProvider))
      .thenReturn(null);

    assertThrows(
      UpstreamFeedNotYetAvailableException.class,
      () -> feedController.getV3Feed(KNOWN_SYSTEM_ID, "geofencing_zones", false)
    );
  }

  @Test
  public void throws502OnConfiguredSystemAndMalformedDiscoveryFeedRequest() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    // GBFS is malformed, as it has no feeds defined
    var gbfs = new GBFSGbfs();

    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);
    when(mockedFeedCache.find(GBFSFeed.Name.GBFS, feedProvider)).thenReturn(gbfs);
    when(mockedFeedCache.find(GBFSFeed.Name.GEOFENCING_ZONES, feedProvider))
      .thenReturn(null);

    assertThrows(
      UpstreamFeedNotYetAvailableException.class,
      () -> feedController.getV3Feed(KNOWN_SYSTEM_ID, "geofencing_zones", false)
    );
  }

  public GBFSGbfs createDiscoveryFileWithFeed(GBFSFeed.Name feedName) {
    var gbfs = new GBFSGbfs();
    var data = new GBFSData();
    var feed = new GBFSFeed().withName(feedName);
    data.setFeeds(List.of(feed));
    gbfs.setData(data);
    return gbfs;
  }
}
