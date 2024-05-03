package org.judexmars.imagecrud.model;

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
 * User's request entity.
 */
@Setter
@Getter
@Entity
@Accessors(chain = true)
@Table(name = "apply_filter_request")
public class ApplyFilterRequestEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "request_id", nullable = false)
  private UUID requestId;

  @ManyToOne
  private ImageEntity image;

  @ManyToOne
  private ImageEntity modifiedImage;

  @Column(name = "modified_image_id")
  private UUID modifiedImageId;

  @ManyToOne
  private RequestStatus status;
}
