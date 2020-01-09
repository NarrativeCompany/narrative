package org.narrative.common.persistence.hibernate;

import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Sep 29, 2005
 * Time: 10:26:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class BiDirectionalCollectionMetadata {
    private PersistentClass owningClassMD;
    private Collection collectionMD;
    private String collectionPropertyName;
    private PersistentClass containedClassMD;
    private String parentPropertyName;

    public BiDirectionalCollectionMetadata(PersistentClass owningClassMD, Collection collectionMD, String collectionPropertyName, PersistentClass containedClassMD, String parentPropertyName) {
        this.owningClassMD = owningClassMD;
        this.collectionMD = collectionMD;
        this.collectionPropertyName = collectionPropertyName;
        this.containedClassMD = containedClassMD;
        this.parentPropertyName = parentPropertyName;
    }

    public String getCollectionPropertyName() {
        return collectionPropertyName;
    }

    public String getParentPropertyName() {
        return parentPropertyName;
    }

    public PersistentClass getOwningClassMD() {
        return owningClassMD;
    }

    public Collection getCollectionMD() {
        return collectionMD;
    }

    public PersistentClass getContainedClassMD() {
        return containedClassMD;
    }
}
