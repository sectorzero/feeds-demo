package org.sectorzero.core.feeds;

import com.google.inject.Singleton;
import org.apache.commons.lang.Validate;
import org.sectorzero.core.articles.ArticleRef;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class FeedViewAggregator {

  /**
   * Merge-Sort Aggregate max N from a list of feed streams ( all in-memory structures )
   *
   * @param feeds
   * @param requiredSz
   * @return
   */
  public List<ArticleRef> aggregate(List<Iterator<ArticleRef>> feeds, int requiredSz) {

    // Create an online sorter for the list of feed-views
    PriorityQueue<ComparableArticleRefIterator> sorter = new PriorityQueue<>();
    feeds.stream().forEach(f -> {
      ComparableArticleRefIterator o = new ComparableArticleRefIterator(f);
      if(o.hasNext()) {
        sorter.add(o);
      }});

    // Collect the required number of unique articles from merge order of all the feeds
    HashMap<String, ArticleRef> collector = new HashMap<>();
    while(sorter.size() > 0 && collector.size() < requiredSz) {
      ComparableArticleRefIterator o = sorter.poll();
      ArticleRef r = o.next();
      if(o.hasNext()) {
        sorter.add(o);
      }
      collector.put(r.getArticleId(), r);
    }

    List<ArticleRef> result = new ArrayList<>();
    result.addAll(collector.values());
    Collections.sort(result);
    Collections.reverse(result);
    return result;
  }

  /**
   * An Iterator wrapper which allows to compare between the head of each iterator stream ( customized to
   * use the timestamp field of ArticleRef for ordering )
   */
  static class ComparableArticleRefIterator implements Comparable<ComparableArticleRefIterator>, Iterator<ArticleRef> {
    final Iterator<ArticleRef> it;
    ArticleRef curr;

    public ComparableArticleRefIterator(Iterator<ArticleRef> it) {
      this.it = it;
      this.curr = (it.hasNext()) ? it.next() : null;
    }

    @Override
    public boolean hasNext() {
      return curr != null;
    }

    @Override
    public ArticleRef next() {
      ArticleRef tmp = curr;
      curr = (it.hasNext()) ? it.next() : null;
      return tmp;
    }

    @Override
    public int compareTo(ComparableArticleRefIterator o) {
      Validate.notNull(o);
      ArticleRef self = curr;
      ArticleRef other = o.curr;
      return self.compareTo(other);
    }
  }

}
