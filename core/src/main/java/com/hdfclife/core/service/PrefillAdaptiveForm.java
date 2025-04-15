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

@Component
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

            // Create JSON structure
            ObjectNode rootNode = objectMapper.createObjectNode();
            ObjectNode afDataNode = objectMapper.createObjectNode();
            ObjectNode afBoundDataNode = objectMapper.createObjectNode();
            ObjectNode customerApplicationNode = objectMapper.createObjectNode();
            
            // Create applicant details
            ObjectNode applicantDetailNode = objectMapper.createObjectNode();
            applicantDetailNode.put("contactPerson", userData.getOrDefault("givenName", ""));
            applicantDetailNode.put("contactPersonEmail", userData.getOrDefault("email", ""));
            applicantDetailNode.put("contactPersonCompany", userData.getOrDefault("company", ""));
            applicantDetailNode.put("contactPersonCity", userData.getOrDefault("city", ""));
            applicantDetailNode.put("contactPersonMobile", userData.getOrDefault("mobile", ""));

            // Create bank details
            ObjectNode bankDetailNode = objectMapper.createObjectNode();
            bankDetailNode.put("iban", userData.getOrDefault("iban", ""));
            bankDetailNode.put("swiftCode", userData.getOrDefault("swiftCode", ""));

            // Create signer details
            ObjectNode signerDetailNode = objectMapper.createObjectNode();
            signerDetailNode.put("signerName", userData.getOrDefault("givenName", ""));

            // Build the complete JSON structure
            customerApplicationNode.set("applicantDetail", applicantDetailNode);
            customerApplicationNode.set("bankDetail", bankDetailNode);
            customerApplicationNode.set("signerDetail", signerDetailNode);
            afBoundDataNode.set("customerApplication", customerApplicationNode);
            afDataNode.set("afBoundData", afBoundDataNode);
            rootNode.set("afData", afDataNode);

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
