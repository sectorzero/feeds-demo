package org.sectorzero.core.feeds;

import com.google.common.collect.Iterators;
import org.sectorzero.core.articles.ArticleRef;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FeedViewStore {

  final Map<String, FeedView> feeds;

  public FeedViewStore(Set<String> feedSet, int maxEntriesPerFeed) {
    this.feeds = new HashMap<>();
    for(String f : feedSet) {
      feeds.put(f, new FeedView(maxEntriesPerFeed));
    }
  }

  public Iterator<ArticleRef> getArticlesForFeed(String feed) {
    if(!feeds.containsKey(feed)) {
      return Iterators.emptyIterator();
    }
    return feeds.get(feed).get();
  }

}
