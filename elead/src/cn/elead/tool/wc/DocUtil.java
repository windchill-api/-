package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.doc.WTDocument;
import wt.doc.WTDocumentDependencyLink;
import wt.doc.WTDocumentHelper;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.doc.WTDocumentUsageLink;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.Link;
import wt.fc.ObjectReference;
import wt.fc.ObjectSetVector;
import wt.fc.ObjectToObjectLink;
import wt.fc.ObjectVector;
import wt.fc.PersistInfo;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.iba.definition.StringDefinition;
import wt.iba.value.StringValue;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.inf.library.WTLibrary;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.part.WTPartReferenceLink;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.pom.Transaction;
import wt.query.ClassAttribute;
import wt.query.ConstantExpression;
import wt.query.OrderBy;
import wt.query.OrderByExpression;
import wt.query.QuerySpec;
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
import wt.util.WTInvalidParameterException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.Iterated;
import wt.vc.Mastered;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.struct.StructHelper;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfState;
import wt.workflow.work.AssociatedProcessProcessor;
import wt.workflow.work.WorkItem;

import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinitionMaster;
import com.ptc.windchill.enterprise.wip.WIPUtils;

/**
 * 
 * @author bjj
 * Document util
 * 20160912
 *
 */
@SuppressWarnings("unchecked")
public class DocUtil implements RemoteAccess {
	private static String CLASSNAME = DocUtil.class.getName();
	private final static Logger logger = LogR.getLogger(DocUtil.class.getName());
	
	/**
	 * @author bjj
	 * Get Document by nummber,name and state.And all param can been set "null".
	 * 
	 * @param number
	 * @param state
	 * @return
	 * @throws WTException
	 * 
	 */
	public static List<WTDocument> getDocument(String number, String name, String state) {
		try {
			if (!RemoteMethodServer.ServerFlag) {   
				return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDocument", DocUtil.class.getName(), null,   
						new Class[] {String.class, String.class, String.class}, new Object[] {number, name, state});
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				List<WTDocument> list = new ArrayList<WTDocument>();
				WTDocument doc = null;
				try {
					QuerySpec querySpec = new QuerySpec(WTDocument.class);
					int conditionCount = 0;
					if (!StringUtils.isEmpty(number)) {
						SearchCondition searchCondi = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, number);
							querySpec.appendWhere(searchCondi, new int[] { 0 });
							conditionCount++;
						}
						if (!StringUtils.isEmpty(name)) {
							if (conditionCount > 0) {
								querySpec.appendAnd();
							}
							SearchCondition searchCondi = new SearchCondition(WTDocument.class, WTDocument.NAME, SearchCondition.EQUAL, name);
							querySpec.appendWhere(searchCondi, new int[] { 0 });
							conditionCount++;
						}
						if (!StringUtils.isEmpty(state)) {
							if (conditionCount > 0) {
								querySpec.appendAnd();
							}
							SearchCondition searchCondi = new SearchCondition(WTDocument.class, WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state);
							querySpec.appendWhere(searchCondi, new int[] { 0 });
						}
						QueryResult qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
						qr = new LatestConfigSpec().process(qr);
						while (qr.hasMoreElements()) {
							doc = (WTDocument) qr.nextElement();
							list.add(doc);
						} 
				} catch (WTException e) {
					logger.error(CLASSNAME+".getDocument:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return list;
			}
		} catch (RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

    /**
	 * getLatestDocumentByNumber
	 * @param number
	 * @return
	 * @throws WTException
	 */
	public static WTDocument getDocumentByNumber(String number) {
        try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTDocument) RemoteMethodServer.getDefault().invoke( "getDocumentByNumber", DocUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] {number});
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				WTDocument result = null;
				try {
					if (StringUtils.isNotBlank(number)) {
						QuerySpec qs = new QuerySpec(WTDocumentMaster.class);
						SearchCondition scnumber = new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
						qs.appendWhere(scnumber, new int[] { 0 });
						QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
						if (qr.hasMoreElements()) {
							WTDocumentMaster master = (WTDocumentMaster) qr.nextElement();
							qr = VersionControlHelper.service.allIterationsOf(master);
							if (qr.hasMoreElements()) {
								result = (WTDocument) qr.nextElement();
							}
						}
					}
			} catch (WTException e) {
				logger.error(CLASSNAME+".getDocumentByNumber:" + e);
			} finally{
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return result;
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) { 
			logger.error(e.getMessage(),e);
		}	
      return null;
	}

