package francisco.ps.tracker.tracker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackerRepository extends JpaRepository<Tracker, Long> {
}
