package org.sectorzero.components;

import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.Time;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.io.File;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Properties;

import static java.lang.String.format;
import static org.sectorzero.utils.DataUtils.*;
import static org.sectorzero.utils.SetupUtils.createDirIfNotExists;

@Slf4j
public class KafkaBroker {

  @Getter final KafkaBrokerConfig config;
  final Zookeeper zookeeper;
  final KafkaConfig kafkaServerConfig;
  final KafkaServer kafkaServer;
  final File logDir;
  final Clock clock;

  public KafkaBroker(
      KafkaBrokerConfig config,
      Zookeeper zookeeper,
      Clock clock) throws Exception {
    this.zookeeper = zookeeper;

    this.config = config;
    this.logDir = createDirIfNotExists(config.getLogDir());
    this.kafkaServerConfig = new KafkaConfig(p(m(
        "zookeeper.connect", format("localhost:%d", config.getZkConfig().getPort()),
        "broker.id", format("%d", config.getBrokerId()),
        "host.name", "localhost",
        "port", format("%d", config.getPort()),
        "auto.create.topics.enable", "true",
        "log.dir", logDir.getAbsolutePath(),
        "log.flush.interval.messages", String.valueOf(1))));

    this.clock = clock;
    ZonedDateTime now = ZonedDateTime.now(clock);

    this.kafkaServer = new KafkaServer(
        kafkaServerConfig,
        new Time() {
          @Override
          public long milliseconds() {
            return now.toInstant().toEpochMilli();
          }
          @Override
          public long nanoseconds() {
            return now.toInstant().getNano();
          }
          @Override
          public void sleep(long l) {
            try {
              Thread.sleep(l);
            } catch (InterruptedException e) {
              Thread.interrupted();
            }
          }
        },
        scala.Option.apply("kafka"));
  }

  public void start() throws Exception {
    try {
      log.info("Starting Kafka Broker, config={}", kafkaServerConfig);
      zookeeper.start();
      kafkaServer.startup();
      log.info("Started Kafka Broker, config={}", kafkaServerConfig);
    } catch (Exception e) {
      throw new RuntimeException(format("Unable to start Kafka Broker, config=%s", config), e);
    }
  }

  public void stop() throws Exception {
    log.info("Stopping Kafka Broker, config={}", kafkaServerConfig);
    kafkaServer.shutdown();
    zookeeper.stop();
    log.info("Started Kafka Broker, config={}", kafkaServerConfig);
  }

  public void createTopic(String topicName, int numPartitions, int replication, Properties topicConfig) {
    int sessionTimeoutMs = 10 * 1000;
    int connectTimeoutMs = 2 * 1000;
    ZkClient zkClient = new ZkClient(
        zookeeper.getConfig().getZookeeperString(),
        sessionTimeoutMs,
        connectTimeoutMs,
        ZKStringSerializer$.MODULE$);
    ZkUtils zkUtils = new ZkUtils(
        zkClient,
        new ZkConnection(zookeeper.getConfig().getZookeeperString()),
        false);
    AdminUtils.createTopic(
        zkUtils,
        topicName,
        numPartitions,
        replication,
        topicConfig);
  }

}
