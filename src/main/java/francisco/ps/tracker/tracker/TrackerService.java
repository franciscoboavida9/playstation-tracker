package francisco.ps.tracker.tracker;

import francisco.ps.tracker.chat.Chat;
import francisco.ps.tracker.chat.ChatRepository;
import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.game.ItemRepository;
import francisco.ps.tracker.game.ItemService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TrackerService {
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
    public Tracker track(Long chatId, String chatType, String itemId) {
        // Check if tracker already exists to prevent spam
        TrackerId trackerId = new TrackerId(chatId, itemId);
        Tracker existingTracker = trackerRepository.findById(trackerId).orElse(null);
        if (existingTracker != null) {
            if (existingTracker.isActive()) {
                throw new IllegalStateException("You are already tracking this game!");
            } else {
                // The user is re-tracking a game they previously untracked!
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
            chat = new Chat(chatId, chatType, LocalDateTime.now());
            chat = chatRepository.save(chat);
        }

        // Item still not exists in the database
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            // Fetch item on the Store
            item = itemService.searchById(itemId);
            if (item == null) {
                throw new IllegalArgumentException("Item could not be found on PlayStation Store.");
            }

            // Save item to database
            item = itemRepository.save(item);
        }

        BigDecimal targetPrice = item.getCurrentPrice().subtract(new BigDecimal("0.01"));
        Tracker newTracker = new Tracker(chat, item, trackerId, targetPrice, true, LocalDateTime.now());
        return trackerRepository.save(newTracker);
    }
}
