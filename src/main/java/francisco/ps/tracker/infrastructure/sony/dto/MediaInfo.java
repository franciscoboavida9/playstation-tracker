package francisco.ps.tracker.infrastructure.sony.dto;

/**
 * Contract for media objects returned by the Sony API.
 * Ensures consistent access to high-resolution image URLs across various
 * fragmented GraphQL response structures.
 */
public interface MediaInfo {
    String imageRole();
    String imageUrl();
}
