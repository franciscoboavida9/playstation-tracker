package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerService;
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

class WishlistCommandHandlerTest {

    private final TrackerService trackerService = mock(TrackerService.class);
    private final WishlistCommandHandler handler = new WishlistCommandHandler(trackerService);
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
    void shouldSendEmptyMessageWhenWishlistIsEmpty() throws TelegramApiException {
        Update update = createMockUpdate("/wishlist", 12345L);
        when(trackerService.wishlist(12345L)).thenReturn(Collections.emptyList());

        handler.handle(update, telegramClient);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient, times(1)).execute(captor.capture());

        SendMessage capturedMessage = captor.getValue();
        assertThat(capturedMessage.getChatId()).isEqualTo("12345");
        assertThat(capturedMessage.getText()).contains("Your wishlist is empty");
    }

    @Test
    void shouldSendHeaderAndItemsWhenWishlistHasGames() throws TelegramApiException {
        Update update = createMockUpdate("/wishlist", 12345L);

        // Arrange
        Item mockItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("39.99"));
        Tracker mockTracker = mock(Tracker.class);
        when(mockTracker.getItem()).thenReturn(mockItem);

        when(trackerService.wishlist(12345L)).thenReturn(List.of(mockTracker));

        // Act
        handler.handle(update, telegramClient);

        // Assert
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient, times(2)).execute(captor.capture());

        List<SendMessage> sentMessages = captor.getAllValues();

        // Check the Header Message
        SendMessage headerMessage = sentMessages.getFirst();
        assertThat(headerMessage.getChatId()).isEqualTo("12345");
        assertThat(headerMessage.getText()).contains("Here are your tracked games");

        // Check the Game Message
        SendMessage gameMessage = sentMessages.get(1);
        assertThat(gameMessage.getChatId()).isEqualTo("12345");
        assertThat(gameMessage.getText()).contains("Elden Ring");
        assertThat(gameMessage.getText()).contains("39.99");
        assertThat(gameMessage.getText()).contains("ON SALE");

        assertThat(gameMessage.getReplyMarkup()).isInstanceOf(InlineKeyboardMarkup.class);
        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) gameMessage.getReplyMarkup();
        String payload = markup.getKeyboard().getFirst().getFirst().getCallbackData();
        assertThat(payload).isEqualTo("untrack:1L");
    }
}