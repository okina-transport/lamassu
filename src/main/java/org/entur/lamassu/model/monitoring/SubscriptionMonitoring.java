package org.entur.lamassu.model.monitoring;

import lombok.Data;

@Data
public class SubscriptionMonitoring {

  private String dataset;
  private String dataType = "GBFS";
  private String httpStatus;
  private String producerUrl;
}
