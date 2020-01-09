package org.narrative.shared.redisson.codec;

public class NarrativeJsonJacksonCodec extends FinalClassFriendlyJsonJacksonCodec {
    private static final String NARR_PACKAGE = "org.narrative";

    public NarrativeJsonJacksonCodec() {
        super(NARR_PACKAGE);
    }
}
