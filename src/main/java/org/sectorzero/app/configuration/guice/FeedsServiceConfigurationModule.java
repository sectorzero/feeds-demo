package org.sectorzero.app.configuration.guice;

import com.google.inject.AbstractModule;

import java.time.Clock;

public class FeedsServiceConfigurationModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Clock.class).toInstance(Clock.systemUTC());
  }

}
