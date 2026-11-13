package dev.mostapha.bidmaster.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import dev.mostapha.bidmaster.application.port.out.ImageRepositoryPort;
import dev.mostapha.bidmaster.application.port.out.ImageStoragePort;
import dev.mostapha.bidmaster.application.port.out.SecurityScanPort;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;

@DisplayName("Pruebas Unitarias de Servicio: ImageService")
class ImageServiceTest {

    private ImageStoragePort imageStoragePort;
    private SecurityScanPort securityScanPort;
    private ImageRepositoryPort imageRepositoryPort;
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageStoragePort = mock(ImageStoragePort.class);
        securityScanPort = mock(SecurityScanPort.class);
        imageRepositoryPort = mock(ImageRepositoryPort.class);
        imageService = new ImageService(imageStoragePort, securityScanPort, imageRepositoryPort);
        ReflectionTestUtils.setField(imageService, "defaultBucketName", "bidmaster-images");
    }

    @Test
    @DisplayName("Debe almacenar imagen con isPrimary y displayOrder y desmarcar portada previa")
    void shouldStorePrimaryImageAndUnsetPreviousPrimary() {
        UUID uploaderId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        byte[] pngBytes = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0 };

        Image previousPrimary = Image.create(
                "old.png", "old.png", "image/png", 12L,
                "bidmaster-images", "old.png", "thumb_old.png",
                uploaderId, ImageType.AUCTION, auctionId);
        previousPrimary.setAsPrimary();

        when(securityScanPort.validateFileType(pngBytes, "image/png")).thenReturn(true);
        when(securityScanPort.scanFile(pngBytes)).thenReturn(true);
        when(imageRepositoryPort.findByReferenceIdAndType(auctionId, ImageType.AUCTION))
                .thenReturn(List.of(previousPrimary));
        when(imageStoragePort.storeImage(pngBytes, "cover.png", "image/png", "bidmaster-images"))
                .thenReturn("obj-cover.png");
        when(imageStoragePort.storeThumbnail(pngBytes, "cover.png", "image/png", "bidmaster-images"))
                .thenReturn("thumb-cover.png");
        when(imageStoragePort.getPublicUrl("obj-cover.png", "bidmaster-images"))
                .thenReturn("http://localhost:9000/bidmaster-images/obj-cover.png");
        when(imageStoragePort.getPublicUrl("thumb-cover.png", "bidmaster-images"))
                .thenReturn("http://localhost:9000/bidmaster-images/thumb-cover.png");
        when(imageRepositoryPort.save(any(Image.class))).thenAnswer(inv -> inv.getArgument(0));

        Image saved = imageService.storeImage(
                pngBytes, "cover.png", "image/png", uploaderId, ImageType.AUCTION, auctionId, true, 1);

        assertFalse(previousPrimary.isPrimary());
        assertTrue(saved.isPrimary());
        assertEquals(1, saved.getDisplayOrder());
        assertEquals("http://localhost:9000/bidmaster-images/obj-cover.png", saved.getUrl());
        verify(imageRepositoryPort, times(2)).save(any(Image.class));
    }

    @Test
    @DisplayName("Debe delegar firma de 6 parámetros con isPrimary=false y displayOrder=0")
    void shouldDefaultPrimaryFalseAndDisplayOrderZeroInLegacySignature() {
        UUID uploaderId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        byte[] pngBytes = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0 };

        when(securityScanPort.validateFileType(pngBytes, "image/png")).thenReturn(true);
        when(securityScanPort.scanFile(pngBytes)).thenReturn(true);
        when(imageStoragePort.storeImage(any(), anyString(), anyString(), anyString())).thenReturn("obj.png");
        when(imageStoragePort.storeThumbnail(any(), anyString(), anyString(), anyString())).thenReturn("thumb.png");
        when(imageRepositoryPort.save(any(Image.class))).thenAnswer(inv -> inv.getArgument(0));

        Image saved = imageService.storeImage(
                pngBytes, "photo.png", "image/png", uploaderId, ImageType.AUCTION, auctionId);

        assertFalse(saved.isPrimary());
        assertEquals(0, saved.getDisplayOrder());
        verify(imageRepositoryPort, never()).findByReferenceIdAndType(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar displayOrder negativo y contenido vacío o inválido")
    void shouldRejectInvalidOrderOrContent() {
        UUID uploaderId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        byte[] pngBytes = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0 };

        assertThrows(IllegalArgumentException.class, () ->
                imageService.storeImage(new byte[0], "a.png", "image/png", uploaderId, ImageType.AUCTION, auctionId, false, 0));

        assertThrows(IllegalArgumentException.class, () ->
                imageService.storeImage(pngBytes, "a.png", "text/plain", uploaderId, ImageType.AUCTION, auctionId, false, 0));

        assertThrows(IllegalArgumentException.class, () ->
                imageService.storeImage(pngBytes, "a.png", "image/png", uploaderId, ImageType.AUCTION, auctionId, false, -1));
    }

    @Test
    @DisplayName("Debe fijar una imagen existente como portada única preservando su versión")
    void shouldSetPrimaryImageAndUnsetPreviousPreservingVersion() {
        UUID uploaderId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        UUID img1Id = UUID.randomUUID();
        UUID img2Id = UUID.randomUUID();

        Image img1 = Image.reconstitute(
                img1Id, "img1.png", "img1.png", "image/png", 100L,
                "/api/images/" + img1Id + "/raw", "/api/images/" + img1Id + "/thumbnail/raw",
                "bidmaster-images", "obj1.png", "thumb1.png", null,
                true, 0, LocalDateTime.now(), uploaderId, ImageType.AUCTION, auctionId, 2L);
        Image img2 = Image.reconstitute(
                img2Id, "img2.png", "img2.png", "image/png", 100L,
                "/api/images/" + img2Id + "/raw", "/api/images/" + img2Id + "/thumbnail/raw",
                "bidmaster-images", "obj2.png", "thumb2.png", null,
                false, 1, LocalDateTime.now(), uploaderId, ImageType.AUCTION, auctionId, 3L);

        when(imageRepositoryPort.findById(img2Id)).thenReturn(Optional.of(img2));
        when(imageRepositoryPort.findByReferenceIdAndType(auctionId, ImageType.AUCTION))
                .thenReturn(List.of(img1, img2));
        when(imageRepositoryPort.save(any(Image.class))).thenAnswer(inv -> inv.getArgument(0));

        Image updated = imageService.setPrimaryImage(img2Id);

        assertFalse(img1.isPrimary());
        assertTrue(updated.isPrimary());
        assertEquals(3L, updated.getVersion());
        assertEquals(2L, img1.getVersion());
        verify(imageRepositoryPort, times(2)).save(any(Image.class));
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException al fijar portada de una imagen inexistente")
    void shouldThrowNotFoundWhenSettingPrimaryOnMissingImage() {
        UUID missingId = UUID.randomUUID();
        when(imageRepositoryPort.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> imageService.setPrimaryImage(missingId));
    }

    @Test
    @DisplayName("Debe reordenar imágenes secuencialmente preservando portada y versión")
    void shouldReorderImagesSequentiallyPreservingPrimaryAndVersion() {
        UUID uploaderId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        UUID img1Id = UUID.randomUUID();
        UUID img2Id = UUID.randomUUID();

        Image img1 = Image.reconstitute(
                img1Id, "img1.png", "img1.png", "image/png", 100L,
                null, null, "bidmaster-images", "obj1.png", "thumb1.png", null,
                true, 0, LocalDateTime.now(), uploaderId, ImageType.AUCTION, auctionId, 4L);
        Image img2 = Image.reconstitute(
                img2Id, "img2.png", "img2.png", "image/png", 100L,
                null, null, "bidmaster-images", "obj2.png", "thumb2.png", null,
                false, 1, LocalDateTime.now(), uploaderId, ImageType.AUCTION, auctionId, 5L);

        when(imageRepositoryPort.findByReferenceIdAndType(auctionId, ImageType.AUCTION))
                .thenReturn(List.of(img1, img2));
        when(imageRepositoryPort.save(any(Image.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Image> result = imageService.reorderImages(auctionId, ImageType.AUCTION, List.of(img2Id, img1Id));

        assertEquals(2, result.size());
        assertEquals(img2Id, result.get(0).getId());
        assertEquals(0, result.get(0).getDisplayOrder());
        assertFalse(result.get(0).isPrimary());
        assertEquals(5L, result.get(0).getVersion());
        assertEquals(img1Id, result.get(1).getId());
        assertEquals(1, result.get(1).getDisplayOrder());
        assertTrue(result.get(1).isPrimary());
        assertEquals(4L, result.get(1).getVersion());
    }

    @Test
    @DisplayName("Debe rechazar reordenación con duplicados, lista incompleta, IDs ajenos o referencia sin imágenes")
    void shouldRejectInvalidReorderRequests() {
        UUID uploaderId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        UUID img1Id = UUID.randomUUID();
        UUID img2Id = UUID.randomUUID();
        UUID foreignId = UUID.randomUUID();

        Image img1 = Image.reconstitute(
                img1Id, "img1.png", "img1.png", "image/png", 100L,
                null, null, "bidmaster-images", "obj1.png", "thumb1.png", null,
                true, 0, LocalDateTime.now(), uploaderId, ImageType.AUCTION, auctionId, 1L);
        Image img2 = Image.reconstitute(
                img2Id, "img2.png", "img2.png", "image/png", 100L,
                null, null, "bidmaster-images", "obj2.png", "thumb2.png", null,
                false, 1, LocalDateTime.now(), uploaderId, ImageType.AUCTION, auctionId, 1L);

        when(imageRepositoryPort.findByReferenceIdAndType(auctionId, ImageType.AUCTION))
                .thenReturn(List.of(img1, img2));

        // Duplicados
        assertThrows(IllegalArgumentException.class, () ->
                imageService.reorderImages(auctionId, ImageType.AUCTION, List.of(img1Id, img1Id)));

        // Lista incompleta
        assertThrows(IllegalArgumentException.class, () ->
                imageService.reorderImages(auctionId, ImageType.AUCTION, List.of(img1Id)));

        // ID ajeno
        assertThrows(IllegalArgumentException.class, () ->
                imageService.reorderImages(auctionId, ImageType.AUCTION, List.of(img1Id, foreignId)));

        // Referencia sin imágenes
        UUID emptyAuctionId = UUID.randomUUID();
        when(imageRepositoryPort.findByReferenceIdAndType(emptyAuctionId, ImageType.AUCTION))
                .thenReturn(List.of());
        assertThrows(NoSuchElementException.class, () ->
                imageService.reorderImages(emptyAuctionId, ImageType.AUCTION, List.of(img1Id)));
    }
}
