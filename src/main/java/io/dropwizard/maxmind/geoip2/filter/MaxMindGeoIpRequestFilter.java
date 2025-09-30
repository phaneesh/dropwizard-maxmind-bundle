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
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.AnonymousIpResponse;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.model.EnterpriseResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.Subdivision;
import com.maxmind.geoip2.record.Traits;
import io.dropwizard.maxmind.geoip2.config.MaxMindConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_ANONYMOUS_IP;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_ANONYMOUS_VPN;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_CITY;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_CONNECTION_TYPE;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_COUNTRY;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_COUNTRY_ISO;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_ISP;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_LATITUDE;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_LOCATION_ACCURACY;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_LONGITUDE;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_MAXMIND_ERROR;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_POSTAL;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_PROXY_LEGAL;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_STATE;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_STATE_ISO;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_TOR;
import static io.dropwizard.maxmind.geoip2.core.MaxMindHeaders.X_USER_TYPE;

/**
 * @author phaneesh
 */
@Slf4j
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class MaxMindGeoIpRequestFilter implements ContainerRequestFilter {

    private final MaxMindConfig config;

    private DatabaseReader databaseReader;

    public MaxMindGeoIpRequestFilter(MaxMindConfig config) {
        this.config = config;
        try {
            this.databaseReader = new DatabaseReader.Builder(new File(config.getDatabaseFilePath()))
                    .withCache(new CHMCache())
                    .build();
        } catch (IOException e) {
            log.error("Error initializing GeoIP database", e);
        }
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) {
        final String clientAddress = containerRequestContext.getHeaders().getFirst(config.getRemoteIpHeader());
        if (Strings.isNullOrEmpty(clientAddress)) {
            return;
        }
        if (log.isDebugEnabled())
            log.debug("Header: {} | Value: {}", config.getRemoteIpHeader(), clientAddress);
        //Multiple Client ip addresses are being sent in case of multiple people stamping the request
        final String[] addresses = clientAddress.split(",");
        InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
        if (!inetAddressValidator.isValid(addresses[0])) {
            log.warn("Invalid IP Address: {}", addresses[0]);
            return;
        }
        final String clientIp = addresses[0];
        InetAddress address;
        if (!Strings.isNullOrEmpty(clientIp)) {
            try {
                address = InetAddress.getByName(clientIp);
            } catch (Exception e) {
                log.warn("Cannot resolve address: {} | Error: {}", clientIp, e.getMessage());
                return;
            }
            //Short circuit if there is no ip address
            if (address == null) {
                log.warn("Cannot resolve address: {}", clientIp);
                return;
            }
            try {
                if (config.isEnterprise()) {
                    final EnterpriseResponse enterpriseResponse = databaseReader.enterprise(address);
                    if (enterpriseResponse == null) return;
                    addInfo(containerRequestContext, enterpriseResponse.getCountry(), enterpriseResponse.getMostSpecificSubdivision(), enterpriseResponse.getCity(), enterpriseResponse.getPostal(), enterpriseResponse.getLocation());
                    if (enterpriseResponse.getTraits() != null) {
                        addTraitsInfo(enterpriseResponse.getTraits(), containerRequestContext);
                    }
                    AnonymousIpResponse anonymousIpResponse = databaseReader.anonymousIp(address);
                    if (anonymousIpResponse != null) {
                        anonymousInfo(anonymousIpResponse, containerRequestContext);
                    }
                } else {
                    addInfo(containerRequestContext, address);
                }
            } catch (Exception e) {
                log.warn("GeoIP Error: {}", e.getMessage());
            }
        }
    }

    private void addInfo(ContainerRequestContext containerRequestContext, InetAddress address) throws IOException, GeoIp2Exception {
        switch (config.getType()) {
            case "country":
                CountryResponse countryResponse = databaseReader.country(address);
                if (countryResponse != null && countryResponse.getCountry() != null) {
                    addCountryInfo(countryResponse.getCountry(), containerRequestContext);
                }
                break;
            case "city":
                CityResponse cityResponse = databaseReader.city(address);
                if (cityResponse != null) {
                    addInfo(containerRequestContext, cityResponse.getCountry(), cityResponse.getMostSpecificSubdivision(), cityResponse.getCity(), cityResponse.getPostal(), cityResponse.getLocation());
                }
                break;
            case "anonymous":
                AnonymousIpResponse anonymousIpResponse = databaseReader.anonymousIp(address);
                if (anonymousIpResponse != null) {
                    anonymousInfo(anonymousIpResponse, containerRequestContext);
                }
                break;
            default:
                containerRequestContext.getHeaders().putSingle(X_MAXMIND_ERROR, "UNKNOWN_TYPE");
        }
    }

    private void addInfo(ContainerRequestContext containerRequestContext, Country country, Subdivision mostSpecificSubdivision, City city, Postal postal, Location location) {
        if (country != null) {
            addCountryInfo(country, containerRequestContext);
        }
        if (mostSpecificSubdivision != null) {
            addStateInfo(mostSpecificSubdivision, containerRequestContext);
        }
        if (city != null) {
            addCityInfo(city, containerRequestContext);
        }
        if (postal != null) {
            addPostalInfo(postal, containerRequestContext);
        }
        if (location != null) {
            addLocationInfo(location, containerRequestContext);
        }
    }


    private void addCountryInfo(Country country, final ContainerRequestContext containerRequestContext) {
        if (!Strings.isNullOrEmpty(country.getName()))
            containerRequestContext.getHeaders().putSingle(X_COUNTRY, toAscii(country.getName()));
        if (!Strings.isNullOrEmpty(country.getIsoCode()))
            containerRequestContext.getHeaders().putSingle(X_COUNTRY_ISO, country.getIsoCode());
    }

    private void addStateInfo(Subdivision subdivision, final ContainerRequestContext containerRequestContext) {
        if (!Strings.isNullOrEmpty(subdivision.getName()))
            containerRequestContext.getHeaders().putSingle(X_STATE, toAscii(subdivision.getName()));
        if (!Strings.isNullOrEmpty(subdivision.getIsoCode()))
            containerRequestContext.getHeaders().putSingle(X_STATE_ISO, subdivision.getIsoCode());
    }

    private void addCityInfo(City city, final ContainerRequestContext containerRequestContext) {
        if (!Strings.isNullOrEmpty(city.getName()))
            containerRequestContext.getHeaders().putSingle(X_CITY, toAscii(city.getName()));
    }

    private void addPostalInfo(Postal postal, final ContainerRequestContext containerRequestContext) {
        if (!Strings.isNullOrEmpty(postal.getCode()))
            containerRequestContext.getHeaders().putSingle(X_POSTAL, postal.getCode());
    }

    private void addLocationInfo(Location location, final ContainerRequestContext containerRequestContext) {
        if (location.getLatitude() != null)
            containerRequestContext.getHeaders().putSingle(X_LATITUDE, String.valueOf(location.getLatitude()));
        if (location.getLongitude() != null)
            containerRequestContext.getHeaders().putSingle(X_LONGITUDE, String.valueOf(location.getLongitude()));
        if (location.getAccuracyRadius() != null)
            containerRequestContext.getHeaders().putSingle(X_LOCATION_ACCURACY, String.valueOf(location.getAccuracyRadius()));
    }

    private void addTraitsInfo(Traits traits, final ContainerRequestContext containerRequestContext) {
        if (!Strings.isNullOrEmpty(traits.getUserType()))
            containerRequestContext.getHeaders().putSingle(X_USER_TYPE, toAscii(traits.getUserType()));
        if (!Strings.isNullOrEmpty(traits.getIsp()))
            containerRequestContext.getHeaders().putSingle(X_ISP, toAscii(traits.getIsp()));
        if (traits.getConnectionType() != null)
            containerRequestContext.getHeaders().putSingle(X_CONNECTION_TYPE, toAscii(traits.getConnectionType().name()));
        containerRequestContext.getHeaders().putSingle(X_PROXY_LEGAL, String.valueOf(traits.isLegitimateProxy()));
    }

    private void anonymousInfo(AnonymousIpResponse anonymousIpResponse, final ContainerRequestContext containerRequestContext) {
        containerRequestContext.getHeaders().putSingle(X_ANONYMOUS_IP, String.valueOf(anonymousIpResponse.isAnonymous()));
        containerRequestContext.getHeaders().putSingle(X_ANONYMOUS_VPN, String.valueOf(anonymousIpResponse.isAnonymousVpn()));
        containerRequestContext.getHeaders().putSingle(X_TOR, String.valueOf(anonymousIpResponse.isTorExitNode()));
    }

    private String toAscii(String input) {
        if (!Strings.isNullOrEmpty(input)) {
            return input.replaceAll("[^\\x20-\\x7e]", "");
        }
        return input;
    }

}