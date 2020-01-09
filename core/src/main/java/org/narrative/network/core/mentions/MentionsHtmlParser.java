package org.narrative.network.core.mentions;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.XMLUtil;
import org.narrative.common.util.html.HTMLParser;
import org.narrative.network.core.user.User;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/22/16
 * Time: 3:05 PM
 */
public class MentionsHtmlParser extends HTMLParser {
    private String anchorElementName;
    private boolean withinAnchor;
    private OID mentionedMemberOid;
    private final Map<String, String> attributes = newHashMap();
    private final Stack<String> ignoreElements = new Stack<>();

    private final Set<OID> mentionedMemberOids = new HashSet<>();

    private static final String A__NAME = "a";
    private static final String MENTIONED_MEMBER_OID__ATT = "data-mentioned-member-oid";

    protected MentionsHtmlParser(String html) {
        super(html, FragmentType.BODY);
    }

    @Override
    protected int startElement(String elementName, boolean isSelfClose) {
        // jw: if we are within the context of a mention, we need to skip all output.
        if (mentionedMemberOid != null) {
            ignoreElements.push(elementName.toLowerCase());
            return 0;
        }
        // jw: since we were not within a mention, lets see if this is a anchor.
        withinAnchor = A__NAME.equalsIgnoreCase(elementName);

        // jw: if we are inside a anchor we need to wait until the endStartElement so that we can be sure we are not within
        //     a mention before we output the starting HTML.
        if (withinAnchor) {
            // jw: lets cache this so that we can output if necessary with the same case used by the author.
            anchorElementName = elementName;
            return 0;
        }

        return super.startElement(elementName, isSelfClose);
    }

    @Override
    protected int addAttribute(String name, String value) {
        // jw: ignore any attributes once we know that we are within a mention.
        if (mentionedMemberOid != null) {
            return 0;
        }
        // jw: lets see if we are adding a mention
        if (withinAnchor) {
            if (MENTIONED_MEMBER_OID__ATT.equalsIgnoreCase(name)) {
                mentionedMemberOid = OID.getOIDFromString(value);
                if (mentionedMemberOid != null) {
                    mentionedMemberOids.add(mentionedMemberOid);
                }

            } else {
                attributes.put(name, value);
            }

            return 0;
        }

        return super.addAttribute(name, value);
    }

    @Override
    protected int endStartElement() {
        // jw: if we are within the context of a mention, we need to skip all output.
        if (mentionedMemberOid != null) {
            return 0;
        }
        // jw: if we are within an anchor, we need output the start of the anchor and all added attributes.
        if (withinAnchor) {
            int charCount = super.startElement(anchorElementName, false);
            for (Map.Entry<String, String> attributeEntry : attributes.entrySet()) {
                charCount += super.addAttribute(attributeEntry.getKey(), attributeEntry.getValue());
            }
            attributes.clear();

            return charCount + super.endStartElement();
        }
        return super.endStartElement();
    }

    @Override
    protected int textNode(String text) {
        // jw: if we are within the context of a mention, we need to skip all output.
        if (mentionedMemberOid != null) {
            return 0;
        }
        return super.textNode(text);
    }

    @Override
    protected int closeElement(String elementName, boolean isSelfClose) {
        if (mentionedMemberOid != null) {
            // jw: if we still have elements to ignore then lets pop it, validate it matches, and continue on.
            if (ignoreElements.size() > 0) {
                String ignoredElement = ignoreElements.pop();
                assert isEqual(ignoredElement, elementName.toLowerCase());

                return 0;
            }

            // jw: since there are no more elements to ignore, guess we are safe to write out the placeholder.
            OID memberOid = mentionedMemberOid;
            mentionedMemberOid = null;
            // jw: this is technically not necessary since the next call to startElement would reset it, but lets be thorough
            withinAnchor = false;
            return textNode(MentionsUtil.getMentionPlaceholder(memberOid));
        }

        return super.closeElement(elementName, isSelfClose);
    }

    @Override
    protected int commentNode(String comment) {
        // jw: ignore any attributes once we know that we are within a mention.
        if (mentionedMemberOid != null) {
            return 0;
        }

        return super.commentNode(comment);
    }

    public Set<OID> getMentionedMemberOids() {
        return mentionedMemberOids;
    }

    public static String escapeMentions(String html) {
        return new MentionsHtmlParser(html).parse();
    }

    public static String getMentionLink(User user) {
        if (!user.isVisible()) {
            return "@" + wordlet("role.formerNarratorUsername");
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("href", user.getProfileUrl());
        params.put(MENTIONED_MEMBER_OID__ATT, user.getOid().toString());

        StringBuilder link = new StringBuilder();
        XMLUtil.addField(link, "a", "@" + user.getUsername(), params);

        return link.toString().trim();
    }
}
