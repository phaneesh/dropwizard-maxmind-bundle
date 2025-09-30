/*
 * Copyright (c) 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.dropwizard.maxmind.geoip2.provider;

import com.google.common.base.Strings;
import io.dropwizard.maxmind.geoip2.core.MaxMindHeaders;
import io.dropwizard.maxmind.geoip2.core.MaxMindInfo;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.function.Function;

/**
 * @author phaneesh
 */
@Slf4j
@Singleton
public class MaxMindInfoProvider extends AbstractValueParamProvider {

    public static final String UNKNOWN = "UNKNOWN";

    /**
     * Injection resolver for {@link MaxMindContext} annotation.
     */
    @Singleton
    public static final class MaxMindContextInjectionResolver extends ParamInjectionResolver<MaxMindContext> {
        @Inject
        public MaxMindContextInjectionResolver(MaxMindInfoProvider valueParamProvider,
                                               Provider<ContainerRequest> request) {
            super(valueParamProvider, MaxMindContext.class, request);
        }
    }

    /**
     * Factory that provides MaxMindInfo instances by extracting headers from the request.
     */
    private static MaxMindInfo createMaxMindInfo(ContainerRequest request) {
        final String anonymousIp = request.getHeaderString(MaxMindHeaders.X_ANONYMOUS_IP);
        final String anonymousVpn = request.getHeaderString(MaxMindHeaders.X_ANONYMOUS_VPN);
        final String tor = request.getHeaderString(MaxMindHeaders.X_TOR);
        final String city = request.getHeaderString(MaxMindHeaders.X_CITY);
        final String state = request.getHeaderString(MaxMindHeaders.X_STATE);
        final String stateIso = request.getHeaderString(MaxMindHeaders.X_STATE_ISO);
        final String postal = request.getHeaderString(MaxMindHeaders.X_POSTAL);
        final String connectionType = request.getHeaderString(MaxMindHeaders.X_CONNECTION_TYPE);
        final String userType = request.getHeaderString(MaxMindHeaders.X_USER_TYPE);
        final String country = request.getHeaderString(MaxMindHeaders.X_COUNTRY);
        final String countryIso = request.getHeaderString(MaxMindHeaders.X_COUNTRY_ISO);
        final String isp = request.getHeaderString(MaxMindHeaders.X_ISP);
        final String latitude = request.getHeaderString(MaxMindHeaders.X_LATITUDE);
        final String longitude = request.getHeaderString(MaxMindHeaders.X_LONGITUDE);
        final String accuracy = request.getHeaderString(MaxMindHeaders.X_LOCATION_ACCURACY);

        return MaxMindInfo.builder()
                .anonymousIp(!Strings.isNullOrEmpty(anonymousIp) && Boolean.parseBoolean(anonymousIp))
                .anonymousVpn(!Strings.isNullOrEmpty(anonymousVpn) && Boolean.parseBoolean(anonymousVpn))
                .tor(!Strings.isNullOrEmpty(tor) && Boolean.parseBoolean(tor))
                .city(Strings.isNullOrEmpty(city) ? UNKNOWN : city)
                .state(Strings.isNullOrEmpty(state) ? UNKNOWN : state)
                .stateIso(Strings.isNullOrEmpty(stateIso) ? UNKNOWN : stateIso)
                .country(Strings.isNullOrEmpty(country) ? UNKNOWN : country)
                .countryIso(Strings.isNullOrEmpty(countryIso) ? UNKNOWN : countryIso)
                .postal(Strings.isNullOrEmpty(postal) ? UNKNOWN : postal)
                .connectionType(Strings.isNullOrEmpty(connectionType) ? UNKNOWN : connectionType)
                .userType(Strings.isNullOrEmpty(userType) ? UNKNOWN : userType)
                .isp(Strings.isNullOrEmpty(isp) ? UNKNOWN : isp)
                .latitude(Strings.isNullOrEmpty(latitude) ? 0 : Double.parseDouble(latitude))
                .longitude(Strings.isNullOrEmpty(longitude) ? 0 : Double.parseDouble(longitude))
                .accuracy(Strings.isNullOrEmpty(accuracy) ? 0 : Integer.parseInt(accuracy))
                .build();
    }


    @Inject
    public MaxMindInfoProvider(Provider<MultivaluedParameterExtractorProvider> mpep) {
        super(mpep, org.glassfish.jersey.model.Parameter.Source.UNKNOWN);
    }

    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        Class<?> classType = parameter.getRawType();
        if (classType == null || (!classType.equals(MaxMindInfo.class))) {
            log.warn("MaxMindContext annotation was not placed on correct object type; Injection might not work correctly!");
            return null;
        }
        return MaxMindInfoProvider::createMaxMindInfo;
    }


    /**
     * Binder for registering the MaxMindInfo provider and injection resolver.
     * This should be registered in your application's Jersey configuration.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(MaxMindInfoProvider.class)
                    .to(ValueParamProvider.class)
                    .in(Singleton.class);
            bind(MaxMindContextInjectionResolver.class)
                    .to(new TypeLiteral<InjectionResolver<MaxMindContext>>() {})
                    .in(Singleton.class);
        }
    }
}