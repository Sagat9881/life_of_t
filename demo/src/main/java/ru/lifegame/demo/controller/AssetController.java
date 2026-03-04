package ru.lifegame.demo.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lifegame.demo.dto.DemoDtos.AssetInfoDto;
import ru.lifegame.demo.service.DemoAssetService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Serves generated pixel-art sprite atlas PNGs and their metadata.
 *
 * <ul>
 *   <li>{@code GET /api/assets}           – list all generated assets</li>
 *   <li>{@code GET /api/assets/{id}.png}  – serve the PNG atlas for {@code id}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final DemoAssetService assetService;

    public AssetController(DemoAssetService assetService) {
        this.assetService = assetService;
    }

    /**
     * Returns metadata for every generated asset.
     */
    @GetMapping
    public ResponseEntity<List<AssetInfoDto>> listAssets() {
        return ResponseEntity.ok(assetService.listAssetInfos());
    }

    /**
     * Serves the PNG sprite-atlas for the asset identified by {@code id}.
     *
     * @param id asset identifier, e.g. {@code tanya_idle}
     */
    @GetMapping(value = "/{id}.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getAssetPng(@PathVariable("id") String id) {
        Path png = assetService.resolveAssetPath(id);
        if (png == null || !Files.exists(png)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(png));
    }
}
