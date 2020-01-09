package org.narrative.network.customizations.narrative.controller.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 9/30/18
 * Time: 5:49 PM
 *
 * @author brian
 */
@ControllerAdvice
public class CaptureRequestBodyAdvice extends RequestBodyAdviceAdapter {
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        // bl: set the request body object (should be an InputDTO) on the RequestResponseHandler so we have access to it for logging
        networkContext().getReqResp().setRequestBodyObject(body);
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }
}
