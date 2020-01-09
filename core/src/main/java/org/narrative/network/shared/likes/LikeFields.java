package org.narrative.network.shared.likes;

import org.hibernate.validator.constraints.Range;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * Date: Oct 8, 2007
 * Time: 9:53:53 AM
 *
 * @author brian
 */
@MappedSuperclass
@Embeddable
public class LikeFields {

    public static final String FIELD__LIKE_COUNT__NAME = "likeCount";

    private int likeCount;

    public LikeFields() {}

    @NotNull
    @Range(min = 0)
    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    @Transient
    public void setFromLikeFields(LikeFields template) {
        this.likeCount = template.likeCount;
    }

    @Transient
    public void addNewLike() {
        likeCount += 1;
    }

    @Transient
    public void removeLike() {
        likeCount = Math.max(0, likeCount - 1);
    }
}
