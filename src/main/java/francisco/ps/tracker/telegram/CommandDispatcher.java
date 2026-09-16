package francisco.ps.tracker.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

/**
 * Central router using the Strategy Pattern.
 * Intercepts all incoming Telegram Updates and dynamically dispatches them to the
 * first matching CommandHandler, keeping the bot stateless and free of if/else chains.
 */
@Component
public class CommandDispatcher {
    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final List<CommandHandler> handlers;

    public CommandDispatcher(List<CommandHandler> handlers) {
        this.handlers = handlers;
    }

    public void dispatch(Update update, TelegramClient telegramClient) {
        for (CommandHandler handler : handlers) {
            if (handler.supports(update)) {
                // INFO: Log which handler is taking the request
                log.info("Routing update {} to {}", update.getUpdateId(), handler.getClass().getSimpleName());
                handler.handle(update, telegramClient);
                return;
            }
        }
        // WARN: A user sent weird input that can't be handled (e.g. emoji)
        log.warn("No handler found to process update: {}", update.getUpdateId());
    }

}