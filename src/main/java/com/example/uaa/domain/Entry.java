package com.example.uaa.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Class parent representing the type entry:
 * DEPOSIT
 * WITHDRAW
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
public class Entry extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @Column(name = "movement_date")
    private ZonedDateTime date;

    @NotNull
    @Column(name = "movement_type", nullable = false)
    private String type;

    @NotNull
    @Column(nullable = false)
    private String amount;

    @NotNull
    @Column(nullable = false)
    private String currency;

    @NotNull
    @Column(name = "_status")
    private String status;

    @JsonIgnore
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "entry")
    private Set<DailyBook> dailyBooks = new HashSet<>();

    public Entry setId(Long id) {
        this.id = id;
        return this;
    }

    public Entry setUser(User user) {
        this.user = user;
        return this;
    }

    public Entry setDate(ZonedDateTime date) {
        this.date = date;
        return this;
    }

    public Entry setType(String type) {
        this.type = type;
        return this;
    }

    public Entry setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public Entry setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public Entry setStatus(String status) {
        this.status = status;
        return this;
    }

    public Entry setDailyBooks(Set<DailyBook> dailyBooks) {
        this.dailyBooks = dailyBooks;
        return this;
    }
}
