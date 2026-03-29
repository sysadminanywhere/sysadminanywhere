package com.sysadminanywhere.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    private VersionService versionService;

    @BeforeEach
    void setUp() {
        versionService = new VersionService();
        ReflectionTestUtils.setField(versionService, "version", "1.0.0");
    }

    @Test
    void getVersion_returnsVersionString() {
        String result = versionService.getVersion();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo("1.0.0");
    }

    @Test
    void getVersion_isNotNull() {
        String result = versionService.getVersion();

        assertThat(result).isNotNull();
    }

    @Test
    void getVersion_isNotEmpty() {
        String result = versionService.getVersion();

        assertThat(result).isNotEmpty();
    }

    @Test
    void getVersion_multipleCalls_consistentResults() {
        String result1 = versionService.getVersion();
        String result2 = versionService.getVersion();

        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isEqualTo("1.0.0");
        assertThat(result2).isEqualTo("1.0.0");
    }

    @Test
    void getVersion_containsExpectedPattern() {
        String result = versionService.getVersion();

        // Version should contain numbers and possibly dots
        assertThat(result).matches(".*\\d+.*");
    }

    @Test
    void getVersion_isReasonableLength() {
        String result = versionService.getVersion();

        // Version string should be reasonably short (less than 50 characters)
        assertThat(result.length()).isLessThan(50);
    }
}
