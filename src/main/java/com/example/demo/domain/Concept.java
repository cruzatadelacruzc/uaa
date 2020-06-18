package com.example.demo.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 *  A concept it can be: Created, Approved ...
 */
@Data
@Entity
public class Concept implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Size(max = 250)
    @Column(length = 250)
    private String name;

    @NotNull
    private Boolean Activated;

    @NotNull
    private String discriminate;


}
