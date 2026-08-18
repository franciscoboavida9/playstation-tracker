package francisco.ps.tracker.game;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_game")
    private Long id;
    private String title;
    private String publisher;

    @OneToMany(mappedBy = "game")
    private List<Edition> editions = new ArrayList<>();

    protected Game() {}

    public Game(String title, String publisher) {
        this.title = title;
        this.publisher = publisher;
    }

    public void addEdition(Edition edition) {
        editions.add(edition);
        edition.setGame(this);
    }
}
