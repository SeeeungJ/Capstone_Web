package PibuStory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@EnableMongoRepositories(basePackages = "PibuStory")
public class DemoVerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoVerApplication.class, args);
	}

}