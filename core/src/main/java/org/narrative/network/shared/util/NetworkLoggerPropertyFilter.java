package org.narrative.network.shared.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import org.narrative.common.util.IPStringUtil;

import static org.narrative.network.core.user.PasswordFields.*;

/**
 * Custom property filter so we don't include sensitive values in JSON log output.
 * Also handles truncating long JSON values (e.g. post bodies).
 * Modeled loosely after RequestResponseHandler.getParametersString()
 *
 * Date: 9/30/18
 * Time: 8:43 PM
 *
 * @author brian
 */
public class NetworkLoggerPropertyFilter implements PropertyFilter {
    @Override
    public void serializeAsField(Object pojo, JsonGenerator gen, SerializerProvider prov, PropertyWriter writer) throws Exception {
        String strVal = getStringReplacement(writer, pojo);
        if(strVal!=null) {
            // bl: do our own custom serialization. write the field name, then write our placeholder string.
            gen.writeFieldName(writer.getName());
            gen.writeString(strVal);
        } else {
            writer.serializeAsField(pojo, gen, prov);
        }
    }

    @Override
    public void serializeAsElement(Object elementValue, JsonGenerator gen, SerializerProvider prov, PropertyWriter writer) throws Exception {
        String strVal = getStringReplacement(writer, elementValue);
        if(strVal!=null) {
            // bl: do our own custom serialization. write the field name, then write our placeholder string.
            gen.writeFieldName(writer.getName());
            gen.writeString(strVal);
        } else {
            writer.serializeAsElement(elementValue, gen, prov);
        }
    }

    @Override
    public void depositSchemaProperty(PropertyWriter writer, ObjectNode propertiesNode, SerializerProvider provider) throws JsonMappingException {
        if(!isSanitize(writer)) {
            //noinspection deprecation
            writer.depositSchemaProperty(propertiesNode, provider);
        }
    }

    @Override
    public void depositSchemaProperty(PropertyWriter writer, JsonObjectFormatVisitor objectVisitor, SerializerProvider provider) throws JsonMappingException {
        if(!isSanitize(writer)) {
            writer.depositSchemaProperty(objectVisitor, provider);
        }
    }

    private String getStringReplacement(PropertyWriter writer, Object pojo) {
        if(isSanitize(writer)) {
            return "****";
        }

        if(!(writer instanceof BeanPropertyWriter)) {
            return null;
        }

        Object val;
        try {
            BeanPropertyWriter beanPropertyWriter = (BeanPropertyWriter)writer;
            val = beanPropertyWriter.get(pojo);
        } catch (Exception e) {
            return null;
        }

        if(!(val instanceof String)) {
            return null;
        }

        String str = (String)val;
        if(str.length()<=100) {
            return null;
        }

        // bl: limit string values to 100 characters at most
        return IPStringUtil.getStringWithEllipsis(IPStringUtil.getTruncatedString(str, 100), false);
    }

    private boolean isSanitize(PropertyWriter writer) {
        return writer.getName().toLowerCase().contains(PASSWORD_PARAM);
    }
}
