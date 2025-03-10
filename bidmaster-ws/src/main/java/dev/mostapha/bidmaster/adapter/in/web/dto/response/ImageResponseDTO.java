package dev.mostapha.bidmaster.adapter.in.web.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.image.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDTO {
    
    private UUID id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private long size;
    private String imageUrl;
    private String thumbnailUrl;
    private LocalDateTime uploadDate;
    private UUID uploadedBy;
    private String type;
    private UUID referenceId;
    
    public static ImageResponseDTO fromDomain(Image image, String imageUrl, String thumbnailUrl) {
        return ImageResponseDTO.builder()
                .id(image.getId())
                .filename(image.getFilename())
                .originalFilename(image.getOriginalFilename())
                .contentType(image.getContentType())
                .size(image.getSize())
                .imageUrl(imageUrl)
                .thumbnailUrl(thumbnailUrl)
                .uploadDate(image.getUploadDate())
                .uploadedBy(image.getUploadedBy())
                .type(image.getType().name())
                .referenceId(image.getReferenceId())
                .build();
    }
}
