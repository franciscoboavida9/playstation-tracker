package francisco.ps.tracker.infrastructure.sony;

import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class PsSearchAdapter {
    // Uses Sony's GraphQL persisted query hash
    private static final String searchUrl = " https://web.np.playstation.com/api/graphql/v1//" +
            "op?operationName=getSearchResults&variables=%7B%22countryCode%22%3A%22PT%22%2C%22" +
            "languageCode%22%3A%22pt%22%2C%22nextCursor%22%3A%22%22%2C%22pageOffset%22%3A0%2C%22" +
            "pageSize%22%3A24%2C%22searchTerm%22%3A%22%s%22%7D&extensions=%7B%22" +
            "persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256" +
            "Hash%22%3A%224df6284f982e57bec70f23c77e2c219dc792eb19af7fb3d3a81767aa3f1958aa%22%7D%7D";

    private final RestClient restClient;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public PsSearchAdapter(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    /**
     * Completes the search url by inserting the user's searched term.
     * @param search The game that was searched.
     * @return The url of the search.
     */
    public URI buildSearchUrl(String search) {
        String encodedSearch = URLEncoder.encode(search, StandardCharsets.UTF_8);
        String finalUrlString = searchUrl.replace("%s", encodedSearch);
        return URI.create(finalUrlString.trim());
    }

    public SearchResponseDto searchResponse(String search) {
        return restClient.get()
                .uri(buildSearchUrl(search))
                // Required to bypass Apollo Server CSRF protection on Sony's backend
                .header("apollo-require-preflight", "true")
                .retrieve()
                .body(SearchResponseDto.class);
    }

}
