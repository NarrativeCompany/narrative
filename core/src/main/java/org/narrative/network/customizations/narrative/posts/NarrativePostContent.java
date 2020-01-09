package org.narrative.network.customizations.narrative.posts;

import org.narrative.common.persistence.DAOObject;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.ContentWithAttachmentsConsumer;
import org.narrative.network.customizations.narrative.posts.dao.NarrativePostContentDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

/**
 * Date: 2019-01-03
 * Time: 11:52
 *
 * @author jonmark
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class NarrativePostContent extends ContentWithAttachmentsConsumer implements DAOObject<NarrativePostContentDAO> {
    @Deprecated
    public NarrativePostContent() {
        super(null);
    }

    public NarrativePostContent(Composition composition) {
        super(composition);
    }

    @Override
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_narrativePostContent_composition")
    @PrimaryKeyJoinColumn
    public Composition getComposition() {
        return super.getComposition();
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    public ContentType getContentType() {
        return ContentType.NARRATIVE_POST;
    }

    @Transient
    @NotNull
    public NarrativePostContentDAO getDAO() {
        return dao();
    }

    public static NarrativePostContentDAO dao() {
        return NetworkDAOImpl.getDAO(NarrativePostContent.class);
    }
}
