package francisco.ps.tracker.game;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Game {
    @Id
    @Column(name = "id_game")
    private String id;
    private String title;

    @OneToMany(mappedBy = "game")
    private List<Edition> editions = new ArrayList<>();

    protected Game() {}

    public Game(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public void addEdition(Edition edition) {
        editions.add(edition);
        edition.setGame(this);
    }
}
