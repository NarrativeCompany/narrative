package org.narrative.common.datagen

import org.narrative.common.persistence.OID
import org.narrative.network.core.search.UserSearchResult
import org.narrative.network.core.user.User
import org.narrative.network.customizations.narrative.niches.niche.Niche
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus
import org.narrative.network.customizations.narrative.niches.niche.services.NicheSearchResult
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil

import java.sql.Timestamp

class SearchGen {

    static buildSearch() {
        List<Object> searchResults = new ArrayList<>()
        searchResults.add(buildUser())
        searchResults.add(buildNiche())
        return searchResults
    }

    static buildUser() {
        OID oid = OID.newInstance()
        UserSearchResult userResult = new UserSearchResult(oid, 1)
        User user = new User()
        user.setOid(oid)
        user.setDisplayName("Jane Doe")
        user.setUsername("janeDoe")
        userResult.setUser(user)
        return userResult
    }

    static buildNiche() {
        OID oid = OID.newInstance()
        NicheSearchResult nicheResult = new NicheSearchResult(oid, 2, new Timestamp(System.currentTimeMillis()))
        Niche niche = new Niche()
        niche.setOid(oid)
        niche.setName("My Niche")
        niche.setDescription("Testing.")
        niche.setStatus(NicheStatus.ACTIVE)
        niche.setPrettyUrlString("narrative.org")
        nicheResult.setNiche(niche)
        return nicheResult
    }

    static List<ReferendumDTO> buildSearchList(count) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildSearch())
        }
        res
    }

    static List<ReferendumDTO> buildUserList(count) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildUser())
        }
        res
    }

    static List<ReferendumDTO> buildNicheList(count) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildNiche())
        }
        res
    }

    static PageDataDTO<ReferendumDTO> buildSearchPage(pageRequest, totalSize) {
        PageUtil.buildPage(buildSearchList(), pageRequest, totalSize)
    }

    static PageDataDTO<ReferendumDTO> buildUserPage(pageRequest, totalSize) {
        PageUtil.buildPage(buildUserList(), pageRequest, totalSize)
    }

    static PageDataDTO<ReferendumDTO> buildNichePage(pageRequest, totalSize) {
        PageUtil.buildPage(buildNicheList(), pageRequest, totalSize)
    }

}
