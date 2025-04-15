package com.hdfclife.core.common;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.fd.fp.config.FormsPortalDraftsandSubmissionConfigService;
import com.adobe.fd.fp.exception.FormsPortalException;
import com.adobe.fd.fp.util.FormsPortalConstants;
import com.adobe.fd.fp.util.RepositoryUtils;
import com.adobe.granite.resourceresolverhelper.ResourceResolverHelper;
import com.adobe.granite.security.user.UserManagementService;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;

@Component(immediate = true)
public class PortalUtilsComponent {

    private final Logger log = LoggerFactory.getLogger(PortalUtilsComponent.class);

    @Reference
    private FormsPortalDraftsandSubmissionConfigService draftsandSubmissionConfiguration;
    
    @Reference
    private UserManagementService usermgmtService;
	
		@Reference
		ResourceResolverHelper resourceResolverHelper;
	
    @Reference
    private SlingRepository repository = null;

    @Reference
    private QueryBuilder queryBuilder;

	public String getDDSFilter(){
		String draftDataFilter     = "(aem.formsportal.impl.prop=" + draftsandSubmissionConfiguration.getDraftDataService() + ")";
		return draftDataFilter;
	}
	
	public String getDMSFilter(){
		String draftMetadataFilter = "(aem.formsportal.impl.prop=" + draftsandSubmissionConfiguration.getDraftMetadataService() + ")";
		return draftMetadataFilter;
	}

	public String getSDSFilter(){
		String submitDataFilter    = "(aem.formsportal.impl.prop=" + draftsandSubmissionConfiguration.getSubmitDataService() + ")";
		return submitDataFilter;
	}
	
	public String getSMSFilter(){
		String submitMetadataFilter= "(aem.formsportal.impl.prop=" + draftsandSubmissionConfiguration.getSubmitMetadataService() + ")";
		return submitMetadataFilter;
	}
	
	public String getPDSFilter(){
		String pendingSignDataFilter= "(aem.formsportal.impl.prop=" + draftsandSubmissionConfiguration.getPendingSignDataService() + ")";
		return pendingSignDataFilter;
	}
	
	public String getPMSFilter(){
		String pendingSignMetadataFilter= "(aem.formsportal.impl.prop=" + draftsandSubmissionConfiguration.getPendingSignMetadataService() + ")";
		return pendingSignMetadataFilter;
	}
	
	public boolean isLoginAnonymous(){
		String userName    = resourceResolverHelper.getResourceResolver().getUserID();
		String anonymousId = usermgmtService.getAnonymousId();
		if(anonymousId != null && anonymousId.equals(userName))
		    return true;
		else 
			return false;
	}


    public boolean isReviewer(Session currentSession, String formPath) throws Exception{
	try{
		if(currentSession != null && formPath != null && !formPath.trim().isEmpty()){
			Node formNode = currentSession.getNode(formPath);
				RepositoryUtils repUtils = RepositoryUtils.getInstance(draftsandSubmissionConfiguration);
			Node metadataNode = repUtils.getMetadataNode(formNode, false);
			if(metadataNode.hasProperty(FormsPortalConstants.STR_FORM_SUBMISSION_REVIEWER_GROUP) && metadataNode.getProperty(FormsPortalConstants.STR_FORM_SUBMISSION_REVIEWER_GROUP) != null){
				String allowedGroupId = metadataNode.getProperty(FormsPortalConstants.STR_FORM_SUBMISSION_REVIEWER_GROUP).getString();
		            UserManager userManager = AccessControlUtil.getUserManager(currentSession);
		            Authorizable authorizable = userManager.getAuthorizable(currentSession.getUserID());
			    Iterator<Group> grpItr = authorizable.declaredMemberOf();
			    while (grpItr.hasNext()) {
			        Group group = grpItr.next();
			        if (group.getID().equals(allowedGroupId)) {
			            return true;
			        }
			    }
			}
		}
	} catch(Exception e){
		log.error("Error occured while verifying if user is authorized or not", e);
	}
	return false;
    }

	public boolean isOwner(Session currentSession, String submitID) throws FormsPortalException {
	    if(currentSession != null && submitID != null) {
	        return getMetadataNodeFromSubmitID(submitID, currentSession) != null;
	    }
	    return false;
	}

	public Node getMetadataNodeFromSubmitID(String submitID, Session session) throws FormsPortalException{
		try{
            Map<String, String> queryMap = new HashMap<String, String>();
            queryMap.put(FormsPortalConstants.STR_PATH, FormsPortalConstants.STR_CONTENT_FORMS_FP);
            queryMap.put(FormsPortalConstants.STR_TYPE, FormsPortalConstants.STR_NT_UNSTRUCTURED);
            queryMap.put("0_property", FormsPortalConstants.STR_SUBMIT_ID);
            queryMap.put("0_property.value", submitID);
            queryMap.put("1_property", FormsPortalConstants.STR_NODE_TYPE);
            queryMap.put("1_property.value", FormsPortalConstants.STR_FP_SUBMITTED_FORM);

            //Creating predicate group from Query Map
            PredicateGroup predicates = PredicateGroup.create(queryMap);
            //Creating query from predicate group
            Query query               = queryBuilder.createQuery(predicates, session);
            //Get result after executing the query
            SearchResult result       = query.getResult ();
            Node submitMetadataNode   = null;
            if(result.getTotalMatches() == 1){
                Iterator<Node> it = result.getNodes();
                it.hasNext();
                submitMetadataNode = it.next();
            } else {
                RepositoryUtils repUtils = RepositoryUtils.getInstance(draftsandSubmissionConfiguration);
                String userName          = resourceResolverHelper.getResourceResolver().getUserID();
                Node userNode            = repUtils.getUserNode(userName, false, session);
                String submitNodeRelativePath = FormsPortalConstants.STR_SUBMIT + "/" + FormsPortalConstants.STR_METADATA + "/" + submitID;
                if(userNode != null && userNode.hasNode(submitNodeRelativePath)) {
                    submitMetadataNode   = userNode.getNode(submitNodeRelativePath);
                }
            }
            return submitMetadataNode;
        } catch(Exception e){
            throw new FormsPortalException(e);
        }
	}
    /**
     * removes the target element from the Array and returns the new array
     * @param objArray
     * @param key
     * @return generic array
     */
    public static <T> T[] removeFromArray(T[] objArray, T key){
        List<T> objList = new ArrayList<T>(Arrays.asList(objArray));
        objList.remove(key);
        T[] resultArray = (T[]) Array.newInstance(key.getClass(), objList.size());
        objList.toArray(resultArray);
        return resultArray;
    }

    public boolean isLoginAnonymous(String userName) {
        String anonymousId = usermgmtService.getAnonymousId();
        if (anonymousId != null && anonymousId.equals(userName)) {
            return true;
        } else {
            return false;
        }
    }
}
