package org.narrative.network.core.area.base;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.dao.RoleContentPageViewDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Date: 2019-03-19
 * Time: 09:07
 *
 * @author brian
 */
@Entity
@Proxy
@Data
@NoArgsConstructor
public class RoleContentPageView implements DAOObject<RoleContentPageViewDAO> {

    public static final int DAYS_OF_HISTORY = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false, insertable = false)
    private long id;

    @Column(columnDefinition = "varchar(128)", nullable = false)
    private String roleId;

    @NotNull
    private OID contentOid;

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    private Instant viewDatetime;

    public RoleContentPageView(String roleId, OID contentOid) {
        this.roleId = roleId;
        this.contentOid = contentOid;
        this.viewDatetime = Instant.now();
    }

    @Override
    public OID getOid() {
        throw UnexpectedError.getRuntimeException("RoleContentPageView doesn't support OIDs!");
    }

    @Override
    public void setOid(OID oid) {
        throw UnexpectedError.getRuntimeException("RoleContentPageView doesn't support OIDs!");
    }

    public static RoleContentPageViewDAO dao() {
        return NetworkDAOImpl.getDAO(RoleContentPageView.class);
    }
}
