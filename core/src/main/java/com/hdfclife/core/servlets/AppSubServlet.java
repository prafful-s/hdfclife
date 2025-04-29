package com.hdfclife.core.servlets;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="endpoint/hdfclife/appsub",
        methods=HttpConstants.METHOD_GET)
@ServiceDescription("Servlet to handle AppSub API")
public class AppSubServlet extends SlingAllMethodsServlet {
    
    private static final Logger log = LoggerFactory.getLogger(AppSubServlet.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) 
            throws ServletException, IOException {
        log.info("Inside doGet method");
        try {
            // Set response headers first
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Read the JSON data from request body
            String jsonData = IOUtils.toString(request.getReader());
            log.info("Request body received in GET: {}", jsonData);

            // If no body or empty, return hardcoded response
            if (jsonData == null || jsonData.trim().isEmpty()) {
                log.info("Empty request body, returning hardcoded response");
                response.getWriter().write(getHardcodedResponse());
                return;
            }

            // Parse and validate request
            JsonNode requestNode = objectMapper.readTree(jsonData);
            
            // Check if request is wrapped in AppSubRequest
            if (requestNode.has("AppSubRequest")) {
                log.info("Request is wrapped in AppSubRequest, extracting inner request");
                requestNode = requestNode.get("AppSubRequest");
            }
            
            validateRequest(requestNode);

            // Get hardcoded response
            String responseJson = getHardcodedResponse();

            // Send response
            response.getWriter().write(responseJson);

        } catch (Exception e) {
            log.error("Error processing AppSub request", e);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
            ObjectNode errorJson = objectMapper.createObjectNode();
            ObjectNode head = errorJson.putObject("head");
            head.put("status", "Error");
            head.put("stausCode", "500");
            head.put("statusMsg", e.getMessage());
            head.put("errordetails", e.getMessage());
            
            response.getWriter().write(errorJson.toString());
        }
    }

    private void validateRequest(JsonNode requestNode) throws ServletException {
        // Validate head section
        JsonNode head = requestNode.get("head");
        if (head == null || !head.has("source") || !head.has("userid") || !head.has("txnid")) {
            throw new ServletException("Invalid request header: missing required fields");
        }

        // Validate body section
        JsonNode body = requestNode.get("body");
        if (body == null || !body.has("quotedtls")) {
            throw new ServletException("Invalid request body: missing quotedtls");
        }

        // Validate quotedtls
        JsonNode quotedtls = body.get("quotedtls");
        if (!quotedtls.has("sumAssured") || !quotedtls.has("term") || !quotedtls.has("lifeassured")) {
            throw new ServletException("Invalid quotedtls: missing required fields");
        }
    }

