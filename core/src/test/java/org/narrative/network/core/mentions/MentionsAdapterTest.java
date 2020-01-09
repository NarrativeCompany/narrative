package org.narrative.network.core.mentions;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 10/28/18
 * Time: 9:55 AM
 *
 * @author brian
 */
class MentionsAdapterTest {

    @Test
    void validate__parsedMention__match() {
        assertEquals(Collections.singletonList("joebob"), MentionsAdapter.getAllMentionedUsernames("this is my post body. isn't it cool, @joebob?"));
    }

    @Test
    void validate__multipleMentions__match() {
        assertEquals(Arrays.asList("joebob", "lucy"), MentionsAdapter.getAllMentionedUsernames("this is my post body. isn't it cool, @joebob? @lucy doesn't think so."));
    }

    @Test
    void validate__startsWithMention__found() {
        assertEquals(Collections.singletonList("joebob"), MentionsAdapter.getAllMentionedUsernames("@joebob this is my post body. isn't it cool?"));
    }

    @Test
    void validate__endsWithMention__found() {
        assertEquals(Collections.singletonList("joebob"), MentionsAdapter.getAllMentionedUsernames("this is my post body. isn't it cool, @joebob"));
    }

    @Test
    void validate__mentionPrefixedPeriod__matches() {
        assertEquals(Collections.singletonList("joebob"), MentionsAdapter.getAllMentionedUsernames(".@joebob this is my post body. isn't it cool?"));
    }

    @Test
    void validate__emailAddress__doesNotMatch() {
        assertEquals(Collections.emptyList(), MentionsAdapter.getAllMentionedUsernames("hi joe@joebob.com this is my post body. isn't it cool?"));
    }
}
