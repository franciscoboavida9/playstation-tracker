package francisco.ps.tracker.tracker;

import francisco.ps.tracker.chat.Chat;
import francisco.ps.tracker.game.Item;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
public class Tracker {
    @EmbeddedId
    private TrackerId id;
    @Column(name = "target_price", precision = 5, scale = 2)
    private BigDecimal targetPrice;
    @Column(name = "is_active")
    private boolean isActive;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatId")
    @JoinColumn(name = "id_chat")
    private Chat chat;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("editionId")
    @JoinColumn(name = "id_item")
    private Item item;

    protected Tracker() {}

    public Tracker(Chat chat, Item item, TrackerId id, BigDecimal targetPrice, boolean isActive, LocalDateTime createdAt) {
        this.chat = chat;
        this.item = item;
        this.id = id;
        this.targetPrice = targetPrice;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
}
