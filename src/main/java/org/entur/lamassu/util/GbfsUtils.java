package org.entur.lamassu.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;

public class GbfsUtils {

  public static List<GBFSFeedName> convertToGBFSFeedNameList(Object input) {
    if (input == null) {
      return Collections.emptyList();
    }

    if (input instanceof List) {
      return ((List<?>) input).stream()
        .map(GbfsUtils::convertToGBFSFeedName)
        .collect(Collectors.toList());
    }

    if (input instanceof String) {
      String strInput = (String) input;
      if (strInput.isEmpty()) {
        return Collections.emptyList();
      }
      return Arrays
        .stream(strInput.split(","))
        .map(String::trim)
        .map(GBFSFeedName::fromValue)
        .collect(Collectors.toList());
    }

    throw new IllegalArgumentException(
      "Unsupported input type: " + input.getClass().getName()
    );
  }

  private static GBFSFeedName convertToGBFSFeedName(Object item) {
    if (item instanceof GBFSFeedName) {
      return (GBFSFeedName) item;
    }
    if (item instanceof String) {
      return GBFSFeedName.fromValue((String) item);
    }
    throw new IllegalArgumentException("Cannot convert to GBFSFeedName: " + item);
  }
}
