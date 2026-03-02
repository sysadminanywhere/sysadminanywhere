package com.sysadminanywhere.incident.service;

import com.sysadminanywhere.incident.model.FilterRule;
import com.sysadminanywhere.incident.model.Operator;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

public class FilterEvaluator {

    private static final Map<Operator, BiPredicate<Object, Object>> OPERATORS = new EnumMap<>(Operator.class);

    static {
        OPERATORS.put(Operator.EQ, (a, b) -> Objects.equals(a, b));

        OPERATORS.put(Operator.NE, (a, b) -> !Objects.equals(a, b));

        OPERATORS.put(Operator.IN, (a, b) -> {
            if (b instanceof Collection<?> col) {
                return col.contains(a);
            }
            return false;
        });

        OPERATORS.put(Operator.NOT_IN, (a, b) -> {
            if (b instanceof Collection<?> col) {
                return !col.contains(a);
            }
            return false;
        });

        OPERATORS.put(Operator.LIKE, (a, b) ->
                a.toString().toLowerCase().contains(b.toString().toLowerCase()));

        OPERATORS.put(Operator.NOT_LIKE, (a, b) ->
                !a.toString().toLowerCase().contains(b.toString().toLowerCase()));

        OPERATORS.put(Operator.GT, (a, b) ->
                Double.parseDouble(a.toString()) > Double.parseDouble(b.toString()));

        OPERATORS.put(Operator.LT, (a, b) ->
                Double.parseDouble(a.toString()) < Double.parseDouble(b.toString()));

        OPERATORS.put(Operator.GTE, (a, b) ->
                Double.parseDouble(a.toString()) >= Double.parseDouble(b.toString()));

        OPERATORS.put(Operator.LTE, (a, b) ->
                Double.parseDouble(a.toString()) <= Double.parseDouble(b.toString()));

        OPERATORS.put(Operator.REGEX, (a, b) ->
                Pattern.compile(b.toString()).matcher(a.toString()).find());
    }

    public static boolean evaluate(Object actual, FilterRule rule) {
        if (actual == null) return false;

        BiPredicate<Object, Object> predicate = OPERATORS.get(rule.getOperator());
        if (predicate == null) {
            throw new IllegalArgumentException("Unsupported operator: " + rule.getOperator());
        }

        return predicate.test(actual, rule.getValue());
    }

}