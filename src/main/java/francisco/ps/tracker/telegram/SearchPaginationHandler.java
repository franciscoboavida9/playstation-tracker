package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.ItemService;
import francisco.ps.tracker.game.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class SearchPaginationHandler implements CommandHandler {

    private final ItemService itemService;
    private final MessageFormatter messageFormatter;
    private static final Logger log = LoggerFactory.getLogger(SearchPaginationHandler.class);

    public SearchPaginationHandler(ItemService itemService, MessageFormatter messageFormatter) {
        this.itemService = itemService;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public boolean supports(Update update) {
        return update.hasCallbackQuery() &&
                update.getCallbackQuery().getData() != null &&
                update.getCallbackQuery().getData().startsWith("srch:");
    }

    @Override
    public void handle(Update update, TelegramClient telegramClient) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Decode the button data: "srch:1:elden ring"
        String[] parts = callbackData.split(":", 3);
        int targetIndex = Integer.parseInt(parts[1]);
        String query = parts[2];

        // Won't hit the API again because of Caching
        List<Item> items = itemService.search(query);
        int displayLimit = Math.min(items.size(), 5);

        if (items.isEmpty() || targetIndex < 0 || targetIndex >= displayLimit) {
            log.warn("Invalid search carousel state for chat {}", chatId);
            return;
        }

        messageFormatter.editSearchCarousel(chatId, messageId, items.get(targetIndex), query, targetIndex, displayLimit, telegramClient);
    }
}
