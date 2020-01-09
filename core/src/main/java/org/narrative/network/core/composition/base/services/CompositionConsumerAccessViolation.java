package org.narrative.network.core.composition.base.services;

import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.Securable;

import javax.servlet.http.HttpServletResponse;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 9/14/16
 * Time: 10:45 AM
 */
public class CompositionConsumerAccessViolation extends AccessViolation {
    protected final CompositionConsumer consumer;

    public CompositionConsumerAccessViolation(CompositionConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public String getTitle() {
        if (isCurrentUserAuthor()) {
            return wordlet("compositionConsumerAccessViolation.thankYou");
        }
        return super.getTitle();
    }

    private String message;

    @Override
    public final String getMessage() {
        if (message == null) {
            String typeName = consumer.getTypeLowercaseNameForDisplay();
            message = wordlet("compositionConsumerAccessViolation.accessError", typeName);
        }
        return message;
    }

    private boolean isCurrentUserAuthor() {
        return consumer.getRealAuthorPrimaryRole().isRegisteredUser() && consumer.getRealAuthor().isCurrentUserThisUser();
    }

    @Override
    public Securable getSecurable() {
        if (isCurrentUserAuthor()) {
            return null;
        }
        return super.getSecurable();
    }

    @Override
    public Integer getStatusCodeOverride() {
        if (isCurrentUserAuthor()) {
            return HttpServletResponse.SC_OK;
        }
        return null;
    }
}
