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

package io.dropwizard.maxmind.geoip2.filter;

import com.google.common.base.Strings;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.*;
import com.maxmind.geoip2.record.*;
import io.dropwizard.maxmind.geoip2.cache.MaxMindCache;
import io.dropwizard.maxmind.geoip2.config.MaxMindConfig;
import io.dropwizard.maxmind.geoip2.core.MaxMindHeaders;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * @author phaneesh
 */
@Slf4j
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class MaxMindGeoIpRequestFilter implements ContainerRequestFilter {

    private final MaxMindConfig config;

    private MaxMindCache maxMindCache;

    public MaxMindGeoIpRequestFilter(MaxMindConfig config) {
        this.config = config;
        try {
            DatabaseReader databaseReader = new DatabaseReader.Builder(new File(config.getDatabaseFilePath())).build();
            this.maxMindCache = new MaxMindCache(config, databaseReader);
        } catch (IOException e) {
            log.error("Error initializing GeoIP database", e);
        }
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        final String clientAddress = containerRequestContext.getHeaders().getFirst(config.getRemoteIpHeader());
        if(Strings.isNullOrEmpty(clientAddress)) {
            return;
        }
        final String clientIp = clientAddress.split(":")[0];
        InetAddress address;
        if (!Strings.isNullOrEmpty(clientIp)) {
            try {
                address = InetAddress.getByName(clientIp);
            } catch (Exception e) {
                log.warn("Cannot resolve address: {} | Error: {}", clientIp, e.getMessage());
                return;
            }
            if (config.isEnterprise() && address != null) {
                final EnterpriseResponse enterpriseResponse = maxMindCache.enterprise(address);
                if(enterpriseResponse == null) return;
                if (enterpriseResponse.getCountry() != null) {
                    addCountryInfo(enterpriseResponse.getCountry(), containerRequestContext);
                }
                if (enterpriseResponse.getMostSpecificSubdivision() != null) {
                    addStateInfo(enterpriseResponse.getMostSpecificSubdivision(), containerRequestContext);
                }
                if (enterpriseResponse.getCity() != null) {
                    addCityInfo(enterpriseResponse.getCity(), containerRequestContext);
                }
                if (enterpriseResponse.getPostal() != null) {
                    addPostalInfo(enterpriseResponse.getPostal(), containerRequestContext);
                }
                if (enterpriseResponse.getLocation() != null) {
                    addLocationInfo(enterpriseResponse.getLocation(), containerRequestContext);
                }
                if (enterpriseResponse.getTraits() != null) {
                    addTraitsInfo(enterpriseResponse.getTraits(), containerRequestContext);
                }

                AnonymousIpResponse anonymousIpResponse = maxMindCache.anonymousIp(address);
                if (anonymousIpResponse != null) {
                    anonymousInfo(anonymousIpResponse, containerRequestContext);
                }
            } else {
                switch (config.getType()) {
                    case "country":
                        CountryResponse countryResponse = maxMindCache.country(address);
                        if (countryResponse!= null && countryResponse.getCountry() != null) {
                            addCountryInfo(countryResponse.getCountry(), containerRequestContext);
                        }
                        break;
                    case "city":
                        CityResponse cityResponse = maxMindCache.city(address);
                        if(cityResponse != null) {
                            if (cityResponse.getMostSpecificSubdivision() != null) {
                                addStateInfo(cityResponse.getMostSpecificSubdivision(), containerRequestContext);
                            }
                            if (cityResponse.getCity() != null) {
                                addCityInfo(cityResponse.getCity(), containerRequestContext);
                            }
                            if (cityResponse.getPostal() != null) {
                                addPostalInfo(cityResponse.getPostal(), containerRequestContext);
                            }
                            if (cityResponse.getLocation() != null) {
                                addLocationInfo(cityResponse.getLocation(), containerRequestContext);
                            }
                        }
                        break;
                    case "anonymous":
                        AnonymousIpResponse anonymousIpResponse = maxMindCache.anonymousIp(address);
                        if (anonymousIpResponse != null) {
                            anonymousInfo(anonymousIpResponse, containerRequestContext);
                        }
                }
            }
        }
    }


    private void addCountryInfo(Country country, final ContainerRequestContext containerRequestContext) {
        if (Strings.isNullOrEmpty(country.getName()))
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_COUNTRY, country.getName());
    }

    private void addStateInfo(Subdivision subdivision, final ContainerRequestContext containerRequestContext) {
        if (Strings.isNullOrEmpty(subdivision.getName()))
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_STATE, subdivision.getName());
    }

    private void addCityInfo(City city, final ContainerRequestContext containerRequestContext) {
        if (Strings.isNullOrEmpty(city.getName()))
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_CITY, city.getName());
    }

    private void addPostalInfo(Postal postal, final ContainerRequestContext containerRequestContext) {
        if (Strings.isNullOrEmpty(postal.getCode()))
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_POSTAL, postal.getCode());
    }

    private void addLocationInfo(Location location, final ContainerRequestContext containerRequestContext) {
        if (location.getLatitude() != null)
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_LATITUDE, String.valueOf(location.getLatitude()));
        if (location.getLongitude() != null)
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_LONGITUDE, String.valueOf(location.getLongitude()));
        if (location.getAccuracyRadius() != null)
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_LOCATION_ACCURACY, String.valueOf(location.getAccuracyRadius()));
    }

    private void addTraitsInfo(Traits traits, final ContainerRequestContext containerRequestContext) {
        if (Strings.isNullOrEmpty(traits.getUserType()))
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_USER_TYPE, traits.getUserType());
        if (Strings.isNullOrEmpty(traits.getIsp()))
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_ISP, traits.getIsp());
        if (traits.getConnectionType() != null)
            containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_CONNECTION_TYPE, traits.getConnectionType().name());
        containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_PROXY_LEGAL, String.valueOf(traits.isLegitimateProxy()));
    }

    private void anonymousInfo(AnonymousIpResponse anonymousIpResponse, final ContainerRequestContext containerRequestContext) {
        containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_ANONYMOUS_IP, String.valueOf(anonymousIpResponse.isAnonymous()));
        containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_ANONYMOUS_VPN, String.valueOf(anonymousIpResponse.isAnonymousVpn()));
        containerRequestContext.getHeaders().putSingle(MaxMindHeaders.X_TOR, String.valueOf(anonymousIpResponse.isTorExitNode()));
    }

}