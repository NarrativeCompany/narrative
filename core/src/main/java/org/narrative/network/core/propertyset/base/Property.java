package org.narrative.network.core.propertyset.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.propertyset.base.dao.PropertyDAO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:38:56 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Property implements DAOObject<PropertyDAO> {
    private OID oid;
    private String propertyType;
    private PropertySet propertySet;
    private String value;

    public static final int MIN_PROPERTY_TYPE_LENGTH = 1;
    public static final int MAX_PROPERTY_TYPE_LENGTH = 255;

    public static final String FIELD__VALUE__NAME = "value";
    public static final String FIELD__VALUE__COLUMN = FIELD__VALUE__NAME;

    public static final String FIELD__PROPERTY_TYPE__NAME = "propertyType";
    public static final String FIELD__PROPERTY_TYPE__COLUMN = FIELD__PROPERTY_TYPE__NAME;

    public static final String FIELD__PROPERTY_SET__NAME = "propertySet";
    public static final String FIELD__PROPERTY_SET__COLUMN = FIELD__PROPERTY_SET__NAME + "_" + PropertySet.FIELD__OID__NAME;

    @Deprecated
    public Property() {}

    public Property(String propertyType, String value, PropertySet propertySet) {
        this.propertyType = propertyType;
        this.propertySet = propertySet;
        this.value = value;
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
    @Length(min = MIN_PROPERTY_TYPE_LENGTH, max = MAX_PROPERTY_TYPE_LENGTH)
    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "FKC8A841F5A2033515")
    public PropertySet getPropertySet() {
        return propertySet;
    }

    public void setPropertySet(PropertySet propertySet) {
        this.propertySet = propertySet;
    }

    @NotNull
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Lob
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static PropertyDAO dao() {
        return DAOImpl.getDAO(Property.class);
    }
}
