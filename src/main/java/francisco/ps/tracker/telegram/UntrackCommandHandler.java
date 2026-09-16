package francisco.ps.tracker.telegram;

import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class UntrackCommandHandler implements CommandHandler {

    private final TrackerService trackerService;
    private final MessageFormatter messageFormatter;
    private static final Logger log = LoggerFactory.getLogger(UntrackCommandHandler.class);

    public UntrackCommandHandler(TrackerService trackerService, MessageFormatter messageFormatter) {
        this.trackerService = trackerService;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public boolean supports(Update update) {
        return update.hasCallbackQuery() &&
                update.getCallbackQuery().getData().startsWith("untrack:");
    }

    @Override
    public void handle(Update update, TelegramClient telegramClient) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQueryId = update.getCallbackQuery().getId();

        String[] parts = callbackData.split(":", 3);
        String itemId = parts[1];
        int currentIndex = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        log.info("User {} attempting to untrack item: {}", chatId, itemId);

        try {
            trackerService.untrack(chatId, itemId);
            log.info("Successfully untracked item {} for user {}", itemId, chatId);

            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text("✅ Game removed from wishlist!")
                    .showAlert(false)
                    .build();
            telegramClient.execute(answer);

            List<Tracker> trackers = trackerService.wishlist(chatId);
            if (trackers == null || trackers.isEmpty()) {
                messageFormatter.editToEmptyWishlist(chatId, messageId, telegramClient);
            } else {
                int nextIndex = Math.min(currentIndex, trackers.size() - 1);

                messageFormatter.editWishlistCarousel(
                        chatId,
                        messageId,
                        trackers.get(nextIndex).getItem(),
                        nextIndex,
                        trackers.size(),
                        telegramClient
                );
            }
        } catch (TelegramApiException e) {
            log.error("Failed to execute Telegram API call", e);
        } catch (Exception e) {
            log.error("Backend error while untracking item", e);
            sendErrorAlert(callbackQueryId, telegramClient);
        }
    }

    private void sendErrorAlert(String callbackQueryId, TelegramClient telegramClient) {
        try {
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text("⚠️ Could not remove game. Please try again.")
                    .showAlert(true)
                    .build();
            telegramClient.execute(answer);
        } catch (TelegramApiException ex) {
            log.error("Failed to send error alert", ex);
        }
    }
}