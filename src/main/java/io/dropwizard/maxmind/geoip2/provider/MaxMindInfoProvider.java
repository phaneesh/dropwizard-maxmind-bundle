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
import io.dropwizard.maxmind.geoip2.Characters;
import io.dropwizard.maxmind.geoip2.core.MaxMindHeaders;
import io.dropwizard.maxmind.geoip2.core.MaxMindInfo;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * @author phaneesh
 */
@Slf4j
@Singleton
@Provider
public class MaxMindInfoProvider extends AbstractValueFactoryProvider {


    public static final class InjectResolver extends ParamInjectionResolver<MaxMindContext> {

        public InjectResolver() {
            super(MaxMindInfoProvider.class);
        }
    }

    private static final class MaxMinfInfoParamValueFactory extends AbstractContainerRequestValueFactory<MaxMindInfo> {

        @Context
        private ResourceContext context;

        public MaxMindInfo provide() {
            final HttpServletRequest request = context.getResource(HttpServletRequest.class);
            final String anonymousIp = request.getHeader(MaxMindHeaders.X_ANONYMOUS_IP);
            final String anonymousVpn = request.getHeader(MaxMindHeaders.X_ANONYMOUS_VPN);
            final String tor = request.getHeader(MaxMindHeaders.X_TOR);
            final String city = request.getHeader(MaxMindHeaders.X_CITY);
            final String state = request.getHeader(MaxMindHeaders.X_STATE);
            final String stateIso = request.getHeader(MaxMindHeaders.X_STATE_ISO);
            final String postal = request.getHeader(MaxMindHeaders.X_POSTAL);
            final String connectionType = request.getHeader(MaxMindHeaders.X_CONNECTION_TYPE);
            final String userType = request.getHeader(MaxMindHeaders.X_USER_TYPE);
            final String country = request.getHeader(MaxMindHeaders.X_COUNTRY);
            final String countryIso = request.getHeader(MaxMindHeaders.X_COUNTRY_ISO);
            final String isp = request.getHeader(MaxMindHeaders.X_ISP);
            final String latitude = request.getHeader(MaxMindHeaders.X_LATITUDE);
            final String longitude = request.getHeader(MaxMindHeaders.X_LONGITUDE);
            final String accuracy = request.getHeader(MaxMindHeaders.X_LOCATION_ACCURACY);
            return MaxMindInfo.builder()
                    .anonymousIp(Strings.isNullOrEmpty(anonymousIp) ? false : Boolean.valueOf(anonymousIp))
                    .anonymousVpn(Strings.isNullOrEmpty(anonymousVpn) ? false : Boolean.valueOf(anonymousVpn))
                    .tor(Strings.isNullOrEmpty(tor) ? false : Boolean.valueOf(tor))
                    .city(Strings.isNullOrEmpty(city) ? "UNKNOWN" : city)
                    .state(Strings.isNullOrEmpty(state) ? "UNKNOWN" : state)
                    .stateIso(Strings.isNullOrEmpty(state) ? "UNKNOWN" : stateIso)
                    .country(Strings.isNullOrEmpty(country) ? "UNKNOWN" : country)
                    .countryIso(Strings.isNullOrEmpty(country) ? "UNKNOWN" : countryIso)
                    .postal(Strings.isNullOrEmpty(postal) ? "UNKNOWN" : postal)
                    .connectionType(Strings.isNullOrEmpty(connectionType) ? "UNKNOWN" : connectionType)
                    .userType(Strings.isNullOrEmpty(userType) ? "UNKNOWN" : userType)
                    .isp(Strings.isNullOrEmpty(isp) ? "UNKNOWN" : isp)
                    .latitude(Strings.isNullOrEmpty(latitude) ? 0 : Double.valueOf(latitude))
                    .longitude(Strings.isNullOrEmpty(longitude) ? 0 : Double.valueOf(longitude))
                    .accuracy(Strings.isNullOrEmpty(accuracy) ? 0 : Integer.valueOf(accuracy))
                    .build();
        }
    }

    @Inject
    public MaxMindInfoProvider(MultivaluedParameterExtractorProvider extractorProvider, ServiceLocator locator) {
        super(extractorProvider, locator, Parameter.Source.UNKNOWN);
    }

    @Override
    protected AbstractContainerRequestValueFactory<?> createValueFactory(Parameter parameter) {
        Class<?> classType = parameter.getRawType();

        if (classType == null || (!classType.equals(MaxMindInfo.class))) {
            log.warn("MaxMindContext annotation was not placed on correct object type; Injection might not work correctly!");
            return null;
        }
        return new MaxMinfInfoParamValueFactory();
    }

}
