package org.narrative.common.util.posting;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * Date: 2019-06-26
 * Time: 13:08
 *
 * @author brian
 */
public class AnchorTextMassagerTest {
    @Test
    void test_simpleUrl_autoLinked() {
        String url = "https://www.google.com";
        String output = AnchorTextMassager.applyAutoLinkAnchorReplacements(url);
        assertEquals("<a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a>", output);
    }

    @Test
    void test_complexUrl_autoLinked() {
        String url = "https://www.google.com/asdf,1234!asdf?qwerty=123";
        String output = AnchorTextMassager.applyAutoLinkAnchorReplacements(url);
        assertEquals("<a href=\"https://www.google.com/asdf,1234!asdf?qwerty=123\" target=\"_blank\">https://www.google.com/asdf,1234!asdf?qwerty=123</a>", output);
    }

    @Test
    void test_urlEndingWithSpace_spaceExcluded() {
        String text = "https://www.google.com a";
        String output = AnchorTextMassager.applyAutoLinkAnchorReplacements(text);
        assertEquals("<a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a> a", output);
    }

    @Test
    void test_urlWrappedInNewlines_newlinesExcluded() {
        String text = "a\nhttps://www.google.com\nb";
        String output = AnchorTextMassager.applyAutoLinkAnchorReplacements(text);
        // bl: newlines get converted to spaces
        assertEquals("a <a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a> b", output);
    }

    @Test
    void test_wrappedUrl_elementExcluded() {
        String text = "<p>https://www.google.com</p>";
        String output = AnchorTextMassager.applyAutoLinkAnchorReplacements(text);
        assertEquals("<p><a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a></p>", output);
    }

    @Test
    void test_urlEndingWithPeriod_periodExcluded() {
        String text = "https://www.google.com.";
        String output = AnchorTextMassager.applyAutoLinkAnchorReplacements(text);
        assertEquals("<a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a>.", output);
    }

    @Test
    void test_urlEndingWithComma_commaExcluded() {
        String text = "https://www.google.com,";
        String output = AnchorTextMassager.applyAutoLinkAnchorReplacements(text);
        assertEquals("<a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a>,", output);
    }

    @Test
    void test_urlEndingWithParen_parenExcluded() {
        String text = "https://www.google.com)";
        String output = AnchorTextMassager.applyAutoLinkAnchorReplacements(text);
        assertEquals("<a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a>)", output);
    }

    @Test
    void test_urlEndingWithPeriodParen_symbolsExcluded() {
        String text = "https://www.google.com.)";
        String output = AnchorTextMassager.applyAutoLinkAnchorReplacements(text);
        assertEquals("<a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a>.)", output);
    }
}
