package francisco.ps.tracker.infrastructure.sony;

import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @ValueSource(strings = {
            "elden ring, id-1, Elden Ring",                 // standard space
            "spider-man, id-2, Spider-Man",                 // hyphen
            "ratchet & clank, id-3, Ratchet and Clank",     // ampersand
            "Nioh 2, id-4, Nioh 2",                         // letters and numbers
            " god  of  war , id-5, God of War",             // leading/trailing spaces
            "🎮 cyberpunk, id-6, Cyberpunk",                // emojis and unicode symbols
            "asdfghjkl12345, id-7, Unknown Game",           // gibberish / non-existent search
            "?!@#$%, id-8, Special Chars Game",             // pure special characters / symbols
    })
    void findSearch(String search, String expectedId, String expectedName) {
        String mockJsonResponse = """
                {
                  "data": {
                    "universalSearch": {
                      "results": [
                        {
                          "id": "%s",
                          "name": "%s"
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
        assertEquals(expectedId, results.getFirst().id());
        assertEquals(expectedName, results.getFirst().name());

        mockServer.verify();
    }
}
