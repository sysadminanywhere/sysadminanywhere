package com.sysadminanywhere.control;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HardwareExplorerTest {
    @Test
    void loadsOnlyTheSelectedSection() {
        AtomicInteger firstLoads = new AtomicInteger();
        AtomicInteger secondLoads = new AtomicInteger();
        HardwareExplorer explorer = new HardwareExplorer("Hardware sections", List.of(
                new HardwareExplorer.Section("overview", "Overview", () -> {
                    firstLoads.incrementAndGet();
                    return new Span("Summary");
                }),
                new HardwareExplorer.Section("processor", "Processor", () -> {
                    secondLoads.incrementAndGet();
                    return new Span("CPU details");
                })));

        Nav navigation = (Nav) explorer.getChildren().findFirst().orElseThrow();
        List<Button> buttons = navigation.getChildren().map(Button.class::cast).toList();
        assertEquals("Hardware sections", navigation.getElement().getAttribute("aria-label"));
        assertEquals(1, firstLoads.get());
        assertEquals(0, secondLoads.get());

        buttons.get(1).click();

        assertEquals(1, secondLoads.get());
        assertEquals("page", buttons.get(1).getElement().getAttribute("aria-current"));
        assertEquals(null, buttons.get(0).getElement().getAttribute("aria-current"));
        assertEquals(2, ((Div) explorer.getChildren().skip(1).findFirst().orElseThrow())
                .getChildren().count());
    }
}
