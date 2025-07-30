package org.entur.lamassu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.entur.lamassu.config.KafkaConfig;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

@ExtendWith(MockitoExtension.class)
class SubscriptionMonitoringServiceTest {

  @Mock
  private KafkaConfig config;

  @Mock
  private KafkaTemplate<Integer, String> template;

  @InjectMocks
  private SubscriptionMonitoringService tested;

  @Test
  void test_sendSubscriptionMonitoringData_whenKafkaIsDisabled_doNotSendMessageToKafka() {
    // Arrange
    when(config.isKafkaEnabled()).thenReturn(false);

    // Act
    tested.sendSubscriptionMonitoringData(null, null, null);

    // Assert
    verify(template, never()).send(any(), any());
  }

  @ParameterizedTest
  @CsvSource(
    {
      "200,https://www.google.fr",
      "404,https://fr.wiktionary.org/wiki/galinette_cendr%C3%A9e",
      "503,https://www.google.fr",
    }
  )
  @SuppressWarnings("unchecked")
  void test_sendSubscriptionMonitoringData_whenHttpStatusIsNotNull_sendMessageToKafka(
    Integer httpStatus,
    URI producerUrl
  ) {
    // Arrange
    when(config.isKafkaEnabled()).thenReturn(true);
    when(config.getSubscriptionMonitoringTopic()).thenReturn("topic");
    when(config.getClientName()).thenReturn("ara");
    when(config.getEnvironment()).thenReturn("test");

    FeedProvider fp = new FeedProvider();
    fp.setOperatorName("1");

    // Act
    tested.sendSubscriptionMonitoringData(httpStatus, fp, producerUrl);

    JsonElement expectedPayload = JsonParser.parseString(
      "{\"dataset\":\"1\",\"dataType\":\"GBFS\",\"httpStatus\":\"%d\",\"producerUrl\":\"%s\"}".formatted(
          httpStatus,
          producerUrl
        )
    );

    // Assert
    verify(template)
      .send(
        (Message<String>) argThat(arg -> {
          GenericMessage<String> message = (GenericMessage<String>) arg;
          assertEquals(expectedPayload, JsonParser.parseString(message.getPayload()));
          assertEquals("topic", message.getHeaders().get(KafkaHeaders.TOPIC));
          assertEquals(
            "ara",
            new String(
              (byte[]) message.getHeaders().get("client"),
              StandardCharsets.UTF_8
            )
          );
          assertEquals(
            "test",
            new String((byte[]) message.getHeaders().get("env"), StandardCharsets.UTF_8)
          );
          return true;
        })
      );
  }

  @Test
  @SuppressWarnings("unchecked")
  void test_sendSubscriptionMonitoringData_whenHttpStatusIsNull_sendMessageToKafka() {
    // Arrange
    when(config.isKafkaEnabled()).thenReturn(true);
    when(config.getSubscriptionMonitoringTopic()).thenReturn("topic");
    when(config.getClientName()).thenReturn("ara");
    when(config.getEnvironment()).thenReturn("test");

    URI producerUrl = URI.create("https://www.google.fr");
    FeedProvider fp = new FeedProvider();
    fp.setOperatorName("1");

    // Act
    tested.sendSubscriptionMonitoringData(null, fp, producerUrl);

    // Assert
    JsonElement expectedPayload = JsonParser.parseString(
      "{\"dataset\":\"1\",\"dataType\":\"GBFS\",\"httpStatus\":\"\",\"producerUrl\":\"%s\"}".formatted(
          producerUrl
        )
    );

    verify(template)
      .send(
        (Message<String>) argThat(arg -> {
          GenericMessage<String> message = (GenericMessage<String>) arg;
          assertEquals(expectedPayload, JsonParser.parseString(message.getPayload()));
          return true;
        })
      );
  }
}
