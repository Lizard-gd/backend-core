package ru.mentee.power.crm.servlet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import gg.jte.TemplateEngine;
import gg.jte.output.PrintWriterOutput;
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
@SuppressWarnings("unchecked")
class LeadListServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private ServletContext servletContext;

  @Mock
  private LeadService leadService;

  @Mock
  private TemplateEngine templateEngine;

  private LeadListServlet servlet;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  // Расширяем сервлет для тестирования
  private static class TestableLeadListServlet extends LeadListServlet {
    private ServletContext mockServletContext;
    private TemplateEngine mockTemplateEngine;

    void setMockServletContext(ServletContext context) {
      this.mockServletContext = context;
    }

    void setMockTemplateEngine(TemplateEngine engine) {
      this.mockTemplateEngine = engine;
    }

    @Override
    public ServletContext getServletContext() {
      return mockServletContext;
    }

    @Override
    public void init() {
      try {
        Field templateEngineField = LeadListServlet.class.getDeclaredField("templateEngine");
        templateEngineField.setAccessible(true);
        templateEngineField.set(this, mockTemplateEngine);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    servlet = new TestableLeadListServlet();
    ((TestableLeadListServlet) servlet).setMockServletContext(servletContext);
    ((TestableLeadListServlet) servlet).setMockTemplateEngine(templateEngine);
    servlet.init();

    when(servletContext.getAttribute("leadService")).thenReturn(leadService);
  }

  @Test
  void shouldSetContentTypeToHtml_whenDoGetCalled() throws Exception {
    // Given
    when(leadService.findAll()).thenReturn(List.of());

    // When
    servlet.doGet(request, response);

    // Then
    verify(response).setContentType("text/html; charset=UTF-8");
  }

  @Test
  void shouldCallTemplateEngineRender_whenDoGetCalled() throws Exception {
    // Given
    List<Lead> testLeads = List.of(
            new Lead("1", "test1", "test1@example.com",
                    "+111", "Company A", "NEW", LocalDateTime.now()),
            new Lead("2", "test2", "test2@example.com",
                    "+222", "Company B", "QUALIFIED", LocalDateTime.now())
    );

    when(leadService.findAll()).thenReturn(testLeads);

    // When
    servlet.doGet(request, response);

    // Then
    verify(templateEngine).render(
            eq("leads/list.jte"),
            any(Map.class),
            any(PrintWriterOutput.class)
    );
  }
}