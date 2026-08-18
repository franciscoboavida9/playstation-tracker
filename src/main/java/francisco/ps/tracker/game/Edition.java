package francisco.ps.tracker.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Entity
public class Edition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_edition")
    private Long id;
    @Column(name = "edition_name")
    private String editionName;
    @Column(name = "store_url")
    private String storeUrl;
    @Column(name = "current_price", precision = 5, scale = 2)
    private BigDecimal currentPrice;
    @Column(name = "base_price", precision = 5, scale = 2)
    private BigDecimal basePrice;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_game")
    private Game game;

    protected Edition() {}

    public Edition(String editionName, String storeUrl, BigDecimal currentPrice, BigDecimal basePrice, Game game) {
        this.editionName = editionName;
        this.storeUrl = storeUrl;
        this.currentPrice = currentPrice;
        this.basePrice = basePrice;
        this.game = game;
    }

}
