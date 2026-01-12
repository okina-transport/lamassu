package org.entur.lamassu.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.hamcrest.core.Every;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class GBFSV3RestIntegrationTest extends AbstractIntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testFeedProviderDiscovery() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.systems.length()").value(2))
      .andExpect(jsonPath("$.systems[0].id").value("MOBIITI:Organisation:1"))
      .andExpect(
        jsonPath("$.systems[0].url")
          .value("http://localhost:8099/gbfs/v3/testatlantis/gbfs")
      )
      .andExpect(jsonPath("$.systems[1].id").value("MOBIITI:Organisation:2"))
      .andExpect(
        jsonPath("$.systems[1].url").value("http://localhost:8099/gbfs/v3/testozon/gbfs")
      );
  }

  @Test
  void testFeedProviderDiscoveryOriginalId() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3?useOriginalId=true").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.systems.length()").value(2))
      .andExpect(jsonPath("$.systems[0].id").value("testatlantis"))
      .andExpect(
        jsonPath("$.systems[0].url")
          .value("http://localhost:8099/gbfs/v3/testatlantis/gbfs?useOriginalId=true")
      )
      .andExpect(jsonPath("$.systems[1].id").value("testozon"))
      .andExpect(
        jsonPath("$.systems[1].url")
          .value("http://localhost:8099/gbfs/v3/testozon/gbfs?useOriginalId=true")
      );
  }

  @Test
  void testManifest() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/manifest.json").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.datasets.length()").value(2))
      .andExpect(jsonPath("$.data.datasets[0].system_id").value("MOBIITI:Organisation:1"))
      .andExpect(
        jsonPath("$.data.datasets[0].versions[0].url")
          .value("http://localhost:8099/gbfs/v2/testatlantis/gbfs")
      )
      .andExpect(jsonPath("$.data.datasets[1].system_id").value("MOBIITI:Organisation:2"))
      .andExpect(
        jsonPath("$.data.datasets[1].versions[0].url")
          .value("http://localhost:8099/gbfs/v3/testozon/gbfs")
      );
  }

  @Test
  void testManifestOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/manifest.json?useOriginalId=true").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.datasets.length()").value(2))
      .andExpect(jsonPath("$.data.datasets[0].system_id").value("testatlantis"))
      .andExpect(
        jsonPath("$.data.datasets[0].versions[0].url")
          .value("http://localhost:8099/gbfs/v2/testatlantis/gbfs?useOriginalId=true")
      )
      .andExpect(jsonPath("$.data.datasets[1].system_id").value("testozon"))
      .andExpect(
        jsonPath("$.data.datasets[1].versions[0].url")
          .value("http://localhost:8099/gbfs/v3/testozon/gbfs?useOriginalId=true")
      );
  }

  @Test
  void testGBFS() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/gbfs").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.last_updated").value("2020-11-30T09:15:10.000+00:00"))
      .andExpect(
        jsonPath("$.data.feeds[*].url")
          .value(
            Every.everyItem(
              Matchers.matchesRegex("^http://localhost:8099/gbfs/v3/testozon/[a-z_]+$")
            )
          )
      );
  }

  @Test
  void testGBFSOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/gbfs?useOriginalId=true").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.last_updated").value("2020-11-30T09:15:10.000+00:00"))
      .andExpect(
        jsonPath("$.data.feeds[*].url")
          .value(
            Every.everyItem(
              Matchers.matchesRegex(
                "^http://localhost:8099/gbfs/v3/testozon/[a-z_]+\\?useOriginalId" +
                "=true$"
              )
            )
          )
      );
  }

  @Test
  @Disabled("gbfs_versions intentionally not mapped")
  void testGBFSVersions() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/gbfs_versions").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.versions[0].version").value("2.1"));
  }

  @Test
  void testSystemInformation() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/system_information").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.system_id").value("MOBIITI:Organisation:2"));
  }

  @Test
  void testSystemInformationOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/system_information?useOriginalId=true")
          .contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.system_id").value("testozon"));
  }

  @Test
  void testVehicleTypes() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/vehicle_types").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.vehicle_types[0].vehicle_type_id").value("abc123"));
  }

  @Test
  void testVehicleTypesOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/vehicle_types?useOriginalId=true")
          .contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.vehicle_types[0].vehicle_type_id").value("abc123"));
  }

  @Test
  void testVehicleStatus() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/vehicle_status").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.vehicles[0].vehicle_id")
          .value("OZO:Vehicle:973a5c94-c288-4a2b-afa6-de8aeb6ae2e5")
      )
      .andExpect(jsonPath("$.data.vehicles[0].vehicle_type_id").value("abc123"));
  }

  @Test
  void testVehicleStatusOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/vehicle_status?useOriginalId=true")
          .contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.vehicles[0].vehicle_id")
          .value("973a5c94-c288-4a2b-afa6-de8aeb6ae2e5")
      )
      .andExpect(jsonPath("$.data.vehicles[0].vehicle_type_id").value("abc123"));
  }

  @Test
  void testSystemRegions() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/system_regions").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.regions[0].region_id").value("OZO:Region:3"));
  }

  @Test
  void testSystemRegionsOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/system_regions?useOriginalId=true")
          .contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.regions[0].region_id").value("3"));
  }

  @Test
  void testSystemPricingPlans() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/system_pricing_plans").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.plans[0].plan_id").value("OZO:PricingPlan:bike_plan_1")
      );
  }

  @Test
  void testSystemPricingPlansOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/system_pricing_plans?useOriginalId=true")
          .contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.plans[0].plan_id").value("bike_plan_1"));
  }

  @Test
  void testStationInformation() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/station_information").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.stations[0].station_id").value("OZO:Station:pga"));
  }

  @Test
  void testStationInformationOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/station_information?useOriginalId=true")
          .contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.stations[0].station_id").value("pga"));
  }

  @Test
  void testStationStatus() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/station_status").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.stations[1].station_id").value("FR:40140:Parking:4:LOC")
      );
  }

  @Test
  void testStationStatusOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/station_status?useOriginalId=true")
          .contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.stations[1].station_id").value("station2"));
  }

  @Test
  void testSystemAlerts() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/system_alerts").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.alerts[0].alert_id").value("OZO:Alert:21"))
      .andExpect(jsonPath("$.data.alerts[0].station_ids[0]").value("OZO:Station:123"));
  }

  @Test
  void testSystemAlertsOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/system_alerts?useOriginalId=true")
          .contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.alerts[0].alert_id").value("21"))
      .andExpect(jsonPath("$.data.alerts[0].station_ids[0]").value("123"));
  }

  @Test
  void testGeofencingZones() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/geofencing_zones").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.geofencing_zones.features[0].properties.name[0].text")
          .value("NE 24th/NE Knott")
      )
      .andExpect(
        jsonPath(
          "$.data.geofencing_zones.features[0].properties.rules[0].vehicle_type_ids[0]"
        )
          .value("moped1")
      )
      .andExpect(
        jsonPath(
          "$.data.geofencing_zones.features[0].properties.rules[0].vehicle_type_ids[1]"
        )
          .value("car1")
      );
  }

  @Test
  void testGeofencingZonesOriginalId() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/geofencing_zones?useOriginalId=true")
          .contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.geofencing_zones.features[0].properties.name[0].text")
          .value("NE 24th/NE Knott")
      )
      .andExpect(
        jsonPath(
          "$.data.geofencing_zones.features[0].properties.rules[0].vehicle_type_ids[0]"
        )
          .value("moped1")
      )
      .andExpect(
        jsonPath(
          "$.data.geofencing_zones.features[0].properties.rules[0].vehicle_type_ids[1]"
        )
          .value("car1")
      );
  }

  @Test
  void testUnknownProviderResponds404() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/foobar/gbfs").contentType("application/json"))
      .andExpect(status().isNotFound());
  }

  @Test
  void testUnsupportedFeedResponds400() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/foobar").contentType("application/json"))
      .andExpect(status().isBadRequest());
  }
}
