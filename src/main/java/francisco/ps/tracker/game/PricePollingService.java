package francisco.ps.tracker.game;


import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class PricePollingService {

    private static final Logger log = LoggerFactory.getLogger(PricePollingService.class);

    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final TrackerRepository trackerRepository;

    public PricePollingService(ItemService itemService, ItemRepository itemRepository, TrackerRepository trackerRepository) {
        this.itemService = itemService;
        this.itemRepository = itemRepository;
        this.trackerRepository = trackerRepository;
    }

    /**
     * Checks Sony for an updated price.
     * If the price dropped, updates the DB and returns the trackers that should be notified.
     */
    @Transactional
    public List<Tracker> pollAndCheckPriceDrop(Item item) {
        Item updatedItem = itemService.searchById(item.getId());
        if (updatedItem == null || updatedItem.getCurrentPrice() == null) {
            log.warn("Could not retrieve price from Sony for item {}", item.getId());
            return Collections.emptyList();
        }

        BigDecimal oldPrice = item.getCurrentPrice();
        BigDecimal newPrice = updatedItem.getCurrentPrice();

        if (newPrice.compareTo(oldPrice) < 0) {
            log.info("Price drop detected for '{}'! Old: €{}, New: €{}", item.getName(), oldPrice, newPrice);

            // Update the item in the database
            item.setCurrentPrice(newPrice);
            if (updatedItem.getBasePrice() != null) {
                item.setBasePrice(updatedItem.getBasePrice());
            }
            itemRepository.save(item);

            // Find all users whose target threshold has been met
            List<Tracker> readyToNotify = trackerRepository.findActiveTrackersReadyToNotify(item.getId(), newPrice);

            // Update target prices to prevent spamming users every single day
            for (Tracker tracker : readyToNotify) {
                tracker.setTargetPrice(newPrice.subtract(new BigDecimal("0.01")));
            }
            trackerRepository.saveAll(readyToNotify);

            return readyToNotify;
        }

        return Collections.emptyList();
    }

}
