package cn.elead.tool.wc;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.fc.IdentityHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.pds.StatementSpec;
import wt.query.ConstantExpression;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.team.TeamReference;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;

import com.google.gwt.rpc.client.impl.RemoteException;

public class DocUtil implements RemoteAccess {
    private final static Logger LOGGER = LogR.getLogger(DocUtil.class.getName());
    
    /**
     * ÅÐ¶ÏÊÇ·ñÊÇ¿Õ×Ö·û´® nullºÍ"" ¶¼·µ»Ø true
     * 
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
    	
       /* if (s != null && !s.equals("") && !s.equals("null")) {
            return false;
        }
        return true;*/
       
    	if (s != null&& s.length() !=0){
    		return false;
    	}
    	return true;
    
    }
	  /**
     * Get Document by nummber and state
     * 
     * @param number
     * @param state
     * @return
     * @throws WTException
     * 
     */
    public static WTDocument getDocument(String number, String name,
            String state) throws WTException {
        WTDocument doc = null;
        
        QuerySpec querySpec = new QuerySpec(WTDocument.class);

        int conditionCount = 0;
        if (!DocUtil.isEmpty(number)) {
            SearchCondition searchCondi = new SearchCondition(WTDocument.class,
                    WTDocument.NUMBER, SearchCondition.EQUAL, number);
            querySpec.appendWhere(searchCondi, new int[] { 0 });
            conditionCount++;
        }

        if (!DocUtil.isEmpty(name)) {
            if (conditionCount > 0) {
                querySpec.appendAnd();
            }
            SearchCondition searchCondi = new SearchCondition(WTDocument.class,
                    WTDocument.NAME, SearchCondition.EQUAL, name);
            querySpec.appendWhere(searchCondi, new int[] { 0 });
            conditionCount++;
        }

        if (!DocUtil.isEmpty(state)) {
            if (conditionCount > 0)
            {
                querySpec.appendAnd();
            }
            SearchCondition searchCondi = new SearchCondition(WTDocument.class,
                    WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state);
            querySpec.appendWhere(searchCondi, new int[] { 0 });
        }
        QueryResult qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
        qr = new LatestConfigSpec().process(qr);
        if (qr.hasMoreElements()) {
            doc = (WTDocument) qr.nextElement();
        }
        return doc;
    }
    
    /**
     * getLatestDocumentByNumber
     * 
     * @param number
     * @return
     * @throws WTException
     * 
     */
    public static WTDocument getDocumentByNumber(String number)
            throws WTException {
    	if (!RemoteMethodServer.ServerFlag) {
            try {
                return (WTDocument) RemoteMethodServer.getDefault().invoke(
                        "getDocumentByNumber", DocUtil.class.getName(),
                        null, new Class[] { String.class },
                        new Object[] {});
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new WTException(e);
           }
        }else {
        if (DocUtil.isEmpty(number)) {
            LOGGER.error(" number is null getDocumentByNumber");
            return null;
        }
        return getDocument(number, null, null);
       }
   }
    /**
     * Get Document by nummber and state
     * 
     * @param number
     * @param state
     * @return
     * @throws WTException
     */
    public static WTDocument getDocumentByNumberAndState(String number,
            String state) throws WTException {
    	if (!RemoteMethodServer.ServerFlag) {
            try {
                return (WTDocument) RemoteMethodServer.getDefault().invoke(
                        "getDocumentByNumberAndState", DocUtil.class.getName(),
                        null, new Class[] { String.class },
                        new Object[] {});
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new WTException(e);
        }
        }else {
        if (DocUtil.isEmpty(number)) {
            LOGGER.error(" number is null getDocumentByNumberAndState");
            return null;
        }
        return getDocument(number, null, state);
        }
    } 
    
    /**
     * get latest WTDocument by name
     * 
     * @param name
     *            document name
     * @return WTDocument
     * @throws WTException
     */
    public static WTDocument getDocumentByName(String name) throws WTException {

    	if (!RemoteMethodServer.ServerFlag) {
            try {
                return (WTDocument) RemoteMethodServer.getDefault().invoke(
                        "getDocumentByName", DocUtil.class.getName(),
                        null, new Class[] { String.class },
                        new Object[] {});
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new WTException(e);
           }
        }else {
        if (DocUtil.isEmpty(name)) {
            LOGGER.error(" name is null  getDocumentByName");
            return null;
        }
        LOGGER.debug("param doc name is " + name);
        return getDocument(null, name, null);
        }
    }
  
    /**
     * @param WTDocument
     * @return ApplicationData
     * @throws Exception
     * @throws WTException
     * @description get ApplicationData by WTDocument
     */
    public static ApplicationData getPrimaryContent(WTDocument document)
            throws WTException {
        ContentHolder contentHolder = null;
        ApplicationData applicationdata = null;
        if (!RemoteMethodServer.ServerFlag) {
            try {
                return (ApplicationData) RemoteMethodServer.getDefault().invoke(
                        "getPrimaryContent", DocUtil.class.getName(),
                        null, new Class[] { String.class },
                        new Object[] {});
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new WTException(e);
           }
        }else {
        try {
            LOGGER.debug("get content of : " + document.getNumber());
            contentHolder = ContentHelper.service
                    .getContents((ContentHolder) document);
            LOGGER.debug("contentHolder : " + contentHolder);
            ContentItem contentitem = ContentHelper
                    .getPrimary((FormatContentHolder) contentHolder);
            LOGGER.debug("primary : " + contentitem);
            applicationdata = (ApplicationData) contentitem;
        } catch (WTException e) {
            LOGGER.error(DocUtil.class + ".getPrimaryContent():", e);
            throw new WTException(e, e.getLocalizedMessage());
        } catch (Exception e) {
            LOGGER.error(DocUtil.class + ".getPrimaryContent():", e);
            throw new WTException(e, e.getLocalizedMessage());
        }
        return applicationdata;
        
        }
    }
    
    /**
     * @param newDocName
     *            String
     * @param doc
     *            WTDocument
     * @return boolean
     * @description
     */
    public static boolean documentRename(String newDocName, WTDocument doc)
            throws WTException {
        LOGGER.info("newDocName = " + newDocName);
        boolean result = false;
        if (!RemoteMethodServer.ServerFlag) {
            try {
                return (boolean) RemoteMethodServer.getDefault().invoke(
                        "WTDocument", DocUtil.class.getName(),
                        null, new Class[] { String.class },
                        new Object[] {});
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new WTException(e);
           }
        }else {
        try {
            // if the same as original then return true
            if (newDocName.equals(doc.getName())) {
                result = true;
            }
            else {
                WTDocumentMaster docmaster = (WTDocumentMaster) doc.getMaster();
                WTDocumentMasterIdentity docmasteridentity = (WTDocumentMasterIdentity) docmaster
                        .getIdentificationObject();
                docmasteridentity.setName(newDocName);
                docmaster = (WTDocumentMaster) IdentityHelper.service
                        .changeIdentity(docmaster, docmasteridentity);
                String newName = docmaster.getName();
                if (newName.equals(newDocName)) {
                    result = true;
                }
            }
        } catch (WTPropertyVetoException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new WTException(e, e.getLocalizedMessage());
        }
        return result;
        }
    }
    
    /**
     * Add the contentHolder attenchMent
     * 
     * @param holder
     * @param filePath
     * @param fileName
     * @param contentType
     * @throws WTException
     */
    private static void addContent(ContentHolder holder, String filePath,
            String fileName, ContentRoleType contentType) throws WTException {
        ApplicationData applicationData = ApplicationData
                .newApplicationData(holder);
        try {
            applicationData.setRole(contentType);
            applicationData = (ApplicationData) PersistenceHelper.manager
                    .save(applicationData);
            applicationData = ContentServerHelper.service.updateContent(holder,
                    applicationData, filePath);
            applicationData.setFileName(fileName);
            applicationData = (ApplicationData) PersistenceHelper.manager
                    .modify(applicationData);
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new WTException(e);
        } catch (PropertyVetoException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new WTException(e);
        }
    }


	/**
     * addAttachments
     * 
     * @param doc
     *            wt document instance
     * @param filePath
     *            file absolute server path
     * @param fileName
     *            file name
     * @throws WTException
     *             Windchill exception
     */
    public static void addAttachments(ContentHolder holder, String filePath,
            String fileName) throws WTException {
        try {
            QueryResult qr = ContentHelper.service.getContentsByRole(holder,
                    ContentRoleType.SECONDARY);
            while (qr.hasMoreElements()) {
                ApplicationData oAppData = (ApplicationData) qr.nextElement();
                String strFileName = oAppData.getFileName();
                if (strFileName != null
                        && strFileName.trim().equalsIgnoreCase(fileName)) {
                    ContentServerHelper.service.deleteContent(holder, oAppData);
                }
            }
            addContent(holder, filePath, fileName, ContentRoleType.SECONDARY);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new WTException(e);
        }
    }
   
    /**
     * Add primary content for a document.
     * 
     * @param doc
     *            document instance
     * @param filePath
     *            file absolute path
     * @param fileName
     *            file name
     * @throws WTException
     *             Windchill exception
     */
    public static void addPrimaryContent(WTDocument doc, String filePath,
            String fileName) throws WTException {
        try {
            ContentHolder contentHolder = ContentHelper.service
                    .getContents(doc);
            ApplicationData applicationData = (ApplicationData) ContentHelper
                    .getPrimary((FormatContentHolder) contentHolder);
            if (applicationData != null) {
                try {
                    ContentServerHelper.service.deleteContent(doc,
                            applicationData);
                } catch (WTPropertyVetoException e) {
                    LOGGER.error(e.getLocalizedMessage());
                    throw new WTException(e);
                }
            }
        } catch (PropertyVetoException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new WTException(e);
        }
        addContent(doc, filePath, fileName, ContentRoleType.PRIMARY);
    }
    
     /**
      * deleteContentByFileName
      * 
      * @param ContentHolder
      * @param String
      * @return
      * @throws WTException
      */
     public static ContentHolder deleteContentByFileName(ContentHolder holder1,
             String fileName) throws WTException {
         ContentHolder holder = holder1;
         if (fileName == null) {
             return holder;
         }
         try {
             holder = ContentHelper.service.getContents(holder);
             List<?> vContentList = ContentHelper.getContentList(holder);
             int iSize = vContentList.size();
             for (int j = 0; j < iSize; j++) {
                 ContentItem contentitem = (ContentItem) vContentList.get(j);
                 String strFileName = ((ApplicationData) contentitem)
                         .getFileName();
                 if (fileName.equals(strFileName)) {
                     ContentServerHelper.service.deleteContent(holder,
                             contentitem);
                 }
             }
             holder = (FormatContentHolder) PersistenceHelper.manager
                     .refresh(holder);
         } catch (PropertyVetoException e) {
             LOGGER.error(e.getLocalizedMessage());
             throw new WTException(e);
         }
         return holder;
     }
     
     /**
      * deleteContents
      * 
      * @param WTDocument
      * @return WTDocument
      * @throws WTException
      */
     public static WTDocument deleteContents(WTDocument doc) throws WTException {
         FormatContentHolder holder = null;
         if (!RemoteMethodServer.ServerFlag) {
             try {
                 return (WTDocument) RemoteMethodServer.getDefault().invoke(
                         "deleteContents", DocUtil.class.getName(),
                         null, new Class[] { String.class },
                         new Object[] {});
             } catch (Exception e) {
                 LOGGER.error(e.getMessage(), e);
                 throw new WTException(e);
            }
         }else {
         try {
             holder = (FormatContentHolder) ContentHelper.service
                     .getContents(doc);
             List<?> items = ContentHelper.getContentListAll(holder);
             for (int i = 0; i < items.size(); i++) {
                 ContentItem item = (ContentItem) items.get(i);
                 ContentServerHelper.service.deleteContent(holder, item);
             }
             holder = (FormatContentHolder) PersistenceHelper.manager
                     .refresh(holder);
         } catch (PropertyVetoException e) {
             LOGGER.error(e.getLocalizedMessage());
             throw new WTException(e);
         }

         return (WTDocument) holder;
         }
     }


     /**
      * 
      * @param doc
      * @param filePath
      * @param fileName
      * @return
      * @throws FileNotFoundException
      * @throws WTException
      * @throws PropertyVetoException
      * @throws IOException
      */
     public static WTDocument updatePrimaryContent(WTDocument doc, String filePath,
             String fileName) throws WTException {
	  if (!RemoteMethodServer.ServerFlag) {
          try {
              return (WTDocument) RemoteMethodServer.getDefault().invoke(
                      "updatePrimaryContent", DocUtil.class.getName(),
                      null, new Class[] { String.class },
                      new Object[] {});
          } catch (Exception e) {
              LOGGER.error(e.getMessage(), e);
              throw new WTException(e);
         }
	   }else {
	         ApplicationData applicationdata = ApplicationData
	                 .newApplicationData(doc);
	         ContentHolder ch = null;
	         try {
	             applicationdata.setFileName(fileName);
	             applicationdata.setUploadedFromPath(filePath);
	             applicationdata.setRole(ContentRoleType.PRIMARY);
	             ch = ContentHelper.service.getContents(doc);
	             ContentServerHelper.service.deleteContent(doc,
	                     ContentHelper.getPrimary((FormatContentHolder) ch));
	             ch = (FormatContentHolder) PersistenceHelper.manager.refresh(ch);
	             ContentServerHelper.service.updateContent(ch, applicationdata,
	                     filePath);
	         } catch (WTPropertyVetoException e) {
	             LOGGER.error(e.getLocalizedMessage());
	             throw new WTException(e);
	         } catch (PropertyVetoException e) {
	             LOGGER.error(e.getLocalizedMessage());
	             throw new WTException(e);
	         } catch (FileNotFoundException e) {
	             LOGGER.error(e.getLocalizedMessage());
	             throw new WTException(e);
	         } catch (IOException e) {
	             LOGGER.error(e.getLocalizedMessage());
	             throw new WTException(e);
	         }
	         return (WTDocument) ch;
	         }
     }


     /**
      * Get document by number version an diteration
      * 
      * @param number
      * @param version
      * @param iteration
      * @return
      * @throws WTException
      */
     public static WTDocument getDocumentByNumberByVersionByIteration(
             String number, String version, String iteration) throws WTException {
	  if (!RemoteMethodServer.ServerFlag) {
          try {
              return (WTDocument) RemoteMethodServer.getDefault().invoke(
                      "getDocumentByNumberByVersionByIteration", DocUtil.class.getName(),
                      null, new Class[] { String.class },
                      new Object[] {});
          } catch (Exception e) {
              LOGGER.error(e.getMessage(), e);
              throw new WTException(e);
         }
	  }else {
	         QuerySpec query = new QuerySpec();
	         query.setAdvancedQueryEnabled(true);
	         int docTable = query.appendClassList(WTDocument.class, true);
	         int docMasterTable = query.appendClassList(WTDocumentMaster.class,
	                 false);
	         String[] aliases = new String[2];
	         aliases[0] = query.getFromClause().getAliasAt(docTable);
	         aliases[1] = query.getFromClause().getAliasAt(docMasterTable);
	
	         TableColumn sVersion = new TableColumn(aliases[0],
	                 "VERSIONIDA2VERSIONINFO");
	         TableColumn sIter = new TableColumn(aliases[0],
	                 "ITERATIONIDA2ITERATIONINFO");
	         TableColumn iDA3MASTERREFERENCE = new TableColumn(aliases[0],
	                 "IDA3MASTERREFERENCE");
	
	         TableColumn numberCol = new TableColumn(aliases[1], "WTDOCUMENTNUMBER");
	         TableColumn iDA2A2Col = new TableColumn(aliases[1], "IDA2A2");
	
	         query.appendWhere(new SearchCondition(numberCol, SearchCondition.EQUAL,
	                 new ConstantExpression(number.toUpperCase())));
	         query.appendAnd();
	
	         query.appendWhere(new SearchCondition(iDA2A2Col, SearchCondition.EQUAL,
	                 iDA3MASTERREFERENCE));
	         query.appendAnd();
	
	         query.appendWhere(new SearchCondition(sVersion, SearchCondition.EQUAL,
	                 new ConstantExpression(version)));
	         query.appendAnd();
	
	         query.appendWhere(new SearchCondition(sIter, SearchCondition.EQUAL,
	                 new ConstantExpression(iteration)));
	
	         QueryResult qr = PersistenceHelper.manager.find((StatementSpec) query);
	
	         WTDocument document = null;
	         if (qr.hasMoreElements()) {
	             Object obj[] = (Object[]) qr.nextElement();
	             document = (WTDocument) obj[0];
	         }
	         return document;
	       }
     }
   
     /**
      * create wtdocument in windchill by name, container,folder and type
      * type default is WTDocument, folder default value is Default,
      * name and container must has value.
      * 
      * Only used to add in PDMLinkProduct and WTLibrary.
      * 
      * @param strCsv
      * @param strExcel
      * @param docName
      * @param designLab
      * @return
      * @throws WTException
      * 
      */
     public static WTDocument createDoc(String docName, String containername, String folder, String type)
             throws WTException {
	  if (!RemoteMethodServer.ServerFlag) {
          try {
              return (WTDocument) RemoteMethodServer.getDefault().invoke(
                      "createDoc", DocUtil.class.getName(),
                      null, new Class[] { String.class },
                      new Object[] {});
          } catch (Exception e) {
              LOGGER.error(e.getMessage(), e);
              throw new WTException(e);
         }
      }else {
	         LOGGER.debug("=============create doc begin============");
	         if (docName == null || "".equals(docName)) {
	             return null;
	         }
	         if (containername == null || "".equals(containername)) {
	             return null;
	         }
	         WTDocument document = WTDocument.newWTDocument();
	         try {
	             TypeDefinitionReference typeDefinitionRef = null;
	             if (type == null || "".equals(type)) {
	                 typeDefinitionRef =
	                         TypedUtility.getTypeDefinitionReference("wt.doc.WTDocument");
	             } else {
	                 typeDefinitionRef =
	                         TypedUtility.getTypeDefinitionReference(type);
	             }
	             document.setTypeDefinitionReference(typeDefinitionRef);
	             document.setName(docName);
	             document.setNumber(docName.toUpperCase());
	             // Set Container
	             WTContainer container = DocUtil.getWtContainerByName(containername);
	             // Set Folder
	             Folder docFold = null;
	             if (folder == null || "".equals(folder)) {
	                 docFold = FolderHelper.service.getFolder("Default", WTContainerRef.newWTContainerRef(container));
	             } else {
	                 docFold = FolderHelper.service.getFolder("Default/" + folder,
	                         WTContainerRef.newWTContainerRef(container));
	             }
	             FolderHelper.assignLocation((FolderEntry) document, docFold);
	             // Set Container
	             PersistenceHelper.manager.save(document);
	             LOGGER.debug("=============create doc end============");
	         } catch (WTException e) {
	             LOGGER.error(e.getLocalizedMessage(), e);
	             throw new WTException(e);
	         } catch (WTPropertyVetoException e) {
	             LOGGER.error(e.getLocalizedMessage(), e);
	             throw new WTException(e);
	         }
            return document;
         }
     }

     /**
      * Get container by name
      * 
      * @param name
      *            container name
      * @return container
      * @throws WTException
      *    Windchill exception
      */
     public static WTContainer getWtContainerByName(String name) throws WTException {

         WTContainer obj = null;
         if (!RemoteMethodServer.ServerFlag) {
             try {
                 Class aclass[] = { String.class };
                 Object aobj[] = { name };
                 obj = (WTContainer) RemoteMethodServer.getDefault().invoke("getWtContainerByName",
                         DocUtil.class.getName(), null, aclass, aobj);
             } catch (Exception e) {
                 LOGGER.error(e.getMessage(), e);
                 throw new WTException(e);
             }
         }
         else {
             QuerySpec qs = new QuerySpec(WTContainer.class);
             SearchCondition sc = new SearchCondition(WTContainer.class, WTContainer.NAME, "=", name);
             qs.appendWhere(sc);
            //qr = PersistenceServerHelper.manager.query(qs);
             QueryResult  qr = PersistenceHelper.manager.find(qs);
             while (qr.hasMoreElements()) {
                 obj = (WTContainer) qr.nextElement();
             }
         }
         return obj;
     }

     /**
      * get document version list, just as A.1, A.2 and so on.
      * 
      * @param docNum
      * @return
      */
     public static List<String> getDocumentVersionList(String docNum)
             throws WTException {

         if (!RemoteMethodServer.ServerFlag) {
             try {

                 return (List<String>) RemoteMethodServer.getDefault().invoke(
                         "getDocumentVersionList", DocUtil.class.getName(),
                         null, new Class[] { String.class },
                         new Object[] { docNum });
             } catch (Exception e) {
                 LOGGER.error(e.getMessage(), e);
                 throw new WTException(e);
             }
         } else {
             QuerySpec spec = new QuerySpec(WTDocument.class);
             spec.appendSearchCondition(new SearchCondition(WTDocument.class,
                     WTDocument.NUMBER, SearchCondition.EQUAL, docNum, true));

             QueryResult results = PersistenceHelper.manager.find(spec);
             List<String> list = new ArrayList<String>();
             TreeMap<String, String> sortMap = new TreeMap<String, String>();

             NumberFormat nf = NumberFormat.getInstance();
             nf.setMinimumIntegerDigits(5);
             nf.setMaximumIntegerDigits(5);
             nf.setGroupingUsed(false);

             while (results.hasMoreElements()) {
                 WTDocument doc = (WTDocument) results.nextElement();
                 sortMap.put(
                         doc.getVersionIdentifier().getValue()
                                 + "."
                                 + nf.format(Integer.parseInt(doc
                                         .getIterationIdentifier().getValue())),
                         doc.getVersionIdentifier().getValue() + "."
                                 + doc.getIterationIdentifier().getValue());
             }
             for (String key : sortMap.keySet()) {
                 list.add(sortMap.get(key));
             }
             return list;
         }
     }
    
     public static ApplicationData updateApplicationDataToDoc(WTDocument document, String fileName,
             String filePath, boolean isPrimary) throws WTException, WTPropertyVetoException,
             java.beans.PropertyVetoException, IOException {
         ApplicationData applicationdata = ApplicationData.newApplicationData(document);
         applicationdata.setFileName(fileName);
         applicationdata.setUploadedFromPath(filePath);
         if (isPrimary) {
             applicationdata.setRole(ContentRoleType.PRIMARY);
         }
         wt.content.ContentHolder ch = wt.content.ContentHelper.service.getContents(document);
         // delete original ApplicationData
         if (!isPrimary) {// Attachment
             Vector apps = ContentHelper.getApplicationData(ch);
             for (int i = 0; i < apps.size(); i++) {
                 ApplicationData app = (ApplicationData) apps.elementAt(i);
                 if (app.getFileName().equals(fileName)) {
                     ContentServerHelper.service.deleteContent(ch, app);
                 }
             }
         } else {// Primary
             ContentItem item = (ContentItem) ContentHelper.service.getPrimary(document);
             ContentServerHelper.service.deleteContent(ch, item);
         }
         if (!RemoteMethodServer.ServerFlag) {//
             try {
                 Class aclass[] = { ContentHolder.class, ApplicationData.class, String.class };
                 Object aobj[] = { ch, applicationdata, filePath + fileName };
                 RemoteMethodServer.getDefault().invoke("addApplicationDataToDoc", DocUtil.class.getName(),
                         null, aclass, aobj);
             } catch (Exception e) {
                 LOGGER.error("-------[addApplicationDataToDoc]------------" + e.getLocalizedMessage());
             }
         } else {// 
             ContentServerHelper.service.updateContent(ch, applicationdata, filePath);
             try {
                 ContentServerHelper.service.updateHolderFormat((FormatContentHolder) document);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return applicationdata;
     }

     /**
      * only get attachment 
      * Get Attachment by pre-fix name
      * @param doc
      * @param preFix
      * @return
      */
     public static ApplicationData getAttachement(WTDocument doc,String preFix){
         QueryResult qr;
         try {
             qr = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
             while (qr.hasMoreElements()) {
                 ApplicationData appData = (ApplicationData) qr.nextElement(); 
                 if(appData != null){
                     String fileName = appData.getFileName();
                     if(fileName.startsWith(preFix)){
                         return appData;
                     }
                 }
             }
         } catch (WTException e) {
             e.printStackTrace();
         }
         return null;
         
     }


     /**
      * only get Primary 
      * Get Attachment by pre-fix name
      * @param doc
      * @param preFix
      * @return
      */
     public static ApplicationData getPrimaryContent(WTDocument doc,String preFix){
         QueryResult qr;
         try {
             qr = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
             while (qr.hasMoreElements()) {
                 ApplicationData appData = (ApplicationData) qr.nextElement(); 
                 if(appData != null){
                     String fileName = appData.getFileName();
                     if(fileName.startsWith(preFix)){
                         return appData;
                     }
                 }
             }
         } catch (WTException e) {
             e.printStackTrace();
         }
         return null;
         
     }
     
     /**
      * revise WTDocument
      * 
      * @param doc
      *            WTDocument
      * @return WTDocument
      * @throws WTException
      */
     public static WTDocument reviseWTDocument(WTDocument document, String comment) throws WTException {
         WTDocument wtdocument = document;
         try {
             if (wtdocument == null) {
                 return null;
             }
             WTContainer container = wtdocument.getContainer();
             WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container); // container.getContainerReference();//
             TeamReference teamReference = wtdocument.getTeamId();
             Folder oldFoler = FolderHelper.getFolder(wtdocument);
             if (oldFoler == null) {
                 String strLocation = wtdocument.getLocation();
                 oldFoler = FolderHelper.service.getFolder(strLocation, containerRef);
             }

             wtdocument = (WTDocument) wt.vc.VersionControlHelper.service.newVersion((wt.vc.Versioned) wtdocument);
             if (teamReference != null) {
                 wtdocument.setTeamId(teamReference);
             }
             VersionControlHelper.setNote(wtdocument, comment == null ? "" : comment);
             wtdocument.setContainer(container);

             FolderHelper.assignLocation((FolderEntry) wtdocument, oldFoler);

             wtdocument = (WTDocument) PersistenceHelper.manager.save(wtdocument);
             wtdocument = (WTDocument) PersistenceHelper.manager.refresh(wtdocument);

         } catch (Exception e) {
             LOGGER.error("Auto Revise Failed :" + e.toString());
             LOGGER.error("Original comment :" + comment);
             throw new WTException(e);
         }
         return wtdocument;
     }

   
    public static void test() throws RemoteException, InvocationTargetException, WTException{
		
    	WTDocument doc = DocUtil.getDocumentByNumber("0000000101");
    	//System.out.println(getDocumentByName("test02"));
		if(doc != null){
    		ApplicationData obj = getPrimaryContent(doc);	
//			System.out.println("obj Role£º" +obj.getRole());

			//String filePath = "D:\\ptc\\Windchill_10.2\\Windchill\\temp\\test01.txt";
			//String fileName="test01.txt";
			String filePath = "D:\\cba.txt";
			String fileName="cba.txt";
			
			//addAttachments(doc, filePath, fileName);
			//addPrimaryContent(doc, filePath, fileName);
			//deleteContentByFileName(doc, fileName);
			//Doc.deleteContents(doc);//delete all contents about the doc.
			//Doc.updatePrimaryContent(doc, filePath, fileName);
			//System.out.println(Doc.getDocumentByNumberByVersionByIteration("0000000101", "A", "2"));
			//Doc.createDoc("test01", "²âÊÔ²úÆ·", "ÎÄµµÆëÌ×¼ì²é", "wt.doc.WTDocument");
		/*
		 * List<String> list=getDocumentVersionList("WORKFLOW_NODE_TREND_CONFIG");	
		   System.out.println(list);
		*/
			/*try {
				System.out.println(updateApplicationDataToDoc(doc,fileName,filePath,true));
			} catch (WTPropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			//System.out.println("getAttachement"+"&&&&&&"+getAttachement(doc,"c"));
			//System.out.println("getPrimaryContent"+"&&&&&&"+getPrimaryContent(doc,"c"));
			System.out.println("-----------"+DocUtil.reviseWTDocument(doc, "ddd"));
			
		}
        /*System.out.println(Doc.getDocumentByNumberAndState("ENCODING_RULES_CONFIG", "INWORK"));*/ 
    	/*
    	WTDocument doc1=Doc.getDocumentByName("test01");
    	if(doc1 != null){
    		Doc.documentRename("test02", doc1);
    		System.out.println(Doc.documentRename("test02", doc1));
    	}
    	*/
System.out.println(reviseWTDocument(doc,"hahahahaha"));
	
	}
    
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		if (!RemoteMethodServer.ServerFlag) {
			try {
				RemoteMethodServer.getDefault().invoke("test", DocUtil.class.getName(), null,
						new Class[] {},
						new Object[] {});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

    
    
}
