package org.entur.lamassu.controller;

import org.entur.lamassu.service.FeedProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedproviders")
public class FeedProviderController {

  private final FeedProviderService feedProviderService;

  public FeedProviderController(FeedProviderService feedProviderService) {
    this.feedProviderService = feedProviderService;
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
}
