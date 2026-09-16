package francisco.ps.tracker.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Interface defining the strategy contract for routing Telegram Updates.
 * Every concrete handler must define what it supports and how to handle it.
 */
public interface CommandHandler {

    boolean supports(Update update);

    void handle(Update update, TelegramClient telegramClient);
}