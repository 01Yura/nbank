package common.extensions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private Map<String, Long> startTimes = new HashMap<>();

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        String testName =context.getRequiredTestClass().getPackageName() + "." + context.getDisplayName();
        startTimes.put(testName, System.currentTimeMillis());
        System.out.println("Current thread: " + Thread.currentThread().getName() + "Test name: " + testName + " " +
                "started");
    }


    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        String testName =context.getRequiredTestClass().getPackageName() + "." + context.getDisplayName();
        Long testDuration =System.currentTimeMillis() - startTimes.get(testName);
        System.out.println("Current thread: " + Thread.currentThread().getName() + "Test name: " + testName + " " +
                "finished" + ", test duration: " + testDuration + "ms");
    }
}
