package org.narrative.network.customizations.narrative.service.impl.file;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileOnDiskStatus;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.customizations.narrative.service.api.model.input.FileUploadInput;
import org.narrative.network.customizations.narrative.service.api.model.input.TempFileUploadInput;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-28
 * Time: 20:11
 *
 * @author jonmark
 */
public abstract class FileUploadInputProcessor<F extends FileOnDisk, C> {
    private final FileUploadInput input;
    private final String fieldName;
    private final String fieldWordletName;

    private C consumer;
    private F uploadedFile;

    public FileUploadInputProcessor(FileUploadInput input, String fieldName, String fieldWordletName, C consumer) {
        assert input != null : "input should always be provided.";
        assert !isEmpty(fieldName) : "fieldName should always be provided.";
        assert !isEmpty(fieldWordletName) : "fieldWordletName should always be provided.";

        this.consumer = consumer;
        this.input = input;
        this.fieldName = fieldName;
        this.fieldWordletName = fieldWordletName;
    }

    protected abstract F getExistingFile();
    protected abstract void updateFile(F file);

    public void validate(ValidationHandler handler) {
        TempFileUploadInput tempFile = input.getTempFile();
        if (tempFile != null) {
            FileOnDisk fileOnDisk = FileOnDisk.dao().get(tempFile.getOid(), ImageOnDisk.class);
            if (handler.validateExists(fileOnDisk, fieldName, fieldWordletName)) {
                // bl: if the logo exists, then validate that the token is valid, too!
                if (!isEqual(tempFile.getToken(), fileOnDisk.getTempFileToken())) {
                    throw UnexpectedError.getRuntimeException("Invalid temp file token supplied. Rejecting request!");
                }
                uploadedFile = (F) fileOnDisk;
            }
        }
    }

    protected C getConsumer() {
        assert exists(this.consumer) : "Should only ever call this method after a consumer has been set!";

        return consumer;
    }

    public void setConsumer(C consumer) {
        assert !exists(this.consumer) : "Should only ever call this method when a consumer could not be setup as part of construction!";

        this.consumer = consumer;
    }

    public void process() {
        assert exists(consumer) : "A consumer should always be set by the time process is called.";

        // jw: if no file was uploaded and we are not removing the existing file then there is nothing to do.
        if (!exists(uploadedFile) && !input.isRemove()) {
            return;
        }

        // jw: before we do anything else, let's get a reference to the existing file, since we know its being changed.
        F existingFile = getExistingFile();

        if (input.isRemove()) {
            // jw: clear the file on the consumer.
            updateFile(null);

            // jw: if we have a uploaded file, delete it.
            if (exists(uploadedFile)) {
                FileOnDisk.dao().delete(uploadedFile);
            }
        } else {
            assert exists(uploadedFile) : "If we are not clearing out the existing file then we should always have a file to replace it with.";

            // bl: change the status from TEMP_FILE to ACTIVE
            uploadedFile.setStatus(FileOnDiskStatus.ACTIVE);

            updateFile(uploadedFile);
        }

        // jw: finally, since we have replaced the file on the consumer we are now safe to delete the existing file if there is one.
        if (exists(existingFile)) {
            FileOnDisk.dao().delete(existingFile);
        }
    }
}
