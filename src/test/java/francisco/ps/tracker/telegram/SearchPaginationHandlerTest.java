package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.game.ItemService;
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

class SearchPaginationHandlerTest {

    private final ItemService itemService = mock(ItemService.class);
    private final MessageFormatter messageFormatter = mock(MessageFormatter.class);
    private final SearchPaginationHandler handler = new SearchPaginationHandler(itemService, messageFormatter);
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
    void shouldSupportSearchPaginationCallback() {
        Update update = createMockCallbackUpdate("srch:1:elden ring", null, null);
        assertThat(handler.supports(update)).isTrue();
    }

    @Test
    void shouldNotSupportOtherCallback() {
        Update update = createMockCallbackUpdate("untrack:123:0", null, null);
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
        Update update = createMockCallbackUpdate("srch:1:elden ring", 12345L, 999);

        Item item0 = new Item("0L", "Elden Ring 0", new BigDecimal("59.99"), new BigDecimal("39.99"), "url0");
        Item item1 = new Item("1L", "Elden Ring 1", new BigDecimal("49.99"), new BigDecimal("29.99"), "url1");

        when(itemService.search("elden ring")).thenReturn(List.of(item0, item1));

        handler.handle(update, telegramClient);

        // Verifies the edit method is called with index 1 and limit 2
        verify(messageFormatter).editSearchCarousel(12345L, 999, item1, "elden ring", 1, 2, telegramClient);
    }

    @Test
    void shouldIgnoreWhenInvalidIndexIsProvided() {
        // Trying to access index 5, but there are only 2 items
        Update update = createMockCallbackUpdate("srch:5:elden ring", 12345L, 999);

        Item item0 = new Item("0L", "Elden Ring 0", new BigDecimal("59.99"), new BigDecimal("39.99"), "url0");
        Item item1 = new Item("1L", "Elden Ring 1", new BigDecimal("49.99"), new BigDecimal("29.99"), "url1");

        when(itemService.search("elden ring")).thenReturn(List.of(item0, item1));

        handler.handle(update, telegramClient);

        verifyNoInteractions(messageFormatter);
    }

    @Test
    void shouldIgnoreWhenItemsAreEmpty() {
        Update update = createMockCallbackUpdate("srch:1:elden ring", 12345L, 999);

        when(itemService.search("elden ring")).thenReturn(Collections.emptyList());

        handler.handle(update, telegramClient);

        verifyNoInteractions(messageFormatter);
    }
}
