package francisco.ps.tracker.infrastructure.sony;

import francisco.ps.tracker.infrastructure.sony.dto.ItemDetailsDto;
import francisco.ps.tracker.infrastructure.sony.dto.ItemMediaDto;
import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * External Adapter for the undocumented Sony PlayStation Store GraphQL API.
 * Acts as a Backend-For-Frontend (BFF) client, managing HTTP headers, CSRF bypass,
 * and executing the specific endpoint queries required for data aggregation.
 */
@Component
public class SonyStoreClient {

    private static final Logger log = LoggerFactory.getLogger(SonyStoreClient.class);

    // Uses Sony's GraphQL persisted query hash
    private static final String searchUrl = "https://web.np.playstation.com/api/graphql/v1//" +
            "op?operationName=getSearchResults&variables=%7B%22countryCode%22%3A%22PT%22%2C%22" +
            "languageCode%22%3A%22pt%22%2C%22nextCursor%22%3A%22%22%2C%22pageOffset%22%3A0%2C%22" +
            "pageSize%22%3A24%2C%22searchTerm%22%3A%22%s%22%7D&extensions=%7B%22" +
            "persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256" +
            "Hash%22%3A%224df6284f982e57bec70f23c77e2c219dc792eb19af7fb3d3a81767aa3f1958aa%22%7D%7D";

    // Uses Sony's GraphQL persisted query hash
    private static final String itemUrl = "https://web.np.playstation.com/api/graphql/v1/" +
            "op?operationName=productRetrieveForCtasWithPrice&variables=%7B%22" +
            "productId%22%3A%22%s%22%7D&extensions=%7B%22" +
            "persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%221f0ca607e170abbfb7d67bd76c9bbc97f21fe2e807be49e5fe764e14566cb605%22%7D%7D";

    // Uses Sony's GraphQL persisted query hash
    private static final String mediaUrl = "https://web.np.playstation.com/api/graphql/v1/" +
            "op?operationName=productRetrieveForMediaCarousel&variables=%7B%22" +
            "productId%22%3A%22%s%22%7D&extensions=%7B%22" +
            "persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%221c9f30320cd7bdc24baf4e311fe1457247e9819c0be1019890e47ebc4fa4886a%22%7D%7D";

    private final RestClient restClient;

    public SonyStoreClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
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
        URI requestUri = buildUrl(data, url);

        log.info("Calling Sony GraphQL API for data: '{}'", data);
        log.debug("Exact Request URI: {}", requestUri);

        return restClient.get()
                .uri(requestUri)
                .header("apollo-require-preflight", "true")
                .header("x-psn-store-locale-override", "pt-PT")
                .header("Accept-Language", "pt-PT, pt;q=0.9")
                .retrieve()
                .body(responseType);
    }

    /**
     * Searches the PlayStation Store for games matching the provided search term.
     * @param search The user's input.
     * @return A SearchResponseDto containing the list of matching games and editions.
     */
    public SearchResponseDto searchResponse(String search) {
        return fetchFromSony(search, searchUrl, SearchResponseDto.class);
    }

    /**
     * Fetches detailed information for a specific PlayStation Store item.
     * @param id The unique identifier of the item in the Sony store.
     * @return An ItemDetailsDto containing the detailed information for the item.
     */
    public ItemDetailsDto itemDetails(String id) {
        return fetchFromSony(id, itemUrl, ItemDetailsDto.class);
    }

    /**
     * Fetches media assets and image details for a specific PlayStation Store item.
     * @param id The unique identifier of the item in the Sony store.
     * @return An ItemMediaDto containing the media list and image details for the item.
     */
    public ItemMediaDto itemMedia(String id) {
        return fetchFromSony(id, mediaUrl, ItemMediaDto.class);
    }
}
