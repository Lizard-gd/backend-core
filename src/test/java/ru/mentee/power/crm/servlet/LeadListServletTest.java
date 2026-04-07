package ru.mentee.power.crm.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

@ExtendWith(MockitoExtension.class)
class LeadListServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private ServletContext servletContext;

  @Mock
  private LeadService leadService;

  private TestableLeadListServlet servlet;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  private static class TestableLeadListServlet extends LeadListServlet {
    private ServletContext mockServletContext;

    void setMockServletContext(ServletContext context) {
      this.mockServletContext = context;
    }

    @Override
  public ServletContext getServletContext() {
      return mockServletContext;
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    servlet = new TestableLeadListServlet();
    servlet.setMockServletContext(servletContext);
  }

  @Test
  void shouldSetContentTypeToHtml_whenDoGetCalled() throws Exception {
        // Given
    when(servletContext.getAttribute("leadService")).thenReturn(leadService);
    when(leadService.findAll()).thenReturn(List.of());

        // When
    servlet.doGet(request, response);

        // Then
    verify(response).setContentType("text/html; charset=UTF-8");
  }

  @Test
  void shouldGenerateHtmlTable_whenDoGetCalledWithLeads() throws Exception {
        // Given: создаем тестовых лидов
    List<Lead> testLeads = List.of(
                new Lead("1", "test1@example.com", "+111", "Company A", "NEW"),
                new Lead("2", "test2@example.com", "+222", "Company B", "QUALIFIED")
        );

    when(servletContext.getAttribute("leadService")).thenReturn(leadService);
    when(leadService.findAll()).thenReturn(testLeads);

        // When
    servlet.doGet(request, response);

        // Then
    printWriter.flush();
    String html = stringWriter.toString();

    assertThat(html).contains("<table");
    assertThat(html).contains("test1@example.com");
    assertThat(html).contains("Company B");
    assertThat(html).contains("QUALIFIED");
  }
}