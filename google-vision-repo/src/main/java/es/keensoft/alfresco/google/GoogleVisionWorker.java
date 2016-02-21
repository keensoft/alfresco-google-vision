package es.keensoft.alfresco.google;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.TempFileProvider;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

public class GoogleVisionWorker {
	
    private String applicationName;
    private String credentialsJsonPath;
    private Integer maxResults;
    private String translateLanguage;
    private String translateApiKey;
    
	
    public GoogleVisionBean execute(ContentReader reader) throws Exception {
    	
    	if (isImage(reader)) {
    		
	        File sourceFile = TempFileProvider.createTempFile(getClass().getSimpleName(), ".tmp");
	        reader.getContent(sourceFile);
	        
	        GoogleVisionBean gvb = searchData(Files.toByteArray(sourceFile));
	        return translate(gvb);
	        
    	} else {
	        return null;
    	}
	    
    }
    
    private GoogleVisionBean translate(GoogleVisionBean gvb) throws Exception {
    	
    	if (!translateLanguage.equals("")) {
    		
            Translate translate = new Translate(
            		GoogleNetHttpTransport.newTrustedTransport(), 
            		JacksonFactory.getDefaultInstance(), 
                    null);
    		
        	if (gvb.getLabels() != null) {
        		List<String> labels = new ArrayList<String>();
		        for (String label : gvb.getLabels()) {
		            List<String> list = new ArrayList<String>();
		        	list.add(label);
		        	Translate.Translations.List requestTranslate = translate.translations().list(list, "es").setKey(translateApiKey);
		        	String result = requestTranslate.execute().getTranslations().get(0).getTranslatedText();
		        	if (result != null) {
		        		labels.add(result);
		        	}
		    	}
		        gvb.setLabels(labels);
        	}
        	
        	if (gvb.getText() != null) {
        		List<String> text = new ArrayList<String>();
		        for (String textItem : gvb.getText()) {
		            List<String> list = new ArrayList<String>();
		        	list.add(textItem);
		        	Translate.Translations.List requestTranslate = translate.translations().list(list, "es").setKey(translateApiKey);
		        	String result = requestTranslate.execute().getTranslations().get(0).getTranslatedText();
		        	if (result != null) {
		        		text.add(result);
		        	}
		    	}
		        gvb.setText(text);
        	}
        	
        	if (gvb.getLandmark() != null) {
	            List<String> list = new ArrayList<String>();
	        	list.add(gvb.getLandmark());
	        	Translate.Translations.List requestTranslate = translate.translations().list(list, "es").setKey(translateApiKey);
	        	String result = requestTranslate.execute().getTranslations().get(0).getTranslatedText();
	        	if (result != null) {
	        		gvb.setLandmark(result);
	        	}
        	}
        	
    	}
    	
    	return gvb;
    	
    }
    
    public Vision getVisionService() throws Exception {
        GoogleCredential credential =
            GoogleCredential.fromStream(new FileInputStream(new File(credentialsJsonPath))).createScoped(VisionScopes.all());
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                .setApplicationName(applicationName)
                .build();
    }
    
    public GoogleVisionBean searchData(byte[] data) throws Exception {
    	
    	Vision vision = getVisionService();
    	
    	GoogleVisionBean gvb = new GoogleVisionBean();

        AnnotateImageRequest request =
            new AnnotateImageRequest()
                .setImage(new Image().encodeContent(data))
                .setFeatures(ImmutableList.of(
                    new Feature()
                        .setType("LABEL_DETECTION")
                        .setMaxResults(maxResults),
                    new Feature()
                        .setType("LOGO_DETECTION")
                        .setMaxResults(1),
                    new Feature()
                        .setType("TEXT_DETECTION")
                        .setMaxResults(maxResults),
                    new Feature()
                        .setType("LANDMARK_DETECTION")
                        .setMaxResults(1)));
        Vision.Images.Annotate annotate = vision.images().annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
        annotate.setDisableGZipContent(true);

        BatchAnnotateImagesResponse response = annotate.execute();
        
        if (response.getResponses().get(0).getLabelAnnotations() != null) {
        	List<String> labels = new ArrayList<String>();
        	for (EntityAnnotation ea: response.getResponses().get(0).getLabelAnnotations()) {
        		labels.add(ea.getDescription());
        	}
        	gvb.setLabels(labels);
        }
        
        if (response.getResponses().get(0).getLandmarkAnnotations() != null) {
    	    for (EntityAnnotation ea : response.getResponses().get(0).getLandmarkAnnotations()) {
    	    	gvb.setLandmark(ea.getDescription());
    	    }
        }
        
        if (response.getResponses().get(0).getLogoAnnotations() != null) {
    	    for (EntityAnnotation ea : response.getResponses().get(0).getLogoAnnotations()) {
    	    	gvb.setLogo(ea.getDescription());
    	    }
        }
        
        if (response.getResponses().get(0).getTextAnnotations() != null) {
        	List<String> text = new ArrayList<String>();
    	    for (EntityAnnotation ea : response.getResponses().get(0).getTextAnnotations()) {
	    	    text.add(ea.getDescription());
    	    }
    	    gvb.setText(text);
        }
        
        return gvb;

    }
    
    public boolean isImage(ContentReader reader) {
		 return reader.getMimetype().startsWith("image/");
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setCredentialsJsonPath(String credentialsJsonPath) {
		this.credentialsJsonPath = credentialsJsonPath;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	public void setTranslateLanguage(String translateLanguage) {
		this.translateLanguage = translateLanguage;
	}

	public void setTranslateApiKey(String translateApiKey) {
		this.translateApiKey = translateApiKey;
	}

}