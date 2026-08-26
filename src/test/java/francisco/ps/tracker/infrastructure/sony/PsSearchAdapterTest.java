package francisco.ps.tracker.infrastructure.sony;

import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(PsSearchAdapter.class)
public class PsSearchAdapterTest {
    @Autowired
    private PsSearchAdapter adapter;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void findSearch() {
        String search = "elden ring";

        String mockJsonResponse = """
                {
                  "data": {
                    "universalSearch": {
                      "results": [
                        {
                          "id": "EP0700-PPSA04609_00-ELDENRING0000000",
                          "name": "Elden Ring PS4 & PS5"
                        }
                      ]
                    }
                  }
                }
                """;

        // Simulate Sony GraphQL endpoint and enforce required Apollo preflight header
        mockServer.expect(requestTo(adapter.buildSearchUrl(search)))
                .andExpect(header("apollo-require-preflight", "true"))
                .andRespond(withSuccess(mockJsonResponse, MediaType.APPLICATION_JSON));

        SearchResponseDto response = adapter.searchResponse(search);

        assertNotNull(response);
        assertNotNull(response.data());

        var results = response.data().search().results();

        assertEquals(1, results.size());
        assertEquals("EP0700-PPSA04609_00-ELDENRING0000000", results.getFirst().id());
        assertEquals("Elden Ring PS4 & PS5", results.getFirst().name());

        mockServer.verify();
    }
}
