package francisco.ps.tracker.telegram;

import francisco.ps.tracker.game.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class MessageFormatter {

    private static final Logger log = LoggerFactory.getLogger(MessageFormatter.class);

    /**
     * Formats and sends an Item message to the user, either as a Photo or standard text.
     *
     * @param chatId The chat to send the message to.
     * @param item The game item to display.
     * @param buttonText The text to display on the inline button.
     * @param callbackData The data sent when the button is clicked.
     * @param telegramClient The client used to send the message.
     */
    public void sendItemMessage(long chatId, Item item, String buttonText, String callbackData, TelegramClient telegramClient) {
        StringBuilder text = new StringBuilder();
        text.append("🎮 <b>").append(item.getName()).append("</b>\n\n");

        if (item.getCurrentPrice().compareTo(item.getBasePrice()) < 0) {
            text.append("🔥 <b>ON SALE:</b> €").append(item.getCurrentPrice()).append("\n");
            text.append("<s>Regular Price: €").append(item.getBasePrice()).append("</s>");
        } else {
            text.append("💰 Price: €").append(item.getCurrentPrice());
        }

        InlineKeyboardButton actionButton = InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(callbackData)
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(actionButton))
                .build();

        try {
            if (item.getCoverImage() != null && !item.getCoverImage().isEmpty()) {
                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(item.getCoverImage()))
                        .caption(text.toString())
                        .parseMode("HTML")
                        .replyMarkup(keyboard)
                        .build();
                telegramClient.execute(sendPhoto);
            } else {
                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text(text.toString())
                        .parseMode("HTML")
                        .replyMarkup(keyboard)
                        .build();
                telegramClient.execute(message);
            }
        } catch (TelegramApiException e) {
            log.error("Failed to send formatted item message for item {}", item.getId(), e);
        }
    }

    /**
     * A simple helper to send standard text messages.
     */
    public void sendTextMessage(long chatId, String text, TelegramClient telegramClient) {
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
}
