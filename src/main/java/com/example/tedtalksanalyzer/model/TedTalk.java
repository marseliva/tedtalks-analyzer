package com.example.tedtalksanalyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ted_talks", indexes = {
        @Index(name = "idx_tedtalks_author", columnList = "author"),
        @Index(name = "idx_tedtalks_date", columnList = "date")
})
@NoArgsConstructor
@Getter
@Setter
public class TedTalk {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Long views;

    @Column(nullable = false)
    private Long likes;

    @Column(nullable = false, length = 1000)
    private String link;

    public TedTalk(String title, String author, LocalDate date, Long views, Long likes, String link) {
        this.title = title;
        this.author = author;
        this.date = date;
        this.views = views;
        this.likes = likes;
        this.link = link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TedTalk tedTalk = (TedTalk) o;
        return Objects.equals(id, tedTalk.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
