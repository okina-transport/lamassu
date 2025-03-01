package org.entur.lamassu.integration;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class GBFSRestIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testFeedProviderDiscovery() throws Exception {
        mockMvc.perform(get("/gbfs")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systems[0].id").value("testatlantis"));
    }

    @Test
    public void testGBFS() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/gbfs")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.last_updated").value(1606727710));
    }

    @Test @Ignore("gbfs_versions intentionally not mapped")
    public void testGBFSVersions() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/gbfs_versions")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.versions[0].version").value("2.1"));
    }

    @Test
    public void testSystemInformation() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/system_information")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.system_id").value("testatlantis"));
    }

    @Test
    public void testVehicleTypes() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/vehicle_types")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vehicle_types[0].vehicle_type_id").value("TST:VehicleType:Scooter"));
    }

    @Test
    public void testFreeBikeStatus() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/free_bike_status")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bikes[0].bike_id").value("TST:Vehicle:1234"))
                .andExpect(jsonPath("$.data.bikes[0].vehicle_type_id").value("TST:VehicleType:Scooter"))
                .andExpect(jsonPath("$.data.bikes[0].pricing_plan_id").value("TST:PricingPlan:Basic"));
    }

    @Test
    public void testSystemRegions() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/system_regions")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.regions[0].region_id").value("TST:Region:Sahara"));
    }

    @Test
    public void testSystemPricingPlans() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/system_pricing_plans")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plans[0].plan_id").value("TST:PricingPlan:Basic"));
    }

    @Test
    public void testStationInformation() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/station_information")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stations[0].station_id").value("TST:Station:1"));
    }

    @Test
    public void testStationStatus() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/station_status")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stations[1].station_id").value("TST:Station:2"));
    }

    @Test
    public void testSystemHours() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/system_hours")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rental_hours[0].user_types[0]").value("member"));
    }

    @Test
    public void testSystemCalendar() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/system_calendar")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.calendars[0].start_month").value(1));
    }

    @Test
    public void testSystemAlerts() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/system_alerts")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alerts[0].alert_id").value("TST:Alert:1"))
                .andExpect(jsonPath("$.data.alerts[0].station_ids[0]").value("TST:Station:1"))
                .andExpect(jsonPath("$.data.alerts[0].region_ids[0]").value("TST:Region:1"));
    }

    @Test
    public void testGeofencingZones() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/geofencing_zones")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.geofencing_zones.features[0].properties.name").value("Nes"));
    }

    @Test
    public void testUnknownProviderResponds404() throws Exception {
            mockMvc.perform(get("/gbfs/foobar/gbfs")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void missingOptionalFeedResponds404() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/foobar")
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnsupportedFeedResponds400() throws Exception {
        mockMvc.perform(get("/gbfs/testatlantis/foobar")
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }
}
