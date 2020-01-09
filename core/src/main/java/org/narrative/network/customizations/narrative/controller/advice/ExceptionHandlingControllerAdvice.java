package org.narrative.network.customizations.narrative.controller.advice;

import com.google.common.annotations.VisibleForTesting;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.util.PageNotFoundError;
import org.narrative.common.util.TaskValidationException;
import org.narrative.common.util.ValidationError;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.security.jwt.InvalidSignInException;
import org.narrative.network.core.security.jwt.JwtToken2FAExpiredException;
import org.narrative.network.core.security.jwt.JwtTokenInvalidException;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.ValidationException;
import org.narrative.network.customizations.narrative.service.api.model.ErrorDTO;
import org.narrative.network.customizations.narrative.service.api.model.ErrorType;
import org.narrative.network.customizations.narrative.service.api.model.ValidationErrorDTO;
import org.narrative.network.customizations.narrative.service.mapper.FieldErrorMapper;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.Securable;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.MessageSource;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Controller advice to handle REST exceptions appropriately
 *
 * Date: 8/20/18
 * Time: 1:49 PM
 *
 * @author brian
 */
@ControllerAdvice
public class ExceptionHandlingControllerAdvice {
    private static final NetworkLogger logger = new NetworkLogger(ExceptionHandlingControllerAdvice.class);

    private final MessageSource messageSource;
    private final WebMvcProperties webMvcProperties;
    private final FieldErrorMapper fieldErrorMapper;
    private final ObjectFactory<ValidationContext> validationContextFactory;

    public ExceptionHandlingControllerAdvice(MessageSource messageSource, WebMvcProperties webMvcProperties, FieldErrorMapper fieldErrorMapper, ObjectFactory<ValidationContext> validationContextFactory) {
        this.messageSource = messageSource;
        this.webMvcProperties = webMvcProperties;
        this.fieldErrorMapper = fieldErrorMapper;
        this.validationContextFactory = validationContextFactory;
    }

    @ExceptionHandler
    public ResponseEntity<Object> handle(Exception e, HttpServletRequest request) {
        // bl: if there is an exception, then the PartitionGroup needs to be marked as being in error.
        setCurrentPartitionGroupInError();

        Pair<HttpStatus,?> pair = getResponse(e);

        // record the exception, too!
        StatisticManager.recordException(e, false, null);
        if(logger.isErrorEnabled()) logger.error("Exception during Spring REST API processing", e);

        Object dto = pair.getSecond();

        // bl: if it's a GET request "validation error", then convert it to an unknown ErrorDTO.
        // we don't want to return any validation errors for GET requests. really, this is indicative of a bug
        // whereas a validation error is generally indicative of user input error.
        if(dto instanceof ValidationErrorDTO && IPHttpUtil.isGet(request)) {
            pair = getUnknownErrorResponse(e, dto);
        }

        return new ResponseEntity<>(pair.getSecond(), pair.getFirst());
    }

    /**
     * Wrap this so we can mock while performing MVC tests
     */
    @VisibleForTesting
    void setCurrentPartitionGroupInError(){
        PartitionGroup.setCurrentPartitionGroupInError(true);
    }

