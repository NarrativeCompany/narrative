package org.narrative.common.util;

import com.opensymphony.xwork2.XWorkMessages;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Sep 20, 2007
 * Time: 11:14:11 AM
 */
public abstract class BaseValidationHandler implements ValidationHandler {
    @Override
    public final boolean validateNumber(Number value, Number min, Number max, String fieldName, String fieldWordletName, Object... args) {
        return validateNumberWithLabel(value, min, max, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public final boolean validateNumberWithLabel(Number value, Number min, Number max, String fieldName, String labelName) {
        if (value == null) {
            addRequiredFieldError(fieldName, labelName);
            return false;
        }
        if (value.doubleValue() < min.doubleValue() || value.doubleValue() > max.doubleValue()) {
            addWordletizedFieldError(fieldName, "baseValidationHandler.valueOutOfRange", labelName, min, max);
            return false;
        }
        return true;
    }

    @Override
    public final boolean validateNumberMin(Number value, Number min, String fieldName, String fieldWordletName, Object... args) {
        return validateNumberMinWithLabel(value, min, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public final boolean validateNumberMinWithLabel(Number value, Number min, String fieldName, String labelName) {
        if (value == null) {
            addRequiredFieldError(fieldName, labelName);
            return false;
        }
        if (value.doubleValue() < min.doubleValue()) {
            addWordletizedFieldError(fieldName, "baseValidationHandler.valueTooSmall", labelName, min);
            return false;
        }
        return true;
    }

    @Override
    public final boolean validateNumberMax(Number value, Number max, String fieldName, String fieldWordletName, Object... args) {
        return validateNumberMaxWithLabel(value, max, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public final boolean validateNumberMaxWithLabel(Number value, Number max, String fieldName, String labelName) {
        if (value == null) {
            addRequiredFieldError(fieldName, labelName);
            return false;
        }
        if (value.doubleValue() > max.doubleValue()) {
            addWordletizedFieldError(fieldName, "baseValidationHandler.valueTooLarge", labelName, max);
            return false;
        }
        return true;
    }

    @Override
    public final boolean validateNotNull(Object value, String fieldName, String fieldWordletName, Object... args) {
        return validateNotNullWithLabel(value, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public final boolean validateNotNullWithLabel(Object value, String fieldName, String labelName) {
        if (value == null) {
            addRequiredFieldError(fieldName, labelName);
            return false;
        }
        return true;
    }

    @Override
    public boolean validateBigDecimal(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName, String fieldWordletName, Object... args) {
        return validateBigDecimalWithLabel(value, min, max, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public boolean validateBigDecimalWithLabel(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName, String labelName) {
        assert min != null || max != null : "Should always have a min or max when using this, otherwise what is the point?";

        if (validateNotNullWithLabel(value, fieldName, labelName)) {
            if (min != null && min.compareTo(value) > 0) {
                addWordletizedFieldError(fieldName, "baseValidationHandler.valueTooSmall", labelName, min);
                return false;
            }
            if (max != null && max.compareTo(value) < 0) {
                addWordletizedFieldError(fieldName, "baseValidationHandler.valueTooLarge", labelName, max);
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public final boolean validateExists(Object value, String fieldName, String fieldWordletName, Object... args) {
        return validateExistsWithLabel(value, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public final boolean validateExistsWithLabel(Object value, String fieldName, String labelName) {
        if (!exists(value)) {
            addRequiredFieldError(fieldName, labelName);
            return false;
        }
        return true;
    }

    @Override
    public final boolean validateNotEmpty(String value, String fieldName, String fieldWordletName, Object... args) {
        return validateNotEmptyWithLabel(value, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public final boolean validateNotEmptyWithLabel(String value, String fieldName, String labelName) {
        if (isEmpty(value)) {
            addRequiredFieldError(fieldName, labelName);
            return false;
        }
        return true;
    }

    @Override
    public boolean validateNotEmptyOrNull(Collection value, String fieldName, String fieldWordletName, Object... args) {
        return validateNotEmptyOrNullWithLabel(value, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public boolean validateNotEmptyOrNullWithLabel(Collection value, String fieldName, String labelName) {
        if (isEmptyOrNull(value)) {
            addRequiredFieldError(fieldName, labelName);
            return false;
        }
        return true;
    }

    @Override
    public final boolean validateString(String value, int min, int max, String fieldName, String fieldWordletName, Object... args) {
        return validateStringWithLabel(value, min, max, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public final boolean validateStringWithLabel(String value, int min, int max, String fieldName, String labelName) {
        if (isEmpty(value) && min > 0) {
            addRequiredFieldError(fieldName, labelName);
            return false;
        }
        if (IPStringUtil.strLength(value) < min || IPStringUtil.strLength(value) > max) {
            if (min == max) {
                addWordletizedFieldError(fieldName, "baseValidationHandler.stringLengthOutOfBoundsExact", labelName, min);
            } else {
                addWordletizedFieldError(fieldName, "baseValidationHandler.stringLengthOutOfBounds", labelName, min, max);
            }
            return false;
        }
        return true;
    }

    @Override
    public final boolean validateString(String value, String pattern, int min, int max, String fieldName, String fieldWordletName, Object... args) {
        return validateStringWithLabel(value, pattern, min, max, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public final boolean validateStringWithLabel(String value, String pattern, int min, int max, String fieldName, String labelName) {
        boolean ret = validateStringWithLabel(value, min, max, fieldName, labelName);
        boolean ret2 = validateStringPatternWithLabel(value, pattern, fieldName, labelName);
        return ret && ret2;
    }

    private boolean validateStringPatternWithLabel(String value, String pattern, String fieldName, String labelName) {
        if (!isEmpty(value) && !value.matches(pattern)) {
            addInvalidFieldError(fieldName, labelName);
            return false;
        }
        return true;
    }

    @Override
    public boolean validateString(String value, Pattern pattern, String fieldName, String fieldWordletName, Object... args) {
        return validateStringWithLabel(value, pattern, fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public boolean validateStringWithLabel(String value, Pattern pattern, String fieldName, String labelName) {
        if (!isEmpty(value) && !pattern.matcher(value).matches()) {
            addInvalidFieldError(fieldName, labelName);
            return false;
        }
        return true;
    }

    @Override
    public void addWordletizedRequiredFieldError(String fieldName, String fieldWordletName, Object... args) {
        addRequiredFieldError(fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public void addRequiredFieldError(String fieldName, String fieldLabel) {
        addWordletizedFieldError(fieldName, "required.fieldvalue", fieldLabel);
    }

    @Override
    public final void addWordletizedInvalidFieldError(String fieldName, String fieldWordletName, Object... args) {
        addInvalidFieldError(fieldName, resolveFieldWordletName(fieldName, fieldWordletName, args));
    }

    @Override
    public void addInvalidFieldError(String fieldName, String fieldLabel) {
        addWordletizedFieldError(fieldName, XWorkMessages.DEFAULT_INVALID_FIELDVALUE, fieldLabel);
    }

    private String resolveFieldWordletName(String fieldName, String fieldWordletName, Object... args) {
        if (fieldWordletName == null) {
            return fieldName;
        }
        return getText(fieldWordletName, args);
    }

    public abstract String getText(String wordlet, Object... args);
}
