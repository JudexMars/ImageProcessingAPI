package org.judexmars.imagecrud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "image_id")
  private ImageEntity image;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "modified_image_id")
  private ImageEntity modifiedImage;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "status_id")
  private RequestStatus status;
}
