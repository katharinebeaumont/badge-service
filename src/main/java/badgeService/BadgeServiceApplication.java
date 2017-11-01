package badgeService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BadgeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BadgeServiceApplication.class, args);
	}
}
