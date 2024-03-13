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

package org.entur.lamassu.mapper.feedmapper;

import org.entur.gbfs.v2_3.free_bike_status.GBFSBike;
import org.entur.gbfs.v2_3.free_bike_status.GBFSData;
import org.entur.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FreeBikeStatusFeedMapper extends AbstractFeedMapper<GBFSFreeBikeStatus> {
    @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
    private String targetGbfsVersion;

    @Override
    public GBFSFreeBikeStatus map(GBFSFreeBikeStatus source, FeedProvider feedProvider) {
        if (source == null) {
            return null;
        }

        var mapped = new GBFSFreeBikeStatus();
        mapped.setVersion(targetGbfsVersion);
        mapped.setLastUpdated(source.getLastUpdated());
        mapped.setTtl(source.getTtl());
        mapped.setData(mapData(source.getData(), feedProvider));
        return mapped;
    }

    private GBFSData mapData(GBFSData data, FeedProvider feedProvider) {
        var mapped = new GBFSData();


        data.getBikes().forEach(bike -> bike.setBikeId(bike.getBikeId().replace(" ", "_")
                                                            .replace("(","_")
                                                            .replace(")","_")
                                                            .replace("é","_")
                                                            .replace("à","_")
        ));


        mapped.setBikes(
                data.getBikes().stream()
                        .filter(bike -> (bike.getLon() != null && bike.getLat() != null) || bike.getStationId() != null)
                        .map(bike -> mapBike(bike, feedProvider))
                        .collect(Collectors.toList())
        );
        return mapped;
    }

    protected GBFSBike mapBike(GBFSBike bike, FeedProvider feedProvider) {
        var mapped = new GBFSBike();
        mapped.setBikeId(IdMappers.mapId(feedProvider.getCodespace(), IdMappers.BIKE_ID_TYPE, bike.getBikeId()));
        mapped.setLat(bike.getLat());
        mapped.setLon(bike.getLon());
        mapped.setIsReserved(bike.getIsReserved());
        mapped.setIsDisabled(bike.getIsDisabled());
        mapped.setRentalUris(bike.getRentalUris());
        mapped.setVehicleTypeId(mapVehicleTypeId(bike.getVehicleTypeId(), feedProvider));
        mapped.setLastReported(bike.getLastReported());
        mapped.setCurrentRangeMeters(bike.getCurrentRangeMeters() != null ? bike.getCurrentRangeMeters() : 0);
        mapped.setCurrentFuelPercent(bike.getCurrentFuelPercent());
        mapped.setStationId(mapStationId(bike.getStationId(), feedProvider));
        mapped.setHomeStationId(mapStationId(bike.getHomeStationId(), feedProvider));
        mapped.setPricingPlanId(mapPricingPlanId(bike.getPricingPlanId(), feedProvider));
        mapped.setVehicleEquipment(bike.getVehicleEquipment());
        mapped.setAvailableUntil(bike.getAvailableUntil());
        return mapped;
    }

    private String mapVehicleTypeId(String vehicleTypeId, FeedProvider feedProvider) {
        if (feedProvider.getVehicleTypes() != null) {
            return IdMappers.mapId(
                    feedProvider.getCodespace(),
                    IdMappers.VEHICLE_TYPE_ID_TYPE,
                    feedProvider.getVehicleTypes().get(0).getVehicleTypeId()
            );
        }

        if (vehicleTypeId == null) {
            return null;
        }

        return IdMappers.mapId(feedProvider.getCodespace(), IdMappers.VEHICLE_TYPE_ID_TYPE, vehicleTypeId);
    }

    private String mapStationId(String stationId, FeedProvider feedProvider) {
        if (stationId == null) {
            return null;
        }

        return IdMappers.mapId(feedProvider.getCodespace(), IdMappers.STATION_ID_TYPE, stationId);
    }

    private String mapPricingPlanId(String pricingPlanId, FeedProvider feedProvider) {
        if (feedProvider.getPricingPlans() != null) {
            return IdMappers.mapId(
                    feedProvider.getCodespace(),
                    IdMappers.PRICING_PLAN_ID_TYPE,
                    feedProvider.getPricingPlans().get(0).getPlanId()
            );
        }

        if (pricingPlanId == null) {
            return null;
        }

        return IdMappers.mapId(feedProvider.getCodespace(), IdMappers.PRICING_PLAN_ID_TYPE, pricingPlanId);
    }
}
