package francisco.ps.tracker.telegram;

import francisco.ps.tracker.tracker.TrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class TrackCommandHandler implements CommandHandler {

    private final TrackerService trackerService;
    private static final Logger log = LoggerFactory.getLogger(TrackCommandHandler.class);

    public TrackCommandHandler(TrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @Override
    public boolean supports(Update update) {
        return update.hasCallbackQuery() &&
                update.getCallbackQuery().getData().startsWith("track:");
    }

    @Override
    public void handle(Update update, TelegramClient telegramClient) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String chatType = update.getCallbackQuery().getMessage().getChat().getType();
        String callbackQueryId = update.getCallbackQuery().getId();
        String itemId = callbackData.substring(6);

        // INFO: Announce the attempt
        log.info("User {} attempting to track item: {}", chatId, itemId);

        try {
            trackerService.track(chatId, chatType, itemId);

            // INFO: Success
            log.info("Successfully tracked item {} for user {}", itemId, chatId);
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text("✅ Game added to your watchlist!")
                    .showAlert(false)
                    .build();
            telegramClient.execute(answer);

            SendMessage confirmation = SendMessage.builder()
                    .chatId(chatId)
                    .text("🎮 Successfully added to your tracked games! I will notify you when the price drops.")
                    .build();
            telegramClient.execute(confirmation);

        } catch (IllegalStateException e) {
            // WARN: The user tried to track a game they already have
            log.warn("User {} tried to track item {}, but it is already active.", chatId, itemId);
            sendAlert(callbackQueryId, "⚠️ You are already tracking this game!", telegramClient);
        } catch (NumberFormatException e) {
            log.error("Invalid item ID format in callback data: {}", callbackData);
        } catch (TelegramApiException e) {
            log.error("Failed to execute Telegram API call", e);
        } catch (Exception e) {
            log.error("Backend error while tracking item", e);
            sendAlert(callbackQueryId, "⚠️ Could not track game. Please try again later.", telegramClient);
        }
    }

    private void sendAlert(String callbackQueryId, String text, TelegramClient telegramClient) {
        try {
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text(text)
                    .showAlert(true)
                    .build();
            telegramClient.execute(answer);
        } catch (TelegramApiException ex) {
            log.error("Failed to send alert popup", ex);
        }
    }
}