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
public class WishlistCommandHandler implements CommandHandler {

    private final TrackerService trackerService;
    private final MessageFormatter messageFormatter;
    private static final Logger log = LoggerFactory.getLogger(WishlistCommandHandler.class);

    public WishlistCommandHandler(TrackerService trackerService, MessageFormatter messageFormatter) {
        this.trackerService = trackerService;
        this.messageFormatter = messageFormatter;
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
        log.info("User {} requested their wishlist", chatId);

        List<Tracker> trackers = trackerService.wishlist(chatId);

        if (trackers == null || trackers.isEmpty()) {
            log.info("User {} has an empty wishlist", chatId);
            messageFormatter.sendTextMessage(chatId, "📋 <b>Your wishlist is empty!</b>\n\nUse /search to find games and start tracking them.", telegramClient);
            return;
        }

        // Trigger the carousel starting at Index 0
        messageFormatter.sendWishlistCarousel(chatId, trackers.getFirst().getItem(), 0, trackers.size(), telegramClient);
    }
}