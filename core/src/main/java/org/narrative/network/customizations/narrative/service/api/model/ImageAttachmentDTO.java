package org.narrative.network.customizations.narrative.service.api.model;

import lombok.Builder;
import lombok.Value;

/**
 * Upload response DTO compatible with Froala's built-in uploader.
 *
 * Date: 2019-01-11
 * Time: 13:22
 *
 * @author jonmark
 */
@Value
@Builder(toBuilder = true)
public class ImageAttachmentDTO {
    private final String link;
}
