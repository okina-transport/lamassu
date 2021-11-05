package org.entur.lamassu.listener.delegates;

import org.entur.gbfs.v2_2.free_bike_status.GBFSBike;
import org.entur.gbfs.v2_2.free_bike_status.GBFSFreeBikeStatus;
import org.entur.gbfs.v2_2.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_2.system_information.GBFSSystemInformation;
import org.entur.gbfs.v2_2.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleTypes;
import org.entur.lamassu.cache.GBFSFeedCacheV2;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.entitymapper.PricingPlanMapper;
import org.entur.lamassu.mapper.entitymapper.SystemMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleTypeMapper;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FreeBikeStatusListenerDelegate implements CacheEntryListenerDelegate<Object, GBFSFreeBikeStatus> {
    private final VehicleCache vehicleCache;
    private final GBFSFeedCacheV2 feedCache;
    private final FeedProviderService feedProviderService;
    private final VehicleSpatialIndex spatialIndex;
    private final SystemMapper systemMapper;
    private final PricingPlanMapper pricingPlanMapper;
    private final VehicleTypeMapper vehicleTypeMapper;
    private final VehicleMapper vehicleMapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public FreeBikeStatusListenerDelegate(
            VehicleCache vehicleCache,
            GBFSFeedCacheV2 feedCache,
            FeedProviderService feedProviderService,
            VehicleSpatialIndex spatialIndex,
            VehicleMapper vehicleMapper,
            SystemMapper systemMapper,
            PricingPlanMapper pricingPlanMapper,
            VehicleTypeMapper vehicleTypeMapper
    ) {
        this.vehicleCache = vehicleCache;
        this.feedCache = feedCache;
        this.feedProviderService = feedProviderService;
        this.spatialIndex = spatialIndex;
        this.vehicleMapper = vehicleMapper;
        this.systemMapper = systemMapper;
        this.pricingPlanMapper = pricingPlanMapper;
        this.vehicleTypeMapper = vehicleTypeMapper;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        for (var event : iterable) {
            addOrUpdateVehicles(event);
        }
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        for (var event : iterable) {
            addOrUpdateVehicles(event);
        }
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        // noop
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        // noop
    }

    private void addOrUpdateVehicles(CacheEntryEvent<? extends String, ?> event) {
        var split = event.getKey().split("_");
        var feedProvider = feedProviderService.getFeedProviderBySystemId(split[split.length - 1]);
        var freeBikeStatusFeed = (GBFSFreeBikeStatus) event.getValue();

        var systemInformationFeed = (GBFSSystemInformation) feedCache.find(GBFSFeedName.SystemInformation, feedProvider);
        var pricingPlansFeed = (GBFSSystemPricingPlans) feedCache.find(GBFSFeedName.SystemPricingPlans, feedProvider);
        var vehicleTypesFeed = (GBFSVehicleTypes) feedCache.find(GBFSFeedName.VehicleTypes, feedProvider);

        if (freeBikeStatusFeed.getData() == null) {
            logger.warn("freeBikeStatusFeed has no data! provider={} feed={}", feedProvider, freeBikeStatusFeed);
            return;
        }

        var vehicleIds = freeBikeStatusFeed.getData().getBikes().stream()
                .map(GBFSBike::getBikeId)
                .collect(Collectors.toSet());

        Set<String> vehicleIdsToRemove = null;

        if (event.isOldValueAvailable()) {
            var oldFreeBikeStatusFeed = (GBFSFreeBikeStatus) event.getOldValue();

            if (oldFreeBikeStatusFeed.getData() != null) {
                vehicleIdsToRemove = oldFreeBikeStatusFeed.getData().getBikes().stream()
                        .map(GBFSBike::getBikeId).collect(Collectors.toSet());

                // Find vehicle ids in old feed not present in new feed
                vehicleIdsToRemove.removeAll(vehicleIds);
                logger.debug("Found {} vehicleIds to remove from old free_bike_status feed: {}", vehicleIdsToRemove.size(), oldFreeBikeStatusFeed);

                // Add vehicle ids that are staged for removal to the set of vehicle ids that will be used to
                // fetch current vehicles from cache
                vehicleIds.addAll(vehicleIdsToRemove);
            }
        }

        if (vehicleIdsToRemove == null) {
            vehicleIdsToRemove = new HashSet<>(vehicleIds);
            logger.info("Old free_bike_status feed was not available or had no data. As a workaround, removing all vehicles for provider {}", feedProvider.getSystemId());
        }

        var currentVehicles = vehicleCache.getAllAsMap(
                vehicleIds.stream()
                        .map(id -> getVehicleCacheKey(id, feedProvider))
                        .collect(Collectors.toSet())
        );

        var vehicleTypes = getVehicleTypes(feedProvider, vehicleTypesFeed);

        if (vehicleTypes.isEmpty()) {
            logger.warn("no vehicle types provider={} feed={}", feedProvider, vehicleTypesFeed);
            return;
        }

        var system = getSystem(feedProvider, systemInformationFeed);

        if (system == null) {
            logger.warn("no system information provider={} feed={}", feedProvider, systemInformationFeed);
            return;
        }

        var pricingPlans = getPricingPlans(feedProvider, pricingPlansFeed);

        if (pricingPlans.isEmpty()) {
            logger.warn("no pricing plans provider={} feed={}", feedProvider, pricingPlansFeed);
            return;
        }

        var vehicles = freeBikeStatusFeed.getData().getBikes().stream()
                .filter(new VehicleFilter(pricingPlans, vehicleTypes))
                .map(vehicle -> vehicleMapper.mapVehicle(
                        vehicle,
                        vehicleTypes.get(vehicle.getVehicleTypeId()),
                        pricingPlans.get(vehicle.getPricingPlanId()),
                        system
                ))
                .collect(Collectors.toMap(v -> getVehicleCacheKey(v.getId(), feedProvider), v -> v));

        Set<String> spatialIndicesToRemove = new java.util.HashSet<>(Set.of());
        Map<String, Vehicle> spatialIndexUpdateMap = new java.util.HashMap<>(Map.of());

        vehicles.forEach((key, vehicle) -> {
            var spatialIndexId = SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, feedProvider);
            var previousVehicle = currentVehicles.get(key);

            if (previousVehicle != null) {
                var oldSpatialIndexId = SpatialIndexIdUtil.createVehicleSpatialIndexId(previousVehicle, feedProvider);
                if (!oldSpatialIndexId.equalsIgnoreCase(spatialIndexId)) {
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
            vehicleCache.updateAll(vehicles);
        }

        if (!spatialIndexUpdateMap.isEmpty()) {
            logger.debug("Updating {} entries in spatial index", spatialIndexUpdateMap.size());
            spatialIndex.addAll(spatialIndexUpdateMap);
        }
    }
    
    private Map<String, VehicleType> getVehicleTypes(FeedProvider feedProvider, GBFSVehicleTypes vehicleTypesFeed) {
        if (vehicleTypesFeed == null) {
            logger.warn("Missing vehicle types feed for provider {}", feedProvider);
            return Map.of();
        }

        if (vehicleTypesFeed.getData() == null) {
            logger.warn("Missing vehicle types data for provider={} feed={}", feedProvider, vehicleTypesFeed);
            return Map.of();
        }

        return vehicleTypesFeed.getData().getVehicleTypes().stream()
                .map(vehicleType -> vehicleTypeMapper.mapVehicleType(vehicleType, feedProvider.getLanguage()))
                .collect(Collectors.toMap(VehicleType::getId, i -> i));
    }

    private org.entur.lamassu.model.entities.System getSystem(FeedProvider feedProvider, GBFSSystemInformation systemInformationFeed) {
        if (systemInformationFeed == null) {
            logger.warn("Missing system information feed for provider {}", feedProvider);
            return null;
        }

        if (systemInformationFeed.getData() == null) {
            logger.warn("Missing system information data for provider={} feed={}", feedProvider, systemInformationFeed);
            return null;
        }

        return systemMapper.mapSystem(systemInformationFeed.getData(), feedProvider);
    }

    private Map<String, PricingPlan> getPricingPlans(FeedProvider feedProvider, GBFSSystemPricingPlans pricingPlansFeed) {
        if (pricingPlansFeed == null) {
            logger.warn("Missing pricing plans feed for provider {}", feedProvider);
            return Map.of();
        }

        if (pricingPlansFeed.getData() == null) {
            logger.warn("Missing pricing plans data for provider={} feed={}", feedProvider, pricingPlansFeed);
            return Map.of();
        }

        return pricingPlansFeed.getData().getPlans().stream()
                .map(pricingPlan -> pricingPlanMapper.mapPricingPlan(pricingPlan, feedProvider.getLanguage()))
                .collect(Collectors.toMap(PricingPlan::getId, i -> i));
    }


    private String getVehicleCacheKey(String vehicleId, FeedProvider feedProvider) {
        return vehicleId + "_" + feedProvider.getSystemId();
    }

}
