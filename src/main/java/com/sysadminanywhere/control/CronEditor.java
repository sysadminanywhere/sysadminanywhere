package com.sysadminanywhere.control;

import com.vaadin.flow.component.AbstractField;
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
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CronEditor extends CustomField<String> {

    // --- Константы режимов ---
    protected static final String EVERY = "*";
    protected static final String SPECIFIC = "Specific";
    protected static final String RANGE = "Range";
    protected static final String STEP = "Step"; // Для "*/X"
    protected static final String STEP_RANGE = "Step Range"; // Для "A/X"
    protected static final String NO_SPECIFIC = "?"; // Только для DayOfMonth/DayOfWeek

    // --- UI Компоненты ---
    private final FormLayout formLayout = new FormLayout();
    private final TextField generatedCron = new TextField("Generated Cron");

    // --- Редакторы для каждой части Cron ---
    private final CronPartEditor minutesEditor;
    private final CronPartEditor hoursEditor;
    private final CronPartEditor dayOfMonthEditor;
    private final CronPartEditor monthEditor;
    private final CronPartEditor dayOfWeekEditor;

    /**
     * Конструктор по умолчанию.
     */
    public CronEditor() {
        this("Cron Configuration");
    }

    /**
     * Конструктор с меткой.
     * @param label Метка для компонента.
     */
    public CronEditor(String label) {
        // Устанавливаем значение по умолчанию через конструктор CustomField
        super("0 * * * * ?"); // Default: Every minute at second 0

        setLabel(label);

        minutesEditor = new CronPartEditor("Minutes", 0, 59, false);
        hoursEditor = new CronPartEditor("Hours", 0, 23, false);
        dayOfMonthEditor = new CronPartEditor("Day of Month", 1, 31, true);
        monthEditor = new CronPartEditor("Month", 1, 12, false, getMonthNames());
        // Стандарт Cron: 0=SUN, 1=MON, ..., 6=SAT (или 1-7, но 0-6 чаще)
        dayOfWeekEditor = new CronPartEditor("Day of Week", 0, 6, true, getDayOfWeekNames());

        generatedCron.setWidthFull();
        generatedCron.setReadOnly(true); // Поле генерации всегда только для чтения

        // ? super ComponentValueChangeEvent<CustomField<String>, String>>

        // Слушатель изменений в любом из дочерних редакторов
        HasValue.ValueChangeListener<? super ComponentValueChangeEvent<CustomField<String>, String>> internalListener = e -> {
            // Проверяем, не вызвано ли изменение программно для ?
            if (!e.isFromClient() && Objects.equals(e.getValue(), NO_SPECIFIC)) {
                // Если ? установлен программно из handleDayMonthExclusivity,
                // то просто обновим строку без лишних проверок
                updateCronString();
            } else if (e.isFromClient()) {
                // Если изменение от пользователя в дочернем компоненте
                updateCronString();
            }
            // Если изменение программное, но не '?', то скорее всего это результат setValue,
            // и строка обновится в setPresentationValue -> updateGeneratedCronFieldOnly
        };

        minutesEditor.addValueChangeListener(internalListener);
        hoursEditor.addValueChangeListener(internalListener);
        dayOfMonthEditor.addValueChangeListener(internalListener);
        monthEditor.addValueChangeListener(internalListener);
        dayOfWeekEditor.addValueChangeListener(internalListener);

        // Обработка взаимной эксклюзивности '?'
        dayOfMonthEditor.addValueChangeListener(e -> handleDayMonthExclusivity(dayOfMonthEditor, dayOfWeekEditor, e.isFromClient()));
        dayOfWeekEditor.addValueChangeListener(e -> handleDayMonthExclusivity(dayOfWeekEditor, dayOfMonthEditor, e.isFromClient()));

        // --- Настройка Layout ---
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2) // Два столбца на экранах шире 500px
        );
        formLayout.add(
                minutesEditor, hoursEditor,
                dayOfMonthEditor, monthEditor, dayOfWeekEditor
        );
        // Растянем последний элемент на всю ширину, если колонок 2
        formLayout.setColspan(dayOfWeekEditor, 2);

        VerticalLayout mainLayout = new VerticalLayout(formLayout, generatedCron);
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);
        add(mainLayout);

        // Обновляем отображаемое поле `generatedCron` начальным значением
        updateGeneratedCronFieldOnly();
    }

    // --- Вспомогательные методы для имен ---

    private Map<Integer, String> getMonthNames() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            // Используем SHORT для краткости, Locale.ENGLISH как стандарт для Cron
            map.put(i, Month.of(i).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH).toUpperCase());
        }
        return map;
    }

    private Map<Integer, String> getDayOfWeekNames() {
        Map<Integer, String> map = new LinkedHashMap<>();
        // Стандарт Cron: 0=SUN, 1=MON, ..., 6=SAT
        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int i = 0; i <= 6; i++) {
            map.put(i, String.format("%d (%s)", i, days[i]));
        }
        return map;
    }

    // --- Логика эксклюзивности '?' ---

    private void handleDayMonthExclusivity(CronPartEditor changedEditor, CronPartEditor otherEditor, boolean fromClient) {
        // Реагируем только на изменения от пользователя, чтобы избежать зацикливания
        if (!fromClient) {
            return;
        }
        if (!changedEditor.isQuestionMarkAllowed() || !otherEditor.isQuestionMarkAllowed()) return;

        String changedValue = changedEditor.generateModelValue(); // Текущее значение изменившегося
        String otherValue = otherEditor.generateModelValue();     // Текущее значение другого

        if (!NO_SPECIFIC.equals(changedValue) && !EVERY.equals(changedValue)) {
            // Если в измененном поле выбрано что-то конкретное (не * и не ?),
            // то в другом поле должен быть '?'
            if (!NO_SPECIFIC.equals(otherValue)) {
                // Устанавливаем '?' в другом редакторе.
                // Это вызовет его ValueChangeListener -> internalListener -> updateCronString
                otherEditor.setPartValueProgrammatically(NO_SPECIFIC); // Используем спец. метод
            }
        } else if (NO_SPECIFIC.equals(changedValue)) {
            // Если в измененном поле выбрали '?', убедимся что другое поле не '?'
            if (NO_SPECIFIC.equals(otherValue)) {
                // Ставим '*' в другом, если оба были '?'
                otherEditor.setPartValueProgrammatically(EVERY); // Используем спец. метод
            }
        }
        // Обновление строки произойдет через цепочку событий от otherEditor
    }

    // --- Основная логика CustomField ---

    /**
     * Обновляет значение всего компонента CronEditor и текстовое поле,
     * вызывая setModelValue для уведомления внешних слушателей.
     * Вызывается, когда изменяется значение одного из дочерних редакторов.
     */
    private void updateCronString() {
        String newCronValue = generateModelValue(); // Генерируем актуальную строку из UI
        generatedCron.setValue(newCronValue);       // Обновляем текстовое поле для отображения

        // Уведомляем фреймворк и внешних слушателей об изменении значения.
        // true - означает, что изменение пришло от клиента (из дочерних UI контролов)
        setModelValue(newCronValue, true);
    }

    /**
     * Обновляет только текстовое поле generatedCron без вызова setModelValue.
     * Используется при инициализации и в setPresentationValue для синхронизации отображения.
     */
    private void updateGeneratedCronFieldOnly() {
        // Генерируем значение из текущего состояния дочерних редакторов
        // и просто отображаем его в текстовом поле.
        generatedCron.setValue(generateModelValue());
    }

    /**
     * Генерирует полную cron-строку на основе текущего состояния дочерних редакторов.
     * Этот метод вызывается CustomField для получения текущего значения компонента.
     * @return Собранная Cron-строка.
     */
    @Override
    protected String generateModelValue() {
        // Собираем строку из значений дочерних редакторов
        return String.join(" ",
                "0", // Секунды пока не поддерживаем, всегда 0
                minutesEditor.generateModelValue(),
                hoursEditor.generateModelValue(),
                dayOfMonthEditor.generateModelValue(),
                monthEditor.generateModelValue(),
                dayOfWeekEditor.generateModelValue()
                // Год обычно не используется в стандартном cron
        );
    }

    /**
     * Обновляет внутренние UI компоненты (дочерние редакторы) на основе
     * нового значения, установленного извне через setValue().
     * Этот метод вызывается CustomField после setModelValue(value, false).
     * @param newPresentationValue Новая строка Cron для отображения.
     */
    @Override
    protected void setPresentationValue(String newPresentationValue) {
        String valueToParse = newPresentationValue;
        // Используем значение по умолчанию, если пришел null
        if (valueToParse == null) {
            valueToParse = "0 * * * * ?";
        }

        String[] parts = valueToParse.trim().split("\\s+");
        boolean parsedOk = false;
        if (parts.length >= 6) { // Ожидаем как минимум 6 частей (включая секунды)
            try {
                // Игнорируем секунды parts[0] при парсинге UI
                minutesEditor.parseValue(parts[1]);
                hoursEditor.parseValue(parts[2]);
                dayOfMonthEditor.parseValue(parts[3]);
                monthEditor.parseValue(parts[4]);
                dayOfWeekEditor.parseValue(parts[5]);
                parsedOk = true;
            } catch (Exception e) {
                System.err.println("Error parsing cron string in setPresentationValue: '" + valueToParse + "' - " + e.getMessage());
                // Ошибка парсинга, далее сбросим UI к дефолту
                parsedOk = false;
            }
        } else {
            System.err.println("Invalid cron string format in setPresentationValue: '" + valueToParse + "' - expected at least 6 parts.");
        }

        // Если парсинг не удался или формат некорректен, сбрасываем UI к дефолту
        if (!parsedOk) {
            minutesEditor.parseValue("*");
            hoursEditor.parseValue("*");
            dayOfMonthEditor.parseValue("*");
            monthEditor.parseValue("*");
            dayOfWeekEditor.parseValue("?"); // Стандартный безопасный дефолт
        }

        // ВАЖНО: После обновления дочерних UI, нужно синхронизировать
        // отображаемую строку в `generatedCron`, так как парсинг мог
        // нормализовать или изменить исходное значение (например, при ошибке).
        // НЕ вызывайте здесь updateCronString() или setModelValue(), чтобы избежать рекурсии!
        updateGeneratedCronFieldOnly();
    }

    // --- ReadOnly ---
    @Override
    public void setReadOnly(boolean readOnly) {
        // Убедимся, что базовый класс обработал readOnly
        super.setReadOnly(readOnly);
        // Установим readOnly для всех дочерних редакторов
        minutesEditor.setReadOnly(readOnly);
        hoursEditor.setReadOnly(readOnly);
        dayOfMonthEditor.setReadOnly(readOnly);
        monthEditor.setReadOnly(readOnly);
        dayOfWeekEditor.setReadOnly(readOnly);
        // Поле generatedCron всегда read-only, его состояние не меняем.
    }

    // --- Внутренний класс для редактирования одной части Cron ---
    private static class CronPartEditor extends CustomField<String> {

        @Getter // Lombok
        private final String partName;
        private final int min;
        private final int max;
        @Getter // Lombok
        private final boolean questionMarkAllowed;
        private final Map<Integer, String> valueMap; // Для имен месяцев/дней недели

        // --- UI Компоненты этой части ---
        private final RadioButtonGroup<String> modeSelect = new RadioButtonGroup<>();
        private final MultiSelectComboBox<Integer> specificValues = new MultiSelectComboBox<>();
        private final IntegerField rangeStart = new IntegerField();
        private final IntegerField rangeEnd = new IntegerField();
        private final IntegerField stepValue = new IntegerField();
        private final IntegerField stepStart = new IntegerField(); // Для A/X

        // --- Layouts для режимов ---
        private final VerticalLayout editorLayout = new VerticalLayout(); // Основной layout части
        private final HorizontalLayout specificLayout = new HorizontalLayout(specificValues);
        private final HorizontalLayout rangeLayout = new HorizontalLayout(new Span("From"), rangeStart, new Span("to"), rangeEnd);
        private final HorizontalLayout stepLayout = new HorizontalLayout(new Span("Every"), stepValue); // */X
        private final HorizontalLayout stepRangeLayout = new HorizontalLayout(new Span("Every"), stepValue, new Span("starting at"), stepStart); // A/X

        // Храним последнее сгенерированное/установленное значение для этой части
        private String currentPartValue = "*";

        /**
         * Конструктор редактора части Cron.
         */
        public CronPartEditor(String partName, int min, int max, boolean questionMarkAllowed) {
            this(partName, min, max, questionMarkAllowed, null);
        }

        /**
         * Конструктор редактора части Cron с картой имен (для месяцев/дней недели).
         */
        public CronPartEditor(String partName, int min, int max, boolean questionMarkAllowed, Map<Integer, String> valueMap) {
            super(EVERY); // Начальное значение по умолчанию для этой части
            this.partName = partName;
            this.min = min;
            this.max = max;
            this.questionMarkAllowed = questionMarkAllowed;
            this.valueMap = valueMap; // null если используются просто числа

            setLabel(partName); // Устанавливаем метку для этой части

            // --- Настройка режимов ---
            List<String> modes = new ArrayList<>(Arrays.asList(EVERY, SPECIFIC, RANGE, STEP, STEP_RANGE));
            if (questionMarkAllowed) {
                modes.add(NO_SPECIFIC);
            }
            modeSelect.setItems(modes);
            modeSelect.setValue(EVERY); // Режим по умолчанию

            // --- Настройка полей ввода ---
            setupSpecificValues();
            setupRangeFields();
            setupStepFields();

            // --- Настройка Layouts ---
            configureLayouts();

            // --- Слушатели изменений ---
            // Слушатель для внутренних компонентов UI этой части
            HasValue.ValueChangeListener<ComponentValueChangeEvent<?, ?>> internalUiListener = e -> {
                if (e.isFromClient()) { // Реагируем только на действия пользователя
                    updatePartValue();
                }
            };
            specificValues.addValueChangeListener(internalUiListener);
            rangeStart.addValueChangeListener(internalUiListener);
            rangeEnd.addValueChangeListener(internalUiListener);
            stepValue.addValueChangeListener(internalUiListener);
            stepStart.addValueChangeListener(internalUiListener);

            // Слушатель для смены режима
            modeSelect.addValueChangeListener(e -> {
                if (e.isFromClient()) { // Реагируем только на действия пользователя
                    onModeChange(e.getValue()); // Обновить видимость и очистить поля
                    updatePartValue();          // Обновить значение части
                }
            });

            specificLayout.setSpacing(false);
            specificLayout.getThemeList().add("spacing-s");

            rangeLayout.setSpacing(false);
            rangeLayout.getThemeList().add("spacing-s");

            stepLayout.setSpacing(false);
            stepLayout.getThemeList().add("spacing-s");

            stepRangeLayout.setSpacing(false);
            stepRangeLayout.getThemeList().add("spacing-s");

            // --- Добавление компонентов в Layout ---
            editorLayout.add(modeSelect, specificLayout, rangeLayout, stepLayout, stepRangeLayout);
            add(editorLayout); // Добавляем основной layout в CustomField

            // Инициализация видимости полей
            onModeChange(EVERY); // Показать/скрыть поля для режима по умолчанию
            this.currentPartValue = generateModelValueInternal(); // Установить начальное значение
        }

        // --- Настройка UI элементов ---

        private void setupSpecificValues() {
            List<Integer> items = IntStream.rangeClosed(min, max).boxed().collect(Collectors.toList());
            specificValues.setItems(items);
            if (valueMap != null) {
                // Используем карту имен для отображения
                specificValues.setItemLabelGenerator(i -> valueMap.getOrDefault(i, String.valueOf(i)));
            } else {
                specificValues.setItemLabelGenerator(String::valueOf);
            }
            specificValues.setWidth("300px"); // Даем больше места для MultiSelect
        }

        private void setupRangeFields() {
            rangeStart.setMin(min); rangeStart.setMax(max); rangeStart.setStepButtonsVisible(true);
            rangeEnd.setMin(min); rangeEnd.setMax(max); rangeEnd.setStepButtonsVisible(true);
            rangeStart.setWidth("80px"); rangeEnd.setWidth("80px");
        }

        private void setupStepFields() {
            stepValue.setMin(1); stepValue.setMax(max); stepValue.setStepButtonsVisible(true); // Шаг от 1
            stepStart.setMin(min); stepStart.setMax(max); stepStart.setStepButtonsVisible(true);
            stepValue.setWidth("80px"); stepStart.setWidth("80px");
        }

        private void configureLayouts() {
            specificLayout.setVisible(false); specificLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            rangeLayout.setVisible(false); rangeLayout.setAlignItems(FlexComponent.Alignment.BASELINE); rangeLayout.setSpacing(false);
            stepLayout.setVisible(false); stepLayout.setAlignItems(FlexComponent.Alignment.BASELINE); stepLayout.setSpacing(false);
            stepRangeLayout.setVisible(false); stepRangeLayout.setAlignItems(FlexComponent.Alignment.BASELINE); stepRangeLayout.setSpacing(false);

            editorLayout.setPadding(false);
            editorLayout.setSpacing(false); // Уменьшаем вертикальные отступы
        }

        // --- Обработка смены режима ---

        private void onModeChange(String newMode) {
            // Скрываем/показываем layouts в зависимости от режима
            specificLayout.setVisible(SPECIFIC.equals(newMode));
            rangeLayout.setVisible(RANGE.equals(newMode));
            stepLayout.setVisible(STEP.equals(newMode));
            stepRangeLayout.setVisible(STEP_RANGE.equals(newMode));

            // Очищаем поля, которые не относятся к новому режиму
            clearUnusedFields(newMode);
        }

        /** Очищает значения полей, не используемых в текущем режиме. */
        private void clearUnusedFields(String currentMode) {
            if (!SPECIFIC.equals(currentMode)) specificValues.clear();
            if (!RANGE.equals(currentMode)) { rangeStart.clear(); rangeEnd.clear(); }
            // Поле stepValue используется в STEP и STEP_RANGE
            if (!STEP.equals(currentMode) && !STEP_RANGE.equals(currentMode)) stepValue.clear();
            if (!STEP_RANGE.equals(currentMode)) stepStart.clear();
        }

        // --- Логика обновления значения части ---

        /** Вызывается, когда внутренние UI компоненты изменяются пользователем. */
        private void updatePartValue() {
            String newValue = generateModelValueInternal(); // Генерируем значение из текущего UI
            // Сравниваем с предыдущим значением, чтобы избежать лишних событий, если значение не изменилось
            if (!Objects.equals(currentPartValue, newValue)) {
                this.currentPartValue = newValue; // Обновляем внутреннее состояние
                // Уведомляем родительский компонент (CronEditor) об изменении значения этой части
                // true - изменение от клиента (пользователя)
                setModelValue(newValue, true);
            }
        }

        /**
         * Устанавливает значение этой части программно (например, для '?').
         * Обновляет UI и вызывает setModelValue с флагом fromClient=false.
         * @param value Новое значение для установки.
         */
        public void setPartValueProgrammatically(String value) {
            parseValue(value); // Обновляем UI в соответствии со значением
            String newValue = generateModelValueInternal(); // Получаем значение после парсинга
            if (!Objects.equals(currentPartValue, newValue)) {
                this.currentPartValue = newValue;
                // Уведомляем родительский компонент, false - изменение не от пользователя
                setModelValue(newValue, false);
            }
        }

        // --- Генерация и Парсинг значения части ---

        /**
         * Генерирует строковое представление этой части Cron на основе текущего состояния UI.
         * @return Строка для этой части Cron (e.g., "*", "1,5", "10-20/2", "?").
         */
        private String generateModelValueInternal() {
            String mode = modeSelect.getValue();
            if (mode == null) mode = EVERY; // Обработка случая, когда значение еще не установлено

            try {
                switch (mode) {
                    case SPECIFIC:
                        Set<Integer> selected = specificValues.getValue();
                        if (selected == null || selected.isEmpty()) return EVERY;
                        return selected.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
                    case RANGE:
                        Integer start = rangeStart.getValue();
                        Integer end = rangeEnd.getValue();
                        // Добавляем валидацию
                        if (start == null || end == null || start < min || start > max || end < min || end > max || start > end) {
                            return EVERY; // Возвращаем '*' при невалидном диапазоне
                        }
                        return start + "-" + end;
                    case STEP: // */X
                        Integer step = stepValue.getValue();
                        if (step == null || step <= 0 || step > max) {
                            return EVERY; // Невалидный шаг
                        }
                        return EVERY + "/" + step;
                    case STEP_RANGE: // A/X
                        Integer stepStartVal = stepStart.getValue();
                        Integer stepRangeVal = stepValue.getValue();
                        // Добавляем валидацию
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
                return EVERY; // Возвращаем '*' при любой ошибке генерации
            }
        }

        /**
         * Парсит строковое значение части Cron и обновляет соответствующие UI компоненты.
         * @param value Строка для парсинга (e.g., "*", "1,5", "10-20/2", "?").
         */
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
                    } else { throw new IllegalArgumentException("Invalid range format: " + valueToParse); }
                } else if (valueToParse.contains("/")) {
                    String[] parts = valueToParse.split("/");
                    if (parts.length == 2) {
                        int step = Integer.parseInt(parts[1]);
                        stepValue.setValue(step); // Устанавливаем шаг
                        if (EVERY.equals(parts[0])) {
                            modeSelect.setValue(STEP);
                            stepStart.clear(); // Очищаем поле начала шага
                        } else {
                            modeSelect.setValue(STEP_RANGE);
                            stepStart.setValue(Integer.parseInt(parts[0]));
                        }
                    } else { throw new IllegalArgumentException("Invalid step format: " + valueToParse); }
                } else { // Попытка распарсить как одно число (Specific)
                    modeSelect.setValue(SPECIFIC);
                    int singleValue = Integer.parseInt(valueToParse);
                    if (singleValue >= min && singleValue <= max) {
                        specificValues.setValue(Collections.singleton(singleValue));
                    } else { throw new IllegalArgumentException("Value out of range: " + valueToParse); }
                }
            } catch (Exception e) {
                // Ошибка парсинга - сбрасываем на EVERY
                System.err.println("Failed to parse cron part '" + valueToParse + "' for " + partName + ": " + e.getMessage() + ". Resetting to '*'.");
                modeSelect.setValue(EVERY);
            } finally {
                // Обновляем видимость полей и очищаем неиспользуемые после установки режима
                onModeChange(modeSelect.getValue());
                // Сохраняем значение, которое получилось ПОСЛЕ парсинга и обновления UI
                this.currentPartValue = generateModelValueInternal();
            }
        }

        // --- Реализация CustomField для CronPartEditor ---

        /**
         * Возвращает текущее значение этой части Cron (последнее сгенерированное или установленное).
         */
        @Override
        protected String generateModelValue() {
            return this.currentPartValue;
        }

        /**
         * Обновляет UI этой части Cron на основе значения, установленного извне.
         * Вызывается CustomField после setModelValue(value, false).
         */
        @Override
        protected void setPresentationValue(String newPresentationValue) {
            // Просто вызываем наш парсер для обновления UI
            parseValue(newPresentationValue);
        }

        // --- ReadOnly для CronPartEditor ---
        @Override
        public void setReadOnly(boolean readOnly) {
            super.setReadOnly(readOnly); // Важно вызвать super!
            // Устанавливаем readOnly для всех интерактивных элементов
            modeSelect.setReadOnly(readOnly);
            specificValues.setReadOnly(readOnly);
            rangeStart.setReadOnly(readOnly);
            rangeEnd.setReadOnly(readOnly);
            stepValue.setReadOnly(readOnly);
            stepStart.setReadOnly(readOnly);
        }

        // addValueChangeListener не нужно переопределять, используется реализация CustomField
    }

    // Методы HasValue (getValue, setValue, addValueChangeListener) реализованы в CustomField.
    // Мы взаимодействуем с ними через generateModelValue, setPresentationValue и setModelValue.

}