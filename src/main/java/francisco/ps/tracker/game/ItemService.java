package francisco.ps.tracker.game;

import francisco.ps.tracker.infrastructure.sony.SonyStoreClient;
import francisco.ps.tracker.infrastructure.sony.dto.ItemDetailsDto;
import francisco.ps.tracker.infrastructure.sony.dto.ItemMediaDto;
import francisco.ps.tracker.infrastructure.sony.dto.MediaInfo;
import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

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
        log.debug("Formatting search results for query: {}", input);
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
            String coverImage = extractCoverImage(search.media());
            String name = search.name();

            SearchResponseDto.PriceDto priceObj = search.price();
            BigDecimal basePrice = parsePrice(priceObj != null ? priceObj.basePrice() : null);
            BigDecimal currentPrice = parsePrice(priceObj != null ? priceObj.currentPrice() : null);

            results.add(new Item(id, name, basePrice, currentPrice, coverImage));
        }

        return results;
    }

    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return new BigDecimal("0.00");
        }

        String cleanPrice = priceStr.replaceAll("[^\\d.,]", "");
        cleanPrice = cleanPrice.replace(",", ".");
        if (cleanPrice.isEmpty() || cleanPrice.equals(".")) {
            return new BigDecimal("0.00");
        }

        try {
            return new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            log.error("Could not parse price! Original: '{}', Cleaned: '{}'", priceStr, cleanPrice);
            return new BigDecimal("0.00");
        }
    }

    /**
     * Fetches item details from the Sony Store and returns the parsed Item object.
     * @param itemId The unique identifier of the item.
     * @return An Item containing the details of the product.
     */
    public Item searchById(String itemId) {
        ItemDetailsDto itemDetailsDto = sonyStoreClient.itemDetails(itemId);

        ItemDetailsDto.ProductRetrieveDto product = Optional.ofNullable(itemDetailsDto)
                .map(ItemDetailsDto::data)
                .map(ItemDetailsDto.DataDto::productRetrieve)
                .orElse(null);

        if (product == null) {
            return null;
        }

        ItemDetailsDto.PriceDto price = Optional.ofNullable(product.webctas())
                .filter(webctas -> !webctas.isEmpty())
                .flatMap(webctas -> webctas.stream()
                        .filter(cta -> cta.price() != null && !Boolean.TRUE.equals(cta.price().isTiedToSubscription()))
                        .filter(cta -> cta.type() == null || !cta.type().startsWith("UPSELL"))
                        .map(ItemDetailsDto.WebCtaDto::price)
                        .findFirst()
                        .or(() -> Optional.ofNullable(webctas.getFirst().price())))
                .orElse(null);

        ItemMediaDto itemMediaDto = sonyStoreClient.itemMedia(itemId);

        List<ItemMediaDto.MediaDto> mediaList = Optional.ofNullable(itemMediaDto)
                .map(ItemMediaDto::data)
                .map(ItemMediaDto.DataDto::productRetrieve)
                .map(ItemMediaDto.ProductRetrieveDto::media)
                .orElse(Collections.emptyList());

        String id = product.id();
        String coverImage = extractCoverImage(mediaList);
        String name = product.name();
        BigDecimal basePrice = parsePrice(price != null ? price.basePrice() : null);
        BigDecimal currentPrice = parsePrice(price != null ? price.currentPrice() : null);

        log.debug("Found Item: {} | Base: {} | Current: {}", name, basePrice, currentPrice);

        return new Item(id, name, basePrice, currentPrice, coverImage);
    }

    private String extractCoverImage(List<? extends MediaInfo> mediaList) {
        if (mediaList == null || mediaList.isEmpty()) {
            return null;
        }

        return mediaList.stream()
                .filter(media -> "MASTER".equalsIgnoreCase(media.imageRole()))
                .map(MediaInfo::imageUrl)
                .findFirst()
                .orElseGet(() -> mediaList.getLast().imageUrl());
    }
}
