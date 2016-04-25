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

package io.dropwizard.maxmind.geoip2.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.AnonymousIpResponse;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.ConnectionTypeResponse;
import com.maxmind.geoip2.model.CountryResponse;
import io.dropwizard.maxmind.geoip2.config.MaxMindConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author phaneesh
 */
@Slf4j
public class MaxMindCache  {

    private LoadingCache<InetAddress, CountryResponse> countryCache;

    private LoadingCache<InetAddress, CityResponse> cityCache;

    private LoadingCache<InetAddress, AnonymousIpResponse> anonymousCache;

    private LoadingCache<InetAddress, ConnectionTypeResponse> connectionTypeCache;

    public MaxMindCache(MaxMindConfig config, DatabaseReader databaseReader) {
        countryCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getCacheTTL(), TimeUnit.SECONDS)
                .maximumSize(config.getCacheMaxEntries())
                .recordStats()
                .build(new CacheLoader<InetAddress, CountryResponse>() {
                    @Override
                    public CountryResponse load(InetAddress key) throws Exception {
                        return databaseReader.country(key);
                    }
                });
        cityCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getCacheTTL(), TimeUnit.SECONDS)
                .maximumSize(config.getCacheMaxEntries())
                .recordStats()
                .build(new CacheLoader<InetAddress, CityResponse>() {
                    @Override
                    public CityResponse load(InetAddress key) throws Exception {
                        return databaseReader.city(key);
                    }
                });
        anonymousCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getCacheTTL(), TimeUnit.SECONDS)
                .maximumSize(config.getCacheMaxEntries())
                .recordStats()
                .build(new CacheLoader<InetAddress, AnonymousIpResponse>() {
                    @Override
                    public AnonymousIpResponse load(InetAddress key) throws Exception {
                        return databaseReader.anonymousIp(key);
                    }
                });
        connectionTypeCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getCacheTTL(), TimeUnit.SECONDS)
                .maximumSize(config.getCacheMaxEntries())
                .recordStats()
                .build(new CacheLoader<InetAddress, ConnectionTypeResponse>() {
                    @Override
                    public ConnectionTypeResponse load(InetAddress key) throws Exception {
                        return databaseReader.connectionType(key);
                    }
                });
    }

    public CountryResponse country(InetAddress address) throws IOException {
        try {
            return countryCache.get(address);
        } catch (ExecutionException e) {
            log.error("Error fetching country info from cache: {}", e.getMessage());
            return null;
        }
    }

    public CityResponse city(InetAddress address) throws IOException {
        try {
            return cityCache.get(address);
        } catch (ExecutionException e) {
            log.error("Error fetching city info from cache: {}", e.getMessage());
            return null;
        }
    }

    public AnonymousIpResponse anonymousIp(InetAddress address) throws IOException {
        try {
            return anonymousCache.get(address);
        } catch (ExecutionException e) {
            log.error("Error fetching anonymous ip info from cache: {}", e.getMessage());
            return null;
        }
    }

    public ConnectionTypeResponse connectionType(InetAddress address) throws IOException {
        try {
            return  connectionTypeCache.get(address);
        } catch (ExecutionException e) {
            log.error("Error fetching connection type info from cache: {}", e.getMessage());
            return null;
        }
    }

}
