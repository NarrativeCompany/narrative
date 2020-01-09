package org.narrative.common.datagen

import org.narrative.common.persistence.OID
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO
import org.narrative.network.customizations.narrative.service.api.model.UserDTO
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil
import io.inbot.testfixtures.Person
import io.inbot.testfixtures.RandomNameGenerator
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.math.RandomUtils
import spock.lang.Specification


class UserGen extends Specification {
    public static final RandomNameGenerator randomNameGenerator = new RandomNameGenerator(1234)

    static buildUser() {
        Person p = randomNameGenerator.nextPerson()

        UserDTO.builder()
        .oid(new OID(RandomUtils.nextLong()))
        .avatarSquareUrl("http://bogus.com/" + RandomStringUtils.randomAlphanumeric(20))
        .displayName(p.fullName)
        .username(p.email)
        .build()
    }

    static buildUserList(size) {
        def userDTOList = []
        for (int i = 0; i < size; i++) {
            userDTOList.add(buildUser())
        }
        userDTOList
    }

    static PageDataDTO<UserDTO> buildUserPage(count, pageRequest, totalSize) {
        PageUtil.buildPage(buildUserList(count), pageRequest, totalSize)
    }
}