package francisco.ps.tracker.infrastructure.sony.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Data Transfer Object mapping the Sony Media GraphQL response.
 * Fetched concurrently during the Aggregation phase to retrieve high-resolution
 * cover art that is intentionally stripped from the standard details endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemMediaDto(
        DataDto data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DataDto(
            ProductRetrieveDto productRetrieve
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductRetrieveDto(
            List<MediaDto> media
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MediaDto(
            @JsonProperty("role") String imageRole,
            @JsonProperty("url") String imageUrl
    ) implements MediaInfo {}
}
