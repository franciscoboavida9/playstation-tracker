package francisco.ps.tracker.infrastructure.sony.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GameAndEditionDto(
        DataDto data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DataDto(
            ProductRetrieveDto productRetrieve
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductRetrieveDto (
            ConceptDto concept
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConceptDto (
            List<ProductDto> products,
            @JsonProperty("name") String title
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductDto (
            String id,
            @JsonProperty("name") String editionName,
            List<WebCtaDto> webctas
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WebCtaDto(
            PriceDto price
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PriceDto(
            String basePrice,
            @JsonProperty("discountedPrice") String currentPrice
    ) {}
}
