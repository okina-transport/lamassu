package org.entur.lamassu.service;

import com.google.gson.Gson;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.entur.lamassu.config.KafkaConfig;
import org.entur.lamassu.model.monitoring.SubscriptionMonitoring;
import org.entur.lamassu.model.provider.FeedProvider;
import org.jetbrains.annotations.Nullable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubscriptionMonitoringService {

  private static final Gson GSON = new Gson();
  private final KafkaConfig kafkaConfig;
  private final KafkaTemplate<Integer, String> template;

  public SubscriptionMonitoringService(
    KafkaConfig kafkaConfig,
    KafkaTemplate<Integer, String> template
  ) {
    this.kafkaConfig = kafkaConfig;
    this.template = template;
  }

  public void sendSubscriptionMonitoringData(
    @Nullable Integer httpStatus,
    FeedProvider fp,
    URI producerUrl
  ) {
    if (!kafkaConfig.isKafkaEnabled()) {
      log.info("Kafka is disabled, abort");
      return;
    }
    SubscriptionMonitoring sm = new SubscriptionMonitoring();
    sm.setDataset(fp.getOperatorName().toUpperCase());
    sm.setHttpStatus(httpStatus == null ? "" : httpStatus.toString());
    sm.setProducerUrl(producerUrl.toString());
    Map<String, Object> headers = new HashMap<>();
    log.debug("Send subscription monitoring data to KAFKA: {}", sm);
    headers.put(KafkaHeaders.TOPIC, kafkaConfig.getSubscriptionMonitoringTopic());
    headers.put(
      KafkaConfig.KAFKA_HEADERS_ENV,
      kafkaConfig.getEnvironment().getBytes(StandardCharsets.UTF_8)
    );
    headers.put(
      KafkaConfig.KAFKA_HEADERS_CLIENT,
      kafkaConfig.getClientName().getBytes(StandardCharsets.UTF_8)
    );
    GenericMessage<String> message = new GenericMessage<>(GSON.toJson(sm), headers);
    template.send(message);
  }
}
