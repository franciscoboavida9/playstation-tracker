package francisco.ps.tracker.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class CommandDispatcher {

    private final List<CommandHandler> handlers;

    public CommandDispatcher(List<CommandHandler> handlers) {
        this.handlers = handlers;
    }

    public void dispatch(Update update, TelegramClient telegramClient) {
        for (CommandHandler handler : handlers) {
            if (handler.supports(update)) {
                handler.handle(update, telegramClient);
                return;
            }
        }
    }

}