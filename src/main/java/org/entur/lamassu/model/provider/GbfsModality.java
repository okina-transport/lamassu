package org.entur.lamassu.model.provider;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GbfsModality {
  CYCLE_RENTAL("cycleRental", "vélo libre service"),
  CAR_SHARING("carsharing", "auto-partage"),
  CARPOOL("carpool", "co-voiturage"),
  SCOOTER("scooter", "trottinette"),
  PARKING("parking", "parking"),
  GLOBAL("global", "");

  private final String value;
  private final String frenchLabel;

  GbfsModality(String value, String label) {
    this.value = value;
    this.frenchLabel = label;
  }

  public static GbfsModality fromValue(String value) {
    for (GbfsModality s : values()) {
      if (s.getValue().equals(value)) {
        return s;
      }
    }
    throw new IllegalArgumentException("Invalid status value: " + value);
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
