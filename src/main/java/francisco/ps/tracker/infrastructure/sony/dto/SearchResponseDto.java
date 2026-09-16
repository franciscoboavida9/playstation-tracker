package francisco.ps.tracker.infrastructure.sony.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Data Transfer Object mapping the Sony Universal Search GraphQL response.
 * Acts as the first step in the data pipeline, resolving unpredictable user
 * text queries into exact PlayStation Store product IDs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResponseDto(
        DataDto data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DataDto(
       @JsonProperty("universalSearch") SearchDto search
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchDto(
            List<ResultDto> results
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultDto(
            String id,
            List<MediaDto> media,
            String name,
            PriceDto price
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MediaDto(
            @JsonProperty("role") String imageRole,
            @JsonProperty("url") String imageUrl
    ) implements MediaInfo {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PriceDto(
            String basePrice,
            @JsonProperty("discountedPrice") String currentPrice
    ) {}
}
