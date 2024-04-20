package org.judexmars.imagecrud.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * Representation of the Image entity in database.
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "image")
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "filename")
    private String filename;

    @Column(name = "size")
    private Integer size;

    @Column(name = "link")
    private String link;

    @ManyToOne(cascade = CascadeType.MERGE)
    private AccountEntity author;
}
