package com.example.travel_platform.board;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.example.travel_platform.board.reply.Reply;
import com.example.travel_platform.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity
@Table(name = "board_tb")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Convert(converter = BoardCategoryConverter.class)
    @Column(nullable = false, length = 20)
    private BoardCategory category;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    private Board(User user, String title, BoardCategory category, String content, Integer viewCount) {
        this.user = user;
        this.title = title;
        this.category = category;
        this.content = content;
        this.viewCount = viewCount == null ? 0 : viewCount;
    }

    public static Board create(User user, String title, BoardCategory category, String content) {
        return Board.builder()
                .user(user)
                .title(title)
                .category(category)
                .content(content)
                .viewCount(0)
                .build();
    }

    public static Board create(User user, String title, String categoryCode, String content) {
        return create(user, title, BoardCategory.fromCode(categoryCode), content);
    }

    public void update(String title, BoardCategory category, String content) {
        this.title = title;
        this.category = category;
        this.content = content;
    }

    public void update(String title, String categoryCode, String content) {
        update(title, BoardCategory.fromCode(categoryCode), content);
    }

    public void increaseViewCount(Integer viewerUserId) {
        Integer authorId = this.user.getId();

        if (viewerUserId != null && viewerUserId.equals(authorId)) {
            return;
        }
        this.viewCount = this.viewCount + 1;
    }

    public String getCategoryCode() {
        if (category == null) {
            return null;
        }
        return category.getCode();
    }
}
