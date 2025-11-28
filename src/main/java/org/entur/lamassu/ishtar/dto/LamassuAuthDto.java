package org.entur.lamassu.ishtar.dto;

import java.util.Map;
import lombok.Data;
import org.entur.lamassu.model.provider.AuthenticationScheme;

@Data
public class LamassuAuthDto {

  private AuthenticationScheme type;
  private Map<String, String> properties;
}
