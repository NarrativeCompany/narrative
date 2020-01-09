package org.narrative.common.util.html;

import org.narrative.common.util.NarrativeLogger;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/31/14
 * Time: 1:26 PM
 */
public class HTMLEmbedCodeParser extends HTMLEmbedCodeParserBase {
    private static final NarrativeLogger logger = new NarrativeLogger(HTMLEmbedCodeParser.class);

    private String src;
    private BigDecimal width = null;
    private BigDecimal height = null;
    private boolean iframe = false;
    private String flashvars = null;

    private boolean foundResult;

    private static final List<String> PRIMARY_PARSABLE_ELEMENTS = Collections.unmodifiableList(Arrays.asList(EMBED__ELEMENT_NAME, IFRAME__ELEMENT_NAME, OBJECT__ELEMENT_NAME));

    private static final int DEFAULT_WIDTH = 560;
    private static final int DEFAULT_HEIGHT = 315;

    private HTMLEmbedCodeParser(String embedCode) {
        super(embedCode, PRIMARY_PARSABLE_ELEMENTS);
    }

    @Override
    protected boolean continueProcessing() {
        return !foundResult;
    }

    @Override
    protected void addResult(String src, BigDecimal width, BigDecimal height, boolean iframe, String flashvars) {
        this.src = src;
        if (width != null) {
            this.width = width;
        }
        if (height != null) {
            this.height = height;
        }
        this.iframe = iframe;
        this.flashvars = flashvars;
        foundResult = true;
    }

    public static class EmbedCodeDetails {
        private final String src;
        private final BigDecimal width;
        private final BigDecimal height;
        private final boolean iframe;
        private final String flashvars;

        private EmbedCodeDetails(String src, BigDecimal width, BigDecimal height, boolean iframe, String flashvars) {
            this.src = src;
            this.width = width;
            this.height = height;
            this.iframe = iframe;
            this.flashvars = flashvars;
        }

        public String getSrc() {
            return src;
        }

        public BigDecimal getWidth() {
            return width;
        }

        public BigDecimal getHeight() {
            return height;
        }

        public boolean isIframe() {
            return iframe;
        }

        public String getFlashvars() {
            return flashvars;
        }
    }

    public static EmbedCodeDetails parse(String embedCode) {
        if (isEmpty(embedCode)) {
            return null;
        }
        HTMLEmbedCodeParser parser = new HTMLEmbedCodeParser(embedCode);
        parser.parse();

        if (!parser.foundResult) {
            return null;
        }

        return new EmbedCodeDetails(parser.src, getDimension(parser.width, DEFAULT_WIDTH), getDimension(parser.height, DEFAULT_HEIGHT), parser.iframe, parser.flashvars);
    }

    private static BigDecimal getDimension(BigDecimal value, int defaultValue) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.valueOf(defaultValue);
        }

        return value;
    }

    public static void main(String[] args) {
        String[] embeds = new String[]{"<iframe width=\"480\" height=\"270\" src=\"http://www.youtube.com/embed/H_7BDgNY41w?feature=oembed&amp;wmode=opaque\" frameborder=\"0\"></iframe>", "<object style=\"height: 390px; width: 640px\"><param name=\"movie\" value=\"http://www.youtube.com/v/PTsulAaFySA?version=3&amp;feature=player_detailpage\"></param><param name=\"allowFullScreen\" value=\"false\"></param><param name=\"allowScriptAccess\" value=\"never\"></param><embed wmode=\"opaque\" src=\"http://www.youtube.com/v/PTsulAaFySA?version=3&amp;feature=player_detailpage\" type=\"application/x-shockwave-flash\" allowfullscreen=\"false\" allowscriptaccess=\"never\" width=\"640\" height=\"360\"></embed> <param name=\"wmode\" value=\"opaque\"></param></object>", "<iframe src=\"//player.vimeo.com/video/101957670\" width=\"500\" height=\"271\" frameborder=\"0\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe> <p><a href=\"http://vimeo.com/101957670\">Horde</a> from <a href=\"http://vimeo.com/brutuscollective\">BRUTUS collective</a> on <a href=\"https://vimeo.com\">Vimeo</a>.</p>"};

        for (String embed : embeds) {
            System.out.println("Processing: " + embed);
            EmbedCodeDetails details = parse(embed);
            if (details == null) {
                System.out.println("Unable To Parse");
            } else {
                System.out.println("src: " + details.getSrc());
                System.out.println("width: " + details.getWidth());
                System.out.println("height: " + details.getHeight());
                System.out.println("iframe: " + details.isIframe());
                System.out.println("flashvars: " + details.getFlashvars());
            }

            System.out.println();
        }
    }
}
