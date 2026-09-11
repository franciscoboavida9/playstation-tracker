package francisco.ps.tracker.telegram;

import francisco.ps.tracker.tracker.TrackerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UntrackCommandHandlerTest {

    private final TrackerService trackerService = mock(TrackerService.class);
    private final UntrackCommandHandler handler = new UntrackCommandHandler(trackerService);
    private final TelegramClient telegramClient = mock(TelegramClient.class);

    private Update createMockCallbackUpdate(String callbackData, Long chatId, Integer messageId, String queryId) {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);

        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn(queryId);
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
    void shouldSupportUntrackCallback() {
        Update update = createMockCallbackUpdate("untrack:123", null, null, null);
        assertThat(handler.supports(update)).isTrue();
    }

    @Test
    void shouldNotSupportDifferentCallback() {
        Update update = createMockCallbackUpdate("track:123", null, null, null);
        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldNotSupportUpdatesWithoutCallbackQuery() {
        Update update = mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldHandleValidUntrackCallbackSuccessfully() throws TelegramApiException {
        Update update = createMockCallbackUpdate("untrack:EP4497-ELDENRING", 12345L, 999, "query-1");
        handler.handle(update, telegramClient);

        verify(trackerService, times(1)).untrack(12345L, "EP4497-ELDENRING");

        ArgumentCaptor<AnswerCallbackQuery> answerCaptor = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(telegramClient).execute(answerCaptor.capture());

        AnswerCallbackQuery capturedAnswer = answerCaptor.getValue();
        assertThat(capturedAnswer.getCallbackQueryId()).isEqualTo("query-1");
        assertThat(capturedAnswer.getText()).contains("removed from wishlist");

        ArgumentCaptor<EditMessageText> editCaptor = ArgumentCaptor.forClass(EditMessageText.class);
        verify(telegramClient).execute(editCaptor.capture());

        EditMessageText capturedEdit = editCaptor.getValue();
        assertThat(capturedEdit.getChatId()).isEqualTo("12345");
        assertThat(capturedEdit.getMessageId()).isEqualTo(999);
        assertThat(capturedEdit.getText()).contains("no longer being tracked");
        assertThat(capturedEdit.getReplyMarkup()).isNull(); // Asserts that the button was removed
    }

    @Test
    void shouldSendAlertPopupWhenDatabaseThrowsException() throws TelegramApiException {
        Update update = createMockCallbackUpdate("untrack:SOME_ID", 12345L, 999, "query-2");

        doThrow(new RuntimeException("DB Deletion failed"))
                .when(trackerService).untrack(12345L, "SOME_ID");

        handler.handle(update, telegramClient);

        ArgumentCaptor<AnswerCallbackQuery> answerCaptor = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(telegramClient).execute(answerCaptor.capture());

        AnswerCallbackQuery capturedAnswer = answerCaptor.getValue();
        assertThat(capturedAnswer.getCallbackQueryId()).isEqualTo("query-2");
        assertThat(capturedAnswer.getText()).contains("Could not remove game");
        assertThat(capturedAnswer.getShowAlert()).isTrue();

        verify(telegramClient, never()).execute(any(EditMessageText.class));
    }
}