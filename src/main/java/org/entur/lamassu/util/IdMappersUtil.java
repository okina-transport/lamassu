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

package org.entur.lamassu.util;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.entur.lamassu.model.provider.FeedProvider;

public class IdMappersUtil {

  public static final String STATION_ID_TYPE = "Station";
  public static final String REGION_ID_TYPE = "Region";
  public static final String ALERT_ID_TYPE = "Alert";
  public static final String PRICING_PLAN_ID_TYPE = "PricingPlan";
  public static final String VEHICLE_TYPE_ID_TYPE = "VehicleType";
  public static final String BIKE_ID_TYPE = "Vehicle";

  private IdMappersUtil() {}

  public static @Nullable String mapId(
    @Nullable String id,
    @NonNull Map<String, String> idMap
  ) {
    if (id == null) {
      return null;
    }
    return idMap.getOrDefault(id, id);
  }

  public static @Nullable List<String> mapIds(
    @Nullable List<String> ids,
    @NonNull Map<String, String> idMap
  ) {
    if (CollectionUtils.isEmpty(ids)) {
      return ids;
    }
    return ids.stream().map(id -> mapId(id, idMap)).toList();
  }

  public static @Nullable Map<String, Double> mapIdsMap(
    @Nullable Map<String, Double> ids,
    @NonNull Map<String, String> idMap
  ) {
    if (MapUtils.isEmpty(ids)) {
      return ids;
    }
    Map<String, Double> mappedIds = new HashMap<>();
    for (var entry : ids.entrySet()) {
      mappedIds.put(mapId(entry.getKey(), idMap), entry.getValue());
    }
    return mappedIds;
  }

  public static String enturSuperToOriginalIdMapping(String superId) {
    if (superId == null) {
      return null;
    }
    String[] split = superId.split(":", 3);
    if (split.length == 1) {
      return split[0];
    }
    if (split.length == 3) {
      return split[2];
    }
    // unknown superId format
    return superId;
  }

  public static Map<String, String> enturSuperToOriginalIdsMapping(Set<String> superIds) {
    if (CollectionUtils.isEmpty(superIds)) {
      return new HashMap<>();
    }
    Map<String, String> idsMap = new HashMap<>();
    for (String superId : superIds) {
      idsMap.put(superId, IdMappersUtil.enturSuperToOriginalIdMapping(superId));
    }
    return idsMap;
  }

  public static Map<String, String> enturOriginalToSuperIdsMapping(
    Set<String> originalIds,
    @NonNull FeedProvider feedProvider,
    @NonNull String entityType
  ) {
    if (CollectionUtils.isEmpty(originalIds)) {
      return new HashMap<>();
    }
    Map<String, String> idsMap = new HashMap<>();
    for (String originalId : originalIds) {
      idsMap.put(
        originalId,
        String.format("%s:%s:%s", feedProvider.getCodespace(), entityType, originalId)
      );
    }
    return idsMap;
  }
}
