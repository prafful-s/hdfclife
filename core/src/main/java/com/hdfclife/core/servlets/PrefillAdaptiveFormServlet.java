// package com.hdfclife.core.servlets;

// import com.adobe.forms.common.service.DataXMLOptions;
// import com.hdfclife.core.service.PrefillAdaptiveForm;
// import org.apache.sling.api.SlingHttpServletRequest;
// import org.apache.sling.api.SlingHttpServletResponse;
// import org.apache.sling.api.resource.Resource;
// import org.apache.sling.api.servlets.SlingAllMethodsServlet;
// import org.apache.sling.servlets.annotations.SlingServletPaths;
// import org.osgi.service.component.annotations.Component;
// import org.osgi.service.component.annotations.Reference;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import javax.servlet.Servlet;
// import java.io.IOException;
// import java.io.InputStream;
// import org.apache.commons.io.IOUtils;

// @Component(service = Servlet.class)
// @SlingServletPaths("/bin/prefill/adaptiveform")
// public class PrefillAdaptiveFormServlet extends SlingAllMethodsServlet {

//     private static final Logger log = LoggerFactory.getLogger(PrefillAdaptiveFormServlet.class);

//     @Reference
//     private PrefillAdaptiveForm prefillAdaptiveForm;

//     @Override
//     protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) 
//             throws IOException {
//         log.info("PrefillAdaptiveFormServlet doGet method called");
//         try {
//             // Get the form path from request parameter
//             String formPath = request.getParameter("formPath");
//             if (formPath == null || formPath.isEmpty()) {
//                 response.setStatus(400);
//                 response.getWriter().write("formPath parameter is required");
//                 return;
//             }

//             // Get the form resource
//             Resource formResource = request.getResourceResolver().getResource(formPath);
//             if (formResource == null) {
//                 response.setStatus(404);
//                 response.getWriter().write("Form not found at path: " + formPath);
//                 return;
//             }

//             // Create DataXMLOptions
//             DataXMLOptions options = new DataXMLOptions();
//             options.setFormResource(formResource);

//             // Call the prefill service
//             InputStream xmlData = prefillAdaptiveForm.getDataXMLForDataRef(options);
            
//             if (xmlData != null) {
//                 response.setContentType("application/xml");
//                 // Convert InputStream to String and write to response
//                 String xmlString = IOUtils.toString(xmlData, "UTF-8");
//                 response.getWriter().write(xmlString);
//             } else {
//                 response.setStatus(500);
//                 response.getWriter().write("Failed to generate prefill data");
//             }

//         } catch (Exception e) {
//             log.error("Error in prefill servlet", e);
//             response.setStatus(500);
//             response.getWriter().write("Error: " + e.getMessage());
//         }
//     }
// }
