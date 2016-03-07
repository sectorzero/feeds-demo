package org.sectorzero.components;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.sectorzero.utils.SetupUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class Zookeeper {

  @Getter final ZookeeperConfig config;
  final File snapshotDir;
  final File logDir;
  final int tickTime;
  final ServerCnxnFactory factory;
  final ZooKeeperServer zkServer;

  public Zookeeper(ZookeeperConfig config) throws Exception {
    this.config = config;
    this.snapshotDir = SetupUtils.createDirIfNotExists(config.getSnapshotDir());
    this.logDir = SetupUtils.createDirIfNotExists(config.getLogDir());
    this.tickTime = config.getTickTime();
    this.zkServer = new ZooKeeperServer(snapshotDir, logDir, tickTime);
    this.factory = NIOServerCnxnFactory.createFactory();
    this.factory.configure(new InetSocketAddress("localhost", config.getPort()), 1024);
  }

  public void start() throws Exception {
    try {
      log.info("Starting Zookeeper, Port={}", config.getPort());
      factory.startup(zkServer);
      log.info("Started Zookeeper, Port={}", config.getPort());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      throw new RuntimeException("Unable to start ZooKeeper", e);
    }
  }

  public void stop() throws Exception {
    log.info("Stopping Zookeeper, Port={}", config.getPort());
    factory.shutdown();
    log.info("Stopped Zookeeper, Port={}", config.getPort());
  }

}
