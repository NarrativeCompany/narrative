package org.narrative.common.util.html;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UrlUtil;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 12/14/14
 * Time: 9:58 AM
 */
public class HTMLMediaWhiteListDomainParser extends HTMLEmbedCodeParserBase {
    private final Set<String> whiteListDomains;
    private boolean foundNonWhiteListDomain = false;

    private static final List<String> PRIMARY_PARSABLE_ELEMENTS = Collections.unmodifiableList(Arrays.asList(EMBED__ELEMENT_NAME, IFRAME__ELEMENT_NAME, OBJECT__ELEMENT_NAME, IMG__ELEMENT_NAME, AUDIO__ELEMENT_NAME, VIDEO__ELEMENT_NAME, SOURCE__ELEMENT_NAME));

    private HTMLMediaWhiteListDomainParser(String html, String areaDomain, String whiteList) {
        super(html, PRIMARY_PARSABLE_ELEMENTS);
        Set<String> whiteListDomains = newHashSet(IPStringUtil.getArrayFromDelimitedString(whiteList));
        whiteListDomains.add(areaDomain);

        this.whiteListDomains = Collections.unmodifiableSet(whiteListDomains);
    }

    @Override
    protected boolean continueProcessing() {
        // jw: stop processing as soon as we have found a single inline element with an unknown domain.
        return !foundNonWhiteListDomain;
    }

    @Override
    protected void addResult(String src, BigDecimal width, BigDecimal height, boolean iframe, String flashvars) {
        // jw: we are supporting relative src references in this parser, but the getDomainFromUrl does not, so lets add
        //     http: to the front if it is a relative protocol src.
        if (src.startsWith("//")) {
            src = "http:" + src;
        }
        String domain = UrlUtil.getDomainFromUrl(src);

        foundNonWhiteListDomain = !whiteListDomains.contains(domain);
    }

    public static boolean containsNonWhiteListedDomain(String html, String areaDomain, String domainWhiteList) {
        if (isEmpty(html)) {
            return false;
        }
        HTMLMediaWhiteListDomainParser parser = new HTMLMediaWhiteListDomainParser(html, areaDomain, domainWhiteList);
        parser.parse();

        return parser.foundNonWhiteListDomain;
    }

    public static void main(String[] args) {
        String[] htmls = new String[]{"<iframe width=\"480\" height=\"270\" src=\"http://www.youtube.com/embed/H_7BDgNY41w?feature=oembed&amp;wmode=opaque\" frameborder=\"0\"></iframe> <img src=\"http://thumb9.shutterstock.com/display_pic_with_logo/260188/214456996/stock-photo--cattails-in-carolyn-holmberg-preserve-in-broomfield-colorado-214456996.jpg\" />", "<img src=\"http://jonmark.dev/imaginary_image.jpg\" width=\"50\" height=\"100\" /> <iframe width=\"480\" height=\"270\" src=\"http://www.youtube.com/embed/H_7BDgNY41w?feature=oembed&amp;wmode=opaque\" frameborder=\"0\"></iframe>", "<iframe src=\"//player.vimeo.com/video/101957670\" width=\"500\" height=\"271\" frameborder=\"0\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe> <p><a href=\"http://vimeo.com/101957670\">Horde</a> from <a href=\"http://vimeo.com/brutuscollective\">BRUTUS collective</a> on <a href=\"https://vimeo.com\">Vimeo</a>.</p> <img src=\"http://jonmark.dev/imaginary_image.jpg\" width=\"50\" height=\"100\" />"};

        String areaDomain = "jonmark.dev";
        String domainWhiteList = "brian.dev www.youtube.com";

        for (String html : htmls) {
            System.out.println("\nProcessing: " + html);
            boolean containsNonWhiteListedDomain = containsNonWhiteListedDomain(html, areaDomain, domainWhiteList);
            System.out.println(containsNonWhiteListedDomain ? "Contains Non-Whitelisted Domain" : "Does Not Contain Non-Whitelisted Domain");
            System.out.println();
        }
    }
}
