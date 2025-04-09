/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 *  Copyright 2016 Adobe Systems Incorporated
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

package com.hdfclife.core.customhandler;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.adobe.fd.fp.exception.FormsPortalException;
import com.adobe.fd.fp.service.DraftDataService;
import com.adobe.fd.fp.service.PendingSignDataService;
import com.adobe.fd.fp.service.SubmitDataService;
import com.adobe.fd.fp.util.FormsPortalConstants;
import com.adobe.granite.resourceresolverhelper.ResourceResolverHelper;

@ObjectClassDefinition(
    name = "Forms Portal Sample Data service Impl",
    description = "Forms Portal Sample Data service Implementation"
)
@interface Config {
    @AttributeDefinition(
        name = "Name of the table to store data blob",
        description = "Name of the table to store data blob"
    )
    String datatable() default FormsPortalConstants.STR_DEFAULT_DATA_TABLE;

    @AttributeDefinition(
        name = "Name of the configured Data Source",
        description = "Name of the configured Data Source"
    )
    String datasource() default FormsPortalConstants.STR_DEFAULT_DATA_SOURCE_NAME;

    @AttributeDefinition(
        name = "Forms Portal Implementation Property",
        description = "Forms Portal Implementation Property"
    )
    String aem_formsportal_impl_prop() default "formsportal.sampledataservice";
}

@Component(
    service = { SubmitDataService.class, DraftDataService.class, PendingSignDataService.class },
    immediate = true
)
@Designate(ocd = Config.class)
public class FormsPortalSampleDataServiceImpl implements SubmitDataService, DraftDataService, PendingSignDataService {

    private String dataTable = FormsPortalConstants.STR_DEFAULT_DATA_TABLE;
    private String dataSource = FormsPortalConstants.STR_DEFAULT_DATA_SOURCE_NAME;
    
    @Reference
    protected SlingRepository slingRepository;
    
    @Reference
    private ResourceResolverHelper resourceResolverHelper;
    
    private BundleContext bundleContext;
    
    @Activate
    protected void activate(Config config, ComponentContext context) {
        dataSource = config.datasource();
        dataTable = config.datatable();
        bundleContext = context.getBundleContext();
    }
    
    private String getDataSourceName(){
        return dataSource;
    }
    
    private String getDataTableName(){
        return dataTable;
    }
    
    // Returns a connection using the configured DataSourcePool
    private Connection getConnection() throws Exception{
        try {
            String filter = "(&(objectclass=javax.sql.DataSource)(datasource.name=" + getDataSourceName() + "))";
            ServiceReference[] refs = bundleContext.getAllServiceReferences(null, filter);
            if (refs != null && refs.length == 1) {
                DataSource dataSource = (javax.sql.DataSource) bundleContext.getService(refs[0]);
                return dataSource.getConnection();
            }
            throw new FormsPortalException();
        } catch (Exception e) {
            throw new FormsPortalException(e.getMessage(), e);
        }
    }
    
    /**
     * To save user data, this method takes
     * 1. id for the userdata (id will be null if you are creating this draft/submission instance for the first time)
     * 2. formName, form's name
     * 3. formdata, actual data to be stored
     * Here to maintain owner's information, this method is internally getting that information and saving it
     * 
     *  Leveraging (Insert into ..... On Duplicate Key Update ......) query to insert into table if there is no such instance.	 
     *  Returns the data "id"
     */
    public String saveData(String id, String formName, String formdata) throws FormsPortalException {		
        return saveDataInternal(id, formdata.getBytes(), resourceResolverHelper.getResourceResolver().getUserID());
    }
    
