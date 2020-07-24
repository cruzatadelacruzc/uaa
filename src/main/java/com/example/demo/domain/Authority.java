package com.example.demo.domain;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * The authorities belong to a user
 */
@Data
@Entity
@Table(name = "uaa_authority")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Authority implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @Column(length = 50)
    private String name;

    public Authority setName(String name) {
        this.name = name;
        return this;
    }
}
