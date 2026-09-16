package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerService;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WishlistCommandHandlerTest {

    private final TrackerService trackerService = mock(TrackerService.class);
    private final MessageFormatter messageFormatter = mock(MessageFormatter.class);
    private final WishlistCommandHandler handler = new WishlistCommandHandler(trackerService, messageFormatter);
    private final TelegramClient telegramClient = mock(TelegramClient.class);

    private Update createMockUpdate(String text, Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(text != null);
        when(message.getText()).thenReturn(text);
        if (chatId != null) {
            when(message.getChatId()).thenReturn(chatId);
        }
        return update;
    }

    @Test
    void shouldSupportWishlistCommand() {
        Update update = createMockUpdate("/wishlist", null);
        assertThat(handler.supports(update)).isTrue();
    }

    @Test
    void shouldNotSupportOtherCommands() {
        Update update = createMockUpdate("/search", null);
        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldSendEmptyMessageWhenWishlistIsEmpty() {
        Update update = createMockUpdate("/wishlist", 12345L);
        when(trackerService.wishlist(12345L)).thenReturn(Collections.emptyList());

        handler.handle(update, telegramClient);
        verify(messageFormatter).sendTextMessage(eq(12345L), contains("Your wishlist is empty"), eq(telegramClient));
    }

    @Test
    void shouldSendWishlistCarouselWhenWishlistHasGames() {
        Update update = createMockUpdate("/wishlist", 12345L);
        Item mockItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("39.99"), "http://example.com/image.png");
        Tracker mockTracker = mock(Tracker.class);
        when(mockTracker.getItem()).thenReturn(mockItem);

        when(trackerService.wishlist(12345L)).thenReturn(List.of(mockTracker));
        handler.handle(update, telegramClient);

        // Verifies the carousel is launched starting at index 0
        verify(messageFormatter).sendWishlistCarousel(12345L, mockItem, 0, 1, telegramClient);
    }
}