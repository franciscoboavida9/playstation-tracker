package francisco.ps.tracker.game;

import francisco.ps.tracker.tracker.Tracker;
import francisco.ps.tracker.tracker.TrackerRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PricePollingServiceTest {

    private final ItemService itemService = mock(ItemService.class);
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final TrackerRepository trackerRepository = mock(TrackerRepository.class);

    private final PricePollingService pricePollingService = new PricePollingService(itemService, itemRepository, trackerRepository);

    @Test
    void shouldReturnTrackersAndUpdateDbWhenPriceDrops() {
        Item oldItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("59.99"), "url");
        Item freshItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("39.99"), "url");

        Tracker mockTracker = mock(Tracker.class);

        when(itemService.searchById("1L")).thenReturn(freshItem);
        when(trackerRepository.findActiveTrackersReadyToNotify(eq("1L"), any(BigDecimal.class)))
                .thenReturn(List.of(mockTracker));

        List<Tracker> result = pricePollingService.pollAndCheckPriceDrop(oldItem);

        assertThat(result).hasSize(1);

        assertThat(oldItem.getCurrentPrice()).isEqualTo(new BigDecimal("39.99"));
        verify(itemRepository).save(oldItem);

        verify(mockTracker).setTargetPrice(new BigDecimal("39.98"));
        verify(trackerRepository).saveAll(List.of(mockTracker));
    }

    @Test
    void shouldDoNothingWhenPriceRemainsTheSame() {
        Item oldItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("59.99"), "url");
        Item freshItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("59.99"), "url");

        when(itemService.searchById("1L")).thenReturn(freshItem);

        List<Tracker> result = pricePollingService.pollAndCheckPriceDrop(oldItem);

        assertThat(result).isEmpty();

        verify(itemRepository, never()).save(any());
        verify(trackerRepository, never()).saveAll(any());
    }

    @Test
    void shouldHandleNullResponseFromSonyGracefully() {
        Item oldItem = new Item("1L", "Elden Ring", new BigDecimal("59.99"), new BigDecimal("59.99"), "url");

        when(itemService.searchById("1L")).thenReturn(null);

        List<Tracker> result = pricePollingService.pollAndCheckPriceDrop(oldItem);

        assertThat(result).isEmpty();
        verifyNoInteractions(itemRepository);
        verifyNoInteractions(trackerRepository);
    }
}