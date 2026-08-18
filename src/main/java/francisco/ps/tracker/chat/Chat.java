package francisco.ps.tracker.chat;

import francisco.ps.tracker.tracker.Tracker;
import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Chat {
    // Chat id is provided by the Platform
    @Id
    @Column(name = "id_chat")
    private Long id;
    @Column(name = "chat_type")
    private String chatType;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chat")
    private List<Tracker> trackers = new ArrayList<>();

    protected Chat() {}

    public Chat(Long id, String chatType, LocalDateTime createdAt) {
        this.id = id;
        this.chatType = chatType;
        this.createdAt = createdAt;
    }

    public void addTracker(Tracker tracker) {
        trackers.add(tracker);
        tracker.setChat(this);
    }

}
