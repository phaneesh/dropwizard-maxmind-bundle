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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.maxmind.db.NodeCache;
import io.dropwizard.maxmind.geoip2.config.MaxMindConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author phaneesh
 */
@Slf4j
public class MaxMindCache implements NodeCache {

    private LoadingCache<Integer, JsonNode> cache;

    private Loader loader;

    public MaxMindCache(MaxMindConfig config) {
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getCacheTTL(), TimeUnit.SECONDS)
                .maximumSize(config.getCacheMaxEntries())
                .recordStats()
                .build(new CacheLoader<Integer, JsonNode>() {
                    @Override
                    public JsonNode load(Integer key) throws Exception {
                        return loader.load(key);
                    }
                });
    }

    @Override
    public JsonNode get(int i, Loader loader) throws IOException {
        if(loader != null) {
            this.loader = loader;
        }
        try {
            return cache.get(i);
        } catch (ExecutionException e) {
            log.error("Error fetching info from cache", e);
            return null;
        }
    }
}
