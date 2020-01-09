package org.narrative.network.core.moderation;

import org.narrative.common.persistence.*;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.content.base.Content;

import org.narrative.network.core.moderation.dao.ModeratedContentDAO;
import org.narrative.network.shared.daobase.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Date: 2019-07-31
 * Time: 09:16
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ModeratedContent implements DAOObject<ModeratedContentDAO> {
    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = Fields.content)})
    private OID oid;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @PrimaryKeyJoinColumn
    @ForeignKey(name = "fk_moderatedContent_content")
    private Content content;

    @NotNull
    @Type(type= HibernateInstantType.TYPE)
    private Instant moderationDatetime;

    public ModeratedContent(Content content) {
        this.content = content;
        this.moderationDatetime = Instant.now();
    }

    public static ModeratedContentDAO dao() {
        return NetworkDAOImpl.getDAO(ModeratedContent.class);
    }
}
