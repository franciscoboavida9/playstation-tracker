package francisco.ps.tracker.tracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Spring Data JPA repository for managing Chat-to-Item relationships.
 */
public interface TrackerRepository extends JpaRepository<Tracker, TrackerId> {
    @Query("SELECT t FROM Tracker t JOIN FETCH t.item WHERE t.chat.id = :chatId AND t.isActive = true")
    List<Tracker> findByChatIdAndIsActiveTrue(Long chatId);
}
