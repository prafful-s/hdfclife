/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2013 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
package com.hdfclife.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.fd.fp.config.FormsPortalDraftsandSubmissionConfigService;
import com.adobe.fd.fp.exception.FormsPortalRepositoryException;


/**
 * @author sharoon
 *
 */
public class RepositoryUtils {
	/** Default log. */
	private static final Logger log = LoggerFactory.getLogger(RepositoryUtils.class);

	private static final String STR_SLING_ORDERED_FOLDER = "sling:OrderedFolder";
	
	private static final String STR_GUIDE_NAME = "guideName";
	
	private static final String STR_SUBMIT = "submit";
	
	private static final String STR_TEMP = "temp";
	
	private static final String STR_DRAFT = "drafts";
	
	public static final String METADATA_NODE_NAME = "metadata";
	
	public static final String JCR_CONTENT_NODE_NAME = "jcr:content";
	
	public static final String RENDITIONS_NODE_NAME = "renditions";
	
	public static final String ORIGINAL_RENDITION_NODE_NAME = "original";
	
	public static final String GUIDE_CONTAINER_NODE_NAME = "guideContainer";
	
	private static RepositoryUtils repositoryUtils = null;

    private FormsPortalDraftsandSubmissionConfigService draftsAndSubmissionConfiguration;
    private RepositoryUtils(FormsPortalDraftsandSubmissionConfigService draftsAndSubmissionConfigObject) {
        this.draftsAndSubmissionConfiguration = draftsAndSubmissionConfigObject;
    }

	public static RepositoryUtils getInstance(FormsPortalDraftsandSubmissionConfigService draftsAndSubmissionConfigObject) {
		if (repositoryUtils == null) {
			synchronized (RepositoryUtils.class) {
				if (repositoryUtils == null) {
					repositoryUtils = new RepositoryUtils(draftsAndSubmissionConfigObject);
				}
			}
		} else if (repositoryUtils.draftsAndSubmissionConfiguration != draftsAndSubmissionConfigObject) {
			synchronized (RepositoryUtils.class) {
				if (repositoryUtils.draftsAndSubmissionConfiguration != draftsAndSubmissionConfigObject) {
					repositoryUtils = new RepositoryUtils(draftsAndSubmissionConfigObject);
				}
			}
		}
		return repositoryUtils;
	}
	
	/** Returns the Forms Portal root Node. Currently is /content/forms/fp
	 * Creates it if does not exists
	 * @param session
	 * @return
	 * @throws FormsPortalRepositoryException
	 * @throws ItemExistsException
	 * @throws PathNotFoundException
	 * @throws NoSuchNodeTypeException
	 * @throws LockException
	 * @throws VersionException
	 * @throws ConstraintViolationException
	 * @throws RepositoryException
	 */
	public Node getRootNode(Session session) throws ItemExistsException, PathNotFoundException, 
		NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException 
	 {
		Node portalRootNode = null;
			if (!session.nodeExists(draftsAndSubmissionConfiguration.getFormsPortalRoot())) {
	        	portalRootNode = session.getRootNode().addNode(draftsAndSubmissionConfiguration.getFormsPortalRoot().substring(1), STR_SLING_ORDERED_FOLDER);
	        } else {
	        	portalRootNode = session.getNode(draftsAndSubmissionConfiguration.getFormsPortalRoot());
	        }
		return portalRootNode;
	}
	
