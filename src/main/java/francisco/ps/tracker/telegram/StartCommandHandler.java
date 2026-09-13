package francisco.ps.tracker.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class StartCommandHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(StartCommandHandler.class);

    @Override
    public boolean supports(Update update) {
        return update.hasMessage() &&
                update.getMessage().hasText() &&
                update.getMessage().getText().equals("/start");
    }

    @Override
    public void handle(Update update, TelegramClient telegramClient) {
        long chatId = update.getMessage().getChatId();
        log.info("User {} triggered /start command", chatId);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("👋 Welcome to PS Tracker!\n\nI track PlayStation Store discounts and alert you the moment prices drop.\n\nTry sending /search to find a game!")
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send start message", e);
        }
    }
}