package francisco.ps.tracker.tracker;

import francisco.ps.tracker.chat.Chat;
import francisco.ps.tracker.chat.ChatRepository;
import francisco.ps.tracker.game.Edition;
import francisco.ps.tracker.game.EditionRepository;
import francisco.ps.tracker.game.Game;
import francisco.ps.tracker.game.GameRepository;
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
    private static final String URL_ER = "https://store.playstation.com/pt-pt/product/EP0700-PPSA04609_00-ELDENRING0000000";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TrackerRepository trackerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EditionRepository editionRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should successfully persist and retrieve a Tracker with composite key")
    void shouldSaveAndRetrieveTrackerSuccessfully() {
        Game eldenRing = gameRepository.save(new Game("Elden Ring"));
        Edition edition = editionRepository.save(new Edition(
                "Standard Edition",
                URL_ER,
                new BigDecimal("59.99"),
                new BigDecimal("59.99"),
                eldenRing
        ));

        Chat chat = chatRepository.save(new Chat(123456789L, "private", LocalDateTime.now()));

        TrackerId trackerId = new TrackerId(chat.getId(), edition.getId());
        Tracker tracker = trackerRepository.save(new Tracker(
                chat,
                edition,
                trackerId,
                new BigDecimal("59.99"),
                true,
                LocalDateTime.now()
        ));

        entityManager.flush();
        entityManager.clear();

        Optional<Tracker> findTracker = trackerRepository.findById(tracker.getId());
        assertThat(findTracker).isPresent();
        assertThat(findTracker.get().getTargetPrice().equals(edition.getCurrentPrice()));
        assertThat(findTracker.get().isActive());
        assertThat(findTracker.get().getEdition().getEditionName().equals(edition.getEditionName()));
    }
}
