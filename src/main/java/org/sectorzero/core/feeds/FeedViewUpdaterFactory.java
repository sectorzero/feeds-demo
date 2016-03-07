package org.sectorzero.core.feeds;

import java.time.Duration;

public interface FeedViewUpdaterFactory {
  FeedViewUpdater getInstance(String feed, FeedView feedView, Duration pollFrequency);
}