    private Pair<HttpStatus,?> getResponse(Exception e) {
        if(e instanceof ValidationException) {
            return getValidationErrorResponse((ValidationException)e);
        }

        if(e instanceof TaskValidationException) {
            return getValidationErrorResponse((TaskValidationException)e);
        }

        if(e instanceof MethodArgumentNotValidException) {
            return getValidationErrorResponse(((MethodArgumentNotValidException)e).getBindingResult());
        }

        if(e instanceof BindException) {
            return getValidationErrorResponse((BindException)e);
        }

        if(e instanceof InvalidSignInException) {
            return getValidationErrorResponse((InvalidSignInException)e);
        }

        if(e instanceof AuthenticationException) {
            return getValidationErrorResponse((AuthenticationException)e);
        }

        if (e instanceof MissingServletRequestParameterException) {
            return getValidationErrorResponse((MissingServletRequestParameterException) e);
        }

        if (e instanceof MethodArgumentTypeMismatchException) {
            return getValidationErrorResponse((MethodArgumentTypeMismatchException) e);
        }

        /*
         * Don't explicitly handle ConstraintViolationExceptions for now
        if (e instanceof ConstraintViolationException) {
            return getValidationErrorResponse((ConstraintViolationException) e);
        }
        */

        if(e instanceof MissingServletRequestPartException) {
            return getValidationErrorResponse((MissingServletRequestPartException)e);
        }

        return getErrorResponse(e);
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(TaskValidationException e) {
        // unwrap the TaskValidationException errors into a usable ValidationContext from which we can generate the response.
        ValidationContext context = validationContextFactory.getObject();
        context.addValidationErrors(e.getValidationErrors());
        return getValidationErrorResponse(context);
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(ValidationException e) {
        return getValidationErrorResponse(e.getValidationContext());
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(ValidationContext context) {
        // collect method errors
        List<String> methodErrors = context.getMethodValidationErrors().stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.toList());
        // collect field errors
        Map<String,List<String>> fieldErrors = context.getFieldValidationErrors().stream()
                .collect(Collectors.groupingBy(ValidationError::getField, Collectors.mapping(ValidationError::getMessage, Collectors.toList())));
        return Pair.of(HttpStatus.BAD_REQUEST, ValidationErrorDTO.builder().methodErrors(methodErrors).fieldErrors(fieldErrorMapper.mapFieldErrorsMapToFieldErrorDTO(fieldErrors)).build());
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(BindingResult bindingResult) {
        // collect global/method errors
        List<String> methodErrors = bindingResult.getGlobalErrors().stream()
                .map(error -> messageSource.getMessage(error, webMvcProperties.getLocale()))
                .collect(Collectors.toList());
        // collect field errors
        Map<String,List<String>> fieldErrors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(error -> messageSource.getMessage(error, webMvcProperties.getLocale()), Collectors.toList())));
        return Pair.of(HttpStatus.BAD_REQUEST, ValidationErrorDTO.builder().methodErrors(methodErrors).fieldErrors(fieldErrorMapper.mapFieldErrorsMapToFieldErrorDTO(fieldErrors)).build());
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(MissingServletRequestPartException e) {
        return buildBadParameterValidationResponse(e.getRequestPartName(), e.getMessage());
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(AuthenticationException e) {
        // bl: just use a generic sign in failure message for any AuthenticationException
        return Pair.of(HttpStatus.BAD_REQUEST, ValidationErrorDTO.builder().methodErrors(Collections.singletonList(wordlet("login.signInFailed"))).build());
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(InvalidSignInException e) {
        // bl: use the supplied failure message for any InvalidSignInException
        return Pair.of(HttpStatus.BAD_REQUEST, ValidationErrorDTO.builder().methodErrors(Collections.singletonList(e.getMessage())).build());
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(MissingServletRequestParameterException e) {
        return buildBadParameterValidationResponse(e.getParameterName(), e.getMessage());
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(MethodArgumentTypeMismatchException e) {
        return buildBadParameterValidationResponse(e.getName(), e.getMessage());
    }

    private Pair<HttpStatus, ValidationErrorDTO> getValidationErrorResponse(ConstraintViolationException e) {
        Map<String, List<String>> fieldErrors = e.getConstraintViolations().stream()
                .map(err -> {
                    String propPath = err.getPropertyPath().toString();
                    propPath = propPath.substring(propPath.lastIndexOf('.') + 1);
                    return new ImmutablePair<>(propPath, err.getMessage());
                })
                .collect(Collectors.toMap(ImmutablePair::getLeft, errPair -> Collections.singletonList(errPair.getRight())));
        return Pair.of(HttpStatus.BAD_REQUEST, ValidationErrorDTO.builder().fieldErrors(fieldErrorMapper.mapFieldErrorsMapToFieldErrorDTO(fieldErrors)).build());
    }

    private Pair<HttpStatus, ValidationErrorDTO> buildBadParameterValidationResponse(String paramName, String message){
        Map<String, List<String>> fieldErrors = new HashMap<>();
        fieldErrors.put(paramName, Collections.singletonList(message));
        return Pair.of(HttpStatus.BAD_REQUEST, ValidationErrorDTO.builder().fieldErrors(fieldErrorMapper.mapFieldErrorsMapToFieldErrorDTO(fieldErrors)).build());
    }

    private Pair<HttpStatus,ErrorDTO> getErrorResponse(Exception e) {
        if(e instanceof ApplicationError) {
            return getResponse((ApplicationError)e);
        }

        if(e instanceof JwtTokenInvalidException) {
            return getResponse((JwtTokenInvalidException)e);
        }

        if(e instanceof NoHandlerFoundException) {
            return getResponse((NoHandlerFoundException)e);
        }

        return getUnknownErrorResponse(e, null);
    }

    private Pair<HttpStatus,ErrorDTO> getUnknownErrorResponse(Exception e, Object detail) {
        ErrorDTO.ErrorDTOBuilder builder = ErrorDTO.builder();

        String referenceId = NetworkRegistry.getInstance().getReferenceIdFromException(e);
        builder.type(ErrorType.UNKNOWN_ERROR)
                .referenceId(referenceId)
                .detail(detail);

        return Pair.of(HttpStatus.INTERNAL_SERVER_ERROR, builder.build());
    }

    private Pair<HttpStatus,ErrorDTO> getResponse(ApplicationError appError) {
        ErrorDTO.ErrorDTOBuilder builder = ErrorDTO.builder()
                .title(appError.getTitle())
                .message(appError.getMessage());

        boolean isAccessViolation = appError instanceof AccessViolation;

        // bl: if an ErrorType is set via the ApplicationError, then use it
        if(appError.getErrorType()!=null) {
            builder.type(appError.getErrorType());
        }

        // jw: if a detail object is provided by the ApplicationError, then use it.
        {
            Object detailObject = appError.getErrorDetailObject();
            // jw: choosing to only set this if we have an object to ensure that we are only overriding existing values
            //     if we have something.
            if (detailObject != null) {
                builder.detail(detailObject);
            }
        }

        // bl: allow ApplicationErrors to define their own custom status code, if desired.
        if (appError.getStatusCodeOverride() != null) {
            return Pair.of(HttpStatus.valueOf(appError.getStatusCodeOverride()), builder.build());
        }

        if (isAccessViolation) {
            HttpStatus status;
            if(networkContext().isHasPrimaryRole() && networkContext().getPrimaryRole().isRegisteredUser()) {
                status = HttpStatus.FORBIDDEN;
                // jw: only set the error type if the error did not set one explicitly.
                if (appError.getErrorType()==null) {
                    builder.type(ErrorType.ACCESS_DENIED);
                }
            } else {
                status = HttpStatus.UNAUTHORIZED;
                builder.type(ErrorType.LOGIN_REQUIRED);
            }
            return Pair.of(status, builder.build());
        }

        if (appError instanceof PageNotFoundError) {
            builder.type(ErrorType.NOT_FOUND);
            return Pair.of(HttpStatus.NOT_FOUND, builder.build());
        }

        // bl: 400 / Bad Request is the default for all other ApplicationErrors.
        return Pair.of(HttpStatus.BAD_REQUEST, builder.build());
    }

    private Pair<HttpStatus,ErrorDTO> getResponse(JwtTokenInvalidException e) {
        ErrorDTO.ErrorDTOBuilder builder = ErrorDTO.builder();

        if (e instanceof JwtToken2FAExpiredException) {
            builder.type(ErrorType.JWT_2FA_EXPIRED);
        } else {
            builder.type(ErrorType.JWT_INVALID);
        }

        return Pair.of(HttpStatus.BAD_REQUEST, builder.build());
    }

    private Pair<HttpStatus,ErrorDTO> getResponse(NoHandlerFoundException e) {
        ErrorDTO.ErrorDTOBuilder builder = ErrorDTO.builder();
        builder.type(ErrorType.NOT_FOUND);
        builder.message(e.getMessage());

        return Pair.of(HttpStatus.NOT_FOUND, builder.build());
    }
}
