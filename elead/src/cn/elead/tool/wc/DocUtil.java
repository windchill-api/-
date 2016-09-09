package cn.elead.tool.wc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
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
import wt.doc.DocumentType;
import wt.doc.WTDocument;
import wt.doc.WTDocumentDependencyLink;
import wt.doc.WTDocumentHelper;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.doc.WTDocumentUsageLink;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.EPMDocumentMasterIdentity;
import wt.epm.structure.EPMDescribeLink;
import wt.fc.IdentificationObject;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.Link;
import wt.fc.ObjectReference;
import wt.fc.ObjectSetVector;
import wt.fc.ObjectVector;
import wt.fc.ObjectVectorIfc;
import wt.fc.PersistInfo;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
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
import wt.inf.team.ContainerTeamManaged;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.part.WTPartReferenceLink;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.pom.Transaction;
import wt.project.Role;
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
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamReference;
import wt.team.WfRoleHelper;
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
import wt.vc.config.ConfigHelper;
import wt.vc.config.IteratedOrderByPrimitive;
import wt.vc.config.LatestConfigSpec;
import wt.vc.config.VersionedOrderByPrimitive;
import wt.vc.struct.StructHelper;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
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
	public static WTDocument getDocument(String number, String name,String state) throws WTException {
		    WTDocument doc = null;
            if (!RemoteMethodServer.ServerFlag) {   
                try {
					return (WTDocument) RemoteMethodServer.getDefault().invoke("getDocument", PDMLinkProduct.class.getName(), null,   
					        new Class[] {String.class, String.class,String.class},   
					        new Object[] {number, name, state});
				} catch (java.rmi.RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
            } else {   
	            	boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
			
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
					SessionServerHelper.manager.setAccessEnforced(enforce);
					return doc;
            }
			return null;
			
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
						"getDocumentByNumber", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {number});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
        	boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
			if (DocUtil.isEmpty(number)&& DocUtil.isDocumentExist(number)) {
				LOGGER.error("The number is null in the method of getDocumentByNumber()");
				return null;
			}
			SessionServerHelper.manager.setAccessEnforced(enforce);
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
		WTDocument wtdocument = null;
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"getDocumentByNumberAndState", DocUtil.class.getName(),
						null, new Class[] { String.class,String.class }, new Object[] {number,state});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			if (DocUtil.isEmpty(number)) {
				LOGGER.error("The number is null in the method of getDocumentByNumberAndState.");
				return wtdocument;
			}
			if (DocUtil.isEmpty(state)) {
				LOGGER.error("The state is null in the method of getDocumentByNumberAndState.");
				return wtdocument;
			} 
			return getDocument(number, null, state);
		    }
	}

	/**
     * Judge whether the document is exist. 
     */
	public static boolean isDocumentExist(String strNumber){
    	WTDocumentMaster wtdocumentmaster = null;
        if(!StringUtils.isEmpty(strNumber)){
        	try {
        		wtdocumentmaster =  getDocumentMasterByNumber(strNumber);

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
    public static WTDocumentMaster getDocumentMasterByNumber(String docNumber) throws WTException {
    	if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (WTDocumentMaster) RemoteMethodServer.getDefault().invoke("getDocumentMasterByNumber", UserUtil.class.getName(), null,   
				        new Class[] {String.class},   
				        new Object[] {docNumber});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
			boolean accessEnforced = SessionServerHelper.manager
					.setAccessEnforced(false);
	        QuerySpec querySpec = new QuerySpec(WTDocumentMaster.class);
	        docNumber = docNumber.toUpperCase();
	        WhereExpression searchCondition = new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NUMBER, SearchCondition.EQUAL, docNumber, false);
	        querySpec.appendWhere(searchCondition,new int[] { 0 });
	        QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec)querySpec);
	        while (queryResult.hasMoreElements()) {
	            WTDocumentMaster docMaster = (WTDocumentMaster) queryResult.nextElement();
	            return docMaster;
	        }	SessionServerHelper.manager.setAccessEnforced(accessEnforced);
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
	public static WTDocument getDocumentByName(String number) throws WTException {
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
			boolean accessEnforced = SessionServerHelper.manager
					.setAccessEnforced(false);
			if(!DocUtil.isEmpty(number)&& isDocumentExist(number)){
				doc = getDocument(null, number, null);
			}else{
				LOGGER.error(" name is null  getDocumentByName");
				return null;
			}
			LOGGER.debug("param doc number is " + number);
			SessionServerHelper.manager.setAccessEnforced(accessEnforced);
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
			boolean accessEnforced = SessionServerHelper.manager
					.setAccessEnforced(false);
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
			}finally{
			SessionServerHelper.manager.setAccessEnforced(accessEnforced);
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
						"WTDocument", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean accessEnforced = SessionServerHelper.manager
					.setAccessEnforced(false);
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
			}finally{
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
			return result;
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
		WTDocument document = null;
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
			boolean accessEnforced = SessionServerHelper.manager
					.setAccessEnforced(false);
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

		
			if (qr.hasMoreElements()) {
				Object obj[] = (Object[]) qr.nextElement();
				document = (WTDocument) obj[0];
			}
			SessionServerHelper.manager.setAccessEnforced(accessEnforced);
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
						new Class[] { String.class,String.class, String.class, String.class }, 
						new Object[] {docName,containername,folder,type});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean accessEnforced = SessionServerHelper.manager
					.setAccessEnforced(false);
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
			}finally{
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
			}
			return document;
		}
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
	public static WTDocument createDoc(String number, String name,HashMap<String,Object> attributes, Object primaryContent,
			ArrayList secondaryContents, WTContainerRef containerRef) {
		WTDocument doc = null;

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
				boolean accessEnforced = SessionServerHelper.manager
						.setAccessEnforced(false);
				try {

					String docDesc = "";
					String docType = "";
					String docFolder = "";

					if (attributes != null) {
						docType = (String) attributes.get("DESCRIPTION");
						docFolder = (String) attributes.get("TYPE");
						docDesc = (String) attributes.get("FOLDER");
						
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
				}finally{
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}

				return doc;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
						new Class[] { String.class }, new Object[] {name});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
		        boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			    WTContainer obj = null;
				QuerySpec qs = new QuerySpec(WTContainer.class);
				SearchCondition sc = new SearchCondition(WTContainer.class,
						WTContainer.NAME, "=", name);
				qs.appendWhere(sc);
				// qr = PersistenceServerHelper.manager.query(qs);
				QueryResult qr = PersistenceHelper.manager.find(qs);
				while (qr.hasMoreElements()) {
					obj = (WTContainer) qr.nextElement();
				}
				SessionServerHelper.manager.setAccessEnforced(enforce);
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
		List<String> list = new ArrayList<String>();
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
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);

			QuerySpec spec = new QuerySpec(WTDocument.class);
			spec.appendSearchCondition(new SearchCondition(WTDocument.class,
					WTDocument.NUMBER, SearchCondition.EQUAL, docNum, true));

			QueryResult results = PersistenceHelper.manager.find(spec);

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
			SessionServerHelper.manager.setAccessEnforced(enforce);
			return list;
		}
	}



	/**
	 * add remarks/comment and revise WTDocument.
	 * 
	 * @param doc
	 *            WTDocumenh
	 * @return WTDocument
	 * @throws WTException
	 */
	public static WTDocument reviseWTDocument(WTDocument document,
			String comment) throws WTException {
		WTDocument wtdocument = null;
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"reviseWTDocument", DocUtil.class.getName(), null,
						new Class[] { WTDocument.class,String.class }, new Object[] {document,comment});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
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
			}finally{	
				SessionServerHelper.manager.setAccessEnforced(enforce);
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
								new Class[] { String.class, String.class }, new Object[] {states,softTypes});
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
            if (!RemoteMethodServer.ServerFlag) {   
                try {
					return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("queryWTDocument", DocUtil.class.getName(), null,   
					        new Class[] {WTOrganization.class},   
					        new Object[] {state,softType});
				} catch (java.rmi.RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
            } else {  
				
				boolean accessEnforced = SessionServerHelper.manager
								.setAccessEnforced(false);
				String[] states = (state != null && !state.trim().isEmpty()) ? new String[] { state
						.trim() } : null;
				String[] softTypes = (softType != null && !softType.trim().isEmpty()) ? new String[] { softType }
						: null;
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
           
				return queryWTDocument(states, softTypes);
            }
			return new ArrayList<WTDocument>();	
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
		
		try {   
            if (!RemoteMethodServer.ServerFlag) {   
                return (EPMDocument) RemoteMethodServer.getDefault().invoke("getEPMDocument", DocUtil.class.getName(), null,   
                        new Class[] {String.class,String.class},   
                        new Object[] {number,version});   
            } else {  
				boolean accessEnforced = SessionServerHelper.manager
						.setAccessEnforced(false);
				QueryResult result = queryEPMDocument(number, null, null, version, null);
				if (result != null && result.hasMoreElements()) {
					return (EPMDocument) result.nextElement();
				}
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
            }
		 }catch (Exception e) {   
            e.printStackTrace();   
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
		try {   
            if (!RemoteMethodServer.ServerFlag) {   
                return (EPMDocument) RemoteMethodServer.getDefault().invoke("getEPMDocumentByNumber", DocUtil.class.getName(), null,   
                        new Class[] {String.class},   
                        new Object[] {number});   
            } else {  
            	boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
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
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
            }
        }catch (Exception e) {   
		            e.printStackTrace();   
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
		try {   
            if (!RemoteMethodServer.ServerFlag) {   
                return (WTDocument) RemoteMethodServer.getDefault().invoke("getLatestDoc", DocUtil.class.getName(), null,   
                        new Class[] {WTDocumentMaster.class},   
                        new Object[] {docMaster});   
            } else {  
            	boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
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
					}finally{
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					}
			     } 
		  }
        }catch (Exception e) {   
            e.printStackTrace();   
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
		try {   
            if (!RemoteMethodServer.ServerFlag) {   
                return (EPMDocument) RemoteMethodServer.getDefault().invoke("getLatestEPMDoc", DocUtil.class.getName(), null,   
                        new Class[] {EPMDocumentMaster.class},   
                        new Object[] {epmMaster});   
            } else {  
            	boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
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
					}finally{
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					}
						}  }
        }catch (Exception e) {   
            e.printStackTrace();   
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
		try {   
            if (!RemoteMethodServer.ServerFlag) {   
                RemoteMethodServer.getDefault().invoke("renameEPMDoc", DocUtil.class.getName(), null,   
                        new Class[] {EPMDocument.class,String.class},   
                        new Object[] {epmDoc,newName});   
            } else {  
            	boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
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
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					} catch (WTException e) {
						e.printStackTrace();
						}
				    }  
				}
        }catch (Exception e) {   
            e.printStackTrace();   
        } 
		
	}

	public static boolean isEPMDocumentExist(String strNumber){
		EPMDocumentMaster epmdocmaster = null;
            if (!RemoteMethodServer.ServerFlag) {   
                try {
					return (boolean) RemoteMethodServer.getDefault().invoke("isEPMDocumentExist", DocUtil.class.getName(), null,   
					        new Class[] {String.class},   
					        new Object[] {strNumber});
				} catch (java.rmi.RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
            } else {  
            	boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		        if(!StringUtils.isEmpty(strNumber)){
		        	try {
		        		epmdocmaster = getEPMMasterByNumber(strNumber);
					} catch (WTException e) {
						LOGGER.error(">>>>>"+e);
					}
		        }
		        if (epmdocmaster != null) {
		            return true;
		        }
		        SessionServerHelper.manager.setAccessEnforced(accessEnforced);
	        }
			return false;
             
    }

    /**
     * Get EPMDocument master by EPMDocument number
     */
    public static EPMDocumentMaster getEPMMasterByNumber(String docNo) throws WTException {
        QuerySpec querySpec = new QuerySpec(EPMDocumentMaster.class);
        if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (EPMDocumentMaster) RemoteMethodServer.getDefault().invoke("getEPMMasterByNumber", DocUtil.class.getName(), null,   
				        new Class[] {String.class},   
				        new Object[] {docNo});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		        docNo = docNo.toUpperCase();
		        WhereExpression searchCondition = new SearchCondition(EPMDocumentMaster.class, EPMDocumentMaster.NUMBER, SearchCondition.EQUAL, docNo, false);
		        querySpec.appendWhere(searchCondition,new int[] { 0 });
		        QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec)querySpec);
		        while (queryResult.hasMoreElements()) {
		        	EPMDocumentMaster epmDocumentMaster = (EPMDocumentMaster) queryResult.nextElement();
		            return epmDocumentMaster;
		        } 
		        SessionServerHelper.manager.setAccessEnforced(accessEnforced);
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
		if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (String) RemoteMethodServer.getDefault().invoke("getPrimContDownloadURLByNumber", DocUtil.class.getName(), null,   
				        new Class[] {String.class},   
				        new Object[] {number});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
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
				}finally{
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
				}
        }
		return url;
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
		if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDocsByTypeAndNumberSuffix", DocUtil.class.getName(), null,   
				        new Class[] {String.class,String.class},   
				        new Object[] {type,number});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
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
							PublicUtil.queryLikeValueFormat(number));
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
				}finally{
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
				}
				}
		return result;
	}

	public static WTDocument getDocumentByNumberSuffix(String name,
			String numberSuffix) throws WTException {
		WTDocument document = null;
		if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke("getDocumentByNumberSuffix", DocUtil.class.getName(), null,   
				        new Class[] {String.class,String.class},   
				        new Object[] {name,numberSuffix});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				QuerySpec querySpec = new QuerySpec(WTDocument.class);
				querySpec.setAdvancedQueryEnabled(true);
		
				SearchCondition searchCondi = new SearchCondition(WTDocument.class,
						WTDocument.NAME, SearchCondition.EQUAL, name);
				querySpec.appendWhere(searchCondi, new int[] { 0 });
		
				querySpec.appendAnd();
		
				searchCondi = new SearchCondition(WTDocument.class, WTDocument.NUMBER,
						SearchCondition.LIKE, PublicUtil.queryLikeValueFormat(numberSuffix));
				querySpec.appendWhere(searchCondi, new int[] { 0 });
		
				QueryResult qr = PersistenceHelper.manager
						.find((StatementSpec) querySpec);
				LatestConfigSpec lcs = new LatestConfigSpec();
				qr = lcs.process(qr);
				if (qr.hasMoreElements()) {
					document = (WTDocument) qr.nextElement();
				}
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
				}
		return document;
	}

	public static List<WTDocument> getDocumentByNamePrefixByState(
			String namePrefix, String state) throws WTException {
		List<WTDocument> list = new ArrayList<WTDocument>();
		if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDocumentByNamePrefixByState", DocUtil.class.getName(), null,   
				        new Class[] {String.class,String.class},   
				        new Object[] {namePrefix,state});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				
				QuerySpec querySpec = new QuerySpec(WTDocument.class);
				querySpec.setAdvancedQueryEnabled(true);
		
				SearchCondition searchCondi = new SearchCondition(WTDocument.class,
						WTDocument.NAME, SearchCondition.LIKE,
						PublicUtil.queryLikeValueFormat(namePrefix));
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
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
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
		List<WTDocument> list = new ArrayList<WTDocument>();
		if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDocumentByNamePrefix", DocUtil.class.getName(), null,   
				        new Class[] {String.class},   
				        new Object[] {prefix});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
			if (prefix == null || prefix.equalsIgnoreCase("")) {
				LOGGER.error(" prefix is null");
				return null;
			}
			
			LOGGER.debug("param doc prefix is " + prefix);
			QuerySpec querySpec = new QuerySpec(WTDocument.class);
			querySpec.setAdvancedQueryEnabled(true);
			int[] index = { 0 };
			WhereExpression sc = new SearchCondition(WTDocument.class,
					WTDocument.NAME, SearchCondition.LIKE,
					PublicUtil.queryLikeValueFormat(prefix));
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
			SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
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
		List<WTDocument> list = new ArrayList<WTDocument>();
		if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDocumentByNamePrefix", DocUtil.class.getName(), null,   
				        new Class[] {String.class},   
				        new Object[] {suffix});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	 boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
			if (suffix == null || suffix.equalsIgnoreCase("")) {
				LOGGER.error(" suffix is null");
				return null;
			}
		
			LOGGER.debug("param doc suffix is " + suffix);
			QuerySpec querySpec = new QuerySpec(WTDocument.class);
			querySpec.setAdvancedQueryEnabled(true);
			int[] index = { 0 };
			WhereExpression sc = new SearchCondition(WTDocument.class,
					WTDocument.NUMBER, SearchCondition.LIKE,
					PublicUtil.queryLikeValueFormat(suffix) + suffix.toUpperCase());
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
			SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
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
		if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke("getDocumentByWorkItem", DocUtil.class.getName(), null,   
				        new Class[] {WorkItem.class},   
				        new Object[] {workitem});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	 boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
			Persistable persistable = workitem.getPrimaryBusinessObject()
					.getObject();
			if (persistable instanceof WTDocument) {
				document = (WTDocument) persistable;
			}
			SessionServerHelper.manager.setAccessEnforced(accessEnforced);
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
		if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (EPMDescribeLink) RemoteMethodServer.getDefault().invoke("getEPMDescribeLink", DocUtil.class.getName(), null,   
				        new Class[] {WTPart.class,EPMDocument.class},   
				        new Object[] {wtpart,epmdocument});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				QueryResult queryresult = PersistenceHelper.manager.find(
						wt.epm.structure.EPMDescribeLink.class, wtpart,
						EPMDescribeLink.DESCRIBES_ROLE, epmdocument);
				if (queryresult == null || queryresult.size() == 0) {
					return null;
				} else {
					EPMDescribeLink epmdescribelink = (EPMDescribeLink) queryresult
							.nextElement();
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					return epmdescribelink;
				}
        }
		return null;
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
		if (!RemoteMethodServer.ServerFlag) {   
            try {
				return (EPMDescribeLink) RemoteMethodServer.getDefault().invoke("getEPMDescribeLink", DocUtil.class.getName(), null,   
				        new Class[] {WTPart.class,EPMDocument.class},   
				        new Object[] {wtpart,epmdocument});
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        } else {  
        	    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		
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
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
						return null;
					} else {
						LOGGER.info("create wtpart associate EPMDoc successful");
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
						return epmdescribelink;
					}
				}
        }
		return null;
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
								new Class[] { String.class,String.class ,boolean.class }, new Object[] {typeId,state,equalFlag});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
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
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				return list;
			}
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
					String s = PersistenceHelper.manager.getNextSequence(WTDocument.class);
					int seq = Integer.parseInt(s);
					DecimalFormat format = new DecimalFormat(bitFormat);
					return format.format(seq);
				} catch (Exception ex) {
					ex.printStackTrace();
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

	public Workable undoCheckOut(Workable workable) throws WorkInProgressException, WTPropertyVetoException, PersistenceException, WTException{
				if (WorkInProgressHelper.isCheckedOut(workable)) {
					workable = WorkInProgressHelper.service.undoCheckout(workable);
				}
				return workable;
	}
		

	
	
		public static void test() throws RemoteException,InvocationTargetException, WTException {
			/*doCheckIn(DocUtil.getDocumentByNumber("0000000189"));
			doCheckOut(DocUtil.getDocumentByNumber("0000000188"));
			WTDocument doc = DocUtil.getDocumentByNumber("0000000188");
			checkOutWTDocument(doc, "0000000187");
			checkInWTDocument(DocUtil.getDocumentByNumber("0000000185"),"20160902*****");*/
			
			//System.out.println(getDefaultDocSeqNumber());
			
		/*	String docName = "bjj1";
			String containername = "PDMLinkProduct";
			String folder = "test_BJJ";
			String type = "wt.doc.WTDocument";
			System.out.println(createDoc(docName, containername, folder, type));
			
			String docName1 = "bjj1_1";
			String containername1 = "测试产品";
			String folder1 = "HH";
			String type1 = "bjj_WTDocument";
			System.out.println(createDoc(docName1, containername1, folder1, type1));*/
			/*
			System.out.println(DocUtil.getDefaultDocSeqNumber());
			String number = "111";
			String name = "bjj2";
			HashMap<String,Object> attributes =new HashMap<String,Object>();
			attributes.put("DESCRIPTION","bjj3_1");
			attributes.put("TYPE","bjj4_1");
			attributes.put("FOLDER","bjj4_1");
			Object primaryContent = 11;
			ArrayList secondaryContents=new ArrayList();        
			secondaryContents.add("bjj5");//
			WTContainerRef containerRef = WTContainerRef.newWTContainerRef(DocUtil.getWtContainerByName("测试产品"));
			WTDocument a = createDoc(null, name, attributes, primaryContent, secondaryContents, containerRef);
		    System.out.println(a);	
			
		    String number1 = "1111";
			String name1 = "bjj21";
			HashMap<String,Object> attributes1 =new HashMap<String,Object>();
			attributes1.put("DESCRIPTION","bjj3_1");
			attributes1.put("TYPE","bjj4_1");
			attributes1.put("FOLDER","bjj4_1");
			Object primaryContent1 = 11;
			ArrayList secondaryContents1=new ArrayList();        
			secondaryContents.add("bjj51");//
			WTContainerRef containerRef1 = WTContainerRef.newWTContainerRef(DocUtil.getWtContainerByName("测试产品"));
		    System.out.println(createDoc(null, name1, attributes1, primaryContent1, secondaryContents1, containerRef1));	
			*/
			/*System.out.println(getWtContainerByName("测试产品"));
			System.out.println(getWtContainerByName("测品"));*/
            System.out.println(getDocumentMasterByNumber("123456789"));
			
		}

	public static void main(String[] args) throws RemoteException,
			InvocationTargetException, WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				RemoteMethodServer server = RemoteMethodServer.getDefault();
				server.setUserName("wcadmin");
				server.setPassword("wcadmin");

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
