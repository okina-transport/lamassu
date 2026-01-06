package org.entur.lamassu.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;

public class GbfsUtils {

  private GbfsUtils() {}

  public static List<GBFSFeedName> convertToGBFSFeedNameList(Object input) {
    if (input == null) {
      return Collections.emptyList();
    }

    if (input instanceof List) {
      return ((List<?>) input).stream().map(GbfsUtils::convertToGBFSFeedName).toList();
    }

    if (input instanceof String strInput) {
      if (strInput.isEmpty()) {
        return Collections.emptyList();
      }
      return Arrays
        .stream(strInput.split(","))
        .map(String::trim)
        .map(GBFSFeedName::fromValue)
        .toList();
    }

    throw new IllegalArgumentException(
      "Unsupported input type: " + input.getClass().getName()
    );
  }

  private static GBFSFeedName convertToGBFSFeedName(Object item) {
    if (item instanceof GBFSFeedName gbfsFeedName) {
      return gbfsFeedName;
    }
    if (item instanceof String gbfsFeedName) {
      return GBFSFeedName.fromValue(gbfsFeedName);
    }
    throw new IllegalArgumentException("Cannot convert to GBFSFeedName: " + item);
  }
}
