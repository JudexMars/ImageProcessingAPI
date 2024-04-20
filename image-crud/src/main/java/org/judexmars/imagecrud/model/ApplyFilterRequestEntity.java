package org.judexmars.imagecrud.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

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

    @Column(name = "image_id", nullable = false)
    private UUID imageId;

    @ManyToOne
    private RequestStatus status;
}
