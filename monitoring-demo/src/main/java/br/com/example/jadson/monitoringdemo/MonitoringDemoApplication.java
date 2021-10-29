package br.com.example.jadson.monitoringdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This application store monitoring information on Prometheus time series database that will be
 * read and show to use by Grafana Dashboard.
 */
@SpringBootApplication
public class MonitoringDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitoringDemoApplication.class, args);
	}

}
