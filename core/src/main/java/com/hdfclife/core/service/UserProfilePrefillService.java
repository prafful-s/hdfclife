package com.hdfclife.core.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

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
        log.info("HDFC Life Prefill service method called");
        try {
            log.info("Data options: {}", options.getExtras());
            String mobileNumber = (String) options.getExtras().get("id");
            log.info("Mobile number is : {}", mobileNumber);
            //String mobileNumber = "9876543220"; // Replace with actual mobile number retrieval logic
            String userData = userDataService.getUserData(mobileNumber);
            if (userData == null || userData.isEmpty()) {
                userData = "{}";
            }

            // Create JSON structure directly from the userData map
            // The map already contains the correct structure from the database
            // ObjectNode rootNode = objectMapper.createObjectNode();
            // userData.forEach((key, value) -> {
            //     if (value != null) {
            //         try {
            //             switch (key) {
            //                 case "nriCheck":
            //                 case "tobaccoCheck":
            //                 case "countryCode":
            //                 case "medicalHistoryOthers":
            //                 case "medicalHistoryHeart":
            //                     rootNode.put(key, Integer.parseInt(value));
            //                     break;
            //                 case "mobileNumber":
            //                     rootNode.put(key, Long.parseLong(value));
            //                     break;
            //                 case "annualIncome":
            //                 case "existingLifeCover":
            //                 case "height":
            //                 case "weight":
            //                     rootNode.put(key, Double.parseDouble(value));
            //                     break;
            //                 default:
            //                     rootNode.put(key, value);
            //             }
            //         } catch (NumberFormatException e) {
            //             rootNode.put(key, value);
            //         }
            //     }
            // });

            // Convert to JSON string and create input stream
            log.info("Generated JSON for prefill: {}", userData);
            InputStream inputStream = new ByteArrayInputStream(userData.getBytes());
            PrefillData prefillData = new PrefillData(inputStream, ContentType.JSON);
            return prefillData;
        } catch (Exception e) {
            log.error("Error in prefill service: ", e);
            throw new FormsException("Failed to generate prefill data", e);
        }
    }
} 