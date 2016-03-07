package org.sectorzero.core.feeds;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sectorzero.core.articles.ArticleRef;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FeedViewAggregatorTest {

  FeedViewAggregator aggregator;
  FeedView a;
  FeedView b;
  @Mock Clock clock;

  @Before
  public void setUp() throws Exception {
    initMocks(this);

    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(clock.instant()).thenAnswer(invocation -> {
      long randomEpoch = ThreadLocalRandom.current().nextLong(1455237505, 1455239999);
      return Instant.ofEpochMilli(randomEpoch);
    });
    a = generateFeedView("A", 10);
    b = generateFeedView("B", 10);

    aggregator = new FeedViewAggregator();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void foo() {
    List<Iterator<ArticleRef>> articleLists =
        new ImmutableList.Builder<Iterator<ArticleRef>>()
            .add(a.get())
            .add(b.get())
            .build();
    List<ArticleRef> result = aggregator.aggregate(
        articleLists,
        10);
    assertEquals(10, result.size());
  }

  FeedView generateFeedView(String feedLabel, int n) {
    FeedView f = new FeedView(n);
    IntStream.range(0, n)
        .forEach(i -> f.append(
            new ArticleRef(
                ZonedDateTime.now(clock),
                String.format("article:%s:%d", feedLabel, i))));
    return f;
  }

}