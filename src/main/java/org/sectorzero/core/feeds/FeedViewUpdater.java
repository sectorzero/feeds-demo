package org.sectorzero.core.feeds;

import lombok.Getter;
import org.sectorzero.components.KafkaBrokerConfig;
import org.sectorzero.core.articles.ArticleRef;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import static org.sectorzero.utils.DataUtils.*;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeedViewUpdater {

  static final Duration defaultPollFrequency = Duration.ofMillis(1000);

  final String feed;
  @Getter final FeedView feedView;
  final KafkaConsumer<String, Long> feedConsumer;
  final ExecutorService updater;
  final Duration pollFrequency;
  final Clock clock;

  @Inject
  public FeedViewUpdater(
      @Assisted String feed,
      @Assisted FeedView feedView,
      @Named("FeedsKafkaBrokerConfig") KafkaBrokerConfig brokerConfig,
      @Assisted Duration pollFrequency,
      Clock clock) {
    this.feed = feed;
    this.feedView = feedView;
    this.feedConsumer = getConsumer(feed, feed, brokerConfig.getBrokerString());
    this.updater = Executors.newSingleThreadExecutor();
    this.pollFrequency = (pollFrequency.compareTo(defaultPollFrequency) < 0) ? defaultPollFrequency : pollFrequency;
    this.clock = clock;
  }

  public void init() {
    // This optimization is required for scale. But unfortunately I am not getting it
    // to work as this fails to work well when the system is restarted. So ignoring this
    // for now to achieve correctness. The feed consumer just reads from the beginning
    // of the feed log and populates a last n window in memory
    // resetOffsetToLastN(feedConsumer, 1000);
  }

  public void start() {
    init();
    updater.submit(() -> {
      receive(feedConsumer, r -> {
        ArticleRef a = new ArticleRef(
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(r.value()), clock.getZone()),
            r.key());
        feedView.append(a);
      });
    });
  }

  public void stop() {
    updater.shutdown();
  }

  KafkaConsumer<String, Long> getConsumer(String groupId, String topic, String kafkaBrokerString) {
    KafkaConsumer<String, Long> consumer = new KafkaConsumer<>(p(m(
        "bootstrap.servers", kafkaBrokerString,
        "group.id", groupId,
        "auto.offset.reset", "earliest",
        "enable.auto.commit", Boolean.FALSE.toString(),
        "auto.commit.interval.ms", "1000",
        "session.timeout.ms", "30000",
        "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
        "value.deserializer", "org.apache.kafka.common.serialization.LongDeserializer"
    )));
    consumer.subscribe(ImmutableList.of(topic));
    return consumer;
  }

  void resetOffsetToLastN(KafkaConsumer<String, Long> consumer, int n) {
    if(n <= 0) {
      return;
    }
    // Set the offset to approximately to (last - n) distributing evenly across partitions
    List<PartitionInfo> parts = consumer.partitionsFor(feed);
    int part_n = n / parts.size();
    for(PartitionInfo p : parts) {
      TopicPartition tp = new TopicPartition(p.topic(), p.partition());
      try {
        long pos = consumer.position(tp);
        consumer.seek(tp, pos - part_n);
      } catch (Exception e) {
        log.error("Exception trying to set inital position for topic-partition for consumer, Ignoring : consumer={}, TopicPartition={}", consumer, tp, e);
      }
    }
  }

  void receive(KafkaConsumer<String, Long> consumer, Consumer<ConsumerRecord<String, Long>> f) {
    while(true) {
      try {
        ConsumerRecords<String, Long> records = consumer.poll(1000);
        if (records == null) {
          continue;
        }
        if(records.count() > 0) {
          log.info("[POLL]:  Feed:{}, Consumer:{}, Records:{}", feed, consumer, records.count());
        } else {
          log.debug("[POLL]:  Feed:{}, Consumer:{}, Records:{}", feed, consumer, records.count());
        }
        for (ConsumerRecord<String, Long> r : records) {
          f.accept(r);
        }
      } catch (Exception e) {
        log.error("[POLL]: Unknown Exception Occurred, Ignoring and continuing to poll, for Feed={}, Consumer={}", e);
      }
    }
  }

}
