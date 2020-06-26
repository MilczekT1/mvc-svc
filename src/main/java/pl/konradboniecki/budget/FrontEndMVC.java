package pl.konradboniecki.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"pl.konradboniecki"})
public class FrontEndMVC {

	public static void main(String[] args) {
		SpringApplication.run(FrontEndMVC.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

