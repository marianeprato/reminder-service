package org.reminderservice.correlation;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void generatesAndEchoesCorrelationIdWhenAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reminders");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String[] observedDuringChain = new String[1];
        FilterChain chain = (req, res) -> observedDuringChain[0] = MDC.get(CorrelationIdContext.MDC_KEY);

        filter.doFilter(request, response, chain);

        assertThat(observedDuringChain[0]).isNotBlank();
        assertThat(response.getHeader(CorrelationIdContext.HEADER_NAME)).isEqualTo(observedDuringChain[0]);
        assertThat(MDC.get(CorrelationIdContext.MDC_KEY)).isNull();
    }

    @Test
    void reusesIncomingCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reminders");
        request.addHeader(CorrelationIdContext.HEADER_NAME, "existing-id-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String[] observedDuringChain = new String[1];
        FilterChain chain = (req, res) -> observedDuringChain[0] = MDC.get(CorrelationIdContext.MDC_KEY);

        filter.doFilter(request, response, chain);

        assertThat(observedDuringChain[0]).isEqualTo("existing-id-123");
        assertThat(response.getHeader(CorrelationIdContext.HEADER_NAME)).isEqualTo("existing-id-123");
    }
}
