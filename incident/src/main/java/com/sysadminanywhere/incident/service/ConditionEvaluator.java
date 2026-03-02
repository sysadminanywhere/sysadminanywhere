package com.sysadminanywhere.incident.service;

import org.springframework.expression.*;
import org.springframework.expression.spel.standard.*;
import org.springframework.expression.spel.support.*;

import java.util.Map;

public class ConditionEvaluator {

    private static final ExpressionParser parser = new SpelExpressionParser();

    public static boolean evaluate(String condition, Map<String, Object> context) {

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariables(context);

        Expression exp = parser.parseExpression(condition);

        Boolean result = exp.getValue(ctx, Boolean.class);

        return Boolean.TRUE.equals(result);
    }

}