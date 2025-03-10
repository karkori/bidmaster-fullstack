package dev.mostapha.bidmaster.infrastructure.persistence.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {
    
    @Id
    private UUID id;
    
    @Column
    private String filename;
    
    @Column
    private String originalFilename;
    
    @Column
    private String contentType;
    
    @Column
    private long size;
    
    @Column
    private String bucketName;
    
    @Column
    private String objectName;
    
    @Column
    private String thumbnailObjectName;
    
    @Column
    private LocalDateTime uploadDate;
    
    @Column
    private UUID uploadedBy;
    
    @Column
    private String type;
    
    @Column
    private UUID referenceId;
    
    @Version
    private Long version;
}
