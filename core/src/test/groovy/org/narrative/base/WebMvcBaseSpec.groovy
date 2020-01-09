package org.narrative.base

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.narrative.config.JacksonConfiguration
import org.narrative.network.customizations.narrative.controller.advice.CaptureRequestBodyAdvice
import org.narrative.network.customizations.narrative.controller.advice.ExceptionHandlingControllerAdvice
import org.narrative.network.customizations.narrative.service.api.ValidationContext
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO
import org.narrative.network.customizations.narrative.service.api.model.ValidationErrorDTO
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper
import org.narrative.network.customizations.narrative.service.impl.common.ValidationContextImpl
import org.narrative.network.customizations.narrative.service.impl.common.ValidationExceptionFactory
import org.narrative.network.customizations.narrative.service.mapper.FieldErrorMapperImpl
import org.narrative.network.customizations.narrative.util.ValidationHelper
import groovy.util.logging.Slf4j
import jodd.util.LocaleUtil
import org.apache.commons.collections.CollectionUtils
import org.spockframework.mock.MockUtil
import org.springframework.beans.BeansException
import org.springframework.beans.factory.ObjectFactory
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.core.convert.converter.Converter
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.View
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.view.json.MappingJackson2JsonView
import spock.lang.Shared
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.stream.Collectors

/**
 * Base class for MVC tests
 */
@Slf4j
abstract class WebMvcBaseSpec extends Specification {
    static final List controllerAdviceList
    static final ObjectMapper objectMapper = new ObjectMapper()
    static final WebMvcProperties webMvcProperties
    static final ResourceBundleMessageSource messageSource
    static MappingJackson2JsonView mappingJackson2JsonView
    static ObjectFactory<ValidationContext> objectFactory
    protected static SpringValidatorAdapter validator
    protected static Locale locale = Locale.getDefault()
    protected static LocalValidatorFactoryBean validatorFactoryBean
    @Shared
    int pageResultCount = 10
    @Shared
    PageableHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver = new PageableHandlerMethodArgumentResolver()
    @Shared
    MockMvc mockMvc;
    @Shared
    MockUtil mockUtil = new MockUtil()
    @Shared
    ExceptionHandlingControllerAdvice exceptionHandlingControllerAdvice
    @Shared
    LogSuppressor logSuppressor = new LogSuppressor();
    @Shared
    StaticMethodWrapper staticMethodWrapper
    @Shared
    ValidationHelper validationHelper
    @Shared
    ValidationExceptionFactory validationExceptionFactory

    static {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(ControllerAdvice));
        controllerAdviceList = provider.findCandidateComponents(ExceptionHandlingControllerAdvice.class.getPackage().name).stream()
            //Exclude the exception handler advice since it requires some initialization
            .filter{ca -> !ca.beanClassName.equals(ExceptionHandlingControllerAdvice.class.name)}
            //Exclude the request body capture advice since it requires NetworkContext
            .filter{ca -> !ca.beanClassName.equals(CaptureRequestBodyAdvice.class.name)}
            .map{ca -> Class.forName(ca.beanClassName)}
            .collect(Collectors.toList())

        //Customize the Jackson configuration as per our JacksonConfiguration
        Jackson2ObjectMapperBuilder objectMapperBuilder = new Jackson2ObjectMapperBuilder()
        new JacksonConfiguration().jackson2ObjectMapperBuilderCustomizer().customize(objectMapperBuilder)
        objectMapperBuilder.featuresToEnable(SerializationFeature.INDENT_OUTPUT)
        objectMapperBuilder.modules(new JavaTimeModule())
        mappingJackson2JsonView = new  MappingJackson2JsonView(objectMapperBuilder.build())

        //Set up things needed ExceptionHandlingControllerAdvice
        webMvcProperties = new WebMvcProperties();
        webMvcProperties.setLocale(LocaleUtil.getLocale('en', 'US'))
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames('narrative','narrative_niches','global')

        validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource)
        validatorFactoryBean.afterPropertiesSet()
        validator = new SpringValidatorAdapter(validatorFactoryBean.validator)

        objectFactory = new ObjectFactory<ValidationContext>() {
            @Override
            ValidationContext getObject() throws BeansException {
                return new ValidationContextImpl(messageSource, locale)
            }
        }
    }

    /**
     * Defer controller creation to the implementing test spec.  Hand off a DetachedMockFactory - implementing specs
     * must use this to create their mocks so that they can be attached/detached from the spec per test case.  This
     * allows mocks to be @Shared in order to have a single set up of MockMvc per spec since this is an expensive operation.
     */
    abstract def buildController(DetachedMockFactory detachedMockFactory)

    /**
     * Get a list of all mocks used by the tested controller
     */
    abstract def getMockList()

    /** Get a list of all converters used by the tested controller
     *
     */
    protected def getConverters() {
        []
    }

    def setupSpec() {
        staticMethodWrapper = Mock(StaticMethodWrapper)
        validationHelper = (ValidationHelper) Spy(ValidationHelper, constructorArgs: [objectFactory, validator])
        validationExceptionFactory = (ValidationExceptionFactory) Spy(ValidationExceptionFactory, constructorArgs: [objectFactory])

        def mockFactory = new DetachedMockFactory()
        def controller = buildController(mockFactory)

        def curAdviceList = new ArrayList(controllerAdviceList)

        //Spy the exception handler advice so we can stub out static calls
        exceptionHandlingControllerAdvice = mockFactory.Spy(ExceptionHandlingControllerAdvice, constructorArgs: [messageSource, webMvcProperties, new FieldErrorMapperImpl(), objectFactory]) as ExceptionHandlingControllerAdvice
        curAdviceList.add(exceptionHandlingControllerAdvice)

        //Since this is MockMvc and we don't have a real context, post process the controller to wrap with
        //validation handling
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor()
        postProcessor.setValidator(validator)
        postProcessor.setValidatedAnnotationType(RestController)
        postProcessor.afterPropertiesSet()
        Object advisedController = postProcessor.postProcessAfterInitialization(controller, 'controller')

        def mockMvcBuilder = MockMvcBuilders.standaloneSetup(advisedController)
                .setCustomArgumentResolvers(pageableHandlerMethodArgumentResolver)
                .setViewResolvers(new ViewResolver() {
                        @Override
                        public View resolveViewName(String viewName, Locale locale) throws Exception {
                            return mappingJackson2JsonView;
                        }
                })
                .setControllerAdvice(curAdviceList.toArray())
                .setValidator(validatorFactoryBean)

        def converters = getConverters()
        if (CollectionUtils.isNotEmpty(converters)) {
            FormattingConversionService fcs = new FormattingConversionService()
            for (Converter c : getConverters()) {
                fcs.addConverter(c)
            }
            mockMvcBuilder.setConversionService(fcs)
        }

        mockMvc = mockMvcBuilder.build();
    }

    /**
     * Attach detached mocks at test case setup
     */
    def setup() {
        for (Object mock : getMockList()) {
            mockUtil.attachMock(mock, this)
        }

        mockUtil.attachMock(staticMethodWrapper, this)
        mockUtil.attachMock(exceptionHandlingControllerAdvice, this)
    }

    /**
     * Detach  mocks at test case teardown
     */
    def cleanup() {
        mockUtil.detachMock(staticMethodWrapper)
        mockUtil.detachMock(exceptionHandlingControllerAdvice)

        for (Object mock : getMockList()) {
            mockUtil.detachMock(mock)
        }
    }

    def validatePageRequest(Pageable pageRequest, expectedPage, expectedSize) {
        assert pageRequest.pageNumber == expectedPage
        assert pageRequest.pageSize == expectedSize
    }

    /**
     * Bind the parameter map to the URI passed in
     */
    def bindParamsToUri(uriString, Map argMap=[:]) {
        def firstArg = true
        def argFrag = ''
        argMap.each { k, v ->
            argFrag += (firstArg ? "?" : "&") + k + "=" + v
            if (firstArg) {
                firstArg = false
            }
        }

        uriString + argFrag
    }

    def buildGetRequest(uriString, params = [:], headers = [:]) {
        def builder = mapParams(
            MockMvcRequestBuilders.get(new URI(uriString))
                , params
        )
        addHeaders(builder, headers)
    }

    def buildPostRequest(uriString, params = [:], headers = [:]) {
        def builder = MockMvcRequestBuilders.post(new URI(uriString)).contentType(MediaType.APPLICATION_JSON)
        if (!params.isEmpty()) {
            builder = mapParams(builder, params)
        }
        addHeaders(builder, headers)
    }

    def buildPostRequest(uriString, String postJson, params = [:], headers = [:]) {
        def builder = mapParams(
            MockMvcRequestBuilders.post(new URI(uriString))
                    .content(postJson)
                    , params
        ).contentType(MediaType.APPLICATION_JSON)
        addHeaders(builder, headers)
    }

    def buildPutRequest(uriString, String putJson, params = [:], headers = [:]) {
        def builder = mapParams(
                MockMvcRequestBuilders.put(new URI(uriString)).content(putJson),
                params
        ).contentType(MediaType.APPLICATION_JSON)
        addHeaders(builder, headers)
    }

    def buildPutRequest(uriString, params = [:], headers = [:]) {
        def builder = MockMvcRequestBuilders.put(new URI(uriString)).contentType(MediaType.APPLICATION_JSON)
        if (!params.isEmpty()) {
            builder = mapParams(builder, params)
        }
        addHeaders(builder, headers)
    }

    def buildDeleteRequest(uriString, headers = [:]) {
        def builder = MockMvcRequestBuilders.delete(new URI(uriString))
        addHeaders(builder, headers)
    }

    def addHeaders(MockHttpServletRequestBuilder builder, headers) {
        if (!headers.isEmpty()){
            for(Map.Entry<String, String> entry: headers) {
                builder.header(entry.key, entry.value)
            }
        }
        builder
    }

    MockHttpServletRequestBuilder mapParams(MockHttpServletRequestBuilder builder, Map<String, String> map) {
        for ( e in map ) {
            builder.param(e.key, [e.value] as String[])
        }
        builder
    }

    /**
     * Deserialize the response
     */
    def <T> T convertResultToObject(MvcResult mvcResult, Class<T> resType) {
        (T) mappingJackson2JsonView.getObjectMapper().readValue(mvcResult.response.getContentAsString(), resType);
    }

    /**
     * Deserialize the response into a List
     */
    def <T> List<T> convertResultToObjectList(MvcResult mvcResult, Class<T> resType) {
        JavaType type = mappingJackson2JsonView.getObjectMapper().getTypeFactory().constructCollectionType(List.class, resType);
        (List<T>) mappingJackson2JsonView.getObjectMapper().readValue(mvcResult.response.getContentAsString(), type);
    }

    /**
     * Deserialize the response into a Pageable
     */
    def <T> PageDataDTO<T> convertResultToObjectPage(MvcResult mvcResult, Class<T> resType) {
        JavaType type = mappingJackson2JsonView.getObjectMapper().getTypeFactory().constructParametricType(PageDataDTO.class, resType);
        (PageDataDTO<T>) mappingJackson2JsonView.getObjectMapper().readValue(mvcResult.response.getContentAsString(), type);
    }

    def buildPageRequest(page, size) {
        PageRequest.of(page, size)
    }

    /**
     * Extract the {@link PageableDefault} from the specified method
     */
    def extractPageableDefaultFromMethod(Class tested, methodName, Class[] argTypes) {
        Method method = tested.getMethod(methodName, argTypes)
        method.getParameters()
        PageableDefault pageableDefault = null
        for (Parameter parameter: method.getParameters()) {
            if (Pageable.isAssignableFrom(parameter.type)) {
                pageableDefault = parameter.getAnnotation(PageableDefault)
                break
            }
        }
        return pageableDefault
    }

    def extractKeyValues(ValidationErrorDTO validationErrorDTO) {
        validationErrorDTO.fieldErrors.stream()
            .map{fieldError -> fieldError.name}
            .collect(Collectors.toSet())
    }

    void vaidatePageResult(PageDataDTO resPage, PageDataDTO expected, int page, int size, int totalCount) {
        assert resPage != null
        assert resPage.items == expected.items
        assert resPage.info == expected.info
        assert resPage.info.totalElements == expected.info.totalElements
        assert resPage.info.number == page
        assert resPage.info.size == size
        assert resPage.info.first == (page == 0)
        assert resPage.info.last == (page * size) >= (totalCount - size)
    }
}