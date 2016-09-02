package cn.elead.tool.wc;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.content.Streamed;
import wt.doc.DocumentType;
import wt.doc.WTDocument;
import wt.doc.WTDocumentDependencyLink;
import wt.doc.WTDocumentHelper;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.doc.WTDocumentUsageLink;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.EPMDocumentMasterIdentity;
import wt.epm.structure.EPMDescribeLink;
import wt.fc.IdentificationObject;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.Link;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.ObjectSetVector;
import wt.fc.ObjectVector;
import wt.fc.ObjectVectorIfc;
import wt.fc.PersistInfo;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTValuedHashMap;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.folder.FolderNotFoundException;
import wt.iba.definition.AbstractAttributeDefinition;
import wt.iba.definition.StringDefinition;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.definition.litedefinition.IBAUtility;
import wt.iba.definition.litedefinition.IntegerDefView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.value.FloatValue;
import wt.iba.value.IntegerValue;
import wt.iba.value.StringValue;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.inf.library.WTLibrary;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.part.WTPartReferenceLink;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.pom.Transaction;
import wt.query.ArrayExpression;
import wt.query.AttributeRange;
import wt.query.ClassAttribute;
import wt.query.ConstantExpression;
import wt.query.OrderBy;
import wt.query.OrderByExpression;
import wt.query.QuerySpec;
import wt.query.RangeExpression;
import wt.query.RelationalExpression;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.query.SubSelectExpression;
import wt.query.TableColumn;
import wt.query.WhereExpression;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.TeamReference;
import wt.type.ClientTypedUtility;
import wt.type.TypeDefinitionForeignKey;
import wt.type.TypeDefinitionReference;
import wt.type.Typed;
import wt.type.TypedUtility;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.Iterated;
import wt.vc.Mastered;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.config.ConfigHelper;
import wt.vc.config.IteratedOrderByPrimitive;
import wt.vc.config.LatestConfigSpec;
import wt.vc.config.VersionedOrderByPrimitive;
import wt.vc.struct.StructHelper;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfState;
import wt.workflow.work.AssociatedProcessProcessor;
import wt.workflow.work.WorkItem;

import com.google.gwt.rpc.client.impl.RemoteException;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.TypeIdentifierHelper;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinitionMaster;
import com.ptc.windchill.enterprise.copy.server.CoreMetaUtility;
import com.ptc.windchill.enterprise.wip.WIPUtils;
import com.ptc.windchill.uwgm.soap.uwgmdb.Master;



public class DocUtil implements RemoteAccess {
	private static String className = DocUtil.class.getName();
	private final static Logger LOGGER = LogR.getLogger(DocUtil.class.getName());

	/**
	 * To judge the String whether an empty or not. Then returns true
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s) {

		if (s != null && s.length() != 0) {
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
			if (conditionCount > 0) {
				querySpec.appendAnd();
			}
			SearchCondition searchCondi = new SearchCondition(WTDocument.class,
					WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state);
			querySpec.appendWhere(searchCondi, new int[] { 0 });
		}
		QueryResult qr = PersistenceHelper.manager
				.find((StatementSpec) querySpec);
		qr = new LatestConfigSpec().process(qr);
		if (qr.hasMoreElements()) {
			doc = (WTDocument) qr.nextElement();
		}
		return doc;
	}

/*	*//**
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
						"getDocumentByNumber", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			if (DocUtil.isEmpty(number)) {
				LOGGER.error("The number is null in the method of getDocumentByNumber()");
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
						null, new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			if (DocUtil.isEmpty(number)) {
				LOGGER.error("The number is null in the method of getDocumentByNumberAndState.");
				return null;
			}
			if (DocUtil.isEmpty(state)) {
				LOGGER.error("The state is null in the method of getDocumentByNumberAndState.");
				return null;
			} 
			return getDocument(number, null, state);
		    }
	}

	/**
     * Judge whether the document is exist. 
     */
	public static boolean isDocumentExist(String strName){
    	WTDocumentMaster wtdocumentmaster = null;
        if(!StringUtils.isEmpty(strName)){
        	try {
        		wtdocumentmaster =  getDocumentMasterByName(strName);

			} catch (WTException e) {
				LOGGER.error(">>>>>"+e);
			}
        }
        if (wtdocumentmaster == null) {
            return false;
        } else {
            return true;
        }
    }

