package com.hdfclife.core.service;

import com.adobe.forms.common.service.DataXMLOptions;
import com.adobe.forms.common.service.DataXMLProvider;
import com.adobe.forms.common.service.FormsException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.jcr.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Map;

@Component (service = DataXMLProvider.class, immediate = true)
public class PrefillAdaptiveForm implements DataXMLProvider {
  private static final Logger log = LoggerFactory.getLogger(PrefillAdaptiveForm.class);

  @Reference
  private UserDataService userDataService;

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
    InputStream xmlDataStream = null;
    try {

      // Get user data from MySQL database
      Map<String, String> userData = userDataService.getUserData("9876543210");
      log.info("User data: {}", userData);

      // Get all required properties from the database
      // Get all required properties from userData map
      String givenName = userData.get("givenName");
      String email = userData.get("email"); 
      String company = userData.get("company");
      String city = userData.get("city");
      String mobile = userData.get("mobile");
      String iban = userData.get("iban");
      String swiftCode = userData.get("swiftCode");
      log.info("Given name: {}", givenName);

      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();

      // Create the root structure as per schema
      Element afDataElement = doc.createElement("afData");
      doc.appendChild(afDataElement);

      Element afBoundDataElement = doc.createElement("afBoundData");
      afDataElement.appendChild(afBoundDataElement);

      //In this method, you can fetch the data from any source and return the input stream of data document.
      Element customerApplicationElement = doc.createElement("customerApplication");
      afBoundDataElement.appendChild(customerApplicationElement);

      // Create applicant details section
      Element applicantDetailElement = doc.createElement("applicantDetail");
      customerApplicationElement.appendChild(applicantDetailElement);

      // Create and set applicant details elements
      Element contactPersonElement = doc.createElement("contactPerson");
      contactPersonElement.setTextContent(givenName);
      applicantDetailElement.appendChild(contactPersonElement);
      log.debug("Created contactPerson Element");

      Element contactPersonEmailElement = doc.createElement("contactPersonEmail");
      contactPersonEmailElement.setTextContent(email);
      applicantDetailElement.appendChild(contactPersonEmailElement);
      log.debug("Created contactPersonEmail Element");

      Element contactPersonCompanyElement = doc.createElement("contactPersonCompany");
      contactPersonCompanyElement.setTextContent(company);
      applicantDetailElement.appendChild(contactPersonCompanyElement);
      log.debug("Created contactPersonCompany Element");

      Element contactPersonCityElement = doc.createElement("contactPersonCity");
      contactPersonCityElement.setTextContent(city);
      applicantDetailElement.appendChild(contactPersonCityElement);
      log.debug("Created contactPersonCity Element");

      Element contactPersonMobileElement = doc.createElement("contactPersonMobile");
      contactPersonMobileElement.setTextContent(mobile);
      applicantDetailElement.appendChild(contactPersonMobileElement);
      log.debug("Created contactPersonMobile Element");

      // Create bank details section
      Element bankDetailElement = doc.createElement("bankDetail");
      customerApplicationElement.appendChild(bankDetailElement);

      // Create and set bank details elements
      Element ibanElement = doc.createElement("iban");
      ibanElement.setTextContent(iban);
      bankDetailElement.appendChild(ibanElement);
      log.debug("Created IBAN Element");

      Element swiftCodeElement = doc.createElement("swiftCode");
      swiftCodeElement.setTextContent(swiftCode);
      bankDetailElement.appendChild(swiftCodeElement);
      log.debug("Created swiftCode Element");

      // Create signer details section
      Element signerDetailElement = doc.createElement("signerDetail");
      customerApplicationElement.appendChild(signerDetailElement);

      // Create and set signer details
      Element signerNameElement = doc.createElement("signerName");
      signerNameElement.setTextContent(givenName);
      signerDetailElement.appendChild(signerNameElement);
      log.debug("Created signerName Element");

      log.info("Document created successfully");

      // Transform the document to XML
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult outputTarget = new StreamResult(outputStream);
      transformer.transform(source, outputTarget);

      log.info("Document transformed successfully");

      // Debug output if enabled
      if (log.isDebugEnabled()) {
        FileOutputStream output = new FileOutputStream("afdata.xml");
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);
      }
      xmlDataStream = new ByteArrayInputStream(outputStream.toByteArray());
      log.info("XML data stream: {}", xmlDataStream.toString());
      return xmlDataStream;
    } catch (Exception e) {
      log.error("Error in prefill service: ", e);
      throw new FormsException("Failed to generate prefill data", e);
    }
  }
}
