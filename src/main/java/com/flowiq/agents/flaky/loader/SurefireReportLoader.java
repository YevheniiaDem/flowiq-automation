package com.flowiq.agents.flaky.loader;

import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestOutcome;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class SurefireReportLoader implements TestExecutionLoader {

    private final String sourceName;
    private final Path directory;

    public SurefireReportLoader(String sourceName, Path directory) {
        this.sourceName = sourceName;
        this.directory = directory;
    }

    @Override
    public String sourceName() {
        return sourceName;
    }

    @Override
    public List<TestExecutionRecord> load() {
        List<TestExecutionRecord> records = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            log.debug("Surefire directory not found: {}", directory);
            return records;
        }
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().startsWith("TEST-")
                            && path.toString().endsWith(".xml"))
                    .forEach(path -> records.addAll(parseSuite(path)));
        } catch (Exception e) {
            log.warn("Failed to read Surefire reports from {}: {}", directory, e.getMessage());
        }
        log.info("Loaded {} Surefire records from {}", records.size(), directory);
        return records;
    }

    private List<TestExecutionRecord> parseSuite(Path file) {
        List<TestExecutionRecord> records = new ArrayList<>();
        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(file.toFile());
            NodeList testCases = document.getElementsByTagName("testcase");
            for (int i = 0; i < testCases.getLength(); i++) {
                Element testcase = (Element) testCases.item(i);
                String className = testcase.getAttribute("classname");
                String methodName = testcase.getAttribute("name");
                String testKey = className + "#" + methodName;

                TestOutcome outcome = TestOutcome.PASSED;
                String message = "";
                String trace = "";
                if (testcase.getElementsByTagName("failure").getLength() > 0) {
                    outcome = TestOutcome.FAILED;
                    Element failure = (Element) testcase.getElementsByTagName("failure").item(0);
                    message = failure.getAttribute("message");
                    trace = failure.getTextContent();
                } else if (testcase.getElementsByTagName("error").getLength() > 0) {
                    outcome = TestOutcome.BROKEN;
                    Element error = (Element) testcase.getElementsByTagName("error").item(0);
                    message = error.getAttribute("message");
                    trace = error.getTextContent();
                } else if (testcase.getElementsByTagName("skipped").getLength() > 0) {
                    outcome = TestOutcome.SKIPPED;
                }

                long durationMs = (long) (Double.parseDouble(
                        testcase.getAttribute("time").isBlank() ? "0" : testcase.getAttribute("time")) * 1000);

                records.add(TestExecutionRecord.builder()
                        .testKey(testKey)
                        .className(className)
                        .methodName(methodName)
                        .suite(AllureResultsLoader.extractClassName(className).contains("Contract") ? "contract"
                                : className.contains("Smoke") ? "smoke"
                                : className.contains("Regression") ? "regression"
                                : className.contains("Ui") ? "ui" : "other")
                        .outcome(outcome)
                        .message(message)
                        .stackTrace(trace)
                        .source(sourceName + ":" + file.getFileName())
                        .durationMs(durationMs)
                        .build());
            }
        } catch (Exception e) {
            log.warn("Skipping Surefire file {}: {}", file, e.getMessage());
        }
        return records;
    }
}
