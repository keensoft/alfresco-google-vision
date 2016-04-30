package es.keensoft.alfresco.google.behaviour;

import java.io.File;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentLimitViolationException;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import com.google.common.io.Files;

import es.keensoft.alfresco.google.GoogleVisionWorker;

public class InappropriateContentFirewall  implements ContentServicePolicies.OnContentPropertyUpdatePolicy {
	
    Log logger = LogFactory.getLog(InappropriateContentFirewall.class);
    
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private ContentService contentService;
    private GoogleVisionWorker googleVisionWorker;
	
	public void init() {
		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentPropertyUpdatePolicy.QNAME, 
				ContentModel.TYPE_CONTENT, 
				new JavaBehaviour(this, "onContentPropertyUpdate", NotificationFrequency.EVERY_EVENT));		
    }
	
	@Override
	public void onContentPropertyUpdate(NodeRef nodeRef, QName propertyQName, ContentData beforeValue, ContentData afterValue) {
		
    	if (googleVisionWorker.getSafeSearchConfig().getEnabled()) {
	        Status status = nodeService.getNodeStatus(nodeRef);
	        boolean innapropiateContent = false;
	        if (!status.isDeleted() && nodeService.exists(nodeRef)) {
	        	if (afterValue.getMimetype().startsWith("image/")) {
	        		try {
		    	        File sourceFile = TempFileProvider.createTempFile(getClass().getSimpleName(), ".tmp");
		    	        ContentReader reader = contentService.getRawReader(afterValue.getContentUrl());
		    	        reader.getContent(sourceFile);
		        	    if (googleVisionWorker.isInappropriateContent(Files.toByteArray(sourceFile))) {
		        	    	innapropiateContent = true;
		        	    }
	        		} catch (Exception e) {
	        			logger.warn("Google Vision process exception", e);
	        		}
	        		if (innapropiateContent) {
	        	    	throw new ContentLimitViolationException(I18NUtil.getMessage("inappropriate.content.exception"));
	        		}
	        	}
	        }
    	}

	}
	
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setGoogleVisionWorker(GoogleVisionWorker googleVisionWorker) {
		this.googleVisionWorker = googleVisionWorker;
	}

}
