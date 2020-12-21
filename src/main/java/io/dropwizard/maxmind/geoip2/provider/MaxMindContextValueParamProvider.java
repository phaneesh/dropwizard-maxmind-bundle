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
import org.glassfish.hk2.api.*;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author phaneesh
 */
@Slf4j
@Singleton
@Provider
public class MaxMindContextValueParamProvider implements ValueParamProvider {

    @Override
    public Function<ContainerRequest, ?> getValueProvider(Parameter parameter) {
        if (parameter.getRawType().equals(MaxMindInfo.class)
                && parameter.isAnnotationPresent(MaxMindContext.class)) {
            return new MaxMindInfoParamProvider();
        }
        log.info("class and annotation not compatible");
        return null;
    }
    public static final class MaxMindInfoParamProvider implements Function<ContainerRequest,MaxMindInfo> {

        @Override
        public MaxMindInfo apply(@Context ContainerRequest request) {
            //final HttpServletRequest request = context.getResource(HttpServletRequest.class);
            log.info("container request = {}",request);
            if (request.equals(null)){
                log.info("request is null");
            }
            final String anonymousIp = getHeader(request,MaxMindHeaders.X_ANONYMOUS_IP);
            final String anonymousVpn = getHeader(request,MaxMindHeaders.X_ANONYMOUS_VPN);
            final String tor = getHeader(request,MaxMindHeaders.X_TOR);
            final String city = getHeader(request,MaxMindHeaders.X_CITY);
            final String state = getHeader(request,MaxMindHeaders.X_STATE);
            final String stateIso = getHeader(request,MaxMindHeaders.X_STATE_ISO);
            final String postal = getHeader(request,MaxMindHeaders.X_POSTAL);
            final String connectionType = getHeader(request,MaxMindHeaders.X_CONNECTION_TYPE);
            final String userType = getHeader(request,MaxMindHeaders.X_USER_TYPE);
            final String country = getHeader(request,MaxMindHeaders.X_COUNTRY);
            final String countryIso = getHeader(request,MaxMindHeaders.X_COUNTRY_ISO);
            final String isp = getHeader(request,MaxMindHeaders.X_ISP);
            final String latitude = getHeader(request,MaxMindHeaders.X_LATITUDE);
            final String longitude = getHeader(request,MaxMindHeaders.X_LONGITUDE);
            final String accuracy = getHeader(request,MaxMindHeaders.X_LOCATION_ACCURACY);
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
        private String getHeader(ContainerRequest request,String name) {
            List<String> headerList = request.getRequestHeader(name);
            return Optional.ofNullable(headerList)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .findFirst()
                    .orElse(null);
        }
    }
    @Override
    public PriorityType getPriority() {
        return Priority.HIGH;
    }
}
