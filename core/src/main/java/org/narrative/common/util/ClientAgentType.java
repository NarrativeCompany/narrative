package org.narrative.common.util;

import java.util.LinkedList;
import java.util.List;

/**
 * Date: Feb 28, 2006
 * Time: 7:50:52 AM
 *
 * @author Brian
 */
public enum ClientAgentType {
    /**
     * HTML Standard Client Agent Types.
     */
    // bl: by default, treat the "HTML Browser" default client agent as if it is a spider.
    HTML_DEFAULT("HTML Browser", null, true),
    HTML_NS("Netscape", HTML_DEFAULT, false),
    HTML_NS3UP("Netscape3+", HTML_NS, false),
    HTML_NS4UP("Netscape4+", HTML_NS3UP, false),
    HTML_NS6UP("Netscape6+", HTML_NS4UP, false),
    HTML_NS7UP("Netscape7+", HTML_NS6UP, false),
    HTML_NS8UP("Netscape8+", HTML_NS7UP, false),
    HTML_FIREFOX("Firefox", HTML_DEFAULT, false),
    HTML_FIREFOX10UP("Firefox1.0+", HTML_FIREFOX, false),
    HTML_FIREFOX15UP("Firefox1.5+", HTML_FIREFOX10UP, false),
    HTML_FIREFOX16UP("Firefox1.6+", HTML_FIREFOX15UP, false),
    HTML_IE("IE", HTML_DEFAULT, false),
    HTML_IE4UP("IE4+", HTML_IE, false),
    HTML_IE5UP("IE5+", HTML_IE4UP, false),
    HTML_IE6UP("IE6+", HTML_IE5UP, false),
    HTML_IE7UP("IE7+", HTML_IE6UP, false),
    HTML_IE8UP("IE8+", HTML_IE7UP, false),
    HTML_IE9UP("IE9+", HTML_IE8UP, false),
    HTML_IE10UP("IE10+", HTML_IE9UP, false),
    HTML_IE11UP("IE11+", HTML_IE10UP, false),
    HTML_OPERA("Opera", HTML_DEFAULT, false),
    HTML_OPERA5UP("Opera5+", HTML_OPERA, false),
    HTML_OPERA6UP("Opera6+", HTML_OPERA5UP, false),
    HTML_OPERA7UP("Opera7+", HTML_OPERA6UP, false),
    HTML_OPERA8UP("Opera8+", HTML_OPERA7UP, false),
    HTML_OPERA9UP("Opera9+", HTML_OPERA8UP, false),
    HTML_MOZILLA("Mozilla", HTML_DEFAULT, false),
    HTML_SAFARI("Safari", HTML_DEFAULT, false),
    HTML_SAFARI2UP("Safari2+", HTML_SAFARI, false),
    HTML_SAFARI3UP("Safari3+", HTML_SAFARI2UP, false),
    HTML_CHROME("Chrome", HTML_DEFAULT, false),
    HTML_OMNIWEB("OmniWeb", HTML_DEFAULT, false),
    HTML_KONQUEROR("Konqueror", HTML_DEFAULT, false),
    HTML_EPIPHANY("Epiphany", HTML_DEFAULT, false),
    HTML_EPIPHANY14UP("Epiphany14+", HTML_EPIPHANY, false),
    HTML_WEBTV("WebTV", HTML_DEFAULT, false),
    HTML_UC_BROWSER("UCBrowser", HTML_DEFAULT, false),
    // jw: Starting to see these show up in the logs, looks like a cross platform mobile development framework:
    //     http://www.appcelerator.com/titanium/
    HTML_APPCELERATOR("Appcelerator", HTML_DEFAULT, false),
    HTML_IPHONE_OS("WebKit.WebContent", HTML_DEFAULT, false),
    HTML_STAGE_FRIGHT("StageFright", HTML_DEFAULT, false),
    HTML_ANDROID_DOWNLOAD_MANAGER("AndroidDownloadManager", HTML_DEFAULT, false),
    HTML_FACEBOOK("Facebook External Hit", HTML_DEFAULT, true),
    HTML_GOOGLE_PLUS_SNIPPET("Google+ Snippet", HTML_DEFAULT, true),
    HTML_GOOGLE_ASSOC_SERVICE("Google Association Service", HTML_DEFAULT, true),
    HTML_TWITTERBOT("Twitterbot", HTML_DEFAULT, true),
    HTML_DOWNFOREVERYONEORJUSTME("downforeveryoneorjustme.com", HTML_DEFAULT, true),
    HTML_PINTEREST("Pinterest", HTML_DEFAULT, true),

    HTML_THREE_SIXTY_SPIDER("360Spider", HTML_DEFAULT, true),
    HTML_TRENDICTION_BOT("TrendictionBot", HTML_DEFAULT, true),
    HTML_DIFF_BOT("Diffbot", HTML_DEFAULT, true),
    HTML_BAIDU_BOT("Baidu", HTML_DEFAULT, true),
    HTML_NETSEER_BOT("NetSeer", HTML_DEFAULT, true),
    HTML_MSN_BOT("MsnBot", HTML_DEFAULT, true),
    HTML_GOOGLE_BOT("GoogleBot", HTML_DEFAULT, true),
    HTML_SOGOU_WEB_SPIDER("Sogou Web Spider", HTML_DEFAULT, true),
    HTML_LINKPAD_BOT("LinkpadBot", HTML_DEFAULT, true),
    HTML_SURVEY_BOT("SurveyBot", HTML_DEFAULT, true),
    HTML_GO_BOT("Go 1.1", HTML_DEFAULT, true),
    HTML_MICROSOFT_EXISTENCE_BOT("Microsoft Office Existence Discovery", HTML_DEFAULT, true),

