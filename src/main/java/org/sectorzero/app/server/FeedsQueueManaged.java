package org.sectorzero.app.server;

import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;
import org.sectorzero.core.feeds.FeedsQueuePersistentStore;
import org.sectorzero.core.feeds.FeedsView;

@Slf4j
public class FeedsQueueManaged implements Managed {

  final FeedsView feedsView;
  final FeedsQueuePersistentStore feedsQueuePersistentStore;

  @Inject
  public FeedsQueueManaged(
      FeedsView feedsView,
      FeedsQueuePersistentStore feedsQueuePersistentStore) throws Exception {
    this.feedsView = feedsView;
    this.feedsQueuePersistentStore = feedsQueuePersistentStore;
  }

  @Override
  public void start() throws Exception {
    feedsQueuePersistentStore.start();
    feedsView.start();
  }

  @Override
  public void stop() throws Exception {
    feedsView.stop();
    feedsQueuePersistentStore.stop();
  }

}