    private String saveDataInternal(String id, byte[] formData, String userName) throws FormsPortalException {
        Connection connection = null;
        PreparedStatement prStmt = null;
        try {
            String updateStatement = "INSERT INTO " + getDataTableName() + "(id, data, owner)" + " VALUES((?), (?), (?))" + 
                            "ON DUPLICATE KEY UPDATE " + "data = (?)";
            connection = getConnection();
            // Setting auto commit false here to maintain atomic transactional behavior
            connection.setAutoCommit(false);
            
            prStmt = connection.prepareStatement(updateStatement);
            if (StringUtils.isEmpty(id)) {
                id = getId();
            }
    
            prStmt.setString(1, id);
            prStmt.setBlob(2, new ByteArrayInputStream(formData));
            prStmt.setString(3, userName);
            prStmt.setBlob(4, new ByteArrayInputStream(formData));
            prStmt.execute();
            
            /**
             * Committing after all the operations
             */
            connection.commit();            
            return id;
        } catch (Exception e) {
            try {
                /**
                 *  In case of any error, rollback
                 */
                connection.rollback();
            } catch (SQLException e1) {
                throw new FormsPortalException(e);
            }
            throw new FormsPortalException(e);
        } finally {
            try {
                /**
                 * Close the statement and connection in finally block
                 */
                if(prStmt != null){
                    prStmt.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /**
     * To get data stored against the id
     * This is the same data that we stored in saveData method
     * It takes only "id" as argument
     * Using logged-in user's information to verify if he is the authorized person to view this data
     * Returns byte array of data requested
     */
    public byte[] getData(String id) throws FormsPortalException {
        Connection connection = null;
        PreparedStatement prStmt = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            
            String getDataStmt = "SELECT " + FormsPortalConstants.STR_DATA_COLUMN + " from " + getDataTableName() + " WHERE id = (?)";
            prStmt = connection.prepareStatement(getDataStmt);
            prStmt.setString(1, id);
            
            resultSet = prStmt.executeQuery();			
            resultSet.next();
            byte[] response =  resultSet.getBytes(FormsPortalConstants.STR_DATA_COLUMN);
            
            return response;
        } catch (Exception e) {
            throw new FormsPortalException(e);	
        } finally {
            try {
                /**
                 * Close the resultset, statement and connection in finally block
                 */
                if(resultSet != null){
                    resultSet.close();
                }
                if(prStmt != null){
                    prStmt.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /**
     * To delete the data that we have saved in saveData method
     * This method takes only "id" as argument
     * Again using logged-in user's information to verify if he is the authorized person to delete this data 
     * Returns the status of delete operation
     */
    public boolean deleteData(String id) throws FormsPortalException {
        Connection connection = null;
        PreparedStatement prStmt = null;
        try {
            String userId = resourceResolverHelper.getResourceResolver().getUserID();
            connection = getConnection();
            connection.setAutoCommit(false);
            
            String deleteStmt = "DELETE FROM " + getDataTableName() + " WHERE id = (?) ";
            if (!userId.equals(FormsPortalConstants.STR_SERVICE_USER_ID)) {
                deleteStmt+= "AND owner = (?)";
            }
            prStmt = connection.prepareStatement(deleteStmt);
            prStmt.setString(1, id);
            if (!userId.equals(FormsPortalConstants.STR_SERVICE_USER_ID)) {
                prStmt.setString(2, userId);
            }
            prStmt.execute();
            
            connection.commit();
            return true;
        } catch (Exception e) {
            try{
                /**
                 * Rollback in case of any error
                 */
                connection.rollback();
            } catch(SQLException e2){
                throw new FormsPortalException(e);
            }
            throw new FormsPortalException(e);
        } finally {
            try {
                /**
                 * Close the statement and connection in finally block
                 */
                if(prStmt != null){
                    prStmt.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }
    /**
     * Just like the user data, we need to save attachments uploaded alongwith the form.
     * To do so, we provide 
     * 1. attachmentBytes, attachment data to be stored
     * Returns the attachment "id"
     */
    public String saveAttachment(byte[] attachmentBytes) throws FormsPortalException {
        return saveAttachmentInternal(attachmentBytes, resourceResolverHelper.getResourceResolver().getUserID());
    }
    
    private String saveAttachmentInternal(byte[] attachmentBytes, String owner) throws FormsPortalException {
        Connection connection = null;
        PreparedStatement prStmt = null;
        try {
            String id = getId();
            connection = getConnection();
            connection.setAutoCommit(false);
            
            String saveAttachmentStmt = "INSERT INTO " + getDataTableName() + "(id, data, owner)" + " VALUES((?), (?), (?))";
            prStmt = connection.prepareStatement(saveAttachmentStmt);
            prStmt.setString(1, id);
            prStmt.setBytes(2, attachmentBytes);
            prStmt.setString(3, owner);
            prStmt.execute();
            
            connection.commit();
            return id;
        }catch(Exception e){
            try{
                /**
                 * Rollback in case of any error
                 */
                if (connection != null) {
                    connection.rollback();
                }
                
            } catch(SQLException e2){
                throw new FormsPortalException(e);
            }
            throw new FormsPortalException(e);	
        } finally {
            try {
                /**
                 * Close the statement and connection in finally block
                 */
                if(prStmt != null){
                    prStmt.close();
                }
                if(connection != null){
                    connection.close();	
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }
    /**
     * To get the attachment data 
     * "id" associated with the attachment is passed as argument
     * Returns byte array of attachment data  
     */
    public byte[] getAttachment(String id) throws FormsPortalException {
        return getData(id);
    }
    
    /**
     * To delete the attachment that we have saved in saveAttachment method
     * This method takes only "id" as argument
     * Again using logged-in user's information to verify if he is the authorized person to delete this attachment 
     * Returns the status of delete operation
     */
    public boolean deleteAttachment(String id) throws FormsPortalException {
        return deleteData(id);
    }
    
    private String getId() {
        return String.valueOf(System.nanoTime());
    }

    @Override
    public String saveData(String id, byte[] data) throws FormsPortalException {
        return saveDataInternal(id, data, resourceResolverHelper.getResourceResolver().getUserID());
    }

    /* (non-Javadoc)
     * @see com.adobe.fd.fp.service.SubmitDataService#saveDataAsynchronusly(byte[], java.util.Map)
     */
    @Override
    public String saveDataAsynchronusly(byte[] data, Map<String, Object> options) throws FormsPortalException {
        String dataId = null;
        if (resourceResolverHelper.getResourceResolver().getUserID().equals(FormsPortalConstants.STR_SERVICE_USER_ID)) {
            dataId = saveDataInternal((String)options.get(FormsPortalConstants.STR_ID), data, (String)options.get(FormsPortalConstants.STR_OWNER));
        }
        return dataId;
    }

    /* (non-Javadoc)
     * @see com.adobe.fd.fp.service.SubmitDataService#saveAttachmentAsynchronously(byte[], java.util.Map)
     */
    @Override
    public String saveAttachmentAsynchronously(byte[] attachmentBytes, Map<String, Object> options)
        throws FormsPortalException {
        String attachmentId = null;
        if (resourceResolverHelper.getResourceResolver().getUserID().equals(FormsPortalConstants.STR_SERVICE_USER_ID)) {
            attachmentId = saveAttachmentInternal(attachmentBytes, (String)options.get(FormsPortalConstants.STR_OWNER));
        }
        return attachmentId;
    }

    @Override
    public String saveData(byte[] data) throws FormsPortalException {
        return saveDataInternal(null, data, resourceResolverHelper.getResourceResolver().getUserID());
    }

    @Override
    public String updateData(String userDataID, byte[] data)
        throws FormsPortalException {
        return saveDataInternal(userDataID, data, resourceResolverHelper.getResourceResolver().getUserID());
    }
}
 