package org.entur.lamassu.config;

import org.entur.gbfs.GbfsSubscriptionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GbfsConfig {

  @Bean
  public GbfsSubscriptionManager gbfsSubscriptionManager() {
    // Initialize GbfsSubscriptionManager with required dependencies (if any)
    return new GbfsSubscriptionManager();
  }
}
