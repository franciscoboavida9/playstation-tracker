package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerService;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WishlistPaginationHandlerTest {

    private final TrackerService trackerService = mock(TrackerService.class);
    private final MessageFormatter messageFormatter = mock(MessageFormatter.class);
    private final WishlistPaginationHandler handler = new WishlistPaginationHandler(trackerService, messageFormatter);
    private final TelegramClient telegramClient = mock(TelegramClient.class);

    private Update createMockCallbackUpdate(String callbackData, Long chatId, Integer messageId) {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getMessage()).thenReturn(message);

        if (chatId != null) {
            when(message.getChatId()).thenReturn(chatId);
        }
        if (messageId != null) {
            when(message.getMessageId()).thenReturn(messageId);
        }

        return update;
    }

    @Test
    void shouldSupportWishlistPaginationCallback() {
        Update update = createMockCallbackUpdate("wishlist_page:1", null, null);
        assertThat(handler.supports(update)).isTrue();
    }

    @Test
    void shouldNotSupportOtherCallback() {
        Update update = createMockCallbackUpdate("srch:1:test", null, null);
        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldNotSupportUpdatesWithoutCallbackQuery() {
        Update update = mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldHandleValidPaginationCallbackSuccessfully() {
        Update update = createMockCallbackUpdate("wishlist_page:1", 12345L, 999);

        Item item0 = new Item("0L", "Elden Ring 0", new BigDecimal("59.99"), new BigDecimal("39.99"), "url0");
        Tracker tracker0 = mock(Tracker.class);
        when(tracker0.getItem()).thenReturn(item0);

        Item item1 = new Item("1L", "Elden Ring 1", new BigDecimal("49.99"), new BigDecimal("29.99"), "url1");
        Tracker tracker1 = mock(Tracker.class);
        when(tracker1.getItem()).thenReturn(item1);

        when(trackerService.wishlist(12345L)).thenReturn(List.of(tracker0, tracker1));

        handler.handle(update, telegramClient);

        // Verifies the edit method is called with index 1 and total size 2
        verify(messageFormatter).editWishlistCarousel(12345L, 999, item1, 1, 2, telegramClient);
    }

    @Test
    void shouldIgnoreWhenInvalidIndexIsProvided() {
        Update update = createMockCallbackUpdate("wishlist_page:5", 12345L, 999);

        Item item0 = new Item("0L", "Elden Ring 0", new BigDecimal("59.99"), new BigDecimal("39.99"), "url0");
        Tracker tracker0 = mock(Tracker.class);
        when(tracker0.getItem()).thenReturn(item0);

        when(trackerService.wishlist(12345L)).thenReturn(List.of(tracker0));

        handler.handle(update, telegramClient);

        verifyNoInteractions(messageFormatter);
    }

    @Test
    void shouldIgnoreWhenWishlistIsEmpty() {
        Update update = createMockCallbackUpdate("wishlist_page:0", 12345L, 999);

        when(trackerService.wishlist(12345L)).thenReturn(Collections.emptyList());

        handler.handle(update, telegramClient);

        verifyNoInteractions(messageFormatter);
    }
}