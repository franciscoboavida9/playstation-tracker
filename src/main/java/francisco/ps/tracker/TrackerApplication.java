package francisco.ps.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main entry point for the Spring Boot application.
 * Bootstraps the application context and enables application-wide caching.
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class TrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackerApplication.class, args);
    }

}
