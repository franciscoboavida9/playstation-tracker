package francisco.ps.tracker.infrastructure.sony.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Data Transfer Object mapping the Sony Product Details GraphQL response.
 * Used during the Aggregation phase to extract pristine, SKU-specific pricing
 * while intentionally bypassing Sony's bundle upsell bugs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemDetailsDto(
        DataDto data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DataDto(
            ProductRetrieveDto productRetrieve
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductRetrieveDto (
            String id,
            String name,
            List<WebCtaDto> webctas
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WebCtaDto(
            PriceDto price,
            String type
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PriceDto(
            String applicability,
            String basePrice,
            @JsonProperty("discountedPrice") String currentPrice,
            Boolean isTiedToSubscription
    ) {}
}
