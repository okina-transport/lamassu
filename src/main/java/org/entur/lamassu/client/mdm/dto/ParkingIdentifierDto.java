package org.entur.lamassu.client.mdm.dto;

import lombok.Data;

@Data
public class ParkingIdentifierDto {

  private String operator;
  private String originalId;
  private String countryCode;
  private String insee;
  private String superId;
}
