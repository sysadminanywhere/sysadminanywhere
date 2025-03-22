package com.sysadminanywhere.service;

import com.sysadminanywhere.entity.RuleEntity;
import com.sysadminanywhere.model.monitoring.Rule;
import com.sysadminanywhere.repository.RuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleService {

    private final RuleRepository ruleRepository;
    private final List<Rule> rules;

    public RuleService(RuleRepository ruleRepository, List<Rule> rules) {
        this.ruleRepository = ruleRepository;
        this.rules = rules;
    }

    public Rule getRules(String type) {
        return rules.stream()
                .filter(rule -> rule.getClass().getSimpleName().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No rule implementation found for type: " + type));
    }

    public List<RuleEntity> getAllRules() {
        return ruleRepository.findAll();
    }

    public RuleEntity addRule(RuleEntity rule) {
        return ruleRepository.save(rule);
    }

    public RuleEntity updateRule(Long id, RuleEntity rule) {
        if (ruleRepository.existsById(id)) {
            rule.setId(id);
            return ruleRepository.save(rule);
        }
        return null;
    }

    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

}