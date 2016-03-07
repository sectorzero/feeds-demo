package org.sectorzero.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.Validate;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

@Slf4j
public class KafkaTest {

  static final int TEST_LOCAL_ZOOKEEPER_PORT = 9902;
  static final int TEST_LOCAL_BROKER_PORT = 9903;

  static final String TEST_TOPIC_FOO = "foo";
  static final String TEST_TOPIC_BAR = "bar";

  @Rule
  public KafkaUnitRule kafkaUnitRule = new KafkaUnitRule(TEST_LOCAL_ZOOKEEPER_PORT, TEST_LOCAL_BROKER_PORT);

  KafkaUnit instance;

  @Before
  public void setup() throws Exception {
    instance = kafkaUnitRule.getKafkaUnit();
    instance.createTopic(TEST_TOPIC_FOO);
    instance.createTopic(TEST_TOPIC_BAR);
    Thread.sleep(1000);
  }

  @Test
  public void read_messages_from_multiple_consumers() throws Exception {
    send(messages(TEST_TOPIC_FOO, 1000));
    Thread.sleep(1000);

    KafkaConsumer<String, String> c_1 = consumer("cg-1", ImmutableList.of(TEST_TOPIC_FOO), false);
    List<ConsumerRecord> result_1 = receive(c_1, 1000);
    assertEquals(1000, result_1.size());

    KafkaConsumer<String, String> c_2 = consumer("cg-2", ImmutableList.of(TEST_TOPIC_FOO), false);
    List<ConsumerRecord> result_2 = receive(c_2, 1000);
    assertEquals(1000, result_2.size());
  }

  @Test
  public void reread_messages_from_same_consumer() throws Exception {
    send(messages(TEST_TOPIC_FOO, 1000));
    Thread.sleep(1000);

    KafkaConsumer<String, String> c_1 = consumer("cg-1", ImmutableList.of(TEST_TOPIC_FOO), false);

    // First Read Pass
    List<ConsumerRecord> result_1 = receive(c_1, 1000);
    assertEquals(1000, result_1.size());

    // Seek to beginning and read again
    c_1.seekToBeginning();
    List<ConsumerRecord> result_2 = receive(c_1, 1000);
    assertEquals(1000, result_2.size());
  }

  @Test
  public void last_n_messages() throws Exception {
    send(messages(TEST_TOPIC_FOO, 1000));
    Thread.sleep(1000);

    // First Read Pass
    KafkaConsumer<String, String> c_1 = consumer("cg-1", ImmutableList.of(TEST_TOPIC_FOO), false);
    List<ConsumerRecord> result_1 = receive(c_1, 1000);
    assertEquals(1000, result_1.size());

    // Set the offset to approximately to (last - n) distributing evenly across partitions
    int n = 100;
    List<PartitionInfo> parts = c_1.partitionsFor(TEST_TOPIC_FOO);
    int part_n = n / parts.size();
    for(PartitionInfo p : parts) {
      TopicPartition tp = new TopicPartition(p.topic(), p.partition());
      long pos = c_1.position(tp);
      c_1.seek(tp, pos - part_n);
    }

    // Read the last n
    List<ConsumerRecord> result_2 = receive(c_1, n);
    assertEquals(n, result_2.size());
  }

  List<ConsumerRecord> receive(KafkaConsumer<String, String> consumer, int n) {
    List<ConsumerRecord> result = new ArrayList<>();
    while(result.size() < n) {
      log.info("[POLL]: {}", consumer);
      ConsumerRecords<String, String> records = consumer.poll(1000);
      for(ConsumerRecord<String, String> r : records) {
        result.add(r);
      }
    }
    return result;
  }

  void send_2(List<KeyedMessage<String, String>> messages) {
    Producer<String, String> producer = new Producer<>(new ProducerConfig(p(m(
        "serializer.class", StringEncoder.class.getName(),
        "metadata.broker.list", format("localhost:%d", TEST_LOCAL_BROKER_PORT)))));
    producer.send(messages);
  }

  void send(List<KeyedMessage<String, String>> messages) {
    KafkaProducer<String, String> producer = createProducer();
    for(KeyedMessage<String, String> m : messages) {
      producer.send(new ProducerRecord<>(m.topic(), m.key(), m.message()));
    }
  }

  KafkaConsumer<String, String> consumer(String groupId, List<String> topics, boolean autoCommit) {
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(p(m(
        "bootstrap.servers", format("localhost:%d", TEST_LOCAL_BROKER_PORT),
        "group.id", groupId,
        "auto.offset.reset", "earliest",
        "enable.auto.commit", Boolean.toString(autoCommit),
        "auto.commit.interval.ms", "1000",
        "session.timeout.ms", "30000",
        "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
        "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"
    )));
    consumer.subscribe(topics);
    return consumer;
  }

  KafkaProducer<String, String> createProducer() {
    return new KafkaProducer<>(p(m(
        "bootstrap.servers", format("localhost:%d", TEST_LOCAL_BROKER_PORT),
        "acks", "all",
        "retries", "0",
        "batch.size", "16384",
        "linger.ms", "1",
        "buffer.memory", "33554432",
        "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer", "org.apache.kafka.common.serialization.StringSerializer"
    )));
  }

  List<KeyedMessage<String, String>> messages(String topic, int n) {
    return IntStream.range(0, n)
        .mapToObj(i -> new KeyedMessage<>(topic, format("k-%s-%d", topic, i), format("%d", i)))
        .collect(Collectors.<KeyedMessage<String, String>>toList());
  }

  Properties p(Map<String, String> records)
  {
    Properties properties = new Properties();
    records.entrySet().stream()
        .forEach(e -> properties.setProperty(e.getKey(), e.getValue()));
    return properties;
  }

  Map<String, String> m(String... elements) {
    Validate.notNull(elements);
    Validate.isTrue(elements.length % 2 == 0);
    if(elements.length == 0) {
      return ImmutableMap.of();
    }
    ImmutableMap.Builder<String, String> b = new ImmutableMap.Builder<>();
    for(int i = 0; i < elements.length; i += 2) {
      b.put(elements[i], elements[i+1]);
    }
    return b.build();
  }

}
