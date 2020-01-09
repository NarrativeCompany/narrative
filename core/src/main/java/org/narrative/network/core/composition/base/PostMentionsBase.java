package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.DAO;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateSetType;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/26/16
 * Time: 12:44 PM
 */
@MappedSuperclass
public abstract class PostMentionsBase<T extends DAO> implements DAOObject<T> {
    private Set<OID> mentionedMemberOids;

    protected PostMentionsBase() {
        mentionedMemberOids = new HashSet<>();
    }

    @Transient
    protected abstract PostBase getPostBase();

    @Type(type = HibernateSetType.TYPE, parameters = {@Parameter(name = HibernateSetType.SET_OBJECT_TYPE_CLASS, value = OID.TYPE)})
    public Set<OID> getMentionedMemberOids() {
        return mentionedMemberOids;
    }

    public void setMentionedMemberOids(Set<OID> mentionedMemberOids) {
        this.mentionedMemberOids = mentionedMemberOids;
    }

    public void addMentionedMemberOids(Collection<OID> memberOids) {
        Set<OID> mentionedMemberOids = getMentionedMemberOids();
        mentionedMemberOids.addAll(memberOids);

        // jw: call the setter so that Hibernate will know about the change.
        setMentionedMemberOids(mentionedMemberOids);
    }
}
