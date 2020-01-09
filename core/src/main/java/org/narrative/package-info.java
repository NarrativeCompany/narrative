/**
 * Make OIDGenerator available by name to entities for everything in this package.
 */
@org.hibernate.annotations.GenericGenerator(name = org.narrative.common.persistence.OIDGenerator.NAME, strategy = "org.narrative.common.persistence.OIDGenerator")
package org.narrative;

