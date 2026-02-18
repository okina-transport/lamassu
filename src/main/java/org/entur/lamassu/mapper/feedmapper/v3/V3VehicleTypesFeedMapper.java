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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.entur.lamassu.mapper.feedidmapper.v3.V3VehicleTypesFeedIdMapper;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSData;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleType;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleTypes;
import org.springframework.stereotype.Component;

@Component
public class V3VehicleTypesFeedMapper extends AbstractFeedMapper<GBFSVehicleTypes> {

  private static final String TARGET_GBFS_VERSION = "3.0";

  private final V3VehicleTypesFeedIdMapper idMapper;

  public V3VehicleTypesFeedMapper(V3VehicleTypesFeedIdMapper idMapper) {
    this.idMapper = idMapper;
  }

  @Override
  public GBFSVehicleTypes map(
    GBFSVehicleTypes source,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    // TODO should we support custom vehicle types?
    //if (feedProvider.getVehicleTypes() != null) {
    //  return customVehicleTypes(feedProvider);
    //}

    if (source == null) {
      return null;
    }

    var mapped = new GBFSVehicleTypes();
    mapped.setVersion(TARGET_GBFS_VERSION);
    mapped.setTtl(source.getTtl());
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setData(mapData(source.getData()));

    idMapper.mapIds(mapped, feedProvider, toOriginalId);
    return mapped;
  }

  private GBFSData mapData(GBFSData data) {
    var mapped = new GBFSData();
    mapped.setVehicleTypes(mapVehicleTypes(data.getVehicleTypes()));
    return mapped;
  }

  private List<GBFSVehicleType> mapVehicleTypes(List<GBFSVehicleType> vehicleTypes) {
    return vehicleTypes.stream().map(this::mapVehicleType).collect(Collectors.toList());
  }

  private GBFSVehicleType mapVehicleType(GBFSVehicleType vehicleType) {
    var mapped = new GBFSVehicleType();
    mapped.setVehicleTypeId(vehicleType.getVehicleTypeId());
    mapped.setFormFactor(vehicleType.getFormFactor());
    mapped.setRiderCapacity(vehicleType.getRiderCapacity());
    mapped.setCargoVolumeCapacity(vehicleType.getCargoVolumeCapacity());
    mapped.setCargoLoadCapacity(vehicleType.getCargoLoadCapacity());
    mapped.setPropulsionType(vehicleType.getPropulsionType());
    mapped.setEcoLabels(vehicleType.getEcoLabels());
    mapped.setMaxRangeMeters(vehicleType.getMaxRangeMeters());
    mapped.setName(vehicleType.getName());
    mapped.setVehicleAccessories(vehicleType.getVehicleAccessories());
    mapped.setgCO2Km(vehicleType.getgCO2Km());
    mapped.setVehicleImage(vehicleType.getVehicleImage());
    mapped.setMake(vehicleType.getMake());
    mapped.setModel(vehicleType.getModel());
    mapped.setColor(vehicleType.getColor());
    mapped.setDescription(vehicleType.getDescription());
    mapped.setWheelCount(vehicleType.getWheelCount());
    mapped.setMaxPermittedSpeed(vehicleType.getMaxPermittedSpeed());
    mapped.setRatedPower(vehicleType.getRatedPower());
    mapped.setDefaultReserveTime(vehicleType.getDefaultReserveTime());
    mapped.setReturnConstraint(vehicleType.getReturnConstraint());
    mapped.setVehicleAssets(vehicleType.getVehicleAssets());
    mapped.setDefaultPricingPlanId(vehicleType.getDefaultPricingPlanId());
    mapped.setPricingPlanIds(
      vehicleType.getPricingPlanIds() != null
        ? new ArrayList<>(vehicleType.getPricingPlanIds())
        : null
    );
    return mapped;
  }
}
