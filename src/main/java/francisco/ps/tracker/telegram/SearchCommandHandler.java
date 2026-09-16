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
public class SearchCommandHandler implements CommandHandler {

    private final ItemService itemService;
    private final MessageFormatter messageFormatter;
    private static final Logger log = LoggerFactory.getLogger(SearchCommandHandler.class);

    public SearchCommandHandler(ItemService itemService, MessageFormatter messageFormatter) {
        this.itemService = itemService;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public boolean supports(Update update) {
        return update.hasMessage() &&
                update.getMessage().hasText() &&
                (update.getMessage().getText().trim().equals("/search") ||
                        update.getMessage().getText().trim().matches("^/search\\s+\\S.+$"));
    }

    @Override
    public void handle(Update update, TelegramClient telegramClient) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText().trim();
        String query = messageText.replaceFirst("^/search", "").trim();

        if (query.isEmpty()) {
            log.warn("User {} issued /search with no query.", chatId);
            messageFormatter.sendTextMessage(chatId, "⚠️ Please provide a game name. Example:\n<code>/search Elden Ring</code>", telegramClient);
            return;
        }

        log.info("User {} searching for: '{}'", chatId, query);
        List<Item> items = itemService.search(query);

        if (items == null || items.isEmpty()) {
            log.info("No results found for query: '{}'", query);
            messageFormatter.sendTextMessage(chatId, "🕵️‍♂️ No games found for <b>" + query + "</b>. Check your spelling or try another name!", telegramClient);
            return;
        }

        int displayLimit = Math.min(items.size(), 5);
        log.info("Found {} results for query: '{}', sending to user {}", items.size(), query, chatId);

        messageFormatter.sendSearchCarousel(chatId, items.getFirst(), query, 0, displayLimit, telegramClient);
    }
}