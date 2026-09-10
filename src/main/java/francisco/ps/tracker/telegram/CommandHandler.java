package francisco.ps.tracker.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface CommandHandler {

    boolean supports(Update update);

    void handle(Update update, TelegramClient telegramClient);
}