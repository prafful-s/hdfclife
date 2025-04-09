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
package com.hdfclife.core.model;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import com.adobe.fd.fp.model.DraftMetadata;
import com.adobe.fd.fp.util.FormsPortalConstants;

/**
 * @author sharoon
 * @date 13-Oct-2016
 * @time 11:21:13 am
 */
public class PendingSignMetadata extends DraftMetadata {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -5047158395830000736L;

	public static final String PENDING_SIGN_ID = "pendingSignID";
	
	private String pendingSignID;
	
	private String agreementId;
	
	private String status;
	
	private String eSignStatus;

	private String[] nextSigners;
	
	/**
	 * Default Constructor
	 */
	public PendingSignMetadata() {
		super();
	}

	public JSONObject getJSONObject() throws JSONException {
		JSONObject pendingSignGuideJson = super.getJSONObject();
		pendingSignGuideJson.put(PENDING_SIGN_ID, getPendingSignID());
		pendingSignGuideJson.put(FormsPortalConstants.STR_AGREEMENT_ID, getAgreementId());
		if (nextSigners != null) {
			JSONArray signerArray = new JSONArray();
			for (String signer: nextSigners) {
				signerArray.put(signer);
			}
			pendingSignGuideJson.put(FormsPortalConstants.STR_NEXT_SIGNERS, signerArray);
		}
		return pendingSignGuideJson;
	}

	public String getPendingSignID() {
		return pendingSignID;
	}

	public void setPendingSignID(String pendingSignID) {
		this.pendingSignID = pendingSignID;
	}

	/**
	 * @return the agreementId
	 */
	public String getAgreementId() {
		return agreementId;
	}

	/**
	 * @param agreementId the agreementId to set
	 */
	public void setAgreementId(String agreementId) {
		this.agreementId = agreementId;
	}
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the nextSigners
	 */
	public String[] getNextSigners() {
		return nextSigners;
	}

	/**
	 * @param nextSigners the nextSigners to set
	 */
	public void setNextSigners(String[] nextSigners) {	//NOSONAR
		this.nextSigners = nextSigners;
	}

	/**
	 * @return the eSignStatus
	 */
	public String geteSignStatus() {
		return eSignStatus;
	}

	/**
	 * @param eSignStatus the eSignStatus to set
	 */
	public void seteSignStatus(String eSignStatus) {
		this.eSignStatus = eSignStatus;
	}
}
