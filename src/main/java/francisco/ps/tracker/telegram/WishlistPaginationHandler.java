package francisco.ps.tracker.telegram;

import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class WishlistPaginationHandler implements CommandHandler {

    private final TrackerService trackerService;
    private final MessageFormatter messageFormatter;
    private static final Logger log = LoggerFactory.getLogger(WishlistPaginationHandler.class);

    public WishlistPaginationHandler(TrackerService trackerService, MessageFormatter messageFormatter) {
        this.trackerService = trackerService;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public boolean supports(Update update) {
        return update.hasCallbackQuery() &&
                update.getCallbackQuery().getData() != null &&
                update.getCallbackQuery().getData().startsWith("wishlist_page:");
    }

    @Override
    public void handle(Update update, TelegramClient telegramClient) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Extract the target page index
        int targetIndex = Integer.parseInt(callbackData.split(":")[1]);

        List<Tracker> trackers = trackerService.wishlist(chatId);

        if (trackers == null || trackers.isEmpty() || targetIndex < 0 || targetIndex >= trackers.size()) {
            log.warn("Invalid carousel state for chat {}", chatId);
            return;
        }

        messageFormatter.editWishlistCarousel(
                chatId,
                messageId,
                trackers.get(targetIndex).getItem(),
                targetIndex,
                trackers.size(),
                telegramClient
        );
    }
}