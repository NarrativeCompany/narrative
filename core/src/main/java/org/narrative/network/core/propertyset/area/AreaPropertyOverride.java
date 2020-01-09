package org.narrative.network.core.propertyset.area;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.propertyset.area.dao.AreaPropertyOverrideDAO;
import org.narrative.network.core.propertyset.base.Property;
import org.narrative.network.core.propertyset.base.PropertyType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 4:34:32 PM
 */
@Entity
@Proxy
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {AreaPropertyOverride.FIELD__AREA_PROPERTY_SET__COLUMN, AreaPropertyOverride.FIELD__PROPERTY_TYPE__COLUMN})})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AreaPropertyOverride implements DAOObject<AreaPropertyOverrideDAO> {
    private OID oid;
    private String propertyType;
    private AreaPropertySet areaPropertySet;
    private String value;

    public static final String FIELD__PROPERTY_TYPE__NAME = "propertyType";
    public static final String FIELD__AREA_PROPERTY_SET__NAME = "areaPropertySet";

    public static final String FIELD__PROPERTY_TYPE__COLUMN = FIELD__PROPERTY_TYPE__NAME;
    public static final String FIELD__AREA_PROPERTY_SET__COLUMN = FIELD__AREA_PROPERTY_SET__NAME + "_" + AreaPropertySet.FIELD__OID__NAME;

    @Deprecated
    public AreaPropertyOverride() {}

    public AreaPropertyOverride(String value, PropertyType propertyType, AreaPropertySet areaPropertySet) {
        this.value = value;
        this.areaPropertySet = areaPropertySet;
        this.propertyType = propertyType.getName();
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @NotNull
    @Length(min = Property.MIN_PROPERTY_TYPE_LENGTH, max = Property.MAX_PROPERTY_TYPE_LENGTH)
    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "FK8DA9478E1A5DBDD")
    public AreaPropertySet getAreaPropertySet() {
        return areaPropertySet;
    }

    public void setAreaPropertySet(AreaPropertySet areaPropertySet) {
        this.areaPropertySet = areaPropertySet;
    }

    @Basic(fetch = FetchType.EAGER, optional = true)//changed this to optional = true.  To support null overrides
    @Lob
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static AreaPropertyOverrideDAO dao() {
        return DAOImpl.getDAO(AreaPropertyOverride.class);
    }
}
