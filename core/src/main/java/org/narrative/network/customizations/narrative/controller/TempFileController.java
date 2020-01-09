package org.narrative.network.customizations.narrative.controller;

import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.customizations.narrative.service.api.TempFileService;
import org.narrative.network.customizations.narrative.service.api.model.TempFileDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Date: 2019-08-19
 * Time: 15:46
 *
 * @author brian
 */
@RestController
@RequestMapping("/temp-files")
@Validated
public class TempFileController {
    private final TempFileService tempFileService;

    public TempFileController(TempFileService tempFileService) {
        this.tempFileService = tempFileService;
    }

    @PostMapping
    public TempFileDTO uploadFile(@RequestParam FileUsageType fileUsageType, @RequestParam("file") MultipartFile file) {
        return tempFileService.uploadTempFile(fileUsageType, file);
    }
}
