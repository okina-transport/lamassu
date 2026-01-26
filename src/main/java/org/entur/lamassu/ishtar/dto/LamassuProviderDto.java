package org.entur.lamassu.ishtar.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.entur.lamassu.model.provider.GbfsModality;

@Data
public class LamassuProviderDto {

  private String systemId;
  private String operatorId;
  private String operatorName;
  private String codespace;
  private String url;
  private String language;
  private String version;
  private List<String> excludeFeeds;
  private Boolean aggregate;
  private Map<String, String> vehicleTypes;
  private Map<String, String> pricingPlans;
  private LamassuAuthDto authentication;
  private GbfsModality modality;
}
