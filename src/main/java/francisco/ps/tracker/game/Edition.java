package francisco.ps.tracker.game;

import francisco.ps.tracker.tracker.Tracker;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Edition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_edition")
    private String id;
    @Column(name = "edition_name")
    private String editionName;
    @Column(name = "current_price", precision = 5, scale = 2)
    private BigDecimal currentPrice;
    @Column(name = "base_price", precision = 5, scale = 2)
    private BigDecimal basePrice;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_game")
    private Game game;

    @OneToMany(mappedBy = "edition")
    private List<Tracker> trackers = new ArrayList<>();

    protected Edition() {}

    public Edition(String editionName, BigDecimal currentPrice, BigDecimal basePrice, Game game) {
        this.editionName = editionName;
        this.currentPrice = currentPrice;
        this.basePrice = basePrice;
        this.game = game;
    }

    public void addTracker(Tracker tracker) {
        trackers.add(tracker);
        tracker.setEdition(this);
    }

}
