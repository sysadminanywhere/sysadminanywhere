package com.sysadminanywhere.control;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CronEditor extends CustomField<String> {

    protected static final String EVERY = "*";
    protected static final String SPECIFIC = "Specific";
    protected static final String RANGE = "Range";
    protected static final String STEP = "Step";
    protected static final String STEP_RANGE = "Step Range";
    protected static final String NO_SPECIFIC = "?";

    private final FormLayout formLayout = new FormLayout();
    private final TextField generatedCron = new TextField("Generated Cron");

    private final CronPartEditor minutesEditor;
    private final CronPartEditor hoursEditor;
    private final CronPartEditor dayOfMonthEditor;
    private final CronPartEditor monthEditor;
    private final CronPartEditor dayOfWeekEditor;

    public CronEditor() {
        this("Cron Configuration");
    }

    public CronEditor(String label) {

        super("0 * * * * ?"); // Default: Every minute at second 0

        setLabel(label);

        minutesEditor = new CronPartEditor("Minutes", 0, 59, false);
        hoursEditor = new CronPartEditor("Hours", 0, 23, false);
        dayOfMonthEditor = new CronPartEditor("Day of Month", 1, 31, true);
        monthEditor = new CronPartEditor("Month", 1, 12, false, getMonthNames());
        dayOfWeekEditor = new CronPartEditor("Day of Week", 0, 6, true, getDayOfWeekNames());

        generatedCron.setWidthFull();
        generatedCron.setReadOnly(true);

        HasValue.ValueChangeListener<? super ComponentValueChangeEvent<CustomField<String>, String>> internalListener = e -> {
            if (!e.isFromClient() && Objects.equals(e.getValue(), NO_SPECIFIC)) {
                updateCronString();
            } else if (e.isFromClient()) {
                updateCronString();
            }
        };

        minutesEditor.addValueChangeListener(internalListener);
        hoursEditor.addValueChangeListener(internalListener);
        dayOfMonthEditor.addValueChangeListener(internalListener);
        monthEditor.addValueChangeListener(internalListener);
        dayOfWeekEditor.addValueChangeListener(internalListener);

        dayOfMonthEditor.addValueChangeListener(e -> handleDayMonthExclusivity(dayOfMonthEditor, dayOfWeekEditor, e.isFromClient()));
        dayOfWeekEditor.addValueChangeListener(e -> handleDayMonthExclusivity(dayOfWeekEditor, dayOfMonthEditor, e.isFromClient()));

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.add(
                minutesEditor, hoursEditor,
                dayOfMonthEditor, monthEditor, dayOfWeekEditor
        );

        formLayout.setColspan(dayOfWeekEditor, 2);

        VerticalLayout mainLayout = new VerticalLayout(formLayout, generatedCron);
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);
        add(mainLayout);

        updateGeneratedCronFieldOnly();
    }

    private Map<Integer, String> getMonthNames() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            map.put(i, Month.of(i).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH).toUpperCase());
        }
        return map;
    }

    private Map<Integer, String> getDayOfWeekNames() {
        Map<Integer, String> map = new LinkedHashMap<>();
        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int i = 0; i <= 6; i++) {
            map.put(i, String.format("%d (%s)", i, days[i]));
        }
        return map;
    }

    private void handleDayMonthExclusivity(CronPartEditor changedEditor, CronPartEditor otherEditor, boolean fromClient) {
        if (!fromClient) {
            return;
        }
        if (!changedEditor.isQuestionMarkAllowed() || !otherEditor.isQuestionMarkAllowed()) return;

        String changedValue = changedEditor.generateModelValue();
        String otherValue = otherEditor.generateModelValue();

        if (!NO_SPECIFIC.equals(changedValue) && !EVERY.equals(changedValue)) {
            if (!NO_SPECIFIC.equals(otherValue)) {
                otherEditor.setPartValueProgrammatically(NO_SPECIFIC);
            }
        } else if (NO_SPECIFIC.equals(changedValue)) {
            if (NO_SPECIFIC.equals(otherValue)) {
                otherEditor.setPartValueProgrammatically(EVERY);
            }
        }
    }

    private void updateCronString() {
        String newCronValue = generateModelValue();
        generatedCron.setValue(newCronValue);
        setModelValue(newCronValue, true);
    }

    private void updateGeneratedCronFieldOnly() {
        generatedCron.setValue(generateModelValue());
    }

    @Override
    protected String generateModelValue() {
        return String.join(" ",
                "0",
                minutesEditor.generateModelValue(),
                hoursEditor.generateModelValue(),
                dayOfMonthEditor.generateModelValue(),
                monthEditor.generateModelValue(),
                dayOfWeekEditor.generateModelValue()
        );
    }

    @Override
    protected void setPresentationValue(String newPresentationValue) {
        String valueToParse = newPresentationValue;
        if (valueToParse == null) {
            valueToParse = "0 * * * * ?";
        }

        String[] parts = valueToParse.trim().split("\\s+");
        boolean parsedOk = false;
        if (parts.length >= 6) {
            try {
                minutesEditor.parseValue(parts[1]);
                hoursEditor.parseValue(parts[2]);
                dayOfMonthEditor.parseValue(parts[3]);
                monthEditor.parseValue(parts[4]);
                dayOfWeekEditor.parseValue(parts[5]);
                parsedOk = true;
            } catch (Exception e) {
                System.err.println("Error parsing cron string in setPresentationValue: '" + valueToParse + "' - " + e.getMessage());
                parsedOk = false;
            }
        } else {
            System.err.println("Invalid cron string format in setPresentationValue: '" + valueToParse + "' - expected at least 6 parts.");
        }

        if (!parsedOk) {
            minutesEditor.parseValue("*");
            hoursEditor.parseValue("*");
            dayOfMonthEditor.parseValue("*");
            monthEditor.parseValue("*");
            dayOfWeekEditor.parseValue("?");
        }

        updateGeneratedCronFieldOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        minutesEditor.setReadOnly(readOnly);
        hoursEditor.setReadOnly(readOnly);
        dayOfMonthEditor.setReadOnly(readOnly);
        monthEditor.setReadOnly(readOnly);
        dayOfWeekEditor.setReadOnly(readOnly);
    }

    private static class CronPartEditor extends CustomField<String> {

        @Getter
        private final String partName;
        private final int min;
        private final int max;
        @Getter
        private final boolean questionMarkAllowed;
        private final Map<Integer, String> valueMap;

        private final RadioButtonGroup<String> modeSelect = new RadioButtonGroup<>();
        private final MultiSelectComboBox<Integer> specificValues = new MultiSelectComboBox<>();
        private final IntegerField rangeStart = new IntegerField();
        private final IntegerField rangeEnd = new IntegerField();
        private final IntegerField stepValue = new IntegerField();
        private final IntegerField stepStart = new IntegerField();

        private final Span startingAtSpan = new Span("starting at");

        private final VerticalLayout editorLayout = new VerticalLayout();
        private final HorizontalLayout specificLayout = new HorizontalLayout(specificValues);
        private final HorizontalLayout rangeLayout = new HorizontalLayout(new Span("From"), rangeStart, new Span("to"), rangeEnd);
        private final HorizontalLayout stepLayout = new HorizontalLayout(new Span("Every"));
        private final HorizontalLayout stepRangeLayout = new HorizontalLayout(new Span("Every"));

        private String currentPartValue = "*";

        public CronPartEditor(String partName, int min, int max, boolean questionMarkAllowed) {
            this(partName, min, max, questionMarkAllowed, null);
        }

        public CronPartEditor(String partName, int min, int max, boolean questionMarkAllowed, Map<Integer, String> valueMap) {
            super(EVERY);
            this.partName = partName;
            this.min = min;
            this.max = max;
            this.questionMarkAllowed = questionMarkAllowed;
            this.valueMap = valueMap;

            setLabel(partName);

            List<String> modes = new ArrayList<>(Arrays.asList(EVERY, SPECIFIC, RANGE, STEP, STEP_RANGE));
            if (questionMarkAllowed) {
                modes.add(NO_SPECIFIC);
            }
            modeSelect.setItems(modes);
            modeSelect.setValue(EVERY);

            setupSpecificValues();
            setupRangeFields();
            setupStepFields();

            configureLayouts();

            stepRangeLayout.add(stepValue, startingAtSpan, stepStart);

            HasValue.ValueChangeListener<ComponentValueChangeEvent<?, ?>> internalUiListener = e -> {
                if (e.isFromClient()) {
                    updatePartValue();
                }
            };
            specificValues.addValueChangeListener(internalUiListener);
            rangeStart.addValueChangeListener(internalUiListener);
            rangeEnd.addValueChangeListener(internalUiListener);
            stepValue.addValueChangeListener(internalUiListener);
            stepStart.addValueChangeListener(internalUiListener);

            modeSelect.addValueChangeListener(e -> {
                if (e.isFromClient()) {
                    onModeChange(e.getValue());
                    updatePartValue();
                }
            });

            editorLayout.add(modeSelect, specificLayout, rangeLayout, stepLayout, stepRangeLayout);
            add(editorLayout);

            String initialMode = EVERY;
            modeSelect.setValue(initialMode);
            onModeChange(initialMode);
            this.currentPartValue = generateModelValueInternal();
        }

        private void setupSpecificValues() {
            List<Integer> items = IntStream.rangeClosed(min, max).boxed().collect(Collectors.toList());
            specificValues.setItems(items);
            if (valueMap != null) {
                specificValues.setItemLabelGenerator(i -> valueMap.getOrDefault(i, String.valueOf(i)));
            } else {
                specificValues.setItemLabelGenerator(String::valueOf);
            }
            specificValues.setWidth("300px");
        }

        private void setupRangeFields() {
            rangeStart.setMin(min);
            rangeStart.setMax(max);
            rangeStart.setStepButtonsVisible(true);
            rangeEnd.setMin(min);
            rangeEnd.setMax(max);
            rangeEnd.setStepButtonsVisible(true);
            rangeStart.setWidth("80px");
            rangeEnd.setWidth("80px");
        }

        private void setupStepFields() {
            stepValue.setMin(1);
            stepValue.setMax(max);
            stepValue.setStepButtonsVisible(true);
            stepStart.setMin(min);
            stepStart.setMax(max);
            stepStart.setStepButtonsVisible(true);
            stepValue.setWidth("80px");
            stepStart.setWidth("80px");
        }

        private void configureLayouts() {
            specificLayout.setVisible(false);
            specificLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            rangeLayout.setVisible(false);
            rangeLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            rangeLayout.setSpacing(false);
            stepLayout.setVisible(false);
            stepLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            stepLayout.setSpacing(false);
            stepRangeLayout.setVisible(false);
            stepRangeLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            stepRangeLayout.setSpacing(false);

            specificLayout.getThemeList().add("spacing-s");
            rangeLayout.getThemeList().add("spacing-s");
            stepLayout.getThemeList().add("spacing-s");
            stepRangeLayout.getThemeList().add("spacing-s");

            editorLayout.setPadding(false);
            editorLayout.setSpacing(false);
        }

        private void onModeChange(String newMode) {
            specificLayout.setVisible(SPECIFIC.equals(newMode));
            rangeLayout.setVisible(RANGE.equals(newMode));
            stepLayout.setVisible(STEP.equals(newMode));
            stepRangeLayout.setVisible(STEP_RANGE.equals(newMode));

            if (STEP.equals(newMode)) {
                stepLayout.addComponentAtIndex(1, stepValue);
            } else if (STEP_RANGE.equals(newMode)) {
                stepRangeLayout.addComponentAtIndex(1, stepValue);
                stepRangeLayout.addComponentAtIndex(2, startingAtSpan);
                stepRangeLayout.addComponentAtIndex(3, stepStart);
            }

            clearUnusedFields(newMode);
        }

        private void clearUnusedFields(String currentMode) {
            if (!SPECIFIC.equals(currentMode)) specificValues.clear();
            if (!RANGE.equals(currentMode)) { rangeStart.clear(); rangeEnd.clear(); }

            if (!STEP.equals(currentMode) && !STEP_RANGE.equals(currentMode)) {
                stepValue.clear();
            }
            if (!STEP_RANGE.equals(currentMode)) {
                stepStart.clear();
            }
        }

        private void updatePartValue() {
            String newValue = generateModelValueInternal();
            if (!Objects.equals(currentPartValue, newValue)) {
                this.currentPartValue = newValue;
                setModelValue(newValue, true);
            }
        }

        public void setPartValueProgrammatically(String value) {
            parseValue(value);
            String newValue = generateModelValueInternal();
            if (!Objects.equals(currentPartValue, newValue)) {
                this.currentPartValue = newValue;
                setModelValue(newValue, false);
            }
        }

        private String generateModelValueInternal() {
            String mode = modeSelect.getValue();
            if (mode == null) mode = EVERY;

            try {
                switch (mode) {
                    case SPECIFIC:
                        Set<Integer> selected = specificValues.getValue();
                        if (selected == null || selected.isEmpty()) return EVERY;
                        return selected.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
                    case RANGE:
                        Integer start = rangeStart.getValue();
                        Integer end = rangeEnd.getValue();
                        if (start == null || end == null || start < min || start > max || end < min || end > max || start > end) {
                            return EVERY;
                        }
                        return start + "-" + end;
                    case STEP:
                        Integer step = stepValue.getValue();
                        if (step == null || step <= 0 || step > max) {
                            return EVERY;
                        }
                        return EVERY + "/" + step;
                    case STEP_RANGE:
                        Integer stepStartVal = stepStart.getValue();
                        Integer stepRangeVal = stepValue.getValue();
                        if (stepStartVal == null || stepRangeVal == null || stepRangeVal <= 0 || stepStartVal < min || stepStartVal > max || stepRangeVal > max) {
                            return EVERY;
                        }
                        return stepStartVal + "/" + stepRangeVal;
                    case NO_SPECIFIC:
                        return NO_SPECIFIC;
                    case EVERY:
                    default:
                        return EVERY;
                }
            } catch (Exception e) {
                System.err.println("Error generating model value for " + partName + ": " + e.getMessage());
                return EVERY;
            }
        }

        public void parseValue(String value) {
            String valueToParse = (value == null || value.trim().isEmpty()) ? EVERY : value.trim();

            try {
                if (EVERY.equals(valueToParse)) {
                    modeSelect.setValue(EVERY);
                } else if (NO_SPECIFIC.equals(valueToParse) && questionMarkAllowed) {
                    modeSelect.setValue(NO_SPECIFIC);
                } else if (valueToParse.contains(",")) {
                    modeSelect.setValue(SPECIFIC);
                    Set<Integer> values = Arrays.stream(valueToParse.split(","))
                            .map(String::trim)
                            .map(Integer::parseInt)
                            .filter(v -> v >= min && v <= max)
                            .collect(Collectors.toSet());
                    specificValues.setValue(values);
                } else if (valueToParse.contains("-")) {
                    modeSelect.setValue(RANGE);
                    String[] parts = valueToParse.split("-");
                    if (parts.length == 2) {
                        rangeStart.setValue(Integer.parseInt(parts[0]));
                        rangeEnd.setValue(Integer.parseInt(parts[1]));
                    } else {
                        throw new IllegalArgumentException("Invalid range format: " + valueToParse);
                    }
                } else if (valueToParse.contains("/")) {
                    String[] parts = valueToParse.split("/");
                    if (parts.length == 2) {
                        int step = Integer.parseInt(parts[1]);
                        stepValue.setValue(step);
                        if (EVERY.equals(parts[0])) {
                            modeSelect.setValue(STEP);
                            stepStart.clear();
                        } else {
                            modeSelect.setValue(STEP_RANGE);
                            stepStart.setValue(Integer.parseInt(parts[0]));
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid step format: " + valueToParse);
                    }
                } else {
                    modeSelect.setValue(SPECIFIC);
                    int singleValue = Integer.parseInt(valueToParse);
                    if (singleValue >= min && singleValue <= max) {
                        specificValues.setValue(Collections.singleton(singleValue));
                    } else {
                        throw new IllegalArgumentException("Value out of range: " + valueToParse);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse cron part '" + valueToParse + "' for " + partName + ": " + e.getMessage() + ". Resetting to '*'.");
                modeSelect.setValue(EVERY);
            } finally {
                onModeChange(modeSelect.getValue());
                this.currentPartValue = generateModelValueInternal();
            }
        }

        @Override
        protected String generateModelValue() {
            return this.currentPartValue;
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            parseValue(newPresentationValue);
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            super.setReadOnly(readOnly);
            modeSelect.setReadOnly(readOnly);
            specificValues.setReadOnly(readOnly);
            rangeStart.setReadOnly(readOnly);
            rangeEnd.setReadOnly(readOnly);
            stepValue.setReadOnly(readOnly);
            stepStart.setReadOnly(readOnly);
        }

    }
}