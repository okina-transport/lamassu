package org.entur.lamassu.config.v3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gbfs.global")
@Getter
@Setter
public class GlobalFeedConfiguration {

  private boolean enabled = false;
  private String email = "";
  private String feedContactEmail = "";
  private String systemIdPrefix = "lmd_";
  private String timezone = "Europe/Paris";
  private String language = "fr";
  private String systemName = "";
  private String openingHours = "Mo-Su 00:00-24:00";
  private Integer timeToLive = 300;
  private String hostUrl = "";
  private String defaultStationId = "DEFAULT";
}
