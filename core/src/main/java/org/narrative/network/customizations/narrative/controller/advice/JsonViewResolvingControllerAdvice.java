package org.narrative.network.customizations.narrative.controller.advice;

import com.fasterxml.jackson.annotation.JsonView;
import org.narrative.network.customizations.narrative.serialization.jackson.view.View;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

/**
 * Controller advise to allow dynamic {@link JsonView} selection via request parameter.
 */
@ControllerAdvice
public class JsonViewResolvingControllerAdvice extends AbstractMappingJacksonResponseBodyAdvice {
    private static final String JSONVIEW = "_jsonview";

    @Override
    protected void beforeBodyWriteInternal(@NotNull MappingJacksonValue bodyContainer, @NotNull MediaType contentType, @NotNull MethodParameter returnType, @NotNull ServerHttpRequest req, @NotNull ServerHttpResponse res) {
        ServletServerHttpRequest request = (ServletServerHttpRequest) req;

        //If a view name is specified, apply the view
        String viewName = request.getServletRequest().getParameter(JSONVIEW);
        if (StringUtils.isNotEmpty(viewName)) {
            bodyContainer.setSerializationView(View.resolveView(viewName));
        }
    }
}
