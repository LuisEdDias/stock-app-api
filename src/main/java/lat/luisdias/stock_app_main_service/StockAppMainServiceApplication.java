package lat.luisdias.stock_app_main_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
//@EnableDiscoveryClient
@EnableAsync
public class StockAppMainServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockAppMainServiceApplication.class, args);
	}

}
