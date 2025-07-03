package org.entur.lamassu.mapper.feedprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.networknt.schema.utils.StringUtils;
import java.time.Clock;
import java.time.Instant;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.model.provider.FeedProviderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedProviderStatusMapperTest {

  @Mock
  Clock clock;

  @InjectMocks
  FeedProviderStatusMapper tested;

  @Test
  void test_mapFeedProvider_acceptance() {
    // Arrange
    when(clock.instant()).thenReturn(Instant.parse("2025-06-30T14:48:00Z"));
    FeedProvider input = new FeedProvider();
    input.setSystemId("systemId");
    input.setCodespace("CSP");
    input.setLastSuccessfulProducerCall(null);
    input.setLastFailedProducerCall(null);
    input.setUrl("https://www.google.fr");

    // Act
    var output = tested.mapFeedProvider(input);

    // Assert
    assertEquals("systemId", output.getId(), "should map id properly");
    assertEquals("CSP", output.getDataset(), "should map dataset properly");
    assertEquals("https://www.google.fr", output.getUrl(), "should map url properly");
    assertEquals(
      FeedProviderStatus.DailyStatus.UNKNOWN,
      output.getDailyStatus(),
      "should map daily status " + "properly"
    );
  }

  @ParameterizedTest
  @CsvSource(
    {
      ",,UNKNOWN", // all green when no producer call occurred
      "2025-06-29T14:43:00Z,,GREEN",
      "2025-06-30T14:43:00Z,,GREEN",
      "2025-06-30T14:45:00Z,2025-06-29T14:43:00Z,GREEN",
      "2025-06-30T14:45:00Z,2025-06-30T12:43:00Z,ORANGE",
      ",2025-06-30T12:43:00Z,RED",
      "2025-06-30T12:41:00Z,2025-06-30T12:43:00Z,RED",
      "2025-06-29T12:45:00Z,2025-06-30T12:43:00Z,RED",
    }
  )
  void test_mapFeedProvider_whenLastSuccessfulProducerCallIsXAndLastFailedProducerCallIsY_thenDailyStatusIsZ(
    String x,
    String y,
    FeedProviderStatus.DailyStatus z
  ) {
    // Arrange
    when(clock.instant()).thenReturn(Instant.parse("2025-06-30T14:48:00Z"));
    FeedProvider input = new FeedProvider();
    input.setSystemId("systemId");
    input.setCodespace("CSP");
    input.setLastSuccessfulProducerCall(StringUtils.isBlank(x) ? null : Instant.parse(x));
    input.setLastFailedProducerCall(StringUtils.isBlank(y) ? null : Instant.parse(y));
    input.setUrl("https://www.google.fr");

    // Act
    var output = tested.mapFeedProvider(input);

    // Assert
    assertEquals(z, output.getDailyStatus(), "should map daily status properly");
  }
}
