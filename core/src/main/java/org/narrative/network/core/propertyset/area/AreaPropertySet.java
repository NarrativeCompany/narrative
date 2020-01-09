package org.narrative.network.core.propertyset.area;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.IPUtil;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.propertyset.area.dao.AreaPropertySetDAO;
import org.narrative.network.core.propertyset.base.Property;
import org.narrative.network.core.propertyset.base.PropertySet;
import org.narrative.network.core.propertyset.base.PropertySetType;
import org.narrative.network.core.propertyset.base.PropertySetTypeDataType;
import org.narrative.network.core.propertyset.base.PropertyType;
import org.narrative.network.core.propertyset.base.services.PropertyMap;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 4:34:18 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AreaPropertySet implements DAOObject<AreaPropertySetDAO>, PropertyMap {
    private OID oid;
    private PropertySetType propertySetType;
    private Timestamp lastModificationDate;

    private Map<String, AreaPropertyOverride> propertyOverrides;

    private Set<AreaRlm> areaRlms;

    @Deprecated
    public AreaPropertySet() {}

    /**
     * create an AreaPropertySet and default the set's name to the
     * default PropertySet's name
     *
     * @param defaultPropertySet the default PropertySet to use for the new AreaPropertySet
     */
    public AreaPropertySet(PropertySet defaultPropertySet) {
        propertySetType = defaultPropertySet.getPropertySetType();
        assert !propertySetType.isGlobal() : "Should only ever create AreaPropertySet for propertySetType that supports it. Not pst/" + propertySetType.getType();

        lastModificationDate = new Timestamp(System.currentTimeMillis());
        propertyOverrides = new HashMap<>();
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
    // bl: use columnDefinition to limit the column size. can't use @Length, as that forces the object's
    // toString() method to be used, which is obviously not what we want.
    @Column(columnDefinition = "varchar(" + PropertySet.MAX_PROPERTY_SET_TYPE_LENGTH + ")")
    @Type(type = PropertySetTypeDataType.TYPE)
    public PropertySetType getPropertySetType() {
        return propertySetType;
    }

    public void setPropertySetType(PropertySetType propertySetType) {
        this.propertySetType = propertySetType;
    }

    @Transient
    public PropertySet getDefaultPropertySet() {
        return getPropertySetType().getDefaultPropertySet();
    }

    @NotNull
    //@Temporal(TemporalType.TIMESTAMP)
    public Timestamp getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Timestamp lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * @return
     * @deprecated hibernate bug.  Use getPropertyOverridesInited
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = AreaPropertyOverride.FIELD__AREA_PROPERTY_SET__NAME, cascade = CascadeType.ALL)
    @MapKey(name = AreaPropertyOverride.FIELD__PROPERTY_TYPE__NAME)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Map<String, AreaPropertyOverride> getPropertyOverrides() {
        return propertyOverrides;
    }

    @Transient
    public Map<String, AreaPropertyOverride> getPropertyOverridesInited() {
        return initHibernateMap(getPropertyOverrides());
    }

    public void setPropertyOverrides(Map<String, AreaPropertyOverride> propertyOverrides) {
        this.propertyOverrides = propertyOverrides;
    }

    public void setPropertyValue(String name, String value) {
        setPropertyValue(getDefaultPropertySet().getPropertySetType().getPropertyTypeByName(name), value);
    }

    public void setPropertyValue(PropertyType propertyType, String value) {
        assert propertyType != null : "Must supply a PropertyType to set the value for! aps/" + getOid() + " dpst/" + getPropertySetType().getType() + " to value/" + value;
        PropertySet propertySet = getDefaultPropertySet();
        assert exists(propertySet) : "Failed to identify the default PropertySet to set the value for! aps/" + getOid() + " dpst/" + getPropertySetType().getType() + " to value/" + value;
        Property property = propertySet.getPropertyByName(propertyType.getName());

        //if there is no default and they are setting the value to null, then just remove any existing properties if they even exist
        if (property == null && value == null) {
            AreaPropertyOverride apo = getPropertyOverridesInited().remove(propertyType.getName());
            if (apo != null) {
                AreaPropertyOverride.dao().delete(apo);
            }
            return;
        }

        //see if we already have an override, and the value is different than the default
        AreaPropertyOverride override = getPropertyOverridesInited().get(propertyType.getName());
        if (override != null && (property == null || !IPUtil.isEqual(property.getValue(), value))) {
            override.setValue(value);

            // no override already and the values differ
        } else if (property == null || !IPUtil.isEqual(property.getValue(), value)) {
            AreaPropertyOverride areaPropertyOverride = new AreaPropertyOverride(value, propertyType, this);
            getPropertyOverridesInited().put(areaPropertyOverride.getPropertyType(), areaPropertyOverride);

            // override not needed since it matches the default value, so remove from the collections.
        } else {
            AreaPropertyOverride apo = getPropertyOverridesInited().remove(propertyType.getName());
            if (apo != null) {
                AreaPropertyOverride.dao().delete(apo);
            }
        }
    }

    @Transient
    @Nullable
    public String getPropertyValueByName(String name) {
        AreaPropertyOverride override = getPropertyOverridesInited().get(name);
        if (override != null) {
            return override.getValue();
        }
        // not all properties are required, so need to do a null check
        Property defaultProperty = getDefaultPropertySet().getPropertyByName(name);
        return defaultProperty == null ? null : defaultProperty.getValue();
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = AreaRlm.FIELD__SETTINGS_SET__NAME)
    public Set<AreaRlm> getAreaRlms() {
        return areaRlms;
    }

    public void setAreaRlms(Set<AreaRlm> areaRlms) {
        this.areaRlms = areaRlms;
    }

    public static AreaPropertySetDAO dao() {
        return DAOImpl.getDAO(AreaPropertySet.class);
    }
}
