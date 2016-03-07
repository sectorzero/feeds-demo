package org.sectorzero.core.feeds;

import lombok.Value;

import java.util.Set;

@Value
public class FeedsConfig {
  Set<String> feeds;
  int maxEntriesPerFeedView;
  long feedsPollFrequencyMs;
}
