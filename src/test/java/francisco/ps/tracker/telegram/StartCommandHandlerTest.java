package francisco.ps.tracker.telegram;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StartCommandHandlerTest {

    private final StartCommandHandler handler = new StartCommandHandler();
    private final TelegramClient telegramClient = mock(TelegramClient.class);

    @Test
    void shouldSupportStartCommand() {
        Update update = new Update();
        Message message = new Message();
        message.setText("/start");
        update.setMessage(message);

        assertThat(handler.supports(update)).isTrue();
    }

    @Test
    void shouldNotSupportOtherCommands() {
        Update update = new Update();
        Message message = new Message();
        message.setText("/search");
        update.setMessage(message);

        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldNotSupportUpdatesWithoutMessages() {
        // Simulates a button click where getMessage() is null
        Update update = new Update();

        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldNotSupportMessagesWithoutText() {
        Update update = new Update();
        Message message = new Message();
        // Simulates a user sending a photo or sticker (no text)
        update.setMessage(message);

        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldExecuteSendMessageOnHandleWithCorrectData() throws TelegramApiException {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/start");
        when(message.getChatId()).thenReturn(12345L);

        handler.handle(update, telegramClient);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient, times(1)).execute(messageCaptor.capture());

        SendMessage capturedMessage = messageCaptor.getValue();

        assertThat(capturedMessage.getChatId()).isEqualTo("12345");
        assertThat(capturedMessage.getText()).contains("Welcome to PS Tracker");
    }
}
