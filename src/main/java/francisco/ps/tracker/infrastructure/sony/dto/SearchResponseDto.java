package francisco.ps.tracker.infrastructure.sony.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
            String name,
            PriceDto price
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PriceDto(
            String basePrice,
            @JsonProperty("discountedPrice") String currentPrice
    ) {}
}
