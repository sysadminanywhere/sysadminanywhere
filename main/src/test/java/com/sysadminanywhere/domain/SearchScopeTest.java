package com.sysadminanywhere.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchScopeTest {

    @Test
    void shouldHaveCorrectNumberOfValues() {
        assertEquals(3, SearchScope.values().length);
    }

    @Test
    void shouldContainExpectedValues() {
        assertNotNull(SearchScope.OBJECT);
        assertNotNull(SearchScope.ONELEVEL);
        assertNotNull(SearchScope.SUBTREE);
    }

    @Test
    void valueOf_shouldMatchExactCase() {
        assertEquals(SearchScope.OBJECT, SearchScope.valueOf("OBJECT"));
        assertEquals(SearchScope.ONELEVEL, SearchScope.valueOf("ONELEVEL"));
        assertEquals(SearchScope.SUBTREE, SearchScope.valueOf("SUBTREE"));
    }

    @Test
    void valueOf_shouldBeCaseSensitive() {
        assertThrows(IllegalArgumentException.class, () -> {
            SearchScope.valueOf("object");
        });
    }

    @Test
    void values_shouldReturnAllScopes() {
        SearchScope[] scopes = SearchScope.values();
        
        assertEquals(SearchScope.OBJECT, scopes[0]);
        assertEquals(SearchScope.ONELEVEL, scopes[1]);
        assertEquals(SearchScope.SUBTREE, scopes[2]);
    }
}
