package francisco.ps.tracker.game;

import francisco.ps.tracker.infrastructure.sony.SonyStoreClient;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
    private final SonyStoreClient sonyStoreClient;

    public ItemService(SonyStoreClient sonyStoreClient, ItemRepository itemRepository) {
        this.sonyStoreClient = sonyStoreClient;
    }

    /*
    public List<Item> search(String search) {
        SearchResponseDto searchResponseDto = sonyStoreClient.searchResponse(search);

    }

     */
}
