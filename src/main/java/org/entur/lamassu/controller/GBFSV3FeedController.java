/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.v3.GlobalFeedConfiguration;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.model.provider.GbfsModality;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.GlobalFeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.CacheUtil;
import org.jetbrains.annotations.NotNull;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSGbfs;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSManifest;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({ "/gbfs/v3" })
public class GBFSV3FeedController {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${org.entur.lamassu.gbfs.cache-control.minimum-ttl:0}")
  private int cacheControlMinimumTtl;

  private final SystemDiscoveryService systemDiscoveryService;
  private final FeedProviderService feedProviderService;
  private final GBFSV3FeedCache v3FeedCache;
  private final GlobalFeedProviderService globalFeedProviderService;
  private final GlobalFeedConfiguration globalFeedConfiguration;
  private final GbfsV3DeliveryMapper deliveryMapper;

  @Autowired
  public GBFSV3FeedController(
    SystemDiscoveryService systemDiscoveryService,
    GBFSV3FeedCache v3FeedCache,
    FeedProviderService feedProviderService,
    GlobalFeedProviderService globalFeedProviderService,
    GlobalFeedConfiguration globalFeedConfiguration,
    GbfsV3DeliveryMapper deliveryMapper
  ) {
    this.v3FeedCache = v3FeedCache;
    this.systemDiscoveryService = systemDiscoveryService;
    this.feedProviderService = feedProviderService;
    this.globalFeedProviderService = globalFeedProviderService;
    this.globalFeedConfiguration = globalFeedConfiguration;
    this.deliveryMapper = deliveryMapper;
  }

  @GetMapping({ "", "/" })
  public ResponseEntity<SystemDiscovery> getFeedProviderDiscovery(
    @RequestParam(name = "useOriginalId", defaultValue = "false") boolean useOriginalId
  ) {
    var data = systemDiscoveryService.getSystemDiscovery(
      GBFSVersion.Version._3_0,
      useOriginalId
    );
    return ResponseEntity
      .ok()
      .cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic())
      .body(data);
  }

  @GetMapping("/manifest.json")
  public ResponseEntity<GBFSManifest> getV3Manifest(
    @RequestParam(name = "useOriginalId", defaultValue = "false") boolean useOriginalId
  ) {
    var manifest = systemDiscoveryService.getGBFSManifest(useOriginalId);

    return ResponseEntity
      .ok()
      .cacheControl(CacheControl.maxAge(3600, TimeUnit.MINUTES).cachePublic())
      .body(manifest);
  }

  @GetMapping(value = { "/{systemId}/{feed}", "/{systemId}/{feed}.json" })
  public ResponseEntity<Object> getV3Feed(
    @PathVariable String systemId,
    @PathVariable String feed,
    @RequestParam(name = "useOriginalId", defaultValue = "false") boolean useOriginalId
  ) {
    try {
      var feedName = GBFSFeed.Name.fromValue(feed);

      var data = getFeed(systemId, feed, useOriginalId);

      return ResponseEntity
        .ok()
        .cacheControl(
          CacheControl
            .maxAge(
              CacheUtil.getMaxAge(
                org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeedName.implementingClass(feedName),
                data,
                systemId,
                feed,
                (int) Instant.now().getEpochSecond(),
                cacheControlMinimumTtl
              ),
              TimeUnit.SECONDS
            )
            .cachePublic()
        )
        .lastModified(
          CacheUtil.getLastModified(
            org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeedName.implementingClass(feedName),
            data,
            systemId,
            feed
          )
        )
        .body(data);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping(value = { "/aggregate/{mode}/{feed}", "/aggregate/{mode}/{feed}.json" })
  public ResponseEntity<Object> getV3FeedAggregated(
    @PathVariable GbfsModality mode,
    @PathVariable String feed,
    @RequestParam(name = "useOriginalId", defaultValue = "false") boolean useOriginalId
  ) {
    if (globalFeedConfiguration.isEnabled()) {
      return ResponseEntity
        .ok()
        .body(globalFeedProviderService.getGlobalFeed(mode, feed, useOriginalId));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
  }

  @NotNull
  protected Object getFeed(String systemId, String feed, boolean useOriginalId) {
    LocalDateTime start = LocalDateTime.now();
    var feedName = GBFSFeed.Name.fromValue(feed);
    var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);

    if (feedProvider == null) {
      throw new NoSuchElementException();
    }

    var data = v3FeedCache.find(feedName, feedProvider);

    if (data == null) {
      throwsIfFeedCouldOrShouldExist(feedName, feedProvider);
      throw new NoSuchElementException();
    }

    if (useOriginalId) {
      data = deliveryMapper.mapSingleGbfsFeed(data, feedProvider, true);
    }

    LocalDateTime end = LocalDateTime.now();
    double duration = ChronoUnit.MILLIS.between(start, end) * 0.001;
    logger.debug(
      "GBFS stream playback time on a customer call : {}  : {}s",
      feedProvider.getSystemId(),
      duration
    );
    return data;
  }

  /*
    Throws an UpstreamFeedNotYetAvailableException, if either the discoveryFile (gbf file) is not yet cached,
    the requested feed is published in the discovery file, or the discovery file is malformed.
   */
  protected void throwsIfFeedCouldOrShouldExist(
    GBFSFeed.Name feedName,
    FeedProvider feedProvider
  ) {
    try {
      GBFSGbfs discoveryFile = v3FeedCache.find(GBFSFeed.Name.GBFS, feedProvider);
      if (
        discoveryFile == null ||
        discoveryFile
          .getData()
          .getFeeds()
          .stream()
          .map(GBFSFeed::getName)
          .anyMatch(name -> name.equals(feedName))
      ) {
        throw new UpstreamFeedNotYetAvailableException();
      }
    } catch (NullPointerException e) {
      // in case the gbfs is malformed, e.g. no languages are defined, or no feeds,
      // this is an upstream error and the requested feed might exist
      throw new UpstreamFeedNotYetAvailableException();
    }
  }
}
