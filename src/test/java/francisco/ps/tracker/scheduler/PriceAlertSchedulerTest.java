package francisco.ps.tracker.scheduler;

import francisco.ps.tracker.chat.Chat;
import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.game.PricePollingService;
import francisco.ps.tracker.telegram.MessageFormatter;
import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerRepository;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class PriceAlertSchedulerTest {

    private final TrackerRepository trackerRepository = mock(TrackerRepository.class);
    private final PricePollingService pricePollingService = mock(PricePollingService.class);
    private final MessageFormatter messageFormatter = mock(MessageFormatter.class);
    private final TelegramClient telegramClient = mock(TelegramClient.class);

    private final PriceAlertScheduler scheduler = new PriceAlertScheduler(
            trackerRepository, pricePollingService, messageFormatter, telegramClient);

    @Test
    void shouldPollPricesAndSendAlertsWhenDropsDetected() {
        Item trackedItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("59.99"), "url");
        when(trackerRepository.findDistinctActiveItems()).thenReturn(List.of(trackedItem));

        Tracker mockTracker = mock(Tracker.class);
        Chat mockChat = mock(Chat.class);

        when(mockTracker.getItem()).thenReturn(trackedItem);
        when(mockTracker.getChat()).thenReturn(mockChat);
        when(mockChat.getId()).thenReturn(12345L);

        when(pricePollingService.pollAndCheckPriceDrop(trackedItem)).thenReturn(List.of(mockTracker));

        scheduler.pollPricesAndNotifyUsers();

        verify(messageFormatter).sendPriceDropAlert(12345L, trackedItem, telegramClient);
    }

    @Test
    void shouldNotSendAlertsWhenNoPriceDropsDetected() {
        Item trackedItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("59.99"), "url");
        when(trackerRepository.findDistinctActiveItems()).thenReturn(List.of(trackedItem));

        when(pricePollingService.pollAndCheckPriceDrop(trackedItem)).thenReturn(Collections.emptyList());

        scheduler.pollPricesAndNotifyUsers();

        verifyNoInteractions(messageFormatter);
    }

    @Test
    void shouldContinuePollingIfOneItemThrowsException() {
        Item item1 = new Item("1L", "Game 1", new BigDecimal("10.00"), new BigDecimal("10.00"), "url");
        Item item2 = new Item("2L", "Game 2", new BigDecimal("20.00"), new BigDecimal("20.00"), "url");

        when(trackerRepository.findDistinctActiveItems()).thenReturn(List.of(item1, item2));

        when(pricePollingService.pollAndCheckPriceDrop(item1)).thenThrow(new RuntimeException("API Timeout"));
        when(pricePollingService.pollAndCheckPriceDrop(item2)).thenReturn(Collections.emptyList());

        scheduler.pollPricesAndNotifyUsers();

        verify(pricePollingService).pollAndCheckPriceDrop(item1);
        verify(pricePollingService).pollAndCheckPriceDrop(item2);
    }
}
