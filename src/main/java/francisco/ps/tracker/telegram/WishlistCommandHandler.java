package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerService;
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
public class WishlistCommandHandler implements CommandHandler {

    private final TrackerService trackerService;

    private static final Logger log = LoggerFactory.getLogger(WishlistCommandHandler.class);

    public WishlistCommandHandler(TrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @Override
    public boolean supports(Update update) {
        return update.hasMessage() &&
                update.getMessage().hasText() &&
                update.getMessage().getText().trim().equals("/wishlist");
    }

    @Override
    public void handle(Update update, TelegramClient telegramClient) {
        long chatId = update.getMessage().getChatId();

        List<Tracker> trackers = trackerService.wishlist(chatId);
        if (trackers == null || trackers.isEmpty()) {
            sendTextMessage(chatId, "📋 <b>Your wishlist is empty!</b>\n\nUse /search to find games and start tracking them.", telegramClient);
            return;
        }

        sendTextMessage(chatId, "📋 <b>Here are your tracked games:</b>", telegramClient);

        for (Tracker tracker : trackers) {
            Item item = tracker.getItem();
            sendFormattedItemMessage(chatId, item, telegramClient);
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
            log.error("Failed to send simple text message", e);
        }
    }

    private void sendFormattedItemMessage(long chatId, Item item, TelegramClient telegramClient) {
        StringBuilder text = new StringBuilder();
        text.append("🎮 <b>").append(item.getName()).append("</b>\n\n");

        if (item.getCurrentPrice().compareTo(item.getBasePrice()) < 0) {
            text.append("🔥 <b>ON SALE:</b> €").append(item.getCurrentPrice()).append("\n");
            text.append("<s>Regular: €").append(item.getBasePrice()).append("</s>");
        } else {
            text.append("💰 Current Price: €").append(item.getCurrentPrice());
        }

        InlineKeyboardButton untrackButton = InlineKeyboardButton.builder()
                .text("❌ Stop Tracking")
                .callbackData("untrack:" + item.getId())
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(untrackButton))
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
            log.error("Failed to send message", e);
        }
    }
}