package com.hdfclife.core.service;

import com.adobe.forms.common.service.DataXMLOptions;
import com.adobe.forms.common.service.DataXMLProvider;
import com.adobe.forms.common.service.FormsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.jcr.Session;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@Component(service = PrefillAdaptiveForm.class)
public class PrefillAdaptiveForm implements DataXMLProvider {
    private static final Logger log = LoggerFactory.getLogger(PrefillAdaptiveForm.class);

    @Reference
    private UserDataService userDataService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getServiceDescription() {
        return "HDFC Life AEM Forms PreFill Service";
    }

    @Override
    public String getServiceName() {
        return "HDFCLifeAemFormsPrefillService";
    }

    @Override
    public InputStream getDataXMLForDataRef(DataXMLOptions dataXmlOptions) throws FormsException {
        log.info("HDFC Life Prefill service method called");
        Resource aemFormContainer = dataXmlOptions.getFormResource();
        ResourceResolver resolver = aemFormContainer.getResourceResolver();
        Session session = (Session) resolver.adaptTo(Session.class);
        try {
            UserManager um = ((JackrabbitSession) session).getUserManager();
            Authorizable loggedinUser = um.getAuthorizable(session.getUserID());
            log.info("The path of the user is " + loggedinUser.getPath());

            // Get user data from MySQL database using mobile number
            // For testing, you can hardcode a mobile number or get it from user properties
            String mobileNumber = "9876543210"; // Replace with actual mobile number retrieval logic
            Map<String, String> userData = userDataService.getUserData(mobileNumber);
            log.info("Retrieved user data: {}", userData);

            // Create JSON structure directly from the userData map
            // The map already contains the correct structure from the database
            ObjectNode rootNode = objectMapper.createObjectNode();
            userData.forEach((key, value) -> {
                if (value != null) {
                    try {
                        switch (key) {
                            case "nriCheck":
                            case "tobaccoCheck":
                            case "countryCode":
                            case "medicalHistoryOthers":
                            case "medicalHistoryHeart":
                                rootNode.put(key, Integer.parseInt(value));
                                break;
                            case "mobileNumber":
                                rootNode.put(key, Long.parseLong(value));
                                break;
                            case "annualIncome":
                            case "existingLifeCover":
                            case "height":
                            case "weight":
                                rootNode.put(key, Double.parseDouble(value));
                                break;
                            default:
                                rootNode.put(key, value);
                        }
                    } catch (NumberFormatException e) {
                        rootNode.put(key, value);
                    }
                }
            });

            // Convert to JSON string and create input stream
            String jsonString = objectMapper.writeValueAsString(rootNode);
            log.info("Generated JSON for prefill: {}", jsonString);
            
            return new ByteArrayInputStream(jsonString.getBytes());
        } catch (Exception e) {
            log.error("Error in prefill service: ", e);
            throw new FormsException("Failed to generate prefill data", e);
        }
    }
}
