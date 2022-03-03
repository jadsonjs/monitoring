# Monitoring Spring Boot Applications with Grafana

**Jadson Santos - jadsonjs@gmail.com** <br/>
**Last updated at: 03/03/2022** <br/>

Basic monitoring project for a Spring Boot application.
This project collects JVM metrics and stores them in the **Prometheus** database. Well, as it configures the application to generate log files, collects these files with **Promtail**, promtail sends the data to the **Loki** database. The data can then be viewed in the **Grafana** interface.
All applications run on Docker, there is a docker-compose.yml file that raises the entire environment with a single command: **docker-compose up -d**

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
The appender can use a rolling policy to discard old logs.

In the bellow example, we are using **RoollingFileAppender** class to rollover daily the log files. The log file will be rollover when reaches 10MB. We define a _app_request.log_, and use a LevelFilter to not save errors on this file.


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

<br/> <br/> <br/>

## Storing metrics with Prometheus

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/micrometer_prometheus.png)

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


<br/> <br/> <br/>
## Store Application Logs in Loki

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/promtail_loki.png)

>_"Loki is a logging management system created as part of the Grafana project, and it has been created with a different approach in mind than Elasticsearch.
Loki is a horizontally-scalable, highly-available, multi-tenant log aggregation system inspired by Prometheus. It is designed to be very cost effective and easy to operate. It does not index the contents of the logs, but rather a set of labels for each log stream"_
Alex Vazquez - https://medium.com/coderbyte/discover-loki-a-lightweight-alternative-to-the-elk-stack-3b67ad216327

Loki Architecture

 - Distributor: Receives client logs and sends them to ingester
 - Ingester: Receives logs and stores
 - Querier: The service that receives LogQL commands
 - Query Frontend: Manage the executed queries
 - Chunk Store: Holds the storage of loki indexes 

### Promtail

O Promtail is a log collector responsible for collect log of the application and send it to loki

**It is usually deployed to every machine that has applications needed to be monitored.**

Replace http://loki:3100 by the loki address, as we are using docker, replace it by the machine IP, “localhost” alias will be the docker IP and will not work.


You can use environment variable references in the configuration file to set values that need to be configurable during deployment. To do this, pass -config.expand-env=true and use:

${VAR}

Where VAR is the name of the environment variable.

Now, install and execute the promtail inside a docker with the follow command:

```
docker run -d -p 9080:9080 --name promtail -v $PWD/promtail-config.yaml:/etc/promtail/promtail.yml -v /var/log/apps:/var/log/ grafana/promtail:2.4.2 -config.file=/etc/promtail/promtail.yml
```

Access http://localhost:9080 to check if promtail is working


![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/promtail.png)


### Loki

Now, to execute the LOKI on docker, type the follow command


```
docker run -d -p 3100:3100 -p 9096:9096 --name monitoring-loki -v $PWD/loki-config.yaml:/mnt/config/loki.yml -v /data/loki/:/tmp/ grafana/loki:2.4.2 -config.file=/mnt/config/loki.yml
````

Access http://localhost:3100/ready to see if loki is running.


![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/loki.png)


## Visualizing metrics on Grafana

Prometheus and Loki offers crude information, for a rich UI, we need to add Grafana.
Grafana can pull data from various data sources like Prometheus, Elasticsearch, InfluxDB, etc.

To run Grafana with Docker, execute the following command:


```
docker run -d -p 3000:3000 --name monitoring-grafana-p -v /data/grafana:/var/lib/grafana grafana/grafana:8.4.1
````

Access http://localhost:3000 to see the grafana login page.


![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/grafana_login.png)

The initital user and password are "admin" and "admin".

To visualize the metrics stored in prometheus:

>Configuration -> Data Source -> Add data Source and Choose Prometheus.

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/grafana_prometheus_data.png)

Enable basic authentication and inform the user and password used in file **web.yml**

Now to visualize the log data stored in Loki

>Configuration -> Data Source -> Add data Source and select Loki.


![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/grafana_loki_data.png)

### Grafana Dashboards

With Grafana, we can create a DashBoards from zero, or import a new one.


For Spring Boot projects, the **JVM dashboard** (https://grafana.com/grafana/dashboards/4701 ) is very popular dashboard to visualize the prometheus metrics.

To see the metrics collected by prometheus, on Grafana Home page, click on **"+"** icon and on "Import a new Daskboard". Informe the number (4701) or json file of the dashboard and choose data source of prometheus.

Now we can see in a rich way, metric information about the use of our Spring Boot Application:


![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/grafana_jvm_dashboard.png)


How log information usually are specific for your application, there is not a common dashboard for logs like we have for JVM metrics. So, most of time, you will need to create one.

We the use the explore to see the content of a log file:

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/grafana_explore.png)
![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/grafana_explore_2.png)


