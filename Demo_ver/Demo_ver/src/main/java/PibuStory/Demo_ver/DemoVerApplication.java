package ChromaSkin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class DemoVerApplication {

	@Autowired

	public static void main(String[] args) {
		SpringApplication.run(DemoVerApplication.class, args);
	}
}
