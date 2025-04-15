package com.hdfclife.core.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.forms.common.service.ContentType;
import com.adobe.forms.common.service.DataOptions;
import com.adobe.forms.common.service.DataProvider;
import com.adobe.forms.common.service.FormsException;
import com.adobe.forms.common.service.PrefillData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component(immediate = true, service = {DataProvider.class})
public class UserProfilePrefillService implements DataProvider {
    
    private static final Logger log = LoggerFactory.getLogger(UserProfilePrefillService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Reference
    private UserDataService userDataService;

    @Override
    public String getServiceDescription() {
        return "HDFC Life Prefill Service for User Profile Data";
    }

    @Override
    public String getServiceName() {
        return "HDFC Life Prefill Service for User Profile Data";
    }

    @Override
    public PrefillData getPrefillData(DataOptions options) throws FormsException {
        try {
            // SlingHttpServletRequest request = (SlingHttpServletRequest) options.getExtras().get("request");
            // log.info("DataOptions extras: {}", options.getExtras().keySet());
            // log.info("Request: {}", request);
            // log.info("Cookies: {}", request.getCookies());
            // String mobileNumber = request.getCookies() != null ? 
            //       java.util.Arrays.stream(request.getCookies())
            //           .filter(c -> "mobileNumber".equals(c.getName()))
            //           .map(javax.servlet.http.Cookie::getValue)
            //           .findFirst()
            //           .orElse(null) : null;
            // log.info("Mobile number: {}", mobileNumber);
            // Create sample JSON data (replace this with actual data from userDataService)
            ObjectNode rootNode = objectMapper.createObjectNode();
            
            // Personal Information
            rootNode.put("firstName", "Kritika");
            rootNode.put("lastName", "Goyal");
            rootNode.put("gender", "female");
            rootNode.put("dateOfBirth", "2000-01-01");
            rootNode.put("nriCheck", 0);
            rootNode.put("tobaccoCheck", 0);
            rootNode.put("countryCode", 91);
            rootNode.put("mobileNumber", 1231231231);
            rootNode.put("emailId", "goyal@gmail.com");
            
            // Communication Preferences
            rootNode.put("baseCommunicationCheck", "on");
            rootNode.put("advanceCommunicationCheck", "on");
            
            // Professional Information
            rootNode.put("qualificationDropDown", "post-graduate");
            rootNode.put("occupationDropDown", "salaried");
            rootNode.put("annualIncome", 123);
            rootNode.put("existingLifeCover", 112312313);
            
            // Health Information
            rootNode.put("height", 123);
            rootNode.put("weight", 123);
            rootNode.put("medicalHistoryOthers", 0);
            rootNode.put("medicalHistoryHeart", 0);
            
            // Additional Text Inputs
            rootNode.put("textinput1744272105398", "123");
            rootNode.put("textinput_5337428181744273137273", "123");
            rootNode.put("textinput_18708444311744272126739", "123");
            rootNode.put("textinput_17219861231744272269290", "123123");
            rootNode.put("textinput_18217582801744275499381", "123");
            
            // Panel Containers (empty arrays with one empty object)
            ArrayNode panelContainer = rootNode.putArray("panelcontainer1744093626683");
            panelContainer.addObject();
            
            ArrayNode additionalFeatures = rootNode.putArray("panelAdditionalFeaturesRepeatable");
            additionalFeatures.addObject();
            
            // Terms and Conditions
            ObjectNode termsNode = rootNode.putObject("termsandconditions1744275661352");
            termsNode.put("approvalcheckbox", "true");
            
            // Convert to JSON string
            String jsonString = objectMapper.writeValueAsString(rootNode);
            log.info("Generated prefill JSON: {}", jsonString);
            
            // Create input stream from JSON string
            InputStream is = new ByteArrayInputStream(jsonString.getBytes());
            
            // Return prefill data with JSON content type
            return new PrefillData(is, ContentType.JSON);
            
        } catch (Exception e) {
            log.error("Error generating prefill data", e);
            throw new FormsException("Failed to generate prefill data", e);
        }
    }
} 