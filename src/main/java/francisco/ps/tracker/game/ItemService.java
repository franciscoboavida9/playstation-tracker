package francisco.ps.tracker.game;

import francisco.ps.tracker.infrastructure.sony.SonyStoreClient;
import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    private final SonyStoreClient sonyStoreClient;

    public ItemService(SonyStoreClient sonyStoreClient) {
        this.sonyStoreClient = sonyStoreClient;
    }

    /**
     * Executes a search in the PS Store and returns the item results.
     * @param input The user's input.
     * @return The list of results of the search.
     */
    public List<Item> search(String input) {
        SearchResponseDto searchResponseDto = sonyStoreClient.searchResponse(input);
        List<Item> results = new ArrayList<>();

        // Check for possible null values in information given by the Sony API
        List<SearchResponseDto.ResultDto> safeResults = Optional.ofNullable(searchResponseDto)
                .map(SearchResponseDto::data)
                .map(SearchResponseDto.DataDto::search)
                .map(SearchResponseDto.SearchDto::results)
                .orElse(Collections.emptyList());

        for (SearchResponseDto.ResultDto search : safeResults) {
            String id = search.id();
            String name = search.name();
            BigDecimal basePrice = parsePrice(search.price(), false);
            BigDecimal currentPrice = parsePrice(search.price(), true);

            results.add(new Item(id, name, basePrice, currentPrice));
        }

        return results;
    }

    private BigDecimal parsePrice(SearchResponseDto.PriceDto priceDto, boolean isCurrentPrice) {
        if (priceDto == null) {
            return new BigDecimal("0.00");
        }

        String priceStr = isCurrentPrice ? priceDto.currentPrice() : priceDto.basePrice();
        if (priceStr == null || priceStr.trim().isEmpty() || priceStr.equalsIgnoreCase("Grátis")) {
            return new BigDecimal("0.00");
        }

        String cleanPrice = priceStr.replace("€", "").replace(",", ".").trim();
        return new BigDecimal(cleanPrice);
    }

    public Item searchById(String itemId) {
        // todo
        return null;
    }
}
