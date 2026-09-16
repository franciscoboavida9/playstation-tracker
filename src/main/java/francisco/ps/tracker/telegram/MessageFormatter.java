package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The centralized View Layer for the bot.
 * Responsible for constructing UI elements (Inline Keyboards, Carousels) and executing
 * native Telegram API calls (like EditMessageMedia) to ensure visual consistency across all commands.
 */
@Component
public class MessageFormatter {

    private static final Logger log = LoggerFactory.getLogger(MessageFormatter.class);
    private static final File FALLBACK_IMAGE = new File("docs/images/ps-store.png");

    private InputFile resolveInputPhoto(Item item) {
        return (item.getCoverImage() != null && !item.getCoverImage().isEmpty())
                ? new InputFile(item.getCoverImage())
                : new InputFile(FALLBACK_IMAGE);
    }

    private InputMediaPhoto resolveEditPhoto(Item item) {
        return (item.getCoverImage() != null && !item.getCoverImage().isEmpty())
                ? new InputMediaPhoto(item.getCoverImage())
                : new InputMediaPhoto(FALLBACK_IMAGE, "ps-store.png");
    }

    private String buildCarouselText(Item item, int currentIndex, int totalItems) {
        return "🎮 <b>" + item.getName() + "</b>\n\n" +
                "💰 Price: €" + item.getCurrentPrice() + "\n\n" +
                "<i>Game " + (currentIndex + 1) + " of " + totalItems + "</i>";
    }

    private InlineKeyboardMarkup buildSearchKeyboard(String itemId, String query, int currentIndex, int totalItems) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        if (currentIndex > 0) {
            row.add(InlineKeyboardButton.builder().text("⬅️").callbackData("srch:" + (currentIndex - 1) + ":" + query).build());
        }
        row.add(InlineKeyboardButton.builder().text("➕ Track").callbackData("track:" + itemId).build());
        if (currentIndex < totalItems - 1) {
            row.add(InlineKeyboardButton.builder().text("➡️").callbackData("srch:" + (currentIndex + 1) + ":" + query).build());
        }
        return InlineKeyboardMarkup.builder().keyboardRow(new InlineKeyboardRow(row)).build();
    }

    public void sendSearchCarousel(long chatId, Item item, String query, int currentIndex, int totalItems, TelegramClient telegramClient) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .photo(resolveInputPhoto(item))
                .caption(buildCarouselText(item, currentIndex, totalItems))
                .parseMode("HTML")
                .replyMarkup(buildSearchKeyboard(item.getId(), query, currentIndex, totalItems))
                .build();
        try {
            telegramClient.execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Failed to send search carousel", e);
        }
    }

    public void editSearchCarousel(long chatId, int messageId, Item item, String query, int currentIndex, int totalItems, TelegramClient telegramClient) {
        InputMediaPhoto mediaPhoto = resolveEditPhoto(item);
        mediaPhoto.setCaption(buildCarouselText(item, currentIndex, totalItems));
        mediaPhoto.setParseMode("HTML");

        EditMessageMedia edit = EditMessageMedia.builder()
                .chatId(chatId)
                .messageId(messageId)
                .media(mediaPhoto)
                .replyMarkup(buildSearchKeyboard(item.getId(), query, currentIndex, totalItems))
                .build();
        try {
            telegramClient.execute(edit);
        } catch (TelegramApiException e) {
            log.error("Failed to edit search carousel", e);
        }
    }

    private InlineKeyboardMarkup buildWishlistKeyboard(String itemId, int currentIndex, int totalItems) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        if (currentIndex > 0) {
            row.add(InlineKeyboardButton.builder().text("⬅️").callbackData("wishlist_page:" + (currentIndex - 1)).build());
        }

        row.add(InlineKeyboardButton.builder().text("❌ Untrack").callbackData("untrack:" + itemId + ":" + currentIndex).build());

        if (currentIndex < totalItems - 1) {
            row.add(InlineKeyboardButton.builder().text("➡️").callbackData("wishlist_page:" + (currentIndex + 1)).build());
        }

        return InlineKeyboardMarkup.builder().keyboardRow(new InlineKeyboardRow(row)).build();
    }

    public void sendWishlistCarousel(long chatId, Item item, int currentIndex, int totalItems, TelegramClient telegramClient) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .photo(resolveInputPhoto(item))
                .caption(buildCarouselText(item, currentIndex, totalItems))
                .parseMode("HTML")
                .replyMarkup(buildWishlistKeyboard(item.getId(), currentIndex, totalItems))
                .build();
        try {
            telegramClient.execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Failed to send wishlist carousel", e);
        }
    }

    public void editWishlistCarousel(long chatId, int messageId, Item item, int currentIndex, int totalItems, TelegramClient telegramClient) {
        InputMediaPhoto mediaPhoto = resolveEditPhoto(item);
        mediaPhoto.setCaption(buildCarouselText(item, currentIndex, totalItems));
        mediaPhoto.setParseMode("HTML");

        EditMessageMedia edit = EditMessageMedia.builder()
                .chatId(chatId)
                .messageId(messageId)
                .media(mediaPhoto)
                .replyMarkup(buildWishlistKeyboard(item.getId(), currentIndex, totalItems))
                .build();
        try {
            telegramClient.execute(edit);
        } catch (TelegramApiException e) {
            log.error("Failed to edit wishlist carousel", e);
        }
    }

    public void sendTextMessage(long chatId, String text, TelegramClient telegramClient) {
        SendMessage message = SendMessage.builder().chatId(chatId).text(text).parseMode("HTML").build();
        try { telegramClient.execute(message); } catch (TelegramApiException e) { log.error("Error sending text", e); }
    }

    public void sendItemMessage(long chatId, Item item, String buttonText, String callbackData, TelegramClient telegramClient) {
        StringBuilder text = new StringBuilder();
        text.append("🎮 <b>").append(item.getName()).append("</b>\n\n");
        text.append("💰 Price: €").append(item.getCurrentPrice());

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text(buttonText).callbackData(callbackData).build()))
                .build();

        try {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId)
                    .photo(resolveInputPhoto(item))
                    .caption(text.toString())
                    .parseMode("HTML")
                    .replyMarkup(keyboard)
                    .build();
            telegramClient.execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Failed to send item message", e);
        }
    }

    public void editToEmptyWishlist(long chatId, int messageId, TelegramClient telegramClient) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .build();

        SendMessage sendMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text("📋 <b>Your wishlist is now empty!</b>\n\nUse /search to find games and start tracking them.")
                        .parseMode("HTML")
                        .build();

        try {
            telegramClient.execute(deleteMessage);
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to edit message to empty wishlist state", e);
        }
    }
}