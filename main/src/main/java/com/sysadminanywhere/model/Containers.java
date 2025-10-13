package com.sysadminanywhere.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Containers {

    private List<Container> LIST;

    public Containers() {
        LIST = new ArrayList<>();
    }

    public List<Container> getContainers() {
        return LIST;
    }

    public List<Container> getRootContainers() {
        return LIST.stream()
                .filter(container -> container.getParent() == null)
                .sorted(Comparator.comparing(Container::getName))
                .collect(Collectors.toList());
    }

    public List<Container> getChildContainers(Container parent) {
        return LIST.stream().filter(container -> Objects.equals(container.getParent(), parent))
                .sorted(Comparator.comparing(Container::getName))
                .collect(Collectors.toList());
    }

}