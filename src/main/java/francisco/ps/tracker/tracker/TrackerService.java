package francisco.ps.tracker.tracker;

import francisco.ps.tracker.chat.Chat;
import francisco.ps.tracker.chat.ChatRepository;
import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.game.ItemRepository;
import francisco.ps.tracker.game.ItemService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TrackerService {
    private final ChatRepository chatRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    public TrackerService(ChatRepository chatRepository, ItemRepository itemRepository, ItemService itemService) {
        this.chatRepository = chatRepository;
        this.itemRepository = itemRepository;
        this.itemService = itemService;
    }

    public Tracker track(Long chatId, String chatType, String itemId) {
        // Chat does not exist in the database
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            // Save chat to the database
            chat = new Chat(chatId, chatType, LocalDateTime.now());
            chatRepository.save(chat);
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


        return null;
    }
}
