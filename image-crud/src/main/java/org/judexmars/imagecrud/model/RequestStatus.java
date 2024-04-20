package org.judexmars.imagecrud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Entity that represents status of the request to apply filter on image.
 */
@Getter
@Setter
@Entity
@Accessors(chain = true)
@Table(name = "request_status")
public class RequestStatus {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_status_gen")
  @SequenceGenerator(
      name = "request_status_gen",
      sequenceName = "request_status_seq",
      allocationSize = 1)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "name")
  private String name;
}
