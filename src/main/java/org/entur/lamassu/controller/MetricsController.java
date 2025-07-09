package org.entur.lamassu.controller;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.ws.rs.Produces;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("leader & !test")
public class MetricsController {

  private final PrometheusMeterRegistry meterRegistry;

  public MetricsController(PrometheusMeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @GetMapping("/scrape")
  @Produces("text/plain")
  public String getMetrics() {
    return meterRegistry.scrape();
  }
}
