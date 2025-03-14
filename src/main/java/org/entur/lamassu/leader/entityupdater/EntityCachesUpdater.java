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

package org.entur.lamassu.leader.entityupdater;

import org.entur.gbfs.GbfsDelivery;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityCachesUpdater {
    private final VehiclesUpdater vehiclesUpdater;
    private final StationsUpdater stationsUpdater;
    private final GeofencingZonesUpdater geofencingZonesUpdater;

    @Autowired
    public EntityCachesUpdater(
            VehiclesUpdater vehiclesUpdater,
            StationsUpdater stationsUpdater,
            GeofencingZonesUpdater geofencingZonesUpdater
    ) {
        this.vehiclesUpdater = vehiclesUpdater;
        this.stationsUpdater = stationsUpdater;
        this.geofencingZonesUpdater = geofencingZonesUpdater;
    }

    public void updateEntityCaches(FeedProvider feedProvider, GbfsDelivery delivery, GbfsDelivery oldDelivery) {
        if (canUpdateVehicles(delivery)) {
            vehiclesUpdater.addOrUpdateVehicles(
                    feedProvider,
                    delivery,
                    oldDelivery
            );
        }

        if (canUpdateStations(delivery)) {
            stationsUpdater.addOrUpdateStations(
                    feedProvider,
                    delivery,
                    oldDelivery
            );
        }

        if (delivery.getGeofencingZones() != null) {
            geofencingZonesUpdater.addOrUpdateGeofencingZones(
                    feedProvider,
                    delivery.getGeofencingZones()
            );
        }
    }

    private boolean canUpdateVehicles(GbfsDelivery delivery) {
        return delivery.getFreeBikeStatus() != null
                && delivery.getFreeBikeStatus().getData() != null
                && delivery.getSystemInformation() != null
                && delivery.getSystemInformation().getData() != null
                && delivery.getVehicleTypes() != null
                && delivery.getVehicleTypes().getData() != null
                && delivery.getSystemPricingPlans() != null
                && delivery.getSystemPricingPlans().getData() != null;
    }

    private boolean canUpdateStations(GbfsDelivery delivery) {
        return delivery.getStationStatus() != null
                && delivery.getStationStatus().getData() != null
                && delivery.getStationInformation() != null
                && delivery.getStationInformation().getData() != null
                && delivery.getSystemInformation() != null
                && delivery.getSystemInformation().getData() != null
                && delivery.getVehicleTypes() != null
                && delivery.getVehicleTypes().getData() != null
                && delivery.getSystemPricingPlans() != null
                && delivery.getSystemPricingPlans().getData() != null;
    }
}
