package org.entur.lamassu.model.monitoring;

import lombok.Data;

@Data
public class InputSubscriptionData {

  private String dataset;
  private String dataType = "GBFS";
  private long nbElements;
}
