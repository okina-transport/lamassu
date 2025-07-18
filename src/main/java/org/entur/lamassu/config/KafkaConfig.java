package org.entur.lamassu.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class KafkaConfig {

  public static final String KAFKA_HEADERS_ENV = "env";
  public static final String KAFKA_HEADERS_CLIENT = "client";

  private final boolean isKafkaEnabled;
  private final String environment;
  private final String clientName;
  private final String subscriptionMonitoringTopic;

  public KafkaConfig(
    @Value("${fr.okina.lamassu.kafka.enabled:true}") boolean isKafkaEnabled,
    @Value("${fr.okina.lamassu.environment}") String env,
    @Value("${fr.okina.lamassu.clientName}") String clientName,
    @Value(
      "${fr.okina.lamassu.kafka.topics.subscriptionMonitoring:tr_in_subscription_monitoring}"
    ) String subscriptionMonitoringTopic
  ) {
    this.isKafkaEnabled = isKafkaEnabled;
    this.environment = env;
    this.clientName = clientName;
    this.subscriptionMonitoringTopic = subscriptionMonitoringTopic;
  }
}
