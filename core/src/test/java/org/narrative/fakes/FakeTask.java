package org.narrative.fakes;

import org.narrative.common.util.Task;
import mockit.Mock;
import mockit.MockUp;

public class FakeTask extends MockUp<Task> {
    @Mock
    private void validateViaFunction() {}
}
