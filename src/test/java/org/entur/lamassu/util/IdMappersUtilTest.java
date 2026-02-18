package org.entur.lamassu.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IdMappersUtilTest {

  @Test
  void testMapId_nullId() {
    Map<String, String> map = Map.of("A", "B");
    assertNull(IdMappersUtil.mapId(null, map));
  }

  @Test
  void testMapId_notMapped() {
    Map<String, String> map = Map.of("A", "B");
    assertEquals("X", IdMappersUtil.mapId("X", map));
  }

  @Test
  void testMapId_mapped() {
    Map<String, String> map = Map.of("A", "B");
    assertEquals("B", IdMappersUtil.mapId("A", map));
  }

  @Test
  void testMapIdsList_nullOrEmpty() {
    assertNull(IdMappersUtil.mapIds(null, Map.of()));
    assertTrue(IdMappersUtil.mapIds(List.of(), Map.of()).isEmpty());
  }

  @Test
  void testMapIdsList_mapping() {
    List<String> ids = List.of("A", "X");
    Map<String, String> map = Map.of("A", "B");

    List<String> result = IdMappersUtil.mapIds(ids, map);

    assertEquals(List.of("B", "X"), result);
  }

  @Test
  void testMapIdsMap_nullOrEmpty() {
    assertNull(IdMappersUtil.mapIdsMap((Map<String, Double>) null, Map.of()));
    assertTrue(IdMappersUtil.mapIdsMap(Map.of(), Map.of()).isEmpty());
  }

  @Test
  void testMapIdsMap_mapping() {
    Map<String, Double> ids = Map.of("A", 1.0, "X", 2.0);
    Map<String, String> map = Map.of("A", "B");

    Map<String, Double> result = IdMappersUtil.mapIdsMap(ids, map);

    assertEquals(2, result.size());
    assertEquals(1.0, result.get("B"));
    assertEquals(2.0, result.get("X"));
  }

  @Test
  void testEnturSuperToOriginalId_null() {
    assertNull(IdMappersUtil.enturSuperToOriginalIdMapping(null));
  }

  @Test
  void testEnturSuperToOriginalId_noSeparator() {
    assertEquals("ABC", IdMappersUtil.enturSuperToOriginalIdMapping("ABC"));
  }

  @Test
  void testEnturSuperToOriginalId_validSuperId() {
    assertEquals("123", IdMappersUtil.enturSuperToOriginalIdMapping("ENT:Station:123"));
  }

  @Test
  void testEnturSuperToOriginalId_unknownFormat() {
    assertEquals("A:B", IdMappersUtil.enturSuperToOriginalIdMapping("A:B"));
  }

  @Test
  void testEnturSuperToOriginalIdsMapping_empty() {
    assertTrue(IdMappersUtil.enturSuperToOriginalIdsMapping(Set.of()).isEmpty());
  }

  @Test
  void testEnturSuperToOriginalIdsMapping_valid() {
    Set<String> ids = Set.of("ENT:Station:1", "X");
    Map<String, String> result = IdMappersUtil.enturSuperToOriginalIdsMapping(ids);

    assertEquals(2, result.size());
    assertEquals("1", result.get("ENT:Station:1"));
    assertEquals("X", result.get("X"));
  }

  @Test
  void testEnturOriginalToSuperIdsMapping_empty() {
    FeedProvider provider = Mockito.mock(FeedProvider.class);
    assertTrue(
      IdMappersUtil
        .enturOriginalToSuperIdsMapping(Set.of(), provider, "Station")
        .isEmpty()
    );
  }

  @Test
  void testEnturOriginalToSuperIdsMapping_valid() {
    FeedProvider provider = Mockito.mock(FeedProvider.class);
    Mockito.when(provider.getCodespace()).thenReturn("ENT");

    Set<String> ids = Set.of("1", "2");

    Map<String, String> result = IdMappersUtil.enturOriginalToSuperIdsMapping(
      ids,
      provider,
      "Station"
    );

    assertEquals("ENT:Station:1", result.get("1"));
    assertEquals("ENT:Station:2", result.get("2"));
  }
}
