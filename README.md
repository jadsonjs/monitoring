# Monitoring Spring Boot Applications with Grafana


![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/tools.png)


## Creating a Spring Boot Application to Monitoring Metrics

We created a simple spring boot application adding the **actuator** and **prometheus** dependences


![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/spring_dependences.png)


Actuator is a Java library that will generate the metrics about the spring boot application, like CPU usage, Memory, Up time, etc.. 

Micrometer Prometheus library will convert the data generated by actuator to prometheus format.

To run the application of this project, just import the project as a gradle project and run the MonitoringDemoApplication class

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/running_application.png)

As the actuator dependence is present in the project, we can access http://localhost:8080/actuator to have some information available.


![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/actuator_metrics.png)

By default, just the **health** endpoint have free access.

We added the following line on application.properties, to expose all endpoints.

```
management.endpoints.web.exposure.include=*
```

The micrometer prometheus dependence will generate a prometheus endpoint

http://localhost:8080/baseimd/actuator/prometheus 

This url will be access by prometheus to recovery metrics about the application


The Actuator endpoints reveal sensitive information about the application. Anyone who has access to the actuator endpoint can know things like the Beans, properties configurations and other metrics about the application. Therefore, do not leave endpoints without protection.


For that, we define a spring security user name and password in application.properties file.

```
spring.security.user.name=prometheus
spring.security.user.password=$2a$10$KiJV109h9BQtFxwK.928ke9QKxRSuRwzHoCCsV4VceWeIXU8BU9Oa
spring.security.user.roles=ENDPOINT_ADMIN
```

And configure spring basic authentication in the SecurityConfig class.
```
protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests().requestMatchers(EndpointRequest.toAnyEndpoint())
            .authenticated()
            .and()
            .httpBasic();
}
```


## Creating a Spring Boot Application to Monitoring Logs

Spring boot comes with Logback. We can use SLF4J to generate logs.

> The Simple Logging Facade for Java (SLF4J) serves as a simple facade or abstraction for various logging frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging framework at deployment time.


We can also define the level and the file to log in application.properties file as bellow image.

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/log_configuration.png)

Logback also allow you make configuration in a specific file the **logback-spring.xml** under the resources directory

In this file can define some **appenders** for configuration. Through the appenders we can to specify  a file to save the log and each type of log will be saved there. We can also use a filter, to filter by the level of Log. 
The appender can use a rolling policy to to discard old logs.

In the bellow example, we are using RoollingFileAppender class to rollover daily the log files. The log file will be rollover when reaches 10MB. We define a _app_request.log_, and use a LevelFilter to not save errors on this file.


Lastly, we define a **logger** for INFO level that will use this appender as well the
console appender to save log in application console also.

```

<property name="LOGS" value="/var/log/apps" />
<property name="LOG_PATTERN" value="%d %p %C{1.} [%t] %m%n" />

 <appender name="AppInfoLog" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOGS}/app_info.log</file>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <Pattern>${LOG_PATTERN}</Pattern>
        </encoder>

        <!-- Exclude ERROR level of this file -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>DENY</onMatch>
            <onMismatch>ACCEPT</onMismatch>
        </filter>

        <!-- rollover daily and when the file reaches 10 MegaBytes -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOGS}/archived/app_info-%d{yyyy-MM-dd}.%i.log
            </fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
    </appender>


    <logger name="br.com.example.jadson" level="INFO" additivity="false" >
        <appender-ref ref="AppInfoLog" />
        <appender-ref ref="Console" />
    </logger>

```



## Storing metrics with Prometheus

Prometheus is a time-series database that stores our metric data by pulling it periodically over HTTP.
 
Copy this configuration file to the current directory as prometheus-config.yaml. Because we are using docker, replace it by the machine IP, “localhost” alias will be the docker IP and will not work.

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/prometheus_config.png)

To run prometheus as a Docker,  type the following command:


```
docker run -d -p 9090:9090 -v $PWD/prometheus-config.yaml:/etc/prometheus/prometheus.yml -v $PWD/web.yml:/etc/prometheus/web.yml -v /data/prometheus/:/prometheus --name monitoring-prometheus-auth prom/prometheus:v2.33.3 --config.file=/etc/prometheus/prometheus.yml --web.config.file=/etc/prometheus/web.yml
```

Go to http://localhost:9090/targets  to see if the application is being monitored.


![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/prometheus.png)


If the UP status appears, it means that everything is fine with the installation of prometheus and the metrics generated by the application are being collected.

To protect the prometheus base, create a web.yml file with the following content:

```
basic_auth_users:
    admin:$2b$12$hNf2lSsxfm0.i4a.1kVpSOVyBCfIB51VRjgBUyv6kdny
```

Where $2b$12… is the hash of the encoded user "admin" password. Which can be generated with the following Java code:

```
public static void main(String[] args) {
    BCryptPasswordEncoder  bCrypt = new BCryptPasswordEncoder();
    System.out.println(bCrypt.encode("senha"));
}
```


## Store Application Logs in Loki
