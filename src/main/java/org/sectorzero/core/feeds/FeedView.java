package org.sectorzero.core.feeds;

import org.sectorzero.core.articles.ArticleRef;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class FeedView {

  final int maxEntries;
  final ConcurrentLinkedDeque<ArticleRef> buffer;
  final AtomicInteger bufferSize;
  final ReentrantLock appendLock;

  public FeedView(int maxEntries) {
    this.maxEntries = maxEntries;
    this.buffer = new ConcurrentLinkedDeque<>();
    this.bufferSize = new AtomicInteger(0);
    this.appendLock = new ReentrantLock();
  }

  public void append(ArticleRef articleRef) {
    try {
      appendLock.lock();
      if(bufferSize.get() == maxEntries) {
        buffer.removeFirst();
        buffer.addLast(articleRef);
      } else {
        buffer.addLast(articleRef);
      }
    } finally {
      appendLock.unlock();
    }
  }

  Iterator<ArticleRef> get() {
    return buffer.descendingIterator();
  }

  // TODO : Deal with NULL result
  ZonedDateTime lastRefTimestamp() {
    ArticleRef last = buffer.peekLast();
    if(last == null) {
      return null;
    }
    return last.getTimestamp();
  }

}
