package francisco.ps.tracker.chat;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for persisting and retrieving Chat entities.
 */
public interface ChatRepository extends JpaRepository<Chat, Long> {
}
