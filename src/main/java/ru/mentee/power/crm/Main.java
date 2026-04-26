package ru.mentee.power.crm;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.service.LeadService;
import ru.mentee.power.crm.servlet.LeadListServlet;

public class Main {
  public static void main(String[] args) throws Exception {
    Tomcat tomcat = new Tomcat();
    tomcat.setPort(8080);

    LeadRepository repository = new LeadRepository();
    LeadService leadService = new LeadService(repository);

    leadService.addLead("John", "john@example.com", "+123456789", "Tech Corp", "NEW");
    leadService.addLead("Jane", "jane@example.com", "+987654321", "Design Studio", "QUALIFIED");
    leadService.addLead("Ron", "ron@example.com", "+712345678", "Example Corp", "QUALIFIED");
    leadService.addLead("Alex", "alex@example.com", "+134567890", "Prompt Studio", "NEW");
    leadService.addLead("Marty", "marty@example.com", "+987654321", "Merge Corp", "NEW");
    leadService.addLead("Hacker", "<script>alert('XSS')</script>", "+999999999", "Hacker Corp", "NEW");

    Context context = tomcat.addContext("", new File(".").getAbsolutePath());
    context.getServletContext().setAttribute("leadService", leadService);

    tomcat.addServlet(context, "LeadListServlet", new LeadListServlet());
    context.addServletMappingDecoded("/leads", "LeadListServlet");

    tomcat.getConnector();
    tomcat.start();
    System.out.println("Tomcat started on port 8080");
    System.out.println("Open http://localhost:8080/leads in browser");
    tomcat.getServer().await();
  }
}
