package io.dropwizard.maxmind.geoip2.feature;

import io.dropwizard.maxmind.geoip2.provider.MaxMindContextValueParamProvider;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class MaxMindContextFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(MaxMindContextValueParamProvider.class)
                        .to(ValueParamProvider.class);
            }
        });

        return true;
    }
}
