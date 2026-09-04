package francisco.ps.tracker.game;

import francisco.ps.tracker.infrastructure.sony.SonyStoreClient;
import francisco.ps.tracker.infrastructure.sony.dto.ItemDetailsDto;
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
            BigDecimal basePrice = parsePrice(search.price().basePrice(), false);
            BigDecimal currentPrice = parsePrice(search.price().currentPrice(), true);

            results.add(new Item(id, name, basePrice, currentPrice));
        }

        return results;
    }

    private BigDecimal parsePrice(String priceStr, boolean isCurrentPrice) {
        if (priceStr == null || priceStr.trim().isEmpty() || priceStr.equalsIgnoreCase("Grátis")) {
            return new BigDecimal("0.00");
        }

        String cleanPrice = priceStr.replace("€", "").replace(",", ".").trim();
        return new BigDecimal(cleanPrice);
    }

    /**
     * Fetches item details from the Sony Store and returns the parsed Item object.
     * @param itemId The unique identifier of the item.
     * @return An Item containing the details of the product.
     */
    public Item searchById(String itemId) {
        ItemDetailsDto itemDetailsDto = sonyStoreClient.itemDetails(itemId);

        ItemDetailsDto.ProductDto product = Optional.ofNullable(itemDetailsDto)
                .map(ItemDetailsDto::data)
                .map(ItemDetailsDto.DataDto::productRetrieve)
                .map(ItemDetailsDto.ProductRetrieveDto::concept)
                .map(ItemDetailsDto.ConceptDto::products)
                .filter(products -> !products.isEmpty())
                .map(List::getFirst)
                .orElse(null);

        if (product == null) {
            return null;
        }

        ItemDetailsDto.PriceDto price = Optional.ofNullable(product.webctas())
                .filter(webctas -> !webctas.isEmpty())
                .map(List::getFirst)
                .map(ItemDetailsDto.WebCtaDto::price)
                .orElse(null);

        String id = product.id();
        String name = product.name();
        BigDecimal basePrice = parsePrice(price != null ? price.basePrice() : null, false);
        BigDecimal currentPrice = parsePrice(price != null ? price.currentPrice() : null, true);

        return new Item(id, name, basePrice, currentPrice);
    }
}
