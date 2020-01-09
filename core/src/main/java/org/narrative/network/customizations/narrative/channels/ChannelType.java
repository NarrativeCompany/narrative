package org.narrative.network.customizations.narrative.channels;

import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheRole;
import org.narrative.network.customizations.narrative.personaljournal.PersonalJournal;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationRole;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2018-12-19
 * Time: 10:16
 *
 * @author jonmark
 */
public enum ChannelType implements IntegerEnum, NameForDisplayProvider {
    NICHE(0, "niches", NicheRole.class) {
        @Override
        public Niche getConsumer(Channel channel) {
            assert channel.getType() == this : "Should always use this method using channel of the appropriate type. Not/"+channel.getType();

            return Niche.dao().get(channel.getOid());
        }

        @Override
        public Set<LedgerEntryType> getLedgerEntryTypes() {
            return LedgerEntryType.NICHE_TYPES;
        }
    }
    // jw: updating this to use a underscore, since we cannot use dashes in graphql enum names!
    //     see: https://github.com/graphql/graphql-js/issues/936
    ,PERSONAL_JOURNAL(1, "personal-journals", null) {
        @Override
        public PersonalJournal getConsumer(Channel channel) {
            assert channel.getType() == this : "Should always use this method using channel of the appropriate type. Not/"+channel.getType();

            return PersonalJournal.dao().get(channel.getOid());
        }
    }
    ,PUBLICATION(2, "publications", PublicationRole.class) {
        @Override
        public Publication getConsumer(Channel channel) {
            assert channel.getType() == this : "Should always use this method using channel of the appropriate type. Not/"+channel.getType();

            return Publication.dao().get(channel.getOid());
        }

        @Override
        public Set<LedgerEntryType> getLedgerEntryTypes() {
            return LedgerEntryType.PUBLICATION_TYPES;
        }
    }
    ;

    private static final Map<String, ChannelType> REST_RESOURCE_TO_CHANNEL_TYPE_MAP;
    static {
        Map<String, ChannelType> restResourceLookup = new HashMap<>();
        for (ChannelType channelType : values()) {
            String resource = channelType.getRestResourcePath();
            restResourceLookup.put(resource, channelType);

            // jw: We need to support an underscore version for any types using hyphens, since we cannot use hyphens
            //     in graphql enum names! see: https://github.com/graphql/graphql-js/issues/936
            if (resource.contains("-")) {
                restResourceLookup.put(resource.replace('-', '_'), channelType);
            }
        }

        REST_RESOURCE_TO_CHANNEL_TYPE_MAP = Collections.unmodifiableMap(restResourceLookup);
    }

    private final int id;
    private final String restResourcePath;
    private final Class<? extends ChannelRole> roleType;

    <T extends Enum<T> & ChannelRole> ChannelType(int id, String restResourcePath, Class<T> roleType) {
        this.id = id;
        this.restResourcePath = restResourcePath;
        this.roleType = roleType;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getNameForDisplay() {
        return wordlet("channelType." + this);
    }

    public String getDeletedConsumerNameForDisplay() {
        assert !isPersonalJournal() : "Currently, this should never be called for personal journals.";

        return wordlet("channelType.deleted." + this);
    }

    private String getRestResourcePath() {
        return restResourcePath;
    }

    public static ChannelType getChannelTypeForRestResourcePath(String restResourcePath) {
        return REST_RESOURCE_TO_CHANNEL_TYPE_MAP.get(restResourcePath);
    }

    public boolean isNiche() {
        return this == NICHE;
    }

    public boolean isPersonalJournal() {
        return this == PERSONAL_JOURNAL;
    }

    public boolean isPublication() {
        return this == PUBLICATION;
    }

    public boolean isPrimaryPublishingChannel() {
        return isPersonalJournal() || isPublication();
    }

    public boolean isContentStreamSupportsQualityFilter() {
        // bl: Publications are the only type that don't support the quality filter for content streams
        return !isPublication();
    }

    public boolean isContentStreamSupportsSortOrder() {
        // bl: Publications are the only type that don't support sort orders for content streams
        return !isPublication();
    }

    public boolean isSupportsFeaturedPosts() {
        // bl: for now, only Publications support featured posts
        return isPublication();
    }

    public boolean isSupportsStatusAppeals() {
        // jw: for now, the only type that does not support status appeals is personal journals.
        return !isPersonalJournal();
    }

    public boolean isBlockRemovedPosts() {
        // bl: only block removed posts in Niches. when a Publication removes a post, the post doesn't need to
        // be irrevocably blocked. the Publication editors may just be sending it back to the author for further edits.
        return isNiche();
    }

    public boolean isAllowPrimaryChannelChanges() {
        // bl: once a post is assigned to a Publication, changes are not allowed through the edit process
        return !isPublication();
    }

    public Set<LedgerEntryType> getLedgerEntryTypes() {
        throw UnexpectedError.getRuntimeException("Should never call this method for types that do not support channel based ledger entry lookups! type/"+this);
    }

    public <T extends Enum & ChannelRole> Class<T> getRoleType() {
        // jw: the constructor asserts that this class is also a enum... Unfortunately you cannot do multiple inheritance
        //     definitions with a wildcard generic.
        return (Class<T>) roleType;
    }

    public abstract <T extends ChannelConsumer> T getConsumer(Channel channel);
}