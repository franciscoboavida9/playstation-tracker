package francisco.ps.tracker.game;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Service
public class GameService {
    private final RestClient restClient;

    private static final String URL = "https://web.np.playstation.com/api/graphql/v1/op?operationName=productRetrieveForUpsellWithCtas&variables=%7B%22productId%22%3A%22EP0700-PPSA04609_00-ELDENRING0000000%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%22a3674adcab1c43cc5847002da67e12a2d138f3ad9dc67dd362452220ea492b26%22%7D%7D";

    public GameService() {
        this.restClient = RestClient.builder().build();
    }

    public String fetchGameName() {
        return restClient.get()
                .uri(URI.create(URL))
                .header("apollo-require-preflight", "true")
                .retrieve()
                .body(String.class);
    }

}
