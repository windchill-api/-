package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.epm.EPMDocument;
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
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pds.StatementSpec;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionServerHelper;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTRuntimeException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.sessioniteration.sessioniterationResource;
import wt.vc.struct.IteratedDescribeLink;

import com.google.gwt.rpc.client.impl.RemoteException;

public class EPMUtil implements RemoteAccess {
	private static final int[] DEFAULT_CONDITION_ARRAY = new int[] { 0 };
	private final static String CLASSNAME = EPMUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	// Logger
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);

	public static void main(String[] args) {
		try {
			RemoteMethodServer server = RemoteMethodServer.getDefault();
			server.setUserName("wcadmin");
			server.setPassword("wcadmin");
			try {
				// server.invoke("setPartTenclosure",
				// Test.class.getName(), null, new Class[]
				// {String.class},
				// new Object[] {args[0]});
				server.invoke("test", EPMUtil.class.getName(), null,
						new Class[] {}, new Object[] {});
			} catch (java.rmi.RemoteException e) {
				e.printStackTrace();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static void test() throws Exception {
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
		// EPMDocument epmDocument= getEPMDocByNumber("0000000021");
		// System.out.println(epmDocument.getNumber());
		// System.out.println(epmDocument.getName());
		// System.out.println(epmDocument.getType());
		// System.out.println("getEPMNumber>>>"+getEPMNumber(epmDocument));
		// System.out.println("getPartMasterByNumber>>>>"+getPartMasterByNumber("CAD测试01"));
		// System.out.println("getBigVersionByEPM>>>"+getBigVersionByEPM(epmDocument));
		// System.out.println("getLifeCycleState>>>>"+getLifeCycleState(epmDocument));
	}

	public static boolean isEPMDoc(String strNumber) {
		EPMDocument EPMDoc = null;
		if (!StringUtils.isEmpty(strNumber)) {
			try {
				EPMDoc = getEPMDocByNumber(strNumber);
			} catch (WTException e) {
				logger.error(">>>>>" + e);
			}
		}
		if (EPMDoc == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * getCADDocumentsByPart
	 * 
	 * @author zhangxj
	 * @param part
	 * @return
	 * @throws WTException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static List<EPMDocument> getCADDocumentsByPart(WTPart part)
			throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (List<EPMDocument>) RemoteMethodServer.getDefault()
						.invoke("getCADDocumentsByPart",
								EPMUtil.class.getName(), null,
								new Class[] { WTPart.class },
								new Object[] { part });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				List<EPMDocument> epmDocs = new ArrayList<EPMDocument>();
				EPMDocument epmDoc = null;
				try {
					if (part != null) {
						QueryResult result = PartDocHelper.service
								.getAssociatedDocuments(part);
						while (result.hasMoreElements()) {
							WTObject obj = (WTObject) result.nextElement();
							if (obj instanceof EPMDocument) {
								epmDoc = (EPMDocument) obj;
								epmDocs.add(epmDoc);
								LOGGER.debug("编号为...." + part.getNumber()
										+ "的部件关联的三维图纸(EPMDocument)的编号是....."
										+ epmDoc.getNumber());
							} else if (obj instanceof IteratedDescribeLink) {
								obj = (WTObject) ((IteratedDescribeLink) obj)
										.getDescribedBy();
								epmDoc = (EPMDocument) obj;
								epmDocs.add(epmDoc);
								LOGGER.debug("编号为...."
										+ part.getNumber()
										+ "的部件关联的三维图纸(IteratedDescribeLink)的编号是....."
										+ epmDoc.getNumber());
							}
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("e>>>"+e);
				} finally {
					SessionServerHelper.manager
							.setAccessEnforced(isAccessEnforced);
				}
				return epmDocs;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getTypeByEPMDoc by EPMDoc
	 * 
	 * @param EPMDoc
	 * @return
	 */
	public static String getTypeByEPMDoc(EPMDocument EPMDoc) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke(
						"getTypeByEPMDoc", EPMUtil.class.getName(), null,
						new Class[] { EPMDocument.class },
						new Object[] { EPMDoc });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				String EPMDocType = null;
				try {
					if (EPMDoc != null && isEPMDoc(EPMDoc.getNumber())) {
						EPMDocType = TypedUtilityServiceHelper.service
								.getExternalTypeIdentifier(EPMDocType)
								.toString();
					}
				} catch (java.rmi.RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					SessionServerHelper.manager
							.setAccessEnforced(isAccessEnforced);
				}
				return EPMDocType;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				RemoteMethodServer.getDefault().invoke("changeLocation",
						EPMUtil.class.getName(), null,
						new Class[] { EPMDocument.class, String.class },
						new Object[] { epm, folder1 });
			} else {
				boolean accessFlag = SessionServerHelper.manager
						.isAccessEnforced();
				try {
					String oldFolder = epm.getLocation();
					String oldContext = epm.getContainerName();
					String folder = folder1;
					WTContainer wtcontainer =WCUtil.getWTContainerByName(oldContext);
					WTContainerRef wtcRef = WTContainerRef
							.newWTContainerRef(wtcontainer);
					LOGGER.debug("图纸:" + epm.getNumber() + "原有的存储路径为...."
							+ oldFolder);
					if (!oldFolder.equals(folder)) {
						Folder newfolder = null;
						newfolder = FolderHelper.service.getFolder(folder,
								wtcRef);
						epm = (EPMDocument) FolderHelper.service.changeFolder(
								(FolderEntry) epm, newfolder);
						PersistenceHelper.manager.refresh(epm);
					}
				} catch (ObjectNoLongerExistsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					SessionServerHelper.manager.setAccessEnforced(accessFlag);
				}
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				return (EPMDocument) RemoteMethodServer.getDefault().invoke(
						"changeLocation", EPMUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] { oid });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				EPMDocument EPMDoc = null;
				if (!StringUtils.isEmpty(oid)) {
					ReferenceFactory rf = new ReferenceFactory();
					try {
						EPMDoc = (EPMDocument) rf.getReference(oid).getObject();
					} catch (WTRuntimeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (WTException e) {
						// TODO Auto-generated catch block
						logger.error("e>>>"+e);
						e.printStackTrace();
					} finally {
						SessionServerHelper.manager
								.setAccessEnforced(isAccessEnforced);
					}
				}
				return EPMDoc;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public static List<EPMDocument> geEPMDocumentByName(String EPMDocName)
			throws WTException {
		try {
			List<EPMDocument> EPMList = new ArrayList<EPMDocument>();
			if (!RemoteMethodServer.ServerFlag) {
				return (List<EPMDocument>) RemoteMethodServer.getDefault()
						.invoke("geEPMDocumentByName", EPMUtil.class.getName(),
								null, new Class[] { String.class },
								new Object[] { EPMDocName });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				try {
					if (!StringUtils.isEmpty(EPMDocName)) {
						QuerySpec qs = new QuerySpec(EPMDocument.class);
						SearchCondition sc = new SearchCondition(
								EPMDocument.class, EPMDocument.NAME,
								SearchCondition.EQUAL, EPMDocName, false);
						qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
						QueryResult qr = PersistenceServerHelper.manager
								.query((StatementSpec) qs);
						qr = new LatestConfigSpec().process(qr);
						EPMDocument EPMDoc = null;
						while (qr.hasMoreElements()) {
							EPMDoc = (EPMDocument) qr.nextElement();
							EPMList.add(EPMDoc);
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("e>>>"+e);
				} finally {
					SessionServerHelper.manager
							.setAccessEnforced(isAccessEnforced);
				}
				return EPMList;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public static EPMDocument getEPMDocByNumber(String EPMNum)
			throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (EPMDocument) RemoteMethodServer.getDefault().invoke(
						"getEPMDocByNumber", EPMUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] { EPMNum });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				EPMDocument EPMDoc = null;
				try {
					if ("".equals(EPMNum) || null == EPMNum) {
						logger.error("请检查EPM编号是否正确");
					} else {
						QuerySpec querySpec = new QuerySpec(EPMDocument.class);
						EPMNum = EPMNum.toUpperCase();
						WhereExpression searchCondition = new SearchCondition(
								EPMDocument.class, EPMDocument.NUMBER,
								SearchCondition.EQUAL, EPMNum, false);
						querySpec.appendWhere(searchCondition,
								DEFAULT_CONDITION_ARRAY);
						QueryResult queryResult = PersistenceHelper.manager
								.find((StatementSpec) querySpec);
						while (queryResult.hasMoreElements()) {
							EPMDoc = (EPMDocument) queryResult.nextElement();
						}
					}
				} catch (Exception e) {
					logger.error("e>>>"+e);
				} finally {
					SessionServerHelper.manager
							.setAccessEnforced(isAccessEnforced);
				}
				return EPMDoc;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public static List<EPMDocument> get2DEPM(EPMDocument epm)
			throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (List<EPMDocument>) RemoteMethodServer.getDefault()
							.invoke("get2DEPM", EPMUtil.class.getName(), null,
									new Class[] { EPMDocument.class },
									new Object[] { epm });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				QueryResult qr = null;
				List<EPMDocument> result = new ArrayList<EPMDocument>();
				try {
					if (epm == null) {
						logger.error("epm为空>>>");
						return null;
					} else {
						qr = PersistenceHelper.manager.navigate(epm.getMaster(),
								"referencedBy",
								wt.epm.structure.EPMReferenceLink.class, false);
						while (qr.hasMoreElements()) {
							EPMReferenceLink epmreferencelink = (EPMReferenceLink) qr
									.nextElement();
							EPMDocument epm2d = (EPMDocument) epmreferencelink
									.getOtherObject(epm.getMaster());
							if (epm2d != null) {
								LOGGER.debug("编号为:" + epm.getNumber()
										+ "的三维图纸关联的二维图纸编号为....."
										+ epm2d.getNumber());
								result.add(epm2d);
							}
						}
						return result;
					}
				} catch (Exception e) {
					logger.error(e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getEPMNumber by epmDocument
	 * 
	 * @param epmDocument
	 * @return name
	 */
	public static String getEPMNumber(EPMDocument epmDocument) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (String) RemoteMethodServer.getDefault().invoke(
							"getEPMNumber", EPMUtil.class.getName(), null,
							new Class[] { EPMDocument.class },
							new Object[] { epmDocument });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				String number = null;
				try {
					if (epmDocument != null && !"".equals(epmDocument.getNumber())) {
						number = epmDocument.getNumber();
					}
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("e>>>"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
				return number;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getEPMName by epmDocument
	 * 
	 * @param epmDocument
	 * @return name
	 */
	public static String getEPMName(EPMDocument epmDocument) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (String) RemoteMethodServer.getDefault().invoke(
							"getEPMName", EPMUtil.class.getName(), null,
							new Class[] { EPMDocument.class },
							new Object[] { epmDocument });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				String name = null;
				try {
					if (epmDocument != null && !"".equals(epmDocument.getName())) {
						name = epmDocument.getNumber();
					}
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("e>>>"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
				return name;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getPartMasterByNumber
	 * 
	 * @param partNo
	 * @return epmDocumentMaster
	 * @throws WTException
	 */
	public static EPMDocumentMaster getPartMasterByNumber(String EPMDocName)
			throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (EPMDocumentMaster) RemoteMethodServer.getDefault()
							.invoke("getPartMasterByNumber",
									EPMUtil.class.getName(), null,
									new Class[] { String.class },
									new Object[] { EPMDocName });
			} else {
				boolean enforce = SessionServerHelper.manager
						.setAccessEnforced(false);
				EPMDocumentMaster epmDocumentMaster = null;
				try {
					QuerySpec querySpec = new QuerySpec(EPMDocumentMaster.class);
					WhereExpression searchCondition = new SearchCondition(
							EPMDocumentMaster.class, EPMDocumentMaster.NUMBER,
							SearchCondition.EQUAL, EPMDocName, false);
					querySpec.appendWhere(searchCondition, new int[] { 0 });
					QueryResult queryResult = PersistenceHelper.manager
							.find((StatementSpec) querySpec);
					while (queryResult.hasMoreElements()) {
						epmDocumentMaster = (EPMDocumentMaster) queryResult
								.nextElement();
					}
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("e>>>"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return epmDocumentMaster;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getLatestEPMByMaster
	 * 
	 * @param epmDocumentMaster
	 * @return latestEPMDoc
	 * @throws WTException
	 */
	public static EPMDocument getLatestEPMByMaster(
			EPMDocumentMaster epmDocumentMaster) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (EPMDocument) RemoteMethodServer.getDefault().invoke(
							"getLatestPartByMaster", EPMUtil.class.getName(), null,
							new Class[] { EPMDocumentMaster.class },
							new Object[] { epmDocumentMaster });
			} else {
				boolean enforce = SessionServerHelper.manager
						.setAccessEnforced(false);
				EPMDocument latestEPMDoc = null;
				try {
					if (epmDocumentMaster != null
							&& !"".equals(epmDocumentMaster.getNumber())) {
						QueryResult qr = VersionControlHelper.service
								.allIterationsOf(epmDocumentMaster);
						if (qr.hasMoreElements()) {
							latestEPMDoc = (EPMDocument) qr.nextElement();
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("e>>>"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return latestEPMDoc;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getLifeCycleState by EPM
	 * 
	 * @param EPM
	 * @return state
	 */
	public static wt.lifecycle.State getLifeCycleState(EPMDocument epm) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (wt.lifecycle.State) RemoteMethodServer.getDefault()
							.invoke("getLifeCycleState", EPMUtil.class.getName(),
									null, new Class[] { EPMDocument.class },
									new Object[] { epm });
			} else {
				boolean enforce = SessionServerHelper.manager
						.setAccessEnforced(false);
				wt.lifecycle.State state = null;
				try {
					if (epm != null && !"".equals(epm.getNumber())) {
						state = epm.getLifeCycleState();
					}
				} catch (Exception e) {
					logger.error(">>>>>" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return state;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getBigVersionByEPM
	 * 
	 * @param epm
	 * @return
	 */
	public static String getBigVersionByEPM(EPMDocument epm) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (String) RemoteMethodServer.getDefault().invoke(
							"getBigVersionByEPM", EPMUtil.class.getName(), null,
							new Class[] { WTPart.class }, new Object[] { epm });
			} else {
				boolean enforce = SessionServerHelper.manager	.setAccessEnforced(false);
				String wt = null;
				try {
					if (epm != null && !"".equals(epm.getNumber())) {
						wt = epm.getVersionIdentifier().getValue();
					}
					return wt;
				} catch (Exception e) {
					logger.error(">>>>>" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}
