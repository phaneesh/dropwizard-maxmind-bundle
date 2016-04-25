# Dropwizard MaxMind Bundle [![Travis build status](https://travis-ci.org/phaneesh/dropwizard-maxmind-bundle.svg?branch=master)](https://travis-ci.org/phaneesh/dropwizard-maxmind-bundle)

This bundle adds MaxMind GeoIP2 support for dropwizard.
This bundle compiles only on Java 8.

## Note
Please bump up jackson dependencies from 2.6.3 to 2.7.0 in your dropwizard project to use this bundle.

```xml
<properties>
    <jackson.version>2.7.0</jackson.version>
</properties>    
```

```xml
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-xml</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-yaml</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.jaxrs</groupId>
            <artifactId>jackson-jaxrs-json-provider</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.module</groupId>
            <artifactId>jackson-module-afterburner</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.module</groupId>
            <artifactId>jackson-module-jaxb-annotations</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jdk8</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jdk7</artifactId>
            <version>2.6.6</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-guava</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-guava</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-joda</artifactId>
            <version>${jackson.version}</version>
        </dependency>
```
## Dependencies
* Dropwizard 0.9.2
* [GeoIP2 Java API](https://github.com/maxmind/GeoIP2-java)  

## Usage
The bundle adds MaxMind GeoIP2 support for which makes it easier for geo ip information that is required by location aware services.
The bundle works with the free light version of MaxMind GeoIP2 database as well.

### Build instructions
  - Clone the source:

        git clone github.com/phaneesh/dropwizard-maxmind-bundle

  - Build

        mvn install

### Maven Dependency
Use the following repository:
```xml
<repository>
    <id>clojars</id>
    <name>Clojars repository</name>
    <url>https://clojars.org/repo</url>
</repository>
```
Use the following maven dependency:
```xml
<dependency>
    <groupId>io.dropwizard.xml</groupId>
    <artifactId>dropwizard-maxmind-bundle</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Using MaxMind bundle

#### Configuration
```yaml
maxmind:
  databaseFilePath: /path/to/maxmind/database/file.mmdb
  remoteIpHeader: "CLIENT-IP" #default is X-FORWARDED-FOR (when used behind a loadbalancer)
  cacheTTL: 120 #In seconds, Default is 300 seconds
  cacheMaxEntries: 102400 #Default is 10240
  enterprise: true #default: false. Enable maxmind enterprise database mode
  type: anonymous #If it is not a enterprise database; set the type of database that is being used. Supported: country, city, anonymous
  maxMindContext: false #If you need MaxMindInfo injection into resource methods; set it to true  
```

#### Bootstrap
```java
    @Override
    public void initialize(final Bootstrap...) {
        bootstrap.addBundle(new MaxMindBundle() {
            
            public MaxMindConfig getMaxMindConfig(T configuration) {
                ...
            }
        });
    }
```

#### Headers stamped
* X-MAXMIND-REQUEST-COUNTRY
* X-MAXMIND-REQUEST-STATE
* X-MAXMIND-REQUEST-CITY
* X-MAXMIND-REQUEST-POSTAL-CODE
* X-MAXMIND-REQUEST-LATITUDE
* X-MAXMIND-REQUEST-LONGITUDE
* X-MAXMIND-REQUEST-LOCATION-ACCURACY
* X-MAXMIND-REQUEST-USER-TYPE
* X-MAXMIND-REQUEST-CONNECTION-TYPE
* X-MAXMIND-REQUEST-ISP
* X-MAXMIND-REQUEST-LEGAL-PROXY
* X-MAXMIND-REQUEST-ANONYMOUS-IP
* X-MAXMIND-REQUEST-ANONYMOUS-VPN
* X-MAXMIND-REQUEST-TOR-NODE

#### MaxMindContext in Resource
Use MaxMindInfo which is much more convinient if you want easier access to all the stamped headers in a simple object model
* Example

```java 
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
@Path("/")
public class MaxMindResource {

    @GET
    public Response maxMindInfoApi(@MaxMindContext final MaxMindInfo maxMindInfo) {
      // Do work and return something
        return Response.status(OK).entity(maxMindInfo).build();
    }
} 
``` 


LICENSE
-------

Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
