package org.narrative.network.customizations.narrative.service.impl.file;

import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.TempFileService;
import org.narrative.network.customizations.narrative.service.api.model.TempFileDTO;
import org.narrative.network.customizations.narrative.services.GoogleCloudStorageFileHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Date: 2019-08-19
 * Time: 15:47
 *
 * @author brian
 */
@Service
public class TempFileServiceImpl implements TempFileService {
    private final AreaTaskExecutor areaTaskExecutor;

    public TempFileServiceImpl(AreaTaskExecutor areaTaskExecutor) {
        this.areaTaskExecutor = areaTaskExecutor;
    }

    @Override
    public TempFileDTO uploadTempFile(FileUsageType fileUsageType, MultipartFile file) {
        FileOnDisk fileOnDisk = areaTaskExecutor.executeAreaTask(new UploadTempFileTask(fileUsageType, file));

        String fileUrl;
        if(fileOnDisk.getFileType().isImageFile()) {
            ImageOnDisk imageOnDisk = (ImageOnDisk)fileOnDisk;
            fileUrl = imageOnDisk.getPrimaryImageUrl();
        } else {
            fileUrl = GoogleCloudStorageFileHandler.IMAGES.getFileUri(fileOnDisk.getNetworkPath());
        }

        return TempFileDTO.builder()
                .oid(fileOnDisk.getOid())
                .token(fileOnDisk.getTempFileToken())
                .url(fileUrl)
                .build();
    }
}
