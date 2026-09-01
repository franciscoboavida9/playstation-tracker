package francisco.ps.tracker.game;

import francisco.ps.tracker.tracker.Tracker;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Item {
    @Id
    @Column(name = "id_item")
    private String id;
    @Column(name = "item_name")
    private String name;
    @Column(name = "current_price", precision = 5, scale = 2)
    private BigDecimal currentPrice;
    @Column(name = "base_price", precision = 5, scale = 2)
    private BigDecimal basePrice;

    @OneToMany(mappedBy = "item")
    private List<Tracker> trackers = new ArrayList<>();

    protected Item() {}

    public Item(String id, String name, BigDecimal basePrice, BigDecimal currentPrice) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.currentPrice = currentPrice;
    }

    public void addTracker(Tracker tracker) {
        trackers.add(tracker);
        tracker.setItem(this);
    }

}
