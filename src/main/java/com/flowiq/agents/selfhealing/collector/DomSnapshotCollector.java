package com.flowiq.agents.selfhealing.collector;

import com.flowiq.agents.selfhealing.model.DomElement;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DomSnapshotCollector {

    private static final Pattern OPEN_TAG = Pattern.compile(
            "<([a-zA-Z][a-zA-Z0-9]*)\\b([^>]*)>",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTR = Pattern.compile(
            "([a-zA-Z_:][a-zA-Z0-9_:\\-]*)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern TEXT_NODE = Pattern.compile(
            ">([^<]{1,200})<");

    public List<DomElement> collect(Path snapshotFile) {
        if (snapshotFile == null || !Files.isRegularFile(snapshotFile)) {
            log.debug("DOM snapshot not found: {}", snapshotFile);
            return List.of();
        }
        try {
            return parseHtml(Files.readString(snapshotFile));
        } catch (IOException e) {
            log.warn("Failed to read DOM snapshot {}: {}", snapshotFile, e.getMessage());
            return List.of();
        }
    }

    public List<DomElement> parseHtml(String html) {
        if (html == null || html.isBlank()) {
            return List.of();
        }
        List<DomElement> elements = new ArrayList<>();
        Matcher tagMatcher = OPEN_TAG.matcher(html);
        int index = 0;
        while (tagMatcher.find()) {
            String tag = tagMatcher.group(1).toLowerCase();
            if (isSkippableTag(tag)) {
                continue;
            }
            String attrs = tagMatcher.group(2);
            DomElement.DomElementBuilder builder = DomElement.builder()
                    .tagName(tag)
                    .sourceIndex(index++);

            Matcher attrMatcher = ATTR.matcher(attrs);
            while (attrMatcher.find()) {
                String name = attrMatcher.group(1).toLowerCase();
                String value = attrMatcher.group(2).trim();
                switch (name) {
                    case "data-testid" -> builder.testId(value);
                    case "aria-label" -> builder.ariaLabel(value);
                    case "role" -> builder.role(value);
                    case "id" -> builder.id(value);
                    case "class" -> builder.cssClasses(value);
                    default -> { }
                }
            }

            String text = extractNearbyText(html, tagMatcher.end());
            if (text != null && !text.isBlank() && supportsVisibleText(tag)) {
                builder.textContent(text.trim());
            }
            elements.add(builder.build());
        }
        log.debug("Parsed {} DOM element(s) from snapshot", elements.size());
        return elements;
    }

    private static String extractNearbyText(String html, int fromIndex) {
        if (fromIndex >= html.length()) {
            return null;
        }
        Matcher textMatcher = TEXT_NODE.matcher(html.substring(fromIndex));
        if (textMatcher.find()) {
            String text = textMatcher.group(1).trim();
            if (!text.isBlank() && !text.startsWith("/")) {
                return text;
            }
        }
        return null;
    }

    private static boolean supportsVisibleText(String tag) {
        return switch (tag) {
            case "button", "a", "label", "span", "p", "h1", "h2", "h3", "h4", "h5", "h6" -> true;
            default -> false;
        };
    }

    private static boolean isSkippableTag(String tag) {
        return switch (tag) {
            case "html", "head", "meta", "link", "style", "script", "br", "hr" -> true;
            default -> false;
        };
    }
}
