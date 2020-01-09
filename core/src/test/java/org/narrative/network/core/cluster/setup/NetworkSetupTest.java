package org.narrative.network.core.cluster.setup;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Properties;

class NetworkSetupTest {
    private final String TEST_KEY = "testKey";
    private final String TEST_VALUE = "testValue";
    private final String ANOTHER_TEST_KEY = "anotherTestKey";
    private final String ANOTHER_TEST_VALUE = "anotherTestValue";
    private final String CONTEXT_PREFIXED_TEST_KEY = "server.servlet.context-parameters." + TEST_KEY;

    @Test
    void renamePropertyKey_contextInitParameter_prefixRemovedFromKey() {
        Assert.assertEquals(TEST_KEY, NetworkSetup.renamePropertyKey(CONTEXT_PREFIXED_TEST_KEY));
    }

    @Test
    void renamePropertyKey_passThroughParameter_propertyKeyNotModified() {
        Assert.assertEquals(TEST_KEY, NetworkSetup.renamePropertyKey(TEST_KEY));
    }

    @Test
    void mergeProperties_initialSourceToEmptyTarget_targetContainsSource() {
        final Properties initialSource = new Properties();
        initialSource.setProperty(TEST_KEY, TEST_VALUE);
        initialSource.setProperty(ANOTHER_TEST_KEY, ANOTHER_TEST_VALUE);

        final Properties emptyTarget = new Properties();

        NetworkSetup.mergeProperties(initialSource, emptyTarget);

        Assert.assertEquals(initialSource, emptyTarget);
    }

    @Test
    void mergeProperties_anotherPropertyKeyAdded_resultContainsExistingAndNewProperties() {
        final Properties source = new Properties();
        source.setProperty(ANOTHER_TEST_KEY, ANOTHER_TEST_VALUE);

        final Properties target = new Properties();
        target.setProperty(TEST_KEY, TEST_VALUE);

        final Properties result = new Properties();
        result.setProperty(TEST_KEY, TEST_VALUE);
        result.setProperty(ANOTHER_TEST_KEY, ANOTHER_TEST_VALUE);

        NetworkSetup.mergeProperties(source, target);

        Assert.assertEquals(result, target);
    }

    @Test
    void mergeProperties_duplicatePropertyKey_duplicatePropertyKeyValueOverwitten() {
        final Properties source = new Properties();
        source.setProperty(TEST_KEY, TEST_VALUE);

        final Properties target = new Properties();
        target.setProperty(TEST_KEY, "replaceMe");

        final Properties result = new Properties();
        result.setProperty(TEST_KEY, TEST_VALUE);

        NetworkSetup.mergeProperties(source, target);

        Assert.assertEquals(result, target);
    }

    @Test
    void mergeProperties_contextPrefixedTestKeyPropertyRenamed_propertyKeyRenamedAndContextPrefixedTestKeyNotRetained() {
        final Properties source = new Properties();
        source.setProperty(CONTEXT_PREFIXED_TEST_KEY, TEST_VALUE);

        final Properties target = new Properties();

        final Properties result = new Properties();
        result.setProperty(TEST_KEY, TEST_VALUE);

        NetworkSetup.mergeProperties(source, target);

        Assert.assertEquals(result, target);
    }

    @Test
    void mergeProperties_contextPrefixedTestKeyRenamedValueReplaced_propertyKeyRenamedAndOriginalPropertyKeyNotRetained() {
        final Properties source = new Properties();
        source.setProperty(CONTEXT_PREFIXED_TEST_KEY, ANOTHER_TEST_VALUE);

        final Properties target = new Properties();
        target.setProperty(TEST_KEY, TEST_VALUE);

        final Properties result = new Properties();
        result.setProperty(TEST_KEY, ANOTHER_TEST_VALUE);

        NetworkSetup.mergeProperties(source, target);

        Assert.assertEquals(result, target);
    }
}
