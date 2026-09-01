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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private SonyStoreClient sonyStoreClient;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item("1", "ELDEN RING", BigDecimal.valueOf(59.99), BigDecimal.valueOf(39.99));
    }

    @Test
    @DisplayName("Should return mapped items when the search is valid")
    void search_ValidData() {
        // Arrange
        SearchResponseDto.PriceDto fakePrice = new SearchResponseDto.PriceDto("€59,99", "€39,99");
        SearchResponseDto.ResultDto fakeResult = new SearchResponseDto.ResultDto("1", "ELDEN RING", fakePrice);
        SearchResponseDto.SearchDto fakeSearch = new SearchResponseDto.SearchDto(List.of(fakeResult));
        SearchResponseDto.DataDto fakeData = new SearchResponseDto.DataDto(fakeSearch);
        SearchResponseDto fakeResponse = new SearchResponseDto(fakeData);
        when(sonyStoreClient.searchResponse("elden ring")).thenReturn(fakeResponse);

        // Act
        List<Item> actualResults = itemService.search("elden ring");

        // Assert
        assertEquals(1, actualResults.size());

        Item firstItem = actualResults.getFirst();
        assertEquals(testItem.getId(), firstItem.getId());
        assertEquals(testItem.getName(), firstItem.getName());

        assertEquals(testItem.getBasePrice(), firstItem.getBasePrice());
        assertEquals(testItem.getCurrentPrice(), firstItem.getCurrentPrice());
    }

    @Test
    @DisplayName("Should return empty list when the entire API response is null")
    void search_NullResponse() {

    }

    @Test
    @DisplayName("Should return empty list when the API returns an empty results array")
    void search_EmptyResults() {

    }

    @Test
    @DisplayName("Should map prices to 0.00 when the store string says 'Grátis'")
    void search_GratisPrice() {

    }

    @Test
    @DisplayName("Should map prices to 0.00 when the price object is completely missing")
    void search_MissingPriceObject() {

    }
}
