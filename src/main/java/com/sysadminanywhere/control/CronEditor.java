package com.sysadminanywhere.control;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CronEditor extends CustomField<String> {

    private final ComboBox<String> minuteComboBox;
    private final ComboBox<String> hourComboBox;
    private final ComboBox<String> dayOfMonthComboBox;
    private final ComboBox<String> monthComboBox;
    private final ComboBox<String> dayOfWeekComboBox;

    public CronEditor() {
        minuteComboBox = createComboBox("Minute", 0, 59);
        hourComboBox = createComboBox("Hour", 0, 23);
        dayOfMonthComboBox = createComboBox("Day of Month", 1, 31);
        monthComboBox = createComboBox("Month", 1, 12);
        dayOfWeekComboBox = createComboBox("Day of Week", 0, 6); // 0 = Sunday, 6 = Saturday

        HorizontalLayout layout = new HorizontalLayout(minuteComboBox, hourComboBox, dayOfMonthComboBox, monthComboBox, dayOfWeekComboBox);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);

        add(layout);

        minuteComboBox.addValueChangeListener(e -> updateCronExpression());
        hourComboBox.addValueChangeListener(e -> updateCronExpression());
        dayOfMonthComboBox.addValueChangeListener(e -> updateCronExpression());
        monthComboBox.addValueChangeListener(e -> updateCronExpression());
        dayOfWeekComboBox.addValueChangeListener(e -> updateCronExpression());
    }

    private ComboBox<String> createComboBox(String label, int start, int end) {
        ComboBox<String> comboBox = new ComboBox<>(label);
        comboBox.setItems(generateRange(start, end));
        comboBox.setValue(String.valueOf(start));
        return comboBox;
    }

    private List<String> generateRange(int start, int end) {
        return Arrays.stream(new int[end - start + 1])
                .mapToObj(i -> String.valueOf(start + i))
                .collect(Collectors.toList());
    }

    private void updateCronExpression() {
        String minute = minuteComboBox.getValue();
        String hour = hourComboBox.getValue();
        String dayOfMonth = dayOfMonthComboBox.getValue();
        String month = monthComboBox.getValue();
        String dayOfWeek = dayOfWeekComboBox.getValue();

        setValue(String.join(" ", minute, hour, dayOfMonth, month, dayOfWeek));
    }

    @Override
    protected String generateModelValue() {
        return String.join(" ", minuteComboBox.getValue(), hourComboBox.getValue(),
                dayOfMonthComboBox.getValue(), monthComboBox.getValue(),
                dayOfWeekComboBox.getValue());
    }

    @Override
    protected void setPresentationValue(String value) {
        if (value != null && !value.isEmpty()) {
            String[] parts = value.split(" ");
            if (parts.length == 5) {
                minuteComboBox.setValue(parts[0]);
                hourComboBox.setValue(parts[1]);
                dayOfMonthComboBox.setValue(parts[2]);
                monthComboBox.setValue(parts[3]);
                dayOfWeekComboBox.setValue(parts[4]);
            }
        }
    }

}