package org.entur.lamassu.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.entur.lamassu.config.KafkaConfig;
import org.entur.lamassu.model.monitoring.SubscriptionMonitoring;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.utils.KafkaTestUtils;

class SubscriptionMonitoringIntegrationTest extends AbstractIntegrationTestBase {

  private static final Gson GSON = new Gson();

  @Autowired
  private KafkaConfig kafkaConfig;

  @Test
  void testSubscriptionMonitoringDataSentToKafka() {
    // When executing this test, GBFS subscriptions are created based on configured feed providers.
    // It queries provider URLs and shall send subscription monitoring data to KAFKA.
    // Arrange
    ConsumerRecords<String, String> kafkaRecords;
    try (
      KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
        KafkaTestUtils.consumerProps(broker.getBrokersAsString(), "test")
      )
    ) {
      consumer.subscribe(
        Collections.singleton(kafkaConfig.getSubscriptionMonitoringTopic())
      );
      kafkaRecords = consumer.poll(Duration.ofSeconds(1));
    }

    // Act - nothing to act messages shall have been sent to KAFKA already

    // Assert
    List<SubscriptionMonitoring> monitorings = new ArrayList<>();
    kafkaRecords.forEach(r ->
      monitorings.add(GSON.fromJson(r.value(), SubscriptionMonitoring.class))
    );

    assertFalse(
      monitorings.isEmpty(),
      "should retrieve subscription monitoring data from KAFKA"
    );
    assertTrue(
      monitorings
        .stream()
        .anyMatch(sm ->
          "TESTATLANTIS".equals(sm.getDataset()) &&
          "GBFS".equals(sm.getDataType()) &&
          "200".equals(sm.getHttpStatus()) &&
          "http://localhost:8888/testatlantis/gbfs".equals(sm.getProducerUrl())
        ),
      "shall contain ATLANTIS subscription monitoring data"
    );
    assertTrue(
      monitorings
        .stream()
        .anyMatch(sm ->
          "TESTOZON".equals(sm.getDataset()) &&
          "GBFS".equals(sm.getDataType()) &&
          "200".equals(sm.getHttpStatus()) &&
          "http://localhost:8888/testozon/gbfs".equals(sm.getProducerUrl())
        ),
      "shall contain OZON subscription monitoring data"
    );
  }
}
