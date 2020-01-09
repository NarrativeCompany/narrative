package org.narrative.common.datagen

import org.narrative.common.persistence.OID
import org.narrative.network.customizations.narrative.service.api.model.UserDTO
import org.apache.commons.lang.math.RandomUtils
import org.apache.commons.lang3.RandomStringUtils

class TribunalMemberGen {
    static UserDTO buildTribunalMember(OID oid = null) {
        oid = oid == null ? new OID(RandomUtils.nextLong()) : oid
        UserDTO.builder()
               .oid(oid)
               .username(RandomStringUtils.randomAlphanumeric(16))
               .displayName(RandomStringUtils.randomAlphabetic(10))
               .labels(Arrays.asList("founder","tribunal","patron"))
               .avatarSquareUrl("www.example.com/img.jpg")
               .build()
    }

    static List<UserDTO> buildUserList(count) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildTribunalMember())
        }
        res
    }
}
