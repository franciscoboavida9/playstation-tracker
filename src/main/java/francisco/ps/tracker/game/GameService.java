package francisco.ps.tracker.game;

import francisco.ps.tracker.infrastructure.sony.SonyStoreClient;
import francisco.ps.tracker.infrastructure.sony.dto.SearchResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {
    private final SonyStoreClient sonyStoreClient;

    public GameService(SonyStoreClient sonyStoreClient, ItemRepository itemRepository) {
        this.sonyStoreClient = sonyStoreClient;
    }

    /*
    public List<Item> search(String search) {
        SearchResponseDto searchResponseDto = sonyStoreClient.searchResponse(search);

    }

     */
}
