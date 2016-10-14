package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.epm.EPMDocument;
import wt.epm.EPMDocumentHelper;
import wt.epm.EPMDocumentMaster;
import wt.epm.structure.EPMReferenceLink;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleException;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionServerHelper;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.struct.IteratedDescribeLink;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
/**
 * EPM 文档的增删 改查 相关操作
 * @author zhangxj
 * @version
 *
 */
public class EPMUtil implements RemoteAccess {
	private static final int[] DEFAULT_CONDITION_ARRAY = new int[] { 0 };
	private final static String CLASSNAME = EPMUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * 
	 * 
	 * 判断EPM文档是否存在
	 * <功能详细描述>
	 * @author  zhangxj
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean isEPMDocExist(String strNumber) {
		try {
			if(RemoteMethodServer.ServerFlag){
			 RemoteMethodServer.getDefault().invoke("isEPMDocExist", EPMUtil.class.getName(), null, new Class []{String.class}, new Object []{strNumber});
			}else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					EPMDocument EPMDoc = null;
					if (!StringUtils.isEmpty(strNumber)) {
						EPMDoc = getEPMDocByNumber(strNumber);
					}
					if (EPMDoc == null) {
						return false;
					} else {
						return true;
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+".isEPMDocExist:"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
		return false;
	}

	/**
	 * getCADDocumentsByPart
	 * 
	 * @author zhangxj
	 * @param part
	 * @return
	 * @throws WTException
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static List<EPMDocument> getCADDocumentsByPart(WTPart part) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (List<EPMDocument>) RemoteMethodServer.getDefault().invoke("getCADDocumentsByPart",
						EPMUtil.class.getName(), null,new Class[] { WTPart.class },new Object[] { part });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				List<EPMDocument> epmDocs = new ArrayList<EPMDocument>();
				EPMDocument epmDoc = null;
				try {
					if (part  !=  null) {
						QueryResult result = PartDocHelper.service.getAssociatedDocuments(part);
						while (result.hasMoreElements()) {
							WTObject obj = (WTObject) result.nextElement();
							if (obj instanceof EPMDocument) {
								epmDoc = (EPMDocument) obj;
								epmDocs.add(epmDoc);
							} else if (obj instanceof IteratedDescribeLink) {
								obj = (WTObject) ((IteratedDescribeLink) obj).getDescribedBy();
								epmDoc = (EPMDocument) obj;
								epmDocs.add(epmDoc);
							}
						}
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".getCADDocumentsByPart:"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
				return epmDocs;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * getTypeByEPMDoc by EPMDoc
	 * @author zhangxj
	 * @param EPMDoc
	 * @return
	 */
	public static String getTypeByEPMDoc(EPMDocument EPMDoc) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke("getTypeByEPMDoc", EPMUtil.class.getName(), null,
						new Class[] { EPMDocument.class },new Object[] { EPMDoc });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				String EPMDocType = null;
				try {
					if (EPMDoc != null ) {
						EPMDocType = TypedUtilityServiceHelper.service.getExternalTypeIdentifier(EPMDocType).toString();
					}
				} catch (java.rmi.RemoteException e) {
					logger.error(CLASSNAME+".getTypeByEPMDoc:"+e);
				} catch (WTException e) {
					logger.error(CLASSNAME+".getTypeByEPMDoc:"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
				return EPMDocType;
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * 
	 * @Description: 更改图纸的存储位置
	 * @author zhangxj
	 * @param epm
	 * @param folder1
	 */
	public static void changeLocation(EPMDocument epm, String folder1) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("changeLocation",EPMUtil.class.getName(), null,
						new Class[] { EPMDocument.class, String.class },new Object[] { epm, folder1 });
			} else {
				boolean accessFlag = SessionServerHelper.manager.isAccessEnforced();
				try {
					if(epm != null && folder1 !=null){
						String oldFolder = epm.getLocation();
						String oldContext = epm.getContainerName();
						String folder = folder1;
						WTContainer wtcontainer =WCUtil.getWtContainerByName(oldContext);
						WTContainerRef wtcRef = WTContainerRef.newWTContainerRef(wtcontainer);
						if (!oldFolder.equals(folder)) {
							Folder newfolder = null;
							newfolder = FolderHelper.service.getFolder(folder,wtcRef);
							epm = (EPMDocument) FolderHelper.service.changeFolder((FolderEntry) epm, newfolder);
							PersistenceHelper.manager.refresh(epm);
						}
					}
				} catch (ObjectNoLongerExistsException e) {
					logger.error(CLASSNAME+".changeLocation:"+e);
				} catch (WTException e) {
					logger.error(CLASSNAME+".changeLocation:"+e);
					e.printStackTrace();
				} finally {
					SessionServerHelper.manager.setAccessEnforced(accessFlag);
				}
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	}

	/**
	 * geEPMDocumentByOid
	 * 
	 * @author zhangxj
	 * @param oid
	 * @return EPMDoc
	 * @throws WTException
	 * @throws WTRuntimeException
	 */
	public static EPMDocument geEPMDocumentByOid(String oid) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (EPMDocument) RemoteMethodServer.getDefault().invoke("changeLocation", EPMUtil.class.getName(), null,
				new Class[] { String.class }, new Object[] { oid });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				EPMDocument EPMDoc = null;
				if (!StringUtils.isEmpty(oid)) {
					ReferenceFactory rf = new ReferenceFactory();
					try {
						EPMDoc = (EPMDocument) rf.getReference(oid).getObject();
					} catch (WTRuntimeException e) {
						logger.error(CLASSNAME+".geEPMDocumentByOid:"+e);
					} catch (WTException e) {
						logger.error(CLASSNAME+".geEPMDocumentByOid:"+e);
					} finally {
						SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
					}
				}
				return EPMDoc;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * geEPMDocumentByName
	 * 
	 * @author zhangxj
	 * @param EPMDocName
	 * @return EPMList
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static List<EPMDocument> geEPMDocumentByName(String EPMDocName)throws WTException {
		try {
			List<EPMDocument> EPMList = new ArrayList<EPMDocument>();
			if (!RemoteMethodServer.ServerFlag) {
				return (List<EPMDocument>) RemoteMethodServer.getDefault().invoke("geEPMDocumentByName", EPMUtil.class.getName(),null,
				new Class[] { String.class },new Object[] { EPMDocName });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				try {
					if (!StringUtils.isEmpty(EPMDocName)) {
						QuerySpec qs = new QuerySpec(EPMDocument.class);
						SearchCondition sc = new SearchCondition(EPMDocument.class, EPMDocument.NAME,SearchCondition.EQUAL, EPMDocName, false);
						qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
						QueryResult qr = PersistenceServerHelper.manager.query((StatementSpec) qs);
						qr = new LatestConfigSpec().process(qr);
						EPMDocument EPMDoc = null;
						while (qr.hasMoreElements()) {
							EPMDoc = (EPMDocument) qr.nextElement();
							EPMList.add(EPMDoc);
						}
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".geEPMDocumentByName:"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
				return EPMList;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * getEPMDocByNumber
	 * 
	 * @author zhangxj
	 * @param EPMNum
	 * @return EPMDoc
	 * @throws WTException
	 */
	public static EPMDocument getEPMDocByNumber(String EPMNum)throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (EPMDocument) RemoteMethodServer.getDefault().invoke("getEPMDocByNumber", EPMUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] { EPMNum });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				EPMDocument EPMDoc = null;
				try {
					if ("".equals(EPMNum) || null == EPMNum) {
						return null;
					} else {
						QuerySpec querySpec = new QuerySpec(EPMDocument.class);
						EPMNum = EPMNum.toUpperCase();
						WhereExpression searchCondition = new SearchCondition(EPMDocument.class, EPMDocument.NUMBER,SearchCondition.EQUAL, EPMNum, false);
						querySpec.appendWhere(searchCondition,DEFAULT_CONDITION_ARRAY);
						QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec) querySpec);
					     if (queryResult.hasMoreElements()) {
					    	 EPMDoc = (EPMDocument) queryResult.nextElement();
						}
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".getEPMDocByNumber:"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
				return EPMDoc;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * 根据三维图纸获取相关的二维图纸
	 * 
	 * @author zhangxj
	 * @param epm
	 * @return result
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static List<EPMDocument> get2DEPM(EPMDocument epm)throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (List<EPMDocument>) RemoteMethodServer.getDefault().invoke("get2DEPM", EPMUtil.class.getName(), null,
					new Class[] { EPMDocument.class },new Object[] { epm });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				QueryResult qr = null;
				List<EPMDocument> result = new ArrayList<EPMDocument>();
				try {
					if (epm == null) {
						return null;
					} else {
						qr = PersistenceHelper.manager.navigate(epm.getMaster(),"referencedBy",wt.epm.structure.EPMReferenceLink.class, false);
						while (qr.hasMoreElements()) {
							EPMReferenceLink epmreferencelink = (EPMReferenceLink) qr.nextElement();
							EPMDocument epm2d = (EPMDocument) epmreferencelink.getOtherObject(epm.getMaster());
							if (epm2d != null) {
								result.add(epm2d);
							}
						}
						return result;
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".get2DEPM:"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * getEPMNumber by epmDocument
	 * @author zhangxj
	 * @param epmDocument
	 * @return name
	 */
	public static String getEPMNumber(EPMDocument epmDocument) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (String) RemoteMethodServer.getDefault().invoke("getEPMNumber", EPMUtil.class.getName(), null,
							new Class[] { EPMDocument.class },new Object[] { epmDocument });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				String number = null;
				try {
					if (epmDocument != null && !"".equals(epmDocument.getNumber())) {
						number = epmDocument.getNumber();
					}
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
				return number;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * getEPMName by epmDocument
	 * @author zhangxj
	 * @param epmDocument
	 * @return name
	 */
	public static String getEPMName(EPMDocument epmDocument) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (String) RemoteMethodServer.getDefault().invoke("getEPMName", EPMUtil.class.getName(), null,
							new Class[] { EPMDocument.class },new Object[] { epmDocument });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				String name = null;
				try {
					if (epmDocument != null && !"".equals(epmDocument.getName())) {
						name = epmDocument.getNumber();
					}
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
				return name;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * getPartMasterByNumber
	 * @author zhangxj
	 * @param EPMDocName
	 * @return epmDocumentMaster
	 * @throws WTException
	 */
	public static EPMDocumentMaster getEPMMasterByNumber(String EPMDocName)throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (EPMDocumentMaster) RemoteMethodServer.getDefault().invoke("getPartMasterByNumber",EPMUtil.class.getName(), null,
					new Class[] { String.class },new Object[] { EPMDocName });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				EPMDocumentMaster epmDocumentMaster = null;
				try {
					if(StringUtils.isEmpty(EPMDocName)){
							QuerySpec querySpec = new QuerySpec(EPMDocumentMaster.class);
							WhereExpression searchCondition = new SearchCondition(EPMDocumentMaster.class, EPMDocumentMaster.
									NUMBER,SearchCondition.EQUAL, EPMDocName, false);
							querySpec.appendWhere(searchCondition, new int[] { 0 });
							QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec) querySpec);
							while (queryResult.hasMoreElements()) {epmDocumentMaster = (EPMDocumentMaster) queryResult.nextElement();
							}
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".getPartMasterByNumber:"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return epmDocumentMaster;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * getLatestEPMByMaster
	 * @author zhangxj
	 * @param epmDocumentMaster
	 * @return latestEPMDoc
	 * @throws WTException
	 */
//	public static EPMDocument getLatestEPMByMaster(EPMDocumentMaster epmDocumentMaster) throws WTException {
//		try {
//			if (!RemoteMethodServer.ServerFlag) {
//					return (EPMDocument) RemoteMethodServer.getDefault().invoke("getLatestPartByMaster", EPMUtil.class.getName(), null,
//							new Class[] { EPMDocumentMaster.class },new Object[] { epmDocumentMaster });
//			} else {
//				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
//				EPMDocument latestEPMDoc = null;
//				try {
//					if (epmDocumentMaster != null) {
//						QueryResult qr = VersionControlHelper.service.allIterationsOf(epmDocumentMaster);
//						if (qr.hasMoreElements()) {
//							latestEPMDoc = (EPMDocument) qr.nextElement();
//						}
//					}
//				} catch (WTException e) {
//					logger.error(CLASSNAME+".getLatestEPMByMaster:"+e);
//				} finally {
//					SessionServerHelper.manager.setAccessEnforced(enforce);
//				}
//				return latestEPMDoc;
//			}
//		} catch (RemoteException e) {
//			logger.error(e.getMessage(),e);
//		} catch (InvocationTargetException e) {
//			logger.error(e.getMessage(),e);
//		}
//		return null;
//	}

	/**
	 * getLatestEPMByMaster
	 * @author zhangxj
	 * @param epmDocumentMaster
	 * @return latestEPMDoc
	 * @throws WTException
	 */
	public static EPMDocument getLatestEPMByMaster(EPMDocumentMaster epmDocumentMaster) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (EPMDocument) RemoteMethodServer.getDefault().invoke("getLatestPartByMaster", EPMUtil.class.getName(), null,
							new Class[] { EPMDocumentMaster.class },new Object[] { epmDocumentMaster });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				EPMDocument latestEPMDoc = null;
				try {
					if (epmDocumentMaster != null) {
						QueryResult qr = VersionControlHelper.service.allIterationsOf(epmDocumentMaster);
						if (qr.hasMoreElements()) {
							latestEPMDoc = (EPMDocument) qr.nextElement();
						}
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".getLatestEPMByMaster:"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return latestEPMDoc;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}



	/**
	 * getBigVersionByEPM
	 * @author zhangxj
	 * @param epm
	 * @return
	 */
	public static String getBigVersionByEPM(EPMDocument epm) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
						return (String) RemoteMethodServer.getDefault().invoke("getBigVersionByEPM", EPMUtil.class.getName(), null,new Class[] { WTPart.class },
								new Object[] { epm });
			} else {
						boolean enforce = SessionServerHelper.manager	.setAccessEnforced(false);
						String wt = null;
						try {
								if (epm != null ) {
									wt = epm.getVersionIdentifier().getValue();
								}
							} finally {
								SessionServerHelper.manager.setAccessEnforced(enforce);
							}
						return wt;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	/**
	 * @author zhangxj
	 * @param epmDocument
	 * @return
	 */
	public static boolean ischeckOut(EPMDocument  epmDocument){
		try {
			if (!RemoteMethodServer.ServerFlag) {
				 RemoteMethodServer.getDefault().invoke("ischeckOut", EPMUtil.class.getName(), null,new Class[] { EPMDocument.class },
						new Object[] { epmDocument });
			}else{
				boolean falg=false;
				boolean enforce=SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if(epmDocument != null){
						falg= WorkInProgressHelper.isCheckedOut(epmDocument);
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".ischeckOut"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return falg;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	}
	
	/**
	 * 
	 * @param epmDocument
	 */
	public static void doCheckIn(EPMDocument epmDocument){
        try {
			if (!RemoteMethodServer.ServerFlag) {
			       RemoteMethodServer.getDefault().invoke("doCheckIn", EPMUtil.class.getName(), null, new Class[] { EPMDocument.class},
			        		new Object[] { epmDocument});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try{
					if(epmDocument!=null){
						Workable workable = null;
						if(!WorkInProgressHelper.isWorkingCopy(epmDocument)){
							workable = doCheckOut(epmDocument);//从检出方法中得到已检出工作副本
						}else{
							workable = epmDocument;
						}
							workable = WorkInProgressHelper.service.checkin(workable, "AutoCheckIn");
					}
				} catch(WTPropertyVetoException e){
					logger.error(">>>>>"+e);
				} catch(WTException e){
					logger.error(">>>>>"+e);
				}finally {
			        SessionServerHelper.manager.setAccessEnforced(enforce);
			    }
			}
		} catch (RemoteException e) {
		    logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
		    logger.error(e.getMessage(),e);
		}
	}
		
	/**
	 * 
	 * @param epmDocumen
	 * @return
	 */
	public static Workable doCheckOut(Workable epmDocumen){
        try {
			if (!RemoteMethodServer.ServerFlag) {
			       return (Workable)RemoteMethodServer.getDefault().invoke("doCheckOut", EPMUtil.class.getName(), null, new Class[] { EPMDocument.class},
			        		new Object[] { epmDocumen});
			} else {
				Workable workable = null;
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try{
					if(epmDocumen!=null){
						if(epmDocumen instanceof Iterated){
							Iterated it = (Iterated) epmDocumen;
							epmDocumen=(EPMDocument) VersionControlHelper.service.getLatestIteration(it, false);
							Boolean checkOutFlag=WorkInProgressHelper.isCheckedOut(epmDocumen);
							if(checkOutFlag){ 
								if(!WorkInProgressHelper.isWorkingCopy(epmDocumen)){
							         workable=WorkInProgressHelper.service.workingCopyOf(epmDocumen);
								}else{
									workable = epmDocumen;
								}
							}else{
								Folder myFolder= WorkInProgressHelper.service.getCheckoutFolder();
								CheckoutLink link = WorkInProgressHelper.service.checkout(epmDocumen, myFolder, "AutoCheckOut");
								workable = link.getWorkingCopy();
							}
						}	
					}
				} catch(WTPropertyVetoException e){
					logger.error(CLASSNAME+".doCheckOut:"+e);
				} catch(WTException e){
					logger.error(CLASSNAME+".doCheckOut:"+e);
				} finally {
			        SessionServerHelper.manager.setAccessEnforced(enforce);
			    }
				return workable;
			}
		} catch (RemoteException e) {
		    logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
		    logger.error(e.getMessage(),e);
		}
		return null;
	}
	/**
	 * 
	 * @param epmDocumen
	 */
	public static void doCheckIn(Workable epmDocumen){
        try {
			if (!RemoteMethodServer.ServerFlag) {
			       RemoteMethodServer.getDefault().invoke("doCheckIn", EPMUtil.class.getName(), null, new Class[] { Workable.class},
			        		new Object[] { epmDocumen});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try{
					if(epmDocumen!=null){
						Workable workable = null;
						if(!WorkInProgressHelper.isWorkingCopy(epmDocumen)){
							workable = doCheckOut(epmDocumen);//从检出方法中得到已检出工作副本
						}else{
							workable = epmDocumen;
						}
							workable = WorkInProgressHelper.service.checkin(workable, "AutoCheckIn");
					}
				} catch(WTPropertyVetoException e){
					logger.error(CLASSNAME+".doCheckIn："+e);
				} catch(WTException e){
					logger.error(CLASSNAME+".doCheckIn："+e);
				}finally {
			        SessionServerHelper.manager.setAccessEnforced(enforce);
			    }
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	}

	/**
	 * 
	 * 更新EPM文档
	 * <功能详细描述>
	 * @author  zhangxj
	 * @see [类、类#方法、类#成员]
	 */
	@SuppressWarnings("deprecation")
	public static void updateEPMDocument(EPMDocument epmDocument,String newNumber,String newName) throws WTPropertyVetoException{
	    try {
			if (!RemoteMethodServer.ServerFlag) {
			        RemoteMethodServer.getDefault().invoke("updateEPMDocument", EPMUtil.class.getName(), null, new Class[] { EPMDocument.class,String.class,String.class},
			        		new Object[] { epmDocument,newNumber,newName});
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if(epmDocument==null){
						return;
					}
				EPMDocumentMaster epmDocumentMaster = (EPMDocumentMaster) epmDocument.getMaster();
					if(!StringUtil.isEmpty(newName)){
						epmDocumentMaster= EPMDocumentHelper.service.changeCADName(epmDocumentMaster, newName);
						//这个缺少更新编号， 需要补充
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".updateEPMDocument:"+e);
				} finally {
			        SessionServerHelper.manager.setAccessEnforced(enforce);
			    }
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 
	 * @param epmDocument
	 * @param state
	 */
	public static void SetLifeCycle(EPMDocument epmDocument,String state){
		try {
			if (!RemoteMethodServer.ServerFlag) {
			    RemoteMethodServer.getDefault().invoke("SetLifeCycle", EPMUtil.class.getName(), null, new Class[] { EPMDocument.class,String.class},
			    		new Object[] { epmDocument,state});
			}else{
				try {
					if(epmDocument != null && !StringUtils.isEmpty(state)){
						epmDocument = (EPMDocument) LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) epmDocument,State.toState(state));
						PersistenceHelper.manager.refresh(epmDocument);
					}
				} catch (WTInvalidParameterException e) {
					logger.error(CLASSNAME+".SetLifeCycle:"+e);
				} catch (LifeCycleException e) {
					logger.error(CLASSNAME+".SetLifeCycle:"+e);
				} catch (ObjectNoLongerExistsException e) {
					logger.error(CLASSNAME+".SetLifeCycle:"+e);
				} catch (WTException e) {
					logger.error(CLASSNAME+".SetLifeCycle:"+e);
				}
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	}
	public static void main(String[] args) throws RemoteException, InvocationTargetException {
			RemoteMethodServer server = RemoteMethodServer.getDefault();
			server.setUserName("wcadmin");
			server.setPassword("wcadmin");
			server.invoke("test", EPMUtil.class.getName(), null,new Class[] {}, new Object[] {});
	}
	
	public static void test() throws WTException, WTPropertyVetoException {
		// System.out.println("@@"+getObjectByNumber("测试098"));
		// getObjectByNumber("测试099");
		// String oidString="VR:wt.epm.EPMDocument:7412137";

		// System.out.println("$$$$$$$$$"+geEPMDocumentByOid(oidString));
		// System.out.println("!!!!!!!!!!"+geEPMDocumentByName(EPMName));
		// System.out.println(getEPMDocByNumber(EPMNum));
		// EPMDocument EPMDoc = getEPMDocByNumber(EPMNum);
		// System.out.println(EPMDoc);
		// System.out.println("^^^^^"+getTypeByEPMDoc(EPMDoc));
		// getCADDocumentsByPart(PartUtil.getPartByNumber("ceshisdfsdf"));
//		 EPMDocument epmDocument= getEPMDocByNumber("0000000021");
		// System.out.println(epmDocument.getNumber());
		// System.out.println(epmDocument.getName());
		// System.out.println(epmDocument.getType());
		// System.out.println("getEPMNumber>>>"+getEPMNumber(epmDocument));
		// System.out.println("getPartMasterByNumber>>>>"+getPartMasterByNumber("CAD测试01"));
		// System.out.println("getBigVersionByEPM>>>"+getBigVersionByEPM(epmDocument));
//		System.out.println("getLifeCycleState>>>>"+getLifeCycleState(epmDocument));
//		// doCheckOut(epmDocument);
//		 //ischeckOut(epmDocument);
//		// doCheckIn(epmDocument);
//		System.out.println(epmDocument.getName()+"&&&&&&&&&&"+epmDocument.getNumber());
//		SetLifeCycle(epmDocument,"INWORK");
//		System.out.println("getLifeCycleState>>>>"+getLifeCycleState(epmDocument));
//		updateEPMDocument(epmDocument,"xiaojie.dwg","teshi");
		
	}
}
