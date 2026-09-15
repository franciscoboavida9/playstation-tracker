package francisco.ps.tracker.infrastructure.sony;

import francisco.ps.tracker.infrastructure.sony.dto.ItemDetailsDto;
import francisco.ps.tracker.infrastructure.sony.dto.ItemMediaDto;
import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.test.web.client.MockRestServiceServer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(SonyStoreClient.class)
public class SonyStoreClientTest {
    @Autowired
    private SonyStoreClient adapter;

    @Autowired
    private MockRestServiceServer mockServer;

    @ParameterizedTest
    @CsvSource({
            "elden ring, id-1, Elden Ring",
            "spider-man, id-2, Spider-Man",
            "ratchet & clank, id-3, Ratchet and Clank",
            "Nioh 2, id-4, Nioh 2",
            " god  of  war , id-5, God of War",
            "🎮 cyberpunk, id-6, Cyberpunk",
            "asdfghjkl12345, id-7, Unknown Game",
            "?!@#$%, id-8, Special Chars Game",
    })
    void findSearch(String search, String expectedId, String expectedName) {
        String mockJsonResponse = String.format("""
                {
                  "data": {
                    "universalSearch": {
                      "results": [
                        {
                          "id": "%s",
                          "name": "%s",
                          "price": {
                            "basePrice": "59.99",
                            "discountedPrice": "39.99"
                          },
                          "media": [
                            {
                              "role": "MASTER",
                              "url": "http://example.com/cover.png"
                            }
                          ]
                        }
                      ]
                    }
                  }
                }
                """, expectedId, expectedName);

        String expectedEncodedSearch = URLEncoder.encode(search, StandardCharsets.UTF_8);

        mockServer.expect(requestTo(containsString(expectedEncodedSearch)))
                .andExpect(header("apollo-require-preflight", "true"))
                .andRespond(withSuccess(mockJsonResponse, MediaType.APPLICATION_JSON));

        SearchResponseDto response = adapter.searchResponse(search);

        assertNotNull(response);
        assertNotNull(response.data());

        var results = response.data().search().results();

        assertEquals(1, results.size());
        assertEquals(expectedId, results.getFirst().id());
        assertEquals(expectedName, results.getFirst().name());
        assertEquals("59.99", results.getFirst().price().basePrice());
        assertEquals("http://example.com/cover.png", results.getFirst().media().getFirst().imageUrl());

        mockServer.verify();
    }


    @Test
    void findGameDetails() {
        String id = "1";

        String mockJsonResponse = """
                {
                  "data": {
                    "productRetrieve": {
                      "id": "1",
                      "name": "elden ring ps4 and ps5",
                      "webctas": [
                        {
                          "price": {
                            "basePrice": "59.99",
                            "discountedPrice": "59.99"
                          }
                        }
                      ]
                    }
                  }
                }
                """;

        String expectedEncodedSearch = URLEncoder.encode(id, StandardCharsets.UTF_8);

        mockServer.expect(requestTo(containsString(expectedEncodedSearch)))
                .andExpect(header("apollo-require-preflight", "true"))
                .andRespond(withSuccess(mockJsonResponse, MediaType.APPLICATION_JSON));

        ItemDetailsDto response = adapter.itemDetails(id);

        assertNotNull(response);
        assertNotNull(response.data());

        var product = response.data().productRetrieve();

        assertEquals("1", product.id());
        assertEquals("elden ring ps4 and ps5", product.name());
        assertEquals("59.99", product.webctas().getFirst().price().basePrice());

        mockServer.verify();
    }

    @Test
    void findItemMedia() {
        String id = "1";

        String mockJsonResponse = """
                {
                  "data": {
                    "productRetrieve": {
                      "media": [
                        {
                          "role": "MASTER",
                          "url": "http://example.com/cover.png"
                        }
                      ]
                    }
                  }
                }
                """;

        String expectedEncodedSearch = URLEncoder.encode(id, StandardCharsets.UTF_8);

        mockServer.expect(requestTo(containsString(expectedEncodedSearch)))
                .andExpect(header("apollo-require-preflight", "true"))
                .andRespond(withSuccess(mockJsonResponse, MediaType.APPLICATION_JSON));

        ItemMediaDto response = adapter.itemMedia(id);

        assertNotNull(response);
        assertNotNull(response.data());

        var mediaList = response.data().productRetrieve().media();

        assertEquals(1, mediaList.size());
        assertEquals("MASTER", mediaList.getFirst().imageRole());
        assertEquals("http://example.com/cover.png", mediaList.getFirst().imageUrl());

        mockServer.verify();
    }
}