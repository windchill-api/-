package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.Foldered;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTOrganization;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartMasterIdentity;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.query.WhereExpression;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.TeamReference;
import wt.type.TypedUtility;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

/**
 * this PartUtil includes create part,delete part,update part,get part, and some basic things of part
 * @author WangY
 */
public class PartUtil implements RemoteAccess, Serializable {
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = PartUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * judge whether part exist by part number
	 * @param strNumber
	 * @return	if strNumber exist in windChill, return true;	such as: strNumber = "0000000022"
     * 				else return false;	such as: strNumber = "asd" or strNumber = ""  or strNumber = null
	 */
	public static Boolean isPartExist(String strNumber) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (Boolean) RemoteMethodServer.getDefault().invoke("isPartExist", PartUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] { strNumber });
			} else {
				boolean flag = false;
 	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		    	try {
		    		if (!StringUtils.isEmpty(strNumber)) {
			    		WTPartMaster wtpartmaster = getPartMasterByNumber(strNumber);
						if (wtpartmaster != null) {
							flag = true;
				        }
			        }
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
     * get part by oid
     * @param oid
     * @return	if oid exist in windChill, return WTPart;
	 * 				else if oid is not exist in windChill, throw exception and return null; such as oid = "VR:wt.part.WTPart:asdf"
	 * 							or oid = "asdf"; 
	 * 				else if oid is null or "",return null
     */
	public static WTPart getPartByOid(String oid) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTPart) RemoteMethodServer.getDefault().invoke("getPartByOid", PartUtil.class.getName(), null,
						new Class[] { String.class }, new Object[] { oid });
	        } else {
	        	WTPart part = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (!StringUtils.isEmpty(oid)) {
						ReferenceFactory rf = new ReferenceFactory();
						part = (WTPart) rf.getReference(oid).getObject();
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".isPartExist:"+e);
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return part;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get part by partNumber
	 * if partNumber exist in windChill,return WTPart;	such as:partNumber = "0000000041"
	 * 		else return null	such as 
	 */
	public static WTPart getPartByNumber(String partNumber) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTPart) RemoteMethodServer.getDefault().invoke("getPartByNumber", PartUtil.class.getName(),
						null, new Class[] { String.class }, new Object[] { partNumber });
	        } else {
	        	WTPart part = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
					if (!StringUtils.isEmpty(partNumber) && isPartExist(partNumber)) {
						QuerySpec qs = new QuerySpec(WTPart.class);
						WhereExpression  we = new SearchCondition(WTPart.class,WTPart.NUMBER, SearchCondition.EQUAL, partNumber);
						qs.appendWhere(we, new int[] { 0 });
						QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
						LatestConfigSpec cfg = new LatestConfigSpec();  //构建过滤器
						QueryResult qr1 = cfg.process(qr); //按小版本排序
						if (qr1.hasMoreElements()) {
							part =(WTPart) qr1.nextElement(); //获取最新小版本的WTPart对象
						}
					}
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".getPartByNumber:"+e);
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return part;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
    /**
     * get part by number,name,state,type,view
     * @param number
     * @param name
     * @param state
     * @param type
     * @return	if number,name,state,type one of them exist in windChill or they all empty,return parts;	
     * 				such as:number = "0000000022", name = "002", state = "INWORK", type = "wt.part.WTPart|wt.part.SubPart"
     * 			else return []
     * 				such as:getPartByProperties("asd","asd", "asd","asd")
     * @throws WTException
     */
    @SuppressWarnings("unchecked")
	public static List<WTPart> getPartByProperties(String number, String name, String state, String type) {
    	try {
    		if (!RemoteMethodServer.ServerFlag) {
    			return (List<WTPart>) RemoteMethodServer.getDefault().invoke("getPartByProperties", PartUtil.class.getName(), null, 
    					new Class[] { String.class, String.class, String.class, String.class }, new Object[] { number, name, state, type });
	        } else {
	        	List<WTPart> partList = new ArrayList<WTPart>();
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	int count = 0;
	        	WTPart part = null;
	        	try {
		        	QuerySpec qs = new QuerySpec(WTPart.class);
		        	if (!StringUtils.isEmpty(number)) {
		        		SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER,SearchCondition.EQUAL, number, false);
				        qs.appendWhere(sc, new int[] { 0 });
				        count++;
		        	}
		        	if (!StringUtils.isEmpty(name)) {
		        		if(count != 0) {
		        			qs.appendAnd();
		        		}
		        		SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NAME,SearchCondition.EQUAL, name, false);
				        qs.appendWhere(sc, new int[] { 0 });
				        count++;
		        	}
		        	QueryResult qr = PersistenceServerHelper.manager.query((StatementSpec) qs);
			        qr = new LatestConfigSpec().process(qr);
		        	while (qr.hasMoreElements()) {
		        		part = (WTPart) qr.nextElement();
			            String ibaType = "";
			            String partState = "";
			            if (!StringUtils.isEmpty(type)) {
			            	ibaType = TypedUtility.getTypeIdentifier(part).getTypename();
			            }
			            if (!StringUtils.isEmpty(state)) {
			            	partState = part.getState().toString();
			            }
			            if (partState.indexOf(state) != -1 && ibaType.indexOf(type) != -1) {
			                partList.add(part);
			            } else if (partState.indexOf(state) != -1) {
			            	partList.add(part);
			            } else if (ibaType.indexOf(type) != -1) {
			            	partList.add(part);
			            }
			        }
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".getPartByProperties:"+e);
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return partList;
        	}
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * Get part master by part number
     * @param partNo
     * @return	if partNo exist in windChill, return WTPartMaster,	such as:partNo = "0000000022"
     * 				else return null	such as:partNo = "asd" or partNo = "" or partNo = null
     * @throws WTException
     */
    public static WTPartMaster getPartMasterByNumber(String partNo) {
        try {
	        if (!RemoteMethodServer.ServerFlag) {
	        	return (WTPartMaster) RemoteMethodServer.getDefault().invoke("getPartMasterByNumber", 
	        				PartUtil.class.getName(), null, new Class[] { String.class },
	                		new Object[] { partNo });
	        } else {
	        	WTPartMaster partMaster = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		QuerySpec querySpec = new QuerySpec(WTPartMaster.class);
			        WhereExpression searchCondition = new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, 
			        		SearchCondition.EQUAL, partNo, false);
			        querySpec.appendWhere(searchCondition,new int[] { 0 });
			        QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec)querySpec);
			        while (queryResult.hasMoreElements()) {
			            partMaster = (WTPartMaster) queryResult.nextElement();
			        }
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".getPartMasterByNumber:"+e);
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return partMaster;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }
    
    /**
     * Get the Latest Part by PartMaster
     * 
     * @param partMaster
     * @return		if  partMaster is exist in windChill,return part
     * 				else if partMaster is not exist or partMaster is null,return null
     * @throws WTException
     */
    public static WTPart getLatestPartByMaster(WTPartMaster partMaster) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTPart)RemoteMethodServer.getDefault().invoke("getLatestPartByMaster", PartUtil.class.getName(),
						null, new Class[] { WTPartMaster.class }, new Object[] { partMaster });
	        } else {
	        	WTPart latestPart = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		if (partMaster == null) {
	        			return null;
	        		}
        			QueryResult qr = VersionControlHelper.service.allIterationsOf(partMaster);
			        if (qr.hasMoreElements()) {
			        	latestPart = (WTPart) qr.nextElement();
			        }
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".getLatestPartByMaster:"+e);
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return latestPart;
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }
    
    
    /**
     * get type by part
     * @param part
     * @return	if part exist in windChill,return part type
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
     */
    public static String getTypeByPart(WTPart part) {
    	try {
    		if (!RemoteMethodServer.ServerFlag) {
    			return (String) RemoteMethodServer.getDefault().invoke("getTypeByPart", PartUtil.class.getName(), null, 
    					new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	String partType = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return null;
					}
					partType = TypedUtilityServiceHelper.service.getExternalTypeIdentifier(part).toString();
				} catch (WTException e) {
					logger.error(CLASSNAME+".getTypeByPart:"+e);
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return partType;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get number by part
	 * @param part
	 * @return	if part exist in windChill,return part number
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
	 */
	public static String getNumberByPart(WTPart part) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke("getNumberByPart", PartUtil.class.getName(), null, 
						new Class[] { WTPart.class}, new Object[] { part });
	        } else {
	        	String partNumber = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return null;
					}
					partNumber = part.getNumber();
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return partNumber;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get name by part
	 * @param part
	 * @return	if part exist in windChill,return part name
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
	 */
	public static String getNameByPart(WTPart part) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke("getNameByPart", PartUtil.class.getName(), null,
						new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	String partName = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null){
						return null;
					}
					partName = part.getName();
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return partName;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get bigVersion part by part
	 * @param part
	 * @return	if part exist in windChill,return part bigVersion
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
	 */
	public static String getBigVersionByPart(WTPart part) {
        try {
	        if (!RemoteMethodServer.ServerFlag) {
	        	return (String) RemoteMethodServer.getDefault().invoke("getBigVersionByPart", PartUtil.class.getName(),
	        			null, new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	String bigVersion = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return null;
					}
					bigVersion = part.getVersionIdentifier().getValue();
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return bigVersion;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get smallVersion part by part
	 * @param part
	 * @return	if part exist in windChill,return part SmallVersion
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
	 */
	public static String getSmallVersionByPart(WTPart part) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke("getSmallVersionByPart", PartUtil.class.getName(), 
						null, new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	String smallVersion = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return null;
					}
					smallVersion = part.getIterationIdentifier().getValue();
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return smallVersion;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get subPart by part number
	 * @param number
	 * @return	if number is exist in WindChill and the part has subPart,return subParts
	 * 			else if number is not exist in windChill,return []
	 * 			else if number is null or number is empty,return []
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static List<WTPartMaster> getSubPartByPartNumber(String number) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (List<WTPartMaster>) RemoteMethodServer.getDefault().invoke("getSubPartByPartNumber", 
						PartUtil.class.getName(), null, new Class[] { String.class }, new Object[] { number });
	        } else {
	        	List<WTPartMaster> wtpmList = new ArrayList<WTPartMaster>(); 
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
					if (!StringUtils.isEmpty(number) && isPartExist(number)) {
						QuerySpec qs = new QuerySpec();
						int wtp = qs.appendClassList(WTPart.class,false);
						int wtm = qs.appendClassList(WTPartMaster.class,false);
						int wtu = qs.appendClassList(WTPartUsageLink.class, true);
						String str[] = new String[3];
						str[0] = qs.getFromClause().getAliasAt(wtp);
						str[1] = qs.getFromClause().getAliasAt(wtm);
						str[2] = qs.getFromClause().getAliasAt(wtu);
						TableColumn wtpCol = new TableColumn(str[0], "ida3masterreference");
						TableColumn wtpICol = new TableColumn(str[0], "ida2a2");
						TableColumn wtmCol = new TableColumn(str[1], "ida2a2");
						TableColumn wtuCol = new TableColumn(str[2], "ida3a5");
						WhereExpression w1 = new SearchCondition(wtmCol, SearchCondition.EQUAL, wtpCol);
						WhereExpression w2 = new SearchCondition(wtpICol, SearchCondition.EQUAL, wtuCol); 
						WhereExpression w3 = new SearchCondition(WTPart.class, WTPart.NUMBER, "=", number); 
						qs.appendWhere(w1,new int[] { 0 });
						qs.appendAnd();
						qs.appendWhere(w2,new int[] { 0 });
						qs.appendAnd();
						qs.appendWhere(w3,new int[] { 0 });
						QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
						while (qr.hasMoreElements()) {
							Object[] obj = (Object[]) qr.nextElement();
							WTPartUsageLink wu = (WTPartUsageLink) obj[0];
							WTPartMaster pa = (WTPartMaster)wu.getUses();
							wtpmList.add(pa);
						}
						int l = wtpmList.size();
						for (int i = 0; i < l-1; i++) {
							for (int j = i+1; j < l; j++) {
								if(wtpmList.get(i)==wtpmList.get(j)){
									wtpmList.remove(j);
									l = l-1;
								}
							}
						}
					}
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".getSubPartByPartNumber:"+e);
	        	} finally {
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
	        	return wtpmList;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get discriptionDoc by part
	 * @param part
	 * @return	if part is exist in windChill,return documents
	 * 			else if part is null or part is not exist in windChill,return []
	 */
	@SuppressWarnings("unchecked")
	public static List<WTDocument> getDisDocByPart(WTPart part) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDisDocByPart", 
						PartUtil.class.getName(), null, new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	List<WTDocument> docList = new ArrayList<WTDocument>();
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return null;
					}
					QueryResult qr = WTPartHelper.service.getDescribedByDocuments(part);
					while (qr.hasMoreElements()) {
						Object object = qr.nextElement();
						if (object instanceof WTDocument) {
							WTDocument doc = (WTDocument)object;
							docList.add(doc);
						}
					}
				} catch(WTException e) {
					logger.error(CLASSNAME+".getDisDocByPart:"+e);
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
	 * get referenceDoc by part
	 * @author WangY
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public static List<WTDocumentMaster> getRefDocByPart(WTPart part) {
        try {
	        if (!RemoteMethodServer.ServerFlag) {
	                return (List<WTDocumentMaster>) RemoteMethodServer.getDefault().invoke("getRefDocByPart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	List<WTDocumentMaster> docMasterList = new ArrayList<WTDocumentMaster>();
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return null;
					}
					QueryResult qr = WTPartHelper.service.getReferencesWTDocumentMasters(part);
					while (qr.hasMoreElements()) {
						Object object = qr.nextElement();
					if (object instanceof WTDocumentMaster) {
						WTDocumentMaster doct = (WTDocumentMaster)object;
						docMasterList.add(doct);
						}
					}
				} catch(WTException e) {
					logger.error(CLASSNAME+".getRefDocByPart:"+e);
	        	} finally {
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
				return docMasterList;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * create part 
	 * @param newPartName
	 * @param newPartNumber
	 * @param partNumber
	 * @return	if partNumber exist in windChill and newPartName is not empty,return WTPart;
	 * 					such as:createPart("123","123","0000000022")
	 * 				else if newPartNumber is empty,return WTPart
	 * 					such as:createPart("","1234","0000000022")
	 * 				else return null
	 * 					such as:createPart("asd","","0000000022") or createPart("","1234","asd")
	 */
	public static WTPart createPart(String newPartNumber, String newPartName, String partNumber) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTPart) RemoteMethodServer.getDefault().invoke("createPart", PartUtil.class.getName(), null, 
						new Class[] { String.class ,String.class, String.class }, new Object[] { newPartName, 
						newPartNumber, partNumber });
	        } else {
	        	WTPart part = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (!StringUtils.isEmpty(newPartName) && !StringUtils.isEmpty(partNumber) && isPartExist(partNumber)) {
						part = WTPart.newWTPart();
						part.setName(newPartName);//设置名字
						if (!StringUtils.isEmpty(newPartNumber) && !isPartExist(newPartNumber)) {
							part.setNumber(newPartNumber);//设置编号
						}
						part.setContainer(getPartByNumber(partNumber).getContainer());//设置容器
						PersistenceHelper.manager.save(part);
					}
				} catch(WTException e) {
					logger.error(CLASSNAME+".createPart:"+e);
	        	} catch(WTPropertyVetoException e) {
	        		logger.error(CLASSNAME+".createPart:"+e);
	        	} finally {
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
				return part;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * delete part by part
	 * @param part
	 * 		if part is exist in windChill,delete it
	 * 		else if part is not exist in windChill or part is null,there is nothing to do
	 */
	public static void deletePart(WTPart part) {
        try {
	        if (!RemoteMethodServer.ServerFlag) {
	        	RemoteMethodServer.getDefault().invoke("deletePart", PartUtil.class.getName(), null, new Class[]
	        			{ WTPart.class }, new Object[] { part });
	        } else {
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return;
					}
					PersistenceHelper.manager.delete(part);
				} catch (WTException e) {
					logger.error(CLASSNAME+".deletePart:"+e);
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
	 * update part number,name
	 * @param part
	 * @param newNumber
	 * @param newName
	 * 		if part is exist in windChill,newNumber is not exist in windChill and newName is not empty,update the part
	 * 		else there is nothing to do
	 */
	public static void updatePart(WTPart part, String newNumber, String newName) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("updatePart", PartUtil.class.getName(), null, new Class[]
						{ WTPart.class, String.class, String.class }, new Object[] { part,newNumber,newName });
	        } else {
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return;
					}
					WTPartMaster partMaster = (WTPartMaster) part.getMaster();
					WTOrganization wtorganization = part.getOrganization();
					if (!StringUtils.isEmpty(newNumber) && !isPartExist(newNumber)) {
						partMaster = WTPartHelper.service.changeWTPartMasterIdentity((WTPartMaster) partMaster,	part.getName(),
								newNumber, wtorganization);
					}
					if (!StringUtils.isEmpty(newName)) {
						partMaster = WTPartHelper.service.changeWTPartMasterIdentity((WTPartMaster) partMaster,	newName, 
								part.getNumber(), wtorganization);
					}
				} catch(WTPropertyVetoException e) {
					logger.error(CLASSNAME+".updatePart:"+e);
				} catch (WTException e) {
					logger.error(CLASSNAME+".updatePart:"+e);
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
	 * To determine whether part is checkout 
	 * @param part
	 * @return	if part is exist in windChill and is checkOut,return true
	 * 				else return false
	 */
	public static Boolean isCheckOut(WTPart part) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (Boolean)RemoteMethodServer.getDefault().invoke("isCheckOut", PartUtil.class.getName(),
						null, new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return false;
					}
					flag = WorkInProgressHelper.isCheckedOut(part);
				} catch (WTException e) {
					logger.error(CLASSNAME+".isCheckOut:"+e);
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
	 * To determine whether part is workingCopy
	 * @param part
	 * @return		if part is exist in windChill and is workingCopy,return true
	 * 				else return false
	 */
	public static Boolean isWorkingCopy(WTPart part) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (Boolean)RemoteMethodServer.getDefault().invoke("isWorkingCopy", PartUtil.class.getName(), null,
						new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return false;
					}
					flag = WorkInProgressHelper.isWorkingCopy(part);
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
	 * To determine whether part can modify
	 * @param part
	 * @return		if part is exist in windChill and is workingCopy,return true
	 * 				else return false
	 */
	public static Boolean isModifiable(WTPart part) {
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	               return (Boolean)RemoteMethodServer.getDefault().invoke("isModifiable", PartUtil.class.getName(), null,
	            		   new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return false;
					}
					flag = WorkInProgressHelper.isModifiable(part);
				} catch (WTException e) {
					logger.error(CLASSNAME+".isModifiable:"+e);
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
	 * checkout part and return wokingCopy
	 * @param part
	 * @return		if part is exist in windChill,return workable
	 * 				else if part is not exist in windChill or part is null,return null
	 */
	public static Workable doCheckOut(Workable part) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (Workable)RemoteMethodServer.getDefault().invoke("doCheckOut", PartUtil.class.getName(), null,
						new Class[] { Workable.class },new Object[] { part });
	        } else {
	        	Workable workable = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (part == null) {
						return null;
					}
					if (part instanceof Iterated) {
						Iterated it = (Iterated) part;
						part=(Workable) VersionControlHelper.service.getLatestIteration(it, false);
						Boolean checkOutFlag=WorkInProgressHelper.isCheckedOut(part);
						if (checkOutFlag) { 
							if (!WorkInProgressHelper.isWorkingCopy(part)) {
								workable=WorkInProgressHelper.service.workingCopyOf(part);
							} else {
								workable = part;
							}
						} else {
							Folder myFolder= WorkInProgressHelper.service.getCheckoutFolder();
							CheckoutLink link = WorkInProgressHelper.service.checkout(part, myFolder, "AutoCheckOut");
							workable = link.getWorkingCopy();
						}
					}	
				} catch(WTPropertyVetoException e) {
					logger.error(CLASSNAME+".doCheckOut:"+e);
				} catch(WTException e) {
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
	 * checkIn part
	 * @param part
	 * 		if part is exist in windChill,checkIn part
	 * 		else if part is not exist in windChill or part is null,there is nothing to do
	 */
	public static void doCheckIn(Workable part) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("doCheckIn", PartUtil.class.getName(), null, new Class[] 
						{ Workable.class }, new Object[] { part });
	        } else {
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		if (part == null) {
	        			return;
	        		}
					Workable workable = null;
					if (!WorkInProgressHelper.isWorkingCopy(part)) {
						workable = doCheckOut(part);//从检出方法中得到已检出工作副本
					} else {
						workable = part;
					}
						workable = WorkInProgressHelper.service.checkin(workable, "AutoCheckIn");
	        	} catch(WTPropertyVetoException e) {
	        		logger.error(CLASSNAME+".doCheckIn:"+e);
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".doCheckIn:"+e);
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
	 * revise WTPart by part
	 * @param part
	 * @param comment
	 * @return	if part is exist in windChill,revise part
	 * 			else if part is not exist in windChill or part is null,return null
	 * @throws WTException
	 */
    @SuppressWarnings("deprecation")
	public static WTPart reviseWTPart(WTPart part, String comment) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTPart)RemoteMethodServer.getDefault().invoke("reviseWTPart", PartUtil.class.getName(), null, 
						new Class[] { WTPart.class, String.class}, new Object[] { part, comment });
	        } else {
	        	WTPart wtpart = part;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (wtpart == null) {
		        		return null;
		            }
		            WTContainer container = wtpart.getContainer();
		            WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container);
		            TeamReference teamReference = wtpart.getTeamId();
		            Folder oldFoler = FolderHelper.getFolder(wtpart);// wtpart.getFolderingInfo().getFolder();
		            if (oldFoler == null) {
		                String strLocation = wtpart.getLocation();
		                oldFoler = FolderHelper.service.getFolder(strLocation, containerRef);
		            }
		            wtpart = (WTPart) VersionControlHelper.service.newVersion((Versioned) wtpart);
		            if (wtpart != null) {
		                wtpart.setTeamId(teamReference);
		            }
		            VersionControlHelper.setNote(wtpart, comment);
		            wtpart.setContainer(container);
		            FolderHelper.assignFolder((Foldered) wtpart, oldFoler);
		            wtpart = (WTPart) PersistenceHelper.manager.save(wtpart);
		            wtpart = (WTPart) PersistenceHelper.manager.refresh(wtpart);
		        } catch (WTPropertyVetoException e) {
		        	logger.error(CLASSNAME+".reviseWTPart:"+e);
		        } catch (WTException e) {
		        	logger.error(CLASSNAME+".reviseWTPart:"+e);
		        } finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
		        return wtpart;
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }
	

	/**
     * this method is used to get working version of part.(check out part).
     * @param part
     * @return	if part is exist,return wokingCopy part
     * 				else return null
     */
    public static WTPart getWorkingCopyOfPart(WTPart part) {
    	try {
    		if (!RemoteMethodServer.ServerFlag) {
    			return (WTPart)RemoteMethodServer.getDefault().invoke("getWorkingCopyOfPart", PartUtil.class.getName(), 
    					null, new Class[] { WTPart.class }, new Object[] { part });
	        } else {
	        	WTPart workingPart = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (part == null) {
		        		return null;
		        	}
	        		if (!WorkInProgressHelper.isCheckedOut(part)) {
	        			Folder folder = WorkInProgressHelper.service.getCheckoutFolder();
			            CheckoutLink checkoutlink = WorkInProgressHelper.service.checkout(part, folder, 
			            		SessionHelper.manager.getPrincipal().getName());
			            workingPart = (WTPart) checkoutlink.getWorkingCopy();
			        } else {
			        	if (!WorkInProgressHelper.isWorkingCopy(part)) {
			        		workingPart = (WTPart) WorkInProgressHelper.service.workingCopyOf(part);
			            } else {
			            	workingPart = part;
			            }
			        }
		        } catch(WTException e) {
		        	logger.error(CLASSNAME+".getWorkingCopyOfPart:"+e);
		        } catch(WTPropertyVetoException e) {
		        	logger.error(CLASSNAME+".getWorkingCopyOfPart:"+e);
		        } finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
		        return workingPart;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }
    
    /**
     * Judge if the two given parts has usage link between them
     * @param parentPart
     * @param childMaster
     * @return		if parentPart and childMaster is exist in windChill and has link ,return true
     * 				else return false
     * @throws WTException
     */
    public static Boolean hasUsageLink(WTPart parentPart, WTPartMaster childMaster) {
    	try {
        	if (!RemoteMethodServer.ServerFlag) {
        		return (Boolean)RemoteMethodServer.getDefault().invoke("hasUsageLink", PartUtil.class.getName(), null,
        				new Class[] { WTPart.class, WTPartMaster.class }, new Object[] { parentPart, childMaster });
	        } else {
	        	boolean hasUsageLink = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        WTPartUsageLink usageLink;
				try {
					if (parentPart == null || childMaster == null) {
						return false;
					}
					usageLink = getPartUsageLink(parentPart, childMaster);
					if (usageLink == null){
						return false;
					}
		        	hasUsageLink = true;
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return hasUsageLink;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return false;
    }
    
    /**
     * Get part usage link
     * 
     * @param parentPart
     *            parent part
     * @param childMaster
     *            child part master
     * @return if parentPart and childMaster is exist in windChill,childMaster is the subPartMaster,return UsageLink
     * 		else return null
     * @throws WTException
     *             Windchill exception
     */
    public static WTPartUsageLink getPartUsageLink(WTPart parentPart,WTPartMaster childMaster) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTPartUsageLink)RemoteMethodServer.getDefault().invoke("getPartUsageLink", PartUtil.class.getName(),
						null, new Class[] { WTPart.class,WTPartMaster.class }, new Object[] { parentPart,childMaster });
	        } else {
	        	WTPartUsageLink usageLink = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		if (parentPart == null || childMaster == null) {
						return null;
					}
		            QueryResult queryresult = PersistenceHelper.manager.find(WTPartUsageLink.class, parentPart,
						        WTPartUsageLink.USED_BY_ROLE, childMaster);
		            if (queryresult != null && queryresult.size() != 0) {
		            	usageLink = (WTPartUsageLink) queryresult.nextElement();
		            }
	        	} catch (WTException e) {
	        		logger.error(CLASSNAME+".getPartUsageLink:"+e);
	        	} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
	        	return usageLink;
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }
    
    /**
     * Create part usage link
     * 
     * @param parentPart
     *            parent part
     * @param childMaster
     *            child part master
     *
     * @return 		if parentPart and childMaster is exist in windChill,return usageLink
     * 				else return null
     * @throws WTException
     *             WindChill exception
     */
    public static WTPartUsageLink createUsageLink(WTPart parentPart, WTPartMaster childMaster) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTPartUsageLink)RemoteMethodServer.getDefault().invoke("createUsageLink", PartUtil.class.getName(),
						null, new Class[] { WTPart.class, WTPartMaster.class }, new Object[] { parentPart,childMaster });
	        } else {
	        	WTPartUsageLink newLink = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		if (parentPart == null || childMaster == null) {
						return null;
					}
			    	if (getSubPartByPartNumber(parentPart.getNumber()).contains(childMaster)) {
			    		newLink = getPartUsageLink(parentPart, childMaster);
			    	} else {
			    		newLink = WTPartUsageLink.newWTPartUsageLink(parentPart, childMaster);
			            PersistenceServerHelper.manager.insert(newLink);
			    	}
	        	} catch (WTException e) {
	        		logger.error(CLASSNAME+".createUsageLink:"+e);
	        	} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
	        	return newLink;
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }
    
    /**
     * Remove WTPartUsageLink between childPart and root
     * @param childPart
     * @param root
     * @throws WTException
     * 		if root and childPart is exist in windChill,childPart is the subPart,remove the childPart
     * 		else there is nothing to do
     */
    public static void removeUseLink(WTPart childPart, WTPart root) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("removeUseLink", PartUtil.class.getName(), null, new Class[] 
						{ WTPart.class,WTPart.class }, new Object[] { childPart,root });
	        } else {
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		if (childPart == null || root == null) {
	        			return;
	        		}
        			WTPartMaster partMaster = (WTPartMaster) childPart.getMaster();
			        QuerySpec queryspec = new QuerySpec(WTPartUsageLink.class);
			        queryspec.appendWhere( new SearchCondition(WTPartUsageLink.class, "roleAObjectRef.key", "=", 
			        		PersistenceHelper.getObjectIdentifier(root)), new int[] { 0 });
			        queryspec.appendAnd();
			        queryspec.appendWhere( new SearchCondition(WTPartUsageLink.class, "roleBObjectRef.key", "=", 
			        		PersistenceHelper.getObjectIdentifier(partMaster)), new int[] { 0 });
			        QueryResult qr = PersistenceServerHelper.manager.query(queryspec);
			        while (qr.hasMoreElements()) {
			        	WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			            PersistenceServerHelper.manager.remove(link);
			        }
	        	} catch (WTException e) {
	        		logger.error(CLASSNAME+".removeUseLink:"+e);
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
     * remove all useLink
     * 
     * @param root
     * @throws WTException
     * 		if root is exist in windChill and has subParts,remove those subParts
     * 		else there is nothing to do 
     */
    public static void removeAllUseLink(WTPart root) {
    	try { 
    		if (!RemoteMethodServer.ServerFlag) {
    			RemoteMethodServer.getDefault().invoke("removeAllUseLink", PartUtil.class.getName(), null, new Class[] 
    					{ WTPart.class }, new Object[] { root });
	        } else {
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
			    	if (root == null) {
			    		return;
			    	}
		    		QueryResult qr = WTPartHelper.service.getUsesWTParts(root, WTPartHelper.service.findWTPartConfigSpec());
			        while (qr.hasMoreElements()) {
			        	Object[] obj = (Object[]) qr.nextElement();
			            WTPartUsageLink link = (WTPartUsageLink) obj[0];
			            PersistenceServerHelper.manager.remove(link);
			        }
	        	} catch (WTException e) {
	        		logger.error(CLASSNAME+".removeAllUseLink:"+e);
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
     * get part's parents
     * 
     * @param child
     * @return 		if child is exist,return parts
     * 				else return []
     * @throws WTException
     */
    @SuppressWarnings("unchecked")
	public static List<WTPart> getParentsBychildPart(WTPart child) {
    	try {
    		if (!RemoteMethodServer.ServerFlag) {
    			return (List<WTPart>)RemoteMethodServer.getDefault().invoke("getParentsBychildPart", PartUtil.class.getName(), null, 
    					new Class[] { WTPart.class }, new Object[] { child });
	        } else {
	        	List<WTPart> parentList = new ArrayList<WTPart>();
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		if (child ==null) {
	        			return parentList;
	        		}
        			QuerySpec queryspec = new QuerySpec(WTPart.class, WTPartUsageLink.class);
					queryspec.appendWhere(VersionControlHelper.getSearchCondition(WTPart.class, true), new int[] { 0, 1 });
			        QueryResult qr = PersistenceHelper.manager.navigate(child.getMaster(), "usedBy", queryspec, true);
			        LatestConfigSpec lcs = new LatestConfigSpec();
			        qr = lcs.process(qr);
			        while (qr.hasMoreElements()) {
			        	WTPart parent = (WTPart) qr.nextElement();
			        	parentList.add(parent);
			        }
	        	} catch (WTException e) {
	        		logger.error(CLASSNAME+".getParentsBychildPart:"+e);
	        	} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
	        	return parentList;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }
    
    
    public static Boolean updateNumber(WTPart part,String newPartNumber) throws Exception
    {
    	Transaction transaction = null;
		boolean updateSuccess = false;
		String userName = "";
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (Boolean) RemoteMethodServer.getDefault().invoke(
						"updateNumber", PartUtil.class.getName(), null,
						new Class[] { WTPart.class, String.class },
						new Object[] { part, newPartNumber });
			} else {

				try {
					userName = SessionHelper.manager.getPrincipal().getName();
					SessionHelper.manager.setAuthenticatedPrincipal(userName);
					transaction = new Transaction();
					transaction.start();

					/* get Part Master and set new number */
					Identified identified = (Identified) part.getMaster();
					WTPartMasterIdentity masteridentity = (WTPartMasterIdentity) identified
							.getIdentificationObject();
					masteridentity.setNumber(newPartNumber);
					identified = IdentityHelper.service.changeIdentity(
							identified, masteridentity);
					SessionHelper.manager.setPrincipal(userName);
					PersistenceServerHelper.manager.update(part.getMaster());
					transaction.commit();
					transaction = null;
					updateSuccess = true; 
				} finally {
					if (transaction != null) {
						transaction.rollback();
					}
					try {
						SessionHelper.manager.setPrincipal(userName);
					} catch (WTException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return updateSuccess;
    }
	public static void test() throws Exception{
//		System.out.println("/*********************isPartExist********************/");
//		System.out.println("1-------------"+isPartExist("0000000041"));
//		System.out.println("2-------------"+isPartExist("asd"));
//		System.out.println("3-------------"+isPartExist(""));
//		System.out.println("4-------------"+isPartExist(null));
//		System.out.println("/*********************getPartMasterByNumber********************/");
//		System.out.println("1-------------"+getPartMasterByNumber("GC000032"));
//		System.out.println("2-------------"+getPartMasterByNumber("asd"));
//		System.out.println("3-------------"+getPartMasterByNumber(""));
//		System.out.println("4-------------"+getPartMasterByNumber(null));
//		System.out.println("/*********************getPartByOid********************/");
//		System.out.println("1-------------"+getPartByOid("VR:wt.part.WTPart:156934"));
//		System.out.println("2-------------"+getPartByOid("VR:wt.part.WTPart:asdf"));
//		System.out.println("3-------------"+getPartByOid("asdf"));
//		System.out.println("4-------------"+getPartByOid(""));
//		System.out.println("5-------------"+getPartByOid(null));
//		System.out.println("/*********************getPartByNumber********************/");
//		System.out.println(getPartByNumber("0000000022"));
//		System.out.println(getPartByNumber("asd"));
//		System.out.println(getPartByNumber(""));
//		System.out.println(getPartByNumber(null));
//		System.out.println("/*********************getPartByProperties********************/");
//		List<WTPart> list = getPartByProperties("0000000022", "", "", "");
//		for (int i = 0; i < list.size(); i++) {
//			System.out.println("1-------------"+list.get(i));
//		}
//		List<WTPart> list1 = getPartByProperties("", "002", "", "");
//		for (int i = 0; i < list1.size(); i++) {
//			System.out.println("2-------------"+list1.get(i));
//		}
//		List<WTPart> list2 = getPartByProperties("", "", "INWORK", "");
//		for (int i = 0; i < list2.size(); i++) {
//			System.out.println("3-------------"+list2.get(i));
//		}
//		List<WTPart> list3 = getPartByProperties("", "", "", "wt.part.WTPart|wt.part.SubPart");
//		for (int i = 0; i < list3.size(); i++) {
//			System.out.println("4-------------"+list3.get(i));
//		}
//		List<WTPart> list4 = getPartByProperties("", "", "", "");
//		for (int i = 0; i < list4.size(); i++) {
//			System.out.println("5-------------"+list4.get(i));
//		}
//		List<WTPart> list5 = getPartByProperties("0000000022", "002", "", "");
//		for (int i = 0; i < list5.size(); i++) {
//			System.out.println("6-------------"+list5.get(i));
//		}
//		List<WTPart> list6 = getPartByProperties("0000000022", "002", "INWORK", "");
//		for (int i = 0; i < list6.size(); i++) {
//			System.out.println("7-------------"+list6.get(i));
//		}
//		List<WTPart> list7 = getPartByProperties("0000000022", "002", "INWORK", "wt.part.WTPart|wt.part.SubPart");
//		for (int i = 0; i < list7.size(); i++) {
//			System.out.println("8-------------"+list7.get(i));
//		}
//		List<WTPart> list8 = getPartByProperties("asd", "asd", "asd", "asd");
//		for (int i = 0; i < list8.size(); i++) {
//			System.out.println("9-------------"+list8.get(i));
//		}
//		List<WTPart> list9 = getPartByProperties("", "", "", "");
//		for (int i = 0; i < list9.size(); i++) {
//			System.out.println("10-------------"+list9.get(i));
//		}
//		System.out.println("/*********************createPart********************/");
//		System.out.println("1-------------"+createPart("123", "123", "0000000022"));
//		System.out.println("2-------------"+createPart("", "1234", "0000000022"));
//		System.out.println("3-------------"+createPart("asd", "", "asd"));
//		System.out.println("4-------------"+createPart(null, null, null));
//		System.out.println("/*********************getTypeByPart********************/");
//		System.out.println("1-------------"+getTypeByPart(getPartByNumber("0000000022")));
//		System.out.println("2-------------"+getTypeByPart(getPartByNumber("asd")));
//		System.out.println("3-------------"+getTypeByPart(null));
//		System.out.println("/*********************getNumberByPart********************/");
//		System.out.println("1-------------"+getNumberByPart(getPartByNumber("0000000022")));
//		System.out.println("2-------------"+getNumberByPart(getPartByNumber("asd")));
//		System.out.println("3-------------"+getNumberByPart(null));
//		System.out.println("/*********************getNameByPart********************/");
//		System.out.println(getNameByPart(getPartByNumber("0000000022")));
//		System.out.println(getNameByPart(getPartByNumber("asd")));
//		System.out.println(getNameByPart(null));
//		System.out.println("/*********************getBigVersionPartByPart********************/");
//		System.out.println(getBigVersionByPart(getPartByNumber("0000000022")));
//		System.out.println(getBigVersionByPart(getPartByNumber("asd")));
//		System.out.println(getBigVersionByPart(null));
//		System.out.println("/*********************getSmallVersionByPart********************/");
//		System.out.println(getSmallVersionByPart(getPartByNumber("0000000022")));
//		System.out.println(getSmallVersionByPart(getPartByNumber("asd")));
//		System.out.println(getSmallVersionByPart(null));
//		System.out.println("/*********************getSubPartByPartNumber********************/");
//		List<WTPartMaster> list = getSubPartByPartNumber("GC000027");
//		for (int i = 0; i < list.size(); i++) {
//			System.out.println("1-------------"+list.get(i));
//		}
//		List<WTPartMaster> list1 = getSubPartByPartNumber("asd");
//		for (int i = 0; i < list1.size(); i++) {
//			System.out.println("2-------------"+list1.get(i));
//		}
//		List<WTPartMaster> list2 = getSubPartByPartNumber("");
//		for (int i = 0; i < list2.size(); i++) {
//			System.out.println("3-------------"+list2.get(i));
//		}
//		List<WTPartMaster> list3 = getSubPartByPartNumber(null);
//		for (int i = 0; i < list3.size(); i++) {
//			System.out.println("4-------------"+list3.get(i));
//		}
//		System.out.println("/*********************getDisDocByPart********************/");
//		List<WTDocument> list = getDisDocByPart(getPartByNumber("0000000022"));
//		for (int i = 0; i < list.size(); i++) {
//			System.out.println("1-------------"+list.get(i));
//		}
//		List<WTDocument> list1 = getDisDocByPart(getPartByNumber("asd"));
//		for (int i = 0; i < list1.size(); i++) {
//			System.out.println("2-------------"+list1.get(i));
//		}
//		List<WTDocument> list2 = getDisDocByPart(null);
//		for (int i = 0; i < list2.size(); i++) {
//			System.out.println("3-------------"+list2.get(i));
//		}
//		System.out.println("/*********************getRefDocByPart********************/");
//		List<WTDocumentMaster> list = getRefDocByPart(getPartByNumber("0000000022"));
//		for (int i = 0; i < list.size(); i++) {
//			System.out.println("1-------------"+list.get(i));
//		}
//		List<WTDocumentMaster> list1 = getRefDocByPart(getPartByNumber("asd"));
//		for (int i = 0; i < list1.size(); i++) {
//			System.out.println("2-------------"+list1.get(i));
//		}
//		List<WTDocumentMaster> list2 = getRefDocByPart(null);
//		for (int i = 0; i < list2.size(); i++) {
//			System.out.println("3-------------"+list2.get(i));
//		}
//		System.out.println("/*********************deletePart********************/");
//		deletePart(getPartByNumber("0000000022"));
//		deletePart(getPartByNumber("asd"));
//		deletePart(null);
//		System.out.println("/*********************updatePart********************/");
//		updatePart(getPartByNumber("0000000022"), "123456", "123456");
//		updatePart(getPartByNumber("0000000022"),"","123");
//		updatePart(getPartByNumber("asd"),"asdfg","");
//		updatePart(getPartByNumber("asd"),"0000000022","asdfg");
//		System.out.println("/*********************isCheckOut********************/");
//		System.out.println(isCheckOut(getPartByNumber("0000000022")));
//		System.out.println(isCheckOut(getPartByNumber("GC12345")));
//		System.out.println(isCheckOut(getPartByNumber("asd")));
//		System.out.println(isCheckOut(null));
//		System.out.println("/*********************isWorkingCopy********************/");
//		System.out.println(isWorkingCopy(getPartByNumber("0000000022")));
//		System.out.println(isWorkingCopy(getPartByNumber("GC12345")));
//		System.out.println(isWorkingCopy(getPartByNumber("asd")));
//		System.out.println(isWorkingCopy(null));
//		System.out.println("/*********************isModifiable********************/");
//		System.out.println(isModifiable(getPartByNumber("0000000022")));
//		System.out.println(isModifiable(getPartByNumber("GC12345")));
//		System.out.println(isModifiable(getPartByNumber("asd")));
//		System.out.println(isModifiable(null));
//		System.out.println("/*********************doCheckOut********************/");
//		System.out.println(doCheckOut(getPartByNumber("0000000022")));
//		System.out.println(doCheckOut(getPartByNumber("asd")));
//		System.out.println(doCheckOut(null));
//		System.out.println("/*********************doCheckOut********************/");
//		doCheckIn(getPartByNumber("0000000022"));
//		doCheckIn(getPartByNumber("asd"));
//		doCheckIn(null);
//		System.out.println("/*********************reviseWTPart********************/");
//		reviseWTPart(getPartByNumber("GC000032"), "asdfg");
//		reviseWTPart(getPartByNumber("asd"), "asdfg");
//		reviseWTPart(getPartByNumber(null), "asdfg");
//		System.out.println("/*********************removeUseLink********************/");
//		removeUseLink(getPartByNumber("GC000032"), getPartByNumber("1234"));
//		removeUseLink(getPartByNumber("asd"), getPartByNumber("1234"));
//		removeUseLink(null, null);
//		System.out.println("/*********************getPartUsageLink********************/");
//		System.out.println(getPartUsageLink(getPartByNumber("GC000032"), getPartMasterByNumber("GC000027")));
//		System.out.println(getPartUsageLink(getPartByNumber("asd"), getPartMasterByNumber("GC000027")));
//		System.out.println(getPartUsageLink(null, null));
//		System.out.println("/*********************getLatestPartByMaster********************/");
//		System.out.println(getLatestPartByMaster(getPartMasterByNumber("GC000032")));
//		System.out.println(getLatestPartByMaster(getPartMasterByNumber("asd")));
//		System.out.println(getLatestPartByMaster(null));
//		System.out.println("/*********************createUsageLink********************/");
//		System.out.println(createUsageLink(getPartByNumber("GC000032"), getPartMasterByNumber("GC000030")));
//		System.out.println(createUsageLink(getPartByNumber("asd"), getPartMasterByNumber("GC000030")));
//		System.out.println(createUsageLink(null, null));
//		System.out.println("/*********************removeAllUseLink********************/");
//		removeAllUseLink(getPartByNumber("GC000032"));
//		removeAllUseLink(getPartByNumber("asd"));
//		removeAllUseLink(null);
//		System.out.println("/*********************getParentsBychildPart********************/");
//		List<WTPart> list = getParentsBychildPart(getPartByNumber("GC000027"));
//		for (int i = 0; i < list.size(); i++) {
//			System.out.println("1-------------"+list.get(i));
//		}
//		List<WTPart> list1 = getParentsBychildPart(getPartByNumber("asd"));
//		for (int i = 0; i < list1.size(); i++) {
//			System.out.println("2-------------"+list1.get(i));
//		}
//		List<WTPart> list2 = getParentsBychildPart(null);
//		for (int i = 0; i < list2.size(); i++) {
//			System.out.println("3-------------"+list2.get(i));
//		}
//		System.out.println("/*********************getWorkingCopyOfPart********************/");
//		System.out.println(getWorkingCopyOfPart(getPartByNumber("GC000032")));
//		System.out.println(getWorkingCopyOfPart(getPartByNumber("asd")));
//		System.out.println(getWorkingCopyOfPart(null));
//		System.out.println("/*********************hasUsageLink********************/");
//		System.out.println(hasUsageLink(getPartByNumber("GC000032"), getPartMasterByNumber("GC000027")));
//		System.out.println(hasUsageLink(getPartByNumber("asd"), getPartMasterByNumber("asd")));
//		System.out.println(hasUsageLink(null, null));
		updateNumber(getPartByNumber("HQ1211Y001000"),"xiugaishodedongxi01");
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException {
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", PartUtil.class.getName(), null, new Class[] {}, new Object[] {});
		}
	}
}
