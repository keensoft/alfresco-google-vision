Alfresco Google Vision
======================

This addon provides an action to extract information from images in Alfresco by using [Google Vision API](https://cloud.google.com/vision/). It includes following data on every image:
* Labels categorized by Google
* Logo text (if detected)
* Landmark text (if detected)
* Text (by using OCR)

**License**
The plugin is licensed under the [LGPL v3.0](http://www.gnu.org/licenses/lgpl-3.0.html). 

**State**
Current addon release 1.0.0 is ***BETA***

**Compatibility**
The current version has been developed using Alfresco 5.1 and Alfresco SDK 2.1, although it should run in Alfresco 5.0.d and Alfresco 5.0.c
Browser compatibility: 100% supported

**Languages**
Currently only provided in English but it supports (at least) Spanish.

***No original Alfresco resources have been overwritten***

Downloading the ready-to-deploy-plugin
--------------------------------------
The binary distribution is made of one amp file to be deployed in Share:

* [repo AMP](https://github.com/angelborroy/alfresco-google-vision/releases/download/1.0.0/google-vision-repo.amp)

You can install them by using standard [Alfresco deployment tools](http://docs.alfresco.com/community/tasks/dev-extensions-tutorials-simple-module-install-amp.html)

Building the artifacts
----------------------
If you are new to Alfresco and the Alfresco Maven SDK, you should start by reading [Jeff Potts' tutorial on the subject](http://ecmarchitect.com/alfresco-developer-series-tutorials/maven-sdk/tutorial/tutorial.html).

You can build the artifacts from source code using maven
```$ mvn clean package```

Configuration
----------------------
After installation, following properties must be included in **alfresco-global.properties**

```
# Google vision
google.vision.application.name=keensoft-Alfresco/1.0
# Credentials path
google.vision.credentials.json=/tmp/key.json
google.vision.max.results=3
# Google translate
google.translate.language=es
google.translate.api.key=XXXXXXXXXXXXXXXXX

# Background process
google.pool.core.size=1
google.pool.maximum.size=1
google.pool.thread.priority=5

```
Usage
----------------------
* Including a rule on a folder by selecting **google-vision-action**
* Every dropped image on this folder will be sent to Google Vision in order to get labels as Alfresco Tags, landmark and logo as Alfresco Custom Aspect and OCR text as Alfresco Description. 
* If a language is selected for Google Translate in **alfresco-global.properties**, labels, landmark and text is translated before including values in Alfresco. 
* This operation is performed asynchronously, so results are not available just after uploading the image.
