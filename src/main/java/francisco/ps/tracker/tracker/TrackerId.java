package francisco.ps.tracker.tracker;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Embeddable
public class TrackerId implements Serializable {
    @Column(name = "id_chat")
    private Long chatId;

    @Column(name = "id_edition")
    private Long editionId;

    public TrackerId() {}

    public TrackerId(Long chatId, Long editionId) {
        this.chatId = chatId;
        this.editionId = editionId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrackerId trackerId = (TrackerId) o;
        return Objects.equals(chatId, trackerId.chatId) && Objects.equals(editionId, trackerId.editionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, editionId);
    }
}
