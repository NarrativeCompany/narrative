package org.narrative.network.customizations.narrative.service.impl.common

import org.apache.commons.lang3.LocaleUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.context.MessageSource
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ValidationContextImplSpec extends Specification {
    @Shared Locale locale = LocaleUtils.toLocale("en_US")
    MessageSource messageSource = Mock(MessageSource)
    ValidationContextImpl spied = Spy(ValidationContextImpl, constructorArgs:[messageSource, locale]);

    def 'Test add validation error'() {
        given:
            def key = 'Some key'
            def translated = 'This is {some} template {message}'
            def interpolated = 'This is some interpolated message'
            def args = ['one', 'two']
        when:
            spied.addValidationError('someField1', key, ValidationContextImpl.ValidationErrorType.FIELD, args)
            spied.addValidationError('someField2', key, ValidationContextImpl.ValidationErrorType.FIELD, args)
            spied.addValidationError(StringUtils.EMPTY, key, ValidationContextImpl.ValidationErrorType.METHOD, args)
            spied.addValidationError(StringUtils.EMPTY, key, ValidationContextImpl.ValidationErrorType.METHOD, args)
        then:
            4 * messageSource.getMessage(key, null, key, locale) >> translated
            4 * spied.interpolateParameters(translated, args) >> interpolated
            spied.hasErrors()
            spied.fieldValidationErrors.size() == 2
            spied.getFieldValidationErrors().get(0).field == 'someField1'
            spied.getFieldValidationErrors().get(1).field == 'someField2'
            spied.methodValidationErrors.size() == 2
            spied.getMethodValidationErrors().get(0).field == StringUtils.EMPTY
            spied.getMethodValidationErrors().get(1).field == StringUtils.EMPTY
            spied.validationErrors.size() == 4
    }

    @Unroll
    def 'Test interpolate params message:#msg params:#params expect:#expect'() {
        when:
            def res = spied.interpolateParameters(msg, params.toArray())
        then:
            res == expect
        where:
            msg                             | params                 || expect
            'This is a parm-less message'   | []                     || 'This is a parm-less message'
            '{1} some {two} params {here}'  | ['val1','val2','val3'] || 'val1 some val2 params val3'
            '{1} some {two} params {here}'  | ['val1','val2']        || 'val1 some val2 params {here}'
            '{1} some {two} params {here}'  | []                     || '{1} some {two} params {here}'
    }

    def 'Test add all error types'() {
        given:
            def key = 'Some key'
            def translated = 'This is {some} template {message}'
        when:
            spied.addFieldError('someField1', key, 'one', 'two')
            spied.addFieldError('someField2', key)
            spied.addMethodError(key, 'one', 'two')
            spied.addMethodError(key)
        then:
            4 * messageSource.getMessage(key, null, key, locale) >> translated
            spied.hasErrors()
            spied.fieldValidationErrors.size() == 2
            spied.getFieldValidationErrors().get(0).field == 'someField1'
            spied.getFieldValidationErrors().get(1).field == 'someField2'
            spied.methodValidationErrors.size() == 2
            spied.getMethodValidationErrors().get(0).field == StringUtils.EMPTY
            spied.getMethodValidationErrors().get(1).field == StringUtils.EMPTY
            spied.validationErrors.size() == 4
    }

}
