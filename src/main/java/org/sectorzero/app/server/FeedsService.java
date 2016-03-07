package org.sectorzero.app.server;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import io.dropwizard.lifecycle.Managed;
import org.sectorzero.app.configuration.FeedsServiceConfiguration;
import org.sectorzero.app.configuration.guice.FeedsServiceConfigurationModule;
import org.sectorzero.app.configuration.guice.FeedsModule;
import org.sectorzero.resources.AggregateResource;
import org.sectorzero.resources.FeedsResource;
import org.sectorzero.servizio.app.BaseService;

import java.util.List;

public class FeedsService extends BaseService<FeedsServiceConfiguration> {

  public static void main(String[] args) throws Exception {
    new FeedsService().run(args);
  }

  @Override
  public String getName() {
    return "Feeds Service";
  }

  @Override
  protected List<AbstractModule> userGuiceModules() {
    return new ImmutableList.Builder<AbstractModule>()
        .add(new FeedsServiceConfigurationModule())
        .add(new FeedsModule())
        .build();
  }

  @Override
  protected List<Class<? extends Managed>> userLifecycleManagedClasses() {
    return new ImmutableList.Builder<Class <? extends Managed>>()
        .add(FeedsQueueManaged.class)
        .build();
  }

  @Override
  protected List<Class> userResourceClasses() {
    return new ImmutableList.Builder<Class>()
        .add(FeedsResource.class)
        .add(AggregateResource.class)
        .build();
  }
}
