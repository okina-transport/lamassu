package org.entur.lamassu.controller;

import static org.entur.lamassu.metrics.MetricsService.METRIC_INCOMING_DATA_MONITORING;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.Set;
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
  public String getMetrics() {
    // to filter by metric name, you must replace dots "." with underscores "_"
    // no need to add "_total" suffix
    return meterRegistry.scrape(
      "text/plain;charset=UTF-8",
      Set.of(METRIC_INCOMING_DATA_MONITORING.replace('.', '_'))
    );
  }
}
