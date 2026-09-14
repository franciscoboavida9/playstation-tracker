package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.game.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SearchCommandHandlerTest {

    private final ItemService itemService = mock(ItemService.class);
    private final MessageFormatter messageFormatter = mock(MessageFormatter.class);
    private final SearchCommandHandler handler = new SearchCommandHandler(itemService, messageFormatter);
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
    void shouldSupportSearchCommandWithQuery() {
        Update update = createMockUpdate("/search Elden Ring", null);
        assertThat(handler.supports(update)).isTrue();
    }

    @Test
    void shouldSupportBareSearchCommand() {
        Update update = createMockUpdate("/search", null);
        assertThat(handler.supports(update)).isTrue();
    }

    @Test
    void shouldNotSupportOtherCommands() {
        Update update = createMockUpdate("/start", null);
        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldSendUsageInstructionWhenQueryIsEmpty() {
        Update update = createMockUpdate("/search", 12345L);

        handler.handle(update, telegramClient);

        verify(messageFormatter).sendTextMessage(eq(12345L), contains("Please provide a game name"), eq(telegramClient));
    }

    @Test
    void shouldSendNoGamesFoundMessageWhenServiceReturnsEmpty() {
        Update update = createMockUpdate("/search UnknownGame", 12345L);
        when(itemService.search("UnknownGame")).thenReturn(Collections.emptyList());

        handler.handle(update, telegramClient);

        verify(messageFormatter).sendTextMessage(eq(12345L), contains("No games found"), eq(telegramClient));
    }

    @Test
    void shouldExecuteSendMessageOnHandleWithCorrectItemData() {
        Update update = createMockUpdate("/search Elden Ring", 12345L);

        Item mockItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("39.99"), "http://example.com/image.png");

        when(itemService.search("Elden Ring")).thenReturn(List.of(mockItem));
        handler.handle(update, telegramClient);

        verify(messageFormatter).sendItemMessage(12345L, mockItem, "➕ Track Game", "track:1L", telegramClient);
    }
}
