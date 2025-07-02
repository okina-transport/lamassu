package org.entur.lamassu.controller;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.mapper.feedprovider.FeedProviderStatusMapper;
import org.entur.lamassu.model.provider.FeedProviderStatus;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedproviders")
@Profile("leader")
public class FeedProviderController {

  private final FeedProviderService feedProviderService;
  private final FeedProviderStatusMapper feedProviderStatusMapper;

  public FeedProviderController(
    FeedProviderService feedProviderService,
    FeedProviderStatusMapper feedProviderStatusMapper
  ) {
    this.feedProviderService = feedProviderService;
    this.feedProviderStatusMapper = feedProviderStatusMapper;
  }

  @GetMapping("/synchronize")
  public ResponseEntity<Void> synchronize() {
    feedProviderService.refreshFeedProviders();
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{systemId}")
  public ResponseEntity<Void> deleteFeedProviderBySystemId(
    @PathVariable("systemId") String systemId
  ) {
    feedProviderService.deleteFeedProviderBySystemId(systemId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/dailyStatuses")
  public List<FeedProviderStatus> getFeedProviderStatuses() {
    return CollectionUtils
      .emptyIfNull(feedProviderService.getFeedProviders())
      .stream()
      .map(feedProviderStatusMapper::mapFeedProvider)
      .toList();
  }
}
