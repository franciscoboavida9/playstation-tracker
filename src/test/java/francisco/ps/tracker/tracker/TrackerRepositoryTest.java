package francisco.ps.tracker.tracker;

import francisco.ps.tracker.chat.Chat;
import francisco.ps.tracker.chat.ChatRepository;
import francisco.ps.tracker.game.Item;
import francisco.ps.tracker.game.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrackerRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TrackerRepository trackerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should successfully persist and retrieve a Tracker with composite key")
    void shouldSaveAndRetrieveTrackerSuccessfully() {
        Item item = itemRepository.save(new Item(
                "1", "Standard Edition",
                new BigDecimal("59.99"),
                new BigDecimal("59.99")
        ));

        Chat chat = chatRepository.save(new Chat(123456789L, "private", LocalDateTime.now()));

        TrackerId trackerId = new TrackerId(chat.getId(), item.getId());
        Tracker tracker = trackerRepository.save(new Tracker(
                chat,
                item,
                trackerId,
                new BigDecimal("59.99"),
                true,
                LocalDateTime.now()
        ));

        entityManager.flush();
        entityManager.clear();

        Optional<Tracker> findTracker = trackerRepository.findById(tracker.getId());
        assertThat(findTracker).isPresent();
        assertThat(findTracker.get().getTargetPrice()).isEqualByComparingTo(item.getCurrentPrice());
        assertThat(findTracker.get().isActive()).isTrue();
        assertThat(findTracker.get().getItem().getName()).isEqualTo(item.getName());
    }
}