    HTML_BASH_SCRIPT("Bin Bash", HTML_DEFAULT, true),
    HTML_JAVA_APP("Java", HTML_DEFAULT, true),
    FLASH("Shockwave Flash", HTML_DEFAULT, false),

    PRERENDER("prerender", HTML_DEFAULT, true);

    private final ClientAgentType parent;
    private final String description;
    private final boolean isSearchEngineSpider;

    /**
     * this is the constructor for HTML agent types
     *
     * @param description          The meaningful name of the agent type.
     * @param parent               The optional parent of the agent
     * @param isSearchEngineSpider Whether this agent is a search engine spider
     */
    private ClientAgentType(String description, ClientAgentType parent, boolean isSearchEngineSpider) {
        this.parent = parent;
        this.description = description;
        this.isSearchEngineSpider = isSearchEngineSpider;
    }

    public ClientAgentType getParent() {
        return parent;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSearchEngineSpider() {
        return isSearchEngineSpider;
    }

    public boolean isKnownSearchEngineSpider() {
        return isSearchEngineSpider() && !isDefault();
    }

    public boolean isDefault() {
        return this == HTML_DEFAULT;
    }

    public boolean isBypassRedirects() {
        // bl: let's always bypass the redirects for Facebook since it doesn't seem to follow them very well.
        // bl: also for downforeveryoneorjustme.com.
        // bl: also for Pinterest.
        // bl: also Googlebot (and the other Google variants).
        return isFacebook() || isGooglebot() || isGooglePlusSnippet() || isTwitterbot() || isClientAgentType(ClientAgentType.HTML_DOWNFOREVERYONEORJUSTME) || isClientAgentType(ClientAgentType.HTML_PINTEREST);
    }

    public boolean isInternetExplorer() {
        return isClientAgentType(ClientAgentType.HTML_IE);
    }

    public boolean isFirefox() {
        return isClientAgentType(ClientAgentType.HTML_FIREFOX);
    }

    public boolean isOpera() {
        return isClientAgentType(ClientAgentType.HTML_OPERA);
    }

    public boolean isSafari() {
        return isClientAgentType(ClientAgentType.HTML_SAFARI);
    }

    public boolean isSafari2() {
        return isSafari() && !isSafari3();
    }

    public boolean isSafari3() {
        return isClientAgentType(ClientAgentType.HTML_SAFARI3UP);
    }

    public boolean isChrome() {
        return isClientAgentType(ClientAgentType.HTML_CHROME);
    }

    public boolean isNetscape() {
        return isClientAgentType(ClientAgentType.HTML_NS);
    }

    public boolean isMozilla() {
        return isClientAgentType(ClientAgentType.HTML_MOZILLA);
    }

    public boolean isKonqueror() {
        return isClientAgentType(ClientAgentType.HTML_KONQUEROR);
    }

    public boolean isEpiphany() {
        return isClientAgentType(ClientAgentType.HTML_EPIPHANY);
    }

    public boolean isWebTv() {
        return isClientAgentType(ClientAgentType.HTML_WEBTV);
    }

    public boolean isFacebook() {
        return isClientAgentType(ClientAgentType.HTML_FACEBOOK);
    }

    public boolean isGooglePlusSnippet() {
        return isClientAgentType(ClientAgentType.HTML_GOOGLE_PLUS_SNIPPET);
    }

    public boolean isTwitterbot() {
        return isClientAgentType(ClientAgentType.HTML_TWITTERBOT);
    }

    public boolean isGooglebot() {
        return isClientAgentType(HTML_GOOGLE_BOT);
    }

    public boolean isInternetExplorer9OrLower() {
        return isInternetExplorer() && this != HTML_IE10UP && this != HTML_IE11UP;
    }

    public boolean isPrerender() {
        return isClientAgentType(PRERENDER);
    }

    public String toString() {
        return description;
    }

    public boolean isClientAgentType(ClientAgentType agentType) {
        List<ClientAgentType> supportedTypes = getSupportedClientAgentTypes(this);
        for (ClientAgentType type : supportedTypes) {
            if (type.equals(agentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return all agent types supported by this agent type.
     */
    public static List<ClientAgentType> getSupportedClientAgentTypes(ClientAgentType agentType) {
        List<ClientAgentType> supportedClientAgentTypes = new LinkedList<ClientAgentType>();
        supportedClientAgentTypes.add(agentType);
        ClientAgentType parent = agentType.getParent();
        while (parent != null) {
            supportedClientAgentTypes.add(parent);
            parent = parent.getParent();
        }
        return supportedClientAgentTypes;
    }
}
