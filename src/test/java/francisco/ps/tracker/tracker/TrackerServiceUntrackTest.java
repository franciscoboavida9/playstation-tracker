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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class TrackerServiceUntrackTest {
    @Mock
    private TrackerRepository trackerRepository;

    @InjectMocks
    private TrackerService trackerService;

    @Test
    @DisplayName("Successfully untracks an active item by setting isActive to false")
    void shouldUntrackActiveItem() {
        // Arrange
        Long chatId = 123L;
        String itemId = "PPSA01234_00";
        TrackerId trackerId = new TrackerId(chatId, itemId);

        Chat chat = new Chat(chatId, "private", LocalDateTime.now());
        Item item = new Item(itemId, "Test Game", new BigDecimal("59.99"), new BigDecimal("39.99"));
        Tracker activeTracker = new Tracker(chat, item, trackerId, new BigDecimal("39.99"), true, LocalDateTime.now());

        Mockito.when(trackerRepository.findById(trackerId)).thenReturn(Optional.of(activeTracker));

        // Act
        trackerService.untrack(chatId, itemId);

        // Assert
        assertThat(activeTracker.isActive()).isFalse();
        Mockito.verify(trackerRepository).save(activeTracker);
    }

    @Test
    @DisplayName("Throws exception when trying to untrack an item that is not in the database")
    void shouldThrowExceptionWhenUntrackingNonExistentItem() {
        // Arrange
        Long chatId = 123L;
        String itemId = "PPSA01234_00";
        TrackerId trackerId = new TrackerId(chatId, itemId);

        Mockito.when(trackerRepository.findById(trackerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> trackerService.untrack(chatId, itemId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You are not tracking this game.");

        // Prove that the database was never touched!
        Mockito.verify(trackerRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    @DisplayName("Throws exception when trying to untrack an item that is already inactive")
    void shouldThrowExceptionWhenUntrackingAlreadyInactiveItem() {
        // Arrange
        Long chatId = 123L;
        String itemId = "PPSA01234_00";
        TrackerId trackerId = new TrackerId(chatId, itemId);

        Chat chat = new Chat(chatId, "private", LocalDateTime.now());
        Item item = new Item(itemId, "Test Game", new BigDecimal("59.99"), new BigDecimal("39.99"));
        Tracker inactiveTracker = new Tracker(chat, item, trackerId, new BigDecimal("39.99"), false, LocalDateTime.now());

        Mockito.when(trackerRepository.findById(trackerId)).thenReturn(Optional.of(inactiveTracker));

        // Act & Assert
        assertThatThrownBy(() -> trackerService.untrack(chatId, itemId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You are already not tracking this game.");

        // Prove that the redundant database write was prevented!
        Mockito.verify(trackerRepository, Mockito.never()).save(Mockito.any());
    }
}
