package org.narrative.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.servlet.http.HttpServletRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The purpose of this class is to provide information re. the nature of
 * the 'Client Agent'.
 * <p>
 * Life-cycle:
 * - Typically created and stored along with the Client Http Session. If there is
 * a need for ClientAgentInformation before this (e.g. reporting errors when
 * creating SessionValue) then clients are expected to create a ClientAgentInformation
 * on the fly.
 * <p>
 * OpenTopic Specific:
 * - A factory method exists in getClientBrowserInformation()
 * that should be used. This method guards against an invalid SessionValue etc.
 * - Client code can also set extra 'custom' parameters for this specific client.
 * This is useful if client code wants to record additional params at run time.
 * e.g. the HDML redirect solution.
 *
 * @author Carl Whitehead.
 */
public class ClientAgentInformation {

    private static final NarrativeLogger logger = new NarrativeLogger(ClientAgentInformation.class);

    @JsonIgnore
    public boolean isMac() {
        return isMac;
    }

    @JsonIgnore
    public boolean isIPad() {
        return isIPad;
    }

    @JsonIgnore
    public boolean isIPhone() {
        return isIPhone;
    }

    @JsonIgnore
    public boolean isMobile() {
        return isMobile;
    }

    @JsonIgnore
    public boolean isTablet() {
        return isTablet;
    }

    @JsonIgnore
    public boolean isMacFirefox() {
        return isMac && clientAgentType.isFirefox();
    }

    /**
     * Get the client agent type.
     */
    public ClientAgentType getClientAgentType() {
        return clientAgentType;
    }

    public String getUserAgentString() {
        return userAgentString;
    }

    public ClientVersion getClientVersion() {
        return clientVersion;
    }

    public MimeType getPreferredMimeType() {
        return preferredMimeType;
    }

    /**
     * determine if this client supports the wysiwyg editor.  very display specific,
     * but i can't think of a better place for this code to go right now
     *
     * @return true if the client supports the wysiwyg editor.  false if it does not.
     */
    @JsonIgnore
    public boolean isSupportsWysiwygEditor() {
        if (isMobile()) {
            return false;
        }
        // bl: don't want the Kindle Fire's Silk browser to use the WYSIWYG editor since it has issues.
        if (isKindleFireSilk) {
            return false;
        }
        if (isClientAgentType(ClientAgentType.HTML_SAFARI3UP)) {
            return true;
        }
        if (isClientAgentType(ClientAgentType.HTML_CHROME)) {
            return true;
        }
        if (isClientAgentType(ClientAgentType.HTML_SAFARI)) {
            return false;
        }
        if (isClientAgentType(ClientAgentType.HTML_EPIPHANY14UP)) {
            return false;
        }
        if (isClientAgentType(ClientAgentType.HTML_OPERA)) {
            return false;
        }
        if (isClientAgentType(ClientAgentType.HTML_IE5UP)) {
            return true;
        }
        if (isClientAgentType(ClientAgentType.HTML_FIREFOX)) {
            return true;
        }
        if (isClientAgentType(ClientAgentType.HTML_NS7UP)) {
            return true;
        }
        if (isClientAgentType(ClientAgentType.HTML_MOZILLA)) {
            return true;
        }
        return false;
    }

    @JsonIgnore
    public boolean isUseManualFileSelection() {
        // bl: we need to use a lame-o manual file selection process for IE9 and below and the Kindle Fire Silk browser.
        return isKindleFireSilk || getClientAgentType().isInternetExplorer9OrLower();
    }

    /**
     * Return all agent types supported by this agent instance.
     * Note: From base agent to most precise recognized agent type.
     */
    public List<ClientAgentType> getSupportedClientAgentTypes() {
        return ClientAgentType.getSupportedClientAgentTypes(getClientAgentType());
    }

    /**
     * Get the MIME type required by this client browser.
     */

    /**
     * Is this client agenr considered to be the supplied agent type ?
     * Note: Uses the agent hierarchy.
     */
    public boolean isClientAgentType(ClientAgentType agentType) {
        return getClientAgentType().isClientAgentType(agentType);
    }

