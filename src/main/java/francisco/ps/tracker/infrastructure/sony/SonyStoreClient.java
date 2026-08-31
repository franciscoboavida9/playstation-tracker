package francisco.ps.tracker.infrastructure.sony;

import francisco.ps.tracker.infrastructure.sony.dto.GameAndEditionDto;
import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Infrastructure client responsible for communicating directly with Sony's GraphQL API.
 * Handles HTTP requests, injecting required preflight headers,
 * and mapping raw JSON responses into typed DTO records for search and product details.
 */
@Component
public class SonyStoreClient {
    // Uses Sony's GraphQL persisted query hash
    private static final String searchUrl = "https://web.np.playstation.com/api/graphql/v1//" +
            "op?operationName=getSearchResults&variables=%7B%22countryCode%22%3A%22PT%22%2C%22" +
            "languageCode%22%3A%22pt%22%2C%22nextCursor%22%3A%22%22%2C%22pageOffset%22%3A0%2C%22" +
            "pageSize%22%3A24%2C%22searchTerm%22%3A%22%s%22%7D&extensions=%7B%22" +
            "persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256" +
            "Hash%22%3A%224df6284f982e57bec70f23c77e2c219dc792eb19af7fb3d3a81767aa3f1958aa%22%7D%7D";

    // Uses Sony's GraphQL persisted query hash
    private static final String gameUrl = "https://web.np.playstation.com/api/graphql/v1/" +
            "op?operationName=productRetrieveForUpsellWithCtas&variables=%7B%22" +
            "productId%22%3A%22%s%22%7D&" +
            "extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256" +
            "Hash%22%3A%22a3674adcab1c43cc5847002da67e12a2d138f3ad9dc67dd362452220ea492b26%22%7D%7D";


    private final RestClient restClient;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SonyStoreClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    /**
     * Completes the search url by inserting the user's searched term or specific game id.
     * @param data The search term of specific game id
     * @param url The url of the search
     * @return The url of the search
     */
    private URI buildUrl(String data, String url) {
        String encodedData = URLEncoder.encode(data, StandardCharsets.UTF_8);
        String finalUrlString = url.replace("%s", encodedData);
        return URI.create(finalUrlString.trim());
    }

    /**
     * Executes a generic request against Sony's GraphQL API.
     *
     * @param data The search term or game ID being requested.
     * @param url The specific GraphQL endpoint URL template.
     * @param responseType The target DTO class to map the response into.
     * @param <T> The type of the response DTO.
     * @return The populated response DTO.
     */
    private <T> T fetchFromSony(String data, String url, Class<T> responseType) {
        return restClient.get()
                .uri(buildUrl(data, url))
                // Required to bypass Apollo Server CSRF protection on Sony's backend
                .header("apollo-require-preflight", "true")
                .retrieve()
                .body(responseType);
    }


    /**
     * Searches the PlayStation Store for games matching the provided search term.
     *
     * @param search The user's input.
     * @return A SearchResponseDto containing the list of matching games and editions.
     */
    public SearchResponseDto searchResponse(String search) {
        return fetchFromSony(search, searchUrl, SearchResponseDto.class);
    }

    /**
     * Retrieves information and pricing for a specific game or edition.
     *
     * @param id The unique PlayStation Store product ID.
     * @return A GameAndEditionDto containing pricing, metadata, and edition details.
     */
    public GameAndEditionDto gameAndEditionInfo(String id) {
        return fetchFromSony(id, gameUrl, GameAndEditionDto.class);
    }
}