	/** Returns node corresponding userName passed. If create is true, it creates the node.
	 * @param fpRootNode
	 * @param userName
	 * @param create
	 * @param Session
	 * @return
	 * 
	 * @throws FormsPortalRepositoryException
	 * @throws ItemExistsException
	 * @throws PathNotFoundException
	 * @throws NoSuchNodeTypeException
	 * @throws LockException
	 * @throws VersionException
	 * @throws ConstraintViolationException
	 * @throws RepositoryException
	 * @throws UnsupportedEncodingException 
	 */
	public Node getUserNode(String userName, boolean create, Session session) throws ItemExistsException, 
               PathNotFoundException, NoSuchNodeTypeException, LockException,
               VersionException, ConstraintViolationException, RepositoryException, UnsupportedEncodingException {
		Node userNode = null;
		// Users have jcr:all permissions on their submission folder, however they can not read portal root node,
		// so directly reading the user submission folder
		
		//LC-8669 Encoding all usernames, if a usernode is already being created without encoding, keep using it otherwise create encoded username folder
		String encodedUserName = URLEncoder.encode(userName, "UTF-8");
		String defaultUserNode = draftsAndSubmissionConfiguration.getFormsPortalRoot() + "/" + userName;		
		String encodedUserNode = draftsAndSubmissionConfiguration.getFormsPortalRoot() + "/" + encodedUserName;
        try{
            if (session.nodeExists(defaultUserNode)) {
                userNode = session.getNode(defaultUserNode);
            } else if(session.nodeExists(encodedUserNode)){
                userNode = session.getNode(encodedUserNode);
            }else {
            	//Create only by fpadmin context
                // Following code would be executed only when this method is invoked using fpadmin context, do not increase fprootnode's scope 	            
                if (create) {
	            	Node fpRootNode = getRootNode(session);
                    userNode = fpRootNode.addNode(encodedUserName, STR_SLING_ORDERED_FOLDER);
	            }
            }
		} catch(Exception e){
			log.trace("Error while retrieving node path " + defaultUserNode , e);
			if(session.nodeExists(encodedUserNode)){
				userNode = session.getNode(encodedUserNode);
			}else {
			    // Following code would be executed only when this method is invoked using fpadmin context
            if (create) {
					Node fpRootNode = getRootNode(session);
                    userNode = fpRootNode.addNode(encodedUserName, STR_SLING_ORDERED_FOLDER);
				}
			}
		}
		return userNode;
	}
	
