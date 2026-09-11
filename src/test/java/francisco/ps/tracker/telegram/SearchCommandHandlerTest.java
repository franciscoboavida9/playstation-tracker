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
    private final SearchCommandHandler handler = new SearchCommandHandler(itemService);
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
    void shouldSendUsageInstructionWhenQueryIsEmpty() throws TelegramApiException {
        Update update = createMockUpdate("/search", 12345L);

        handler.handle(update, telegramClient);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        assertThat(captor.getValue().getChatId()).isEqualTo("12345");
        assertThat(captor.getValue().getText()).contains("Please provide a game name");
    }

    @Test
    void shouldSendNoGamesFoundMessageWhenServiceReturnsEmpty() throws TelegramApiException {
        Update update = createMockUpdate("/search UnknownGame", 12345L);
        when(itemService.search("UnknownGame")).thenReturn(Collections.emptyList());

        handler.handle(update, telegramClient);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage capturedMessage = captor.getValue();
        assertThat(capturedMessage.getChatId()).isEqualTo("12345");
        assertThat(capturedMessage.getText()).contains("No games found");
    }

    @Test
    void shouldExecuteSendMessageOnHandleWithCorrectItemData() throws TelegramApiException {
        Update update = createMockUpdate("/search Elden Ring", 12345L);

        Item mockItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("39.99"));

        when(itemService.search("Elden Ring")).thenReturn(List.of(mockItem));

        handler.handle(update, telegramClient);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient, atLeastOnce()).execute(captor.capture());

        SendMessage capturedMessage = captor.getValue();
        assertThat(capturedMessage.getChatId()).isEqualTo("12345");
        assertThat(capturedMessage.getText()).contains("Elden Ring");
        assertThat(capturedMessage.getText()).contains("39.99");
        assertThat(capturedMessage.getText()).contains("59.99");

        // Inspect the keyboard and the callback payload
        assertThat(capturedMessage.getReplyMarkup()).isInstanceOf(InlineKeyboardMarkup.class);
        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) capturedMessage.getReplyMarkup();
        String payload = markup.getKeyboard().getFirst().getFirst().getCallbackData();
        assertThat(payload).isEqualTo("track:1L");
    }
}
