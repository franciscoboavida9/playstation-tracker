package francisco.ps.tracker.tracker;

import francisco.ps.tracker.chat.Chat;
import francisco.ps.tracker.chat.ChatRepository;
import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.game.ItemRepository;
import francisco.ps.tracker.game.ItemService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Core business logic for managing user wishlists.
 * Handles tracking validations, wishlist retrieval, and executing soft-deletes when untracking.
 */
@Service
public class TrackerService {

    private static final Logger log = LoggerFactory.getLogger(TrackerService.class);

    private final TrackerRepository trackerRepository;
    private final ChatRepository chatRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    public TrackerService(TrackerRepository trackerRepository, ChatRepository chatRepository,
                          ItemRepository itemRepository, ItemService itemService) {
        this.trackerRepository = trackerRepository;
        this.chatRepository = chatRepository;
        this.itemRepository = itemRepository;
        this.itemService = itemService;
    }

    /**
     * Starts tracking an item for a chat, creating or reactivating the tracker as needed.
     * @param chatId   The chat ID.
     * @param chatType The type of chat.
     * @param itemId   The store item ID.
     * @return The saved Tracker.
     */
    @Transactional
    public Tracker track(Long chatId, String chatType, String itemId) {
        log.debug("Processing track request for User: {} | Item: {}", chatId, itemId);

        // Check if tracker already exists to prevent spam
        TrackerId trackerId = new TrackerId(chatId, itemId);
        Tracker existingTracker = trackerRepository.findById(trackerId).orElse(null);
        if (existingTracker != null) {
            if (existingTracker.isActive()) {
                throw new IllegalStateException("You are already tracking this game!");
            } else {
                log.info("Reactivating previously untracked game {} for user {}", itemId, chatId);
                // The user is re-tracking a game they previously untracked
                existingTracker.setActive(true);
                existingTracker.setTargetPrice(
                        existingTracker.getItem().getCurrentPrice().subtract(new BigDecimal("0.01")));
                return trackerRepository.save(existingTracker);
            }
        }

        // Chat does not exist in the database
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            // Save chat to the database
            log.debug("Creating new Chat record for user {}", chatId);
            chat = new Chat(chatId, chatType, LocalDateTime.now());
            chat = chatRepository.save(chat);
        }

        // Item still not exists in the database
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            log.debug("Item {} not in DB. Fetching from Sony Store...", itemId);
            // Fetch item on the Store
            item = itemService.searchById(itemId);
            if (item == null) {
                log.error("Failed to find Item {} on Sony Store!", itemId);
                throw new IllegalArgumentException("Item could not be found on PlayStation Store.");
            }

            // Save item to database
            item = itemRepository.save(item);
        }

        BigDecimal targetPrice = item.getCurrentPrice().subtract(new BigDecimal("0.01"));
        Tracker newTracker = new Tracker(chat, item, trackerId, targetPrice, true, LocalDateTime.now());

        log.info("Successfully created new tracker for User: {} | Item: {}", chatId, itemId);
        return trackerRepository.save(newTracker);
    }

    /**
     * Stops tracking an item for a chat.
     * @param chatId The chat ID.
     * @param itemId The store item ID.
     */
    public void untrack(Long chatId, String itemId) {
        log.debug("Processing untrack request for User: {} | Item: {}", chatId, itemId);

        TrackerId trackerId = new TrackerId(chatId, itemId);
        Tracker tracker = trackerRepository.findById(trackerId)
                .orElseThrow(() -> new IllegalArgumentException("You are not tracking this game."));

        if (!tracker.isActive()) {
            throw new IllegalStateException("You are already not tracking this game.");
        }

        tracker.setActive(false);
        trackerRepository.save(tracker);
        log.info("Successfully untracked game {} for user {}", itemId, chatId);
    }

    /**
     * Fetches all the games currently being tracked by a specific user
     * @param chatId The chat ID.
     * @return The list of games tracked by the specific user.
     */
    public List<Tracker> wishlist(Long chatId) {
        log.debug("Fetching wishlist for user {}", chatId);
        return trackerRepository.findByChatIdAndIsActiveTrue(chatId);
    }
}