   /**
     * Get document master by document number
     */
    public static WTDocumentMaster getDocumentMasterByName(String docName) throws WTException {
        QuerySpec querySpec = new QuerySpec(WTDocumentMaster.class);
        docName = docName.toUpperCase();
        WhereExpression searchCondition = new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NAME, SearchCondition.EQUAL, docName, false);
        querySpec.appendWhere(searchCondition,new int[] { 0 });
        QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec)querySpec);
        while (queryResult.hasMoreElements()) {
            WTDocumentMaster docMaster = (WTDocumentMaster) queryResult.nextElement();
            return docMaster;
        }
        return null;
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
		WTDocument doc = null;
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"getDocumentByName", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			if(!DocUtil.isEmpty(name)&& isDocumentExist(name)){
				doc = getDocument(null, name, null);
			}else{
				LOGGER.error(" name is null  getDocumentByName");
				return null;
			}
			LOGGER.debug("param doc name is " + name);
			
			//return getDocument(null, name, null);
			return doc;
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
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("getPrimaryContent", DocUtil.class.getName(), null,
								new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
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
	/*public static boolean documentRename(String newDocName, WTDocument doc)
			throws WTException {
		LOGGER.info("newDocName = " + newDocName);
		boolean result = false;
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (boolean) RemoteMethodServer.getDefault().invoke(
						"WTDocument", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			try {
				// if the same as original then return true
				if (newDocName.equals(doc.getName())) {
					result = true;
				} else {
					WTDocumentMaster docmaster = (WTDocumentMaster) doc
							.getMaster();
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
	}*/

	/**
	 * Add the contentHolder attenchMent  and can save the same name files.
	 * 
	 * @param holder
	 * @param filePath
	 * @param fileName
	 * @param contentType
	 * @throws WTException
	 */
	public static void addContent(ContentHolder holder, String filePath,
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
	 * Add attachments and update the same name file. 
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
	 * delete all contents about the doc include PrimaryContent
	 * @param WTDocument
	 * @return WTDocument
	 * @throws WTException
	 */
	public static WTDocument deleteContents(WTDocument doc) throws WTException {
		FormatContentHolder holder = null;
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"deleteContents", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
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
	public static WTDocument updatePrimaryContent(WTDocument doc,
			String filePath, String fileName) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"updatePrimaryContent", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
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
				ch = (FormatContentHolder) PersistenceHelper.manager
						.refresh(ch);
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
						"getDocumentByNumberByVersionByIteration",
						DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
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

			TableColumn numberCol = new TableColumn(aliases[1],
					"WTDOCUMENTNUMBER");
			TableColumn iDA2A2Col = new TableColumn(aliases[1], "IDA2A2");

			query.appendWhere(new SearchCondition(numberCol,
					SearchCondition.EQUAL, new ConstantExpression(number
							.toUpperCase())));
			query.appendAnd();

			query.appendWhere(new SearchCondition(iDA2A2Col,
					SearchCondition.EQUAL, iDA3MASTERREFERENCE));
			query.appendAnd();

			query.appendWhere(new SearchCondition(sVersion,
					SearchCondition.EQUAL, new ConstantExpression(version)));
			query.appendAnd();

			query.appendWhere(new SearchCondition(sIter, SearchCondition.EQUAL,
					new ConstantExpression(iteration)));

			QueryResult qr = PersistenceHelper.manager
					.find((StatementSpec) query);

			WTDocument document = null;
			if (qr.hasMoreElements()) {
				Object obj[] = (Object[]) qr.nextElement();
				document = (WTDocument) obj[0];
			}
			return document;
		}
	}

	/**
	 * create wtdocument in windchill by name, container,folder and type type
	 * default is WTDocument, folder default value is Default, name and
	 * container must has value.
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
	public static WTDocument createDoc(String docName, String containername,
			String folder, String type) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"createDoc", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
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
					typeDefinitionRef = TypedUtility
							.getTypeDefinitionReference("wt.doc.WTDocument");
				} else {
					typeDefinitionRef = TypedUtility
							.getTypeDefinitionReference(type);
				}
				document.setTypeDefinitionReference(typeDefinitionRef);
				document.setName(docName);
				document.setNumber(docName.toUpperCase());
				// Set Container
				WTContainer container = DocUtil.getWtContainerByName(containername);
				// Set Folder
				Folder docFold = null;
				if (folder == null || "".equals(folder)) {
					docFold = FolderHelper.service.getFolder("Default",
							WTContainerRef.newWTContainerRef(container));
				} else {
					docFold = FolderHelper.service.getFolder("Default/"
							+ folder,
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
	 *             Windchill exception
	 */
	public static WTContainer getWtContainerByName(String name)
			throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTContainer) RemoteMethodServer.getDefault().invoke(
						"getWtContainerByName", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			WTContainer obj = null;
			if (!RemoteMethodServer.ServerFlag) {
				try {
					Class aclass[] = { String.class };
					Object aobj[] = { name };
					obj = (WTContainer) RemoteMethodServer.getDefault().invoke(
							"getWtContainerByName", DocUtil.class.getName(), null,
							aclass, aobj);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					throw new WTException(e);
				}
			} else {
				QuerySpec qs = new QuerySpec(WTContainer.class);
				SearchCondition sc = new SearchCondition(WTContainer.class,
						WTContainer.NAME, "=", name);
				qs.appendWhere(sc);
				// qr = PersistenceServerHelper.manager.query(qs);
				QueryResult qr = PersistenceHelper.manager.find(qs);
				while (qr.hasMoreElements()) {
					obj = (WTContainer) qr.nextElement();
				}
			}
			return obj;
		}
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
						"getDocumentVersionList", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] { docNum });
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

	public static ApplicationData updateApplicationDataToDoc(
			WTDocument document, String fileName, String filePath,
			boolean isPrimary) throws WTException, WTPropertyVetoException,
			java.beans.PropertyVetoException, IOException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("updateApplicationDataToDoc",
								DocUtil.class.getName(), null,
								new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			ApplicationData applicationdata = ApplicationData
					.newApplicationData(document);
			applicationdata.setFileName(fileName);
			applicationdata.setUploadedFromPath(filePath);
			if (isPrimary) {
				applicationdata.setRole(ContentRoleType.PRIMARY);
			}
			wt.content.ContentHolder ch = wt.content.ContentHelper.service
					.getContents(document);
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
				ContentItem item = (ContentItem) ContentHelper.service
						.getPrimary(document);
				ContentServerHelper.service.deleteContent(ch, item);
			}
			if (!RemoteMethodServer.ServerFlag) {//
				try {
					Class aclass[] = { ContentHolder.class,
							ApplicationData.class, String.class };
					Object aobj[] = { ch, applicationdata, filePath + fileName };
					RemoteMethodServer.getDefault().invoke(
							"addApplicationDataToDoc", DocUtil.class.getName(),
							null, aclass, aobj);
				} catch (Exception e) {
					LOGGER.error("-------[addApplicationDataToDoc]------------"
							+ e.getLocalizedMessage());
				}
			} else {//
				ContentServerHelper.service.updateContent(ch, applicationdata,
						filePath);
				try {
					ContentServerHelper.service
							.updateHolderFormat((FormatContentHolder) document);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return applicationdata;
		}
	}

	/**
	 * only get attachment Get Attachment by pre-fix name
	 * 
	 * @param doc
	 * @param preFix
	 * @return
	 * @throws WTException
	 */
	public static ApplicationData getAttachement(WTDocument doc, String preFix)
			throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("getAttachement", DocUtil.class.getName(), null,
								new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			QueryResult qr;
			try {
				qr = ContentHelper.service.getContentsByRole(doc,
						ContentRoleType.SECONDARY);
				while (qr.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) qr
							.nextElement();
					if (appData != null) {
						String fileName = appData.getFileName();
						if (fileName.startsWith(preFix)) {
							return appData;
						}
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	/**
	 * only get Primary Get Attachment by pre-fix name
	 * 
	 * @param doc
	 * @param preFix
	 * @return
	 * @throws WTException
	 */
	public static ApplicationData getPrimaryContent(WTDocument doc,
			String preFix) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("getAttachement", DocUtil.class.getName(), null,
								new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			QueryResult qr;
			try {
				qr = ContentHelper.service.getContentsByRole(doc,
						ContentRoleType.PRIMARY);
				while (qr.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) qr
							.nextElement();
					if (appData != null) {
						String fileName = appData.getFileName();
						if (fileName.startsWith(preFix)) {
							return appData;
						}
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * add remarks/comment and revise WTDocument.
	 * 
	 * @param doc
	 *            WTDocument
	 * @return WTDocument
	 * @throws WTException
	 */
	public static WTDocument reviseWTDocument(WTDocument document,
			String comment) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"reviseWTDocument", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			WTDocument wtdocument = document;
			try {
				if (wtdocument == null) {
					return null;
				}
				WTContainer container = wtdocument.getContainer();
				WTContainerRef containerRef = WTContainerRef
						.newWTContainerRef(container); // container.getContainerReference();//
				TeamReference teamReference = wtdocument.getTeamId();
				Folder oldFoler = FolderHelper.getFolder(wtdocument);
				if (oldFoler == null) {
					String strLocation = wtdocument.getLocation();
					oldFoler = FolderHelper.service.getFolder(strLocation,
							containerRef);
				}

				wtdocument = (WTDocument) wt.vc.VersionControlHelper.service
						.newVersion((wt.vc.Versioned) wtdocument);
				Long ida2da = wtdocument.getPersistInfo().getObjectIdentifier()
						.getId();
				if (teamReference != null) {
					wtdocument.setTeamId(teamReference);
				}
				VersionControlHelper.setNote(wtdocument, comment == null ? ""
						: comment);
				wtdocument.setContainer(container);

				FolderHelper.assignLocation((FolderEntry) wtdocument, oldFoler);

				wtdocument = (WTDocument) PersistenceHelper.manager
						.save(wtdocument);
				wtdocument = (WTDocument) PersistenceHelper.manager
						.refresh(wtdocument);

			} catch (Exception e) {
				LOGGER.error("Auto Revise Failed :" + e.toString());
				LOGGER.error("Original comment :" + comment);
				throw new WTException(e);
			}
			return wtdocument;
		}
	}

	/**
	 * Queries WTDocument objects.
	 * 
	 * @param states
	 *            The internal name of life cycle states, e.g.
	 *            PRODUCTIONRELEASED.
	 * @param softTypes
	 *            The soft types of WTDocument objects, e.g.
	 *            wt.doc.WTDocument|com.lenovo.ElementType|com.lenovo.IMG
	 * @return A list of WTDocument objects.
	 * @throws WTException
	 *             Failed to query WTDocument objects with the given parameters.
	 * 
	 */
	public static List<WTDocument> queryWTDocument(String[] states,
			String[] softTypes) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (List<WTDocument>) RemoteMethodServer.getDefault()
						.invoke("queryWTDocument", DocUtil.class.getName(), null,
								new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			LOGGER.debug(">>>>> Enter => queryWTDocument(String[] states, String[] softTypes)");
			List<WTDocument> result = new ArrayList<WTDocument>();
			if ((states == null || states.length == 0)
					&& (softTypes == null || softTypes.length == 0)) {
				return result;
			}
			
			try {
				QuerySpec qs = new QuerySpec();
				int indexWTDocument = qs
						.appendClassList(WTDocument.class, true);
				WhereExpression where = null;
				// Specifies the soft types of target object.
				if (softTypes != null && softTypes.length > 0) {
					int indexWTTypeDefinition = qs.appendClassList(
							WTTypeDefinition.class, false);
					LOGGER.debug("indexWTTypeDefinition"
							+ indexWTTypeDefinition);
					int indexWTTypeDefinitionMaster = qs.appendClassList(
							WTTypeDefinitionMaster.class, false);
					LOGGER.debug("indexWTTypeDefinitionMaster"
							+ indexWTTypeDefinitionMaster);
					where = new SearchCondition(
							new ClassAttribute(WTDocument.class,
									"typeDefinitionReference.key.id"),
							SearchCondition.EQUAL, new ClassAttribute(
									WTTypeDefinition.class,
									"thePersistInfo.theObjectIdentifier.id"));
					qs.appendWhere(where, new int[] { indexWTDocument,
							indexWTTypeDefinition });

					where = new SearchCondition(new ClassAttribute(
							WTTypeDefinition.class, "masterReference.key.id"),
							SearchCondition.EQUAL, new ClassAttribute(
									WTTypeDefinitionMaster.class,
									"thePersistInfo.theObjectIdentifier.id"));
					qs.appendAnd();
					qs.appendWhere(where, new int[] { indexWTTypeDefinition,
							indexWTTypeDefinitionMaster });

					if (softTypes.length == 1) {
						where = new SearchCondition(
								WTTypeDefinitionMaster.class, "intHid",
								SearchCondition.EQUAL, softTypes[0]);
					} else {
						where = new SearchCondition(
								WTTypeDefinitionMaster.class, "intHid",
								softTypes, true);
					}
					qs.appendAnd();
					qs.appendWhere(where,
							new int[] { indexWTTypeDefinitionMaster });
				}
				// Specifies the life cycle state of target object.
				if (states != null && states.length > 0) {
					if (states.length == 1) {
						where = new SearchCondition(WTDocument.class,
								"state.state", SearchCondition.EQUAL, states[0]);
						
					} else {
						where = new SearchCondition(WTDocument.class,
								"state.state", states, true);
					}
					if (qs.getConditionCount() > 0
							&& qs.getWhere().endsWith(")")) {
						qs.appendAnd();
					}
					qs.appendWhere(where, new int[] { indexWTDocument });
				}
				// Sorted by modifying time in descending order.
				OrderBy orderby = new OrderBy(new ClassAttribute(
						WTDocument.class, "thePersistInfo.modifyStamp"), true);
				qs.appendOrderBy(orderby, new int[] { indexWTDocument });
				LOGGER.debug(">>>>> queryWTDocument(): qs=" + qs);
				QueryResult qr = PersistenceHelper.manager
						.find((StatementSpec) qs);
				LOGGER.debug(">>>>> queryWTDocument(): qr=" + qr);
				if (qr != null) {
					ObjectVector vector = new ObjectVector();
					while (qr.hasMoreElements()) {
						Object obj = qr.nextElement();
						if (obj instanceof Persistable[]) {
							Persistable[] apersist = (Persistable[]) obj;
							vector.addElement(apersist[0]);
						} else {
							vector.addElement(obj);
						}
					}
					if (!vector.isEmpty()) {
						qr = new QueryResult(vector);
					}
					qr = (new LatestConfigSpec()).process(qr);
				}
				if (qr != null) {
					while (qr.hasMoreElements()) {
						result.add((WTDocument) qr.nextElement());
					}
				}
				LOGGER.debug(">>>>> queryWTDocument(): result.size()="
						+ result.size());
				return result;
			} catch (Exception e) {
				String errorMsg = MessageFormat
						.format(">>>>> Failed to query WTDocument objects with the given parameters: states-[{0}], softTypes-[{1}].",
								states, softTypes);
				LOGGER.error(">>>>>" + errorMsg, e);
				throw new WTException(e, errorMsg);
			}
		}
	}

	public static List<WTDocument> queryWTDocument(String state, String softType)
			throws WTException {
		String[] states = (state != null && !state.trim().isEmpty()) ? new String[] { state
				.trim() } : null;
		String[] softTypes = (softType != null && !softType.trim().isEmpty()) ? new String[] { softType }
				: null;
		return queryWTDocument(states, softTypes);
	}

	/**
	 * Queries EPMDocument objects.
	 * 
	 * @param number
	 *            The number of EPMDocument object.
	 * @param name
	 *            The name of EPMDocument object.
	 * @param state
	 *            The internal name of life cycle state, e.g. INWORK.
	 * @param version
	 *            The version of EPMDocument object.
	 * @param iteration
	 *            The iteration of EPMDocument object.
	 * @return A QueryResult contains EPMDocument objects, sorted by modifying
	 *         time in descending order.
	 * @throws WTException
	 *             Failed to query EPMDocument objects with the given
	 *             parameters.
	 */
	public static QueryResult queryEPMDocument(String number, String name,
			String state, String version, String iteration) throws WTException {
		try {
			QuerySpec queryspec = new QuerySpec(EPMDocument.class);
			WhereExpression where = null;
			// Specifies the iteration of EPMDocument object.
			if (iteration != null && !iteration.trim().isEmpty()&&isEPMDocumentExist(number)) {
				where = new SearchCondition(EPMDocument.class,
						"iterationInfo.identifier.iterationId",
						SearchCondition.EQUAL, iteration);
				queryspec.appendWhere(where, new int[] { 0 });
			}
			// Specifies the version of EPMDocument object.
			if (version != null && !version.trim().isEmpty()&&isEPMDocumentExist(number)) {
				where = new SearchCondition(EPMDocument.class,
						"versionInfo.identifier.versionId",
						SearchCondition.EQUAL, version);
				if (queryspec.getConditionCount() > 0
						&& queryspec.getWhere().endsWith(")")) {
					queryspec.appendAnd();
				}
				queryspec.appendWhere(where, new int[] { 0 });
			}
			// Specifies the life cycle state of EPMDocument object.
			if (state != null && !state.trim().isEmpty()&&isEPMDocumentExist(number)) {
				where = new SearchCondition(EPMDocument.class,
						EPMDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL,
						state);
				if (queryspec.getConditionCount() > 0
						&& queryspec.getWhere().endsWith(")")) {
					queryspec.appendAnd();
				}
				queryspec.appendWhere(where, new int[] { 0 });
			}
			// Specifies the name of EPMDocument object.
			if (name != null && !name.trim().isEmpty()&&isEPMDocumentExist(number)) {
				where = new SearchCondition(EPMDocument.class,
						EPMDocument.NAME, SearchCondition.EQUAL, name);
				if (queryspec.getConditionCount() > 0
						&& queryspec.getWhere().endsWith(")")) {
					queryspec.appendAnd();
				}
				queryspec.appendWhere(where, new int[] { 0 });
			}
			// Specifies the number of EPMDocument object.
			if (number != null && !number.trim().isEmpty()&&isEPMDocumentExist(number)) {
				where = new SearchCondition(EPMDocument.class,
						EPMDocument.NUMBER, SearchCondition.EQUAL, number);
				if (queryspec.getConditionCount() > 0
						&& queryspec.getWhere().endsWith(")")) {
					queryspec.appendAnd();
				}
				queryspec.appendWhere(where, new int[] { 0 });
			}
			// Sorted by modifying time in descending order.
			OrderBy orderby = new OrderBy(new ClassAttribute(EPMDocument.class,
					"thePersistInfo.modifyStamp"), true);
			queryspec.appendOrderBy(orderby, new int[] { 0 });
			LOGGER.debug(">>>>> DocUtil.queryEPMDocument(): queryspec="
					+ queryspec);
			return PersistenceHelper.manager.find((StatementSpec) queryspec);
		} catch (Exception e) {
			String errorMsg = MessageFormat
					.format("Failed to query EPMDocument objects with the given parameters: number-[{0}],"
							+ " name-[{1}], life cycle state-[{2}], version-[{3}], iteration-[{4}].",
							number, name, state, version, iteration);
			LOGGER.error(">>>>>" + errorMsg, e);
			throw new WTException(e, errorMsg);
		}
		
	}

	/**
	 * Queries EPMDocument objects by specifying its number and version, and
	 * returns the latest iteration.
	 * 
	 * @param number
	 *            The number of EPMDocument object.
	 * @param version
	 *            The version of EPMDocument object (e.g. "A", "AB"). Note: do
	 *            not include iteration.
	 * @return the EPMDocument object.
	 * @throws WTException
	 *             Failed to query EPMDocument objects.
	 */
	public static EPMDocument getEPMDocument(String number, String version)
			throws WTException {
		QueryResult result = queryEPMDocument(number, null, null, version, null);
		if (result != null && result.hasMoreElements()) {
			return (EPMDocument) result.nextElement();
		}
		return null;
	}

	/**
	 * 
	 * @param number
	 * @return
	 * @throws WTException
	 */
	public static EPMDocument getEPMDocumentByNumber(String number)
			throws WTException {

		EPMDocument epmdoc = null;
		String condition = SearchCondition.EQUAL;
		String keyWord = number;

		QuerySpec qs = new QuerySpec();
		int epmId = qs.addClassList(EPMDocument.class, true);
		int epmmId = qs.addClassList(EPMDocumentMaster.class, false);
		WhereExpression where = null;
		ClassAttribute epm = new ClassAttribute(EPMDocument.class,
				"masterReference.key.id");
		ClassAttribute epmm = new ClassAttribute(EPMDocumentMaster.class,
				WTAttributeNameIfc.ID_NAME);
		where = new SearchCondition(epm, SearchCondition.EQUAL, epmm);
		qs.appendWhere(where, new int[] { epmId, epmmId });
		qs.appendAnd();
		where = new SearchCondition(EPMDocument.class,
				WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
		qs.appendWhere(where, new int[] { epmId });
		qs.appendAnd();
		where = new SearchCondition(EPMDocumentMaster.class,
				EPMDocumentMaster.NUMBER, condition, keyWord);
		qs.appendWhere(where, new int[] { epmmId });
		QueryResult queryresult = PersistenceHelper.manager
				.find((StatementSpec) qs);
		LatestConfigSpec lcs = new LatestConfigSpec();
		ObjectVectorIfc obv = new ObjectVector();
		if (queryresult != null) {
			while (queryresult.hasMoreElements()) {
				Object[] objs = (Object[]) queryresult.nextElement();
				Object obj = objs[0];
				obv.addElement(obj);
			}
		}
		QueryResult qr2 = new QueryResult(obv);
		qr2 = lcs.process(qr2);
		while (qr2.hasMoreElements()) {

			epmdoc = (EPMDocument) qr2.nextElement();
			break;
		}

		return epmdoc;
	}

	/**
	 * Gets the latest WTDocument object by WTDocumentMaster.
	 * 
	 * @param docMaster
	 *            the given WTDocumentMaster object.
	 * @return the latest WTDocument object of the given WTDocumentMaster.
	 * @throws WTException
	 *             Failed to get the latest WTDocument object by
	 *             WTDocumentMaster.
	 */
	public static WTDocument getLatestDoc(WTDocumentMaster docMaster)
			throws WTException {
		if (docMaster != null) {
			try {
				QueryResult qr = VersionControlHelper.service
						.allVersionsOf(docMaster);
				if (qr != null && qr.hasMoreElements()) {
					return (WTDocument) qr.nextElement();
				}
			} catch (WTException e) {
				String errorMsg = MessageFormat
						.format("Failed to get the latest WTDocument object by WTDocumentMaster-[{0}].-[{0}].",
								((docMaster != null) ? docMaster
										.getDisplayIdentity() : null));
				LOGGER.error(">>>>>" + errorMsg, e);
				throw new WTException(e, errorMsg);
			}
		}
		return null;
	}

	/**
	 * Gets the latest EPMDocument object by EPMDocumentMaster.
	 * 
	 * @param epmMaster
	 *            the given EPMDocumentMaster object.
	 * @return the latest EPMDocument object of the given EPMDocumentMaster.
	 * @throws WTException
	 *             Failed to get the latest EPMDocument object by
	 *             EPMDocumentMaster.
	 */
	public static EPMDocument getLatestEPMDoc(EPMDocumentMaster epmMaster)
			throws WTException {
		if (epmMaster != null) {
			try {
				QueryResult qr = VersionControlHelper.service
						.allVersionsOf(epmMaster);
				if (qr != null && qr.hasMoreElements()) {
					return (EPMDocument) qr.nextElement();
				}
			} catch (WTException e) {
				String errorMsg = MessageFormat
						.format("Failed to get the latest EPMDocument object by EPMDocumentMaster-[{0}].",
								((epmMaster != null) ? epmMaster
										.getDisplayIdentity() : null));
				LOGGER.error(">>>>>" + errorMsg, e);
				throw new WTException(e, errorMsg);
			}
		}
		return null;
	}

	/**
	 * Rename EPMDocument object to the given new name.
	 * 
	 * @param epmDoc
	 *            the EPMDocument object to be renamed.
	 * @param newName
	 *            the new name of EPMDocument object.
	 * @throws WTException
	 *             Failed to rename the given EPMDocument object to new name.
	 */
	public static void renameEPMDoc(EPMDocument epmDoc, String newName)
			throws WTException {
		
		if (epmDoc == null || newName == null || newName.isEmpty()) {
			LOGGER.error(MessageFormat
					.format(">>>>> Cannot rename the given EPMDocument object-[{0}] to new name-[{1}].",
							((epmDoc != null) ? epmDoc.getDisplayIdentity()
									: null), newName));
			return;
		}
		EPMDocumentMaster epmDocMaster = (EPMDocumentMaster) epmDoc.getMaster();
		WTPrincipal currentUser = null;
		try {
			currentUser = SessionHelper.manager.getPrincipal();
			SessionHelper.manager.setAdministrator();
			EPMDocumentMasterIdentity newIdentity = EPMDocumentMasterIdentity
					.newEPMDocumentMasterIdentity(epmDocMaster);
			newIdentity.setName(newName);
			newIdentity.setNumber(epmDocMaster.getNumber());
			IdentityHelper.service.changeIdentity(epmDocMaster, newIdentity);
		} catch (Exception e) {
			String errorMsg = MessageFormat
					.format("Failed to rename the given EPMDocument object-[{0}] to new name-[{1}].",
							((epmDoc != null) ? epmDoc.getDisplayIdentity()
									: null), newName);
			LOGGER.error(">>>>>" + errorMsg, e);
			throw new WTException(e, errorMsg);
		} finally {
			try {
				SessionHelper.manager.setPrincipal(currentUser.getName());
			} catch (WTException e) {
				e.printStackTrace();
				}
		}
		
	}

	public static boolean isEPMDocumentExist(String strNumber){
		EPMDocumentMaster epmdocmaster = null;
        if(!StringUtils.isEmpty(strNumber)){
        	try {
        		epmdocmaster = getEPMMasterByNumber(strNumber);
			} catch (WTException e) {
				LOGGER.error(">>>>>"+e);
			}
        }
        if (epmdocmaster == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get EPMDocument master by EPMDocument number
     */
    public static EPMDocumentMaster getEPMMasterByNumber(String docNo) throws WTException {
        QuerySpec querySpec = new QuerySpec(EPMDocumentMaster.class);
        docNo = docNo.toUpperCase();
        WhereExpression searchCondition = new SearchCondition(EPMDocumentMaster.class, EPMDocumentMaster.NUMBER, SearchCondition.EQUAL, docNo, false);
        querySpec.appendWhere(searchCondition,new int[] { 0 });
        QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec)querySpec);
        while (queryResult.hasMoreElements()) {
        	EPMDocumentMaster epmDocumentMaster = (EPMDocumentMaster) queryResult.nextElement();
            return epmDocumentMaster;
        }
        return null;
    }

	/**
	 * get the URL to downloads the latest version (revision & iteration) of a
	 * document primary content by number
	 * 
	 * @param type
	 * @param No
	 * @return
	 * @throws WTException
	 */

	public static String getPrimContDownloadURLByNumber(String number)
			throws WTException, java.beans.PropertyVetoException {

		String url = null;
		try {
			QuerySpec qs = new QuerySpec(WTDocumentMaster.class);
			qs.appendWhere(new SearchCondition(WTDocumentMaster.class,
					WTDocumentMaster.NUMBER, SearchCondition.EQUAL, number));

			QueryResult qr = PersistenceHelper.manager.find(qs);
			if (qr.hasMoreElements()) {
				WTDocumentMaster docm = (WTDocumentMaster) qr.nextElement();
				QueryResult qrLatest = ConfigHelper.service
						.filteredIterationsOf(docm, new LatestConfigSpec());
				if (qrLatest.hasMoreElements()) {
					WTDocument doc = (WTDocument) qrLatest.nextElement();
					ContentHolder ch = (ContentHolder) ContentHelper.service
							.getContents(doc);
					ApplicationData ad = (ApplicationData) ContentHelper
							.getPrimary((FormatContentHolder) ch);
					if (ad != null) {
						url = ContentHelper.getDownloadURL(ch, ad, false)
								.toString();
						url = "<a href='" + url + " target='_blank'>"
								+ ad.getFileName() + "</a>";
						LOGGER.debug("doc.getIdentity(): " + doc.getIdentity());
						LOGGER.debug("url: " + url);
					}
				}
			}

			return url;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new WTException(e, e.getMessage());
		}
	}

	/**
	 * Deal with the special characters of fuzzy query value.(SQL Skills)
	 * 
	 * @param sqlStr
	 * @return
	 */
	public static String sqlLikeValueEncode(String value) {
		if (value == null || "".equals(value)) {
			return value;
		}

		// Add "*" or "%" before and after processing
		if (value.startsWith("*") || value.startsWith("%")) {
			value = "%" + value.substring(1);
		}
		if (value.endsWith("*") || value.endsWith("%")) {
			value = value.substring(0, value.length() - 1) + "%";
		}
		if (value.endsWith("*") || value.endsWith("%")) {
			value = value.substring(0, value.length() - 1) + "%";
		}

		// Deal with the special characters
		if (value.contains("[")) {

			value = value.replace("[", "\\[");
			value = "'" + value + "'" + " escape '\\'";
		} else if (value.contains("_")) {

			value = value.replace("_", "\\_");
			value = "'" + value + "'" + " escape '\\'";
		} else {
			value = "'" + value + "'";
		}

		return value;
	}

	/**
	 * Deal with the special characters of fuzzy query value. (Using the
	 * advanced query in Windchill)
	 * 
	 * @param sqlStr
	 * @return
	 */
	public static String queryLikeValueFormat(String value) {
		if (value == null || "".equals(value)) {
			return value;
		}

		// Only add "*" or "%" to begin or end.
		if (value.startsWith("*") || value.startsWith("%")) {
			value = "%" + value.substring(1);
		}
		if (value.endsWith("*") || value.endsWith("%")) {
			value = value.substring(0, value.length() - 1) + "%";
		}

		return value;
	}

	/**
	 * Get WTDocument By Type and number
	 * 
	 * @param type
	 * @param No
	 * @return
	 * @throws WTException
	 */
	public static List<WTDocument> getDocsByTypeAndNumberSuffix(String type,
			String number) throws WTException {
		List<WTDocument> result = new ArrayList<WTDocument>();
		try {
			QuerySpec queryspec = new QuerySpec(WTDocument.class);
			TypeDefinitionReference clientType = ClientTypedUtility
					.getTypeDefinitionReference(type);
			SearchCondition searchCondition = new SearchCondition(
					WTDocument.class, Typed.TYPE_DEFINITION_REFERENCE + "."
							+ TypeDefinitionReference.KEY + "."
							+ TypeDefinitionForeignKey.BRANCH_ID,
					SearchCondition.EQUAL, clientType.getKey().getBranchId());
			queryspec.appendWhere(searchCondition, new int[] { 0 });
			queryspec.appendAnd();
			searchCondition = new SearchCondition(WTDocument.class,
					WTDocument.NUMBER, SearchCondition.LIKE,
					DocUtil.queryLikeValueFormat(number));
			queryspec.appendWhere(searchCondition);
			LOGGER.debug("sql ====" + queryspec.toString());
			QueryResult queryresult = PersistenceServerHelper.manager
					.query(queryspec);
			queryresult = new LatestConfigSpec().process(queryresult);
			while (queryresult.hasMoreElements()) {
				Object object = queryresult.nextElement();
				if (object instanceof WTDocument) {
					WTDocument doc = (WTDocument) object;
					result.add(doc);
				}
			}
			return result;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new WTException(e, e.getMessage());
		}
	}

	public static WTDocument getDocumentByNumberSuffix(String name,
			String numberSuffix) throws WTException {
		WTDocument document = null;
		QuerySpec querySpec = new QuerySpec(WTDocument.class);
		querySpec.setAdvancedQueryEnabled(true);

		SearchCondition searchCondi = new SearchCondition(WTDocument.class,
				WTDocument.NAME, SearchCondition.EQUAL, name);
		querySpec.appendWhere(searchCondi, new int[] { 0 });

		querySpec.appendAnd();

		searchCondi = new SearchCondition(WTDocument.class, WTDocument.NUMBER,
				SearchCondition.LIKE, DocUtil.queryLikeValueFormat(numberSuffix));
		querySpec.appendWhere(searchCondi, new int[] { 0 });

		QueryResult qr = PersistenceHelper.manager
				.find((StatementSpec) querySpec);
		LatestConfigSpec lcs = new LatestConfigSpec();
		qr = lcs.process(qr);
		if (qr.hasMoreElements()) {
			document = (WTDocument) qr.nextElement();
		}
		return document;
	}

	public static List<WTDocument> getDocumentByNamePrefixByState(
			String namePrefix, String state) throws WTException {
		List<WTDocument> list = new ArrayList<WTDocument>();
		QuerySpec querySpec = new QuerySpec(WTDocument.class);
		querySpec.setAdvancedQueryEnabled(true);

		SearchCondition searchCondi = new SearchCondition(WTDocument.class,
				WTDocument.NAME, SearchCondition.LIKE,
				DocUtil.queryLikeValueFormat(namePrefix));
		querySpec.appendWhere(searchCondi, new int[] { 0 });

		if (state != null && !"".equals(state)) {
			querySpec.appendAnd();

			searchCondi = new SearchCondition(WTDocument.class,
					WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state);
			querySpec.appendWhere(searchCondi, new int[] { 0 });
		}

		QueryResult qr = PersistenceHelper.manager
				.find((StatementSpec) querySpec);
		LatestConfigSpec lcs = new LatestConfigSpec();
		qr = lcs.process(qr);
		while (qr.hasMoreElements()) {
			WTDocument document = (WTDocument) qr.nextElement();
			list.add(document);
		}
		return list;
	}

	/**
	 * get latest WTDocuments by name prefix
	 * 
	 * @param prefix
	 *            document prefix
	 * @return QueryResult
	 * @throws WTException
	 */
	public static List<WTDocument> getDocumentByNamePrefix(String prefix)
			throws WTException {
		if (prefix == null || prefix.equalsIgnoreCase("")) {
			LOGGER.error(" prefix is null");
			return null;
		}
		List<WTDocument> list = new ArrayList<WTDocument>();
		LOGGER.debug("param doc prefix is " + prefix);
		QuerySpec querySpec = new QuerySpec(WTDocument.class);
		querySpec.setAdvancedQueryEnabled(true);
		int[] index = { 0 };
		WhereExpression sc = new SearchCondition(WTDocument.class,
				WTDocument.NAME, SearchCondition.LIKE,
				DocUtil.queryLikeValueFormat(prefix));
		querySpec.appendWhere(sc, index);
		QueryResult qr = PersistenceHelper.manager
				.find((StatementSpec) querySpec);
		// get latest version document
		LatestConfigSpec lcs = new LatestConfigSpec();
		qr = lcs.process(qr);
		while (qr.hasMoreElements()) {
			WTDocument document = (WTDocument) qr.nextElement();
			list.add(document);
		}
		return list;
	}

	/**
	 * get latest WTDocuments by number suffix
	 * 
	 * @param prefix
	 *            document prefix
	 * @return QueryResult
	 * @throws WTException
	 */
	public static List<WTDocument> getDocumentByNumberSuffix(String suffix)
			throws WTException {
		if (suffix == null || suffix.equalsIgnoreCase("")) {
			LOGGER.error(" suffix is null");
			return null;
		}
		List<WTDocument> list = new ArrayList<WTDocument>();
		LOGGER.debug("param doc suffix is " + suffix);
		QuerySpec querySpec = new QuerySpec(WTDocument.class);
		querySpec.setAdvancedQueryEnabled(true);
		int[] index = { 0 };
		WhereExpression sc = new SearchCondition(WTDocument.class,
				WTDocument.NUMBER, SearchCondition.LIKE,
				DocUtil.queryLikeValueFormat(suffix) + suffix.toUpperCase());
		querySpec.appendWhere(sc, index);
		ClassAttribute clsAttr = new ClassAttribute(WTDocument.class,
				WTDocument.MODIFY_TIMESTAMP);
		OrderBy order = new OrderBy((OrderByExpression) clsAttr, true);
		querySpec.appendOrderBy(order, new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager
				.find((StatementSpec) querySpec);
		// get latest version document
		LatestConfigSpec lcs = new LatestConfigSpec();
		qr = lcs.process(qr);
		while (qr.hasMoreElements()) {
			WTDocument document = (WTDocument) qr.nextElement();
			LOGGER.debug("document number:" + document.getNumber() + "  "
					+ document);
			list.add(document);
		}
		return list;
	}

	/**
	 * Get WTDocument by WorkItem
	 * 
	 * @param workitem
	 * @return
	 */
	public static WTDocument getDocumentByWorkItem(WorkItem workitem) {
		WTDocument document = null;
		Persistable persistable = workitem.getPrimaryBusinessObject()
				.getObject();
		if (persistable instanceof WTDocument) {
			document = (WTDocument) persistable;
		}
		return document;
	}

	/**
	 * @param wtpart
	 *            WTPart
	 * @param epmdocument
	 *            EPMDocument
	 * @return EPMDescribeLink
	 * @throws WTException
	 */
	public static EPMDescribeLink getEPMDescribeLink(WTPart wtpart,
			EPMDocument epmdocument) throws WTException {
		QueryResult queryresult = PersistenceHelper.manager.find(
				wt.epm.structure.EPMDescribeLink.class, wtpart,
				EPMDescribeLink.DESCRIBES_ROLE, epmdocument);
		if (queryresult == null || queryresult.size() == 0) {
			return null;
		} else {
			EPMDescribeLink epmdescribelink = (EPMDescribeLink) queryresult
					.nextElement();
			return epmdescribelink;
		}
	}

	/**
	 * associateWTPartAndEPMDocument
	 * 
	 * @param wtpart
	 *            WTPart
	 * @param epmdocument
	 *            EPMDocument
	 * @return EPMDescribeLink
	 * @throws WTException
	 *             Windchill exception
	 */
	public static EPMDescribeLink associateWTPartAndEPMDocument(WTPart wtpart,
			EPMDocument epmdocument) throws WTException {
		EPMDescribeLink epmdescribelinkOld = getEPMDescribeLink(wtpart,
				epmdocument);
		if (epmdescribelinkOld != null) {
			LOGGER.info("the link has alseardy exist,no need to create it again");
			return epmdescribelinkOld;
		} else {
			LOGGER.info("no link between the wtpart and wtdocument,will create");
			EPMDescribeLink epmdescribelink1 = EPMDescribeLink
					.newEPMDescribeLink(wtpart, epmdocument);
			PersistenceServerHelper.manager.insert(epmdescribelink1);
			epmdescribelink1 = (EPMDescribeLink) PersistenceHelper.manager
					.refresh(epmdescribelink1);
			EPMDescribeLink epmdescribelink = getEPMDescribeLink(wtpart,
					epmdocument);
			if (epmdescribelink == null) {
				LOGGER.info("create wtpart associate EPMDoc fail");
				return null;
			} else {
				LOGGER.info("create wtpart associate EPMDoc successful");
				return epmdescribelink;
			}
		}
	}

	/**
	 * getExcelFromAttachContent
	 * 
	 * @param WTDocument
	 * @param String
	 * @return
	 * @throws WTException
	 */
	public static List<ApplicationData> getAttachContentList(
			WTDocument wtdocument, String fileName) throws WTException,
			PropertyVetoException {
		ApplicationData applicationData = null;
		List<ApplicationData> applist = new ArrayList<ApplicationData>();
		try {
			ContentHolder contentHolder = ContentHelper.service
					.getContents((ContentHolder) wtdocument);
			List<?> vContentList = ContentHelper.getContentList(contentHolder);
			int iSize = vContentList.size();
			for (int j = 0; j < iSize; j++) {
				ContentItem contentitem = (ContentItem) vContentList.get(j);
				if (contentitem instanceof ApplicationData) {
					applicationData = ((ApplicationData) contentitem);
					applist.add(applicationData);
				}
			}
		} catch (PropertyVetoException e) {
			LOGGER.error(e.getLocalizedMessage());
			throw new WTException(e);
		}
		return applist;
	}

	/**
	 * @param typeId
	 * @param state
	 * @param equalFlag
	 * @return
	 * @throws WTException
	 * @throws Exception
	 */
	public static List<WTDocument> getLatestDocumentListByTypeAndState(
			String typeId, String state, boolean equalFlag) throws WTException,
			Exception {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (List<WTDocument>) RemoteMethodServer.getDefault()
						.invoke("getLatestDocumentListByTypeAndState",
								DocUtil.class.getName(), null,
								new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			List<WTDocument> list = new ArrayList<WTDocument>();
			int[] zero = { 0 };
			QuerySpec qs = new QuerySpec(WTDocument.class);

			TypeDefinitionReference tdr;
			try {
				tdr = ClientTypedUtility.getTypeDefinitionReference(typeId);
			} catch (RemoteException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			}
			qs.appendWhere(new SearchCondition(WTDocument.class,
					WTDocument.TYPE_DEFINITION_REFERENCE + "."
							+ TypeDefinitionReference.KEY + "."
							+ TypeDefinitionForeignKey.BRANCH_ID,
					SearchCondition.EQUAL, tdr.getKey().getBranchId()), zero);

			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}
			if (equalFlag) {
				qs.appendWhere(new SearchCondition(WTDocument.class,
						WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL,
						state), zero);
			} else {
				qs.appendWhere(new SearchCondition(WTDocument.class,
						WTDocument.LIFE_CYCLE_STATE, SearchCondition.NOT_EQUAL,
						state), zero);
			}

			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}
			qs.appendWhere(new SearchCondition(WTDocument.class,
					WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE), zero);

			qs = new LatestConfigSpec().appendSearchCriteria(qs);

			QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
			qr = new LatestConfigSpec().process(qr);
			while (qr.hasMoreElements()) {
				WTDocument doc = (WTDocument) qr.nextElement();
				list.add(doc);
			}

			return list;
		}
	}

	/**
	 * get latest document list by doctype
	 * 
	 * @param typeId
	 * @return
	 * @throws WTException
	 * @throws Exception
	 */
	public static List<WTDocument> getLatestDocumentListByType(String typeId)
			throws WTException, Exception {
		List<WTDocument> list = new ArrayList<WTDocument>();
		int[] zero = { 0 };
		QuerySpec qs = new QuerySpec(WTDocument.class);
		TypeDefinitionReference tdr;
		try {
			tdr = ClientTypedUtility.getTypeDefinitionReference(typeId);
		} catch (RemoteException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new WTException(e, e.getLocalizedMessage());
		}
		qs.appendWhere(new SearchCondition(WTDocument.class,
				WTDocument.TYPE_DEFINITION_REFERENCE + "."
						+ TypeDefinitionReference.KEY + "."
						+ TypeDefinitionForeignKey.BRANCH_ID,
				SearchCondition.EQUAL, tdr.getKey().getBranchId()), zero);
		if (qs.getConditionCount() > 0) {
			qs.appendAnd();
		}
		qs.appendWhere(new SearchCondition(WTDocument.class,
				WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE), zero);
		qs = new LatestConfigSpec().appendSearchCriteria(qs);
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		qr = new LatestConfigSpec().process(qr);
		while (qr.hasMoreElements()) {
			WTDocument doc = (WTDocument) qr.nextElement();
			list.add(doc);
		}
		return list;
	}

	/**
	 * Query latest WTDocuments obj by IBA and container
	 * 
	 * @param prefix
	 *            document prefix
	 * @return QueryResult
	 * @throws WTException
	 */
	public static QueryResult getDocumentByIBAAndContainer(WTContainer con,
			String ibaName, String ibaVaue) throws WTException {
		if (con == null || ibaName == null || "".equalsIgnoreCase(ibaName)
				|| ibaVaue == null || "".equalsIgnoreCase(ibaVaue)) {
			LOGGER.debug("getDocumentByNumberPrefixAndContainer>>>>>>>>>>>>>>> container="
					+ con
					+ "    ibaName="
					+ ibaName
					+ "    ibaVaue=  "
					+ ibaVaue);
			return null;
		}
		QuerySpec querySpec = new QuerySpec();
		int docIndex = querySpec.appendClassList(WTDocument.class, true);
		int ibaStringValueIndex = querySpec.appendClassList(StringValue.class,
				false);
		int ibaStringDefinitionIndex = querySpec.appendClassList(
				StringDefinition.class, false);
		int pdmIndex = 1;
		querySpec.setAdvancedQueryEnabled(true);
		WhereExpression pdm = null;
		if (con instanceof WTLibrary) {
			pdmIndex = querySpec.appendClassList(WTLibrary.class, false);

		} else if (con instanceof PDMLinkProduct) {
			pdmIndex = querySpec.appendClassList(PDMLinkProduct.class, false);

		}
		if (con instanceof WTLibrary) {

			pdm = new SearchCondition(WTLibrary.class, WTLibrary.NAME,
					SearchCondition.EQUAL, con.getName());
			SearchCondition scon = new SearchCondition(WTDocument.class,
					"containerReference.key.id", WTLibrary.class,
					"thePersistInfo.theObjectIdentifier.id");
			querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex });

		} else if (con instanceof PDMLinkProduct) {

			pdm = new SearchCondition(PDMLinkProduct.class,
					PDMLinkProduct.NAME, SearchCondition.EQUAL, con.getName());
			SearchCondition scon = new SearchCondition(WTDocument.class,
					"containerReference.key.id", PDMLinkProduct.class,
					"thePersistInfo.theObjectIdentifier.id");
			querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex });
		}
		if (pdm != null) {

			querySpec.appendWhere(pdm, new int[] { pdmIndex });
			querySpec.appendAnd();
		}

		// IBA Name condition

		ibaName = ibaName.toUpperCase();
		ClassAttribute caIbaName = new ClassAttribute(StringDefinition.class,
				StringDefinition.NAME);
		SearchCondition scStringDefinitionName = new SearchCondition(
				SQLFunction.newSQLFunction(SQLFunction.UPPER, caIbaName),
				SearchCondition.EQUAL, new ConstantExpression(
						(Object) ibaName.toUpperCase()));
		querySpec.appendWhere(scStringDefinitionName,
				new int[] { ibaStringDefinitionIndex });
		querySpec.appendAnd();
		// IBA b condition

		ClassAttribute caIbaValue = new ClassAttribute(StringValue.class,
				StringValue.VALUE2);
		SearchCondition scStringValue = new SearchCondition(caIbaValue,
				SearchCondition.EQUAL, new ConstantExpression((Object) ibaVaue));
		querySpec.appendWhere(scStringValue, new int[] { ibaStringValueIndex });
		querySpec.appendAnd();
		// StringValue and StringDefinition connection condition

		SearchCondition scJoinStringValueStringDefinition = new SearchCondition(
				StringValue.class, "definitionReference.key.id",
				StringDefinition.class, WTAttributeNameIfc.ID_NAME);
		querySpec.appendWhere(scJoinStringValueStringDefinition, new int[] {
				ibaStringValueIndex, ibaStringDefinitionIndex });
		querySpec.appendAnd();
		// Document and StringValue condition
		SearchCondition scStringValueDoc = new SearchCondition(
				StringValue.class, "theIBAHolderReference.key.id",
				WTDocument.class, WTAttributeNameIfc.ID_NAME);
		querySpec.appendWhere(scStringValueDoc, new int[] {
				ibaStringValueIndex, docIndex });
		querySpec.appendAnd();

		querySpec.appendWhere(new SearchCondition(WTDocument.class,
				WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE),
				new int[] { docIndex });
		querySpec.appendAnd();

		querySpec.appendWhere(new SearchCondition(WTDocument.class,
				WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL,
				State.RELEASED), new int[] { docIndex });

		LOGGER.debug("QuerySpec=" + querySpec);
		QueryResult qr = PersistenceHelper.manager
				.find((StatementSpec) querySpec);
		return qr;
	}

	/**
	 * Query latest WTDocuments by number prefix and WTContainer
	 * 
	 * @param WTContainer
	 *            con, String prefix
	 * @return QueryResult
	 * @throws WTException
	 */
	public static QueryResult getDocumentByNumberPrefixAndContainer(
			WTContainer con, String prefix) throws WTException {
		if (prefix == null || prefix.equalsIgnoreCase("")) {
			return null;
		}
		QuerySpec querySpec = new QuerySpec();
		int docIndex = querySpec.appendClassList(WTDocument.class, true);
		int pdmIndex = 1;
		if (con instanceof WTLibrary) {
			pdmIndex = querySpec.appendClassList(WTLibrary.class, false);

		} else if (con instanceof PDMLinkProduct) {
			pdmIndex = querySpec.appendClassList(PDMLinkProduct.class, false);

		}
		querySpec.setAdvancedQueryEnabled(true);
		WhereExpression doc = new SearchCondition(WTDocument.class,
				WTDocument.NUMBER, SearchCondition.LIKE,
				sqlLikeValueEncode(prefix));
		querySpec.appendWhere(doc, new int[] { docIndex });

		WhereExpression pdm = null;
		if (con instanceof WTLibrary) {
			querySpec.appendAnd();
			pdm = new SearchCondition(WTLibrary.class, WTLibrary.NAME,
					SearchCondition.EQUAL, con.getName());
			SearchCondition scon = new SearchCondition(WTDocument.class,
					"containerReference.key.id", WTLibrary.class,
					"thePersistInfo.theObjectIdentifier.id");
			querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex });

		} else if (con instanceof PDMLinkProduct) {
			querySpec.appendAnd();
			pdm = new SearchCondition(PDMLinkProduct.class,
					PDMLinkProduct.NAME, SearchCondition.EQUAL, con.getName());
			SearchCondition scon = new SearchCondition(WTDocument.class,
					"containerReference.key.id", PDMLinkProduct.class,
					"thePersistInfo.theObjectIdentifier.id");
			querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex });
		}
		if (pdm != null) {
			querySpec.appendAnd();
			querySpec.appendWhere(pdm, new int[] { pdmIndex });
		}

		querySpec.appendAnd();
		querySpec.appendWhere(new SearchCondition(WTDocument.class,
				WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE),
				new int[] { docIndex });

		querySpec.appendAnd();
		querySpec.appendWhere(new SearchCondition(WTDocument.class,
				WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL,
				State.RELEASED), new int[] { docIndex });

		LOGGER.debug("QuerySpec=" + querySpec);
		QueryResult qr = PersistenceHelper.manager
				.find((StatementSpec) querySpec);
		return qr;
	}

	/**
	 * get Attribute Value Type
	 * 
	 * @param attributedefdefaultview
	 *            StringValue,IntegerValue,FloatValue
	 * @return
	 * @throws WTException
	 */
	public static Class<?> getAttributeValueClass(
			AttributeDefDefaultView attributedefdefaultview) throws WTException {
		if (attributedefdefaultview instanceof StringDefView) {
			return StringValue.class;
		}
		if (attributedefdefaultview instanceof IntegerDefView) {
			return IntegerValue.class;
		}
		if (attributedefdefaultview instanceof FloatDefView) {
			return FloatValue.class;
		} else {
			throw new UnsupportedOperationException("Attribute "
					+ attributedefdefaultview.getName()
					+ " is not of type string, integer or float");
		}
	}

	/**
	 * decide whether a string contains ","
	 * 
	 * @param attributeValue
	 * @return
	 */
	private static boolean isList(String attributeValue) {
		return attributeValue.indexOf(",") != -1;
	}

	/**
	 * decide whether a string contains "/"
	 * 
	 * @param attributeValue
	 * @return
	 */
	private static boolean isRange(String attributeValue) {
		return attributeValue.indexOf("/") != -1;
	}

	/**
	 * AttributeDefDefaultView is FloatDefView
	 * 
	 * @param class1
	 * @param value
	 * @param attributeValue
	 * @return
	 * @throws WTException
	 */
	public static SearchCondition floatWhere(Class<?> class1, String value,
			String attributeValue) throws WTException {
		Object obj = null;
		String sign = null;
		if (isRange(attributeValue)) {
			StringTokenizer stringtokenizer = new StringTokenizer(
					attributeValue, "/");
			obj = new RangeExpression(new AttributeRange(
					Float.parseFloat(stringtokenizer.nextToken()),
					Float.parseFloat(stringtokenizer.nextToken())));
			sign = "BETWEEN";
		} else {
			Float fvalue = Float.valueOf(attributeValue);
			obj = new ConstantExpression(fvalue);
			sign = "=";
		}
		return new SearchCondition(new ClassAttribute(class1, value), sign,
				((RelationalExpression) (obj)));
	}

	/**
	 * AttributeDefDefaultView is IntegerDefView
	 * 
	 * @param class1
	 * @param value
	 * @param attributeValue
	 * @return
	 * @throws WTException
	 */
	public static SearchCondition integerWhere(Class<?> class1, String value,
			String attributeValue) throws WTException {
		Object obj = null;
		String sign = null;
		if (isList(attributeValue)) {
			StringTokenizer stringtokenizer = new StringTokenizer(
					attributeValue, ",");
			long al[] = new long[stringtokenizer.countTokens()];
			for (int i = 0; i < al.length; i++) {
				al[i] = Long.parseLong(stringtokenizer.nextToken());
			}
			obj = new ArrayExpression(al);
			sign = "IN";
		} else if (isRange(attributeValue)) {
			StringTokenizer stringtokenizer1 = new StringTokenizer(
					attributeValue, "/");
			obj = new RangeExpression(new AttributeRange(
					Long.parseLong(stringtokenizer1.nextToken()),
					Long.parseLong(stringtokenizer1.nextToken())));
			sign = "BETWEEN";
		} else {
			Integer ivalue = Integer.valueOf(sign);
			obj = new ConstantExpression(ivalue);
			sign = "=";
		}
		return new SearchCondition(new ClassAttribute(class1, value), sign,
				((RelationalExpression) (obj)));
	}

	/**
	 * replace "*" to "%" in the string
	 * 
	 * @param attributeValue
	 */
	private static String convertWildCards(String attributeValue) {
		return attributeValue.replace('*', '%');
	}

	/**
	 * decide whether a string contains "*"
	 * 
	 * @param attributeValue
	 * @return
	 */
	private static boolean hasWildCards(String attributeValue) {
		return attributeValue.indexOf('*') != -1;
	}

	/**
	 * AttributeDefDefaultView is StringDefView
	 * 
	 * @param clazz
	 *            StringValue class
	 * @param attributeField
	 * @param attributeValue
	 * @return
	 * @throws WTException
	 */
	public static SearchCondition stringWhere(Class<?> clazz,
			String attributeField, String attributeValue) throws WTException {
		Object obj = null;
		String sign = null;
		if (isList(attributeValue)) {
			StringTokenizer stringtokenizer = new StringTokenizer(
					attributeValue, ",");
			String as[] = new String[stringtokenizer.countTokens()];
			for (int i = 0; i < as.length; i++) {
				as[i] = stringtokenizer.nextToken();
			}
			obj = new ArrayExpression(as);
			sign = "IN";
		} else {
			obj = new ConstantExpression(
					(Object) convertWildCards(attributeValue));
			sign = hasWildCards(attributeValue) ? " LIKE " : "=";
		}
		return new SearchCondition(new ClassAttribute(clazz, attributeField),
				sign, ((RelationalExpression) (obj)));
	}

	/**
	 * Generate WhereExpression According to AttributeDefDefaultView and
	 * AttributeValue, then append WhereExpression to QuerySpec
	 * 
	 * @param spec
	 *            QuerySpec
	 * @param attributeDefDefaultView
	 * @param attributeValue
	 * @param index
	 *            class index
	 * @throws WTException
	 */
	private static void generateWhere(QuerySpec spec,
			AttributeDefDefaultView attributeDefDefaultView,
			String attributeValue, int index) throws WTException {
		if (attributeDefDefaultView instanceof IntegerDefView) {
			spec.appendWhere(
					integerWhere(
							getAttributeValueClass(attributeDefDefaultView),
							"value", attributeValue), new int[] { index });
		} else if (attributeDefDefaultView instanceof FloatDefView) {
			spec.appendWhere(
					floatWhere(getAttributeValueClass(attributeDefDefaultView),
							"value", attributeValue), new int[] { index });
		} else {
			spec.appendWhere(
					stringWhere(
							getAttributeValueClass(attributeDefDefaultView),
							"value", attributeValue.toUpperCase()),
					new int[] { index });
			spec.appendAnd();
			spec.appendWhere(
					stringWhere(
							getAttributeValueClass(attributeDefDefaultView),
							"value2", attributeValue), new int[] { index });
		}
	}

	/**
	 * 
	 * @param IBAName
	 * @return
	 * @throws WTException
	 */
	public static String getIBAHierarchyID(String ibaName) throws WTException {
		String ibaObjectId = null;
		try {
			QuerySpec queryspec = new QuerySpec(
					wt.iba.definition.AbstractAttributeDefinition.class);
			queryspec.appendWhere(new SearchCondition(
					wt.iba.definition.AbstractAttributeDefinition.class,
					"name", "=", ibaName), new int[] { 0 });
			QueryResult queryresult = PersistenceHelper.manager
					.find((StatementSpec) queryspec);
			if (queryresult.hasMoreElements()) {
				AbstractAttributeDefinition attributeDefinition = (AbstractAttributeDefinition) queryresult
						.nextElement();
				ibaObjectId = attributeDefinition.getHierarchyID();
			}
		} catch (WTException e) {
			LOGGER.error(e);
			throw new WTException(e);
		}
		return ibaObjectId;
	}

	/**
	 * lookupAttributeDefinition
	 * 
	 * @param attributeName
	 * @return
	 * @throws WTException
	 * @throws Exception
	 */
	 
	   public static AttributeDefDefaultView lookupAttributeDefinition(String  attributeName) throws WTException, Exception { 
		   try {
			    return IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(attributeName); 
			    } catch(RemoteException remoteexception) { 
			    	throw new WTException(remoteexception); 
			    } 
		   }
	 
	/**
	 * add attribute iba
	 * 
	 * @param queryspec
	 * @param attributeName
	 * @param attributeValue
	 * @throws Exception
	 */
	
	   public static void appendUserAttribute(QuerySpec queryspec, String
	   attributeName, String attributeValue) throws Exception {
	   AttributeDefDefaultView attributedefdefaultview =
	   lookupAttributeDefinition(attributeName); Class<?> classA =
	   queryspec.getClassAt(0); if (attributedefdefaultview == null) { throw new
	   IllegalArgumentException("Attribute " + attributeName +
	   " does not exist"); } else { Class<?> classB =
	   getAttributeValueClass(attributedefdefaultview); int index =
	   queryspec.appendClassList(classB, false); if
	   (queryspec.getConditionCount() > 0) { queryspec.appendAnd(); }
	   SearchCondition searchcondition = new SearchCondition(new
	   ClassAttribute(classA, "thePersistInfo.theObjectIdentifier.id"), "=", new
	   ClassAttribute(classB, "theIBAHolderReference.key.id"));
	   queryspec.appendWhere(searchcondition, new int[] {
	   queryspec.getFromClause().getPosition(classA), index });
	   queryspec.appendAnd(); queryspec.appendWhere(new SearchCondition(classB,
	   "definitionReference.hierarchyID", "=",
	   getIBAHierarchyID(attributeName)), new int[] { index });
	   queryspec.appendAnd(); generateWhere(queryspec, attributedefdefaultview,
	   attributeValue, index); return; } }
	 /**
	 * get latest WTDocuments by IBA and container
	 * 
	 * @param prefix
	 *            document prefix
	 * @return QueryResult
	 * @throws Exception
	 */
	
	  public static List<WTDocument> getDocumentByManyIBAAndContainer(WTContainer con, Map<String, String>
	   ibaMap, String softName) throws Exception { List<WTDocument> result = new
		   ArrayList<WTDocument>();
		  
		   if (con == null && ibaMap.isEmpty()) {
		   LOGGER.debug("getDocumentByManyIBAAndContainer>>>>>>>>>>>>>>> container="
		   + con + "    ibaMap is null"); return null; } QuerySpec querySpec = new
		   QuerySpec(); int docIndex = querySpec.appendClassList(WTDocument.class,
		   true);
		   
		   int pdmIndex = 1; querySpec.setAdvancedQueryEnabled(true);
		   WhereExpression pdm = null; if (con instanceof WTLibrary) { pdmIndex =
		   querySpec.appendClassList(WTLibrary.class, false);
		   
		   } else if (con instanceof PDMLinkProduct) { pdmIndex =
		   querySpec.appendClassList(PDMLinkProduct.class, false);
		  
		   } if (con instanceof WTLibrary) {
		   
		   pdm = new SearchCondition(WTLibrary.class, WTLibrary.NAME,
		   SearchCondition.EQUAL, con.getName()); SearchCondition scon = new
		   SearchCondition(WTDocument.class, "containerReference.key.id",
		   WTLibrary.class, "thePersistInfo.theObjectIdentifier.id");
		   querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex });
		  
		  } else if (con instanceof PDMLinkProduct) {
		  
		   pdm = new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME,
		   SearchCondition.EQUAL, con.getName()); SearchCondition scon = new
		   SearchCondition(WTDocument.class, "containerReference.key.id",
		   PDMLinkProduct.class, "thePersistInfo.theObjectIdentifier.id");
		   querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex }); } if (pdm
		   != null) { querySpec.appendAnd(); querySpec.appendWhere(pdm, new int[] {
		   pdmIndex }); }
		  
		    // IBA Name condition 
		   Set<String> set = ibaMap.keySet();
		  
		  for (String ibaName : set) { String ibaVaue = ibaMap.get(ibaName);// AA  aa Aa aA 
		   LOGGER.debug(" numericID=" + IBAUtility.numericID(ibaName));
		   appendUserAttribute(querySpec, ibaName, ibaVaue); }
		   TypeDefinitionReference tdr;
		   tdr=ClientTypedUtility.getTypeDefinitionReference(softName); if (tdr ==
		   null){
		   LOGGER.debug("getDocumentByManyIBAAndContainer>>>>>>>>>>>>>>> softName="
		   + softName + " The softName is error"); return null; }else {
		   querySpec.appendAnd(); querySpec.appendWhere(new
		   SearchCondition(WTDocument.class, WTDocument.TYPE_DEFINITION_REFERENCE +
		   "." + TypeDefinitionReference.KEY + "." +
		   TypeDefinitionForeignKey.BRANCH_ID, SearchCondition.EQUAL,
		   tdr.getKey().getBranchId()), new int[] { docIndex });
		   querySpec.appendAnd(); querySpec.appendWhere(new
		   SearchCondition(WTDocument.class, WTDocument.LIFE_CYCLE_STATE,
		   SearchCondition.EQUAL, State.RELEASED), new int[] { docIndex });
		   
		   querySpec.appendAnd(); querySpec.appendWhere(new
		   SearchCondition(WTDocument.class, WTDocument.LATEST_ITERATION,
		   SearchCondition.IS_TRUE), new int[] { docIndex }); new
		   VersionedOrderByPrimitive().appendOrderBy(querySpec, 0, true); new
		   IteratedOrderByPrimitive().appendOrderBy(querySpec, 0, true);
		   LOGGER.debug("QuerySpec=" + querySpec); QueryResult qr =
		   PersistenceHelper.manager.find((StatementSpec) querySpec); while
		   (qr.hasMoreElements()) { Object object = qr.nextElement(); if (object
		   instanceof WTDocument) { WTDocument doc = (WTDocument) object;
		   result.add(doc); 
		   }
		   } 
		   return result;
		   } 
		   
	  }
	 

	/**
	 * renNumber the wtdocument
	 * 
	 * @param wtdocument
	 * @param String
	 *            newNumber
	 * @throws WTException
	 */
	public static void reNumberWTDocument(WTDocument document, String newNumber)
			throws WTException, WTPropertyVetoException {

		Identified aIdentified = (Identified) document.getMaster();
		WTDocumentMasterIdentity aWTDocumentMasterIdentity;
		aWTDocumentMasterIdentity = (WTDocumentMasterIdentity) aIdentified
				.getIdentificationObject();
		aWTDocumentMasterIdentity.setNumber(newNumber);
		IdentityHelper.service.changeIdentity(aIdentified,
				aWTDocumentMasterIdentity);
	}

	/**
	 * To create the dependency Link  between document and document.
	 * 
	 * @param docA
	 * @param docB
	 * @throws WTException
	 */
	public static WTDocumentDependencyLink associateDocToDoc(
			WTDocument docRoleA, WTDocument docRoleB) throws WTException {
		WTDocumentDependencyLink result = null;
		try {
			if (!RemoteMethodServer.ServerFlag) {
				try {
					return (WTDocumentDependencyLink) RemoteMethodServer
							.getDefault().invoke(
									"associateDocToDoc",
									DocUtil.class.getName(),
									null,
									new Class[] { WTDocument.class,
											WTDocument.class },
									new Object[] { docRoleA, docRoleB });
				} catch (java.rmi.RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				boolean enforce = SessionServerHelper.manager
						.setAccessEnforced(false);
				try {
					QueryResult queryresult = PersistenceHelper.manager.find(
							WTDocumentDependencyLink.class, docRoleA,
							WTDocumentDependencyLink.DESCRIBES_ROLE, docRoleB);
					if (queryresult == null || queryresult.size() == 0) {
						WTDocumentDependencyLink wtdocdependencylink = WTDocumentDependencyLink
								.newWTDocumentDependencyLink(docRoleA, docRoleB);
						PersistenceServerHelper.manager
								.insert(wtdocdependencylink);
						wtdocdependencylink = (WTDocumentDependencyLink) PersistenceHelper.manager
								.refresh(wtdocdependencylink);
						result = wtdocdependencylink;
					} else {
						return (WTDocumentDependencyLink) queryresult
								.nextElement();
					}
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
		} catch (InvocationTargetException e) {
		} catch (RemoteException e) {
		}
		return result;
	}

	/**
	 * To create the Usage Link  between document and document.
	 * @param docA
	 * @param docB
	 * @throws WTException
	 */
	public static WTDocumentUsageLink associateDocToDocUsageLink(
			WTDocument docRoleA, WTDocumentMaster docRoleB) throws WTException {
		WTDocumentUsageLink result = null;
		try {
			if (!RemoteMethodServer.ServerFlag) {
				try {
					return (WTDocumentUsageLink) RemoteMethodServer
							.getDefault().invoke(
									"associateDocToDocUsageLink",
									DocUtil.class.getName(),
									null,
									new Class[] { WTDocument.class,
											WTDocument.class },
									new Object[] { docRoleA, docRoleB });
				} catch (java.rmi.RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				boolean enforce = SessionServerHelper.manager
						.setAccessEnforced(false);
				try {
					QueryResult queryresult = PersistenceHelper.manager.find(
							WTDocumentUsageLink.class, docRoleA,
							WTDocumentUsageLink.USES_ROLE, docRoleB
							/*WTDocumentUsageLink.ROLE_AOBJECT_ROLE,docRoleB*/);
					if (queryresult == null || queryresult.size() == 0) {
						WTDocumentUsageLink wtdocumentusagelink = WTDocumentUsageLink.newWTDocumentUsageLink(docRoleA, docRoleB);
						PersistenceServerHelper.manager.insert(wtdocumentusagelink);
						wtdocumentusagelink = (WTDocumentUsageLink) PersistenceHelper.manager.refresh(wtdocumentusagelink);
						result = wtdocumentusagelink;
					} else {
						return (WTDocumentUsageLink) queryresult.nextElement();
					}
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
		} catch (InvocationTargetException e) {
		} catch (RemoteException e) {
		}
		return result;
	}
	
   /**
	 * To determine whether already exists a dependency relationship between documents.
	 * @param docA
	 * @param docB
	 * @return
	 * @throws WTException
	 */
	public static WTDocumentDependencyLink getDocDependencyLink(WTDocument docA, WTDocument docB) throws WTException
	{
		WTDocumentDependencyLink link = null;
		
			if (!RemoteMethodServer.ServerFlag) {
				try {
					try {
						return (WTDocumentDependencyLink) RemoteMethodServer
								.getDefault().invoke(
										"getDocDependencyLink",
										DocUtil.class.getName(),
										null,
										new Class[] { WTDocument.class,
												WTDocument.class },
										new Object[] { docA, docA });
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (java.rmi.RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				} else {
					  if (docA != null && docB != null){
						QueryResult queryresult = PersistenceServerHelper.manager.query(WTDocumentDependencyLink.class, docA,
								WTDocumentDependencyLink.DESCRIBES_ROLE, docB);
						if (queryresult.hasMoreElements())
						{
							link = (WTDocumentDependencyLink) queryresult.nextElement();
							
						}
					 } else {
					 LOGGER.error("docA or docB is null");
					 
				 }
			  }
		  return link;
	  }
	
	/**
	 * remove Dependency Link
	 * 
	 * @param doc
	 * @throws WTException
	 */
	public static void removeDependencyLink(WTDocument doc) throws WTException
	{
			if (!RemoteMethodServer.ServerFlag) {
				try {
					  RemoteMethodServer.getDefault().invoke(
							"removeDependencyLink",
							DocUtil.class.getName(), null,
							new Class[] { String.class }, new Object[] {});
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					throw new WTException(e);
				}
				} else {
					
				if (doc == null)
				{
					return;
				}
				QuerySpec queryspec = new QuerySpec(WTDocumentDependencyLink.class);
				queryspec.appendWhere(
						new SearchCondition(WTDocumentDependencyLink.class, "roleAObjectRef.key", "=", PersistenceHelper.getObjectIdentifier(doc)),
						new int[] { 0 });
				QueryResult qr = PersistenceServerHelper.manager.query(queryspec);
				while (qr.hasMoreElements())
					{
						WTDocumentDependencyLink link = (WTDocumentDependencyLink) qr.nextElement();
						PersistenceServerHelper.manager.remove(link);
			
					}
			}
    }
	
	/**
	 * get all DependsOn WTDocuments
	 * 
	 * @param document
	 * @return
	 * @throws WTException
	 */
	public static List<WTDocument> getDependsOnWTDocuments(WTDocument document) throws WTException
	{
		List<WTDocument> documents = new ArrayList<WTDocument>();
		if (document == null)
		{
			return null;
		}
		QueryResult qr = WTDocumentHelper.service.getDependsOnWTDocuments(document);
		while (qr.hasMoreElements())
		{
			WTDocument doc = (WTDocument) qr.nextElement();
			documents.add(doc);
		}
		return documents;
	}
	
	  
	  /**
		 * get obj A.1
		 * 
		 * @param document
		 * @return
		 * @throws WTException
		 */
	  public static String getObjVersion(RevisionControlled obj)
			    throws WTException
			  {
			    if (obj == null)
			      throw new IllegalArgumentException();
			    if (!(obj instanceof RevisionControlled))
			    	throw new WTException("The argument is not the type of RevisionControlled");

			    String version = VersionControlHelper.getVersionIdentifier(obj).getValue
			      ();
			    String iteration = VersionControlHelper.getIterationIdentifier(obj).getValue
			      ();
			    return version + "." + iteration;
			  }

	/**
	 * 获取对象FoLder
	 * 
	 * @param document
	 * @return
	 * @throws WTException
	 */
	  public static Folder getFolder(WTContainerRef containerRef, String folderPath)
		  throws WTException
		  {
		    if ((containerRef == null) || (folderPath == null)) {
		      return null;
		    }

		    if ((folderPath.equals("/Default")) || (folderPath.equals("/Default/")))
		      return FolderHelper.service.getFolder(folderPath, containerRef);

		    Folder resFolder = null;
		    String relaFolderPath = (folderPath.startsWith("/Default")) ? folderPath.substring
		      ("/Default".length() + 1) : folderPath;
		    String basePath = "/Default";
		    String[] folders = relaFolderPath.split("/");
		    for (int i = 0; i < folders.length; ++i) {
		      String fp = folders[i];
		      basePath = basePath + "/" + fp;
		      try {
		        resFolder = FolderHelper.service.getFolder(basePath, 
		          containerRef);
		      } catch (FolderNotFoundException e) {
		        resFolder = FolderHelper.service.createSubFolder(basePath, 
		          containerRef);
		      }
		    }
		    return resFolder;
		  }
	
	  /**
		 * get document short type
		 * 
		 * @param doc
		 *            object
		 * @return doc short type
		 */
		public static String getDocumentShortType(WTDocument doc)
		{
			String strShortType = "";
			String curType = wt.type.TypedUtility.getExternalTypeIdentifier((wt.type.Typed) doc).trim();
			LOGGER.debug("Doc.getDocumentShortType curType=" + curType);
			if (curType != null && !curType.isEmpty())
			{
				strShortType = curType.substring(curType.lastIndexOf(".") + 1, curType.length());
			}
			return strShortType;
		}
	

		/**
		 * Determine whether the document is detected.
		 * 
		 * @param oid
		 * @return boolean
		 * @throws RemoteException
		 *             , InvocationTargetException,WTRuntimeException,WTException
		 */
		public static boolean isCheckOut(String oid) throws RemoteException, InvocationTargetException, WTRuntimeException, WTException
		{
			if (!RemoteMethodServer.ServerFlag)
			{
				try {
					return (Boolean) RemoteMethodServer.getDefault()
							.invoke("isCheckOut", className, null, new Class[] { String.class }, new Object[] { oid });
				} catch (java.rmi.RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
			{
				ReferenceFactory referenceFactory = new ReferenceFactory();
				WTDocument doc = (WTDocument) referenceFactory.getReference(oid).getObject();
				if (doc.isLatestIteration())
				{
					doc = (WTDocument) VersionControlHelper.service.getLatestIteration(doc, false);
				}
				if (WorkInProgressHelper.isCheckedOut(doc))
				{
					LOGGER.debug(">>>>>>>>>>>> The doc was checked out.");
					return true;
				}
			}
			LOGGER.debug(">>>>>>>>>>>> The doc wasn't checked out.");
			return false;

		}
		

		/**
		 * Check out WTDocument
		 * 
		 * @param WTDocument
		 * @param string
		 * @return WTDocument
		 * @throws WTException
		 * @throws VersionControlException
		 */
		public static WTDocument checkOutWTDocument(WTDocument wtdocument1, String description) throws WTException
		{
			LOGGER.debug(">>>>>>>>>>>> mcsbids.Util..checkOutWTDocument() begin");
			WTDocument wtdocument = wtdocument1;
			if (wtdocument == null)
			{
				return wtdocument;
			}
			if (!wtdocument.isLatestIteration())
			{
				wtdocument = (WTDocument) VersionControlHelper.service.getLatestIteration(wtdocument, false);
			}
			wt.org.WTPrincipal wtprincipal = SessionHelper.manager.getPrincipal();
			if (SessionServerHelper.manager.isAccessEnforced()
					&& !AccessControlHelper.manager.hasAccess(wtprincipal, wtdocument, AccessPermission.MODIFY))
			{
				throw new WTException(wtprincipal.getName() + " have no modify permission for part:" + wtdocument.getNumber() + " in "
						+ wtdocument.getContainer().getName() + "/" + wtdocument.getFolderPath());
			}
			try
			{
				LOGGER.debug(">>>>>>>>>>>> mcsbids.Util.checkOutWTDocument() document number=" + wtdocument.getNumber());
				LOGGER.debug(">>>>>>>>>>>> mcsbids.Util.checkOutWTDocument() document version=" + wtdocument.getVersionIdentifier().getValue() + "."
						+ wtdocument.getIterationIdentifier().getValue());
				LOGGER.debug(">>>>>>>>>>>>  mcsbids.Util.checkOutWTDocument() document isCheckout=" + WorkInProgressHelper.isCheckedOut(wtdocument));

				if (WorkInProgressHelper.isWorkingCopy(wtdocument))
				{
					LOGGER.debug(">>>>>>>>>>>> mcsbids.Util.checkOutWTDocument() document is checkout copy" + wtdocument.getNumber());
					return wtdocument;
				} else if (WorkInProgressHelper.isCheckedOut(wtdocument))
				{
					LOGGER.debug(">>>>>>>>>>>> mcsbids.Util.checkOutWTDocument() document is checkout" + wtdocument.getNumber());
					return (WTDocument) WorkInProgressHelper.service.workingCopyOf(wtdocument);
				} else
				{
					LOGGER.debug(">>>>>>>>>>>> mcsbids.Util.checkOutWTDocument() document is not check out,begin check out");
					LOGGER.debug("PersistenceHelper.isPersistent:" + PersistenceHelper.isPersistent(wtdocument));
					Folder folder = WorkInProgressHelper.service.getCheckoutFolder();
					CheckoutLink checkoutLink = WorkInProgressHelper.service.checkout(wtdocument, folder, description);
					return (WTDocument) checkoutLink.getWorkingCopy();
				}
			} catch (WTPropertyVetoException e)
			{
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			}
		}

		/**
		 * check in wtdocument
		 * 
		 * @param wtdocument
		 * @param description
		 * @return
		 * @throws WTException
		 */
		
		
			public static WTDocument checkInWTDocument(WTDocument wtdocument1, String description) throws WTException
		{
			WTDocument wtdocument = wtdocument1;
			if (wtdocument == null)
			{
				return wtdocument;
			}
			LOGGER.debug("wtdocument : " + wtdocument.getNumber() + " - " + wtdocument.getName() + " version : "
					+ wtdocument.getVersionDisplayIdentifier() + " . " + wtdocument.getIterationDisplayIdentifier() + " state : " + wtdocument.getState());
			if (!wtdocument.isLatestIteration())
			{
				wtdocument = (WTDocument) VersionControlHelper.service.getLatestIteration(wtdocument, false);
				LOGGER.debug("wtdocument : " + wtdocument.getNumber() + " - " + wtdocument.getName() + " version : "
						+ wtdocument.getVersionDisplayIdentifier() + " . " + wtdocument.getIterationDisplayIdentifier() + " state : "
						+ wtdocument.getState());
				LOGGER.debug("wtdocument : " + wtdocument.getNumber() + " - " + wtdocument.getName() + " workcopy : "
						+ WorkInProgressHelper.isWorkingCopy(wtdocument) + " checkout? === " + WorkInProgressHelper.isCheckedOut(wtdocument));
			}
			try
			{
				if (WorkInProgressHelper.isWorkingCopy(wtdocument))
				{
					return (WTDocument) WorkInProgressHelper.service.checkin(wtdocument, description);
				} else if (WorkInProgressHelper.isCheckedOut(wtdocument))
				{
					wtdocument = (WTDocument) WorkInProgressHelper.service.workingCopyOf(wtdocument);
					return (WTDocument) WorkInProgressHelper.service.checkin(wtdocument, description);
				} else
				{
					return wtdocument;
				}
			} catch (WTPropertyVetoException e)
			{
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			}
		}
		
		public static String getLifeCycleByWTDocument(WTDocument doc){
			String lifecycle=doc.getLifeCycleState().toString();
			return lifecycle;
		}
      
		/**
		 * Get WTDocument Associated Processes.
		 * 
		 * @param Persistable a,WfState b,WTContainerRef c
		 * @return
		 */
		public static AssociatedProcessProcessor getAssociatedProcesses(Persistable a,WfState b,WTContainerRef c) {
			AssociatedProcessProcessor ass = null;
			try {
				QueryResult queryResult = WfEngineHelper.service.getAssociatedProcesses(a,
						b,c);
				if (queryResult != null && queryResult.hasMoreElements()) {
					ass =  (AssociatedProcessProcessor) queryResult.nextElement();
				}
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ass;
		}
    
			/**
			 * 通过编号查找文档
			 *
			 * @param number:查询文档编号条件
			 * @param accessControlled:是否受到权限制约
			 * @return 返回最新大版本的最新小版本文档
			 */
			public static WTDocument getDoc(String number, boolean accessControlled) {
				try {
					number = number.toUpperCase();

					if (!RemoteMethodServer.ServerFlag) {
						return (WTDocument) RemoteMethodServer.getDefault().invoke(
								"getDoc", DocUtil.class.getName(), null,
								new Class[] { String.class, boolean.class },
								new Object[] { number, accessControlled });
					} else {
						WTDocument doc = null;

						boolean enforce = wt.session.SessionServerHelper.manager
								.setAccessEnforced(accessControlled);
						try {
							QuerySpec spec = new QuerySpec(WTDocument.class);
							spec.appendWhere(new SearchCondition(WTDocument.class,
									WTDocument.NUMBER, SearchCondition.EQUAL, number),
									new int[] { 0 });

							QueryResult qr = PersistenceHelper.manager.find(spec);
							if (qr.hasMoreElements()) {
								WTDocument document = (WTDocument) qr.nextElement();
								QueryResult qr2 = VersionControlHelper.service
										.allIterationsOf(document.getMaster());
								if (qr2.hasMoreElements()) {
									doc = (WTDocument) qr2.nextElement();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}
						return doc;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			/**
			 * 根据文档编号获取最新版本的文档对象
			 * 
			 * @param number
			 * @throws wt.util.WTException
			 * @return
			 */
			public static WTDocument getWTDocByNumber(String number) throws WTException {
				WTDocument doc = null;
				QuerySpec qs = null;
				QueryResult qr = null;
				qs = new QuerySpec(WTDocument.class);
				SearchCondition temp = new SearchCondition(WTDocument.class,
						wt.doc.WTDocument.NUMBER, SearchCondition.EQUAL, number);
				qs.appendSearchCondition(temp);
				qs.appendAnd();
				SearchCondition latest = VersionControlHelper.getSearchCondition(
						wt.doc.WTDocument.class, true);
				qs.appendSearchCondition(latest);
				qr = PersistenceHelper.manager.find(qs);
				LatestConfigSpec cfg = new LatestConfigSpec(); 
				QueryResult qr1 = cfg.process(qr);
				while (qr1.hasMoreElements()) {
					doc = (WTDocument) qr1.nextElement();
					//Log.info("【工艺路线更改】文档版本：" + doc.getName()+doc.getVersionInfo().getIdentifier().getValue());
				}
				return doc;
			}

			/**
			 * 通过文档得到其关联的文档，返回Dependence文档列表
			 *
			 * @param doc:条件文档
			 * @return 依附于条件文档的文档列表
			 */
			public static ArrayList<WTDocument> getDocsByDoc(WTDocument doc) {
				ArrayList<WTDocument> results = new ArrayList<WTDocument>();

				try {
					if (!RemoteMethodServer.ServerFlag) {
						return (ArrayList) RemoteMethodServer.getDefault().invoke(
								"getDocsByDoc", DocUtil.class.getName(), null,
								new Class[] { WTDocument.class }, new Object[] { doc });
					} else {
						boolean enforce = wt.session.SessionServerHelper.manager
								.setAccessEnforced(false);
						try {
							QueryResult qr = WTDocumentHelper.service
									.getDependsOnWTDocuments(doc);
							while (qr.hasMoreElements()) {
								Object tempObj = qr.nextElement();
								if (tempObj instanceof WTDocument) {
									results.add((WTDocument) tempObj);
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						} finally {
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}
						return results;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return results;
			}

			/**
			 * 返回文档所被参考的文档
			 * 
			 * @param srcDoc
			 * @return
			 * @throws RemoteException
			 * @throws InvocationTargetException
			 */
			public static List<WTDocument> getReferenceByDocs(WTDocument srcDoc)
					throws RemoteException, InvocationTargetException {
				if (!RemoteMethodServer.ServerFlag) {
					Class<?>[] clz = new Class<?>[] { WTDocument.class };
					Object[] objs = new Object[] { srcDoc };
					try {
						return (List<WTDocument>) RemoteMethodServer.getDefault().invoke(
								"getReferenceByDocs", DocUtil.class.getName(), null, clz,
								objs);
					} catch (java.rmi.RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				List<WTDocument> rsList = new ArrayList<WTDocument>();
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					QueryResult qr = WTDocumentHelper.service
							.getHasDependentWTDocuments(srcDoc, true);
					while (qr.hasMoreElements()) {
						Object tempObj = qr.nextElement();
						if (tempObj instanceof WTDocument) {
							WTDocument doc = (WTDocument) tempObj;
							rsList.add(doc);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return rsList;
			}

			/**
			 * 通过文档得到其关联的部件（描述），返回Describe部件列表
			 *
			 * @param doc:条件文档
			 * @return 依附于条件文档的（描述）部件列表
			 */
			public static ArrayList<WTPart> getDescPartsByDoc(WTDocument doc) {
				ArrayList<WTPart> results = new ArrayList<WTPart>();

				try {
					if (!RemoteMethodServer.ServerFlag) {
						return (ArrayList) RemoteMethodServer.getDefault().invoke(
								"getDescPartsByDoc", DocUtil.class.getName(), null,
								new Class[] { WTDocument.class }, new Object[] { doc });
					} else {
						boolean enforce = wt.session.SessionServerHelper.manager
								.setAccessEnforced(false);
						try {
							QueryResult qr = WTPartHelper.service
									.getDescribesWTParts(doc);

							while (qr.hasMoreElements()) {
								Object tempObj = qr.nextElement();
								if (tempObj instanceof WTPart) {
									results.add((WTPart) tempObj);
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						} finally {
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}
						return results;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return results;
			}

			/**
			 * 通过文档得到其关联的部件（参考），返回Reference部件列表
			 *
			 * @param doc:条件文档
			 * @return 依附于条件文档的（参考）部件列表
			 */
			public static ArrayList<WTPart> getRefPartsByDoc(WTDocument doc) {
				ArrayList<WTPart> results = new ArrayList<WTPart>();

				try {
					if (!RemoteMethodServer.ServerFlag) {
						return (ArrayList) RemoteMethodServer.getDefault().invoke(
								"getRefPartsByDoc", DocUtil.class.getName(), null,
								new Class[] { WTDocument.class }, new Object[] { doc });
					} else {
						boolean enforce = wt.session.SessionServerHelper.manager
								.setAccessEnforced(false);
						try {
							WTDocumentMaster master = (WTDocumentMaster) doc
									.getMaster();
							QueryResult qr = StructHelper.service.navigateReferencedBy(
									master, WTPartReferenceLink.class, true);

							while (qr.hasMoreElements()) {
								Object tempObj = qr.nextElement();
								if (tempObj instanceof WTPart) {
									results.add((WTPart) tempObj);
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						} finally {
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}
						return results;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return results;
			}

			/**
			 * 得到默认的文档序列号
			 *
			 * @return 10位的序列号字符串
			 */
			public static String getDefaultDocSeqNumber() {
				String bitFormat = "";

				try {
					for (int i = 0; i < 10; i++) {
						bitFormat = bitFormat + "0";
					}

					int seq = Integer.parseInt(PersistenceHelper.manager
							.getNextSequence(WTDocument.class));
					DecimalFormat format = new DecimalFormat(bitFormat);
					return format.format(seq);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return null;
			}

			/**
			 * 创建文档
			 *
			 * @param number
			 *            编号 （默认为系统的Doc Sequence）
			 * @param name
			 *            名称 （必须）
			 * @param attributes
			 *            属性对应表
			 * @param primaryContent
			 *            主内容 (String FilePath 或者 InputStream)
			 * @param secondaryContent
			 *            附件 (String FilePath 或者 InputStream)
			 * @param containerRef
			 *            上下文 （必须）
			 * @return 如果指定编号的文档存在，则返回该文档的最新大小版本对象，若不存在，则返回创建后的文档
			 */
			public static WTDocument createDoc(String number, String name,
					HashMap attributes, Object primaryContent,
					ArrayList secondaryContents, WTContainerRef containerRef) {
				try {
					if (!RemoteMethodServer.ServerFlag) {
						return (WTDocument) RemoteMethodServer
								.getDefault()
								.invoke("createDoc",
										DocUtil.class.getName(),
										null,
										new Class[] { String.class, String.class,
												HashMap.class, Object.class,
												ArrayList.class, WTContainerRef.class },
										new Object[] { number, name, attributes,
												primaryContent, secondaryContents,
												containerRef });
					} else {
						WTDocument doc = null;

						try {

							String docDesc = "";
							String docType = "";
							String docFolder = "";

							if (attributes != null) {
								docDesc = (String) attributes.get(Constant.DESCRIPTION);
								docType = (String) attributes.get(Constant.TYPE);
								docFolder = (String) attributes.get(Constant.FOLDER);
							}

							if (containerRef == null) {
								return null;
							}

							// 设置编号默认值 （默认为系统的Doc Sequence）
							if (number == null || number.equalsIgnoreCase("")) {
								number = DocUtil.getDefaultDocSeqNumber();
							} else {
								// 如果此编号文档已存在，则返回该文档
								WTDocument existDoc = DocUtil.getDoc(number, false);
								if (existDoc != null) {
									return existDoc;
								}
							}

							if (name == null || name.equalsIgnoreCase("")) {
								return null;
							}

							if (docDesc == null) {
								docDesc = "";
							}

							if (docType == null || docType.equalsIgnoreCase("")) {
								docType = "wt.doc.WTDocument";
							}

							if (docFolder == null || docFolder.equalsIgnoreCase("")) {
								docFolder = "/Default";
							} else {
								if (!docFolder.startsWith("/Default")) {
									docFolder = "/Default/" + docFolder;
								}
							}

							doc = WTDocument.newWTDocument(number, name,
									DocumentType.getDocumentTypeDefault());

							// 设置文档描述
							doc.setDescription(docDesc);

							// 设置文档类型
							if (docType != null) {
								TypeIdentifier id = TypeIdentifierHelper
										.getTypeIdentifier(docType);
								doc = (WTDocument) CoreMetaUtility.setType(doc, id);
							}

							// 设置上下文
							doc.setContainerReference(containerRef);

							// 设置文件夹
							Folder location = null;
							// 查询文件夹是否存在
							try {
								location = FolderHelper.service.getFolder(docFolder,
										containerRef);
							} catch (Exception e) {
								location = null;
							}
							// 若文件夹不存在，则创建该文件夹
							if (location == null)
								location = FolderHelper.service.saveFolderPath(
										docFolder, containerRef);
							// 设置文件夹到Doc对象
							if (location != null) {
								WTValuedHashMap map = new WTValuedHashMap();
								map.put(doc, location);
								FolderHelper.assignLocations(map);
							}

							doc = (WTDocument) PersistenceHelper.manager.save(doc);
							doc = (WTDocument) PersistenceHelper.manager.refresh(doc);

							// 设置主内容
							if (primaryContent != null) {
								ApplicationData applicationdata = ApplicationData
										.newApplicationData(doc);
								applicationdata.setRole(ContentRoleType.PRIMARY);
								if (primaryContent instanceof String) {
									String filePath = (String) primaryContent;
									applicationdata = ContentServerHelper.service
											.updateContent(doc, applicationdata,
													filePath);
								} else if (primaryContent instanceof InputStream) {
									InputStream ins = (InputStream) primaryContent;
									applicationdata = ContentServerHelper.service
											.updateContent(doc, applicationdata, ins);
								}

							}

							// 设置附件
							if (secondaryContents != null) {
								for (int i = 0; i < secondaryContents.size(); i++) {
									Object secondaryContent = secondaryContents.get(i);

									ApplicationData applicationdata = ApplicationData
											.newApplicationData(doc);
									applicationdata.setRole(ContentRoleType.SECONDARY);
									if (secondaryContent instanceof String) {
										String filePath = (String) secondaryContent;
										applicationdata = ContentServerHelper.service
												.updateContent(doc, applicationdata,
														filePath);
									} else if (secondaryContent instanceof InputStream) {
										InputStream ins = (InputStream) secondaryContent;
										applicationdata = ContentServerHelper.service
												.updateContent(doc, applicationdata,
														ins);
									}
								}
							}

							doc = (WTDocument) PersistenceServerHelper.manager
									.restore(doc);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}

						return doc;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}


			/**
			 * 删除文档与文档之间的关联（不对小版本进行升级）
			 *
			 * @param docRoleA
			 *            文档A
			 * @param docRoleB
			 *            文档B
			 * @return 删除操作是否成功
			 */
			public static boolean removeDocToDoc(WTDocument docRoleA,
					WTDocument docRoleB) {
				boolean result = false;
				try {
					if (!RemoteMethodServer.ServerFlag) {
						return (Boolean) RemoteMethodServer.getDefault().invoke(
								"removeDocToDoc", DocUtil.class.getName(), null,
								new Class[] { WTDocument.class, WTDocument.class },
								new Object[] { docRoleA, docRoleB });
					} else {
						boolean enforce = wt.session.SessionServerHelper.manager
								.setAccessEnforced(false);
						try {
							// 检查是否文档间已经有连接，得到WTDocumentDependencyLink
							QueryResult queryresult = PersistenceHelper.manager.find(
									WTDocumentDependencyLink.class, docRoleA,
									WTDocumentDependencyLink.DESCRIBES_ROLE, docRoleB);
							if (queryresult == null || queryresult.size() == 0) {
								// 如果没有连接，则退出
								return true;
							} else {
								// 如果有连接，则删除改连接
								WTDocumentDependencyLink link = (WTDocumentDependencyLink) queryresult
										.nextElement();
								PersistenceServerHelper.manager.remove(link);
								result = true;
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
							result = false;
						} finally {
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					result = false;
				}
				return result;
			}

			/**
			 * 替换文档的主内容（不对小版本进行升级）
			 *
			 * @param doc
			 *            需要被替换的文档
			 * @param content
			 *            主内容(String FilePath 或者 InputStream)
			 * @return 被替换的文档
			 */
			public static WTDocument replaceDocPrimaryContent(WTDocument doc,
					Object content) {
				try {
					if (!RemoteMethodServer.ServerFlag) {
						return (WTDocument) RemoteMethodServer.getDefault().invoke(
								"replaceDocPrimaryContent", DocUtil.class.getName(),
								null, new Class[] { WTDocument.class, Object.class },
								new Object[] { doc, content });
					} else {
						try {
							wt.content.ContentItem docContentItem = ContentHelper.service
									.getPrimary((FormatContentHolder) doc);
							ContentServerHelper.service.deleteContent(doc,
									docContentItem);

							ApplicationData applicationdata = ApplicationData
									.newApplicationData(doc);
							applicationdata.setRole(ContentRoleType.PRIMARY);

							if (content instanceof String) {
								String filePath = (String) content;
								applicationdata = ContentServerHelper.service
										.updateContent(doc, applicationdata, filePath);
								PersistenceServerHelper.manager.restore(doc);
							} else if (content instanceof InputStream) {
								InputStream ins = (InputStream) content;
								applicationdata = ContentServerHelper.service
										.updateContent(doc, applicationdata, ins);
								PersistenceServerHelper.manager.restore(doc);
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
						return doc;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return doc;
			}

			/**
			 * 下载文档的内容文件到指定的目录
			 *
			 * @param doc
			 *            对象文档
			 * @param roleType
			 *            内容类型(ContentRoleType.PRIMARY 或 ContentRoleType.SECONDARY)
			 * @param targetFolder
			 *            目的路径
			 * @return 一组文件下载全路径
			 */
			public static ArrayList<String> downloadContentFiles(WTDocument doc,
					ContentRoleType roleType, String targetFolder) {
				ArrayList<String> result = new ArrayList<String>();
				try {
					if (!RemoteMethodServer.ServerFlag) {
						return (ArrayList) RemoteMethodServer.getDefault().invoke(
								"downloadContentFiles",
								DocUtil.class.getName(),
								null,
								new Class[] { WTDocument.class, ContentRoleType.class,
										String.class },
								new Object[] { doc, roleType, targetFolder });
					} else {
						boolean enforce = wt.session.SessionServerHelper.manager
								.setAccessEnforced(false);
						try {
							ContentHolder holder = ContentHelper.service
									.getContents((ContentHolder) doc);
							QueryResult qr = ContentHelper.service.getContentsByRole(
									holder, roleType);
							while (qr.hasMoreElements()) {
								Object objQr = qr.nextElement();
								if (objQr instanceof ApplicationData) {
									ApplicationData ad = (ApplicationData) objQr;
									String adName = ad.getFileName();
									InputStream is = ContentServerHelper.service
											.findContentStream(ad);
									if (is != null) {
										File downloadFolder = new File(targetFolder);
										boolean folderExist = downloadFolder.exists();
										if (!folderExist) {
											folderExist = downloadFolder.mkdirs();
											if (!folderExist) {
												// 文件夹创建失败
												return new ArrayList<String>();
											}
										}

										String targetFilePath = targetFolder
												+ File.separator + adName;
										File downloadFile = new File(targetFilePath);

										FileOutputStream fos = new FileOutputStream(
												downloadFile);
										byte[] buf = new byte[1024];

										int len = 0;
										while ((len = is.read(buf)) >= 0) {
											fos.write(buf, 0, len);
										}
										is.close();
										result.add(targetFilePath);
									}
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						} finally {
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}
						return result;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return result;

			}

			public static QueryResult getDocByName(String name, boolean accessControlled) {
				try {

					if (!RemoteMethodServer.ServerFlag) {
						return (QueryResult) RemoteMethodServer.getDefault().invoke(
								"getDocByName", DocUtil.class.getName(), null,
								new Class[] { String.class, boolean.class },
								new Object[] { name, accessControlled });
					} else {
						QueryResult qr = new QueryResult();
						boolean enforce = wt.session.SessionServerHelper.manager
								.setAccessEnforced(accessControlled);
						try {
							QuerySpec spec = new QuerySpec(WTDocument.class);
							SearchCondition sc = new SearchCondition(WTDocument.class,
									WTDocument.NAME, SearchCondition.EQUAL, name);
							spec.appendWhere(sc, new int[] { 0 });
							spec.appendAnd();
							sc = new SearchCondition(WTDocument.class,
									WTDocument.LATEST_ITERATION,
									SearchCondition.IS_TRUE);
							spec.appendWhere(sc, new int[] { 0 });
							qr = PersistenceHelper.manager.find(spec);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}

						return qr;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			public static <T> QueryResult GetAllPartInContainer(String partType,
					WTContainer container, Class<T> typeClass) {
				QueryResult qr = new QueryResult();
				try {
					QuerySpec qs = new QuerySpec();
					int partIndex = qs.appendClassList(typeClass, true);
					int wttypeIndex = qs.appendClassList(WTTypeDefinition.class, false);
					int containerIndex = qs
							.appendClassList(container.getClass(), false);
					SearchCondition sc = new SearchCondition(typeClass,
							"typeDefinitionReference.key.id", WTTypeDefinition.class,
							"thePersistInfo.theObjectIdentifier.id");
					qs.appendWhere(sc, new int[] { partIndex, wttypeIndex });
					qs.appendAnd();
					sc = new SearchCondition(WTTypeDefinition.class,
							"logicalIdentifier", SearchCondition.EQUAL, partType);
					qs.appendWhere(sc, new int[] { wttypeIndex });
					qs.appendAnd();
					String idStr = container.getIdentity();
					idStr = idStr.substring(idStr.lastIndexOf(':') + 1);
					sc = new SearchCondition(typeClass, "containerReference.key.id",
							container.getClass(),
							"thePersistInfo.theObjectIdentifier.id");
					qs.appendWhere(sc, new int[] { partIndex, containerIndex });
					qs.appendAnd();
					sc = new SearchCondition(container.getClass(),
							"thePersistInfo.theObjectIdentifier.id",
							SearchCondition.EQUAL, Long.valueOf(idStr));
					qs.appendWhere(sc, new int[] { containerIndex });
					qs.appendAnd();
					sc = new SearchCondition(typeClass, "checkoutInfo.state",
							SearchCondition.NOT_EQUAL, "wrk");
					qs.appendWhere(sc, new int[] { partIndex });
					qs.appendAnd();
					sc = new SearchCondition(typeClass, WTDocument.LATEST_ITERATION,
							SearchCondition.IS_TRUE);
					qs.appendWhere(sc, new int[] { partIndex });
					qs = new LatestConfigSpec().appendSearchCriteria(qs);
					qr = PersistenceHelper.manager.find(qs);
				} catch (WTException e) {
					e.printStackTrace();
				}
				return qr;
			}

			public static synchronized <T extends Master> void setTObjectNumber(
					T master, String number) throws WTException {
				boolean access = SessionServerHelper.manager.setAccessEnforced(false);
				String user = SessionHelper.manager.getPrincipal().getName();
				Transaction transaction = null;
				try {
					transaction = new Transaction();
					transaction.start();

					SessionHelper.manager.setAdministrator();

					Identified identified = (Identified) master;
					WTDocumentMasterIdentity masteridentity = (WTDocumentMasterIdentity) identified
							.getIdentificationObject();
					// 必须对masteridentity设置number，不能直接对WTDocument对象设置number
					masteridentity.setNumber(number);
					identified = IdentityHelper.service.changeIdentity(identified,
							masteridentity);
					SessionHelper.manager.setPrincipal(user);
					PersistenceServerHelper.manager.update((Persistable) master);

					transaction.commit();
					transaction = null;
				} catch (WTException e) {
					e.printStackTrace();
				} catch (WTPropertyVetoException e) {
					e.printStackTrace();
				} finally {
					if (transaction != null)
						transaction.rollback();
					SessionHelper.manager.setPrincipal(user);
					SessionServerHelper.manager.setAccessEnforced(access);
				}
			}

			public static synchronized <T extends Mastered> void setTObjectNumber(
					T master, String number) throws WTException {
				boolean access = SessionServerHelper.manager.setAccessEnforced(false);
				String user = SessionHelper.manager.getPrincipal().getName();
				Transaction transaction = null;
				try {
					transaction = new Transaction();
					transaction.start();
					SessionHelper.manager.setAdministrator();
					Identified identified = (Identified) master;
					IdentificationObject masteridentity = (IdentificationObject) identified
							.getIdentificationObject();
					// 必须对masteridentity设置number，不能直接对WTDocument对象设置number
					Method setNumMethod = masteridentity.getClass().getMethod(
							"setNumber", String.class);
					if (setNumMethod != null) {
						setNumMethod.invoke(masteridentity, number);
						identified = IdentityHelper.service.changeIdentity(identified,
								masteridentity);
						SessionHelper.manager.setPrincipal(user);
						PersistenceServerHelper.manager.update(master);
					}

					transaction.commit();
					transaction = null;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (transaction != null)
						transaction.rollback();
					SessionHelper.manager.setPrincipal(user);
					SessionServerHelper.manager.setAccessEnforced(access);
				}
			}

			public static WTCollection createRelationship(WTCollection docs,
					WTPart part, boolean isRefDoc, boolean needAutoCheckIn,
					boolean isPartDoc) throws WTException {

				boolean access = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					WTCollection objects = new WTArrayList();
					WTCollection links = new WTArrayList();
					// be careful about inflating WTReference to a Persistable
					boolean isCOByMe = WIPUtils.enableableObject(part);
					boolean isCOValid = WIPUtils.isCheckOutValid(part, WIPUtils.FULL);
					boolean needCI = false;

					if (isCOByMe || isCOValid) {
						if (isRefDoc) {
							links = createPartDocReferenceLinks(part, docs);
						} else {
							links = createPartDocDescribeLinks(part, docs);
						}
					}
					if (isPartDoc) {
						// Part to Doc actions
						objects.add(part);
						// add all docs as not added links
						objects.addAll(docs);

						for (Iterator it = links.persistableIterator(); it.hasNext();) {
							// remove doc links that were added from the message list
							if (isRefDoc) {
								WTPartReferenceLink refLink = (WTPartReferenceLink) it
										.next();
								WTDocumentMaster docRef = (WTDocumentMaster) refLink
										.getRoleBObject();
								objects.remove(docRef);
							} else {
								WTPartDescribeLink describeLink = (WTPartDescribeLink) it
										.next();
								WTDocument docRef = (WTDocument) describeLink
										.getRoleBObject();
								objects.remove(docRef);
							}
						}
					} else {
						if (links.size() == 0) {
							objects.add(part);
						}
					}
					return objects;
				} finally {
					SessionServerHelper.manager.setAccessEnforced(access);
				}
			}

			public static WTCollection createPartDocDescribeLinks(WTPart wtpart,
					WTCollection wtcollection) {
				Transaction transaction;
				boolean flag;
				WTArrayList wtarraylist;
				transaction = new Transaction();
				flag = false;
				wtarraylist = new WTArrayList();
				try {
					transaction.start();
					// part = (WTPart)
					// PersistenceHelper.manager.prepareForModification(part);
					PersistenceServerHelper.manager.lock(wtpart, true);
					Iterator iterator = wtcollection.persistableIterator();
					do {
						if (!iterator.hasNext())
							break;
						WTDocument wtdocument = (WTDocument) iterator.next();
						QueryResult queryresult = intGetDescribeAssociations(wtpart,
								(WTDocumentMaster) wtdocument.getMaster());
						if (queryresult.size() > 0) {
							do {
								if (!queryresult.hasMoreElements())
									break;
								WTPartDescribeLink wtpartdescribelink = (WTPartDescribeLink) queryresult
										.nextElement();
								if (PersistenceHelper.isEquivalent(wtdocument,
										wtpartdescribelink.getDescribedBy()))
									flag = true;
								else if (!PartDocHelper.isWcPDMMethod())
									PersistenceHelper.manager
											.delete(wtpartdescribelink);
							} while (true);
							if (!flag) {
								WTPartDescribeLink wtpartdescribelink1 = WTPartDescribeLink
										.newWTPartDescribeLink(wtpart, wtdocument);
								PersistenceHelper.manager.store(wtpartdescribelink1);
								wtarraylist.add(wtpartdescribelink1);
							}
						} else {
							WTPartDescribeLink wtpartdescribelink2 = WTPartDescribeLink
									.newWTPartDescribeLink(wtpart, wtdocument);
							PersistenceServerHelper.manager.insert(wtpartdescribelink2);
							wtarraylist.add(wtpartdescribelink2);
						}
					} while (true);
					transaction.commit();
					transaction = null;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (transaction != null) {
						transaction.rollback();
					}
				}
				return wtarraylist;
			}

			public static WTCollection createPartDocReferenceLinks(WTPart part,
					WTCollection documents) throws WTException {
				Transaction trx = new Transaction();
				WTCollection referenceLinks = new WTArrayList();
				try {
					trx.start();
					PersistenceServerHelper.manager.lock(part, true);
					for (Iterator it = documents.persistableIterator(); it.hasNext();) {
						WTDocumentMaster docMaster = (WTDocumentMaster) it.next();
						QueryResult qr = intGetReferenceAssociations(part, docMaster);
						if (qr.size() > 0) {
							// Delete all but the last link.
							for (int i = 0; i < qr.size() - 1; i++) {
								PersistenceHelper.manager
										.delete((WTPartReferenceLink) qr.nextElement());
							}
						} else {
							Link link = (Link) WTPartReferenceLink
									.newWTPartReferenceLink(part, docMaster);
							PersistenceServerHelper.manager.insert(link);
							referenceLinks.add(link);
						}
					}
					trx.commit();
					trx = null;
				} finally {
					if (trx != null)
						trx.rollback();
				}
				return referenceLinks;
			}

			private static QueryResult intGetReferenceAssociations(WTPart wtpart,
					WTDocumentMaster wtdocumentmaster) throws WTException {
				QueryResult queryresult = new QueryResult();
				QuerySpec queryspec = new QuerySpec(WTDocumentMaster.class,
						WTPartReferenceLink.class);
				queryspec.appendWhere(
						new SearchCondition(WTPartReferenceLink.class,
								"roleBObjectRef.key", "=", PersistenceHelper
										.getObjectIdentifier(wtdocumentmaster)),
						new int[] { 1 });
				QueryResult queryresult1 = PersistenceServerHelper.manager.expand(
						wtpart, "references", queryspec, false);
				Vector vector = new Vector();
				WTPartReferenceLink wtpartreferencelink;
				for (; queryresult1.hasMoreElements(); vector.add(wtpartreferencelink))
					wtpartreferencelink = (WTPartReferenceLink) queryresult1
							.nextElement();

				queryresult.append(new ObjectSetVector(vector));
				return queryresult;
			}

			private static QueryResult intGetDescribeAssociations(WTPart wtpart,
					WTDocumentMaster wtdocumentmaster) throws WTException {
				QueryResult queryresult = new QueryResult();
				QuerySpec queryspec = new QuerySpec(WTPartDescribeLink.class);
				queryspec.appendClassList(WTDocument.class, true);
				queryspec.appendWhere(
						new SearchCondition(WTPartDescribeLink.class,
								"roleAObjectRef.key", "=", PersistenceHelper
										.getObjectIdentifier(wtpart)), new int[] { 0 });
				queryspec.appendAnd();
				queryspec.appendWhere(new SearchCondition(WTPartDescribeLink.class,
						"roleBObjectRef.key.id", WTDocument.class,
						"thePersistInfo.theObjectIdentifier.id"), new int[] { 0, 1 });
				queryspec.appendAnd();
				queryspec.appendWhere(
						new SearchCondition(WTDocument.class, "masterReference.key",
								"=", PersistenceHelper
										.getObjectIdentifier(wtdocumentmaster)),
						new int[] { 1 });
				QueryResult queryresult1 = PersistenceHelper.manager.find(queryspec);
				Vector vector = new Vector();
				WTPartDescribeLink wtpartdescribelink;
				for (; queryresult1.hasMoreElements(); vector.add(wtpartdescribelink)) {
					Object aobj[] = (Object[]) (Object[]) queryresult1.nextElement();
					wtpartdescribelink = (WTPartDescribeLink) aobj[0];
					try {
						wtpartdescribelink.setDescribes(wtpart);
						wtpartdescribelink.setDescribedBy((WTDocument) aobj[1]);
					} catch (WTPropertyVetoException wtpropertyvetoexception) {
						throw new WTException(wtpropertyvetoexception);
					}
				}

				queryresult.append(new ObjectSetVector(vector));
				return queryresult;
			}

			/**
			 * 查询指定软属性为指定值的文档的最新版本
			 *
			 * @param ibaname
			 * @param ibavalue
			 * @throws Exception
			 */
			public static List<WTDocument> searchDocumentFilterByIba(String ibaname,
					String ibavalue) throws Exception {
				if (!RemoteMethodServer.ServerFlag) {
					return (List<WTDocument>) RemoteMethodServer.getDefault().invoke(
							"searchDocumentFilterByIba", DocUtil.class.getName(), null,
							new Class[] { String.class, String.class },
							new Object[] { ibaname, ibavalue });
				}
				QuerySpec qs = new QuerySpec();
				qs.setAdvancedQueryEnabled(true);

				int ibaHolderIndex = qs.appendClassList(WTDocument.class, true);
				int ibaStringValueIndex = qs.appendClassList(StringValue.class, false);
				int ibaStringDefinitionIndex = qs.appendClassList(
						StringDefinition.class, false);
				// Latest Iteration
				SearchCondition scLatestIteration = new SearchCondition(
						WTDocument.class, WTAttributeNameIfc.LATEST_ITERATION,
						SearchCondition.IS_TRUE);
				// String Value With IBA Holder
				SearchCondition scJoinStringValueIBAHolder = new SearchCondition(
						StringValue.class, "theIBAHolderReference.key.id",
						WTDocument.class, WTAttributeNameIfc.ID_NAME);
				// String Value With Definition
				SearchCondition scJoinStringValueStringDefinition = new SearchCondition(
						StringValue.class, "definitionReference.key.id",
						StringDefinition.class, WTAttributeNameIfc.ID_NAME);
				// String Definition 软属性名称
				SearchCondition scStringDefinitionName = new SearchCondition(
						StringDefinition.class, StringDefinition.NAME,
						SearchCondition.EQUAL, ibaname);
				// String Value 软属性值
				SearchCondition scStringValueValue = new SearchCondition(
						StringValue.class, StringValue.VALUE, SearchCondition.EQUAL,
						ibavalue.toUpperCase());
				// documentmaster name = type
				qs.appendWhere(scLatestIteration, ibaHolderIndex);
				qs.appendAnd();
				qs.appendWhere(scJoinStringValueIBAHolder, ibaStringValueIndex,
						ibaHolderIndex);
				qs.appendAnd();
				qs.appendWhere(scJoinStringValueStringDefinition, ibaStringValueIndex,
						ibaStringDefinitionIndex);
				qs.appendAnd();
				qs.appendWhere(scStringDefinitionName, ibaStringDefinitionIndex);
				qs.appendAnd();
				qs.appendWhere(scStringValueValue, ibaStringValueIndex);
				QueryResult qr = PersistenceHelper.manager.find(qs);
				List<WTDocument> docList = new Vector<WTDocument>();
				while (qr.hasMoreElements()) {
					Object obj = qr.nextElement();
					Persistable[] persistableArray = (Persistable[]) obj;
					Persistable pst = persistableArray[0];
					if (pst instanceof WTDocument) {
						WTDocument doc = (WTDocument) pst;
						doc = getLatestWTDocument(doc.getMaster());
						if (!docList.contains(doc)) {
							docList.add(doc);
						}
					}

				}
				return docList;
			}
			
			/**
			 * 模糊查询指定软属性为指定值的文档的有此属性的值的文档对象
			 *
			 * @param ibaname
			 * @param ibavalue
			 * @throws Exception
			 */
			public static List<WTDocument> searchDocumentFilterByIbaValue(String ibaname,
					String ibavalue) throws Exception {
				if (!RemoteMethodServer.ServerFlag) {
					return (List<WTDocument>) RemoteMethodServer.getDefault().invoke(
							"searchDocumentFilterByIbaValue", DocUtil.class.getName(), null,
							new Class[] { String.class, String.class },
							new Object[] { ibaname, ibavalue });
				}
				QuerySpec qs = new QuerySpec();
				qs.setAdvancedQueryEnabled(true);

				int ibaHolderIndex = qs.appendClassList(WTDocument.class, true);
				int ibaStringValueIndex = qs.appendClassList(StringValue.class, false);
				int ibaStringDefinitionIndex = qs.appendClassList(
						StringDefinition.class, false);
				// Latest Iteration
				SearchCondition scLatestIteration = new SearchCondition(
						WTDocument.class, WTAttributeNameIfc.LATEST_ITERATION,
						SearchCondition.IS_TRUE);
				// String Value With IBA Holder
				SearchCondition scJoinStringValueIBAHolder = new SearchCondition(
						StringValue.class, "theIBAHolderReference.key.id",
						WTDocument.class, WTAttributeNameIfc.ID_NAME);
				// String Value With Definition
				SearchCondition scJoinStringValueStringDefinition = new SearchCondition(
						StringValue.class, "definitionReference.key.id",
						StringDefinition.class, WTAttributeNameIfc.ID_NAME);
				// String Definition 软属性名称
				SearchCondition scStringDefinitionName = new SearchCondition(
						StringDefinition.class, StringDefinition.NAME,
						SearchCondition.EQUAL, ibaname);
				// String Value 软属性值
				SearchCondition scStringValueValue = new SearchCondition(
						StringValue.class, StringValue.VALUE, SearchCondition.LIKE,
						"%" + ibavalue.toUpperCase() + "%");
				// documentmaster name = type
				qs.appendWhere(scLatestIteration, ibaHolderIndex);
				qs.appendAnd();
				qs.appendWhere(scJoinStringValueIBAHolder, ibaStringValueIndex,
						ibaHolderIndex);
				qs.appendAnd();
				qs.appendWhere(scJoinStringValueStringDefinition, ibaStringValueIndex,
						ibaStringDefinitionIndex);
				qs.appendAnd();
				qs.appendWhere(scStringDefinitionName, ibaStringDefinitionIndex);
				qs.appendAnd();
				qs.appendWhere(scStringValueValue, ibaStringValueIndex);
				QueryResult qr = PersistenceHelper.manager.find(qs);
				List<WTDocument> docList = new Vector<WTDocument>();
				while (qr.hasMoreElements()) {
					Object obj = qr.nextElement();
					Persistable[] persistableArray = (Persistable[]) obj;
					Persistable pst = persistableArray[0];
					if (pst instanceof WTDocument) {
						WTDocument doc = (WTDocument) pst;
						if (!docList.contains(doc)) {
							docList.add(doc);
						}
					}

				}
				return docList;
			}

			/**
			 *
			 * 根据文档对象获取最新版本的文档
			 *
			 * @param obj
			 * @param viewName
			 * @return
			 * @throws WTException
			 */
			private static WTDocument getLatestWTDocument(Mastered mastered)
					throws WTException {
				WTDocument lasterwtdoc = null;
				QueryResult qr = VersionControlHelper.service.allVersionsOf(mastered);
				LatestConfigSpec cfg = new LatestConfigSpec();
				qr = cfg.process(qr);
				while (qr.hasMoreElements()) {
					WTDocument wtdoc = (WTDocument) qr.nextElement();
					if (wtdoc != null)
						lasterwtdoc = wtdoc;
				}
				return lasterwtdoc;
			}

			/**
			 * 获取文档的大小版本 下午06:36:11
			 *
			 * @param doc
			 * @return 形如B.2
			 */
			public static String getVersionAndIdentity(Versioned v) {
				try {
					String Verrsion = v.getVersionIdentifier().getValue();
					RevisionControlled revision = (RevisionControlled) v;
					String Identifier = revision.getIterationInfo().getIdentifier()
							.getValue();
					return Verrsion + "." + Identifier;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			/**
			 * 根据编号和名称查询指定类型的文档
			 *
			 * @param number
			 * @param name
			 * @param softType
			 *            如XieTiaoDan
			 * @return
			 * @throws WTException
			 */
			public static QueryResult searchSoftTypeDoc(String number, String name,
					String softType, String[] states,Integer queryLimit) throws WTException {
				if (!RemoteMethodServer.ServerFlag) {
					try {
						return (QueryResult) RemoteMethodServer.getDefault().invoke(
								"searchSoftTypeDoc", DocUtil.class.getName(), null,
								new Class[] { String.class, String.class,String.class, String[].class },
								new Object[] { number, name,softType, states});
					} catch (Exception e) {
						e.printStackTrace();
						throw new WTException(e);
					}
				}
				try {
					QuerySpec qs = new QuerySpec(WTDocument.class);

					qs.appendWhere(new SearchCondition(WTDocument.class,
							WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE),
							new int[] { 0 });
					if(softType != null){
						TypeDefinitionReference tdref = TypedUtilityServiceHelper.service
								.getTypeDefinitionReference("wt.doc.WTDocument|" + softType);
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(WTDocument.class,
								WTDocument.TYPE_DEFINITION_REFERENCE + ".key.id",
								SearchCondition.EQUAL, tdref.getKey().getId()),
								new int[] { 0 });
					}
					if (number != null) {
						SearchCondition searchCondition = new SearchCondition(
								WTDocument.class, WTDocument.NUMBER,
								SearchCondition.EQUAL, number);
						qs.appendAnd();
						qs.appendWhere(searchCondition, new int[] { 0 });
					}

					if (name != null) {
						SearchCondition searchCondition = new SearchCondition(
								WTDocument.class, WTDocument.NAME,
								SearchCondition.EQUAL, name);
						qs.appendAnd();
						qs.appendWhere(searchCondition, new int[] { 0 });
					}
					if (states != null && states.length > 0) {
						qs.appendAnd();
						qs.appendOpenParen();
						for (int i=0;i<states.length;i++) {
							String state = states[i];
							SearchCondition searchCondition = new SearchCondition(
									WTDocument.class, LifeCycleManaged.STATE + "."
											+ LifeCycleState.STATE,
									SearchCondition.EQUAL, state.toUpperCase());
							qs.appendWhere(searchCondition, new int[] { 0 });
							if(i<states.length-1){
								qs.appendOr();
							}
						}
						qs.appendCloseParen();
					}
					if(queryLimit != null){
						qs.setQueryLimit(queryLimit);
					}
					System.out.println("qs:"+qs.toString());
					return PersistenceHelper.manager.find(qs);
				} catch (Exception e) {
					e.printStackTrace();
					throw new WTException(e);
				}
			}

			/**
			 *
			 * @param softType
			 * @param states
			 * @param queryLimit
			 * @return
			 * @throws WTException
			 * @throws WTPropertyVetoException
			 */
			public static QueryResult searchSoftTypeDoc(String softType, String[] states,Integer queryLimit) throws WTException {
				if (!RemoteMethodServer.ServerFlag) {
					try {
						return (QueryResult) RemoteMethodServer.getDefault().invoke(
								"searchSoftTypeDoc", DocUtil.class.getName(), null,
								new Class[] { String.class, String[].class, Integer.class},
								new Object[] { softType, states,queryLimit});
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new WTException(e);
					}
				}
				try{
					QuerySpec qs = new QuerySpec();
					qs.setAdvancedQueryEnabled(true);
					int a = qs.addClassList(WTDocument.class, true);
					String docAlias = qs.getFromClause().getAliasAt(a);
					TableColumn tc0 = new TableColumn(docAlias, "IDA2TYPEDEFINITIONREFERENCE");
					qs.appendWhere(new SearchCondition(WTDocument.class,
							WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE),
							new int[] { 0 });
					if(softType != null){
						//子查询
						QuerySpec subQs = new QuerySpec();
						subQs.getFromClause().setAliasPrefix("B");
						int[] index = new int[2];
						index[0] = subQs.addClassList(WTTypeDefinitionMaster.class, false);
						index[1] = subQs.addClassList(WTTypeDefinition.class, false);

						String typeMasterAlias = subQs.getFromClause().getAliasAt(index[0]);
						String typeAlias = subQs.getFromClause().getAliasAt(index[1]);

						TableColumn tc00 = new TableColumn(typeMasterAlias, "IDA2A2");
						TableColumn tc01 = new TableColumn(typeMasterAlias, "INTHID");
						TableColumn tc10 = new TableColumn(typeAlias, "IDA3MASTERREFERENCE");
						TableColumn tc11 = new TableColumn(typeAlias, "IDA2A2");
						subQs.appendSelect(tc11, false);
						subQs.appendWhere(new SearchCondition(tc01,
								SearchCondition.EQUAL,new ConstantExpression(softType)),new int[] { 0 });
						subQs.appendAnd();
						subQs.appendWhere(new SearchCondition(tc00,
								SearchCondition.EQUAL,tc10),new int[] { 0 });
						//添加子查询到主查询的条件中
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(tc0,
								SearchCondition.IN, new SubSelectExpression(subQs)),
								new int[] { a });
					}

					if (states != null && states.length > 0) {
						qs.appendAnd();
						qs.appendOpenParen();
						for (int i=0;i<states.length;i++) {
							String state = states[i];
							SearchCondition searchCondition = new SearchCondition(
									WTDocument.class, LifeCycleManaged.STATE + "."
											+ LifeCycleState.STATE,
									SearchCondition.EQUAL, state.toUpperCase());
							qs.appendWhere(searchCondition, new int[] { 0 });
							if(i<states.length-1){
								qs.appendOr();
							}
						}
						qs.appendCloseParen();
					}

					if(queryLimit != null){
						qs.setQueryLimit(queryLimit);
					}
					qs.appendOrderBy(new OrderBy(new ClassAttribute(
							WTDocument.class, WTDocument.PERSIST_INFO + "."
							+ PersistInfo.MODIFY_STAMP), true),
					new int[] { 0 });
					System.out.println("qs:"+qs.toString());
//					return PersistenceHelper.manager.find(qs);
					return PersistenceServerHelper.manager.query(qs);
				}catch(Exception e){
					e.printStackTrace();
					throw new WTException(e);
				}
			}


			/**
			 * 判断传进来的对象是否是最新大版本里的最新小版本
			 * 下午06:25:54
			 * @param v
			 * @return
			 */
			public static boolean isLatestVersion(Versioned v){
				if(isLatestVersion1(v) && isLatestVersion2(v) && isLatestVersion3(v)){
					return true;
				}
				return false;
			}

			/**
			 * 测试正确，07.22 判断传进来的对象是否是最新大版本,不管小版本,Versioned是doc,part等的父类
			 * 下午10:36:30
			 * @param v
			 * @return
			 */
		    public static boolean isLatestVersion1(Versioned v) {
		        if (v != null) {
		            try {
		            	String paramVerrsion = v.getVersionIdentifier().getValue();
		                QueryResult qr = VersionControlHelper.service.allVersionsOf(v);
		                Vector vector = qr.getObjectVectorIfc().getVector();
		                //Vector里存放的大版本由高到底，所以只需要判断第一个即可
		                Versioned obj = (Versioned) vector.get(0);
		                String currentVersion = obj.getVersionIdentifier().getValue();
		                if(paramVerrsion.equals(currentVersion)){
		                	return true;
		                }
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }
		        return false;
		    }

		    /**
		     * 测试正确07.22 判断传进来的对象是否是当前大版本里最新的小版本
		     * 下午10:39:32
		     * @param doc
		     * @return
		     */
		    public static boolean isLatestVersion2(Versioned v){
		    	if(v == null){
		    		return false;
		    	}
		    	return v.isLatestIteration();
		    }

		    /**
		     * 测试正确07.22 判断传进来的对象是否是当前大版本里最新的小版本
		     * 下午10:40:33
		     * @param doc
		     * @return
		     */
		    public static boolean isLatestVersion3(Versioned v){
		    	return VersionControlHelper.isLatestIteration((Iterated) v);
		    }

		    /**
		     * 获取文档的最新已发布版本
		     * 
		     * @param docNumber
		     * @return
		     * @throws PersistenceException
		     * @throws WTException
		     */
		    public static WTDocument findRightVersionDoc(String docNumber)throws PersistenceException, WTException {
				WTDocument returnDoc = null;
				WTDocument doc = getDoc(docNumber, false);
				if (doc == null) {
					return returnDoc;
				}
				WTDocumentMaster docMaster = (WTDocumentMaster) doc.getMaster();
				QueryResult qr = null;
				qr = VersionControlHelper.service.allVersionsOf(docMaster);
				while (qr.hasMoreElements()) {
					WTDocument temp = (WTDocument) qr.nextElement();
					String status = temp.getLifeCycleState().getDisplay(
							Locale.SIMPLIFIED_CHINESE);
					if ("已发放".equals(status)) {
						returnDoc = temp;
						break;
					}
				}
				return returnDoc;
		    }
		    
		    /**
		     * 获取文档的最新已发布版本
		     * 
		     * @param docNumber
		     * @return
		     * @throws PersistenceException
		     * @throws WTException
		     */
		    public static WTDocument findRightVersionDoc(WTDocument doc)throws PersistenceException, WTException {
				WTDocument returnDoc = null;
				if(doc == null){
					return null;
				}
				WTDocumentMaster docMaster = (WTDocumentMaster) doc.getMaster();
				QueryResult qr = null;
				qr = VersionControlHelper.service.allVersionsOf(docMaster);
				while (qr.hasMoreElements()) {
					WTDocument temp = (WTDocument) qr.nextElement();
					String status = temp.getLifeCycleState().getDisplay(
							Locale.SIMPLIFIED_CHINESE);
					if ("已发放".equals(status)) {
						returnDoc = temp;
						break;
					}
				}
				return returnDoc;
		    }
		    
		    public static List<WTDocument> findRightVersionDoc(List<WTDocument> docList)throws PersistenceException, WTException {
		    	List<WTDocument> newDocList = new ArrayList();
		    	for(WTDocument doc:docList){
		    		WTDocument newDoc = findRightVersionDoc(doc);
		    		if(newDoc != null){
		    			newDocList.add(newDoc);
		    		}
				}
				return newDocList;
		    }
		    
		    /**
		     * 根据部件编号和部件版本获取该版本的最新小版本.
		     *
		     * @param number
		     * @param version
		     * @return
		     * @throws WTException
		     */
			public static WTDocument getDocumentByNumberByVersionByIteration(
					String number, String version) throws WTException {
				QuerySpec query = new QuerySpec();
				query.setAdvancedQueryEnabled(true);
				int docTable = query.appendClassList(WTDocument.class, true);
				int docMasterTable = query.appendClassList(WTDocumentMaster.class,
						false);
				String[] aliases = new String[2];
				aliases[0] = query.getFromClause().getAliasAt(docTable);
				aliases[1] = query.getFromClause().getAliasAt(docMasterTable);

				TableColumn VERSION = new TableColumn(aliases[0],
						"VERSIONIDA2VERSIONINFO");
				TableColumn IDA3MASTERREFERENCE = new TableColumn(aliases[0],
						"IDA3MASTERREFERENCE");
				TableColumn LATESTITERATIONINFO = new TableColumn(aliases[0],
						"LATESTITERATIONINFO");
				TableColumn numberCol = new TableColumn(aliases[1], "WTDOCUMENTNUMBER");
				TableColumn IDA2A2Col = new TableColumn(aliases[1], "IDA2A2");
				query.appendWhere(new SearchCondition(numberCol, SearchCondition.EQUAL,
						new ConstantExpression(number)));  
				query.appendAnd();
				query.appendWhere(new SearchCondition(IDA2A2Col, SearchCondition.EQUAL,
						IDA3MASTERREFERENCE));
				query.appendAnd();
				query.appendWhere(new SearchCondition(LATESTITERATIONINFO, SearchCondition.IS_TRUE,
						null));
				query.appendAnd();
				query.appendWhere(new SearchCondition(VERSION, SearchCondition.EQUAL,
						new ConstantExpression(version)));
				System.out.println("sql:" + query.toString());
				QueryResult qr = PersistenceHelper.manager.find(query);
				WTDocument document = null;
				if (qr.hasMoreElements()) {
					Object obj[] = (Object[]) qr.nextElement();
					document = (WTDocument) obj[0];
				}
				return document;
			}
			
			public static Vector getPrimaryContent2(WTDocument wtdocument) throws WTException, PropertyVetoException {
				ContentHolder contentHolder = null;
				Vector contentitem= null;
				try {
					contentHolder = ContentHelper.service.getContents((ContentHolder) wtdocument);
					contentitem = ContentHelper.getApplicationData(contentHolder);
				
				} catch (WTException e1) {
					throw new WTException(e1);
				}
				return contentitem;
			}

			public static ApplicationData getAttachmentByName(WTDocument wtdocument, String fileName) throws WTException, PropertyVetoException {
				ContentHolder contentHolder = null;
				ApplicationData applicationdata = null;
				try {
					contentHolder = ContentHelper.service.getContents((ContentHolder) wtdocument);
					QueryResult qr = ContentHelper.service.getContentsByRole(contentHolder, ContentRoleType.SECONDARY);
					while (qr.hasMoreElements()) {
						ApplicationData appData = (ApplicationData) qr.nextElement();
						String appDataName = appData.getFileName();
						System.out.println("appDataName: " + appDataName);
						if (appDataName.indexOf(fileName) >= 0) {
							applicationdata = appData;
							break;
						}
					}
				} catch (WTException e1) {
					throw new WTException(e1);
				}
				return applicationdata;
			}

			public static void saveFiletoAttachment(ContentHolder contentholder, String filename) throws WTException, WTPropertyVetoException, PropertyVetoException, IOException {
				ApplicationData appData = ApplicationData.newApplicationData(contentholder);
				appData.setRole(ContentRoleType.SECONDARY);
				ContentServerHelper.service.updateContent(contentholder, appData, filename);
			}

			public static WTDocument updatePrimaryContent(String path, WTDocument wtdoc) throws WTException, FileNotFoundException, PropertyVetoException, IOException {
				if (wtdoc == null || path == null || path.trim().length() == 0)
					return wtdoc;
				ContentHolder contentHolder = ContentHelper.service.getContents(wtdoc);
				ContentItem contentItem = ContentHelper.getPrimary((FormatContentHolder) contentHolder);
				if (contentItem instanceof ApplicationData) {
					ApplicationData appData = (ApplicationData) contentItem;
					File file = new File(path);
					if (file != null && file.exists()) {
						FileInputStream fis = new FileInputStream(file);
						Transaction tran = new Transaction(); 
						try {
							tran.start();
							Streamed streamed = getStreamed(appData);
							PersistenceHelper.manager.lockAndRefresh(streamed);
							Streamed streamed_ = streamed.storeStream(wtdoc, appData, fis);
							long filesize = streamed_.getFileSize();
							appData.setFileSize(filesize);
							PersistenceHelper.manager.modify(appData);
							tran.commit();
							tran = null;
						} catch (WTPropertyVetoException e) {
							e.printStackTrace();
						} finally {
							if (tran != null)
								tran.rollback();
							try {
								fis.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				return wtdoc;
			}

			public static Streamed getStreamed(ApplicationData appdata) throws WTException {
				if (appdata == null)
					return null;
				Streamed streamed = null;
				try {
					streamed = (Streamed) PersistenceHelper.manager.refresh(appdata.getStreamData().getObject());
				} catch (ObjectNoLongerExistsException objectnolongerexistsexception) {
					appdata = (ApplicationData) PersistenceHelper.manager.refresh(appdata);
					streamed = (Streamed) PersistenceHelper.manager.refresh(appdata.getStreamData().getObject());
				} catch (WTRuntimeException e) {
					e.printStackTrace();
				}
				return streamed;
			}
			
		


		public static void test() throws RemoteException,
				InvocationTargetException, WTException {
			
		    	WTDocument doc = DocUtil.getDocumentByNumber("0000000101");
		    	doc.getMaster();
		    
		    	String oid = "OR:wt.workflow.work.WorkItem:520337";
		    	try{
		    	     ReferenceFactory rf = new ReferenceFactory();
		    	     WorkItem workItem = (WorkItem)rf.getReference(oid).getObject();
		    	     System.out.println("workItem-------"+workItem);
		    	     System.out.println("-------"+oid);
		    		WTDocument wtdocument = (WTDocument) rf.getReference(oid).getObject();
		    		System.out.println("_________"+wtdocument.getNumber());
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}
		    	
		    	
		        
		    	String oid1 = "VR:wt.part.WTPart:644091";
		    	WTPart wtpart=(WTPart)new ReferenceFactory().getReference(oid1).getObject();
		    	System.out.println("wtpart"+wtpart);
		    	EPMDocument epm=getEPMDocumentByNumber("0000000001");
		    	System.out.println("getEPMDocumentByNumber-----"+epm);
		    	EPMDescribeLink  epmd = getEPMDescribeLink(wtpart, epm);
		    	System.out.println("getEPMDescribeLink----"+epmd);
		    	EPMDescribeLink ep=associateWTPartAndEPMDocument(wtpart, epm);
		    	System.out.println("associateWTPartAndEPMDocument---"+ep);
		    	
		    	
		    	
		    	WTDocument doc1 = DocUtil.getDocumentByNumber("0000000101");
		    	String filePath="D:\\123.xlsx";
		    	String fileName="123.xlsx";
		    	addAttachments(doc1, filePath, fileName);
		    	try {
					System.out.println(getAttachContentList(doc1, fileName));
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	try {
					List<WTDocument> list2=getLatestDocumentListByTypeAndState("wt.doc.WTDocument", "INWORK", true);
					System.out.println(list2);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	try {
					List<WTDocument> list3=getLatestDocumentListByType("wt.doc.WTDocument");
				    System.out.println(list3);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	String oid2 = "OR:wt.workflow.work.WorkItem:520337";
	    	ReferenceFactory rf = new ReferenceFactory();
		     System.out.println("rf-------"+oid2);
			WTDocument wtdocument = (WTDocument) rf.getReference(oid2).getObject();
			
		   	WTContainer wtc=getWtContainerByName("huaqin");
		    
		   	QueryResult qr2=getDocumentByIBAAndContainer(wtc, "com.bjj.IBAAttr", "com.bjj.IBAAttr");
		    	System.out.println(qr2);
		    	if (qr2 != null && qr2.hasMoreElements()) {
			          WTDocument doc12 = (WTDocument) qr2.nextElement();
			          System.out.println(doc12);
			        }
		    	
		    	QueryResult qr1=getDocumentByNumberPrefixAndContainer(wtc, "0%");
		    	System.out.println(qr1);
		        if (qr1 != null && qr1.hasMoreElements()) {
		          WTDocument doc12 = (WTDocument) qr1.nextElement();
		          System.out.println("doc12----"+doc12);
		        }
		    
		      String oid3 = "OR:wt.workflow.work.WorkItem:520337";
		    	ReferenceFactory rf3 = new ReferenceFactory();
			     System.out.println("-------"+oid3);
				WTDocument wtdocument3 = (WTDocument) rf.getReference(oid3).getObject();
			
		    			 QuerySpec querySpec = new QuerySpec(WTDocumentMaster.class);
		    	 try {
					DocUtil.appendUserAttribute(querySpec, "com.bjj.IBAAttr", "com.bjj.IBAAttr");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	 Map <String,String> map=new HashMap<String, String>();
		    	 map.put("com.bjj.IBAAttr", "com.bjj.IBAAttr");  
		    	 try {
					System.out.println("getDocumentByManyIBAAndContainer-----"+getDocumentByManyIBAAndContainer(wtc, map, "com.huaqin.HQ_PRODUCT_MANAGEMENT"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		    	 String oid4 = "OR:wt.pdmlink.PDMLinkProduct:190633";
		     	WTContainer wc=(WTContainer) new ReferenceFactory().getReference(oid4).getObject();
		 		 QuerySpec querySpec4 = new QuerySpec(WTDocumentMaster.class);
		 		 
		 		 
		     	 try {
		 			DocUtil.appendUserAttribute(querySpec, "com.bjj.IBAAttr", "com.bjj.IBAAttr");
		 		} catch (Exception e) {
		 			// TODO Auto-generated catch block
		 			e.printStackTrace();
		 		}
		     	 Map <String,String> map4=new HashMap<String, String>();
		     	 map4.put("com.bjj.IBAAttr", "com.bjj.IBAAttr1");  
		     	 try {
		 			System.out.println(getDocumentByManyIBAAndContainer(wc, map4, "com.huaqin.HQ_PRODUCT_MANAGEMENT"));
		 		} catch (Exception e) {
		 			// TODO Auto-generated catch block
		 			e.printStackTrace();
		 		}

		     	 
		     	 String oid5="VR:wt.doc.WTDocument:258581";
		     	 WTDocument wtdoc=(WTDocument) new ReferenceFactory().getReference(oid).getObject();
		     	try {
		 			DocUtil.reNumberWTDocument(wtdoc,"1001");
		 		} catch (WTPropertyVetoException e) {
		 			// TODO Auto-generated catch block
		 			e.printStackTrace();
		 		}
			
	    
	    //	String oid = "VR:wt.doc.WTDocument:258079";
	    	try{
	    	     ReferenceFactory rf1 = new ReferenceFactory();
	    	      String oid6 = rf1.getReferenceString(doc);
	    	     System.out.println( oid6);
	    		WTDocument wtdocument6 = (WTDocument) rf.getReference(oid6).getObject();
	    		System.out.println("wtdocument6.getNumber()>>>>>>>>"+wtdocument6.getNumber());
	    		
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}

	    	
	    	
			   WTDocument docRoleA = DocUtil.getDocumentByName("test06");
				WTDocument docRoleB = DocUtil.getDocumentByName("test04");
				DocUtil.associateDocToDoc(docRoleA, docRoleB);
				WTDocumentMaster docRoleC = (WTDocumentMaster)DocUtil.getDocumentByName("test05").getMaster();
				DocUtil.associateDocToDocUsageLink(docRoleA, docRoleC);
				
				WTDocumentDependencyLink wtd=getDocDependencyLink(docRoleA, docRoleB);
				System.out.println("getDocDependencyLink(docRoleA, docRoleB)>>>>>>>"+wtd);
			
				
		    WTDocument doc8 = DocUtil.getDocumentByName("test03"); 
			DocUtil.removeDependencyLink(doc8);
			
			WTDocument docRoleF = DocUtil.getDocumentByName("test03");
			WTDocument docRoleG = DocUtil.getDocumentByName("test04");
			DocUtil.associateDocToDoc(docRoleA, docRoleB);
			WTDocument docRoleH = DocUtil.getDocumentByName("test01");
			WTDocument docRoleD = DocUtil.getDocumentByName("test02");
			WTDocument docRoleE = DocUtil.getDocumentByName("test03");
			DocUtil.associateDocToDoc(docRoleF, docRoleB);
			DocUtil.associateDocToDoc(docRoleF, docRoleE);
			DocUtil.associateDocToDoc(docRoleF, docRoleD);
			DocUtil.associateDocToDoc(docRoleD, docRoleE);
			DocUtil.associateDocToDoc(docRoleG, docRoleE);
			getDependsOnWTDocuments(DocUtil.getDocumentByName("test01"));
			System.out.println("getDependsOnWTDocuments>>>>>>>>>>"+getDependsOnWTDocuments(DocUtil.getDocumentByName("test01")));
			String oid9 = "";
			
		
			/*RevisionControlled rew = () new VersionControlHelper.getVersionIdentifier(obj).getObject;
			DocUtil.getObjVersion(obj);*/
			
			
			 WTContainer container = DocUtil.getDocumentByName("test01").getContainer();
			 WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container);
			 String folderPath="/产品/";
			 Folder folder=getFolder(containerRef, folderPath);
			 System.out.println("getFolder>>>>>>>>>>"+folder);
			 
			 RevisionControlled rc = (RevisionControlled)DocUtil.getDocumentByName("test01");
			 DocUtil.getObjVersion(rc);
			 System.out.println("DocUtil.getObjVersion(rc)>>>>>>>>>"+DocUtil.getObjVersion(rc));
	    	
	    	System.out.println("getDocument>>>>>>>>>>"+getDocument("1000", "name", "state"));
			System.out.println("isDocumentExist>>>>>"+isDocumentExist("haha"));
			System.out.println("getDocumentMasterByName>>>>>"+getDocumentMasterByName("test01"));
	    	WTDocument doc10 = DocUtil.getDocumentByNumber("0000000101");
			System.out.println("getAttachement"+"&&&&&&"+getAttachement(doc10,"c"));
			System.out.println("getPrimaryContent"+"&&&&&&"+getPrimaryContent(doc10,"c"));
			System.out.println("-----------"+DocUtil.reviseWTDocument(doc10, "ddd"));
            System.out.println("getPrimaryContent>>>>>>>>>"+getPrimaryContent(doc10));
			System.out.println("getDocumentShortType>>>>>>>>"+DocUtil.getDocumentShortType(DocUtil.getDocumentByName("test01")));
			
			
			String oid11="VR:wt.doc.WTDocument:1078842";
			DocUtil.isCheckOut(oid11);
			DocUtil.checkOutWTDocument(DocUtil.getDocumentByName("test03"), "boy");
			
			//DocUtil.checkInWTDocument(DocUtil.getDocumentByName("test01"), "girl");
			
			DocUtil.getLifeCycleByWTDocument(DocUtil.getDocumentByName("test01"));
			System.out.println("getLifeCycleByWTDocument>>>>>"+DocUtil.getLifeCycleByWTDocument(DocUtil.getDocumentByName("test01")));
			
			AssociatedProcessProcessor ass = getAssociatedProcesses(getDocumentByNumber("1001"),
					WfState.OPEN_RUNNING,WTContainerRef.newWTContainerRef(getDocumentByNumber("1001").getContainer()));
			System.out.println("getAssociatedProcesses>>>>>>>>>"+ass);
			
			
			System.out.println("getDocumentByName>>>>>>>>>>"+DocUtil.getDocumentByName("test99"));
	    	
		}

	public static void main(String[] args) throws RemoteException,
			InvocationTargetException, WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				RemoteMethodServer.getDefault().invoke("test",
						DocUtil.class.getName(), null, new Class[] {},
						new Object[] {});

			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
