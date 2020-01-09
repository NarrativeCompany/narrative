package org.narrative.network.core.propertyset.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.propertyset.base.dao.PropertySetDAO;
import org.narrative.network.core.propertyset.base.services.PropertyMap;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import javax.validation.constraints.NotNull;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import java.util.HashMap;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:38:50 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PropertySet implements DAOObject<PropertySetDAO>, PropertyMap {
    private OID oid;
    private PropertySetType propertySetType;

    public static final int MAX_PROPERTY_SET_TYPE_LENGTH = 40;

    private Map<String, Property> propertyTypeToProperty;

    @Deprecated
    public PropertySet() {}

    public PropertySet(PropertySetType propertySetType) {
        this.propertySetType = propertySetType;
        propertyTypeToProperty = new HashMap<>();
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
    @Column(unique = true, columnDefinition = "varchar(" + MAX_PROPERTY_SET_TYPE_LENGTH + ")")
    @Type(type = PropertySetTypeDataType.TYPE)
    public PropertySetType getPropertySetType() {
        return propertySetType;
    }

    public void setPropertySetType(PropertySetType propertySetType) {
        this.propertySetType = propertySetType;
    }

    /**
     * @return
     * @deprecated hibernate bug.  Use getPropertyTypeToPropertyInited
     */
    @OneToMany(mappedBy = Property.FIELD__PROPERTY_SET__NAME, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapKey(name = Property.FIELD__PROPERTY_TYPE__NAME)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Map<String, Property> getPropertyTypeToProperty() {
        return propertyTypeToProperty;
    }

    @Transient
    public Map<String, Property> getPropertyTypeToPropertyInited() {
        return initHibernateMap(getPropertyTypeToProperty());
    }

    public void setPropertyTypeToProperty(Map<String, Property> propertyTypeToProperty) {
        this.propertyTypeToProperty = propertyTypeToProperty;
    }

    public void setPropertyValue(String name, String value) {
        if (IPStringUtil.isEmpty(value)) {
            PropertyType type = getPropertySetType().getPropertyTypeByName(name);
            assert !type.isRequired() : "must have a value for all required properties! empty value for required property: " + type.getName() + " in propertySetType: " + getPropertySetType().getType();
            Property oldProperty = removePropertyValue(name);
            if (exists(oldProperty)) {
                Property.dao().delete(oldProperty);
            }
        } else {
            Property property = getPropertyTypeToPropertyInited().get(name);
            if (property == null) {
                property = new Property(name, value, this);
                getPropertyTypeToPropertyInited().put(property.getPropertyType(), property);
            } else {
                property.setValue(value);
            }
        }
    }

    public void setPropertyValue(PropertyType type, String value) {
        // bl: only set the default value for those that exist.  for those that do not
        // exist, simply do not even create a Property object
        setPropertyValue(type.getName(), value);
    }

    public Property removePropertyValue(String name) {
        return getPropertyTypeToPropertyInited().remove(name);
    }

    @Transient
    public String getPropertyValueByName(String name) {
        Property property = getPropertyByName(name);
        if (exists(property)) {
            return property.getValue();
        }
        return null;
    }

    @Transient
    public Property getPropertyByName(String name) {
        //assert getPropertyTypeToPropertyInited().containsKey(name) : "Should only attempt to get Property for PropertyTypes that exist! Has init been completed? pt/" + name + " pst/" + getPropertySetType();
        return getPropertyTypeToPropertyInited().get(name);
    }

    public static PropertySetDAO dao() {
        return DAOImpl.getDAO(PropertySet.class);
    }
}
