package org.narrative.network.core.content.base;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.network.core.content.base.dao.TrendingContentDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Date: 2019-02-24
 * Time: 11:39
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@Table(uniqueConstraints = @UniqueConstraint(name = "uidx_trendingContent_content_buildTime", columnNames = {TrendingContent.COLUMN__CONTENT, TrendingContent.COLUMN__BUILD_TIME}))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TrendingContent implements DAOObject<TrendingContentDAO> {
    public static final String FIELD__CONTENT = "content";
    public static final String FIELD__BUILD_TIME = "buildTime";

    public static final String COLUMN__CONTENT = FIELD__CONTENT +"_"+ Content.FIELD__OID__NAME;
    public static final String COLUMN__BUILD_TIME = FIELD__BUILD_TIME;

    // jw: since we will be inserting the records into this table via sql from ItemHourTrendingStats, let's just use an auto-increment PK:
    // https://thoughts-on-java.org/hibernate-tips-use-auto-incremented-column-primary-key/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Fields.oid, updatable = false, nullable = false, insertable = false)
    private OID oid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_trendingContent_content")
    private Content content;

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    private Instant buildTime;

    private long score;

    /**
     * @deprecated for hibernate use only
     */
    public TrendingContent() {}

    public static TrendingContentDAO dao() {
        return NetworkDAOImpl.getDAO(TrendingContent.class);
    }
}
