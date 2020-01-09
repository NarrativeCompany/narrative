package org.narrative.network.core.content.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.user.User;
import org.jetbrains.annotations.NotNull;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * ContentWrapper is the class that should be used to generate Content/Composition
 * objects from ContentFields objects during Content creation and editing.
 * <p>
 * Date: Jan 25, 2006
 * Time: 10:26:07 AM
 *
 * @author Brian
 */
public class ContentWrapper<T extends ContentConsumer, CF extends ContentFields<T>> {
    private final Area area;
    private final Portfolio portfolio;
    private final CF contentFields;
    private final Content content;

    /**
     * use this constructor when a user is creating a new piece of content.
     * nb. you must call this constructor from within an AreaTask for the Area to post to.
     * nb. you must call this constructor from within the context of a Composition task.
     *  @param author            the user creating this content
     * @param contentFields     the content fields for which to create a content wrapper
     * @param portfolio the area content fields for the area that this content is being created for
     */
    public ContentWrapper(@NotNull Partition compositionPartition, User author, @NotNull CF contentFields, @NotNull Portfolio portfolio, @NotNull T contentConsumer) {
        area = currentArea();
        this.portfolio = portfolio;
        this.contentFields = contentFields;
        assert exists(area) : "Current area must be set in order to create a new ContentWrapper! oid/ " + (exists(area) ? area.getOid() : null);
        ContentType contentType = contentFields.getContentType();
        content = new Content(compositionPartition, contentType, contentFields.getLiveDatetime(), contentConsumer);

        applyContentFields();
        content.setPortfolio(this.portfolio);
        if (exists(author)) {
            content.setAreaUserRlm(getAreaUserRlm(author.getAreaUserByArea(area)));

        } else if (contentFields.isSupportsGuestAuthors()) {
            assert !isEmpty(contentFields.getGuestName()) : "Should always have a guest name set if the fields support guest authorship and there is no author in scope already!";

            content.setGuestName(contentFields.getGuestName());
        }
        content.getContentStats().syncStats(contentConsumer.getComposition().getCompositionStats());
    }

    /**
     * use this constructor when editing content and the content will
     * be changed with the ContentFields provided
     *  @param content       the content to wrap
     * @param contentFields the contentFields to update the content with
     */
    public ContentWrapper(@NotNull Content content, @NotNull CF contentFields) {
        area = content.getArea();
        portfolio = content.getPortfolio();
        this.contentFields = contentFields;
        OID contentOid = content.getOid();
        assert exists(content) && contentOid != null : "Can't instantiate a ContentFields without a valid Content object!";
        this.content = Content.dao().lock(content);

        applyContentFields();
        ContentStats.dao().getLocked(content.getOid()).syncStats(content.getComposition().getCompositionStats());
    }

    private void applyContentFields() {
        content.setDraft(contentFields.isDraft());

        content.setSubject(contentFields.getSubject());
        content.setExtract(contentFields.getExtract());

        content.setPrettyUrlString(contentFields.getPrettyUrlString());
    }

    public Content getContent() {
        return content;
    }
}