    private String getHardcodedResponse() {
        try {
            String expectedResponse = "{\"head\":{\"status\":\"Success\",\"stausCode\":\"200\",\"statusMsg\":\"OK\",\"userid\":\"pindianbank1\",\"txnid\":\"17c07009-8b06-4fe9-873e-5952067a1650\",\"errordetails\":\"\"},\"body\":{\"quoteid\":\"qco3rm37etykf\",\"appno\":\"1200026657132\",\"uwtype\":\"FULLUW\",\"qniproduct\":\"\",\"instype\":\"INST_INDV\",\"magnumbenefit\":\"Life\",\"minfactor\":\"\",\"mymixid\":\"\",\"outputjsillustration\":{\"nextPremDDt\":\"11-Jul-2025\",\"BD\":[],\"premwodisc\":701.6624999999999,\"KTAX\":0,\"PS\":[[\"InstalmentPremiumwithoutGST\",702,0,0,0,0,0,0,0,0,0,0,702],[\"InstalmentPremiumwithFirstYearGST\",828,0,0,0,0,0,0,0,0,0,0,828],[\"InstalmentPremiumwithGST2ndYearOnwards\",702,0,0,0,0,0,0,0,0,0,0,702],[null,828,0,0,0,0,0,0,0,0,0,0,828]],\"totPremium\":828,\"threeMnthPrem\":2484,\"prodname\":\"HDFCLifeClick2ProtectSuper\",\"new_ill_chngs_eff_dt\":\"20-NOV-2018\",\"annPremium\":8424,\"cumulativeprem\":[],\"quoteid\":\"qco3rm37etykf\",\"backdttax\":0,\"sumAssured\":\"10000000\",\"BT\":[[1,8023,0,0,0,10000000,0,0,0,0],[2,8023,0,0,0,10000000,0,0,0,0],[3,8023,0,0,0,10000000,0,0,0,0],[4,8023,0,0,0,10000000,0,0,0,0],[5,8023,0,0,0,10000000,0,0,0,0],[6,8023,0,0,0,10000000,0,0,0,0],[7,8023,0,0,0,10000000,0,0,0,0],[8,8023,0,0,0,10000000,0,0,0,0],[9,8023,0,0,0,10000000,0,0,0,0],[10,8023,0,0,0,10000000,0,0,0,0],[11,8023,0,0,0,10000000,0,0,0,0],[12,8023,0,0,0,10000000,0,0,0,0],[13,8023,0,0,0,10000000,0,0,0,0],[14,8023,0,0,0,10000000,0,0,0,0],[15,8023,0,0,0,10000000,0,0,0,0],[16,8023,0,0,0,10000000,0,0,0,0],[17,8023,0,0,0,10000000,0,0,0,0],[18,8023,0,0,0,10000000,0,0,0,0],[19,8023,0,0,0,10000000,0,0,0,0],[20,8023,0,0,0,10000000,0,0,0,0],[21,8023,0,0,0,10000000,0,0,0,0],[22,8023,0,0,0,10000000,0,0,0,0],[23,8023,0,0,0,10000000,0,0,0,0],[24,8023,0,0,0,10000000,0,0,0,0],[25,8023,0,0,0,10000000,0,0,0,0],[26,8023,0,0,0,10000000,0,0,0,0],[27,8023,0,0,0,10000000,0,0,0,0],[28,8023,0,0,0,10000000,0,0,0,0],[29,8023,0,0,0,10000000,0,0,0,0],[30,8023,0,0,0,10000000,0,0,0,0],[31,8023,0,0,0,10000000,0,0,0,0],[32,8023,0,0,0,10000000,0,0,0,0],[33,8023,0,0,0,10000000,0,0,0,0],[34,8023,0,0,0,10000000,0,0,0,0],[35,8023,0,0,0,10000000,0,0,0,0],[36,8023,0,0,0,10000000,0,0,0,0]],\"premium\":702,\"chMap\":\"Direct-Online\",\"GST_RT\":0,\"ST_RT\":126,\"saOnDeath\":10000000,\"uin\":\"101N145V08\",\"id\":\"P1C2PS\",\"ResIndFlg\":true,\"twoMnthPrem\":1656,\"appnum\":\"1200026657132\",\"backdtpremium\":0,\"POD\":{\"nextPremDDt\":\"11-Jul-2025\",\"gstrt_rop\":\"18%*forfirstyear\",\"amr\":\"10%\",\"life_opt\":\"LIFE\",\"freq\":\"Monthly\",\"gstrt_nonrop\":\"18%*\",\"pps_life_flg\":\"2\",\"pps_flg\":\"No\",\"gstrt_2yr\":\"18%*secondyearonwards\",\"sumAssured\":\"10000000\",\"rop\":\"No\",\"spouse_name\":\"undefined\",\"paymethod\":\"OnlinePayment\",\"scb\":\"No\",\"wopdis\":\"No\",\"saf\":\"A\",\"term\":\"36Years\",\"adb\":\"No\",\"suboption\":\"\",\"pps_mop_flg\":\"\",\"instapremium\":702,\"pla\":\"7\",\"pps_life_pyrt\":\"\",\"dbi\":\"No\",\"gstrt\":\"180%*forfirstyear\",\"DeathSA\":10000000,\"ppt\":\"36Years\",\"lifeStage\":\"No\",\"agentname\":\"\",\"tobsts\":\"No\",\"tagline\":\"ANonLinked,NonParticipating,Individual,PureRiskPremium/SavingsLifeInsurancePlan\",\"topupOpt\":\"N\",\"option\":\"Life\",\"wopCI\":\"No\"},\"jnk\":\"0\",\"qtDt\":\"11April2025\",\"incpDt\":\"11-Apr-2025\",\"mt_rt\":0,\"appTax\":126,\"EC_RT\":0,\"txBkDt\":0,\"nxtYrTax\":0,\"RD\":{},\"bkdttax_rider\":0,\"premwor\":701.6624999999999,\"isservice\":\"Y\",\"firstriderPremium\":0,\"V10Flag\":true,\"partyid\":\"000000\",\"totAnnPremium\":9936}}}";

            String formatStr = "{\"head\":{\"status\":\"Success\",\"stausCode:\":\"200\",\"statusMsg\":\"OK\",\"userid\":\"pindianbank1\",\"txnid\":\"17c07009-8b06-4fe9-873e-5952067a1650\",\"errordetails\":\"\"},\"body\":{\"quoteid\":\"qco3rm37etykf\",\"appno\":\"1200026657132\",\"uwtype\":\"FULLUW\",\"qniproduct\":\"\",\"instype\":\"INST_INDV\",\"magnumbenefit\":\"Life\",\"minfactor\":\"\",\"mymixid\":\"\",\"outputjsillustration\":{\"nextPremDDt\":\"15-Jul-2025\",\"BD\":[],\"premwodisc\":701.6624999999999,\"KTAX\":0,\"totPremium\":828,\"threeMnthPrem\":2484,\"prodname\":\"HDFCLifeClick2ProtectSuper\",\"new_ill_chngs_eff_dt\":\"20-NOV-2018\",\"annPremium\":8424,\"cumulativeprem\":[],\"quoteid\":\"qco3rm37etykf\",\"backdttax\":0,\"sumAssured\":\"10000000\",\"premium\":702,\"chMap\":\"Direct-Online\",\"GST_RT\":0,\"ST_RT\":126,\"saOnDeath\":10000000,\"uin\":\"101N145V08\",\"id\":\"P1C2PS\",\"ResIndFlg\":true,\"twoMnthPrem\":1656,\"appnum\":\"1200026657132\",\"backdtpremium\":0,\"POD\":{\"nextPremDDt\":\"11-Jul-2025\",\"gstrt_rop\":\"18%*forfirstyear\",\"amr\":\"10%\",\"life_opt\":\"LIFE\",\"freq\":\"Monthly\",\"gstrt_nonrop\":\"18%*\",\"pps_life_flg\":\"2\",\"pps_flg\":\"No\",\"gstrt_2yr\":\"18%*secondyearonwards\",\"sumAssured\":\"10000000\",\"rop\":\"No\",\"spouse_name\":\"undefined\",\"paymethod\":\"OnlinePayment\",\"scb\":\"No\",\"wopdis\":\"No\",\"saf\":\"A\",\"term\":\"36Years\",\"adb\":\"No\",\"suboption\":\"\",\"pps_mop_flg\":\"\",\"instapremium\":702,\"pla\":\"7\",\"pps_life_pyrt\":\"\",\"dbi\":\"No\",\"gstrt\":\"180%*forfirstyear\",\"DeathSA\":10000000,\"ppt\":\"36Years\",\"lifeStage\":\"No\",\"agentname\":\"\",\"tobsts\":\"No\",\"tagline\":\"ANonLinked,NonParticipating,Individual,PureRiskPremium/SavingsLifeInsurancePlan\",\"topupOpt\":\"N\",\"option\":\"Life\",\"wopCI\":\"No\"},\"jnk\":\"0\",\"qtDt\":\"11April2025\",\"incpDt\":\"11-Apr-2025\",\"mt_rt\":0,\"appTax\":126,\"EC_RT\":0,\"txBkDt\":0,\"nxtYrTax\":0,\"RD\":{},\"bkdttax_rider\":0,\"premwor\":701.6624999999999,\"isservice\":\"Y\",\"firstriderPremium\":0,\"V10Flag\":true,\"partyid\":\"000000\",\"totAnnPremium\":9936}}}";
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(formatStr);
            String jsonContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            
            return jsonContent;
            
        } catch (Exception e) {
            log.error("Error reading response from file: {}", e.getMessage(), e);
            return "{}";
        }
    }
} 