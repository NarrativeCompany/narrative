package org.narrative.common.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Sep 20, 2007
 * Time: 11:13:14 AM
 * Base interface to handle validation of business logic
 */
public interface ValidationHandler {

    boolean validateNumber(Number value, Number min, Number max, String fieldName, String fieldWordletName, Object... args);

    boolean validateNumberWithLabel(Number value, Number min, Number max, String fieldName, String labelName);

    boolean validateNumberMin(Number value, Number min, String fieldName, String fieldWordletName, Object... args);

    boolean validateNumberMinWithLabel(Number value, Number min, String fieldName, String labelName);

    boolean validateNumberMax(Number value, Number max, String fieldName, String fieldWordletName, Object... args);

    boolean validateNumberMaxWithLabel(Number value, Number max, String fieldName, String labelName);

    boolean validateNotNull(Object value, String fieldName, String fieldWordletName, Object... args);

    boolean validateNotNullWithLabel(Object value, String fieldName, String labelName);

    boolean validateBigDecimal(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName, String fieldWordletName, Object... args);

    boolean validateBigDecimalWithLabel(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName, String labelName);

    boolean validateExists(Object value, String fieldName, String fieldWordletName, Object... args);

    boolean validateExistsWithLabel(Object value, String fieldName, String labelName);

    boolean validateNotEmpty(String value, String fieldName, String fieldWordletName, Object... args);

    boolean validateNotEmptyWithLabel(String value, String fieldName, String labelName);

    boolean validateNotEmptyOrNull(Collection value, String fieldName, String fieldWordletName, Object... args);

    boolean validateNotEmptyOrNullWithLabel(Collection value, String fieldName, String labelName);

    boolean validateString(String value, int min, int max, String fieldName, String fieldWordletName, Object... args);

    boolean validateStringWithLabel(String value, int min, int max, String fieldName, String labelNam);

    boolean validateString(String value, String pattern, int min, int max, String fieldName, String fieldWordletName, Object... args);

    boolean validateStringWithLabel(String value, String pattern, int min, int max, String fieldName, String labelName);

    boolean validateString(String value, Pattern pattern, String fieldName, String fieldWordletName, Object... args);

    boolean validateStringWithLabel(String value, Pattern pattern, String fieldName, String labelName);

    void addWordletizedActionError(String anErrorMessage, Object... args);

    void addActionError(String anErrorMessage);

    void addWordletizedRequiredFieldError(String fieldName, String fieldWordletName, Object... args);

    void addRequiredFieldError(String fieldName, String fieldLabel);

    void addWordletizedInvalidFieldError(String fieldName, String fieldWordletName, Object... args);

    void addInvalidFieldError(String fieldName, String fieldLabel);

    void addWordletizedFieldError(String fieldName, String errorMessage, Object... args);

    void addFieldError(String fieldName, String errorMessage);

    boolean hasErrors();

    List<ValidationError> getValidationErrors();

    boolean isThrowApplicationErrorOnValidationError();

}
