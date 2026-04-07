package ru.mentee.power.crm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

@WebServlet("/leads")
public class LeadListServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    LeadService leadService = (LeadService) getServletContext().getAttribute("leadService");

    if (leadService == null) {
      System.err.println("ERROR: leadService is null in ServletContext!");
      response.sendError(500, "LeadService not configured");
      return;
    }

    List<Lead> leads = leadService.findAll();

    System.out.println("Found " + leads.size() + " leads");

    response.setContentType("text/html; charset=UTF-8");

    PrintWriter writer = response.getWriter();
    writer.println("<!DOCTYPE html>");
    writer.println("<html>");
    writer.println("<head><title>Lead List</title></head>");
    writer.println("<body>");
    writer.println("<h1>CRM Lead List</h1>");
    writer.println("<table border='1'>");
    writer.println("<thead><tr><th>Email</th><th>Phone</th>"
            + "<th>Company</th><th>Status</th></tr></thead>");
    writer.println("<tbody>");

    for (Lead lead : leads) {
      writer.println("<tr>");
      writer.println("<td>" + lead.email() + "</td>");
      writer.println("<td>" + lead.phone() + "</td>");
      writer.println("<td>" + lead.company() + "</td>");
      writer.println("<td>" + lead.status() + "</td>");
      writer.println("</tr>");
    }

    writer.println("</tbody>");
    writer.println("</table>");
    writer.println("</body>");
    writer.println("</html>");

    System.out.println("Response sent successfully");
  }
}
