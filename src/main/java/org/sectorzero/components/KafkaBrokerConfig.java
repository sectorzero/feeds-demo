package org.sectorzero.components;

import lombok.Value;

import static java.lang.String.format;

@Value
public class KafkaBrokerConfig {
  int brokerId;
  int port;
  ZookeeperConfig zkConfig;
  String logDir;

  public String getBrokerString() {
    return format("localhost:%d", port);
  }
}
