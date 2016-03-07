package org.sectorzero.core.feeds;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang.Validate;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.sectorzero.components.KafkaBrokerConfig;
import org.sectorzero.core.articles.ArticleRef;
import org.sectorzero.components.KafkaBroker;

import com.google.inject.name.Named;

import java.util.Properties;

import static org.sectorzero.utils.DataUtils.*;

@Singleton
public class FeedsQueuePersistentStore {

  final FeedsConfig feedsConfig;
  final KafkaBroker feedsKafkaBroker;
  final KafkaProducer<String, Long> feedsKafkaProducer;

  @Inject
  public FeedsQueuePersistentStore(
      @Named("FeedsConfig") FeedsConfig feedsConfig,
      @Named("FeedsKafkaBroker") KafkaBroker feedsKafkaBroker) {
    this.feedsConfig = feedsConfig;
    this.feedsKafkaBroker = feedsKafkaBroker;
    this.feedsKafkaProducer = createProducer(feedsKafkaBroker.getConfig());
  }

  public void init() {
    for(String feed : feedsConfig.getFeeds()) {
      feedsKafkaBroker.createTopic(feed, 1, 0, new Properties());
    }
  }

  public void append(String feed, ArticleRef articleRef) {
    Validate.isTrue(feedsConfig.getFeeds().contains(feed));
    feedsKafkaProducer.send(
        new ProducerRecord<>(
            feed,
            articleRef.getArticleId(),
            articleRef.getTimestamp().toInstant().toEpochMilli()));
  }

  KafkaProducer<String, Long> createProducer(KafkaBrokerConfig brokerConfig) {
    return new KafkaProducer<>(p(m(
        "bootstrap.servers", brokerConfig.getBrokerString(),
        "acks", "all",
        "retries", "0",
        "batch.size", "16384",
        "linger.ms", "1",
        "buffer.memory", "33554432",
        "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer", "org.apache.kafka.common.serialization.LongSerializer"
    )));
  }

  public void start() throws Exception {
    feedsKafkaBroker.start();
  }

  public void stop() throws Exception {
    feedsKafkaBroker.stop();
  }

}
