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
import org.entur.gbfs.v2_3.free_bike_status.GBFSBike;
import org.entur.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.entur.gbfs.v2_3.system_information.GBFSSystemInformation;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleTypes;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.mapper.entitymapper.PricingPlanMapper;
import org.entur.lamassu.mapper.entitymapper.SystemMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleTypeMapper;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.CacheUtil;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class VehiclesUpdater {
    private final VehicleCache vehicleCache;
    private final VehicleSpatialIndex spatialIndex;
    private final SystemMapper systemMapper;
    private final PricingPlanMapper pricingPlanMapper;
    private final VehicleTypeMapper vehicleTypeMapper;
    private final VehicleMapper vehicleMapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public VehiclesUpdater(
            VehicleCache vehicleCache,
            VehicleSpatialIndex spatialIndex,
            VehicleMapper vehicleMapper,
            SystemMapper systemMapper,
            PricingPlanMapper pricingPlanMapper,
            VehicleTypeMapper vehicleTypeMapper
    ) {
        this.vehicleCache = vehicleCache;
        this.spatialIndex = spatialIndex;
        this.vehicleMapper = vehicleMapper;
        this.systemMapper = systemMapper;
        this.pricingPlanMapper = pricingPlanMapper;
        this.vehicleTypeMapper = vehicleTypeMapper;
    }

    public void addOrUpdateVehicles(
            FeedProvider feedProvider,
            GbfsDelivery delivery,
            GbfsDelivery oldDelivery
    ) {
        GBFSFreeBikeStatus freeBikeStatusFeed = delivery.getFreeBikeStatus();
        GBFSFreeBikeStatus oldFreeBikeStatusFeed = oldDelivery.getFreeBikeStatus();
        GBFSSystemInformation systemInformationFeed = delivery.getSystemInformation();
        GBFSSystemPricingPlans pricingPlansFeed = delivery.getSystemPricingPlans();
        GBFSVehicleTypes vehicleTypesFeed = delivery.getVehicleTypes();

        var vehicleIds = freeBikeStatusFeed.getData().getBikes().stream()
                .map(GBFSBike::getBikeId)
                .collect(Collectors.toSet());

        Set<String> vehicleIdsToRemove = null;

        if (oldFreeBikeStatusFeed != null && oldFreeBikeStatusFeed.getData() != null) {
            vehicleIdsToRemove = oldFreeBikeStatusFeed.getData().getBikes().stream()
                    .map(GBFSBike::getBikeId).collect(Collectors.toSet());

            // Find vehicle ids in old feed not present in new feed
            vehicleIdsToRemove.removeAll(vehicleIds);
            logger.trace("Found {} vehicleIds to remove from old free_bike_status feed: {}", vehicleIdsToRemove.size(), oldFreeBikeStatusFeed);

            // Add vehicle ids that are staged for removal to the set of vehicle ids that will be used to
            // fetch current vehicles from cache
            vehicleIds.addAll(vehicleIdsToRemove);
        }

        if (vehicleIdsToRemove == null) {
            vehicleIdsToRemove = new HashSet<>(vehicleIds);
            logger.debug("Old free_bike_status feed was not available or had no data. As a workaround, removing all vehicles for provider {}", feedProvider.getSystemId());
        }

        var currentVehicles = vehicleCache.getAllAsMap(
                vehicleIds.stream()
                        .map(id -> getVehicleCacheKey(id, feedProvider))
                        .collect(Collectors.toSet())
        );

        var system = getSystem(feedProvider, systemInformationFeed);
        var pricingPlans = getPricingPlans(pricingPlansFeed, system.getLanguage());
        var vehicleTypes = getVehicleTypes(vehicleTypesFeed, pricingPlans, system.getLanguage());


        var vehicles = freeBikeStatusFeed.getData().getBikes().stream()
                .filter(new VehicleFilter(pricingPlans, vehicleTypes))
                .map(vehicle -> vehicleMapper.mapVehicle(
                        vehicle,
                        vehicleTypes.get(vehicle.getVehicleTypeId()),
                        pricingPlans.get(vehicle.getPricingPlanId()),
                        system
                ))
                .collect(Collectors.toMap(v -> getVehicleCacheKey(v.getId(), feedProvider), v -> v));

        Set<VehicleSpatialIndexId> spatialIndicesToRemove = new java.util.HashSet<>(Set.of());
        Map<VehicleSpatialIndexId, Vehicle> spatialIndexUpdateMap = new java.util.HashMap<>(Map.of());

        vehicles.forEach((key, vehicle) -> {
            var spatialIndexId = SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, feedProvider);
            var previousVehicle = currentVehicles.get(key);

            if (previousVehicle != null) {
                var oldSpatialIndexId = SpatialIndexIdUtil.createVehicleSpatialIndexId(previousVehicle, feedProvider);
                if (!oldSpatialIndexId.equals(spatialIndexId)) {
                    spatialIndicesToRemove.add(oldSpatialIndexId);
                }
            }
            spatialIndexUpdateMap.put(spatialIndexId, vehicle);
        });

        spatialIndicesToRemove.addAll(
                vehicleIdsToRemove.stream()
                        .map(vehicleId -> getVehicleCacheKey(vehicleId, feedProvider))
                        .filter(currentVehicles::containsKey)
                        .map(vehicleCacheKey -> SpatialIndexIdUtil.createVehicleSpatialIndexId(currentVehicles.get(vehicleCacheKey), feedProvider))
                        .collect(Collectors.toSet())
        );

        if (!spatialIndicesToRemove.isEmpty()) {
            logger.debug("Removing {} stale entries in spatial index", spatialIndicesToRemove.size());
            spatialIndex.removeAll(spatialIndicesToRemove);
        }

        if (!vehicleIdsToRemove.isEmpty()) {
            logger.debug("Removing {} vehicles from vehicle cache", vehicleIdsToRemove.size());
            vehicleCache.removeAll(vehicleIdsToRemove.stream().map(id -> getVehicleCacheKey(id, feedProvider)).collect(Collectors.toSet()));
        }

        if (!vehicles.isEmpty()) {
            logger.debug("Adding/updating {} vehicles in vehicle cache", vehicles.size());
            var lastUpdated = freeBikeStatusFeed.getLastUpdated();
            var ttl = freeBikeStatusFeed.getTtl();
            vehicleCache.updateAll(vehicles, CacheUtil.getTtl(lastUpdated, ttl, 300), TimeUnit.SECONDS);
        }

        if (!spatialIndexUpdateMap.isEmpty()) {
            logger.debug("Updating {} entries in spatial index", spatialIndexUpdateMap.size());
            spatialIndex.addAll(spatialIndexUpdateMap);
        }
    }

    private Map<String, VehicleType> getVehicleTypes(GBFSVehicleTypes vehicleTypesFeed, Map<String, PricingPlan> pricingPlans, String language) {
        return vehicleTypesFeed.getData().getVehicleTypes().stream()
                .map(vehicleType -> vehicleTypeMapper.mapVehicleType(vehicleType, new ArrayList<>(pricingPlans.values()), language))
                .collect(Collectors.toMap(VehicleType::getId, i -> i));
    }

    private org.entur.lamassu.model.entities.System getSystem(FeedProvider feedProvider, GBFSSystemInformation systemInformationFeed) {
        return systemMapper.mapSystem(systemInformationFeed.getData(), feedProvider);
    }

    private Map<String, PricingPlan> getPricingPlans(GBFSSystemPricingPlans pricingPlansFeed, String language) {
        return pricingPlansFeed.getData().getPlans().stream()
                .map(pricingPlan -> pricingPlanMapper.mapPricingPlan(pricingPlan, language))
                .collect(Collectors.toMap(PricingPlan::getId, i -> i));
    }


    private String getVehicleCacheKey(String vehicleId, FeedProvider feedProvider) {
        return vehicleId + "_" + feedProvider.getSystemId();
    }
}
