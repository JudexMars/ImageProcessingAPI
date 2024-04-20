package org.judexmars.imagecrud.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
