package org.entur.lamassu.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.entur.gbfs.GbfsSubscriptionManager;
import org.entur.lamassu.config.feedprovider.FeedProviderApiConfig;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.FeedUpdater;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping({ "/gbfs-ishtar-management"})
@Tag(name = "GbfsIshtarManagement", description = "API de GbfsIshtarManagement")
public class GbfsIshtarManagement {

  private final GbfsSubscriptionManager subscriptionManager;
  private final FeedProviderService feedProviderService;
  private final FeedUpdater feedUpdater;
  private final FeedProviderConfig feedProviderConfig;

  @Autowired
  public GbfsIshtarManagement(GbfsSubscriptionManager subscriptionManager, FeedProviderService feedProviderService, FeedUpdater feedUpdater, FeedProviderConfig feedProviderConfig) {
      this.subscriptionManager = subscriptionManager;
      this.feedProviderService = feedProviderService;
      this.feedUpdater = feedUpdater;
      this.feedProviderConfig = feedProviderConfig;
  }

  @DeleteMapping("/unsubscribe/{systemId}")
  public ResponseEntity<Map<String, Object>> deleteProvider(@PathVariable String systemId) {
    try {
      subscriptionManager.unsubscribe(systemId);
      feedProviderService.deleteFeedProvider(systemId);
      return ResponseEntity.ok(Map.of(
              "success", true,
              "message", "Unsubscribe successfully"
      ));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(Map.of(
                      "success", false,
                      "error", e.getMessage()
              ));
    }
  }

  @PostMapping("/synchronize")
  public ResponseEntity<Map<String, Object>> synchronize() {
    try {
      // Forcer le rafraîchissement du cache avant la synchronisation
      if (feedProviderConfig instanceof FeedProviderApiConfig) {
        ((FeedProviderApiConfig) feedProviderConfig).refreshProviders();
      }

      List<FeedProvider> latestProviders = feedProviderConfig.getProviders();

      latestProviders.forEach(newProvider -> {
        String systemId = newProvider.getSystemId();
        FeedProvider existing = feedProviderService.findSubscriptionBySystemId(systemId);

        if (existing == null) {
          feedUpdater.createSubscription(newProvider);
        } else if (hasChanged(existing, newProvider)) {
          subscriptionManager.unsubscribe(systemId);
          feedProviderService.deleteFeedProvider(systemId);
          feedUpdater.createSubscription(newProvider);
        }
      });

      return ResponseEntity.ok(Map.of(
              "success", true,
              "message", "Synchronized successfully"
      ));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of(
                      "success", false,
                      "error", e.getMessage()
              ));
    }
  }

  private boolean hasChanged(FeedProvider existing, FeedProvider newProvider) {
    return !Objects.equals(existing.getOperatorId(), newProvider.getOperatorId())
            || !Objects.equals(existing.getOperatorName(), newProvider.getOperatorName())
            || !Objects.equals(existing.getCodespace(), newProvider.getCodespace())
            || !Objects.equals(existing.getUrl(), newProvider.getUrl())
            || !Objects.equals(existing.getLanguage(), newProvider.getLanguage())
            || !Objects.equals(existing.getAggregate(), newProvider.getAggregate())
            || !Objects.equals(existing.getVersion(), newProvider.getVersion())
            || areListsEqual(existing.getVehicleTypes(), newProvider.getVehicleTypes())
            || areListsEqual(existing.getExcludeFeeds(), newProvider.getExcludeFeeds())
            || areListsEqual(existing.getPricingPlans(), newProvider.getPricingPlans());
  }

  /**
   * Compare deux listes en gérant les cas null et l'ordre des éléments.
   */
  private boolean areListsEqual(List<?> list1, List<?> list2) {
    if (list1 == null && list2 == null) return false;
    if (list1 == null || list2 == null) return true;
    return list1.size() != list2.size()
            || !new HashSet<>(list1).equals(new HashSet<>(list2));
  }
}
