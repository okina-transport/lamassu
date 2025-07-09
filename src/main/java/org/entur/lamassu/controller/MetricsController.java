package org.entur.lamassu.controller;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("leader & !test")
public class MetricsController {

  private final PrometheusMeterRegistry meterRegistry;

  public MetricsController(PrometheusMeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @GetMapping(value = "/scrape", produces = "text/plain")
  public String getMetrics() {
    return meterRegistry.scrape();
  }
}
