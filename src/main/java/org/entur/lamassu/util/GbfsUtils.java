package org.entur.lamassu.util;

import java.util.List;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;

public class GbfsUtils {

  private GbfsUtils() {}

  public static List<GBFSFeedName> convertToGBFSFeedNameList(List<String> input) {
    return input.stream().map(GbfsUtils::convertToGBFSFeedName).toList();
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
