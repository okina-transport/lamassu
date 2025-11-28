package org.entur.lamassu.controller.converters;

import org.entur.lamassu.model.provider.GbfsModality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GbfsModalityConverter implements Converter<String, GbfsModality> {

  private static final Logger LOG = LoggerFactory.getLogger(GbfsModalityConverter.class);

  @Override
  public GbfsModality convert(String source) {
    try {
      return GbfsModality.fromValue(source);
    } catch (Exception e) {
      LOG.error("Unable to convert {} to GbfsModality", source);
      return GbfsModality.GLOBAL;
    }
  }
}
