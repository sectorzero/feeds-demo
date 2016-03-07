package org.sectorzero.app.configuration.guice;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import org.sectorzero.app.configuration.FeedsServiceConfiguration;
import org.sectorzero.components.KafkaBrokerConfig;
import org.sectorzero.components.ZookeeperConfig;
import org.sectorzero.components.KafkaBroker;
import org.sectorzero.components.Zookeeper;
import org.sectorzero.core.feeds.FeedViewUpdater;
import org.sectorzero.core.feeds.FeedViewUpdaterFactory;
import org.sectorzero.core.feeds.FeedsConfig;

import java.time.Clock;

public class FeedsModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
        .implement(FeedViewUpdater.class, FeedViewUpdater.class)
        .build(FeedViewUpdaterFactory.class));
  }

  @Provides
  @Singleton
  @Named("FeedsConfig")
  public FeedsConfig getFeedsConfig(FeedsServiceConfiguration config) {
    return new FeedsConfig(
        new ImmutableSet.Builder<String>()
            .add("f_1")
            .add("f_2")
            .add("f_3")
            .add("f_4")
            .add("f_5")
            .add("f_6")
            .add("f_7")
            .add("f_8")
            .build(),
        100,
        1000);
  }

  @Provides
  @Singleton
  @Named("FeedsZookeeperConfig")
  public ZookeeperConfig getZookeeperConfig(FeedsServiceConfiguration config) {
    return new ZookeeperConfig(
        10992,
        "/tmp/feeds-demo/zookeeper/snapshot",
        "/tmp/feeds-demo/zookeeper/log",
        500);
  }

  @Provides
  @Singleton
  @Named("FeedsZookeeper")
  public Zookeeper getZookeeper(
      @Named("FeedsZookeeperConfig") ZookeeperConfig zkConfig) throws Exception {
    return new Zookeeper(zkConfig);
  }

  @Provides
  @Singleton
  @Named("FeedsKafkaBrokerConfig")
  public KafkaBrokerConfig getKafkaBrokerConfig(
      FeedsServiceConfiguration config,
      @Named("FeedsZookeeperConfig") ZookeeperConfig zkConfig) {
    return new KafkaBrokerConfig(
        1,
        10993,
        zkConfig,
        "/tmp/feeds-demo/kafka");
  }

  @Provides
  @Singleton
  @Named("FeedsKafkaBroker")
  public KafkaBroker getKafkaBroker(
      @Named("FeedsKafkaBrokerConfig") KafkaBrokerConfig config,
      @Named("FeedsZookeeper") Zookeeper zookeeper,
      Clock clock) throws Exception {
    return new KafkaBroker(config, zookeeper, clock);
  }

}
