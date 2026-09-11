package francisco.ps.tracker.telegram;

import francisco.ps.tracker.tracker.TrackerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TrackCommandHandlerTest {

    private final TrackerService trackerService = mock(TrackerService.class);
    private final TrackCommandHandler handler = new TrackCommandHandler(trackerService);
    private final TelegramClient telegramClient = mock(TelegramClient.class);

    private Update createMockCallbackUpdate(String callbackData, Long chatId, String chatType, String queryId) {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);

        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn(queryId);
        when(callbackQuery.getMessage()).thenReturn(message);

        when(message.getChat()).thenReturn(chat);

        if (chatId != null) {
            when(message.getChatId()).thenReturn(chatId);
        }
        if (chatType != null) {
            when(chat.getType()).thenReturn(chatType);
        }

        return update;
    }

    @Test
    void shouldSupportTrackCallback() {
        Update update = createMockCallbackUpdate("track:123", null, null, null);
        assertThat(handler.supports(update)).isTrue();
    }

    @Test
    void shouldNotSupportDifferentCallback() {
        Update update = createMockCallbackUpdate("untrack:123", null, null, null);
        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldNotSupportUpdatesWithoutCallbackQuery() {
        Update update = mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldHandleValidTrackCallbackSuccessfully() throws TelegramApiException {
        Update update = createMockCallbackUpdate("track:EP4497-ELDENRING", 12345L, "private", "query-1");

        handler.handle(update, telegramClient);

        verify(trackerService, times(1)).track(12345L, "private", "EP4497-ELDENRING");

        ArgumentCaptor<AnswerCallbackQuery> answerCaptor = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(telegramClient).execute(answerCaptor.capture());

        AnswerCallbackQuery capturedAnswer = answerCaptor.getValue();
        assertThat(capturedAnswer.getCallbackQueryId()).isEqualTo("query-1");
        assertThat(capturedAnswer.getText()).contains("added to your watchlist");

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());

        SendMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getChatId()).isEqualTo("12345");
        assertThat(capturedMessage.getText()).contains("Successfully added");
    }

    @Test
    void shouldSendAlertPopupWhenDatabaseThrowsException() throws TelegramApiException {
        Update update = createMockCallbackUpdate("track:SOME_ID", 12345L, "private", "query-2");

        doThrow(new RuntimeException("Duplicate track exception"))
                .when(trackerService).track(12345L, "private", "SOME_ID");

        handler.handle(update, telegramClient);

        ArgumentCaptor<AnswerCallbackQuery> answerCaptor = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(telegramClient).execute(answerCaptor.capture());

        AnswerCallbackQuery capturedAnswer = answerCaptor.getValue();
        assertThat(capturedAnswer.getCallbackQueryId()).isEqualTo("query-2");
        assertThat(capturedAnswer.getText()).contains("Could not track game");
        assertThat(capturedAnswer.getShowAlert()).isTrue();

        verify(telegramClient, never()).execute(any(SendMessage.class));
    }
}