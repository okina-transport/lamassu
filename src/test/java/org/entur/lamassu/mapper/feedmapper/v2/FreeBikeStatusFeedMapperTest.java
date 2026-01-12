/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.mapper.feedmapper.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mobilitydata.gbfs.v2_3.free_bike_status.VehicleEquipment.CHILD_SEAT_A;

import java.util.List;
import org.entur.lamassu.mapper.feedidmapper.v2.FreeBikeStatusFeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.EnturIdMappingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSBike;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSData;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSRentalUris;

class FreeBikeStatusFeedMapperTest {

  FreeBikeStatusFeedMapper mapper;

  @BeforeEach
  void prepare() {
    mapper =
      new FreeBikeStatusFeedMapper(
        "2.3",
        new FreeBikeStatusFeedIdMapper(new EnturIdMappingService())
      );
  }

  @Test
  void testMissingCurrentRangeMeters() {
    var mapped = mapper.mapBike(new GBFSBike());
    // if no current_range_meters is provided, we explicitly don't want Lamassu to fill it in
    Assertions.assertNull(mapped.getCurrentRangeMeters());
  }

  @Test
  void testCustomData() {
    var feedProvider = getTestProvider();

    GBFSRentalUris rentalUris = new GBFSRentalUris()
      .withAndroid("android")
      .withIos("ios")
      .withWeb("web");

    GBFSBike bike = new GBFSBike()
      .withBikeId("BikeId")
      .withLat(5.0)
      .withLon(6.0)
      .withIsReserved(true)
      .withIsDisabled(true)
      .withRentalUris(rentalUris)
      .withLastReported(7.0d)
      .withCurrentRangeMeters(8.0d)
      .withCurrentFuelPercent(9.0d)
      .withStationId("StationId")
      .withHomeStationId("HomeStationId")
      .withPricingPlanId("PricingPlanId")
      .withVehicleEquipment(List.of(CHILD_SEAT_A))
      .withAvailableUntil("31/12/2026");

    GBFSData data = new GBFSData().withBikes(List.of(bike));

    GBFSFreeBikeStatus source = new GBFSFreeBikeStatus()
      .withVersion("2.2")
      .withTtl(600)
      .withLastUpdated(900)
      .withData(data);

    var mapped = mapper.map(source, feedProvider, false);

    assertEquals("2.3", mapped.getVersion());
    assertEquals(600, mapped.getTtl());
    assertEquals(900, mapped.getLastUpdated());

    var mappedData = mapped.getData();
    assertEquals(1, mappedData.getBikes().size());

    var mappedBike = mappedData.getBikes().getFirst();
    assertEquals("TST:Vehicle:BikeId", mappedBike.getBikeId());
    assertEquals(5.0, mappedBike.getLat());
    assertEquals(6.0, mappedBike.getLon());
    assertTrue(mappedBike.getIsReserved());
    assertTrue(mappedBike.getIsDisabled());
    assertEquals(7.0d, mappedBike.getLastReported());
    assertEquals(8.0d, mappedBike.getCurrentRangeMeters());
    assertEquals(9.0d, mappedBike.getCurrentFuelPercent());
    assertEquals("TST:Station:StationId", mappedBike.getStationId());
    assertEquals("TST:Station:HomeStationId", mappedBike.getHomeStationId());
    assertEquals("TST:PricingPlan:PricingPlanId", mappedBike.getPricingPlanId());
    assertEquals(1, mappedBike.getVehicleEquipment().size());
    assertEquals(CHILD_SEAT_A, mappedBike.getVehicleEquipment().getFirst());
    assertEquals("31/12/2026", mappedBike.getAvailableUntil());

    var mappedRentalUri = mappedBike.getRentalUris();
    assertEquals("android", mappedRentalUri.getAndroid());
    assertEquals("ios", mappedRentalUri.getIos());
    assertEquals("web", mappedRentalUri.getWeb());
  }

  private FeedProvider getTestProvider() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("testsystem");
    feedProvider.setCodespace("TST");
    feedProvider.setLanguage("en");

    return feedProvider;
  }
}
