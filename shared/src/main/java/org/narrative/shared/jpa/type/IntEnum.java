package org.narrative.shared.jpa.type;

/**
 * Interface for {@link java.lang.Enum} classes that provide an ordinal value as an {@link int}
 */
public interface IntEnum<E extends Enum> {
    /**
     * Get the ordinal value for this {@link Enum}
     */
    int getId();
}
