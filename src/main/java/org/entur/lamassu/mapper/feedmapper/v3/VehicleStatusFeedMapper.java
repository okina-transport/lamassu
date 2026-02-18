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

package org.entur.lamassu.mapper.feedmapper.v3;

import java.util.stream.Collectors;
import org.entur.lamassu.mapper.feedidmapper.v3.VehicleStatusFeedIdMapper;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSData;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicleStatus;
import org.springframework.stereotype.Component;

@Component
public class VehicleStatusFeedMapper extends AbstractFeedMapper<GBFSVehicleStatus> {

  private static final String TARGET_GBFS_VERSION = "3.0";

  private final VehicleStatusFeedIdMapper idMapper;

  public VehicleStatusFeedMapper(VehicleStatusFeedIdMapper idMapper) {
    this.idMapper = idMapper;
  }

  @Override
  public GBFSVehicleStatus map(
    GBFSVehicleStatus source,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSVehicleStatus();
    mapped.setVersion(TARGET_GBFS_VERSION);
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setData(mapData(source.getData()));

    idMapper.mapIds(mapped, feedProvider, toOriginalId);
    return mapped;
  }

  private GBFSData mapData(GBFSData data) {
    var mapped = new GBFSData();
    mapped.setVehicles(
      data.getVehicles().stream().map(this::mapVehicle).collect(Collectors.toList())
    );
    return mapped;
  }

  protected GBFSVehicle mapVehicle(GBFSVehicle vehicle) {
    var mapped = new GBFSVehicle();
    mapped.setVehicleId(vehicle.getVehicleId());
    mapped.setLat(vehicle.getLat());
    mapped.setLon(vehicle.getLon());
    mapped.setIsReserved(vehicle.getIsReserved());
    mapped.setIsDisabled(vehicle.getIsDisabled());
    mapped.setRentalUris(vehicle.getRentalUris());
    mapped.setVehicleTypeId(vehicle.getVehicleTypeId());
    mapped.setLastReported(vehicle.getLastReported());
    mapped.setCurrentRangeMeters(vehicle.getCurrentRangeMeters());
    mapped.setCurrentFuelPercent(vehicle.getCurrentFuelPercent());
    mapped.setStationId(vehicle.getStationId());
    mapped.setHomeStationId(vehicle.getHomeStationId());
    mapped.setPricingPlanId(vehicle.getPricingPlanId());
    mapped.setVehicleEquipment(vehicle.getVehicleEquipment());
    mapped.setAvailableUntil(vehicle.getAvailableUntil());
    return mapped;
  }
}
