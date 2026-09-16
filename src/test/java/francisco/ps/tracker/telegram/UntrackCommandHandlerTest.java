package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UntrackCommandHandlerTest {

    private final TrackerService trackerService = mock(TrackerService.class);
    private final MessageFormatter messageFormatter = mock(MessageFormatter.class);
    private final UntrackCommandHandler handler = new UntrackCommandHandler(trackerService, messageFormatter);
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
        Update update = createMockCallbackUpdate("untrack:123:0", null, null, null);
        assertThat(handler.supports(update)).isTrue();
    }

    @Test
    void shouldNotSupportUpdatesWithoutCallbackQuery() {
        Update update = mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        assertThat(handler.supports(update)).isFalse();
    }

    @Test
    void shouldUntrackAndTransitionToEmptyWishlistWhenNoGamesLeft() throws TelegramApiException {
        // User untracks the only game in their list
        Update update = createMockCallbackUpdate("untrack:EP4497:0", 12345L, 999, "query-1");
        when(trackerService.wishlist(12345L)).thenReturn(Collections.emptyList());

        handler.handle(update, telegramClient);

        verify(trackerService).untrack(12345L, "EP4497");
        ArgumentCaptor<AnswerCallbackQuery> answerCaptor = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(telegramClient).execute(answerCaptor.capture());
        assertThat(answerCaptor.getValue().getText()).contains("removed from wishlist");

        verify(messageFormatter).editToEmptyWishlist(12345L, 999, telegramClient);
    }

    @Test
    void shouldUntrackAndAutoSwipeWhenGamesRemain() throws TelegramApiException {
        Update update = createMockCallbackUpdate("untrack:EP4497:0", 12345L, 999, "query-1");

        Item nextItem = new Item("2L", "Rocket League", new BigDecimal("19.99"), new BigDecimal("19.99"), "url");
        Tracker mockTracker = mock(Tracker.class);
        when(mockTracker.getItem()).thenReturn(nextItem);
        when(trackerService.wishlist(12345L)).thenReturn(List.of(mockTracker)); // 1 game left

        handler.handle(update, telegramClient);
        verify(trackerService).untrack(12345L, "EP4497");
        verify(messageFormatter).editWishlistCarousel(12345L, 999, nextItem, 0, 1, telegramClient);
    }

    @Test
    void shouldSendAlertPopupWhenDatabaseThrowsException() throws TelegramApiException {
        Update update = createMockCallbackUpdate("untrack:SOME_ID:0", 12345L, 999, "query-2");

        doThrow(new RuntimeException("DB Deletion failed")).when(trackerService).untrack(12345L, "SOME_ID");
        handler.handle(update, telegramClient);

        ArgumentCaptor<AnswerCallbackQuery> answerCaptor = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(telegramClient).execute(answerCaptor.capture());

        AnswerCallbackQuery capturedAnswer = answerCaptor.getValue();
        assertThat(capturedAnswer.getCallbackQueryId()).isEqualTo("query-2");
        assertThat(capturedAnswer.getText()).contains("Could not remove game");
        assertThat(capturedAnswer.getShowAlert()).isTrue();

        verifyNoInteractions(messageFormatter);
    }
}