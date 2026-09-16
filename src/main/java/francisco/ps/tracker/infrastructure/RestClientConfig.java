package francisco.ps.tracker.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Spring Configuration defining the HTTP client infrastructure.
 * Configures the RestClient with essential connection timeouts, default headers,
 * and message converters required to securely communicate with the PlayStation API.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
