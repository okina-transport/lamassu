package org.entur.lamassu.mapper.feedidmapper;

import org.entur.lamassu.model.provider.FeedProvider;

public interface FeedIdMapper<T> {
  void mapIds(T mapped, FeedProvider feedProvider, boolean toOriginalId);
}
