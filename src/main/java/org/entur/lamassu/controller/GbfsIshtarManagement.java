package org.entur.lamassu.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.apache.commons.collections.CollectionUtils;
import org.entur.gbfs.GbfsSubscriptionManager;
import org.entur.lamassu.config.feedprovider.FeedProviderApiConfig;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.FeedUpdater;
import org.entur.lamassu.model.provider.Authentication;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping({ "/gbfs-ishtar-management"})
@Tag(name = "GbfsIshtarManagement", description = "API de GbfsIshtarManagement")
public class GbfsIshtarManagement {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
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
      FeedProvider existing = feedProviderService.findSubscriptionBySystemId(systemId);
      if (existing == null) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "The subscription has not been activated, so unsubscribing is not necessary."
        ));
      }
      subscriptionManager.unsubscribe(systemId);
      feedProviderService.deleteFeedProvider(systemId);
      return ResponseEntity.ok(Map.of(
              "success", true,
              "message", "Unsubscribe successfully"
      ));
    } catch (IllegalArgumentException e) {
      logger.error("Error deleting provider {}", systemId, e);
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
      logger.error("Error synchronizing", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of(
                      "success", false,
                      "error", e.getMessage()
              ));
    }
  }

  private boolean hasChanged(FeedProvider existing, FeedProvider newProvider) {
    if (existing == null || newProvider == null) {
      return true;
    }

    boolean basicFieldsChanged = !Objects.equals(existing.getOperatorId(), newProvider.getOperatorId())
            || !Objects.equals(existing.getOperatorName(), newProvider.getOperatorName())
            || !Objects.equals(existing.getCodespace(), newProvider.getCodespace())
            || !Objects.equals(existing.getUrl(), newProvider.getUrl())
            || !Objects.equals(existing.getLanguage(), newProvider.getLanguage())
            || !Objects.equals(existing.getAggregate(), newProvider.getAggregate())
            || !Objects.equals(existing.getVersion(), newProvider.getVersion());

    if (basicFieldsChanged) {
      return true;
    }

    boolean collectionsChanged = !listsEqual(existing.getVehicleTypes(), newProvider.getVehicleTypes())
            || !listsEqual(existing.getExcludeFeeds(), newProvider.getExcludeFeeds())
            || !listsEqual(existing.getPricingPlans(), newProvider.getPricingPlans());

    if (collectionsChanged) {
      return true;
    }

    Authentication existingAuth = existing.getAuthentication();
    Authentication newAuth = newProvider.getAuthentication();

    if (existingAuth == null && newAuth == null) {
      return false;
    }
    if (existingAuth == null || newAuth == null) {
      return true;
    }

    return !Objects.equals(existingAuth.getRequestAuthenticator(), newAuth.getRequestAuthenticator())
            || !propertiesEqual(existingAuth.getProperties(), newAuth.getProperties());
  }

  // Méthode utilitaire pour comparer deux collections en gérant les null
  private boolean listsEqual(List<?> col1, List<?> col2) {
    if (col1 == null && col2 == null) {
      return true;
    }
    if (col1 == null || col2 == null) {
      return false;
    }
    return CollectionUtils.isEqualCollection(col1, col2);
  }

  // Méthode utilitaire pour comparer les propriétés d'authentification
  private boolean propertiesEqual(Map<String, String> props1, Map<String, String> props2) {
    if (props1 == null && props2 == null) {
      return true;
    }
    if (props1 == null || props2 == null) {
      return false;
    }
    return props1.equals(props2);
  }
}
