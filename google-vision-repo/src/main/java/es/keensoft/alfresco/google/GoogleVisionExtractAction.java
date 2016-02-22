package es.keensoft.alfresco.google;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.transaction.TransactionListenerAdapter;

import es.keensoft.alfresco.google.model.GoogleVisionModel;

public class GoogleVisionExtractAction extends ActionExecuterAbstractBase {
	
	private NodeService nodeService;
	private ContentService contentService;
	private TaggingService taggingService;
    private TransactionService transactionService;
    private TransactionListener transactionListener; 
    private ThreadPoolExecutor threadPoolExecutor;
	
	private GoogleVisionWorker googleVisionWorker;
	
    private static final String KEY_GOOGLE_VISION_NODE = GoogleVisionExtractAction.class.getName() + ".GoogleVisionNode";
	
	public void init() {
		super.init();
		this.transactionListener = new GoogleVisionTransactionListener();
	}

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		
		if (!nodeService.hasAspect(actionedUponNodeRef, GoogleVisionModel.ASPECT_GOOGLE_VISION)) {
	        AlfrescoTransactionSupport.bindListener(transactionListener);
	        List<NodeRef> nodeRefsToBeOCRd = null;
	        if (AlfrescoTransactionSupport.getResource(KEY_GOOGLE_VISION_NODE) == null) {
	        	nodeRefsToBeOCRd = new ArrayList<NodeRef>();
	        } else {
	        	nodeRefsToBeOCRd = AlfrescoTransactionSupport.getResource(KEY_GOOGLE_VISION_NODE);
	        }
        	nodeRefsToBeOCRd.add(actionedUponNodeRef);
            AlfrescoTransactionSupport.bindResource(KEY_GOOGLE_VISION_NODE, nodeRefsToBeOCRd);
		}
        
	}
	
    private class GoogleVisionTransactionListener extends TransactionListenerAdapter implements TransactionListener {
 
        @Override
        public void afterCommit() {
        	List<NodeRef> nodesToBeGoogleVisioned = AlfrescoTransactionSupport.getResource(KEY_GOOGLE_VISION_NODE);
        	for (NodeRef nodeToBeGoogleVisioned : nodesToBeGoogleVisioned) {
	            Runnable runnable = new GoogleVisionTask(nodeToBeGoogleVisioned);
	            threadPoolExecutor.execute(runnable);
        	}
        }
         
        @Override
        public void flush() {}
         
    }
    
    private class GoogleVisionTask implements Runnable {
         
        private NodeRef nodeToBeGoogleVisioned;
         
        private GoogleVisionTask(NodeRef nodeToBeOCRd) {
            this.nodeToBeGoogleVisioned = nodeToBeOCRd;
        }
        
        @Override
        public void run() {
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
                 
                public Void doWork() throws Exception {
                     
                    RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>() {
                         
                        @Override
                        public Void execute() throws Throwable {
                        	
                    		ContentReader reader = contentService.getReader(nodeToBeGoogleVisioned, ContentModel.PROP_CONTENT);
                    		
                    	    GoogleVisionBean gvBean = googleVisionWorker.execute(reader);
                    	    
                    	    // Labels
                    	    for (String label : gvBean.getLabels()) {
                    	    	taggingService.addTag(nodeToBeGoogleVisioned, label);
                    	    }
                    	    
                    	    // Text
                    	    if (gvBean.getText() != null && !gvBean.getText().isEmpty()) {
                        	    String description = "google vision:";
	                    	    for (String text : gvBean.getText()) {
	                    	    	description = description + " " + text;
	                    	    }
	                    	    Serializable previousDescription = nodeService.getProperty(nodeToBeGoogleVisioned, ContentModel.PROP_DESCRIPTION);
	                    	    if (previousDescription != null) {
	                    	        description = previousDescription + ", " + description;
	                    	    }
	                    	    nodeService.setProperty(nodeToBeGoogleVisioned, ContentModel.PROP_DESCRIPTION, description);
                    	    }
                    	    
                    	    // Landmark and Logo
                    	    Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
                    	    aspectProperties.put(GoogleVisionModel.PROP_LOGO, gvBean.getLogo());
                    	    aspectProperties.put(GoogleVisionModel.PROP_LANDMARK, gvBean.getLandmark());
							nodeService.addAspect(nodeToBeGoogleVisioned, GoogleVisionModel.ASPECT_GOOGLE_VISION, aspectProperties);
                            
                            return null;
                        }
                    };
                     
                    try {
                        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                        txnHelper.doInTransaction(callback, false, true);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                     
                    return null;
                     
                }
            });
        }
    }

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
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

	public TransactionService getTransactionService() {
		return transactionService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		this.threadPoolExecutor = threadPoolExecutor;
	}

	public void setTaggingService(TaggingService taggingService) {
		this.taggingService = taggingService;
	}

}
