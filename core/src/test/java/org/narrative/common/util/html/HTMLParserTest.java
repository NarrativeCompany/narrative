package org.narrative.common.util.html;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * Date: 2019-01-22
 * Time: 08:13
 *
 * @author brian
 */
public class HTMLParserTest {

    public static final String FROALA_TEST_HTML = "<p><span class=\"fr-inner\"><span class=\"fr-img-caption fr-fic fr-dib fr-fil\" style=\"width: 300px;\"><span class=\"fr-img-wrap\"><img src=\"https://local-images.narrative.org/JonmarkWeber/images/79990031743843193/minithumbnail-15x15-ghost.gif\"><span class=\"fr-inner\">Image Caption<br><br></span></span></span></span></p>";

    // Test names should follow the standard: MethodName_StateUnderTest_ExpectedBehavior
    @Test
    void parse_ImgNotClosed_returnsSelfClosing() {
        String html = "<img src=\"test\">";
        String output = new HTMLParser(html, HTMLParser.FragmentType.BODY).parse();
        assertEquals(html, output);
    }

    @Test
    void parse_ImgNotClosed_returnsExpected() {
        String output = new HTMLParser(FROALA_TEST_HTML, HTMLParser.FragmentType.BODY).parse();
        //String expected = "<p><span class=\"fr-inner\"><span class=\"fr-img-caption fr-fic fr-dib fr-fil\" style=\"width: 300px;\"><span class=\"fr-img-wrap\"><img src=\"https://local-images.narrative.org/JonmarkWeber/images/79990031743843193/minithumbnail-15x15-ghost.gif\"></img><span class=\"fr-inner\">Image Caption<br /><br /></span></span></span></span></p>";
        assertEquals(FROALA_TEST_HTML, output);
    }

    @Test
    void parse_ImgSelfClosed_returnsExpected() {
        String html = "<p><span class=\"fr-inner\"><span class=\"fr-img-caption fr-fic fr-dib fr-fil\" style=\"width: 300px;\"><span class=\"fr-img-wrap\"><img src=\"https://local-images.narrative.org/JonmarkWeber/images/79990031743843193/minithumbnail-15x15-ghost.gif\"/><span class=\"fr-inner\">Image Caption<br><br></span></span></span></span></p>";
        String output = new HTMLParser(html, HTMLParser.FragmentType.BODY).parse();
        assertEquals(FROALA_TEST_HTML, output);
    }
}
