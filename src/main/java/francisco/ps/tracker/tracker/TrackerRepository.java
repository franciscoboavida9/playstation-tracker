package francisco.ps.tracker.tracker;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackerRepository extends JpaRepository<Tracker, TrackerId> {
    List<Tracker> findByChatIdAndIsActiveTrue(Long chatId);
}
