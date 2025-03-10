package org.entur.lamassu.service.impl;

import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.StationFilterParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.util.SpatialIndexIdFilter;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GeoSearchServiceImpl implements GeoSearchService {

    private final VehicleSpatialIndex vehicleSpatialIndex;
    private final StationSpatialIndex stationSpatialIndex;
    private final VehicleCache vehicleCache;
    private final StationCache stationCache;

    @Autowired
    public GeoSearchServiceImpl(VehicleSpatialIndex vehicleSpatialIndex, StationSpatialIndex stationSpatialIndex, VehicleCache vehicleCache, StationCache stationCache) {
        this.vehicleSpatialIndex = vehicleSpatialIndex;
        this.stationSpatialIndex = stationSpatialIndex;
        this.vehicleCache = vehicleCache;
        this.stationCache = stationCache;
    }

    @Override
    public List<Vehicle> getVehiclesNearby(RangeQueryParameters rangeQueryParameters, VehicleFilterParameters vehicleFilterParameters) {
        Double longitude = rangeQueryParameters.getLon();
        Double latitude = rangeQueryParameters.getLat();
        Double range = rangeQueryParameters.getRange();
        Integer count = rangeQueryParameters.getCount();

        List<VehicleSpatialIndexId> indexIds = vehicleSpatialIndex.radius(longitude, latitude, range, GeoUnit.METERS, GeoOrder.ASC);

        var stream = indexIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> SpatialIndexIdFilter.filterVehicle(id, vehicleFilterParameters));

        if (count != null) {
            stream = stream.limit(count.longValue());
        }

        Set<String> vehicleIds = stream.map(this::getVehicleCacheKey)
                .collect(Collectors.toSet());

        return vehicleCache.getAll(vehicleIds);
    }

    private String getVehicleCacheKey(VehicleSpatialIndexId spatialIndexId) {
        return spatialIndexId.getId() + "_" + spatialIndexId.getSystemId();
    }

    @Override
    public List<Station> getStationsNearby(RangeQueryParameters rangeQueryParameters, StationFilterParameters filterParameters) {
        Double longitude = rangeQueryParameters.getLon();
        Double latitude = rangeQueryParameters.getLat();
        Double range = rangeQueryParameters.getRange();
        Integer count = rangeQueryParameters.getCount();

        List<StationSpatialIndexId> indexIds = stationSpatialIndex.radius(longitude, latitude, range, GeoUnit.METERS, GeoOrder.ASC);

        var stream = indexIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> SpatialIndexIdFilter.filterStation(id, filterParameters));

        if (count != null) {
            stream = stream.limit(count.longValue());
        }

        Set<String> stationIds = stream.map(StationSpatialIndexId::getId).collect(Collectors.toSet());

        return stationCache.getAll(stationIds);
    }

    @Override
    public Collection<String> getVehicleSpatialIndexOrphans() {
        var indexIds = vehicleSpatialIndex.getAll();
        return indexIds.stream()
                .filter(Objects::nonNull)
                .map(this::getVehicleCacheKey)
                .filter(key -> !vehicleCache.hasKey(key))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> removeVehicleSpatialIndexOrphans() {
        var indexIds = vehicleSpatialIndex.getAll();
        var orphans = indexIds.stream()
                .filter(Objects::nonNull)
                .filter(indexId -> {
                    var cacheKey = getVehicleCacheKey(indexId);
                    return !vehicleCache.hasKey(cacheKey);
                })
                .collect(Collectors.toSet());

        vehicleSpatialIndex.removeAll(orphans);

        return orphans.stream().map(this::getVehicleCacheKey).collect(Collectors.toList());
    }
}