### LogQL: Log query language

There are two types of LogQL queries:

Log queries return the contents of log lines.
Metric queries extend log queries to calculate values based on query results.

#### Log queries 

All LogQL queries contain a log stream selector.

Example:

```
{app="mysql",name="mysql-backup"}
```

The = operator after the label name is a label matching operator. The following label matching operators are supported:

Regex log stream examples:

{name =~ "mysql.+"}
{name !~ "mysql.+"}
{name !~ `mysql-\d+`}


Line filter expression
The line filter expression does a distributed grep over the aggregated logs from the matching log streams. It searches the contents of the log line, discarding those lines that do not match the case sensitive expression.

Each line filter expression has a filter operator followed by text or a regular expression. These filter operators are supported:

|=: Log line contains string

!=: Log line does not contain string

|~: Log line contains a match to the regular expression

!~: Log line does not contain a match to the regular expression


Example:

All lines of error with the string "NullPointerException"
```
{type="error"} |= "NullPointerException" 
```

All lines of error without the string start with "NullPointerException"

```
{type="error"} !~ "NullPointerException*"
```


#### Metric queries

Log range aggregations

The functions:

**rate(log-range)**: calculates the number of entries per second

**count_over_time(log-range)**: counts the entries for each log stream within the given range.

**bytes_rate(log-range)**: calculates the number of bytes per second for each stream.

**bytes_over_time(log-range)**: counts the amount of bytes used by each log stream for a given range.

**absent_over_time(log-range)**: returns an empty vector if the range vector passed to it has any elements and a 1-element vector with the value 1 if the range vector passed to it has no elements.

Examples:

Count all the log lines within the last five minutes for the MySQL job.

```
count_over_time({job="mysql"}[5m])
```

#### Create your own grafana dashboard

On Grafana home you can click on **"+"** -> Dashboard -> Add a new Panel

Select the Gauge type, specify a threshold

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/grafana_dashboard1.png)


In the **Query** filed enter with a LogQL query that return a integer value. In this case, we are couting all logs of type _"error"_ of application _"app1"_ that have a string _"NullPointerException"_ in the last one hour.

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/grafana_dashboard2.png)


If the value returned by the query was less than the threshold, the gauge graph will turn green, if it passes the threshold, it will turn red.

![alt text](https://github.com/jadsonjs/monitoring/blob/master/imgs/grafana_dashboard3.png)


We can add several panels with different queries to our dashboard.


<br/> <br/> <br/>

## Docker compose

To facilitate management, in the project there is also a **docker-compose.yml** file to run loki, promtail, prometheus and grafana with a single command.

To install and run all applications with a single command, type:

```
docker-compose up -d
```

To stop removing containers run:

```
docker-compose down
```

To stop containers without removing their data, run:

```
docker-compose stop
```

And to start again run:

```
docker-compose start
```

And restart the containers by reloading the docker-compose.yml settings:

```
docker-compose stop
docker-compose up -d
```

  

<br/> <br/> <br/>

## References

Baeldung - Logging in Spring Boot - https://www.baeldung.com/spring-boot-logging

Spring boot multiple log files example - https://howtodoinjava.com/spring-boot2/logging/multiple-log-files/ 

Configuring Logback with Spring Boot - https://www.codingame.com/playgrounds/4497/configuring-logback-with-spring-boot 

Monitoring Spring Boot Apps with Micrometer, Prometheus, and Grafana - https://stackabuse.com/monitoring-spring-boot-apps-with-micrometer-prometheus-and-grafana/

Projetos pequenos e médios usam ELK como um log? Eu vou pregar alguns truques novos. https://mp.weixin.qq.com/s?__biz=MzUzMzQ2MDIyMA==&mid=2247489891&idx=2&sn=712ce17d70e8062f49ffd1a334418f63&scene=21#wechat_redirect (translate from chinese using google chrome)

SECURING PROMETHEUS API AND UI ENDPOINTS USING BASIC AUTH - https://prometheus.io/docs/guides/basic-auth/

Very Good Tutorial Loki and Promtail on Docker - https://www.youtube.com/watch?v=eJtrxj9U_P8

LogQL: Log query language - https://grafana.com/docs/loki/latest/logql/
