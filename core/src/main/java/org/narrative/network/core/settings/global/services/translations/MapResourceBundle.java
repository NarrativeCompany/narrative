package org.narrative.network.core.settings.global.services.translations;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Date: 4/16/12
 * Time: 12:55 PM
 *
 * @author brian
 */
public class MapResourceBundle extends ResourceBundle {

    private final Locale locale;
    private final Map<String, String> map;

    public MapResourceBundle(Locale locale, Map<String, String> map) {
        this.locale = locale;
        this.map = map;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    protected Object handleGetObject(String key) {
        return map.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(map.keySet());
    }
}
