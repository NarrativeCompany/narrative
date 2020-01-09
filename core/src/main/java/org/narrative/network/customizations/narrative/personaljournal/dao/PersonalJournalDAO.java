package org.narrative.network.customizations.narrative.personaljournal.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.personaljournal.PersonalJournal;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 2018-12-19
 * Time: 10:20
 *
 * @author jonmark
 */
public class PersonalJournalDAO extends GlobalDAOImpl<PersonalJournal, OID> {
    public PersonalJournalDAO() {
        super(PersonalJournal.class);
    }
}
