package org.entur.lamassu.integration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.entur.lamassu.TestLamassuApplication;
import org.entur.lamassu.leader.LeaderSingletonService;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.activemq.ArtemisContainer;

@ActiveProfiles({ "test", "leader", "mdm" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = { "tr_in_subscription_monitoring" })
@SpringBootTest(
  classes = TestLamassuApplication.class,
  properties = "scheduling.enabled=false",
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class AbstractIntegrationTestBase {

  private static MockWebServer gbfsProvidersMockWebServer;
  private static MockWebServer ishtarMockServer;
  private static MockWebServer mdmMockWebServer;
  private static MockWebServer oauthServer;
  protected static ArtemisContainer artemisContainer;

  @Autowired
  protected EmbeddedKafkaBroker broker;

  @DynamicPropertySource
  static void artemisProperties(DynamicPropertyRegistry registry) {
    registry.add(
      "spring.artemis.broker-url",
      () ->
        "tcp://%s:%d".formatted(
            artemisContainer.getHost(),
            artemisContainer.getMappedPort(61616)
          )
    );
  }

  @Autowired
  private LeaderSingletonService leaderSingletonService;

  @BeforeAll
  public static void setUp() throws IOException {
    gbfsProvidersMockWebServer = new MockWebServer();
    gbfsProvidersMockWebServer.setDispatcher(new GBFSDispatcher());
    gbfsProvidersMockWebServer.start(8888);

    ishtarMockServer = new MockWebServer();
    ishtarMockServer.setDispatcher(new IshtarServerDispatcher());
    ishtarMockServer.start(8881);

    mdmMockWebServer = new MockWebServer();
    mdmMockWebServer.setDispatcher(new MdmServerDispatcher());
    mdmMockWebServer.start(7878);

    oauthServer = new MockWebServer();
    oauthServer.setDispatcher(new OauthServerDispatcher());
    oauthServer.start(9999);

    artemisContainer =
      new ArtemisContainer("apache/activemq-artemis:2.38.0")
        .withEnv("ANONYMOUS_LOGIN", "true");
    artemisContainer.start();
  }

  @NotNull
  private static MockResponse getMockResponse(String file) {
    return new MockResponse()
      .setResponseCode(200)
      .setHeader("Content-Type", "application/json")
      .setBody(getFileFromResource(file));
  }

  @AfterAll
  public static void tearDown() throws IOException {
    gbfsProvidersMockWebServer.shutdown();
    ishtarMockServer.shutdown();
    mdmMockWebServer.shutdown();
    oauthServer.shutdown();
    artemisContainer.stop();
  }

  private static String getFileFromResource(String fileName) {
    try {
      InputStream inputStream =
        AbstractIntegrationTestBase.class.getClassLoader().getResourceAsStream(fileName);
      if (inputStream == null) {
        throw new IllegalArgumentException("file not found! " + fileName);
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  public void heartbeat() throws InterruptedException {
    Thread.sleep(1000);
    leaderSingletonService.update();
    Thread.sleep(1000);
  }

  public static class GBFSDispatcher extends okhttp3.mockwebserver.Dispatcher {

    @Override
    public @NotNull MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
      return switch (recordedRequest.getPath()) {
        case "/testatlantis/gbfs" -> getMockResponse("v2/gbfs.json");
        case "/testatlantis/gbfs_versions" -> getMockResponse("v2/gbfs_versions.json");
        case "/testatlantis/vehicle_types" -> getMockResponse("v2/vehicle_types.json");
        case "/testatlantis/station_information" -> getMockResponse(
          "v2/station_information.json"
        );
        case "/testatlantis/station_status" -> getMockResponse("v2/station_status.json");
        case "/testatlantis/system_information" -> getMockResponse(
          "v2/system_information.json"
        );
        case "/testatlantis/free_bike_status" -> getMockResponse(
          "v2/free_bike_status.json"
        );
        case "/testatlantis/system_regions" -> getMockResponse("v2/system_regions.json");
        case "/testatlantis/system_pricing_plans" -> getMockResponse(
          "v2/system_pricing_plans.json"
        );
        case "/testatlantis/system_hours" -> getMockResponse("v2/system_hours.json");
        case "/testatlantis/system_calendar" -> getMockResponse(
          "v2/system_calendar.json"
        );
        case "/testatlantis/system_alerts" -> getMockResponse("v2/system_alerts.json");
        case "/testatlantis/geofencing_zones" -> getMockResponse(
          "v2/geofencing_zones.json"
        );
        case "/testozon/gbfs" -> getMockResponse("v3/gbfs.json");
        case "/testozon/gbfs_versions" -> getMockResponse("v3/gbfs_versions.json");
        case "/testozon/vehicle_types" -> getMockResponse("v3/vehicle_types.json");
        case "/testozon/station_information" -> getMockResponse(
          "v3/station_information.json"
        );
        case "/testozon/station_status" -> getMockResponse("v3/station_status.json");
        case "/testozon/system_information" -> getMockResponse(
          "v3/system_information.json"
        );
        case "/testozon/vehicle_status" -> getMockResponse("v3/vehicle_status.json");
        case "/testozon/system_regions" -> getMockResponse("v3/system_regions.json");
        case "/testozon/system_pricing_plans" -> getMockResponse(
          "v3/system_pricing_plans.json"
        );
        case "/testozon/system_alerts" -> getMockResponse("v3/system_alerts.json");
        case "/testozon/geofencing_zones" -> getMockResponse("v3/geofencing_zones.json");
        default -> new MockResponse().setResponseCode(404);
      };
    }
  }

  public static class IshtarServerDispatcher extends okhttp3.mockwebserver.Dispatcher {

    @Override
    public @NotNull MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
      return switch (recordedRequest.getPath()) {
        case "/gbfs-apis/for-lamassu" -> getMockResponse("ishtar/gbfs.for-lamassu.json");
        default -> throw new IllegalStateException(
          "Unexpected request path: " + recordedRequest.getPath()
        );
      };
    }
  }

  public static class MdmServerDispatcher extends okhttp3.mockwebserver.Dispatcher {

    @Override
    public @NonNull MockResponse dispatch(@NonNull RecordedRequest recordedRequest)
      throws InterruptedException {
      return switch (recordedRequest.getPath()) {
        case "/api/v1/organisations/byOriginalId?originalId=testatlantis" -> getMockResponse(
          "mdm" + "/organisations_byOriginalId_testatlantis.json"
        );
        case "/api/v1/organisations/byOriginalId?originalId=testozon" -> getMockResponse(
          "mdm" + "/organisations_byOriginalId_testozon.json"
        );
        case "/api/v1/organisations/bySuperId?superId=1" -> getMockResponse(
          "mdm" + "/organisations_bySuperId_testatlantis.json"
        );
        case "/api/v1/organisations/bySuperId?superId=2" -> getMockResponse(
          "mdm/organisations_bySuperId_testozon.json"
        );
        case "/api/v1/parkings/byOperator?operator=testatlantis" -> getMockResponse(
          "mdm/parkings_byOperator_testatlantis" + ".json"
        );
        case "/api/v1/parkings/byOperator?operator=testozon" -> getMockResponse(
          "mdm/parkings_byOperator_testozon" + ".json"
        );
        default -> throw new IllegalStateException(
          "Unexpected request path: " + recordedRequest.getPath()
        );
      };
    }
  }

  public static class OauthServerDispatcher extends okhttp3.mockwebserver.Dispatcher {

    @Override
    public @NonNull MockResponse dispatch(@NonNull RecordedRequest recordedRequest)
      throws InterruptedException {
      return switch (recordedRequest.getPath()) {
        case "/realms/fakeRealm/protocol/openid-connect/token" -> getMockResponse(
          "oauth/token.json"
        );
        default -> throw new IllegalStateException(
          "Unexpected request path: " + recordedRequest.getPath()
        );
      };
    }
  }
}
