package francisco.ps.tracker.game;

import francisco.ps.tracker.infrastructure.sony.SonyStoreClient;
import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private SonyStoreClient sonyStoreClient;

    @InjectMocks
    private ItemService itemService;

    private Item firstTestItem;
    private Item secondTestItem;
    private Item thirdTestItem;

    @BeforeEach
    void setUp() {
        firstTestItem = new Item("1", "ELDEN RING", BigDecimal.valueOf(59.99), BigDecimal.valueOf(39.99), "http://example.com/cover.png");
        secondTestItem = new Item("2", "ELDEN RING NIGHTREIGN", BigDecimal.valueOf(39.99), BigDecimal.valueOf(39.99), "http://example.com/cover.png");
        thirdTestItem = new Item("3", "Rocket League", new BigDecimal("0.00"), new BigDecimal("0.00"), "http://example.com/cover.png");
    }

    @Test
    @DisplayName("Should return mapped items when the search is valid")
    void search_ValidData() {
        // Arrange
        SearchResponseDto.PriceDto fakePrice = new SearchResponseDto.PriceDto("€59,99", "€39,99");
        SearchResponseDto.MediaDto fakeMedia = new SearchResponseDto.MediaDto("MASTER", "http://example.com/cover.png");
        SearchResponseDto.ResultDto fakeResult = new SearchResponseDto.ResultDto("1", List.of(fakeMedia), "ELDEN RING", fakePrice);
        SearchResponseDto.SearchDto fakeSearch = new SearchResponseDto.SearchDto(List.of(fakeResult));
        SearchResponseDto.DataDto fakeData = new SearchResponseDto.DataDto(fakeSearch);
        SearchResponseDto fakeResponse = new SearchResponseDto(fakeData);
        when(sonyStoreClient.searchResponse("elden ring")).thenReturn(fakeResponse);

        // Act
        List<Item> actualResults = itemService.search("elden ring");

        // Assert
        assertEquals(1, actualResults.size());

        Item firstItem = actualResults.getFirst();
        assertEquals(firstTestItem.getId(), firstItem.getId());
        assertEquals(firstTestItem.getName(), firstItem.getName());
        assertEquals(firstTestItem.getBasePrice(), firstItem.getBasePrice());
        assertEquals(firstTestItem.getCurrentPrice(), firstItem.getCurrentPrice());
    }

    @Test
    @DisplayName("Should map 3 results when the API response gives 3 results")
    void search_MultipleValidData() {
        // Arrange
        SearchResponseDto.PriceDto fakePriceOne = new SearchResponseDto.PriceDto("€59,99", "€39,99");
        SearchResponseDto.MediaDto fakeMediaOne = new SearchResponseDto.MediaDto("MASTER", "http://example.com/cover.png");
        SearchResponseDto.ResultDto fakeResultsOne = new SearchResponseDto.ResultDto("1", List.of(fakeMediaOne), "ELDEN RING", fakePriceOne);
        SearchResponseDto.PriceDto fakePriceTwo = new SearchResponseDto.PriceDto("€39,99", "€39,99");
        SearchResponseDto.MediaDto fakeMediaTwo = new SearchResponseDto.MediaDto("MASTER", "http://example.com/cover.png");
        SearchResponseDto.ResultDto fakeResultsTwo = new SearchResponseDto.ResultDto("2", List.of(fakeMediaTwo),"ELDEN RING NIGHTREIGN", fakePriceTwo);
        SearchResponseDto.SearchDto fakeSearch = new SearchResponseDto.SearchDto(List.of(fakeResultsOne, fakeResultsTwo));
        SearchResponseDto.DataDto fakeData = new SearchResponseDto.DataDto(fakeSearch);
        SearchResponseDto fakeResponse = new SearchResponseDto(fakeData);
        when(sonyStoreClient.searchResponse("elden ring")).thenReturn(fakeResponse);

        // Act
        List<Item> actualResults = itemService.search("elden ring");

        // Assert
        assertEquals(2, actualResults.size());

        Item firstItem = actualResults.getFirst();
        assertEquals(firstTestItem.getId(), firstItem.getId());
        assertEquals(firstTestItem.getName(), firstItem.getName());
        assertEquals(firstTestItem.getBasePrice(), firstItem.getBasePrice());
        assertEquals(firstTestItem.getCurrentPrice(), firstItem.getCurrentPrice());

        Item secondItem = actualResults.get(1);
        assertEquals(secondTestItem.getId(), secondItem.getId());
        assertEquals(secondTestItem.getName(), secondItem.getName());
        assertEquals(secondTestItem.getBasePrice(), secondItem.getBasePrice());
        assertEquals(secondTestItem.getCurrentPrice(), secondItem.getCurrentPrice());
    }

    @Test
    @DisplayName("Should return empty list when the entire API response is null")
    void search_NullResponse() {
        // Arrange
        when(sonyStoreClient.searchResponse(null)).thenReturn(null);

        // Act
        List<Item> actualResults = itemService.search(null);

        // Assert
        assertEquals(0, actualResults.size());
    }

    @Test
    @DisplayName("Should return empty list when the API returns an empty results array")
    void search_EmptyResults() {
        // Arrange
        SearchResponseDto.SearchDto fakeSearch = new SearchResponseDto.SearchDto(List.of());
        SearchResponseDto.DataDto fakeData = new SearchResponseDto.DataDto(fakeSearch);
        SearchResponseDto fakeResponse = new SearchResponseDto(fakeData);
        when(sonyStoreClient.searchResponse("gibberish")).thenReturn(fakeResponse);

        // Act
        List<Item> actualResults = itemService.search("gibberish");

        // Assert
        assertEquals(0, actualResults.size());
    }

    @Test
    @DisplayName("Should map prices to 0.00 when the store string says 'Grátis'")
    void search_GratisPrice() {
        // Arrange
        SearchResponseDto.PriceDto fakePrice = new SearchResponseDto.PriceDto("Grátis", "Grátis");
        SearchResponseDto.MediaDto fakeMedia = new SearchResponseDto.MediaDto("MASTER", "http://example.com/cover.png");
        SearchResponseDto.ResultDto fakeResult = new SearchResponseDto.ResultDto("3", List.of(fakeMedia), "Rocket League", fakePrice);
        SearchResponseDto.SearchDto fakeSearch = new SearchResponseDto.SearchDto(List.of(fakeResult));
        SearchResponseDto.DataDto fakeData = new SearchResponseDto.DataDto(fakeSearch);
        SearchResponseDto fakeResponse = new SearchResponseDto(fakeData);
        when(sonyStoreClient.searchResponse("rocket league")).thenReturn(fakeResponse);

        // Act
        List<Item> actualResults = itemService.search("rocket league");

        // Assert
        assertEquals(1, actualResults.size());

        Item firstItem = actualResults.getFirst();
        assertEquals(thirdTestItem.getId(), firstItem.getId());
        assertEquals(thirdTestItem.getName(), firstItem.getName());
        assertEquals(thirdTestItem.getBasePrice(), firstItem.getBasePrice());
        assertEquals(thirdTestItem.getCurrentPrice(), firstItem.getCurrentPrice());
    }

    @Test
    @DisplayName("Should map prices to 0.00 when the price object is completely missing")
    void search_MissingPriceObject() {
        // Arrange
        SearchResponseDto.MediaDto fakeMedia = new SearchResponseDto.MediaDto("MASTER", "http://example.com/cover.png");
        SearchResponseDto.ResultDto fakeResult = new SearchResponseDto.ResultDto("4", List.of(fakeMedia), "No Price Game", null);
        SearchResponseDto.SearchDto fakeSearch = new SearchResponseDto.SearchDto(List.of(fakeResult));
        SearchResponseDto.DataDto fakeData = new SearchResponseDto.DataDto(fakeSearch);
        SearchResponseDto fakeResponse = new SearchResponseDto(fakeData);
        when(sonyStoreClient.searchResponse("no price")).thenReturn(fakeResponse);

        // Act
        List<Item> actualResults = itemService.search("no price");

        // Assert
        assertEquals(1, actualResults.size());

        Item firstItem = actualResults.getFirst();
        assertEquals("4", firstItem.getId());
        assertEquals(new BigDecimal("0.00"), firstItem.getBasePrice());
        assertEquals(new BigDecimal("0.00"), firstItem.getCurrentPrice());
    }

    @Test
    @DisplayName("Should extract MASTER image when multiple media roles exist")
    void search_ExtractsMasterImage() {
        SearchResponseDto.PriceDto fakePrice = new SearchResponseDto.PriceDto("€59,99", "€39,99");
        SearchResponseDto.MediaDto bgMedia = new SearchResponseDto.MediaDto("BACKGROUND", "http://example.com/bg.png");
        SearchResponseDto.MediaDto masterMedia = new SearchResponseDto.MediaDto("MASTER", "http://example.com/master.png");
        SearchResponseDto.MediaDto logoMedia = new SearchResponseDto.MediaDto("LOGO", "http://example.com/logo.png");

        SearchResponseDto.ResultDto fakeResult = new SearchResponseDto.ResultDto("1", List.of(bgMedia, masterMedia, logoMedia), "Game", fakePrice);
        SearchResponseDto.SearchDto fakeSearch = new SearchResponseDto.SearchDto(List.of(fakeResult));
        SearchResponseDto fakeResponse = new SearchResponseDto(new SearchResponseDto.DataDto(fakeSearch));

        when(sonyStoreClient.searchResponse("game")).thenReturn(fakeResponse);

        List<Item> actualResults = itemService.search("game");

        assertEquals("http://example.com/master.png", actualResults.getFirst().getCoverImage());
    }

    @Test
    @DisplayName("Should fallback to the last image when MASTER role is missing")
    void search_ExtractsLastImageWhenMasterMissing() {
        SearchResponseDto.PriceDto fakePrice = new SearchResponseDto.PriceDto("€59,99", "€39,99");
        SearchResponseDto.MediaDto bgMedia = new SearchResponseDto.MediaDto("BACKGROUND", "http://example.com/bg.png");
        SearchResponseDto.MediaDto logoMedia = new SearchResponseDto.MediaDto("LOGO", "http://example.com/last_image.png");

        SearchResponseDto.ResultDto fakeResult = new SearchResponseDto.ResultDto("1", List.of(bgMedia, logoMedia), "Game", fakePrice);
        SearchResponseDto.SearchDto fakeSearch = new SearchResponseDto.SearchDto(List.of(fakeResult));
        SearchResponseDto fakeResponse = new SearchResponseDto(new SearchResponseDto.DataDto(fakeSearch));

        when(sonyStoreClient.searchResponse("game")).thenReturn(fakeResponse);

        List<Item> actualResults = itemService.search("game");

        assertEquals("http://example.com/last_image.png", actualResults.getFirst().getCoverImage());
    }

    @Test
    @DisplayName("Should return null coverImage when media list is empty or null")
    void search_HandlesEmptyMedia() {
        SearchResponseDto.PriceDto fakePrice = new SearchResponseDto.PriceDto("€59,99", "€39,99");
        SearchResponseDto.ResultDto fakeResult = new SearchResponseDto.ResultDto("1", null, "Game", fakePrice);
        SearchResponseDto.SearchDto fakeSearch = new SearchResponseDto.SearchDto(List.of(fakeResult));
        SearchResponseDto fakeResponse = new SearchResponseDto(new SearchResponseDto.DataDto(fakeSearch));

        when(sonyStoreClient.searchResponse("game")).thenReturn(fakeResponse);

        List<Item> actualResults = itemService.search("game");

        assertNull(actualResults.getFirst().getCoverImage());
    }
}
