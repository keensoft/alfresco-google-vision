package es.keensoft.alfresco.google.model;

import org.alfresco.service.namespace.QName;
	 
public interface GoogleVisionModel {
    static final String URI = "http://www.keensoft.es/model/content/google/1.0";
    static final QName ASPECT_GOOGLE_VISION = QName.createQName(URI, "googleVision");
    static final QName PROP_LANDMARK = QName.createQName(URI, "landmark");
    static final QName PROP_LOGO = QName.createQName(URI, "logo");
}

