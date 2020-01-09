package org.narrative.network.customizations.narrative.niches.niche;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.niche.dao.NicheOfInterestDAO;
import lombok.Data;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

@Data
@Entity
public class NicheOfInterest implements DAOObject<NicheOfInterestDAO> {
    private static final long serialVersionUID = -5949613231176080486L;

    @Id
    private OID oid;

    @OneToOne
    @MapsId
    @ForeignKey(name = "fk_nicheOfInterest_niche")
    private Niche niche;

    public static NicheOfInterestDAO dao() {
        return DAOImpl.getDAO(NicheOfInterest.class);
    }

}
