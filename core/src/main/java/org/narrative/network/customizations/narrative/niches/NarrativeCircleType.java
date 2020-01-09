package org.narrative.network.customizations.narrative.niches;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.core.settings.area.community.advanced.SandboxedCommunitySettings;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/9/18
 * Time: 9:35 AM
 */
public enum NarrativeCircleType implements IntegerEnum {
    FOUNDING_MEMBERS(0, "Founding Members", "Founding Member"),
    TOKEN_SALE_PARTICIPANTS(1, "Token Sale Participants", null),
    NARRATIVE_STAFF(2, "Narrative Staff", null, GlobalSecurable.REMOVE_AUP_VIOLATIONS),
    TRIBUNAL(3, "Tribunal", null, GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS),
    NICHE_OWNERS(6, "Niche Owners", null);

    private final int id;
    private final String name;
    private final String label;
    private final Set<GlobalSecurable> defaultSecurables;

    NarrativeCircleType(int id, String name, String label, GlobalSecurable... defaultSecurables) {
        this.id = id;
        this.name = name;
        this.label = label;
        this.defaultSecurables = isEmptyOrNull(defaultSecurables) ? Collections.emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(defaultSecurables)));
    }

    public static final Set<NarrativeCircleType> MANAGEABLE_CIRCLE_TYPES = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.stream(NarrativeCircleType.values()).filter(NarrativeCircleType::isMembershipManageable).collect(Collectors.toList())));

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public boolean isViewableByAdminsOnly() {
        return isEmpty(label);
    }

    public Set<GlobalSecurable> getDefaultSecurables() {
        return defaultSecurables;
    }

    public AreaCircle getCircle(AuthZone authZone) {
        SandboxedCommunitySettings settings = authZone.getSandboxedCommunitySettings();
        return settings.getCirclesByNarrativeCircleType().get(this);
    }

    public void addUserToCircle(User user) {
        AreaCircle circle = getCircle(user.getAuthZone());

        user.getLoneAreaUser().addToAreaCircle(circle);
    }

    public void removeUserFromCircle(User user) {
        AreaCircle circle = getCircle(user.getAuthZone());

        user.getLoneAreaUser().removeFromAreaCircle(circle);
    }

    public boolean isMembershipManageable() {
        return isNarrativeStaff() || isTribunal();
    }

    public boolean isNarrativeStaff() {
        return this==NARRATIVE_STAFF;
    }

    public boolean isTribunal() {
        return this==TRIBUNAL;
    }
}
