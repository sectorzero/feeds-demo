package org.sectorzero.core.feeds;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class FeedsView {

  @Getter final FeedsConfig config;
  @Getter final FeedViewStore feedViewStore;
  @Getter final Map<String, FeedViewUpdater> feedViewUpdaters;
  final FeedViewUpdaterFactory feedViewUpdaterFactory;

  @Inject
  public FeedsView(
      @Named("FeedsConfig") FeedsConfig feedsConfig,
      FeedViewUpdaterFactory feedViewUpdaterFactory) {
    this.config = feedsConfig;
    this.feedViewStore = new FeedViewStore(
        feedsConfig.getFeeds(),
        feedsConfig.getMaxEntriesPerFeedView());
    this.feedViewUpdaterFactory = feedViewUpdaterFactory;
    this.feedViewUpdaters = new HashMap<>();
    for(String f : feedsConfig.getFeeds()) {
      FeedViewUpdater u = feedViewUpdaterFactory.getInstance(
          f,
          feedViewStore.feeds.get(f),
          Duration.ofMillis(feedsConfig.getFeedsPollFrequencyMs()));
      feedViewUpdaters.put(f, u);
    }
  }

  public void start() throws Exception {
    for(Map.Entry<String, FeedViewUpdater> e : feedViewUpdaters.entrySet()) {
      try {
        log.info("[FEED] Initializing FeedView for Feed={}", e.getKey());
        e.getValue().start();
        log.info("[FEED] Initialized FeedView for Feed={}", e.getKey());
      } catch (Exception ex) {
        log.error("[FEED] Exception Initializing Feed={}", e.getKey(), ex);
        throw ex;
      }
    }
  }

  public void stop() throws Exception {
    for(Map.Entry<String, FeedViewUpdater> e : feedViewUpdaters.entrySet()) {
      try {
        log.info("[FEED] Closing FeedView for Feed={}", e.getKey());
        e.getValue().stop();
        log.info("[FEED] Closed FeedView for Feed={}", e.getKey());
      } catch (Exception ex) {
        log.error("[FEED] Exception Closing Feed={}", e.getKey(), ex);
      }
    }
  }

}
