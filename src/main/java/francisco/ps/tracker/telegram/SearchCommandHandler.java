package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.ItemService;
import francisco.ps.tracker.game.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class SearchCommandHandler implements CommandHandler {

    private final ItemService itemService;

    private static final Logger log = LoggerFactory.getLogger(SearchCommandHandler.class);

    public SearchCommandHandler(ItemService itemService) {
        this.itemService = itemService;
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
            sendTextMessage(chatId, "⚠️ Please provide a game name. Example:\n<code>/search Elden Ring</code>", telegramClient);
            return;
        }

        List<Item> items = itemService.search(query);
        if (items == null || items.isEmpty()) {
            sendTextMessage(chatId, "🕵️‍♂️ No games found for <b>" + query + "</b>. Check your spelling or try another name!", telegramClient);
            return;
        }

        int displayLimit = Math.min(items.size(), 3);
        for (int i = 0; i < displayLimit; i++) {
            sendFormattedItemMessage(chatId, items.get(i), telegramClient);
        }

        if (items.size() > 3) {
            sendTextMessage(chatId, "<i>Showing top 3 results. Be more specific if you don't see your game!</i>", telegramClient);
        }

    }

    private void sendTextMessage(long chatId, String text, TelegramClient telegramClient) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send search message", e);
        }
    }

    private void sendFormattedItemMessage(long chatId, Item item, TelegramClient telegramClient) {
        StringBuilder text = new StringBuilder();
        text.append("🎮 <b>").append(item.getName()).append("</b>\n\n");

        if (item.getCurrentPrice().compareTo(item.getBasePrice()) < 0) {
            text.append("🔥 <b>ON SALE:</b> €").append(item.getCurrentPrice()).append("\n");
            text.append("<s>Regular Price: €").append(item.getBasePrice()).append("</s>");
        } else {
            text.append("💰 Price: €").append(item.getCurrentPrice());
        }

        InlineKeyboardButton trackButton = InlineKeyboardButton.builder()
                .text("➕ Track Game")
                .callbackData("track:" + item.getId())
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow(trackButton);
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text.toString())
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send search message", e);
        }
    }
}
