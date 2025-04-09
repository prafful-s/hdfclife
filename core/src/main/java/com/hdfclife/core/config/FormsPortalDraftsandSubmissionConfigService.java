/*************************************************************************
*
* ADOBE CONFIDENTIAL
* ___________________
*
*  Copyright 2014 Adobe Systems Incorporated
*  All Rights Reserved.
*
* NOTICE:  All information contained herein is, and remains
* the property of Adobe Systems Incorporated and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Adobe Systems Incorporated and its
* suppliers and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Adobe Systems Incorporated.
**************************************************************************/
package com.hdfclife.core.config;

import java.util.Dictionary;

import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Forms Portal Draft and Submission Configuration",
    description = "Forms Portal Draft and Submission Configuration"
)
@interface Config {
    @AttributeDefinition(
        name = "Outboxes",
        description = "Outboxes to be used for Reverse Replication in Forms Portal"
    )
    String[] portal_outboxes() default {"outbox"};

    @AttributeDefinition(
        name = "Forms Portal Draft Data Service",
        description = "Identifier for draft data service"
    )
    String draft_data_service() default "com.adobe.fd.fp.service.impl.DraftDataServiceImpl";

    @AttributeDefinition(
        name = "Forms Portal Draft Metadata Service",
        description = "Identifier for draft metadata service"
    )
    String draft_metadata_service() default "com.adobe.fd.fp.service.impl.DraftMetadataServiceImpl";

    @AttributeDefinition(
        name = "Forms Portal Submit Data Service",
        description = "Identifier for submit data service"
    )
    String submit_data_service() default "com.adobe.fd.fp.service.impl.SubmitDataServiceImpl";

    @AttributeDefinition(
        name = "Forms Portal Submit Metadata Service",
        description = "Identifier for submit metadata service"
    )
    String submit_metadata_service() default "com.adobe.fd.fp.service.impl.SubmitMetadataServiceImpl";

    @AttributeDefinition(
        name = "Forms Portal Pending Sign Data Service",
        description = "Identifier for pending sign data service"
    )
    String pendingSign_data_service() default "com.adobe.fd.fpaddon.service.impl.PendingSignDataServiceImpl";

    @AttributeDefinition(
        name = "Forms Portal Pending Sign Metadata Service",
        description = "Identifier for pending sign metadata service"
    )
    String pendingSign_metadata_service() default "com.adobe.fd.fpaddon.service.impl.PendingSignMetadataServiceImpl";
}

@Component(
    service = FormsPortalDraftsandSubmissionConfigService.class,
    immediate = true
)
@Designate(ocd = Config.class)
public class FormsPortalDraftsandSubmissionConfigService {
    
    private static final String DEFAULT_DRAFT_DATA_SERVICE = "com.adobe.fd.fp.service.impl.DraftDataServiceImpl";
    private static final String DEFAULT_SUBMIT_DATA_SERVICE = "com.adobe.fd.fp.service.impl.SubmitDataServiceImpl";
    private static final String DEFAULT_PENDING_SIGN_DATA_SERVICE = "com.adobe.fd.fpaddon.service.impl.PendingSignDataServiceImpl";
    private static final String DEFAULT_DRAFT_METADATA_SERVICE = "com.adobe.fd.fp.service.impl.DraftMetadataServiceImpl";
    private static final String DEFAULT_SUBMIT_METADATA_SERVICE = "com.adobe.fd.fp.service.impl.SubmitMetadataServiceImpl";
    private static final String DEFAULT_PENDING_SIGN_METADATA_SERVICE = "com.adobe.fd.fpaddon.service.impl.PendingSignMetadataServiceImpl";
    private static final String[] DEFAULT_FP_OUTBOXES = {"outbox"};
    private static final String DEFAULT_FP_ROOT = "/content/forms/fp";    

    private String[] fpOutboxes = DEFAULT_FP_OUTBOXES;
    private String draftDataService = DEFAULT_DRAFT_DATA_SERVICE;
    private String draftMetadataService = DEFAULT_DRAFT_METADATA_SERVICE;
    private String submitDataService = DEFAULT_SUBMIT_DATA_SERVICE;
    private String submitMetadataService = DEFAULT_SUBMIT_METADATA_SERVICE;
    private String pendingSignDataService = DEFAULT_PENDING_SIGN_DATA_SERVICE;
    private String pendingSignMetadataService = DEFAULT_PENDING_SIGN_METADATA_SERVICE;

    @Activate
    protected void activate(Config config) {
        fpOutboxes = config.portal_outboxes();
        draftDataService = config.draft_data_service();
        draftMetadataService = config.draft_metadata_service();
        submitDataService = config.submit_data_service();
        submitMetadataService = config.submit_metadata_service();
        pendingSignDataService = config.pendingSign_data_service();
        pendingSignMetadataService = config.pendingSign_metadata_service();
    }

    public String getDraftDataService() {
        return draftDataService;
    }
    
    public String getSubmitDataService() {
        return submitDataService;
    }
    
    public String getDraftMetadataService() {
        return draftMetadataService;
    }
    
    public String getSubmitMetadataService(){
        return submitMetadataService;
    }
    
    public String getFormsPortalRoot() {
        return DEFAULT_FP_ROOT;
    }
    
    public String[] getFormsPortalOutboxes(){
        return fpOutboxes;
    }

    /**
     * @return the pendingSignDataService
     */
    public String getPendingSignDataService() {
        return pendingSignDataService;
    }

    /**
     * @return the pendingSignMetadataService
     */
    public String getPendingSignMetadataService() {
        return pendingSignMetadataService;
    }

}
