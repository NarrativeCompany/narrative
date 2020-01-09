package org.narrative.network.customizations.narrative.service.api.model.validators;

import org.narrative.common.util.UrlUtil;
import org.narrative.common.util.html.HTMLParser;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.customizations.narrative.service.api.model.input.PostTextInputBase;

import javax.validation.ConstraintValidatorContext;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-15
 * Time: 08:33
 *
 * @author jonmark
 */
public abstract class ValidPostTextInputValidatorBase<T extends PostTextInputBase> {
    public static final int MIN_TITLE_LENGTH = 2;
    public static final int MAX_TITLE_LENGTH = 100;

    protected abstract boolean isDraft(T input);

    public boolean isValid(T input, ConstraintValidatorContext context) {
        if (input == null) {
            return false;
        }

        // jw: I would have made this a instance property, but it seems like the instance is used multiple times, so I'm
        //     goind to assume that this might be used asynchronously by multiple threads.
        boolean isValid;
        if (isDraft(input)) {
            isValid = validateDraft(input, context);
        } else {
            isValid = validatePost(input, context);
        }

        if (!isValid) {
            // jw: Since a custom message must have been specified then we should disable the default message.
            context.disableDefaultConstraintViolation();
        }

        return isValid;
    }

    private boolean validateDraft(T input, ConstraintValidatorContext context) {
        // jw: let's track if this is valid by defaulting to true and updating to false if not.
        boolean isValid = true;

        // jw: if any textual field is provided we need to validate it, and update the above flag accordingly.
        if (!isEmpty(input.getTitle())) {
            isValid = validateTitle(input, context) && isValid;
        }

        if (!isEmpty(input.getSubTitle())) {
            isValid = validateSubTitle(input, context) && isValid;
        }

        if (!isEmpty(input.getBody())) {
            isValid = validateBody(input, context) && isValid;
        }

        return isValid;
    }

    protected boolean validatePost(T input, ConstraintValidatorContext context) {
        // jw: we need to track the valid state.
        boolean isValid = true;

        // jw: first, let's validate all of the textual fields
        isValid = validateTitle(input, context) && isValid;
        isValid = validateSubTitle(input, context) && isValid;
        isValid = validateBody(input, context) && isValid;
        // bl: let's only worry about validating the canonical URL when moving to step 2 (not for saving drafts)
        isValid = validateCanonicalUrl(input, context) && isValid;

        return isValid;
    }

    private boolean validateTitle(T input, ConstraintValidatorContext context) {
        return validateTitleField(input.getTitle(), PostTextInputBase.Fields.title, true, context);
    }

    private boolean validateSubTitle(T input, ConstraintValidatorContext context) {
        return validateTitleField(input.getSubTitle(), PostTextInputBase.Fields.subTitle, false, context);
    }

    private boolean validateTitleField(String title, String fieldName, boolean required, ConstraintValidatorContext context) {
        if (!required && isEmpty(title)) {
            return true;
        }

        if (isEmpty(title) || title.length() < MIN_TITLE_LENGTH || title.length() > MAX_TITLE_LENGTH) {
            context.buildConstraintViolationWithTemplate( "{validNarrativePostInputValidator.mustBeBetween}")
                    .addPropertyNode(fieldName)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean validateBody(T input, ConstraintValidatorContext context) {
        String body = input.getBody();

        if (isEmpty(body)) {
            context.buildConstraintViolationWithTemplate( "{validNarrativePostInputValidator.invalidPostBody}")
                    .addPropertyNode(PostTextInputBase.Fields.body)
                    .addConstraintViolation();
            return false;
        }

        // bl: make sure that the post body isn't too large. the below visible content check can take a really
        // long time due to regex evaluation in linkifyEmailAddresses, so this should at least save on excessive
        // processing that can really hurt app server performance
        if(body.length()>Composition.MAX_BODY_LENGTH) {
            context.buildConstraintViolationWithTemplate( "{validNarrativePostInputValidator.postBodyTooLarge}")
                    .addPropertyNode(PostTextInputBase.Fields.body)
                    .addConstraintViolation();
            return false;
        }

        // jw: we will consider the body valid as long as it has something visible within it.
        if (!HTMLParser.doesHtmlFragmentContainVisibleContent(body)) {
            context.buildConstraintViolationWithTemplate( "{validNarrativePostInputValidator.invalidPostBody}")
                    .addPropertyNode(PostTextInputBase.Fields.body)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean validateCanonicalUrl(T input, ConstraintValidatorContext context) {
        String canonicalUrl = input.getCanonicalUrl();

        // bl: this field is optional, so only validate it's a valid URL if something is in the value
        if(isEmpty(canonicalUrl)) {
            return true;
        }

        // if the URL is not valid, record an error!
        if(!UrlUtil.isUrlValid(canonicalUrl)) {
            context.buildConstraintViolationWithTemplate("{validNarrativePostInputValidator.invalidCanonicalUrl}")
                    .addPropertyNode(PostTextInputBase.Fields.canonicalUrl)
                    .addConstraintViolation();
            return false;
        }

        // if the URL doesn't meet the length requirements, record an error!
        int length = canonicalUrl.length();
        if(length<Composition.MIN_CANONICAL_URL_LENGTH || length>Composition.MAX_CANONICAL_URL_LENGTH) {
            context.buildConstraintViolationWithTemplate("{validNarrativePostInputValidator.canonicalUrlLengthError}")
                    .addPropertyNode(PostTextInputBase.Fields.canonicalUrl)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
