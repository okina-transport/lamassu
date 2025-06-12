package org.entur.lamassu.config.feedprovider;

import jakarta.annotation.PostConstruct;
import java.util.*;
import org.entur.lamassu.ishtar.IshtarClient;
import org.entur.lamassu.model.provider.FeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@Primary
public class FeedProviderApiConfig implements FeedProviderConfig {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final IshtarClient ishtarClient;

  // Cache des providers
  private List<FeedProvider> cachedProviders;

  @Autowired
  public FeedProviderApiConfig(IshtarClient ishtarClient) {
    this.ishtarClient = ishtarClient;
  }

  @PostConstruct
  public void init() {
    refreshProviders();
  }

  @Override
  public List<FeedProvider> getProviders() {
    return cachedProviders != null ? cachedProviders : Collections.emptyList();
  }

  @Scheduled(fixedRate = 5 * 60 * 1000)
  public void refreshProviders() {
    try {
      this.cachedProviders = ishtarClient.fetchGbfsProviders();
      logger.info("Successfully refreshed {} providers", cachedProviders.size());
    } catch (Exception e) {
      logger.error("Failed to refresh providers", e);
    }
  }
}
