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
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.Foldered;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplate;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTOrganization;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
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
 * 
 * @author WangY
 *
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
    public static boolean isPartExist(String strNumber){
    	 boolean flag = false;
    	 boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
         try{
 	        if (!RemoteMethodServer.ServerFlag) {
 	                return (boolean) RemoteMethodServer.getDefault().invoke("isPartExist", 
 	                		PartUtil.class.getName(), null, new Class[] { String.class},
 	                		new Object[] { strNumber });
 	        } else {
		    	WTPartMaster wtpartmaster = null;
		        if(!StringUtils.isEmpty(strNumber)){
		        	try {
						wtpartmaster = getPartMasterByNumber(strNumber);
					} catch (WTException e) {
						logger.error(">>>>>"+e);
					}
		        }
		        if (wtpartmaster != null) {
		            flag = true;
		        } 
 	        }
         } catch (RemoteException e) {
             logger.error(e.getMessage(),e);
         } catch (InvocationTargetException e) {
         	logger.error(e.getMessage(),e);
         } finally {
             SessionServerHelper.manager.setAccessEnforced(enforce);
         }
         return flag;
    }
    
    
    /**
     * get part by oid
     * @param oid
     * @return	if oid exist in windChill, return WTPart;
	 * 				else if oid is not exist in windChill, throw exception and return null; such as oid = "VR:wt.part.WTPart:asdf"
	 * 							or oid = "asdf"; 
	 * 				else if oid is null or "",return null
     */
    public static WTPart getPartByOid(String oid){
		WTPart wtpart = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTPart) RemoteMethodServer.getDefault().invoke("getPartByOid", 
	                		PartUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] { oid });
	        } else {
				try {
					if(!StringUtils.isEmpty(oid)){
						ReferenceFactory rf = new ReferenceFactory();
						wtpart = (WTPart) rf.getReference(oid).getObject();
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return wtpart;
	}
	
	/**
	 * get part by number
	 * if partNumber exist in windChill,return WTPart;	such as:partNumber = "0000000041"
	 * 		else return null	such as 
	 */
	public static WTPart getPartByNumber(String partNumber) throws WTException{
		WTPart part = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTPart) RemoteMethodServer.getDefault().invoke("getPartByNumber", 
	                		PartUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] { partNumber });
	        } else {
				if (!StringUtils.isEmpty(partNumber) && isPartExist(partNumber)) {
					QuerySpec qs = new QuerySpec(WTPart.class);
					WhereExpression  we = new SearchCondition(WTPart.class,WTPart.NUMBER, SearchCondition.EQUAL, partNumber);
					qs.appendWhere(we,new int[] { 0 });
					QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
					LatestConfigSpec cfg = new LatestConfigSpec();  //构建过滤器
					QueryResult qr1 = cfg.process(qr); //按小版本排序
					if(qr1.hasMoreElements())
						part =(WTPart) qr1.nextElement(); //获取最新小版本的WTPart对象
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return part;
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
	public static List<WTPart> getPartByProperties(String number,String name, String state,     
            String type) throws WTException {
        List<WTPart> partList = new ArrayList<WTPart>();
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (List<WTPart>) RemoteMethodServer.getDefault().invoke("getPartByProperties", 
	                		PartUtil.class.getName(), null, new Class[] { String.class,String.class,String.class,String.class},
	                		new Object[] { number,name,state,type});
	        } else {
	        	int count = 0;
	        	WTPart part = null;
	        	QuerySpec qs = new QuerySpec(WTPart.class);
	        	if(!StringUtils.isEmpty(number)){
	        		SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER,
			                SearchCondition.EQUAL, number, false);
			        qs.appendWhere(sc, new int[] { 0 });
			        count++;
	        	}
	        	if(!StringUtils.isEmpty(name)){
	        		if(count != 0){
	        			qs.appendAnd();
	        		}
	        		SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NAME,
			                SearchCondition.EQUAL, name, false);
			        qs.appendWhere(sc, new int[] { 0 });
			        count++;
	        	}
	        	QueryResult qr = PersistenceServerHelper.manager
		                .query((StatementSpec) qs);
		        qr = new LatestConfigSpec().process(qr);
	        	while (qr.hasMoreElements()) {
		            part = (WTPart) qr.nextElement();
		            String ibaType = "";
		            String partState = "";
		            if(!StringUtils.isEmpty(type)){
		            	ibaType = TypedUtility.getTypeIdentifier(part).getTypename();
		            }
		            if(!StringUtils.isEmpty(state)){
		            	partState = part.getState().toString();
		            }
		            if (partState.indexOf(state) != -1 && ibaType.indexOf(type) != -1) {
		                partList.add(part);
		            }else if(partState.indexOf(state) != -1){
		            	partList.add(part);
		            }else if(ibaType.indexOf(type) != -1){
		            	partList.add(part);
		            }
		        }
        	}
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return partList;
    }

    /**
     * Get part master by part number
     * @param partNo
     * @return	if partNo exist in windChill, return WTPartMaster,	such as:partNo = "0000000022"
     * 				else return null	such as:partNo = "asd" or partNo = "" or partNo = null
     * @throws WTException
     */
    public static WTPartMaster getPartMasterByNumber(String partNo) throws WTException {
    	WTPartMaster partMaster = null;
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTPartMaster) RemoteMethodServer.getDefault().invoke("getPartMasterByNumber", 
	                		PartUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] { partNo });
	        } else {
		        QuerySpec querySpec = new QuerySpec(WTPartMaster.class);
		        WhereExpression searchCondition = new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, 
		        		SearchCondition.EQUAL, partNo, false);
		        querySpec.appendWhere(searchCondition,new int[] { 0 });
		        QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec)querySpec);
		        while (queryResult.hasMoreElements()) {
		            partMaster = (WTPartMaster) queryResult.nextElement();
		        }
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return partMaster;
    }
    
    /**
     * Get the Latest Part by PartMaster
     * 
     * @param partMaster
     * @return		if  partMaster is exist in windChill,return part
     * 				else if partMaster is not exist or partMaster is null,return null
     * @throws WTException
     */
    public static WTPart getLatestPartByMaster(WTPartMaster partMaster)
            throws WTException {
        WTPart latestPart = null;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	            return (WTPart)RemoteMethodServer.getDefault().invoke("getLatestPartByMaster", 
	                		PartUtil.class.getName(), null, new Class[] {WTPartMaster.class},
	                		new Object[] { partMaster});
	        } else {
		        if(partMaster!=null && isPartExist(partMaster.getNumber())){
			        QueryResult qr = VersionControlHelper.service
			                .allIterationsOf(partMaster);
			        if (qr.hasMoreElements()) {
			            latestPart = (WTPart) qr.nextElement();
			        }
		        }
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return latestPart;
    }
    
    /**
     * get type by part
     * @param part
     * @return	if part exist in windChill,return part type
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
     */
	public static String getTypeByPart(WTPart part){
		String partType = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (String) RemoteMethodServer.getDefault().invoke("getTypeByPart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber())){
						partType = TypedUtilityServiceHelper.service.getExternalTypeIdentifier(part).toString();
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return partType;
	}
	
	/**
	 * get number by part
	 * @param part
	 * @return	if part exist in windChill,return part number
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
	 */
	public static String getNumberByPart(WTPart part){
		String partNumber = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (String) RemoteMethodServer.getDefault().invoke("getNumberByPart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try{
					if(part!=null && isPartExist(part.getNumber())){
						partNumber = part.getNumber();
					}
				}catch(Exception e){
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return partNumber;
	}
	
	/**
	 * get name by part
	 * @param part
	 * @return	if part exist in windChill,return part name
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
	 */
	public static String getNameByPart(WTPart part){
		String partName = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (String) RemoteMethodServer.getDefault().invoke("getNameByPart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try{
					if(part!=null && isPartExist(part.getNumber())){
						partName = part.getName();
					}
				}catch(Exception e){
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return partName;
	}
	
	/**
	 * get part lifeCycle by part
	 * @param part
	 * @return		if part is exist in windChill,return state
	 * 				else if part is not exist in windChill or part is null,return null
	 */
	public static State getLifeCycleState(WTPart part){
		State state = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	               return (State)RemoteMethodServer.getDefault().invoke("getLifeCycleState", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber())){
						state = part.getLifeCycleState();
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return state;
	}
	
	/**
	 * get part lifeCycleTemplate by part
	 * @param part
	 * @return		if part is exist in windChill,return lifeCycleTemplate
	 * 				else if part is not exist in windChill or part is null,return null
	 */
	@SuppressWarnings("deprecation")
	public static LifeCycleTemplate getLifeCycleTemplate(WTPart part){
		LifeCycleTemplate lt = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	               return (LifeCycleTemplate)RemoteMethodServer.getDefault().invoke("getLifeCycleTemplate", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber())){
						 lt = LifeCycleHelper.service.getLifeCycleTemplate((LifeCycleManaged) part);
					}
				} catch (WTException e) {
					e.printStackTrace();
				}
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return lt;
	}
	
	/**
	 * get bigVersion part by part
	 * @param part
	 * @return	if part exist in windChill,return part bigVersion
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
	 */
	public static String getBigVersionByPart(WTPart part){
		String wt = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (String) RemoteMethodServer.getDefault().invoke("getBigVersionByPart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try{
					if(part!=null && isPartExist(part.getNumber())){
						wt = part.getVersionIdentifier().getValue();
					}
				}catch(Exception e){
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return wt;
	}
	
	/**
	 * get smallVersion part by part
	 * @param part
	 * @return	if part exist in windChill,return part SmallVersion
     * 			else if part is not exist in WinChill ,return null
     * 			else if part is null,return null
	 */
	public static String getSmallVersionByPart(WTPart part){
		String wt = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (String) RemoteMethodServer.getDefault().invoke("getSmallVersionByPart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try{
					if(part!=null && isPartExist(part.getNumber())){
						wt = part.getIterationIdentifier().getValue();
					}
				}catch(Exception e){
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return wt;
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
	public static List<WTPartMaster> getSubPartByPartNumber(String number) throws WTException{
		List<WTPartMaster> wtpmList = new ArrayList<WTPartMaster>(); 
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (List<WTPartMaster>) RemoteMethodServer.getDefault().invoke("getSubPartByPartNumber", 
	                		PartUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] { number});
	        } else {
				if(!StringUtils.isEmpty(number) && isPartExist(number)){
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
					while(qr.hasMoreElements()){
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
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return wtpmList;
	}
	
	/**
	 * get part discriptionDoc by part
	 * @param part
	 * @return	if part is exist in windChill,return documents
	 * 			else if part is null or part is not exist in windChill,return []
	 */
	@SuppressWarnings("unchecked")
	public static List<WTDocument> getDisDocByPart(WTPart part){
		List<WTDocument> listDoc = new ArrayList<WTDocument>();
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (List<WTDocument>) RemoteMethodServer.getDefault().invoke("getDisDocByPart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber())){
						QueryResult qr = WTPartHelper.service.getDescribedByDocuments(part);
						while(qr.hasMoreElements()){
							Object object = qr.nextElement();
							if(object instanceof WTDocument){
							     WTDocument doc = (WTDocument)object;
							     listDoc.add(doc);
							}
						}
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return listDoc;
	}
	
	/**
	 * get referenceDoc by part
	 * @author WangY
	 */
	@SuppressWarnings("unchecked")
	public static List<WTDocumentMaster> getRefDocByPart(WTPart part){
		List<WTDocumentMaster> listDocMaster = new ArrayList<WTDocumentMaster>();
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (List<WTDocumentMaster>) RemoteMethodServer.getDefault().invoke("getRefDocByPart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber())){
						@SuppressWarnings("deprecation")
						QueryResult qr = WTPartHelper.service.getReferencesWTDocumentMasters(part);
						while(qr.hasMoreElements()){
							Object object = qr.nextElement();
						if(object instanceof WTDocumentMaster){
							    WTDocumentMaster doct = (WTDocumentMaster)object;
							    listDocMaster.add(doct);
							}
						}
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return listDocMaster;
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
	public static WTPart createPart(String newPartNumber,String newPartName,String partNumber){
		WTPart part = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTPart) RemoteMethodServer.getDefault().invoke("newPart", 
	                		PartUtil.class.getName(), null, new Class[] { String.class,String.class,String.class},
	                		new Object[] { newPartName,newPartNumber,partNumber});
	        } else {
				try{
					if(!StringUtils.isEmpty(newPartName)&& !StringUtils.isEmpty(partNumber)
							&& isPartExist(partNumber)){
						part = WTPart.newWTPart();
						part.setName(newPartName);//设置名字
						if(!StringUtils.isEmpty(partNumber) && !isPartExist(newPartName)){
							part.setNumber(newPartNumber);//设置编号
						}
						part.setContainer(getPartByNumber(partNumber).getContainer());//设置容器
						PersistenceHelper.manager.save(part);
					}
				}catch(Exception e){
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return part;
	}
	
	/**
	 * delete part by part
	 * @param part
	 * 		if part is exist in windChill,delete it
	 * 		else if part is not exist in windChill or part is null,there is nothing to do
	 */
	public static void deletePart(WTPart part){
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                RemoteMethodServer.getDefault().invoke("deletePart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber())){
						PersistenceHelper.manager.delete(part);
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
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
	public static void updatePart(WTPart part,String newNumber,String newName){
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	                RemoteMethodServer.getDefault().invoke("updatePartNumber", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class,String.class,String.class},
	                		new Object[] { part,newNumber,newName});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber()) && !StringUtils.isEmpty(newNumber) && !isPartExist(newNumber)){
						WTPartMaster partMaster = (WTPartMaster) part.getMaster();
						WTOrganization wtorganization = part.getOrganization();
						if(!StringUtils.isEmpty("newNumber") && !isPartExist(newNumber)){
							partMaster = WTPartHelper.service.changeWTPartMasterIdentity((WTPartMaster) partMaster,	
									part.getName(), newNumber, wtorganization);
						}
						if(!StringUtils.isEmpty(newName)){
							partMaster = WTPartHelper.service.changeWTPartMasterIdentity((WTPartMaster) partMaster,	
									newName, part.getNumber(), wtorganization);
						}
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
	}
	
	/**
	 * To determine whether part is checkout 
	 * @param part
	 * @return	if part is exist in windChill and is checkOut,return true
	 * 				else return false
	 */
	public static boolean isCheckOut(WTPart part){
		boolean bo = false;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	                 return (boolean)RemoteMethodServer.getDefault().invoke("isCheckOutState", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber())){
						bo = WorkInProgressHelper.isCheckedOut(part);
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return bo;
	}
	
	/**
	 * To determine whether part is workingCopy
	 * @param part
	 * @return		if part is exist in windChill and is workingCopy,return true
	 * 				else return false
	 */
	public static boolean isWorkingCopy(WTPart part){
		boolean bo = false;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	               return (boolean)RemoteMethodServer.getDefault().invoke("isWorkingCopy", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber())){
						bo = WorkInProgressHelper.isWorkingCopy(part);
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return bo;
	}
	
	/**
	 * To determine whether part can modify
	 * @param part
	 * @return		if part is exist in windChill and is workingCopy,return true
	 * 				else return false
	 */
	public static boolean isModifiable(WTPart part){
		boolean bo = false;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	               return (boolean)RemoteMethodServer.getDefault().invoke("isModifiable", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try {
					if(part!=null && isPartExist(part.getNumber())){
						bo = WorkInProgressHelper.isModifiable(part);
					}
				} catch (Exception e) {
					logger.error(">>>>>"+e);
				}
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return bo;
	}
	
	/**
	 * checkout part and return wokingCopy
	 * @param part
	 * @return		if part is exist in windChill,return workable
	 * 				else if part is not exist in windChill or part is null,return null
	 */
	public static Workable doCheckOut(Workable part){
		Workable workable = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	               return (Workable)RemoteMethodServer.getDefault().invoke("doCheckOut", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				try{
					if(part!=null){
						if(part instanceof Iterated){
							Iterated it = (Iterated) part;
							part=(Workable) VersionControlHelper.service.getLatestIteration(it, false);
							Boolean checkOutFlag=WorkInProgressHelper.isCheckedOut(part);
							if(checkOutFlag){ 
								if(!WorkInProgressHelper.isWorkingCopy(part)){
							         workable=WorkInProgressHelper.service.workingCopyOf(part);
								}else{
									workable = part;
								}
							}else{
								Folder myFolder= WorkInProgressHelper.service.getCheckoutFolder();
								CheckoutLink link = WorkInProgressHelper.service.checkout(part, myFolder, "AutoCheckOut");
								workable = link.getWorkingCopy();
							}
						}	
					}
				}catch(Exception e){
					e.printStackTrace();
				}
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return workable;
	}
	
	/**
	 * checkIn part by part
	 * @param part
	 * 		if part is exist in windChill,checkIn part
	 * 		else if part is not exist in windChill or part is null,there is nothing to do
	 */
	public static void doCheckIn(Workable part){
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	               RemoteMethodServer.getDefault().invoke("doCheckIn", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { part});
	        } else {
				if(part!=null){
					Workable workable = null;
					if(!WorkInProgressHelper.isWorkingCopy(part)){
						workable = doCheckOut(part);//从检出方法中得到已检出工作副本
					}else{
						workable = part;
					}
					try{
						workable = WorkInProgressHelper.service.checkin(workable, "AutoCheckIn");
					}catch(Exception e){
						logger.error(">>>>>"+e);
					}
				}
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
	}
	
	/**
	 * revise WTPart by part
	 * @param part
	 * @param comment
	 * @return	if part is exist in windChill,checkIn part
	 * 			else if part is not exist in windChill or part is null,return null
	 * @throws WTException
	 */
    @SuppressWarnings("deprecation")
	public static WTPart reviseWTPart(WTPart part, String comment)
            throws WTException {
        WTPart wtpart = part;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	              return (WTPart)RemoteMethodServer.getDefault().invoke("reviseWTPart", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class,String.class},
	                		new Object[] { part,comment});
	        } else {
		        try {
		            if (wtpart == null && !isPartExist(part.getNumber())) {
		                return null;
		            }
		            WTContainer container = wtpart.getContainer();
		            WTContainerRef containerRef = WTContainerRef
		                    .newWTContainerRef(container);
		            TeamReference teamReference = wtpart.getTeamId();
		            Folder oldFoler = FolderHelper.getFolder(wtpart);// wtpart.getFolderingInfo().getFolder();
		            if (oldFoler == null) {
		                String strLocation = wtpart.getLocation();
		                oldFoler = FolderHelper.service.getFolder(strLocation,
		                        containerRef);
		            }
		            wtpart = (WTPart) VersionControlHelper.service
		                    .newVersion((Versioned) wtpart);
		            if (wtpart != null) {
		                wtpart.setTeamId(teamReference);
		            }
		            VersionControlHelper.setNote(wtpart, comment);
		            wtpart.setContainer(container);
		            FolderHelper.assignFolder((Foldered) wtpart, oldFoler);
		            wtpart = (WTPart) PersistenceHelper.manager.save(wtpart);
		            wtpart = (WTPart) PersistenceHelper.manager.refresh(wtpart);
		        } catch (WTPropertyVetoException e) {
		            logger.error(e.getLocalizedMessage(), e);
		            throw new WTException(e, e.getLocalizedMessage());
		        }
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return wtpart;
    }
	

	/**
     * this method is used to get working version of part.(check out part).
     * @param part
     * @return	if part is exist,return wokingCopy part
     * 				else return null
     */
    public static WTPart getWorkingCopyOfPart(WTPart part){
        WTPart workingPart = null;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	          return (WTPart)RemoteMethodServer.getDefault().invoke("getWorkingCopyOfPart", 
	                		PartUtil.class.getName(), null, new Class[] {WTPart.class},
	                		new Object[] { part});
	        } else {
		        try{
			        if(part!=null && isPartExist(part.getNumber())){
				        if (!WorkInProgressHelper.isCheckedOut(part)) {
				            Folder folder = WorkInProgressHelper.service.getCheckoutFolder();
				            CheckoutLink checkoutlink = WorkInProgressHelper.service.checkout(
				                    part, folder, SessionHelper.manager.getPrincipal()
				                            .getName());
				            workingPart = (WTPart) checkoutlink.getWorkingCopy();
				        } else {
				            if (!WorkInProgressHelper.isWorkingCopy(part)) {
				                workingPart = (WTPart) WorkInProgressHelper.service
				                        .workingCopyOf(part);
				            } else {
				                workingPart = part;
				            }
				        }
			        }
		        }catch(WTException e){
		        	logger.error(">>>>>"+e);
		        }catch(WTPropertyVetoException e){
		        	logger.error(">>>>>"+e);
		        }
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return workingPart;
    }
    
    /**
     * Judge if the two given parts has usage link between them
     * @param parentPart
     * @param childMaster
     * @return		if parentPart and childMaster is exist in windChill and has link ,return true
     * 				else return false
     * @throws WTException
     */
    public static boolean hasUsageLink(WTPart parentPart,
            WTPartMaster childMaster) throws WTException {
        boolean hasUsageLink = false;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	          return (boolean)RemoteMethodServer.getDefault().invoke("hasUsageLink", 
	                		PartUtil.class.getName(), null, new Class[] {WTPart.class,WTPartMaster.class},
	                		new Object[] { parentPart,childMaster});
	        } else {
		        WTPartUsageLink usageLink = getPartUsageLink(parentPart, childMaster);
		        if (usageLink != null) {
		            hasUsageLink = true;
		        }
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return hasUsageLink;
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
    public static WTPartUsageLink getPartUsageLink(WTPart parentPart,
            WTPartMaster childMaster) throws WTException {
        WTPartUsageLink usageLink = null;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	             return (WTPartUsageLink)RemoteMethodServer.getDefault().invoke("getPartUsageLink", 
	                		PartUtil.class.getName(), null, new Class[] {WTPart.class,WTPart.class},
	                		new Object[] { parentPart,childMaster});
	        } else {
		        if (parentPart != null && childMaster != null && isPartExist(parentPart.getNumber())
		        		&& isPartExist(childMaster.getNumber())) {
		            QueryResult queryresult = PersistenceHelper.manager.find(
		                    WTPartUsageLink.class, parentPart,
		                    WTPartUsageLink.USED_BY_ROLE, childMaster);
		
		            if (queryresult == null || queryresult.size() == 0) {
		                usageLink = null;
		            } else {
		                usageLink = (WTPartUsageLink) queryresult.nextElement();
		            }
		        }
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return usageLink;
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
     *             Windchill exception
     */
    public static WTPartUsageLink createUsageLink(WTPart parentPart,WTPartMaster childMaster)
            throws WTException {
    	WTPartUsageLink newLink = null;
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	            return (WTPartUsageLink)RemoteMethodServer.getDefault().invoke("createUsageLink", 
	                		PartUtil.class.getName(), null, new Class[] {WTPart.class,WTPartMaster.class},
	                		new Object[] { parentPart,childMaster});
	        } else {
		    	if (parentPart != null && isPartExist(parentPart.getNumber()) && 
		        		childMaster != null && isPartExist(childMaster.getNumber())) {
			    	if(getSubPartByPartNumber(parentPart.getNumber()).contains(childMaster)){
			    		newLink = getPartUsageLink(parentPart, childMaster);
			    	}else{
			            newLink = WTPartUsageLink.newWTPartUsageLink(parentPart,
			                    childMaster);
			            PersistenceServerHelper.manager.insert(newLink);
			    	}
		        }
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return newLink;
    }
    
    /**
     * Remove WTPartUsageLink between childPart and root
     * @param childPart
     * @param root
     * @throws WTException
     * 		if root and childPart is exist in windChill,childPart is the subPart,remove the childPart
     * 		else there is nothing to do
     */
    public static void removeUseLink(WTPart childPart, WTPart root)
            throws WTException {
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	              RemoteMethodServer.getDefault().invoke("removeUseLink", 
	                		PartUtil.class.getName(), null, new Class[] { WTPart.class,WTPart.class},
	                		new Object[] { childPart,root});
	        } else {
		        if (childPart == null || root == null) {
		            return;
		        }
		        WTPartMaster partMaster = (WTPartMaster) childPart.getMaster();
		        QuerySpec queryspec = new QuerySpec(WTPartUsageLink.class);
		        queryspec.appendWhere(
		                new SearchCondition(WTPartUsageLink.class,
		                        "roleAObjectRef.key", "=", PersistenceHelper
		                                .getObjectIdentifier(root)), new int[] { 0 });
		        queryspec.appendAnd();
		        queryspec.appendWhere(
		                new SearchCondition(WTPartUsageLink.class,
		                        "roleBObjectRef.key", "=", PersistenceHelper
		                                .getObjectIdentifier(partMaster)),
		                                new int[] { 0 });
		        QueryResult qr = PersistenceServerHelper.manager.query(queryspec);
		        while (qr.hasMoreElements()) {
		            WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
		            PersistenceServerHelper.manager.remove(link);
		        }
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
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
    public static void removeAllUseLink(WTPart root) throws WTException {
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	          RemoteMethodServer.getDefault().invoke("removeAllUseLink", 
	                		PartUtil.class.getName(), null, new Class[] {WTPart.class},
	                		new Object[] { root});
	        } else {
		    	if(root!=null && isPartExist(root.getNumber())){
			        QueryResult qr = WTPartHelper.service.getUsesWTParts(root,
			                WTPartHelper.service.findWTPartConfigSpec());
			        while (qr.hasMoreElements()) {
			            Object[] obj = (Object[]) qr.nextElement();
			            WTPartUsageLink link = (WTPartUsageLink) obj[0];
			            PersistenceServerHelper.manager.remove(link);
			        }
		    	}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
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
	public static List<WTPart> getParentsBychildPart(WTPart child) throws WTException {
        List<WTPart> result = new ArrayList<WTPart>();
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	          return (List<WTPart>)RemoteMethodServer.getDefault().invoke("getParents", 
	                		PartUtil.class.getName(), null, new Class[] {WTPart.class},
	                		new Object[] { child});
	        } else {
			    if(child!=null && isPartExist(child.getNumber())){
			        QuerySpec queryspec = new QuerySpec(WTPart.class, WTPartUsageLink.class);
			        queryspec.appendWhere(
			                VersionControlHelper.getSearchCondition(WTPart.class, true),
			                new int[] { 0, 1 });
			        QueryResult qr = PersistenceHelper.manager.navigate(child.getMaster(),
			                "usedBy", queryspec, true);
			        LatestConfigSpec lcs = new LatestConfigSpec();
			        qr = lcs.process(qr);
			        while (qr.hasMoreElements()) {
			            WTPart parent = (WTPart) qr.nextElement();
			            result.add(parent);
			        }
		    	}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return result;
    }
    
	public static void test() throws RemoteException, InvocationTargetException, WTException{
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
//		System.out.println("/*********************getLifeCycleState********************/");
//		System.out.println(getLifeCycleState(getPartByNumber("0000000022")));
//		System.out.println(getLifeCycleState(getPartByNumber("asd")));
//		System.out.println(getLifeCycleState(null));
//		System.out.println("/*********************getLifeCycleTemplate********************/");
//		System.out.println(getLifeCycleTemplate(getPartByNumber("0000000022")));
//		System.out.println(getLifeCycleTemplate(getPartByNumber("asd")));
//		System.out.println(getLifeCycleTemplate(null));
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
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", PartUtil.class.getName(), null,
					new Class[] {},
					new Object[] {});
		}
	}
}
