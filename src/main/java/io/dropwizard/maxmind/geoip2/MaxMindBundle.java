/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dropwizard.maxmind.geoip2;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.maxmind.geoip2.config.MaxMindConfig;
import io.dropwizard.maxmind.geoip2.filter.MaxMindGeoIpRequestFilter;
import io.dropwizard.maxmind.geoip2.provider.MaxMindInfoProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * @author phaneesh
 */
public abstract class MaxMindBundle<T extends Configuration> implements ConfiguredBundle<T> {

    public abstract MaxMindConfig getMaxMindConfig(final T configuration);

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(final T configuration, final Environment environment) {
        MaxMindConfig maxMindConfig = getMaxMindConfig(configuration);
        environment.jersey().register(new MaxMindGeoIpRequestFilter(maxMindConfig));
        if(maxMindConfig.isMaxMindContext()) {
            environment.jersey().register(new MaxMindInfoProvider.Binder());
        }
    }
}
