//package com.sysadminanywhere.model.monitoring;
//
//import com.vaadin.flow.component.textfield.TextField;
//import lombok.NoArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//@Scope("prototype")
//@Slf4j
//@NoArgsConstructor
//public class ScheduledRule implements Rule {
//
//    TextField txt1 = new TextField("Test 1");
//    TextField txt2 = new TextField("Test 2");
//    TextField txt3 = new TextField("Test 3");
//
//    @Override
//    public String getName() {
//        return "Scheduled rule";
//    }
//
//    @Override
//    public String getType() {
//        return "ScheduledRule";
//    }
//
//    @Override
//    public String getDescription() {
//        return "Rule for testing";
//    }
//
//    @Override
//    public String execute(Map<String, String> parameters) {
//        log.info("Executing ScheduledRule at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//
//        return "";
//    }
//
//        /*
//
//     ┌───────────── second (0-59)
//     │ ┌───────────── minute (0 - 59)
//     │ │ ┌───────────── hour (0 - 23)
//     │ │ │ ┌───────────── day of the month (1 - 31)
//     │ │ │ │ ┌───────────── month (1 - 12) (or JAN-DEC)
//     │ │ │ │ │ ┌───────────── day of the week (0 - 7)
//     │ │ │ │ │ │          (0 or 7 is Sunday, or MON-SUN)
//     │ │ │ │ │ │
//     * * * * * *
//
//    "0 0 12 * * *" every day at 12:00
//
//    */
//
//    @Override
//    public String getDefaultCron() {
//        return "0 * * * * *";
//    }
//
//    @Override
//    public List<com.vaadin.flow.component.Component> getControls(Map<String, String> parameters) {
//        List<com.vaadin.flow.component.Component> components = new ArrayList<>();
//
//        if (!parameters.isEmpty()) {
//            if(parameters.containsKey("txt1")) txt1.setValue(parameters.get("txt1"));
//            if(parameters.containsKey("txt2")) txt2.setValue(parameters.get("txt2"));
//            if(parameters.containsKey("txt3")) txt3.setValue(parameters.get("txt3"));
//        }
//
//        components.add(txt1);
//        components.add(txt2);
//        components.add(txt3);
//
//        return components;
//    }
//
//    @Override
//    public Map<String, String> getParameters() {
//        Map<String, String> map = new HashMap<>();
//
//        map.put("txt1", txt1.getValue());
//        map.put("txt2", txt2.getValue());
//        map.put("txt3", txt3.getValue());
//
//        return map;
//    }
//
//}