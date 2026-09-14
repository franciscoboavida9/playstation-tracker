package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MessageFormatterTest {

    private final TelegramClient telegramClient = mock(TelegramClient.class);
    private final MessageFormatter messageFormatter = new MessageFormatter();

    @Test
    @DisplayName("Should send a SendPhoto when the item has a valid cover image")
    void shouldSendPhotoWhenCoverImageIsPresent() throws TelegramApiException {
        Item item = new Item("1", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("39.99"), "http://example.com/image.png");

        messageFormatter.sendItemMessage(123L, item, "➕ Track", "track:1", telegramClient);

        // Capture the SendPhoto object that gets sent to the client
        ArgumentCaptor<SendPhoto> captor = ArgumentCaptor.forClass(SendPhoto.class);
        verify(telegramClient).execute(captor.capture());

        SendPhoto captured = captor.getValue();
        assertThat(captured.getChatId()).isEqualTo("123");
        assertThat(captured.getCaption()).contains("Elden Ring");
        assertThat(captured.getPhoto().getAttachName()).isEqualTo("http://example.com/image.png");
    }

    @Test
    @DisplayName("Should fallback to SendMessage when the cover image is null")
    void shouldSendMessageWhenCoverImageIsNull() throws TelegramApiException {
        Item item = new Item("1", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("39.99"), null);

        messageFormatter.sendItemMessage(123L, item, "➕ Track", "track:1", telegramClient);

        // Capture the SendMessage object instead
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage captured = captor.getValue();
        assertThat(captured.getChatId()).isEqualTo("123");
        assertThat(captured.getText()).contains("Elden Ring");
    }

    @Test
    @DisplayName("Should fallback to SendMessage when the cover image is an empty string")
    void shouldSendMessageWhenCoverImageIsEmpty() throws TelegramApiException {
        Item item = new Item("1", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("39.99"), "");

        messageFormatter.sendItemMessage(123L, item, "➕ Track", "track:1", telegramClient);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage captured = captor.getValue();
        assertThat(captured.getChatId()).isEqualTo("123");
    }

    @Test
    @DisplayName("sendTextMessage should correctly build and execute a simple text message")
    void shouldSendSimpleTextMessage() throws TelegramApiException {
        messageFormatter.sendTextMessage(123L, "Simple Message", telegramClient);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage captured = captor.getValue();
        assertThat(captured.getChatId()).isEqualTo("123");
        assertThat(captured.getText()).isEqualTo("Simple Message");
    }
}