	public Node getUserGuideNode(Node node, String guideName, boolean create) throws ItemExistsException, 
		PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
		Node userGuideNode = null;
		if (!node.hasNode(guideName)) {
			if (create) {
				userGuideNode = node.addNode(guideName);
				userGuideNode.setProperty(STR_GUIDE_NAME, guideName);
			}
		} else {
			userGuideNode = node.getNode(guideName);
		}
		return userGuideNode;
	}
	
	public Node getChildNode(Node node, String nodeName, String nodeType, boolean create) throws ItemExistsException, 
	PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException{
		Node childNode = null;
		if(node != null){
			if(node.hasNode(nodeName)){
				childNode = node.getNode(nodeName);
			} else if(create){
				childNode = node.addNode(nodeName, nodeType);
			}  
		}
		return childNode;
		
	}
	
	public Node getSubmitRootNode(Node userNode, boolean create) throws ItemExistsException, 
	PathNotFoundException, NoSuchNodeTypeException, LockException, 
	VersionException, ConstraintViolationException, RepositoryException {
		Node submitRootNode = null;
		if(userNode != null ) {
			if(!userNode.hasNode(STR_SUBMIT)) {
				if(create) {
					submitRootNode = userNode.addNode(STR_SUBMIT, STR_SLING_ORDERED_FOLDER);
				}
			} else {
				submitRootNode = userNode.getNode(STR_SUBMIT);
			}
		}
		return submitRootNode;
	}
	
	public Node getTempRootNode(Node userNode, boolean create) throws ItemExistsException, 
	PathNotFoundException, NoSuchNodeTypeException, LockException, 
	VersionException, ConstraintViolationException, RepositoryException {
		Node submitRootNode = null;
		if(userNode != null ) {
			if(!userNode.hasNode(STR_TEMP)) {
				if(create) {
					submitRootNode = userNode.addNode(STR_TEMP, STR_SLING_ORDERED_FOLDER);
				}
			} else {
				submitRootNode = userNode.getNode(STR_TEMP);
			}
		}
		return submitRootNode;
	}
	
	public Node getDraftsRootNode(Node userNode, boolean create) throws ItemExistsException, 
	PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
		Node draftsRootNode = null;
		if(userNode != null ) {
			if(!userNode.hasNode(STR_DRAFT)) {
				if(create) {
					draftsRootNode = userNode.addNode(STR_DRAFT, STR_SLING_ORDERED_FOLDER);
				}
			} else {
				draftsRootNode = userNode.getNode(STR_DRAFT);
			}
		}
		return draftsRootNode;
	}
	
	public Node getMetadataNode(Node formNode, boolean create) throws RepositoryException,
	ItemExistsException, PathNotFoundException,
	NoSuchNodeTypeException, LockException, VersionException,
	ConstraintViolationException {
		Node metadataNode = null;
		Node contentNode = getContentNode(formNode, create);
		if (contentNode != null) {
			if (!contentNode.hasNode(METADATA_NODE_NAME)) {
				if(create) {
					metadataNode = contentNode.addNode(METADATA_NODE_NAME,
							NodeType.NT_UNSTRUCTURED);
				}
			} else {
				metadataNode = contentNode.getNode(METADATA_NODE_NAME);
			}
		}
		return metadataNode;
	}

	public Node getContentNode(Node formNode, boolean create) throws RepositoryException,
		ItemExistsException, PathNotFoundException,
		NoSuchNodeTypeException, LockException, VersionException,
		ConstraintViolationException {
		Node contentNode = null;
		if (!formNode.hasNode(JCR_CONTENT_NODE_NAME)) {
			if(create) {
				contentNode = formNode.addNode(JCR_CONTENT_NODE_NAME,
						NodeType.NT_UNSTRUCTURED);
			}
		} else {
			contentNode = formNode.getNode(JCR_CONTENT_NODE_NAME);
		}
		return contentNode;
	}
	
	public Node getRenditionNode(Node formNode, boolean create) throws ItemExistsException,
	PathNotFoundException, NoSuchNodeTypeException, LockException,
	VersionException, ConstraintViolationException, RepositoryException {
		Node renditionNode = null;
		Node contentNode = getContentNode(formNode, create);
		if (contentNode != null) {
			if (!contentNode.hasNode(RENDITIONS_NODE_NAME)) {
				if(create) {
					renditionNode = contentNode.addNode(RENDITIONS_NODE_NAME,
							NodeType.NT_FOLDER);
				}
			} else {
				renditionNode = contentNode.getNode(RENDITIONS_NODE_NAME);
			}
		}
		return renditionNode;
	}
	
	public Node getOriginalRenditionNode(Node formNode, boolean create)
			throws ItemExistsException, PathNotFoundException,
			NoSuchNodeTypeException, LockException, VersionException,
			ConstraintViolationException, RepositoryException {
		Node origRenditionNode = null;
		Node renditionNode = getRenditionNode(formNode, create);
		if (renditionNode != null) {
			if (!renditionNode.hasNode(ORIGINAL_RENDITION_NODE_NAME)) {
				if(create) {
					origRenditionNode = renditionNode.addNode(
							ORIGINAL_RENDITION_NODE_NAME, NodeType.NT_FILE);
				}
			} else {
				origRenditionNode = renditionNode
						.getNode(ORIGINAL_RENDITION_NODE_NAME);
			}
		}
		return origRenditionNode;
	}
	
	

	public Node getOriginalRenditionContentNode(Node formNode, boolean create)
			throws ItemExistsException, PathNotFoundException,
			NoSuchNodeTypeException, LockException, VersionException,
			ConstraintViolationException, RepositoryException {
		Node origRenditionContentNode = null;
		Node origRenditionNode = getOriginalRenditionNode(formNode, create);
		if (origRenditionNode != null) {
			if (!origRenditionNode.hasNode(JCR_CONTENT_NODE_NAME)) {
				if(create) {
					origRenditionContentNode = origRenditionNode.addNode(
							JCR_CONTENT_NODE_NAME, NodeType.NT_RESOURCE);
				}
			} else {
				origRenditionContentNode = origRenditionNode
						.getNode(JCR_CONTENT_NODE_NAME);
			}
		}
		return origRenditionContentNode;
	}
	
	public Node getGuideContainerNode (Node guideNode) throws ItemExistsException,
	PathNotFoundException, NoSuchNodeTypeException, LockException,
	VersionException, ConstraintViolationException, RepositoryException {
		Node guideContainerNode = null;
		Node originalRenditionContentNode = getOriginalRenditionContentNode(guideNode, false);
		if (originalRenditionContentNode != null) {
			NodeIterator nItr = originalRenditionContentNode.getNodes(GUIDE_CONTAINER_NODE_NAME);
			if (nItr.hasNext()) {	//Return the first match.
				guideContainerNode = nItr.nextNode();
			}
		}
		return guideContainerNode;
	}
	
	public String findParentCQPage (String currentNodePath, Session session) throws RepositoryException{
		String parentCQPage = null;
		if(session.nodeExists(currentNodePath)){
			Node currentNode = session.getNode(currentNodePath);
			while(!currentNode.isNodeType(FormsPortalConstants.STR_REP_ROOT)){
				if(currentNode.isNodeType(FormsPortalConstants.STR_CQ_PAGE)){
					parentCQPage = currentNode.getPath();
					break;
				}
				currentNode = currentNode.getParent();
			}

		}
		return parentCQPage;
	}

	
	
}


