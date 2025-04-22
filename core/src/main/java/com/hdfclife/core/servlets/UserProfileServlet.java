package com.hdfclife.core.servlets;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hdfclife.core.service.UserDataService;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/userprofile")
public class UserProfileServlet extends SlingAllMethodsServlet {
    
    private static final Logger log = LoggerFactory.getLogger(UserProfileServlet.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Reference
    private UserDataService userDataService;

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Read the JSON data from request body
            String jsonData = IOUtils.toString(request.getReader());
            log.info("Received user profile data from the request: {}", jsonData);

            // Validate JSON structure
            ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(jsonData);
            if (!jsonNode.has("mobileNumber")) {
                throw new ServletException("Mobile number is required in the JSON data");
            }

            // Save the data
            userDataService.saveUserData(jsonData);

            // Send success response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ObjectNode responseJson = objectMapper.createObjectNode();
            responseJson.put("status", "success");
            responseJson.put("message", "User profile data saved successfully");
            response.getWriter().write(responseJson.toString());

        } catch (Exception e) {
            log.error("Error processing user profile data", e);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("status", "error");
            errorJson.put("message", e.getMessage());
            response.getWriter().write(errorJson.toString());
        }
    }
} 