package francisco.ps.tracker.game;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for persisting and retrieving Game Items.
 */
public interface ItemRepository extends JpaRepository<Item, String> {
}
