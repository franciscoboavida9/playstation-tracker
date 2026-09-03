package francisco.ps.tracker.tracker;

import francisco.ps.tracker.chat.Chat;
import francisco.ps.tracker.chat.ChatRepository;
import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.game.ItemRepository;
import francisco.ps.tracker.game.ItemService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrackerServiceTrackTest {
    @Mock
    private TrackerRepository trackerRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private TrackerService trackerService;

    @Test
    @DisplayName("Throws exception when user is already tracking the item")
    void shouldThrowExceptionWhenAlreadyTracking() {
        // Arrange
        Long chatId = 123L;
        String chatType = "private";
        String itemId = "PPSA01234_00";
        TrackerId trackerId = new TrackerId(chatId, itemId);

        Chat chat = new Chat(chatId, chatType, LocalDateTime.now());
        Item item = new Item(itemId, "Test Game", new BigDecimal("59.99"), new BigDecimal("39.99"));
        Tracker activeTracker =
                new Tracker(chat, item, trackerId, new BigDecimal("39.99"), true, LocalDateTime.now());

        when(trackerRepository.findById(trackerId)).thenReturn(Optional.of(activeTracker));

        // Act and Assert
        assertThatThrownBy(() -> trackerService.track(chatId, chatType, itemId))
                .isInstanceOf(IllegalStateException.class) // <--- Make sure this matches the expected exception!
                .hasMessage("You are already tracking this game!");
    }

    @Test
    @DisplayName("Reactivates previously inactive tracker and updates target price")
    void shouldReactivateInactiveTracker() {
        // Arrange
        Long chatId = 123L;
        String chatType = "private";
        String itemId = "PPSA01234_00";
        TrackerId trackerId = new TrackerId(chatId, itemId);

        Chat chat = new Chat(chatId, chatType, LocalDateTime.now());
        Item item = new Item(itemId, "Test Game", new BigDecimal("59.99"), new BigDecimal("39.99"));
        Tracker inactiveTracker = new Tracker(chat, item, trackerId, new BigDecimal("49.99"), false, LocalDateTime.now());

        Mockito.when(trackerRepository.findById(trackerId)).thenReturn(Optional.of(inactiveTracker));
        Mockito.when(trackerRepository.save(Mockito.any(Tracker.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tracker result = trackerService.track(chatId, chatType, itemId);

        // Assert
        assertThat(result.isActive()).isTrue();
        assertThat(result.getTargetPrice()).isEqualByComparingTo(new BigDecimal("39.98")); // currentPrice (39.99) - 0.01
        Mockito.verify(trackerRepository).save(inactiveTracker);
    }

    @Test
    @DisplayName("Throws exception when item is missing in database and PlayStation Store")
    void shouldThrowExceptionWhenItemNotFound() {
        // Arrange
        Long chatId = 123L;
        String chatType = "private";
        String itemId = "NON_EXISTENT_ID";
        TrackerId trackerId = new TrackerId(chatId, itemId);

        Mockito.when(trackerRepository.findById(trackerId)).thenReturn(Optional.empty());
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        Mockito.when(itemService.searchById(itemId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> trackerService.track(chatId, chatType, itemId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item could not be found on PlayStation Store.");
    }

    @Test
    @DisplayName("Successfully creates chat, item, and tracker when nothing exists in database")
    void shouldCreateNewTrackerWhenEverythingIsNew() {
        // Arrange
        Long chatId = 123L;
        String chatType = "private";
        String itemId = "PPSA01234_00";
        TrackerId trackerId = new TrackerId(chatId, itemId);

        Mockito.when(trackerRepository.findById(trackerId)).thenReturn(Optional.empty());
        Mockito.when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        Chat savedChat = new Chat(chatId, chatType, LocalDateTime.now());
        Mockito.when(chatRepository.save(Mockito.any(Chat.class))).thenReturn(savedChat);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        Item fetchedItem = new Item(itemId, "Test Game", new BigDecimal("59.99"), new BigDecimal("39.99"));
        Mockito.when(itemService.searchById(itemId)).thenReturn(fetchedItem);
        Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(fetchedItem);

        Mockito.when(trackerRepository.save(Mockito.any(Tracker.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tracker result = trackerService.track(chatId, chatType, itemId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getTargetPrice()).isEqualByComparingTo(new BigDecimal("39.98")); // currentPrice (39.99) - 0.01
        Mockito.verify(chatRepository).save(Mockito.any(Chat.class));
        Mockito.verify(itemRepository).save(Mockito.any(Item.class));
        Mockito.verify(trackerRepository).save(Mockito.any(Tracker.class));
    }

    @Test
    @DisplayName("Successfully creates tracker using existing chat and item without saving duplicates")
    void shouldCreateTrackerUsingExistingChatAndItem() {
        // Arrange
        Long chatId = 123L;
        String chatType = "private";
        String itemId = "PPSA01234_00";
        TrackerId trackerId = new TrackerId(chatId, itemId);

        Mockito.when(trackerRepository.findById(trackerId)).thenReturn(Optional.empty());

        Chat existingChat = new Chat(chatId, chatType, LocalDateTime.now());
        Mockito.when(chatRepository.findById(chatId)).thenReturn(Optional.of(existingChat));

        Item existingItem = new Item(itemId, "Test Game", new BigDecimal("59.99"), new BigDecimal("39.99"));
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        Mockito.when(trackerRepository.save(Mockito.any(Tracker.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tracker result = trackerService.track(chatId, chatType, itemId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getChat()).isEqualTo(existingChat);
        assertThat(result.getItem()).isEqualTo(existingItem);
        assertThat(result.getTargetPrice()).isEqualByComparingTo(new BigDecimal("39.98"));

        Mockito.verify(chatRepository, Mockito.never()).save(Mockito.any(Chat.class));
        Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any(Item.class));
        Mockito.verify(trackerRepository).save(Mockito.any(Tracker.class));
    }
}
