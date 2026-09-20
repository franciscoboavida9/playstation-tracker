package francisco.ps.tracker.scheduler;

import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.game.PricePollingService;
import francisco.ps.tracker.telegram.MessageFormatter;
import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class PriceAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceAlertScheduler.class);

    private final TrackerRepository trackerRepository;
    private final PricePollingService pricePollingService;
    private final MessageFormatter messageFormatter;
    private final TelegramClient telegramClient;

    public PriceAlertScheduler(TrackerRepository trackerRepository,
                               PricePollingService pricePollingService,
                               MessageFormatter messageFormatter,
                               TelegramClient telegramClient) {
        this.trackerRepository = trackerRepository;
        this.pricePollingService = pricePollingService;
        this.messageFormatter = messageFormatter;
        this.telegramClient = telegramClient;
    }

    /**
     * Executes the background price check.
     * By default, it runs every day at 12:00 PM (Noon).
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void pollPricesAndNotifyUsers() {
        log.info("Starting scheduled price polling job...");

        List<Item> activeItems = trackerRepository.findDistinctActiveItems();
        log.info("Found {} unique active games to check.", activeItems.size());

        for (Item item : activeItems) {
            try {
                List<Tracker> affectedTrackers = pricePollingService.pollAndCheckPriceDrop(item);

                for (Tracker tracker : affectedTrackers) {
                    messageFormatter.sendPriceDropAlert(tracker.getChat().getId(), tracker.getItem(), telegramClient);
                    log.info("Price drop alert sent to chat {} for game {}", tracker.getChat().getId(), item.getName());
                }

                // Sleep for 1 second between games so Sony does not ban the Raspberry Pi IP
                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("Error during price polling for item {}", item.getId(), e);
            }
        }

        log.info("Scheduled price polling job completed.");
    }
}