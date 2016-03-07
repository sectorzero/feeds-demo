package org.sectorzero.components;

import lombok.Value;

import static java.lang.String.format;

@Value
public class ZookeeperConfig {
  int port;
  String snapshotDir;
  String logDir;
  int tickTime;

  public String getZookeeperString() {
    return format("localhost:%d", port);
  }
}
