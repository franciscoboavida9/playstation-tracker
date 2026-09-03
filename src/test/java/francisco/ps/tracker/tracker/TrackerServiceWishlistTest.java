package francisco.ps.tracker.tracker;

import francisco.ps.tracker.chat.Chat;
import francisco.ps.tracker.game.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TrackerServiceWishlistTest {

    @Mock
    private TrackerRepository trackerRepository;

    @InjectMocks
    private TrackerService trackerService;

    @Test
    @DisplayName("Returns active wishlist games successfully")
    void shouldReturnActiveWishlist() {
        // Arrange
        Long chatId = 123L;
        String chatType = "private";
        TrackerId trackerId = new TrackerId(chatId, "PPSA01234_00");

        Chat chat = new Chat(chatId, chatType, LocalDateTime.now());
        Item item = new Item("PPSA01234_00", "Test Game", new BigDecimal("59.99"), new BigDecimal("39.99"));
        Tracker tracker = new Tracker(chat, item, trackerId, new BigDecimal("39.99"), true, LocalDateTime.now());

        Mockito.when(trackerRepository.findByChatIdAndIsActiveTrue(chatId))
                .thenReturn(List.of(tracker));

        // Act
        List<Tracker> result = trackerService.wishlist(chatId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(tracker);
    }

    @Test
    @DisplayName("Returns empty list when chat has no tracked games")
    void shouldReturnEmptyListWhenNoTrackersExist() {
        // Arrange
        Long chatId = 999L;

        Mockito.when(trackerRepository.findByChatIdAndIsActiveTrue(chatId))
                .thenReturn(Collections.emptyList());

        // Act
        List<Tracker> result = trackerService.wishlist(chatId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Returns multiple active wishlist games successfully")
    void shouldReturnMultipleActiveWishlistGames() {
        // Arrange
        Long chatId = 123L;
        String chatType = "private";

        Chat chat = new Chat(chatId, chatType, LocalDateTime.now());

        Item item1 = new Item("PPSA01234_00", "Game One", new BigDecimal("59.99"), new BigDecimal("39.99"));
        Tracker tracker1 = new Tracker(chat, item1, new TrackerId(chatId, "PPSA01234_00"), new BigDecimal("39.99"), true, LocalDateTime.now());

        Item item2 = new Item("PPSA05678_00", "Game Two", new BigDecimal("49.99"), new BigDecimal("29.99"));
        Tracker tracker2 = new Tracker(chat, item2, new TrackerId(chatId, "PPSA05678_00"), new BigDecimal("29.99"), true, LocalDateTime.now());

        Mockito.when(trackerRepository.findByChatIdAndIsActiveTrue(chatId))
                .thenReturn(List.of(tracker1, tracker2));

        // Act
        List<Tracker> result = trackerService.wishlist(chatId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(tracker1, tracker2);
    }
}