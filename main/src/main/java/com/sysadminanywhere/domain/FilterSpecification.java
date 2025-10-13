package com.sysadminanywhere.domain;

import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public interface FilterSpecification<T> extends Specification<T> {

    String getFilters();

}