	/**
	 * Get Document by nummber and state
	 * @param number
	 * @param state
	 * @return
	 * @throws WTException
	 */
	public static List<WTDocument> getDocumentByNumberAndState(String number,String state){
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (List<WTDocument>) RemoteMethodServer.getDefault().invoke( "getDocumentByNumberAndState", DocUtil.class.getName(),
						null, new Class[] { String.class,String.class }, new Object[] {number,state});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
			    List<WTDocument> list = new ArrayList<WTDocument>();
			    try {
					if (StringUtils.isEmpty(number)) {
						return null;
					}
					if (StringUtils.isEmpty(state)) {
						return null;
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+".getDocumentByNumberAndState:" + e.getMessage());
				}finally{
					SessionServerHelper.manager.setAccessEnforced(enforce); 
				}
			    WTDocument doc = (WTDocument) getDocument(number, null, state);
			    list.add(doc);
			    return list;
			}
		} catch (RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @author bjj
     * Judge whether the document is exist. 
     * @param strNumber
     */
	public static boolean isDocumentExist(String strNumber){
		try {
			if (!RemoteMethodServer.ServerFlag) {
					 RemoteMethodServer.getDefault().invoke("isDocumentExist", DocUtil.class.getName(),
							null, new Class[] { String.class }, new Object[] { strNumber });
			} else {
				boolean flag = false;
				WTDocumentMaster wtdocumentmaster = null;
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					wtdocumentmaster =  getDocumentMasterByNumber(strNumber);
					if(!StringUtils.isEmpty(strNumber)){
		        		if (wtdocumentmaster != null) {
		 		            flag = true;
		 		        }
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+"."+"isDocumentExist"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
			    }
				return flag;
			}
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
            logger.error(e.getMessage(),e);
		}
		return false;
	}

    /**
	 * @author bjj
     * Get document master by document number
     * @param docNumber
     * return WTDocumentMaster
     */
    
    public static WTDocumentMaster getDocumentMasterByNumber(String docNumber) throws WTException {
    	try {
			if (!RemoteMethodServer.ServerFlag) {   
					return (WTDocumentMaster) RemoteMethodServer.getDefault().invoke("getDocumentMasterByNumber", DocUtil.class.getName(),
							null, new Class[] {String.class}, new Object[] {docNumber});
			} else { 
			    WTDocumentMaster docMaster = null;
				boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				try {
				    QuerySpec querySpec = new QuerySpec(WTDocumentMaster.class);
				    docNumber = docNumber.toUpperCase();
				    WhereExpression searchCondition = new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NUMBER, SearchCondition.EQUAL, docNumber, false);
				    querySpec.appendWhere(searchCondition,new int[] { 0 });
				    QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec)querySpec);
				    while (queryResult.hasMoreElements()) {
				        docMaster = (WTDocumentMaster) queryResult.nextElement();
				    }	
				} catch (WTException e) {
				}finally{
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
			    return docMaster;
			}
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
            logger.error(e.getMessage(),e);
		}
		return null;
    }

	/**
	 * @author bjj
	 * @param newDocName
	 *            String
	 * @param doc
	 *            WTDocument
	 * @return boolean
	 * @description
	 */
	public static Boolean documentRename(String newDocName, WTDocument doc) throws WTException {
 		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (Boolean) RemoteMethodServer.getDefault().invoke("documentRename", DocUtil.class.getName(), null,
				        new Class[] {String.class, WTDocument.class}, new Object[] { newDocName, doc });
			 } else {
				boolean result = false;
				boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (newDocName.equals(doc.getName())) {
						result = true;
					} else {
						WTDocumentMaster docmaster = (WTDocumentMaster) doc.getMaster();
						WTDocumentMasterIdentity docmasteridentity = (WTDocumentMasterIdentity) docmaster.getIdentificationObject();
						docmasteridentity.setName(newDocName);
						docmaster = (WTDocumentMaster) IdentityHelper.service.changeIdentity(docmaster, docmasteridentity);
						String newName = docmaster.getName();
						if (newName.equals(newDocName)) {
							result = true;
						}
					}
				} catch (WTPropertyVetoException e) {
					logger.error(CLASSNAME+"."+"documentRename"+":"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
				return result;
			}
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
            logger.error(e.getMessage(),e);
		}  
         return false;
	}

	/**
	 * Get document by number version and iteration
	 * @param number
	 * @param version
	 * @param iteration
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static WTDocument getDocumentByNumberByVersionByIteration(String number, String version, String iteration) throws WTException {
        try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTDocument) RemoteMethodServer.getDefault().invoke("getDocumentByNumberByVersionByIteration", DocUtil.class.getName(), 
						null, new Class[] { String.class }, new Object[] { number, version ,iteration });
			} else {
				 WTDocument document = null;
				 boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				 try {
					QuerySpec query = new QuerySpec();
					query.setAdvancedQueryEnabled(true);
					int docTable = query.appendClassList(WTDocument.class, true);
					int docMasterTable = query.appendClassList(WTDocumentMaster.class, false);
					String[] aliases = new String[2];
					aliases[0] = query.getFromClause().getAliasAt(docTable);
					aliases[1] = query.getFromClause().getAliasAt(docMasterTable);
					TableColumn sVersion = new TableColumn(aliases[0], "VERSIONIDA2VERSIONINFO");
					TableColumn sIter = new TableColumn(aliases[0], "ITERATIONIDA2ITERATIONINFO");
					TableColumn iDA3MASTERREFERENCE = new TableColumn(aliases[0], "IDA3MASTERREFERENCE");
					TableColumn numberCol = new TableColumn(aliases[1],	"WTDOCUMENTNUMBER");
					TableColumn iDA2A2Col = new TableColumn(aliases[1], "IDA2A2");
					query.appendWhere(new SearchCondition(numberCol, SearchCondition.EQUAL, new ConstantExpression(number.toUpperCase())));
					query.appendAnd();
					query.appendWhere(new SearchCondition(iDA2A2Col, SearchCondition.EQUAL, iDA3MASTERREFERENCE));
					query.appendAnd();
					query.appendWhere(new SearchCondition(sVersion, SearchCondition.EQUAL, new ConstantExpression(version)));
					query.appendAnd();
					query.appendWhere(new SearchCondition(sIter, SearchCondition.EQUAL, new ConstantExpression(iteration)));
					QueryResult qr = PersistenceHelper.manager.find((StatementSpec) query);
					if (qr.hasMoreElements()) {
						Object obj[] = (Object[]) qr.nextElement();
						document = (WTDocument) obj[0];
					}
			    } catch (WTException e) {
			    	logger.error(CLASSNAME+"."+"getDocumentByNumberByVersionByIteration"+":"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
				return document;
		     }
		} catch (RemoteException e) {
			 logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			 logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @author bjj
	 * create wtdocument in windchill by name, container,folder and type type
	 * default is WTDocument, folder default value is Default, name and
	 * container must has value.
	 * Only used to add in PDMLinkProduct and WTLibrary.
	 * @param strCsv
	 * @param strExcel
	 * @param docName
	 * @param designLab
	 * @return
	 * @throws WTException
	 */
	public static WTDocument createDoc(String docName, String containername, String folder, String type) throws WTException {
	    try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTDocument) RemoteMethodServer.getDefault().invoke("createDoc", DocUtil.class.getName(), null,
					new Class[] { String.class, String.class, String.class, String.class }, new Object[] {docName, containername, folder, type});
			} else {
					boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
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
							typeDefinitionRef = TypedUtility.getTypeDefinitionReference("wt.doc.WTDocument");
						} else {
							typeDefinitionRef = TypedUtility.getTypeDefinitionReference(type);
						}
						try {
							document.setTypeDefinitionReference(typeDefinitionRef);
							document.setName(docName);
							document.setNumber(docName.toUpperCase());
						} catch (WTPropertyVetoException e) {
					        logger.error(CLASSNAME+"."+"createDoc"+":"+e);
							e.printStackTrace();
						}
						WTContainer container = WCUtil.getWtContainerByName(containername);
						Folder docFold = null;
						if (folder == null || "".equals(folder)) {
						    docFold = FolderHelper.service.getFolder("Default", WTContainerRef.newWTContainerRef(container));
						} else {
							docFold = FolderHelper.service.getFolder("Default/" + folder, WTContainerRef.newWTContainerRef(container));
						}
						FolderHelper.assignLocation((FolderEntry) document, docFold);
						PersistenceHelper.manager.save(document);
					} catch (WTException e) {
				        logger.error(CLASSNAME+"."+"createDoc"+":"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					}
					return document;
			    }
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
	    return null;
	}

	/**
	 * @author bjj
	 * add remarks/comment and revise WTDocument.
	 * @param document
	 *  @param comment
	 * @return WTDocument
	 * @throws WTException
	 */
	public static WTDocument reviseWTDocument(WTDocument document, String comment) throws WTException {
			try {
				if (!RemoteMethodServer.ServerFlag) {
					return (WTDocument) RemoteMethodServer.getDefault().invoke("reviseWTDocument", DocUtil.class.getName(), null,
							new Class[] { WTDocument.class,String.class }, new Object[] {document,comment});
				  } else {
					  boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
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
						} catch (WTPropertyVetoException e) {
							logger.error(CLASSNAME+".reviseWTDocument:"+e);
						}finally{
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}
				    	return wtdocument;
				    }
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return null;
	}

	/**
	 * @author bjj
	 * Queries WTDocument objects.
	 * @param states
	 *            The internal name of life cycle states, e.g.
	 *            PRODUCTIONRELEASED.
	 * @param softTypes
	 *            The soft types of WTDocument objects, e.g.
	 *            wt.doc.WTDocument|com.ptc.InterCommData
	 * @return A list of WTDocument objects.
	 * @throws WTException
	 *             Failed to query WTDocument objects with the given parameters.
	 * 
	 */
	public static List<WTDocument> queryWTDocument(String[] states, String[] softTypes) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("queryWTDocument", DocUtil.class.getName(), 
					null, new Class[] { String[].class,String[].class }, new Object[] {states, softTypes});
			} else {
				List<WTDocument> result = new ArrayList<WTDocument>();
				if ((states == null || states.length == 0) && (softTypes == null || softTypes.length == 0)) {
					return result;
				}
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					QuerySpec qs = new QuerySpec();
					int indexWTDocument = qs.appendClassList(WTDocument.class, true);
					WhereExpression where = null;
					if (softTypes != null && softTypes.length > 0) {
						int indexWTTypeDefinition = qs.appendClassList(WTTypeDefinition.class, false);
						int indexWTTypeDefinitionMaster = qs.appendClassList(WTTypeDefinitionMaster.class, false);
						where = new SearchCondition(new ClassAttribute(WTDocument.class, "typeDefinitionReference.key.id"),
								SearchCondition.EQUAL, new ClassAttribute(WTTypeDefinition.class, "thePersistInfo.theObjectIdentifier.id"));
						qs.appendWhere(where, new int[] { indexWTDocument, indexWTTypeDefinition });
				
						where = new SearchCondition(new ClassAttribute( WTTypeDefinition.class,
								"masterReference.key.id"), SearchCondition.EQUAL, new ClassAttribute
								( WTTypeDefinitionMaster.class, "thePersistInfo.theObjectIdentifier.id"));
						qs.appendAnd();
						qs.appendWhere(where, new int[] { indexWTTypeDefinition, indexWTTypeDefinitionMaster });
						if (softTypes.length == 1) {
							where = new SearchCondition( WTTypeDefinitionMaster.class, "intHid", SearchCondition.EQUAL, softTypes[0]);
						} else {
							where = new SearchCondition( WTTypeDefinitionMaster.class, "intHid", softTypes, true);
						}
						qs.appendAnd();
						qs.appendWhere(where, new int[] { indexWTTypeDefinitionMaster });
					}
					// Specifies the life cycle state of target object.
					if (states != null && states.length > 0) {
						if (states.length == 1) {
							where = new SearchCondition(WTDocument.class, "state.state", SearchCondition.EQUAL, states[0]);
						} else {
							where = new SearchCondition(WTDocument.class, "state.state", states, true);
						}
						if (qs.getConditionCount() > 0 && qs.getWhere().endsWith(")")) {
							qs.appendAnd();
						}
						qs.appendWhere(where, new int[] { indexWTDocument });
					}
					// Sorted by modifying time in descending order.
					OrderBy orderby = new OrderBy(new ClassAttribute(WTDocument.class, "thePersistInfo.modifyStamp"), true);
					qs.appendOrderBy(orderby, new int[] { indexWTDocument });
					QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
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
		} catch (WTException e) {
			logger.error(CLASSNAME+"."+"queryWTDocument"+":"+e);
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
		return result;
    }
} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @author bjj
	 * Queries WTDocument objects.
	 * @param state
	 *            The internal name of life cycle states, e.g.
	 *            PRODUCTIONRELEASED.
	 * @param softType
	 *            The soft types of WTDocument objects, e.g.
	 *            wt.doc.WTDocument|com.ptc.InterCommData
	 * @return A list of WTDocument objects.
	 * @throws WTException
	 *             Failed to query WTDocument objects with the given parameters.
	 * 
	 */
	public static List<WTDocument> queryWTDocument(String state, String softType) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {   
			    return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("queryWTDocument", DocUtil.class.getName(), null,   
			            new Class[] { String.class, String.class }, new Object[] { state, softType});   
			} else {  
				boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				String[] states = null;
				String[] softTypes = null;
				try {
					states = (state != null && !state.trim().isEmpty()) ? new String[] { state.trim() } : null;
					softTypes = (softType != null && !softType.trim().isEmpty()) ? new String[] { softType }: null;
				} finally {
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
				return queryWTDocument(states, softTypes);
			}
		} catch (java.rmi.RemoteException e ) {
			logger.error(e.getMessage(),e);
		} catch(InvocationTargetException e){
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @author bjj
	 * Gets the latest WTDocument object by WTDocumentMaster.
	 * @param docMaster
	 *            the given WTDocumentMaster object.
	 * @return the latest WTDocument object of the given WTDocumentMaster.
	 * @throws WTException
	 *             Failed to get the latest WTDocument object by
	 *             WTDocumentMaster.
	 */
	public static WTDocument getLatestDoc(WTDocumentMaster docMaster) throws WTException {
		WTDocument doc = null;
		try {   
            if (!RemoteMethodServer.ServerFlag) {   
                return (WTDocument) RemoteMethodServer.getDefault().invoke("getLatestDoc", DocUtil.class.getName(), null,   
                        new Class[] {WTDocumentMaster.class}, new Object[] {docMaster});   
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
						logger.error(CLASSNAME+"."+"getLatestDoc"+":"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					}
			     } 
				return doc;
		  }
        }catch (Exception e) {   
			logger.error(e.getMessage(),e);
        } 
		return null;
	}

	/**
	 * Get WTDocument By Type and number
	 * @author bjj
	 * @param type
	 * @param number
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings({ "deprecation" })
	public static List<WTDocument> getDocsByTypeAndNumberSuffix(String type, String number) throws WTException {
		try {
			List<WTDocument> result = new ArrayList<WTDocument>();
			if (!RemoteMethodServer.ServerFlag) {   
					return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDocsByTypeAndNumberSuffix", DocUtil.class.getName(), null,   
					        new Class[] {String.class,String.class}, new Object[] {type,number});
			} else {  
				    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
					try {
						QuerySpec queryspec = new QuerySpec(WTDocument.class);
						TypeDefinitionReference clientType = ClientTypedUtility.getTypeDefinitionReference(type);
						SearchCondition searchCondition = new SearchCondition(
								WTDocument.class, Typed.TYPE_DEFINITION_REFERENCE + "." + TypeDefinitionReference.KEY + "." + TypeDefinitionForeignKey.BRANCH_ID,
								SearchCondition.EQUAL, clientType.getKey().getBranchId());
						queryspec.appendWhere(searchCondition, new int[] { 0 });
						queryspec.appendAnd();
						searchCondition = new SearchCondition(WTDocument.class,WTDocument.NUMBER, SearchCondition.LIKE,
								WCUtil.queryLikeValueFormat(number));
						queryspec.appendWhere(searchCondition);
						logger.debug("sql ====" + queryspec.toString());
						QueryResult queryresult = PersistenceServerHelper.manager.query(queryspec);
						queryresult = new LatestConfigSpec().process(queryresult);
						while (queryresult.hasMoreElements()) {
							Object object = queryresult.nextElement();
							if (object instanceof WTDocument) {
								WTDocument doc = (WTDocument) object;
								result.add(doc);
							}
						}
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"getDocsByTypeAndNumberSuffix"+":"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
					}
					return result;
					}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @author bjj
	 * get Document By Suffix Number
	 * @param name
	 * @param numberSuffix
	 * @return
	 * @throws WTException
	 */
	public static WTDocument getDocumentByNumberSuffix(String name, String numberSuffix) throws WTException {
		try {
			WTDocument document = null;
			if (!RemoteMethodServer.ServerFlag) {   
					return (WTDocument) RemoteMethodServer.getDefault().invoke("getDocumentByNumberSuffix", DocUtil.class.getName(), null,   
					        new Class[] {String.class, String.class}, new Object[] {name, numberSuffix});
			} else {  
				    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
					try {
						QuerySpec querySpec = new QuerySpec(WTDocument.class);
						querySpec.setAdvancedQueryEnabled(true);
				
						SearchCondition searchCondi = new SearchCondition(WTDocument.class, WTDocument.NAME, SearchCondition.EQUAL, name);
						querySpec.appendWhere(searchCondi, new int[] { 0 });
						querySpec.appendAnd();
						searchCondi = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.LIKE,
								WCUtil.queryLikeValueFormat(numberSuffix));
						querySpec.appendWhere(searchCondi, new int[] { 0 });
				
						QueryResult qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
						LatestConfigSpec lcs = new LatestConfigSpec();
						qr = lcs.process(qr);
						if (qr.hasMoreElements()) {
							document = (WTDocument) qr.nextElement();
						}
					}catch (Exception e) {
						logger.error(CLASSNAME+"."+"getDocumentByNumberSuffix"+":"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
					}
					return document;
				}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}


	/**
	 * @author bjj
	 * get Document By NamePrefix and By State
	 * @param namePrefix
	 * @param state
	 * @return
	 * @throws WTException
	 */
	public static List<WTDocument> getDocumentByNamePrefixByState( String namePrefix, String state) throws WTException {
		try {
			List<WTDocument> list = new ArrayList<WTDocument>();
			if (!RemoteMethodServer.ServerFlag) {   
					return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDocumentByNamePrefixByState", DocUtil.class.getName(), null,   
					        new Class[] {String.class, String.class}, new Object[] {namePrefix,state});
			} else {  
				    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
					try {
						QuerySpec querySpec = new QuerySpec(WTDocument.class);
						querySpec.setAdvancedQueryEnabled(true);
						SearchCondition searchCondi = new SearchCondition(WTDocument.class, WTDocument.NAME, SearchCondition.LIKE, 
								WCUtil.queryLikeValueFormat(namePrefix));
						querySpec.appendWhere(searchCondi, new int[] { 0 });
						if (state != null && !"".equals(state)) {
							querySpec.appendAnd();
							searchCondi = new SearchCondition(WTDocument.class, WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state);
							querySpec.appendWhere(searchCondi, new int[] { 0 });
						}
						QueryResult qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
						LatestConfigSpec lcs = new LatestConfigSpec();
						qr = lcs.process(qr);
						while (qr.hasMoreElements()) {
							WTDocument document = (WTDocument) qr.nextElement();
							list.add(document);
						}
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"getDocumentByNamePrefixByState"+":"+e);
					}finally{
  					    SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
					}
					return list;
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @author bjj
	 * get latest WTDocuments by name prefix
	 * @param prefix
	 * @return QueryResult
	 * @throws WTException
	 */
	public static List<WTDocument> getDocumentByNamePrefix(String prefix) throws WTException {
		try {
			List<WTDocument> list = new ArrayList<WTDocument>();
			if (!RemoteMethodServer.ServerFlag) {   
					return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDocumentByNamePrefix", DocUtil.class.getName(), null,   
					        new Class[] {String.class}, new Object[] {prefix});
			} else {  
				    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				    try {
						if (prefix == null || prefix.equalsIgnoreCase("")) {
							return null;
						}
						QuerySpec querySpec = new QuerySpec(WTDocument.class);
						querySpec.setAdvancedQueryEnabled(true);
						int[] index = { 0 };
						WhereExpression sc = new SearchCondition(WTDocument.class, WTDocument.NAME, SearchCondition.LIKE,
								WCUtil.queryLikeValueFormat(prefix));
						querySpec.appendWhere(sc, index);
						QueryResult qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
						// get latest version document
						LatestConfigSpec lcs = new LatestConfigSpec();
						qr = lcs.process(qr);
						while (qr.hasMoreElements()) {
							WTDocument document = (WTDocument) qr.nextElement();
							list.add(document);
						}
						
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"getDocumentByNamePrefix"+":"+e);
					} finally {
				        SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
					}
				return list;
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	
	}

	/**
	 * @author bjj
	 * get latest WTDocuments by number suffix
	 * @param suffix
	 *            document prefix
	 * @return QueryResult
	 * @throws WTException
	 */
	public static List<WTDocument> getDocumentByNumberSuffix(String suffix) throws WTException {
		try {
			List<WTDocument> list = new ArrayList<WTDocument>();
			if (!RemoteMethodServer.ServerFlag) {   
					return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDocumentByNamePrefix", DocUtil.class.getName(), null,   
					        new Class[] {String.class}, new Object[] {suffix});
			} else {  
				boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (suffix == null || suffix.equalsIgnoreCase("")) {
						return null;
					}
					QuerySpec querySpec = new QuerySpec(WTDocument.class);
					querySpec.setAdvancedQueryEnabled(true);
					int[] index = { 0 };
					WhereExpression sc = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.LIKE,
							WCUtil.queryLikeValueFormat(suffix) + suffix.toUpperCase());
					querySpec.appendWhere(sc, index);
					ClassAttribute clsAttr = new ClassAttribute(WTDocument.class, WTDocument.MODIFY_TIMESTAMP);
					OrderBy order = new OrderBy((OrderByExpression) clsAttr, true);
					querySpec.appendOrderBy(order, new int[] { 0 });
					QueryResult qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
					LatestConfigSpec lcs = new LatestConfigSpec();
					qr = lcs.process(qr);
					while (qr.hasMoreElements()) {
						WTDocument document = (WTDocument) qr.nextElement();
						list.add(document);
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getDocumentByNumberSuffix"+":"+e);
				} finally {
			        SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
				}
				return list;
			    }
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;

	}

	/**
	 * @author bjj
	 * Get WTDocument by WorkItem
	 * @param workitem
	 * @return
	 */
	public static WTDocument getDocumentByWorkItem(WorkItem workitem) {
		try {
			WTDocument document = null;
			if (!RemoteMethodServer.ServerFlag) {   
			    	return (WTDocument) RemoteMethodServer.getDefault().invoke("getDocumentByWorkItem", DocUtil.class.getName(), null,   
					        new Class[] {WorkItem.class}, new Object[] {workitem});
			} else { 
			    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
			    try {
					Persistable persistable = workitem.getPrimaryBusinessObject().getObject();
					if (persistable instanceof WTDocument) {
						document = (WTDocument) persistable;
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getDocumentByNumberSuffix"+":"+e);
				} finally {
			        SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
				}
			    return document;
		    }
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		} catch (WTRuntimeException e) {
			logger.error(e.getMessage(),e);
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
	public static List<WTDocument> getLatestDocumentListByTypeAndState( String typeId, String state, boolean equalFlag) throws WTException,	Exception {
		try {
			if (!RemoteMethodServer.ServerFlag) {   
			    	return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getLatestDocumentListByTypeAndState",
									DocUtil.class.getName(), null, new Class[] { String.class }, new Object[] {typeId});
			} else {  
				    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
					List<WTDocument> list = new ArrayList<WTDocument>();
					int[] zero = { 0 };
					QuerySpec qs = new QuerySpec(WTDocument.class);
					TypeDefinitionReference tdr;
					try {
						tdr = ClientTypedUtility.getTypeDefinitionReference(typeId);
						qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.TYPE_DEFINITION_REFERENCE + "." + TypeDefinitionReference
								.KEY + "." + TypeDefinitionForeignKey.BRANCH_ID, SearchCondition.EQUAL, tdr.getKey().getBranchId()), zero);
						if (qs.getConditionCount() > 0) {
							qs.appendAnd();
						}
						if (equalFlag) {
							qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state), zero);
						} else {
							qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.LIFE_CYCLE_STATE, SearchCondition.NOT_EQUAL, state), zero);
						}
						if (qs.getConditionCount() > 0) {
							qs.appendAnd();
						}
						qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE), zero);
						qs = new LatestConfigSpec().appendSearchCriteria(qs);
						QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
						qr = new LatestConfigSpec().process(qr);
						while (qr.hasMoreElements()) {
							WTDocument doc = (WTDocument) qr.nextElement();
							list.add(doc);
						}
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"getLatestDocumentListByTypeAndState"+":"+e);
					} finally {
				        SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
					}
					return list;
				}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * get latest document list by doctype
	 * 
	 * @param typeId
	 * @return
	 * @throws WTException
	 * @throws Exception
	 */
	public static List<WTDocument> getLatestDocumentListByType(String typeId) throws WTException, Exception {
		List<WTDocument> list = new ArrayList<WTDocument>();
		 try{
		        if (!RemoteMethodServer.ServerFlag) {
		        	return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getLatestDocumentListByType",
									DocUtil.class.getName(), null, new Class[] { String.class }, new Object[] {typeId});
		} else {
			    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				int[] zero = { 0 };
				QuerySpec qs = new QuerySpec(WTDocument.class);
				TypeDefinitionReference tdr;
				try {
					tdr = ClientTypedUtility.getTypeDefinitionReference(typeId);
					qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.TYPE_DEFINITION_REFERENCE + "."
									+ TypeDefinitionReference.KEY + "." + TypeDefinitionForeignKey.BRANCH_ID,
							SearchCondition.EQUAL, tdr.getKey().getBranchId()), zero);
					if (qs.getConditionCount() > 0) {
						qs.appendAnd();
					}
					qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE), zero);
					qs = new LatestConfigSpec().appendSearchCriteria(qs);
					QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
					qr = new LatestConfigSpec().process(qr);
					while (qr.hasMoreElements()) {
						WTDocument doc = (WTDocument) qr.nextElement();
						list.add(doc);
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getLatestDocumentListByType"+":"+e);
				} finally {
			        SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
				}
				return list;
		    }
	        } catch (RemoteException e) {
	            logger.error(e.getMessage(),e);
	        } catch (InvocationTargetException e) {
	        	logger.error(e.getMessage(),e);
	        }
	        return null;
	}

	/**
	 * @author bjj
	 * Query latest WTDocuments obj by IBA and container
	 * @param
	 *
	 * @return QueryResult
	 * @throws WTException
	 */
	public static QueryResult getDocumentByIBAAndContainer(WTContainer con, String ibaName, String ibaVaue) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
							return (QueryResult) RemoteMethodServer.getDefault().invoke("getDocumentByIBAAndContainer",DocUtil.class.getName(),
									null, new Class[] {WTContainer.class,String.class, String.class }, new Object[] {con,ibaName,ibaVaue});
			} else {
				    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
					QuerySpec querySpec = new QuerySpec();
				    QueryResult qr = null;
				    try {
						if (con == null || ibaName == null || "".equalsIgnoreCase(ibaName) || ibaVaue == null || "".equalsIgnoreCase(ibaVaue)) {
							return null;
						}
						int docIndex = querySpec.appendClassList(WTDocument.class, true);
						int ibaStringValueIndex = querySpec.appendClassList(StringValue.class, false);
						int ibaStringDefinitionIndex = querySpec.appendClassList( StringDefinition.class, false);
						int pdmIndex = 1;
						querySpec.setAdvancedQueryEnabled(true);
						WhereExpression pdm = null;
						if (con instanceof WTLibrary) {
							pdmIndex = querySpec.appendClassList(WTLibrary.class, false);
				
						} else if (con instanceof PDMLinkProduct) {
							pdmIndex = querySpec.appendClassList(PDMLinkProduct.class, false);
						}
						if (con instanceof WTLibrary) {
							pdm = new SearchCondition(WTLibrary.class, WTLibrary.NAME, SearchCondition.EQUAL, con.getName());
							SearchCondition scon = new SearchCondition(WTDocument.class, "containerReference.key.id", WTLibrary.class,
									"thePersistInfo.theObjectIdentifier.id");
							querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex });
						} else if (con instanceof PDMLinkProduct) {
							pdm = new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME, SearchCondition.EQUAL, con.getName());
							SearchCondition scon = new SearchCondition(WTDocument.class, "containerReference.key.id", PDMLinkProduct.class,
									"thePersistInfo.theObjectIdentifier.id");
							querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex });
						}
						if (pdm != null) {
							querySpec.appendWhere(pdm, new int[] { pdmIndex });
							querySpec.appendAnd();
						}
						// IBA Name condition
						ibaName = ibaName.toUpperCase();
						ClassAttribute caIbaName = new ClassAttribute(StringDefinition.class, StringDefinition.NAME);
						SearchCondition scStringDefinitionName = new SearchCondition( SQLFunction.newSQLFunction(SQLFunction.UPPER, caIbaName),
								SearchCondition.EQUAL, new ConstantExpression((Object) ibaName.toUpperCase()));
						querySpec.appendWhere(scStringDefinitionName, new int[] { ibaStringDefinitionIndex });
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
								StringValue.class, "definitionReference.key.id", StringDefinition.class, WTAttributeNameIfc.ID_NAME);
						querySpec.appendWhere(scJoinStringValueStringDefinition, new int[] { ibaStringValueIndex, ibaStringDefinitionIndex });
						querySpec.appendAnd();
						// Document and StringValue condition
						SearchCondition scStringValueDoc = new SearchCondition( StringValue.class, "theIBAHolderReference.key.id",
								WTDocument.class, WTAttributeNameIfc.ID_NAME);
						querySpec.appendWhere(scStringValueDoc, new int[] { ibaStringValueIndex, docIndex });
						querySpec.appendAnd();
				
						querySpec.appendWhere(new SearchCondition(WTDocument.class, WTDocument.LATEST_ITERATION, 
								SearchCondition.IS_TRUE), new int[] { docIndex });
						querySpec.appendAnd();
				
						querySpec.appendWhere(new SearchCondition(WTDocument.class,
								WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL, State.RELEASED), new int[] { docIndex });
				
						logger.debug("QuerySpec=" + querySpec);
						qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
						
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"getDocumentByIBAAndContainer"+":"+e);
					} finally {
				        SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
					}
					return qr;
			        }
		} catch (java.rmi.RemoteException e) {
        	logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
        return null;
	}

	/**
	 * @author bjj
	 * Query latest WTDocuments by number prefix and WTContainer
	 * @param con
	 *            con, String prefix
	 * @return QueryResult
	 * @throws WTException
	 */
	public static QueryResult getDocumentByNumberPrefixAndContainer(WTContainer con, String prefix) throws WTException {
        try {
			if (!RemoteMethodServer.ServerFlag) {
				return (QueryResult) RemoteMethodServer.getDefault().invoke("getDocumentByIBAAndContainer", DocUtil.class.getName(), null,
							new Class[] {WTContainer.class,String.class}, new Object[] {con,prefix});
           } else {
				boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				if (prefix == null || prefix.equalsIgnoreCase("")) {
			        return null;
		        }
				QuerySpec querySpec = new QuerySpec();
				QueryResult qr = null;
				try {
					int docIndex = querySpec.appendClassList(WTDocument.class, true);
					int pdmIndex = 1;
					if (con instanceof WTLibrary) {
						pdmIndex = querySpec.appendClassList(WTLibrary.class, false);
					} else if (con instanceof PDMLinkProduct) {
						pdmIndex = querySpec.appendClassList(PDMLinkProduct.class, false);
					}
					querySpec.setAdvancedQueryEnabled(true);
					WhereExpression doc = new SearchCondition(WTDocument.class,
							WTDocument.NUMBER, SearchCondition.LIKE, WCUtil.sqlLikeValueEncode(prefix));
					querySpec.appendWhere(doc, new int[] { docIndex });
					WhereExpression pdm = null;
					if (con instanceof WTLibrary) {
						querySpec.appendAnd();
						pdm = new SearchCondition(WTLibrary.class, WTLibrary.NAME, SearchCondition.EQUAL, con.getName());
						SearchCondition scon = new SearchCondition(WTDocument.class,
								"containerReference.key.id", WTLibrary.class, "thePersistInfo.theObjectIdentifier.id");
						querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex });
					} else if (con instanceof PDMLinkProduct) {
						querySpec.appendAnd();
						pdm = new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME, SearchCondition.EQUAL, con.getName());
						SearchCondition scon = new SearchCondition(WTDocument.class,
								"containerReference.key.id", PDMLinkProduct.class, "thePersistInfo.theObjectIdentifier.id");
						querySpec.appendWhere(scon, new int[] { docIndex, pdmIndex });
					}
					if (pdm != null) {
						querySpec.appendAnd();
						querySpec.appendWhere(pdm, new int[] { pdmIndex });
					}
		
					querySpec.appendAnd();
					querySpec.appendWhere(new SearchCondition(WTDocument.class,
							WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE), new int[] { docIndex });
		
					querySpec.appendAnd();
					querySpec.appendWhere(new SearchCondition(WTDocument.class,
							WTDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL, State.RELEASED), new int[] { docIndex });
		
					qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getDocumentByNumberPrefixAndContainer"+":"+e);
				} finally {
			        SessionServerHelper.manager.setAccessEnforced(accessEnforced);	
				}
				return qr;
			}
		} catch (java.rmi.RemoteException e) {
        	logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
	    return null;
	}

	/**
	 * @author bjj
	 * renNumber the wtdocument
	 * @param document
	 * @param newNumber
	 * @throws WTException
	 */
	public static void reNumberWTDocument(WTDocument document, String newNumber) throws WTException, WTPropertyVetoException {
        try {
			if (!RemoteMethodServer.ServerFlag) {
					RemoteMethodServer.getDefault().invoke("reNumberWTDocument", DocUtil.class.getName(), null, new Class[] {
						WTDocument.class,String.class}, new Object[] { document,newNumber});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);	
				try {
					Identified aIdentified = (Identified) document.getMaster();
					WTDocumentMasterIdentity aWTDocumentMasterIdentity;
					aWTDocumentMasterIdentity = (WTDocumentMasterIdentity) aIdentified.getIdentificationObject();
					aWTDocumentMasterIdentity.setNumber(newNumber);
					IdentityHelper.service.changeIdentity(aIdentified,
							aWTDocumentMasterIdentity);
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"reNumberWTDocument"+":"+e);
				} finally {
			        SessionServerHelper.manager.setAccessEnforced(enforce);	
				}
			}
		} catch (java.rmi.RemoteException e) {
        	logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
	}

	/**
	 * @author bjj
	 * To create the dependency Link  between document and document.
	 * @param docRoleA
	 * @param docRoleB
	 * @throws WTException
	 */
	public static WTDocumentDependencyLink associateDocToDoc( WTDocument docRoleA, WTDocument docRoleB) throws WTException {
		try {
			WTDocumentDependencyLink wtdocdependencylink = null;
			if (!RemoteMethodServer.ServerFlag) {
					return (WTDocumentDependencyLink) RemoteMethodServer.getDefault().invoke("associateDocToDoc", DocUtil.class.getName(),
									null, new Class[] { WTDocument.class, WTDocument.class }, new Object[] { docRoleA, docRoleB });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				QueryResult queryresult = PersistenceHelper.manager.find( WTDocumentDependencyLink.class, docRoleA, WTDocumentDependencyLink.DESCRIBES_ROLE, docRoleB);
				try{
					if (queryresult == null || queryresult.size() == 0) {
						wtdocdependencylink = WTDocumentDependencyLink.newWTDocumentDependencyLink(docRoleA, docRoleB);
						PersistenceServerHelper.manager.insert(wtdocdependencylink);
						wtdocdependencylink = (WTDocumentDependencyLink) PersistenceHelper.manager.refresh(wtdocdependencylink);
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"associateDocToDoc"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return wtdocdependencylink;
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @author bjj
	 * To create the Usage Link  between document and document.
	 * @param docRoleA
	 * @param docRoleB
	 * @throws WTException
	 */
	public static WTDocumentUsageLink associateDocToDocUsageLink( WTDocument docRoleA, WTDocumentMaster docRoleB) throws WTException {
		WTDocumentUsageLink result = null;
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (WTDocumentUsageLink) RemoteMethodServer.getDefault().invoke( "associateDocToDocUsageLink", DocUtil.class.getName(),
									null, new Class[] { WTDocument.class, WTDocument.class }, new Object[] { docRoleA, docRoleB });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					QueryResult queryresult = PersistenceHelper.manager.find( WTDocumentUsageLink.class, docRoleA,
							WTDocumentUsageLink.USES_ROLE, docRoleB );
					if (queryresult == null || queryresult.size() == 0) {
						WTDocumentUsageLink wtdocumentusagelink = WTDocumentUsageLink.newWTDocumentUsageLink(docRoleA, docRoleB);
						PersistenceServerHelper.manager.insert(wtdocumentusagelink);
						wtdocumentusagelink = (WTDocumentUsageLink) PersistenceHelper.manager.refresh(wtdocumentusagelink);
						result = wtdocumentusagelink;
					} 
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"associateDocToDocUsageLink"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			return result;
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
   /**
    * @author bjj
	 * To determine whether already exists a dependency relationship between documents.
	 * @param docA
	 * @param docB
	 * @return
	 * @throws WTException
	 */
	public static WTDocumentDependencyLink getDocDependencyLink(WTDocument docA, WTDocument docB) throws WTException {
		try {
			WTDocumentDependencyLink link = null;
			if (!RemoteMethodServer.ServerFlag) {
						return (WTDocumentDependencyLink) RemoteMethodServer.getDefault().invoke( "getDocDependencyLink", DocUtil.class.getName(),
								null, new Class[] { WTDocument.class, WTDocument.class }, new Object[] { docA, docA });
				} else {
					  boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					  try {
						  if (docA != null && docB != null){
							QueryResult queryresult = PersistenceServerHelper.manager.query(WTDocumentDependencyLink.class, docA,
									WTDocumentDependencyLink.DESCRIBES_ROLE, docB);
							if (queryresult.hasMoreElements()) {
								link = (WTDocumentDependencyLink) queryresult.nextElement();
							}
					      }
					 } catch (Exception e) {
						logger.error(CLASSNAME+"."+"getDocDependencyLink"+":"+e);
					 } finally {
						SessionServerHelper.manager.setAccessEnforced(enforce);
					 }
			    return link;
			  }
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @author bjj
	 * remove Dependency Link
	 * 
	 * @param doc
	 * @throws WTException
	 */
	public static void removeDependencyLink(WTDocument doc) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					  RemoteMethodServer.getDefault().invoke( "removeDependencyLink", DocUtil.class.getName(), null,
							new Class[] { String.class }, new Object[] {});
				} else {
					boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					try {
						if (doc == null){
							return;
						}
						QuerySpec queryspec = new QuerySpec(WTDocumentDependencyLink.class);
						queryspec.appendWhere( new SearchCondition(WTDocumentDependencyLink.class, 
								"roleAObjectRef.key", "=", PersistenceHelper.getObjectIdentifier(doc)), new int[] { 0 });
						QueryResult qr = PersistenceServerHelper.manager.query(queryspec);
						while (qr.hasMoreElements()){
							WTDocumentDependencyLink link = (WTDocumentDependencyLink) qr.nextElement();
							PersistenceServerHelper.manager.remove(link);
						}
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"removeDependencyLink"+":"+e);
				    } finally {
					    SessionServerHelper.manager.setAccessEnforced(enforce);
				    }
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
    }
	
	/**
	 * @author bjj
	 * get all DependsOn WTDocuments
	 * @param document
	 * @return
	 * @throws WTException
	 */
	public static List<WTDocument> getDependsOnWTDocuments(WTDocument document) throws WTException {
		try {
			List<WTDocument> documents = new ArrayList<WTDocument>();
			if (!RemoteMethodServer.ServerFlag) {
					  RemoteMethodServer.getDefault().invoke( "getDependsOnWTDocuments", DocUtil.class.getName(), null,
							new Class[] { String.class }, new Object[] {document});
				} else {
					boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					try {
						if (document == null) {
							return null;
						}
						QueryResult qr = WTDocumentHelper.service.getDependsOnWTDocuments(document);
						while (qr.hasMoreElements()) {
							WTDocument doc = (WTDocument) qr.nextElement();
							documents.add(doc);
						}
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"getDependsOnWTDocuments"+":"+e);
					} finally {
					    SessionServerHelper.manager.setAccessEnforced(enforce);
				    }
				    return documents;
			   }
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @author bjj
	 * get document short type
	 * 
	 * @param doc
	 *            object
	 * @return doc short type
	 */
	public static String getDocumentShortType(WTDocument doc){
		try {
			if (!RemoteMethodServer.ServerFlag) {
						return (String) RemoteMethodServer.getDefault().invoke("getDocumentShortType", 
								DocUtil.class.getName(), null, new Class[] { WTDocument.class}, new Object[] { doc });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				String strShortType = "";
				try {
					String curType = wt.type.TypedUtility.getExternalTypeIdentifier((wt.type.Typed) doc).trim();
					logger.debug("Doc.getDocumentShortType curType=" + curType);
					if (curType != null && !curType.isEmpty()) {
						strShortType = curType.substring(curType.lastIndexOf(".") + 1, curType.length());
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getDocumentShortType"+":"+e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
			    }
				return strShortType;
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	

	/**
	 * @author bjj
	 * Determine whether the document is detected.
	 * 
	 * @param oid
	 * @return boolean
	 * @throws RemoteException
	 *             , InvocationTargetException,WTRuntimeException,WTException
	 */
	public static boolean isCheckOut(String oid) throws RemoteException, InvocationTargetException, WTRuntimeException, WTException{
		try {
			if (!RemoteMethodServer.ServerFlag){
				return (Boolean) RemoteMethodServer.getDefault().invoke("isCheckOut", CLASSNAME, null,
						new Class[] { String.class }, new Object[] { oid });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				ReferenceFactory referenceFactory = new ReferenceFactory();
				WTDocument doc = (WTDocument) referenceFactory.getReference(oid).getObject();
				try {
					if (doc.isLatestIteration()) {
						doc = (WTDocument) VersionControlHelper.service.getLatestIteration(doc, false);
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"isCheckOut"+":"+e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
			    }
				if (WorkInProgressHelper.isCheckedOut(doc)) {
					return true;
				}
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	
	}

	/**
	 * @author bjj
	 * Check out WTDocument
	 * 
	 * @param wtdocument1
	 * @param description
	 * @return WTDocument
	 * @throws WTException
	 * @throws VersionControlException
	 */
	public static WTDocument checkOutWTDocument(WTDocument wtdocument1, String description) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag){
						return (WTDocument) RemoteMethodServer.getDefault().invoke("checkOutWTDocument", CLASSNAME, null,
										new Class[] { WTDocument.class,String.class }, new Object[] { wtdocument1, description});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				WTDocument wtdocument = wtdocument1;
				if (wtdocument == null){
					return wtdocument;
				}
				if (!wtdocument.isLatestIteration()){
					wtdocument = (WTDocument) VersionControlHelper.service.getLatestIteration(wtdocument, false);
				}
				wt.org.WTPrincipal wtprincipal = SessionHelper.manager.getPrincipal();
				if (SessionServerHelper.manager.isAccessEnforced()
						&& !AccessControlHelper.manager.hasAccess(wtprincipal, wtdocument, AccessPermission.MODIFY)){
					throw new WTException(wtprincipal.getName() + " have no modify permission for part:" + wtdocument.getNumber() + " in "
							+ wtdocument.getContainer().getName() + "/" + wtdocument.getFolderPath());
				}
				try{
					if (WorkInProgressHelper.isWorkingCopy(wtdocument)){
						return wtdocument;
					} else if (WorkInProgressHelper.isCheckedOut(wtdocument)){
						return (WTDocument) WorkInProgressHelper.service.workingCopyOf(wtdocument);
					} else{
						Folder folder = WorkInProgressHelper.service.getCheckoutFolder();
						CheckoutLink checkoutLink = WorkInProgressHelper.service.checkout(wtdocument, folder, description);
						return (WTDocument) checkoutLink.getWorkingCopy();
					}
				} catch (WTPropertyVetoException e){
					logger.error(CLASSNAME+"."+"checkOutWTDocument"+":"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @author bjj
	 *  do CheckOut by document.
	 * @param document
	 * @return
	 * @throws WTException
	 */
	public static Workable doCheckOut(Workable document){
		try {
			if (!RemoteMethodServer.ServerFlag){
					return (Workable) RemoteMethodServer.getDefault()
								.invoke("doCheckOut", CLASSNAME, null, new Class[] { Workable.class }, new Object[] { document});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				Workable workable = null;
				try{
					if(document!=null){
						if(document instanceof Iterated){
							Iterated it = (Iterated) document;
							document=(Workable) VersionControlHelper.service.getLatestIteration(it, false);
							Boolean checkOutFlag=WorkInProgressHelper.isCheckedOut(document);
							if(checkOutFlag){ 
								if(!WorkInProgressHelper.isWorkingCopy(document)){
							         workable=WorkInProgressHelper.service.workingCopyOf(document);
								}else{
									workable = document;
								}
							}else{
								Folder myFolder= WorkInProgressHelper.service.getCheckoutFolder();
								CheckoutLink link = WorkInProgressHelper.service.checkout(document, myFolder, "AutoCheckOut");
								workable = link.getWorkingCopy();
							}
						}	
					}
				}catch(Exception e){
					logger.error(CLASSNAME+"."+"doCheckOut"+":"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			return workable;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	    return null;
	}

	/**
	 * @author bjj
	 *  do CheckOut by document.
	 * @param wtdocument1
	 * @param description
	 * @return
	 * @throws WTException
	 */		
	public static WTDocument checkInWTDocument(WTDocument wtdocument1, String description) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag){
						return  (WTDocument) RemoteMethodServer.getDefault().invoke("checkInWTDocument", 
								CLASSNAME, null, new Class[] { WTDocument.class, String.class }, new Object[] { wtdocument1,description});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				WTDocument wtdocument = wtdocument1;
				if (wtdocument == null) {
					return wtdocument;
				}
				if (!wtdocument.isLatestIteration()){
					wtdocument = (WTDocument) VersionControlHelper.service.getLatestIteration(wtdocument, false);
				}
				try{
					if (WorkInProgressHelper.isWorkingCopy(wtdocument)){
						return (WTDocument) WorkInProgressHelper.service.checkin(wtdocument, description);
					} else if (WorkInProgressHelper.isCheckedOut(wtdocument)){
						wtdocument = (WTDocument) WorkInProgressHelper.service.workingCopyOf(wtdocument);
						return (WTDocument) WorkInProgressHelper.service.checkin(wtdocument, description);
					}
				} catch (WTPropertyVetoException e){
					logger.error(CLASSNAME+"."+"checkInWTDocument"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return wtdocument;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	    return null;
	}
		
	/**
	 * @author bjj
	 * get LifeCycle By WTDocument
	 * @param doc
	 * @return
	 */	
	public static String getLifeCycleByWTDocument(WTDocument doc){
		try {
			if (!RemoteMethodServer.ServerFlag){
						return  (String) RemoteMethodServer.getDefault()
								.invoke("getLifeCycleByWTDocument", CLASSNAME, null, new Class[] { WTDocument.class }, new Object[] { doc });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);	
				String lifecycle;
				try{
					lifecycle=doc.getLifeCycleState().toString();
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return lifecycle;
			    }
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	   return null;	
				
	}
      
	/**
	 * @author bjj
	 * Get WTDocument Associated Processes.
	 * 
	 * @param persistable WfState b,WTContainerRef c
	 * @return
	 */
	public static AssociatedProcessProcessor getAssociatedProcesses(Persistable persistable,WfState wtstate,WTContainerRef containerref) {
		try {
			if (!RemoteMethodServer.ServerFlag){
						return  (AssociatedProcessProcessor) RemoteMethodServer.getDefault()
								.invoke("getAssociatedProcesses", CLASSNAME, null, new Class[] 
										{ Persistable.class, WfState.class,WTContainerRef.class}, new Object[] { persistable, wtstate, containerref });
			} else {
				AssociatedProcessProcessor ass = null;
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);	
				try {
					QueryResult queryResult = WfEngineHelper.service.getAssociatedProcesses( persistable, wtstate, containerref);
					if (queryResult != null && queryResult.hasMoreElements()) {
						ass =  (AssociatedProcessProcessor) queryResult.nextElement();
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+"."+"getAssociatedProcesses"+":"+e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return ass;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @author bjj
	 * 通过文档得到其关联的文档，返回Dependence文档列表
	 * @param doc:条件文档
	 * @return 依附于条件文档的文档列表
	 */
	public static ArrayList<WTDocument> getDocsByDoc(WTDocument doc) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ArrayList<WTDocument>) RemoteMethodServer.getDefault().invoke( "getDocsByDoc", DocUtil.class.getName(), null,
						new Class[] { WTDocument.class }, new Object[] { doc });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager .setAccessEnforced(false);
				ArrayList<WTDocument> results = null;
				results = new ArrayList<WTDocument>();
				try {
					QueryResult qr = WTDocumentHelper.service.getDependsOnWTDocuments(doc);
					while (qr.hasMoreElements()) {
						Object tempObj = qr.nextElement();
						if (tempObj instanceof WTDocument) {
							results.add((WTDocument) tempObj);
						}
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getDocsByDoc"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return results;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**@author bjj
	 * 返回文档所被参考的文档
	 * @param srcDoc
	 * @return
	 * @throws RemoteException
	 * @throws InvocationTargetException
	 */
	public static List<WTDocument> getReferenceByDocs(WTDocument srcDoc)
			throws RemoteException, InvocationTargetException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				Class<?>[] clz = new Class<?>[] { WTDocument.class };
				Object[] objs = new Object[] { srcDoc };
					return (List<WTDocument>) RemoteMethodServer.getDefault().invoke( "getReferenceByDocs", DocUtil.class.getName(), null, clz, objs);
			}
			List<WTDocument> rsList = new ArrayList<WTDocument>();
			boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
			try {
				QueryResult qr = WTDocumentHelper.service.getHasDependentWTDocuments(srcDoc, true);
				while (qr.hasMoreElements()) {
					Object tempObj = qr.nextElement();
					if (tempObj instanceof WTDocument) {
						WTDocument doc = (WTDocument) tempObj;
						rsList.add(doc);
					}
				}
			} catch (Exception e) {
				logger.error(CLASSNAME+"."+"getReferenceByDocs"+":"+e);
			} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return rsList;
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @author bjj
	 * 通过文档得到其关联的部件（描述），返回Describe部件列表
	 * @param doc:条件文档
	 * @return 依附于条件文档的（描述）部件列表
	 */
	public static ArrayList<WTPart> getDescPartsByDoc(WTDocument doc) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ArrayList<WTPart>) RemoteMethodServer.getDefault().invoke(
						"getDescPartsByDoc", DocUtil.class.getName(), null, new Class[] { WTDocument.class }, new Object[] { doc });
			} else {
				ArrayList<WTPart> results = new ArrayList<WTPart>();
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				try {
					QueryResult qr = WTPartHelper.service.getDescribesWTParts(doc);
					while (qr.hasMoreElements()) {
						Object tempObj = qr.nextElement();
						if (tempObj instanceof WTPart) {
							results.add((WTPart) tempObj);
						}
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getDescPartsByDoc"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return results;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @author bjj
	 * 通过文档得到其关联的部件（参考），返回Reference部件列表
	 * @param doc:条件文档
	 * @return 依附于条件文档的（参考）部件列表
	 */
	public static ArrayList<WTPart> getRefPartsByDoc(WTDocument doc) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ArrayList<WTPart>) RemoteMethodServer.getDefault().invoke(
						"getRefPartsByDoc", DocUtil.class.getName(), null, new Class[] { WTDocument.class }, new Object[] { doc });
			} else {
				ArrayList<WTPart> results = new ArrayList<WTPart>();
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				try {
					WTDocumentMaster master = (WTDocumentMaster) doc.getMaster();
					QueryResult qr = StructHelper.service.navigateReferencedBy( master, WTPartReferenceLink.class, true);
					while (qr.hasMoreElements()) {
						Object tempObj = qr.nextElement();
						if (tempObj instanceof WTPart) {
							results.add((WTPart) tempObj);
						}
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getRefPartsByDoc"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return results;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @author bjj
	 * 删除文档与文档之间的关联（不对小版本进行升级）
	 * @param docRoleA
	 *            文档A
	 * @param docRoleB
	 *            文档B
	 * @return 删除操作是否成功
	 */
	public static boolean removeDocToDoc(WTDocument docRoleA, WTDocument docRoleB) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (Boolean) RemoteMethodServer.getDefault().invoke( "removeDocToDoc", DocUtil.class.getName(), null,
						new Class[] { WTDocument.class, WTDocument.class }, new Object[] { docRoleA, docRoleB });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
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
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"removeDocToDoc"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
		        return true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return  false;
	}
	
	/**
	 * @author bjj
	 * get Doc By Name
	 * @param name
	 *            document name
	 * @param accessControlled
	 *            权限控制
	 * @return QueryResult
	 */	
	@SuppressWarnings("deprecation")
	public static QueryResult getDocByName(String name, boolean accessControlled) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (QueryResult) RemoteMethodServer.getDefault().invoke( "getDocByName", DocUtil.class.getName(), null,
						new Class[] { String.class, boolean.class }, new Object[] { name, accessControlled });
			} else {
				QueryResult qr = new QueryResult();
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(accessControlled);
				try {
					QuerySpec spec = new QuerySpec(WTDocument.class);
					SearchCondition sc = new SearchCondition(WTDocument.class, WTDocument.NAME, SearchCondition.EQUAL, name);
					spec.appendWhere(sc, new int[] { 0 });
					spec.appendAnd();
					sc = new SearchCondition(WTDocument.class, WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE);
					spec.appendWhere(sc, new int[] { 0 });
					qr = PersistenceHelper.manager.find(spec);
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getDocByName"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return qr;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @author bjj 
	 * create Relationship
	 * @param docs
	 *            WTCollection
	 * @param part
	 *            WTPart
	 * @param isRefDoc
	 * @param needAutoCheckIn
	 * @param isPartDoc
	 * @return WTCollection
	 */
	public static WTCollection createRelationship(WTCollection docs, WTPart part, boolean isRefDoc, boolean needAutoCheckIn, boolean isPartDoc) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTCollection) RemoteMethodServer.getDefault().invoke("createRelationship", DocUtil.class.getName(), null, 
						new Class[] { WTCollection.class, WTPart.class, boolean.class, boolean.class, boolean.class}, 
						new Object[] { docs,part, isRefDoc, needAutoCheckIn, isPartDoc});
			} else {
				boolean access = SessionServerHelper.manager.setAccessEnforced(false);
				WTCollection objects = new WTArrayList();
				WTCollection links = new WTArrayList();
				// be careful about inflating WTReference to a Persistable
				boolean isCOByMe = WIPUtils.enableableObject(part);
				boolean isCOValid = WIPUtils.isCheckOutValid(part, WIPUtils.FULL);
				try {
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
			
						for (Iterator<?> it = links.persistableIterator(); it.hasNext();) {
							// remove doc links that were added from the message list
							if (isRefDoc) {
								WTPartReferenceLink refLink = (WTPartReferenceLink) it.next();
								WTDocumentMaster docRef = (WTDocumentMaster) refLink.getRoleBObject();
								objects.remove(docRef);
							} else {
								WTPartDescribeLink describeLink = (WTPartDescribeLink) it.next();
								WTDocument docRef = (WTDocument) describeLink.getRoleBObject();
								objects.remove(docRef);
							}
						}
					} else {
						if (links.size() == 0) {
							objects.add(part);
						}
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"createRelationship"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(access);
				}	
				return objects;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @author bjj 
	 * create Relationship
	 * @param docs
	 *            WTCollection
	 * @param part
	 *            WTPart
	 * @param isRefDoc
	 * @param needAutoCheckIn
	 * @param isPartDoc
	 * @return WTCollection
	 */
	public static WTCollection createPartDocDescribeLinks(WTPart wtpart, WTCollection wtcollection) {
        try {
			if (!RemoteMethodServer.ServerFlag) {
						return (WTCollection) RemoteMethodServer.getDefault().invoke("createPartDocDescribeLinks", 
								DocUtil.class.getName(), null, new Class[] { WTPart.class,WTCollection.class},
								new Object[] { wtpart,wtcollection });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				Transaction transaction;
				boolean flag;
				WTArrayList wtarraylist;
				transaction = new Transaction();
				flag = false;
				wtarraylist = new WTArrayList();
				try {
					if(wtpart != null && wtcollection != null){
						transaction.start();
						PersistenceServerHelper.manager.lock(wtpart, true);
						Iterator<?> iterator = wtcollection.persistableIterator();
						do {
							if (!iterator.hasNext()){
								break;
							}
							WTDocument wtdocument = (WTDocument) iterator.next();
							QueryResult queryresult = intGetDescribeAssociations(wtpart, (WTDocumentMaster) wtdocument.getMaster());
							if (queryresult.size() > 0) {
								do {
									if (!queryresult.hasMoreElements()){
										break;
									}
									WTPartDescribeLink wtpartdescribelink = (WTPartDescribeLink) queryresult.nextElement();
									if (PersistenceHelper.isEquivalent( wtdocument, wtpartdescribelink.getDescribedBy())){
										flag = true;
									}
									else if (!PartDocHelper.isWcPDMMethod()){
										PersistenceHelper.manager.delete(wtpartdescribelink);
									}
								} while (true);
								if (!flag) {
									WTPartDescribeLink wtpartdescribelink1 = WTPartDescribeLink.newWTPartDescribeLink(wtpart, wtdocument);
									PersistenceHelper.manager.store(wtpartdescribelink1);
									wtarraylist.add(wtpartdescribelink1);
								}
							} else {
								WTPartDescribeLink wtpartdescribelink2 = WTPartDescribeLink.newWTPartDescribeLink(wtpart, wtdocument);
								PersistenceServerHelper.manager.insert(wtpartdescribelink2);
								wtarraylist.add(wtpartdescribelink2);
							}
						} while (true);
						transaction.commit();
						transaction = null;
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"createPartDocDescribeLinks"+":"+e);
				} finally {
					if (transaction != null) {
						transaction.rollback();
					}
					 SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return wtarraylist;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	   	return null;
	}
	
	/**
	 * @author bjj 
	 * create Relationship
	 * @param docs
	 *            WTCollection
	 * @param part
	 *            WTPart
	 * @param isRefDoc
	 * @param needAutoCheckIn
	 * @param isPartDoc
	 * @return WTCollection
	 */
	public static WTCollection createPartDocReferenceLinks(WTPart part, WTCollection documents) throws WTException {
	    try {
			if (!RemoteMethodServer.ServerFlag) {
						return (WTCollection) RemoteMethodServer.getDefault().invoke("createPartDocReferenceLinks", 
								DocUtil.class.getName(), null, new Class[] { WTPart.class,WTCollection.class},
								new Object[] { part,documents });
					
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				Transaction trx = new Transaction();
				WTCollection referenceLinks = new WTArrayList();
				try {
					trx.start();
					PersistenceServerHelper.manager.lock(part, true);
					for (Iterator<?> it = documents.persistableIterator(); it.hasNext();) {
						WTDocumentMaster docMaster = (WTDocumentMaster) it.next();
						QueryResult qr = intGetReferenceAssociations(part, docMaster);
						if (qr.size() > 0) {
							// Delete all but the last link.
							for (int i = 0; i < qr.size() - 1; i++) {
								PersistenceHelper.manager.delete((WTPartReferenceLink) qr.nextElement());
							}
						} else {
							Link link = (Link) WTPartReferenceLink.newWTPartReferenceLink(part, docMaster);
							PersistenceServerHelper.manager.insert(link);
							referenceLinks.add(link);
						}
					}
					trx.commit();
					trx = null;
				} finally {
					if (trx != null){
						trx.rollback();
					}
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return referenceLinks;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	    return null;		
	}
	
	public static QueryResult intGetReferenceAssociations(WTPart wtpart, WTDocumentMaster wtdocumentmaster) throws WTException {
	    try {
			if (!RemoteMethodServer.ServerFlag) {
				return (QueryResult) RemoteMethodServer.getDefault().invoke("intGetReferenceAssociations", DocUtil.class.getName(), 
						null, new Class[] { WTPart.class,WTDocumentMaster.class}, new Object[] { wtpart,wtdocumentmaster });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				QueryResult queryresult = new QueryResult();
				QuerySpec queryspec = new QuerySpec(WTDocumentMaster.class, WTPartReferenceLink.class);
				try {
					queryspec.appendWhere( new SearchCondition(WTPartReferenceLink.class, "roleBObjectRef.key", "=", 
									PersistenceHelper.getObjectIdentifier(wtdocumentmaster)),new int[] { 1 });
					QueryResult queryresult1 = PersistenceServerHelper.manager.expand( wtpart, "references", queryspec, false);
					Vector<ObjectToObjectLink> vector = new Vector<ObjectToObjectLink>();
					WTPartReferenceLink wtpartreferencelink;
					for (; queryresult1.hasMoreElements(); vector.add(wtpartreferencelink)){
						wtpartreferencelink = (WTPartReferenceLink) queryresult1.nextElement();
					}
					queryresult.append(new ObjectSetVector(vector));
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"intGetReferenceAssociations"+":"+e);
				} finally {
				  SessionServerHelper.manager.setAccessEnforced(enforce);
				} 
				return queryresult;
			}

		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}		
		return null;
	}
	
	
	@SuppressWarnings("deprecation")
	public static QueryResult intGetDescribeAssociations(WTPart wtpart, WTDocumentMaster wtdocumentmaster) throws WTException {
	    try {
			if (!RemoteMethodServer.ServerFlag) {
						return (QueryResult) RemoteMethodServer.getDefault().invoke("intGetDescribeAssociations", 
								DocUtil.class.getName(), null, new Class[] { WTPart.class,WTDocumentMaster.class},
								new Object[] { wtpart,wtdocumentmaster });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				QueryResult queryresult = new QueryResult();
				QuerySpec queryspec = new QuerySpec(WTPartDescribeLink.class);
				if(wtpart != null && wtdocumentmaster != null){
					queryspec.appendClassList(WTDocument.class, true);
					queryspec.appendWhere(new SearchCondition(WTPartDescribeLink.class, "roleAObjectRef.key", "=",
							PersistenceHelper.getObjectIdentifier(wtpart)), new int[] { 0 });
					queryspec.appendAnd();
					queryspec.appendWhere(new SearchCondition(WTPartDescribeLink.class, "roleBObjectRef.key.id",
							WTDocument.class, "thePersistInfo.theObjectIdentifier.id"), new int[] { 0, 1 });
					queryspec.appendAnd();
					queryspec.appendWhere(new SearchCondition(WTDocument.class, "masterReference.key",
									"=", PersistenceHelper.getObjectIdentifier(wtdocumentmaster)), new int[] { 1 });
					QueryResult queryresult1 = PersistenceHelper.manager.find(queryspec);
					Vector<ObjectToObjectLink> vector = new Vector<ObjectToObjectLink>();
					WTPartDescribeLink wtpartdescribelink;
					try {
						for (; queryresult1.hasMoreElements(); vector.add(wtpartdescribelink)) {
							Object aobj[] = (Object[]) (Object[]) queryresult1.nextElement();
							wtpartdescribelink = (WTPartDescribeLink) aobj[0];
								wtpartdescribelink.setDescribes(wtpart);
								wtpartdescribelink.setDescribedBy((WTDocument) aobj[1]);
						}
						queryresult.append(new ObjectSetVector(vector));
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"intGetDescribeAssociations"+":"+e);
					} finally {
					    SessionServerHelper.manager.setAccessEnforced(enforce);
					}
					return queryresult;
				}
			}
		} catch (java.rmi.RemoteException e ) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e){
			logger.error(e.getMessage(),e);
		}
		return null;	
	}
	
	/**
	 * @author bjj
	 * 查询指定软属性为指定值的文档的最新版本
	 * @param ibaname
	 * @param ibavalue
	 * @throws Exception
	 */
	@SuppressWarnings({ "deprecation" })
	public static List<WTDocument> searchDocumentFilterByIba(String ibaname, String ibavalue) throws Exception {
		 try{
	        if (!RemoteMethodServer.ServerFlag) {
	                	return (List<WTDocument>) RemoteMethodServer.getDefault().invoke( "searchDocumentFilterByIba", DocUtil.class.getName(), 
	                			null, new Class[] { String.class, String.class }, new Object[] { ibaname, ibavalue });
	        } else {
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				QuerySpec qs = new QuerySpec();
				qs.setAdvancedQueryEnabled(true);
				List<WTDocument> docList = new Vector<WTDocument>();
                try {
					int ibaHolderIndex = qs.appendClassList(WTDocument.class, true);
					int ibaStringValueIndex = qs.appendClassList(StringValue.class, false);
					int ibaStringDefinitionIndex = qs.appendClassList( StringDefinition.class, false);
					// Latest Iteration
					SearchCondition scLatestIteration = new SearchCondition( WTDocument.class, WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
					// String Value With IBA Holder
					SearchCondition scJoinStringValueIBAHolder = new SearchCondition( StringValue.class, "theIBAHolderReference.key.id",
							WTDocument.class, WTAttributeNameIfc.ID_NAME);
					// String Value With Definition
					SearchCondition scJoinStringValueStringDefinition = new SearchCondition( StringValue.class, "definitionReference.key.id",
							StringDefinition.class, WTAttributeNameIfc.ID_NAME);
					// String Definition 软属性名称
					SearchCondition scStringDefinitionName = new SearchCondition( StringDefinition.class, StringDefinition.NAME, SearchCondition.EQUAL, ibaname);
					// String Value 软属性值
					SearchCondition scStringValueValue = new SearchCondition( StringValue.class, StringValue.VALUE, SearchCondition.EQUAL, ibavalue.toUpperCase());
					// documentmaster name = type
					qs.appendWhere(scLatestIteration, ibaHolderIndex);
					qs.appendAnd();
					qs.appendWhere(scJoinStringValueIBAHolder, ibaStringValueIndex, ibaHolderIndex);
					qs.appendAnd();
					qs.appendWhere(scJoinStringValueStringDefinition, ibaStringValueIndex, ibaStringDefinitionIndex);
					qs.appendAnd();
					qs.appendWhere(scStringDefinitionName, ibaStringDefinitionIndex);
					qs.appendAnd();
					qs.appendWhere(scStringValueValue, ibaStringValueIndex);
					QueryResult qr = PersistenceHelper.manager.find(qs);
	
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
                } catch (Exception e) {
					logger.error(CLASSNAME+"."+"searchDocumentFilterByIba"+":"+e);
                } finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
                }
				return docList;
	        }
	        } catch (RemoteException e) {
	            logger.error(e.getMessage(),e);
	        } catch (InvocationTargetException e) {
	        	logger.error(e.getMessage(),e);
	        }
	        return null;
	}
	
	/**
	 *@author bjj
	 * 根据文档对象获取最新版本的文档
	 * @param mastered
	 * @return
	 * @throws WTException
	 */
	public static WTDocument getLatestWTDocument(Mastered mastered) throws WTException {
	    try {
			if (!RemoteMethodServer.ServerFlag) {
	        	return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"getLatestWTDocument", DocUtil.class.getName(), null, new Class[] { Mastered.class }, new Object[] { mastered });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				WTDocument lasterwtdoc = null;
				QueryResult qr = VersionControlHelper.service.allVersionsOf(mastered);
				try {
			    	LatestConfigSpec cfg = new LatestConfigSpec();
					qr = cfg.process(qr);
					while (qr.hasMoreElements()) {
						WTDocument wtdoc = (WTDocument) qr.nextElement();
						if (wtdoc != null)
							lasterwtdoc = wtdoc;
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"getLatestWTDocument"+":"+e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return lasterwtdoc;
			}
		} catch (java.rmi.RemoteException e) {
        	logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e){
			logger.error(e.getMessage(),e);
		}
	    return null;
	}
	
	/**
	 * @author bjj
	 * 根据编号和名称查询指定类型的文档
	 * @param number
	 * @param name
	 * @param softType
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static QueryResult searchSoftTypeDoc(String number, String name, String softType, String[] states,Integer queryLimit) throws WTException {
        try {
			if (!RemoteMethodServer.ServerFlag) {
			        	return (QueryResult) RemoteMethodServer.getDefault().invoke( "searchSoftTypeDoc", DocUtil.class.getName(), null,
								new Class[] { String.class, String.class,String.class, String[].class }, 
								new Object[] { number, name,softType, states});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				QuerySpec qs = new QuerySpec(WTDocument.class);
				try {
					qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.LATEST_ITERATION, SearchCondition.IS_TRUE), new int[] { 0 });
					if(softType != null){
						TypeDefinitionReference tdref = TypedUtilityServiceHelper.service.getTypeDefinitionReference("wt.doc.WTDocument|" + softType);
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.TYPE_DEFINITION_REFERENCE + ".key.id",
								SearchCondition.EQUAL, tdref.getKey().getId()), new int[] { 0 });
					}
					if (number != null) {
						SearchCondition searchCondition = new SearchCondition( WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, number);
						qs.appendAnd();
						qs.appendWhere(searchCondition, new int[] { 0 });
					}
					if (name != null) {
						SearchCondition searchCondition = new SearchCondition( WTDocument.class, WTDocument.NAME, SearchCondition.EQUAL, name);
						qs.appendAnd();
						qs.appendWhere(searchCondition, new int[] { 0 });
					}
					
					
					if (states != null && states.length > 0) {
						qs.appendAnd();
						qs.appendOpenParen();
						for (int i=0;i<states.length;i++) {
							String state = states[i];
							SearchCondition searchCondition = new SearchCondition(
									WTDocument.class, LifeCycleManaged.STATE + "." + LifeCycleState.STATE, SearchCondition.EQUAL, state.toUpperCase());
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
					
				} catch (Exception e) {
				    logger.error(CLASSNAME+"."+"searchSoftTypeDoc"+":"+e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			    return PersistenceHelper.manager.find(qs);
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
	    return null;
	}
	
	/**
	 *@author bjj
	 * @param softType
	 * @param states
	 * @param queryLimit
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	@SuppressWarnings("deprecation")
	public static QueryResult searchSoftTypeDoc(String softType, String[] states,Integer queryLimit) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (QueryResult) RemoteMethodServer.getDefault().invoke( "searchSoftTypeDoc", DocUtil.class.getName(), null,
						new Class[] { String.class, String[].class, Integer.class}, new Object[] { softType, states,queryLimit});
				}else{
					boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					QuerySpec qs = new QuerySpec();
					try {
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
							subQs.appendWhere(new SearchCondition(tc01, SearchCondition.EQUAL,new ConstantExpression(softType)),new int[] { 0 });
							subQs.appendAnd();
							subQs.appendWhere(new SearchCondition(tc00,
									SearchCondition.EQUAL,tc10),new int[] { 0 });
							//添加子查询到主查询的条件中
							qs.appendAnd();
							qs.appendWhere(new SearchCondition(tc0, SearchCondition.IN, new SubSelectExpression(subQs)), new int[] { a });
						}
	
						if (states != null && states.length > 0) {
							qs.appendAnd();
							qs.appendOpenParen();
							for (int i=0;i<states.length;i++) {
								String state = states[i];
								SearchCondition searchCondition = new SearchCondition(
										WTDocument.class, LifeCycleManaged.STATE + "." + LifeCycleState.STATE, SearchCondition.EQUAL, state.toUpperCase());
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
						qs.appendOrderBy(new OrderBy(new ClassAttribute( WTDocument.class, WTDocument.PERSIST_INFO + "." + PersistInfo.MODIFY_STAMP), true), new int[] { 0 });
					} catch (Exception e) {
					    logger.error(CLASSNAME+"."+"searchSoftTypeDoc"+":"+e);
					} finally {
					    SessionServerHelper.manager.setAccessEnforced(enforce);
					}
					return PersistenceServerHelper.manager.query(qs);
				}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
	    return null;
	}
	
	/**
	 * @author bjj
	 * Undo check out
	 * @param workable
	 * @throws WTException 
	 */
	public Workable undoCheckOut(Workable workable) throws WTException{
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (Workable) RemoteMethodServer.getDefault().invoke( "undoCheckOut", DocUtil.class.getName(), null,
					new Class[] { Workable.class}, new Object[] { workable });
			}else{
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try{
					if (WorkInProgressHelper.isCheckedOut(workable)) {
						workable = WorkInProgressHelper.service.undoCheckout(workable);
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"undoCheckOut"+":"+e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return workable;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
	    return null;
	}
	
	/**
	 * @author bjj
	 * 给当前签审的文档对象设置生命周期状态
	 * @param self
	 * @param pbo
	 * @param lfState
	 * @throws WTException 
	 */
	public static void setDocLifecycleState(ObjectReference self, WTObject pbo, String lfState) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					  RemoteMethodServer.getDefault().invoke(
							"setDocLifecycleState", DocUtil.class.getName(), null,
							new Class[] { ObjectReference.class,WTObject.class,String.class},
							new Object[] { self,pbo, lfState});
				}else{
					boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					try {
						if (pbo instanceof PromotionNotice) {
							PromotionNotice pn = (PromotionNotice) pbo;
							QueryResult qr = MaturityHelper.service.getPromotionTargets(pn);
							while (qr.hasMoreElements()) {
								Object object = (Object) qr.nextElement();
								if (object instanceof WTDocument) {
									WTDocument doc = (WTDocument) object;
									LifeCycleHelper.service.setLifeCycleState(doc, State.toState(lfState));
								}
								if (object instanceof WTPart) {
									WTPart part = (WTPart) object;
									LifeCycleHelper.service.setLifeCycleState(part, State.toState(lfState));
								}
							}
						}
					} catch (Exception e) {
						logger.error(CLASSNAME+"."+"setDocLifecycleState"+":"+e);
					} finally {
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
			   }
		} catch (WTInvalidParameterException | java.rmi.RemoteException | InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
	}
		
	/**
	 * @author: Baijj
	 * Description:             
	 *            Create new reference link between parts and  document  ( Judge whether parts and document have a reference link . 
	 *            If exist ,delete and to build a new relationship )
	 * @throws WTException 
	 */
	public static  void newReferenceDocLink(WTPart part, WTDocument doc) throws WTException{
		try {
			if (!RemoteMethodServer.ServerFlag) {
					  RemoteMethodServer.getDefault().invoke( "newReferenceDocLink", DocUtil.class.getName(), null,
							new Class[] { WTPart.class,WTDocument.class }, new Object[] { part,doc });
			}else{
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (hasReferenceLink(part, doc)) {
						removeReferenceDoc(part, doc);
					}
					WTPartReferenceLink partDocRefLink =WTPartReferenceLink.newWTPartReferenceLink(part, (WTDocumentMaster) doc.getMaster());
					PersistenceServerHelper.manager.insert(partDocRefLink);
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"newReferenceDocLink"+":"+e);
				}
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
	}
	
	/**
	 * @author: baijj
	 * Description:           
	 *         Whether parts and reference document reference relationship exists.   
	 * @param part
	 * @param doc 
	 */
	public static boolean hasReferenceLink(WTPart part,WTDocument doc) throws WTException{
		try {
			if (!RemoteMethodServer.ServerFlag) {
					  RemoteMethodServer.getDefault().invoke( "newReferenceDocLink", DocUtil.class.getName(), null,
							new Class[] { WTPart.class, WTDocument.class }, new Object[] { part,doc });
			}else{
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				boolean flag=false;
				QueryResult qr= PersistenceHelper.manager.navigate(part,
						WTPartReferenceLink.REFERENCES_ROLE, WTPartReferenceLink.class, false);
				try {
					while (qr.hasMoreElements()) {
						WTPartReferenceLink link = (WTPartReferenceLink) qr.nextElement();
						if (link!=null) {
							WTPart p = link.getReferencedBy();
							WTDocumentMaster master=link.getReferences();
							WTDocument d=(WTDocument) VersionUtil.getLatestRevision(master);
								if (part.getNumber().equals(p.getNumber())&&doc.getNumber().equals(d.getNumber())) {
									flag=true;
									return flag;
								}	
							} 
						}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"hasReferenceLink"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return flag;	
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
		return false;
	}
	
	/**
	 * @author: baijuanjuan
	 * Description:  
	 *             Remove the reference link between part and doc.
	 * @throws WTException 
	 */
	public static void removeReferenceDoc(WTPart part, WTDocument doc) throws WTException{
		try {
			if (!RemoteMethodServer.ServerFlag) {
				  RemoteMethodServer.getDefault().invoke( "newReferenceDocLink", DocUtil.class.getName(), null,
						new Class[] { WTPart.class,WTDocument.class }, new Object[] { part,doc });
			}else{
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				QueryResult qr = PersistenceHelper.manager.navigate(part, WTPartReferenceLink.REFERENCES_ROLE, WTPartReferenceLink.class, false);
				try {
					while (qr.hasMoreElements()) {
						WTPartReferenceLink link = (WTPartReferenceLink) qr.nextElement();
						if (link!=null) {
							WTPart p = link.getReferencedBy();
							WTDocumentMaster master=link.getReferences();
							WTDocument d=(WTDocument) VersionUtil.getLatestRevision(master);
							if (part.getNumber().equals(p.getNumber())&&doc.getNumber().equals(d.getNumber())) {
								PersistenceServerHelper.manager.remove(link);
							}
						}
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+"."+"hasReferenceLink"+":"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
		}
	}
	
	public static void test() throws RemoteException,InvocationTargetException, WTException {
			
				WTPart part = PartUtil.getPartByNumber("HQ11100576000");
				WTDocument wtDocument = DocUtil.getDocumentByNumber("CPJY00000544");
//				removeReferenceDoc(part,wtDocument);//多个文档时不能移除
			System.out.println(hasReferenceLink(part,wtDocument));	//不能建立关系
		
	}                                                                   
	
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				RemoteMethodServer server = RemoteMethodServer.getDefault();
				server.setUserName("wcadmin");
				server.setPassword("wcadmin");
	
				RemoteMethodServer.getDefault().invoke("test",
						DocUtil.class.getName(), null, new Class[] {},
						new Object[] {});
	
			} catch (java.rmi.RemoteException e) {
	        	logger.error(e.getMessage(),e);
						}
					}
				}
	
   }
