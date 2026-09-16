package francisco.ps.tracker.tracker;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite Primary Key for the Tracker entity.
 * Enforces database-level uniqueness to guarantee a user can only track a specific Item once.
 */
@Getter
@Embeddable
public class TrackerId implements Serializable {
    @Column(name = "id_chat")
    private Long chatId;

    @Column(name = "id_item")
    private String itemId;

    public TrackerId() {}

    public TrackerId(Long chatId, String itemId) {
        this.chatId = chatId;
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrackerId trackerId = (TrackerId) o;
        return Objects.equals(chatId, trackerId.chatId) && Objects.equals(itemId, trackerId.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, itemId);
    }
}