    /**
     * Does this client agent support the supplied mimeType?
     */
    public boolean supportsMimeType(MimeType mimeType) {
        if (mimeType == null) {
            Debug.assertMsg(logger, false, "A mime type must be supplied!");
        }
        return supportedMimeTypes.contains(mimeType);
    }

    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Client Agent Definition");
        pw.println("Agent Type     : " + getClientAgentType());
        pw.println("Pref MIME type : " + preferredMimeType);
        pw.println("Major Verson   : " + clientVersion.majorVersion);
        pw.println("Minor Version  : " + clientVersion.minorVersion);
        pw.println("Beta Version   : " + clientVersion.betaVersion);
        pw.println("User Agent Str : " + userAgentString);
        pw.println("Agent Accepts  : " + userAgentAcceptsString);
        pw.println("Supported Types: (" + IPStringUtil.getCommaSeparatedList(getSupportedClientAgentTypes()) + ")");
        return sw.toString();
    }

    /**
     * Constructor.
     *
     * @param req Mandatory.
     */
    public ClientAgentInformation(HttpServletRequest req) {
        this(req.getHeader("user-agent"), req.getHeader("accept"));
    }

    public ClientAgentInformation(String userAgentValue) {
        this(userAgentValue, null);
    }

    @JsonCreator
    private ClientAgentInformation(@JsonProperty("userAgentString") String userAgentValue,
                                   @JsonProperty("userAgentAcceptsString") String acceptValue) {
        if(userAgentValue==null) {
            userAgentValue = "";
        }
        userAgentValue = userAgentValue.toLowerCase();

        userAgentString = userAgentValue;
        userAgentAcceptsString = acceptValue;
        uAgentInfo = new UAgentInfo(userAgentValue, acceptValue);

        // Step 1. Supported MIME types.
        Set<MimeType> supportedMimeTypes = new HashSet<MimeType>();
        if (acceptValue != null) {
            if (acceptValue.contains(MimeType.ALL.getMimeTypeString())) {
                supportedMimeTypes.add(MimeType.ALL);
            } else {
                for (MimeType mimeType : MimeType.values()) {
                    if (acceptValue.contains(mimeType.getMimeTypeString())) {
                        supportedMimeTypes.add(mimeType);
                    }
                }
            }
        }

        isMac = (userAgentString.indexOf("macintosh") != -1 || userAgentString.indexOf("mac os x") != -1);
        //isIOS = userAgentValue.contains("iphone") || userAgentValue.contains("ipad") || userAgentValue.contains("mobilesafari");
        isIOS = uAgentInfo.detectIos();
        isIPhone = uAgentInfo.detectIphone();
        isIPad = uAgentInfo.detectIpad();
        //isAndroid = userAgentValue.contains("android");
        isAndroid = uAgentInfo.detectAndroid();
        isKindleFireSilk = uAgentInfo.detectKindleFireSilk();
        isMobile = uAgentInfo.detectMobileQuick();
        isTablet = uAgentInfo.detectTierTablet();

        if (supportedMimeTypes.isEmpty()) {
            // no information, so we'll just assume HTML by default
            supportedMimeTypes.add(MimeType.HTML);
        }

        this.supportedMimeTypes = Collections.unmodifiableSet(supportedMimeTypes);

        // Ok... some form of HTML client then
        preferredMimeType = MimeType.HTML;

        // Step 2.1 Deterimine the agent versions
        ClientVersion defaultClientVersion = getClientVersionFromAgentString(userAgentValue, true);

        // Step 2.2 Determine additional agent types
        // jw: start with know spiders that store cookies!  We need to start with these because some of them include
        //     normal client agents that try to trick our servers into thinking they are chrome or other browsers.

        // bl: detect the mobile apps first and foremost!
        if (userAgentValue.contains("360spider")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_THREE_SIXTY_SPIDER;

            // Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.0; trendictionbot0.5.0; Gecko/20071127 Firefox/3.0.0.11
        } else if (userAgentValue.contains("trendictionbot")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_TRENDICTION_BOT;

            // Mozilla/5.0 (compatible; Diffbot/0.1; +http://www.diffbot.com)
        } else if (userAgentValue.contains("diffbot")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_DIFF_BOT;

            // Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)
        } else if (userAgentValue.contains("baidu")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_BAIDU_BOT;

            // Mozilla/5.0 (compatible; NetSeer crawler/2.0; +http://www.netseer.com/crawler.html; crawler@netseer.com)
        } else if (userAgentValue.contains("netseer")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_NETSEER_BOT;

            // msnbot/2.0b (+http://search.msn.com/msnbot.htm)
        } else if (userAgentValue.contains("msnbot")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_MSN_BOT;

            // Mozilla/5.0 (compatible; Googlebot/2.1; compatible; Googlebot/2.0; +http://www.google.com/bot.html)
            // Google variants: https://support.google.com/webmasters/answer/1061943?hl=en
        } else if (userAgentValue.contains("googlebot") || userAgentValue.contains("adsbot-google") || userAgentValue.contains("mediapartners-google") || userAgentValue.contains("apis-google")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_GOOGLE_BOT;

            // Sogou web spider/4.0(+http://www.sogou.com/docs/help/webmasters.htm#07)
        } else if (userAgentValue.contains("sogou web spider")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_SOGOU_WEB_SPIDER;

            // Mozilla/5.0 (compatible; LinkpadBot/1.06; +http://www.linkpad.ru)
        } else if (userAgentValue.contains("linkpadbot")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_LINKPAD_BOT;

            // Mozilla/5.0 (Windows; U; Windows NT 5.1; en; rv:1.9.0.13) Gecko/2009073022 Firefox/3.5.2 (.NET CLR 3.5.30729) SurveyBot/2.3 (DomainTools)
        } else if (userAgentValue.contains("surveybot")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_SURVEY_BOT;

            // () { foo;};echo; /bin/bash -c "cat /etc/passwd"
        } else if (userAgentValue.contains("/bin/bash")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_BASH_SCRIPT;

            // com.apple.WebKit.WebContent/10600.1.25 CFNetwork/720.0.9 Darwin/14.0.0 (x86_64)
            // com.apple.WebKit.Networking/9537.78.2 CFNetwork/673.4 Darwin/13.3.0 (x86_64) (MacBookPro9%2C2)
        } else if (userAgentValue.contains("webkit.webcontent") || userAgentValue.contains("webkit.networking")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_IPHONE_OS;

            // Go 1.1 package http
        } else if (userAgentValue.contains("go") && userAgentValue.contains("package http")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_GO_BOT;

            // Microsoft Office Existence Discovery
        } else if (userAgentValue.contains("microsoft office existence discovery")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_MICROSOFT_EXISTENCE_BOT;

        } else if (userAgentValue.contains("firefox")) {
            // Mozilla/5.0 (X11; U; SunOS i86pc; en-US; rv:1.8) Gecko/20051130 Firefox/1.5
            String versionString = IPStringUtil.getStringAfterLastIndexOf(userAgentValue, "firefox");
            clientVersion = getClientVersionFromAgentString(versionString, false);
            if (clientVersion.majorVersion >= 1 && clientVersion.minorVersion >= 6) {
                clientAgentType = ClientAgentType.HTML_FIREFOX16UP;
            } else if (clientVersion.majorVersion >= 1 && clientVersion.minorVersion >= 5) {
                clientAgentType = ClientAgentType.HTML_FIREFOX15UP;
            } else if (clientVersion.majorVersion >= 1) {
                clientAgentType = ClientAgentType.HTML_FIREFOX10UP;
            } else {
                clientAgentType = ClientAgentType.HTML_FIREFOX;
            }
            // Opera Browsers (Nb. Before IE as includes MSIE in UAS and Mozilla
        } else if (userAgentValue.contains("opera")) {
            String versionString = IPStringUtil.getStringAfterLastIndexOf(userAgentValue, "opera");
            clientVersion = getClientVersionFromAgentString(versionString, false);
            if ((userAgentValue.contains("opera 5")) || (userAgentValue.contains("opera/5"))) {
                clientAgentType = ClientAgentType.HTML_OPERA5UP;
            } else if ((userAgentValue.contains("opera 6")) || (userAgentValue.contains("opera/6"))) {
                clientAgentType = ClientAgentType.HTML_OPERA6UP;
            } else if ((userAgentValue.contains("opera 7")) || (userAgentValue.contains("opera/7"))) {
                clientAgentType = ClientAgentType.HTML_OPERA7UP;
            } else if ((userAgentValue.contains("opera 8")) || (userAgentValue.contains("opera/8"))) {
                clientAgentType = ClientAgentType.HTML_OPERA8UP;
            } else if ((userAgentValue.contains("opera 9")) || (userAgentValue.contains("opera/9"))) {
                clientAgentType = ClientAgentType.HTML_OPERA9UP;
            } else {
                clientAgentType = ClientAgentType.HTML_OPERA;
            }
        } else if (userAgentValue.contains("omniweb")) {
            // Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US) AppleWebKit/125.4 (KHTML, like Gecko, Safari) OmniWeb/v563.34
            String versionString = IPStringUtil.getStringAfterLastIndexOf(userAgentValue, "omniweb");
            clientVersion = getClientVersionFromAgentString(versionString, false);
            clientAgentType = ClientAgentType.HTML_OMNIWEB;
        } else if (userAgentValue.contains("chrome")) {
            // Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.6 (KHTML, like Gecko) Chrome/16.0.897.0 Safari/535.6
            String versionString = IPStringUtil.getStringAfterLastIndexOf(userAgentValue, "chrome");
            clientVersion = getClientVersionFromAgentString(versionString, false);
            clientAgentType = ClientAgentType.HTML_CHROME;
        } else if (userAgentValue.contains("safari")) {
            // Safari 1: Mozilla/5.0 (Macintosh; U; PPC Mac OS X; fr-fr) AppleWebKit/312.5.1 (KHTML, like Gecko) Safari/312.3.1
            // Safari 2: Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/417.9 (KHTML, like Gecko) Safari/417.8
            // Safari 3: Mozilla/5.0 (Windows; U; Windows NT 5.1; ru) AppleWebKit/522.11.3 (KHTML, like Gecko) Version/3.0 Safari/522.11.3
            String versionString = IPStringUtil.getStringAfterLastIndexOf(userAgentValue, "safari");
            clientVersion = getClientVersionFromAgentString(versionString, false);
            if (clientVersion.majorVersion >= 500) {
                clientAgentType = ClientAgentType.HTML_SAFARI3UP;
            } else if (clientVersion.majorVersion >= 400) {
                clientAgentType = ClientAgentType.HTML_SAFARI2UP;
            } else {
                clientAgentType = ClientAgentType.HTML_SAFARI;
            }
        } else if (userAgentValue.contains("konqueror")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_KONQUEROR;

            // Appcelerator Titanium/3.2.3.GA (iPad/7.1.1; iPhone OS; en_US;)
        } else if (userAgentValue.contains("appcelerator")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_APPCELERATOR;

            // UCWEB/2.0 (MIDP-2.0; U; Adr 4.4.4; en-US; XT1033) U2/1.0.0 UCBrowser/9.4.0.460 U2/1.0.0 Mobile
        } else if (userAgentValue.contains("ucbrowser")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_UC_BROWSER;

            // stagefright/1.2 (Linux;Android 4.4.2)
        } else if (userAgentValue.contains("stagefright")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_STAGE_FRIGHT;

            // AndroidDownloadManager
        } else if (userAgentValue.contains("androiddownloadmanager")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_ANDROID_DOWNLOAD_MANAGER;

        } else if (userAgentValue.contains("epiphany")) {
            String versionString = IPStringUtil.getStringAfterLastIndexOf(userAgentValue, "epiphany");
            clientVersion = getClientVersionFromAgentString(versionString, false);
            if (clientVersion.majorVersion >= 1 && clientVersion.minorVersion >= 4) {
                clientAgentType = ClientAgentType.HTML_EPIPHANY14UP;
            } else {
                clientAgentType = ClientAgentType.HTML_EPIPHANY;
            }
            // Web TV
        } else if (userAgentValue.contains("webtv")) {
            String versionString = IPStringUtil.getStringAfterLastIndexOf(userAgentValue, "webtv");
            clientVersion = getClientVersionFromAgentString(versionString, false);
            clientAgentType = ClientAgentType.HTML_WEBTV;
            // Netscape Browsers
        } else if (userAgentValue.contains("mozilla") && !userAgentValue.contains("spoofer") && !userAgentValue.contains("compatible") && !userAgentValue.contains("hotjava")) {

            // Mozilla ? (includes gecko and no netscape)
            if (userAgentValue.contains("gecko") && !userAgentValue.contains("netscape")) {
                clientVersion = defaultClientVersion;
                clientAgentType = ClientAgentType.HTML_MOZILLA;
            } else {
                String versionString = IPStringUtil.getStringAfterLastIndexOf(userAgentValue, "netscape");
                clientVersion = getClientVersionFromAgentString(versionString, false);

                // Regular NS
                if (clientVersion.majorVersion >= 8) {
                    clientAgentType = ClientAgentType.HTML_NS8UP;
                } else if (clientVersion.majorVersion >= 7) {
                    clientAgentType = ClientAgentType.HTML_NS7UP;
                } else if (clientVersion.majorVersion >= 6) {
                    clientAgentType = ClientAgentType.HTML_NS6UP;
                } else if (clientVersion.majorVersion >= 4) {
                    clientAgentType = ClientAgentType.HTML_NS4UP;
                } else if (clientVersion.majorVersion >= 3) {
                    clientAgentType = ClientAgentType.HTML_NS3UP;
                } else {
                    clientAgentType = ClientAgentType.HTML_NS;
                }
            }
            // IE Browsers
        } else if (userAgentValue.contains("msie") || userAgentValue.contains("microsoft internet explorer")) {
            clientVersion = defaultClientVersion;

            if (clientVersion.majorVersion >= 11) {
                clientAgentType = ClientAgentType.HTML_IE11UP;
            } else if (clientVersion.majorVersion >= 10) {
                clientAgentType = ClientAgentType.HTML_IE10UP;
            } else if (clientVersion.majorVersion >= 9) {
                clientAgentType = ClientAgentType.HTML_IE9UP;
            } else if (clientVersion.majorVersion >= 8) {
                clientAgentType = ClientAgentType.HTML_IE8UP;
            } else if (clientVersion.majorVersion >= 7) {
                clientAgentType = ClientAgentType.HTML_IE7UP;
            } else if (clientVersion.majorVersion >= 6) {
                clientAgentType = ClientAgentType.HTML_IE6UP;
            } else if (clientVersion.majorVersion >= 5) {
                clientAgentType = ClientAgentType.HTML_IE5UP;
            } else if (clientVersion.majorVersion >= 4) {
                clientAgentType = ClientAgentType.HTML_IE4UP;
            } else {
                clientAgentType = ClientAgentType.HTML_IE;
            }
        } else if (userAgentValue.contains("flash")) {
            clientAgentType = ClientAgentType.FLASH;
            clientVersion = defaultClientVersion;
        } else if (userAgentValue.contains("coremedia") || userAgentValue.contains("quicktime")) {
            clientAgentType = ClientAgentType.HTML_SAFARI3UP;
            clientVersion = defaultClientVersion;
        } else if (userAgentValue.contains("facebookexternalhit") || userAgentValue.contains("facebot")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_FACEBOOK;

            // jw: per: https://developers.google.com/+/web/snippet/#faq-snippet-useragent
        } else if (userAgentValue.contains("(+https://developers.google.com/+/web/snippet/)")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_GOOGLE_PLUS_SNIPPET;

        } else if (userAgentValue.contains("googleassociationservice")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_GOOGLE_ASSOC_SERVICE;

        } else if (userAgentValue.contains("twitterbot")) {
            // refer: https://dev.twitter.com/cards/getting-started#crawling
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_TWITTERBOT;

        } else if (userAgentValue.contains("downforeveryoneorjustme")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_DOWNFOREVERYONEORJUSTME;
        } else if (userAgentValue.contains("pinterest")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_PINTEREST;

            // Java/1.6.0_26
        } else if (userAgentValue.contains("java/")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.HTML_JAVA_APP;

        } else if (userAgentValue.contains("prerender")) {
            clientVersion = defaultClientVersion;
            clientAgentType = ClientAgentType.PRERENDER;

        } else {
            clientVersion = defaultClientVersion;

            // default to HTML default if we didn't recognize the agent string
            clientAgentType = ClientAgentType.HTML_DEFAULT;
        }
    }

    public static void main(String[] args) {
        // good place to get user-agent strings:
        // http://www.zytrax.com/tech/web/browser_ids.htm
        Map<ClientAgentInformation, Object[]> cais = new HashMap<ClientAgentInformation, Object[]>();
        // Chrome
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.6 (KHTML, like Gecko) Chrome/16.0.897.0 Safari/535.6"), new Object[]{ClientAgentType.HTML_CHROME, new ClientVersion(16, 0, null)});
        // safari 3.0 (500+)
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru) AppleWebKit/522.11.3 (KHTML, like Gecko) Version/3.0 Safari/522.11.3"), new Object[]{ClientAgentType.HTML_SAFARI3UP, new ClientVersion(522, 11, null)});
        // safari 2.0 (400+)
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/417.9 (KHTML, like Gecko) Safari/417.8"), new Object[]{ClientAgentType.HTML_SAFARI2UP, new ClientVersion(417, 8, null)});
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en) AppleWebKit/417.3 (KHTML, like Gecko) Safari/417.2"), new Object[]{ClientAgentType.HTML_SAFARI2UP, new ClientVersion(417, 2, null)});
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/412 (KHTML, like Gecko) Safari/412"), new Object[]{ClientAgentType.HTML_SAFARI2UP, new ClientVersion(412, -1, null)});
        // safari 1.x
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; fr-fr) AppleWebKit/312.5.1 (KHTML, like Gecko) Safari/312.3.1"), new Object[]{ClientAgentType.HTML_SAFARI, new ClientVersion(312, 3, null)});
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; fr-fr) AppleWebKit/312.5 (KHTML, like Gecko) Safari/312.3"), new Object[]{ClientAgentType.HTML_SAFARI, new ClientVersion(312, 3, null)});
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/106.2 (KHTML, like Gecko) Safari/100.1"), new Object[]{ClientAgentType.HTML_SAFARI, new ClientVersion(100, 1, null)});
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; es) AppleWebKit/85 (KHTML, like Gecko) Safari/85"), new Object[]{ClientAgentType.HTML_SAFARI, new ClientVersion(85, -1, null)});
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/74 (KHTML, like Gecko) Safari/74"), new Object[]{ClientAgentType.HTML_SAFARI, new ClientVersion(74, -1, null)});
        // avant
        cais.put(new ClientAgentInformation("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322; FDM)"), new Object[]{ClientAgentType.HTML_IE6UP, new ClientVersion(6, 0, null)});
        // camino
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.0.1) Gecko/20060118 Camino/1.0b2+"), new Object[]{ClientAgentType.HTML_MOZILLA, new ClientVersion(5, 0, null)});
        // epiphany
        cais.put(new ClientAgentInformation("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)"), new Object[]{ClientAgentType.HTML_EPIPHANY14UP, new ClientVersion(1, 4, null)});
        // firefox 1.6a
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Windows; U; Windows NT 5.2 x64; en-US; rv:1.9a1) Gecko/20060214 Firefox/1.6a1"), new Object[]{ClientAgentType.HTML_FIREFOX16UP, new ClientVersion(1, 6, "a1")});
        // firefox 1.5
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1"), new Object[]{ClientAgentType.HTML_FIREFOX15UP, new ClientVersion(1, 5, null)});
        // ie 6.0
        cais.put(new ClientAgentInformation("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"), new Object[]{ClientAgentType.HTML_IE6UP, new ClientVersion(6, 0, null)});
        // ie 7.0
        cais.put(new ClientAgentInformation("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)"), new Object[]{ClientAgentType.HTML_IE7UP, new ClientVersion(7, 0, null)});
        // konqueror
        cais.put(new ClientAgentInformation("Mozilla/5.0 (compatible; Konqueror/3.4; Linux) KHTML/3.4.3 (like Gecko) (Kubuntu package 4:3.4.3-0ubuntu1)"), new Object[]{ClientAgentType.HTML_KONQUEROR, new ClientVersion(3, 4, null)});
        // seamonkey 1.0
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.1) Gecko/20060130 SeaMonkey/1.0"), new Object[]{ClientAgentType.HTML_MOZILLA, new ClientVersion(5, 0, null)});
        // netscape 8.0.1
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.7.5) Gecko/20050519 Netscape/8.0.1"), new Object[]{ClientAgentType.HTML_NS8UP, new ClientVersion(8, 0, null)});
        // netscape 8
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20050512 Netscape/8.0"), new Object[]{ClientAgentType.HTML_NS8UP, new ClientVersion(8, 0, null)});
        // omniweb
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US) AppleWebKit/125.4 (KHTML, like Gecko, Safari) OmniWeb/v563.34"), new Object[]{ClientAgentType.HTML_OMNIWEB, new ClientVersion(563, 34, null)});
        // opera 9
        cais.put(new ClientAgentInformation("Opera/9.0 (Windows NT 5.1; U; en)"), new Object[]{ClientAgentType.HTML_OPERA9UP, new ClientVersion(9, 0, null)});
        cais.put(new ClientAgentInformation("Opera/9.0 (Macintosh; PPC Mac OS X; U; en)"), new Object[]{ClientAgentType.HTML_OPERA9UP, new ClientVersion(9, 0, null)});
        cais.put(new ClientAgentInformation("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; en) Opera 9.0"), new Object[]{ClientAgentType.HTML_OPERA9UP, new ClientVersion(9, 0, null)});
        // opera 8.5
        cais.put(new ClientAgentInformation("Opera/8.5 (Macintosh; PPC Mac OS X; U; en)"), new Object[]{ClientAgentType.HTML_OPERA8UP, new ClientVersion(8, 5, null)});
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Macintosh; PPC Mac OS X; U; en) Opera 8.5"), new Object[]{ClientAgentType.HTML_OPERA8UP, new ClientVersion(8, 5, null)});
        cais.put(new ClientAgentInformation("Mozilla/4.0 (compatible; MSIE 6.0; Mac_PowerPC Mac OS X; en) Opera 8.5"), new Object[]{ClientAgentType.HTML_OPERA8UP, new ClientVersion(8, 5, null)});
        // opera 8.0
        cais.put(new ClientAgentInformation("Opera/8.01 (Windows NT 5.1)"), new Object[]{ClientAgentType.HTML_OPERA8UP, new ClientVersion(8, 1, null)});
        cais.put(new ClientAgentInformation("Mozilla/5.0 (Windows NT 5.1; U; en) Opera 8.01"), new Object[]{ClientAgentType.HTML_OPERA8UP, new ClientVersion(8, 1, null)});
        // opera 7.0
        cais.put(new ClientAgentInformation("Mozilla/4.0 (compatible; MSIE 6.0; MSIE 5.5; Windows NT 4.0) Opera 7.0 [en]"), new Object[]{ClientAgentType.HTML_OPERA7UP, new ClientVersion(7, 0, null)});
        cais.put(new ClientAgentInformation("Opera/7.11 (Windows NT 5.1; U) [en]"), new Object[]{ClientAgentType.HTML_OPERA7UP, new ClientVersion(7, 11, null)});
        // opera 6.0
        cais.put(new ClientAgentInformation("Mozilla/4.0 (compatible; MSIE 5.0; Windows 95) Opera 6.01 [en]"), new Object[]{ClientAgentType.HTML_OPERA6UP, new ClientVersion(6, 1, null)});
        // webtv
        cais.put(new ClientAgentInformation("Mozilla/4.0 WebTV/2.8 (compatible; MSIE 4.0)"), new Object[]{ClientAgentType.HTML_WEBTV, new ClientVersion(2, 8, null)});

        cais.put(new ClientAgentInformation("Mozilla/5.0 (Linux; U; en-us; KFTT Build/IML74K) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.4 Safari/535.19 Silk-Accelerated=true"), new Object[]{ClientAgentType.HTML_SAFARI3UP, new ClientVersion(535, 19, null)});

        cais.put(new ClientAgentInformation("Mozilla/5.0 (Linux; U; Android 4.0.3; en-us; KFTT Build/IML74K) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.4 Mobile Safari/535.19 Silk-Accelerated=true"), new Object[]{ClientAgentType.HTML_SAFARI3UP, new ClientVersion(535, 19, null)});

        cais.put(new ClientAgentInformation("Google (+https://developers.google.com/+/web/snippet/)"), new Object[]{ClientAgentType.HTML_GOOGLE_PLUS_SNIPPET, new ClientVersion(1, 0, null)});

        cais.put(new ClientAgentInformation("facebookexternalhit/1.0 (+http://www.facebook.com/externalhit_uatext.php)"), new Object[]{ClientAgentType.HTML_FACEBOOK, new ClientVersion(1, 0, null)});
        cais.put(new ClientAgentInformation("facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"), new Object[]{ClientAgentType.HTML_FACEBOOK, new ClientVersion(1, 1, null)});
        cais.put(new ClientAgentInformation("facebookexternalhit/1.1"), new Object[]{ClientAgentType.HTML_FACEBOOK, new ClientVersion(1, 1, null)});
        cais.put(new ClientAgentInformation("Facebot"), new Object[]{ClientAgentType.HTML_FACEBOOK, new ClientVersion(1, 0, null)});

        cais.put(new ClientAgentInformation("Twitterbot"), new Object[]{ClientAgentType.HTML_TWITTERBOT, new ClientVersion(1, 0, null)});
        cais.put(new ClientAgentInformation("Twitterbot/1.0"), new Object[]{ClientAgentType.HTML_TWITTERBOT, new ClientVersion(1, 0, null)});
        cais.put(new ClientAgentInformation("Twitterbot/1.1"), new Object[]{ClientAgentType.HTML_TWITTERBOT, new ClientVersion(1, 1, null)});

        int failedCount = 0;
        for (Map.Entry<ClientAgentInformation, Object[]> entry : cais.entrySet()) {
            ClientAgentInformation cai = entry.getKey();
            ClientAgentType actualType = cai.getClientAgentType();
            ClientVersion actualVersion = cai.getClientVersion();
            Object[] validationArgs = entry.getValue();
            ClientAgentType expectedType = (ClientAgentType) validationArgs[0];
            ClientVersion expectedVersion = (ClientVersion) validationArgs[1];
            if (IPUtil.isEqual(actualType, expectedType)) {
                if (expectedVersion.majorVersion < 0 || expectedVersion.majorVersion == actualVersion.majorVersion) {
                    if (expectedVersion.minorVersion < 0 || expectedVersion.minorVersion == actualVersion.minorVersion) {
                        if (IPStringUtil.isEmpty(expectedVersion.betaVersion) || IPStringUtil.isStringEqualIgnoreCase(expectedVersion.betaVersion, actualVersion.betaVersion)) {
                            // if they checked out correctly, then just continue
                            continue;
                        }
                    }
                }
            }

            // they didn't check out, so output an error.
            System.out.println("Failed: " + cai.getUserAgentString() + "\nActual:   " + cai.getClientAgentType() + "\t" + cai.getClientVersion() + "\nExpected: " + expectedType + "\t" + expectedVersion + "\n\n");

            failedCount++;
        }

        if (failedCount > 0) {
            System.out.println("\n\nFailed: " + failedCount + " of " + cais.size());
        } else {
            System.out.println("Tests succeeded!");
        }
    }

    private ClientVersion getClientVersionFromAgentString(String agentString, boolean useCompatibleIfPossible) {
        Pattern patternToUse;
        if (useCompatibleIfPossible && agentString.contains("compatible;")) {
            patternToUse = UA_COMP_VERSION_PATTERN;
        } else {
            patternToUse = UA_NONCOMP_VERSION_PATTERN;
        }
        Matcher matcher = patternToUse.matcher(agentString);
        if (matcher.find()) {
            int majorVersion = IPUtil.getIntFromString(matcher.group(1), 0);
            int minorVersion = IPUtil.getIntFromString(matcher.group(2), 0);
            String betaVersion = matcher.group(3);
            return new ClientVersion(majorVersion, minorVersion, betaVersion);
        }
        return new ClientVersion(1, 0, null);
    }

    private static final Pattern UA_COMP_VERSION_PATTERN = Pattern.compile("compatible;\\s*?\\w*?[\\s|/][A-Za-z]*?" + "(\\d+)"             // Major version number is everything before the first dot
                    + "\\.?"             // The first dot
                    + "(\\d*)"             // Minor version number is every digit after the first dot
                    + "[\\d\\.]*"        // Throw away remaining numbers and dots
                    + "([^;]*)(.*)"      // Beta version string is up to the ;
            , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern UA_NONCOMP_VERSION_PATTERN = Pattern.compile(".*?[/|\\ ]"         // Version starts with a slash or a space
                    + "[A-Za-z]*?"       // Eat any letters before the major version
                    + "(\\d+)"             // Major version number is everything before the first dot
                    + "\\.?"             // the first dot (optional)
                    + "([\\d]*)"         // Minor version number is every digit after the first dot
                    + "[\\d\\.]*"        // Throw away remaining numbers and dots
                    + "([^\\s]*)(.*)"    // Beta version string is up to next space
            , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private transient final ClientAgentType clientAgentType;
    private final String userAgentString;
    private final String userAgentAcceptsString;
    private transient final MimeType preferredMimeType;
    private transient final Collection<MimeType> supportedMimeTypes;
    private transient final ClientVersion clientVersion;
    private transient final boolean isMac;
    private transient final boolean isIOS;
    private transient final boolean isIPhone;
    private transient final boolean isIPad;
    private transient final boolean isKindleFireSilk;
    private transient final boolean isAndroid;
    private transient final boolean isMobile;
    private transient final boolean isTablet;
    private transient final UAgentInfo uAgentInfo;

    private static class ClientVersion {
        private final int majorVersion;
        private final int minorVersion;
        private final String betaVersion;

        public ClientVersion(int majorVersion, int minorVersion, String betaVersion) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.betaVersion = betaVersion;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (majorVersion >= 0) {
                sb.append(majorVersion);
                sb.append(".");
            }
            if (minorVersion >= 0) {
                sb.append(minorVersion);
                sb.append(".");
            }
            if (!IPStringUtil.isEmpty(betaVersion)) {
                sb.append("beta/");
                sb.append(betaVersion);
            }
            return sb.toString();
        }
    }
}
