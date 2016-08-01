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

package io.dropwizard.maxmind.geoip2.core;

/**
 * @author phaneesh
 */
public interface MaxMindHeaders {

    String X_COUNTRY = "X-MAXMIND-REQUEST-COUNTRY";
    String X_COUNTRY_ISO = "X-MAXMIND-REQUEST-COUNTRY-ISO";
    String X_STATE = "X-MAXMIND-REQUEST-STATE";
    String X_STATE_ISO = "X-MAXMIND-REQUEST-STATE-ISO";
    String X_CITY = "X-MAXMIND-REQUEST-CITY";
    String X_POSTAL = "X-MAXMIND-REQUEST-POSTAL-CODE";
    String X_LATITUDE = "X-MAXMIND-REQUEST-LATITUDE";
    String X_LONGITUDE = "X-MAXMIND-REQUEST-LONGITUDE";
    String X_LOCATION_ACCURACY = "X-MAXMIND-REQUEST-LOCATION-ACCURACY";
    String X_USER_TYPE = "X-MAXMIND-REQUEST-USER-TYPE";
    String X_CONNECTION_TYPE = "X-MAXMIND-REQUEST-CONNECTION-TYPE";
    String X_ISP = "X-MAXMIND-REQUEST-ISP";
    String X_PROXY_LEGAL = "X-MAXMIND-REQUEST-LEGAL-PROXY";
    String X_ANONYMOUS_IP = "X-MAXMIND-REQUEST-ANONYMOUS-IP";
    String X_ANONYMOUS_VPN = "X-MAXMIND-REQUEST-ANONYMOUS-VPN";
    String X_TOR = "X-MAXMIND-REQUEST-TOR-NODE";
}
