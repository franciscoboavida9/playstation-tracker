package francisco.ps.tracker.tracker;

import francisco.ps.tracker.game.Item;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for managing Chat-to-Item relationships.
 */
public interface TrackerRepository extends JpaRepository<@NotNull Tracker, @NotNull TrackerId> {
    @Query("SELECT t FROM Tracker t JOIN FETCH t.item WHERE t.chat.id = :chatId AND t.isActive = true")
    List<Tracker> findByChatIdAndIsActiveTrue(Long chatId);

    @Query("SELECT DISTINCT t.item FROM Tracker t WHERE t.isActive = true")
    List<Item> findDistinctActiveItems();

    @Query("SELECT t FROM Tracker t JOIN FETCH t.chat WHERE t.item.id = :itemId AND t.isActive = true AND t.targetPrice >= :currentPrice")
    List<Tracker> findActiveTrackersReadyToNotify(@Param("itemId") String itemId, @Param("currentPrice") BigDecimal currentPrice);
}
