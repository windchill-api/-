package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeNoticeComplexity;
import wt.change2.ChangeOrder2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeIssue;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTList;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleServerHelper;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class ChangeUtil implements RemoteAccess,Serializable {
	
	/**
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = ChangeUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
     * create changeOrder
     * 
     * @param ecnType
     * @param name
     * @param folder
     * @param changeNoticeComplexity
     * @param description
     * @param timestamp
     * @param ibaMap
     * @return
     * @throws WTException
     * @throws WTPropertyVetoException
     */
    public static WTChangeOrder2 createWTChangeOrder2(String ecnType, String name, Folder folder,
            ChangeNoticeComplexity changeNoticeComplexity, String description, Timestamp timestamp){
    	WTChangeOrder2 changeOrder = null;
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTChangeOrder2) RemoteMethodServer.getDefault().invoke("createWTChangeOrder2", 
	                		ChangeUtil.class.getName(), null, new Class[] { String.class,String.class,Folder.class,
	                	ChangeNoticeComplexity.class,String.class,Timestamp.class},
	                		new Object[] { ecnType,name,folder,changeNoticeComplexity,description,timestamp });
	        } else {
		    	try{
		    		if(!StringUtils.isEmpty(ecnType) && ecnType.indexOf("wt.change2.WTChangeOrder2")!=-1 && 
		    				changeNoticeComplexity!=null && (changeNoticeComplexity.toString().indexOf("BASIC")!=-1 ||
		    						changeNoticeComplexity.toString().indexOf("SIMPLE")!=-1 || 
		    						changeNoticeComplexity.toString().indexOf("COMPLEX")!=-1)){
			    		changeOrder = WTChangeOrder2.newWTChangeOrder2();
			    		if(!StringUtils.isEmpty(name)){
			    			changeOrder.setName(name);
			    		}
				        TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(ecnType);
				        changeOrder.setTypeDefinitionReference(typeDefinitionRef);
				        if (!StringUtils.isEmpty(description)) {
				            changeOrder.setDescription(description);
				        }
				        if (timestamp != null) {
				            changeOrder.setNeedDate(timestamp);
				        }
				        changeOrder.setChangeNoticeComplexity(changeNoticeComplexity);
				        if(folder!=null){
				        	FolderHelper.assignLocation((FolderEntry) changeOrder, folder);
				        }
				        changeOrder = (WTChangeOrder2) ChangeHelper2.service.saveChangeOrder(changeOrder);
		    		}
		        } catch(Exception e){
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
        return changeOrder;
    }
	
	/**
	 * @param number
     * @param name
     * @param ecnType
     * @param folder
     * @param description
     * @return
     * @throws WTException
     * @throws WTPropertyVetoException
     */
    public static WTChangeOrder2 createWTChangeOrder2(String number,String name, String ecnType, Folder folder, String description)
    {
    	WTChangeOrder2 changeOrder = null;
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTChangeOrder2) RemoteMethodServer.getDefault().invoke("createWTChangeOrder2", 
	                		ChangeUtil.class.getName(), null, new Class[] { String.class,String.class,String.class,
	                	Folder.class,String.class},
	                		new Object[] { number,name,ecnType,folder,description});
	        } else {
		    	try{
		    		if(!StringUtils.isEmpty(ecnType) && ecnType.indexOf("wt.change2.WTChangeOrder2")!=-1){
			    		changeOrder = WTChangeOrder2.newWTChangeOrder2();
			    		if(!StringUtils.isEmpty(number)){
			    			changeOrder.setNumber(number);
			    		}
			    		if(!StringUtils.isEmpty(name)){
			    			changeOrder.setName(name);
			    		}
				        TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(ecnType);
				        changeOrder.setTypeDefinitionReference(typeDefinitionRef);
				        if (!StringUtils.isEmpty(description)) {
				            changeOrder.setDescription(description);
				        }
				        changeOrder.setChangeNoticeComplexity(ChangeNoticeComplexity.BASIC);
				        if(folder!=null){
				        	FolderHelper.assignLocation((FolderEntry) changeOrder, folder);
				        }
				        changeOrder = (WTChangeOrder2) ChangeHelper2.service.saveChangeOrder(changeOrder);
		    		}
		        } catch(Exception e){
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
        return changeOrder;
    }
	
    public static WTChangeOrder2 createWTChangeOrder2ByNumber(String number,String name, String ecnType, Folder folder){
    	return createWTChangeOrder2("", name, ecnType, folder, "");
    }
    
    public static WTChangeOrder2 createWTChangeOrder2ByNumber(String number,String name, String ecnType,String description){
    	return createWTChangeOrder2("", name, ecnType, null, "");
    }
    
    public static WTChangeOrder2 createWTChangeOrder2ByNumber(String number,String name, String ecnType){
    	return createWTChangeOrder2("", name, ecnType, null, "");
    }
    
    public static WTChangeOrder2 createWTChangeOrder2ByName(String name, String ecnType, Folder folder, String description){
    	return createWTChangeOrder2("", name, ecnType, folder, description);
    }
    
    public static WTChangeOrder2 createWTChangeOrder2ByName(String name, String ecnType, String description){
    	return createWTChangeOrder2("", name, ecnType, null, description);
    }
    
    public static WTChangeOrder2 createWTChangeOrder2ByName(String name, String ecnType, Folder folder){
    	return createWTChangeOrder2("", name, ecnType, folder,"");
    }
    
    public static WTChangeOrder2 createWTChangeOrder2ByName(String name, String ecnType){
    	return createWTChangeOrder2("", name, ecnType, null,"");
    }
    
    /**
     * create the ECA and set the attribute
     * 
     * @param changeOrder
     * @param econame
     * @return
     * @throws WTException
     * @throws WTPropertyVetoException
     */
    /*public static WTChangeActivity2 createECA(WTChangeOrder2 changeOrder, String econame){
        WTChangeActivity2 changeActivity = null;
        try {
            ChangeOrderIfc co = (ChangeOrderIfc) changeOrder;

            changeActivity = WTChangeActivity2.newWTChangeActivity2(econame);
            changeActivity.setDescription(econame);
            changeActivity.setContainer(changeOrder.getContainer());
            WTContainer container = changeOrder.getContainer();
            WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container);
            Folder folder = FolderUtil.getFolder(containerRef, "/Default");
            ChangeActivityIfc ca = (ChangeActivityIfc) changeActivity;
            FolderHelper.assignLocation((FolderEntry) ca, folder);
            //ca = (ChangeActivityIfc) PersistenceHelper.manager.save(ca);

            IncludedIn2Delegate del = new IncludedIn2Delegate();
            IncludedInIfc inc = (IncludedInIfc) del.getBinaryLink((ChangeItemIfc) co, ca);
            inc = (IncludedInIfc) PersistenceHelper.manager.save(inc);
            changeActivity = (WTChangeActivity2) ca;

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return changeActivity;
    }*/
    
    /**
     * get all affected object from ECN
     * 
     * @param ecn
     * @return
     * @throws ChangeException2
     * @throws WTException
     */
    public static List<WTObject> getChangeBefore1(WTChangeOrder2 ecn) throws ChangeException2, WTException {
        List<WTObject> list = new ArrayList<WTObject>();
        if(ecn!=null){
	        QueryResult result = ChangeHelper2.service.getChangeActivities(ecn);
	        while (result.hasMoreElements()) {
	            WTChangeActivity2 activity = (WTChangeActivity2) result.nextElement();
	            QueryResult res = ChangeHelper2.service.getChangeablesBefore(activity);
	            while (res.hasMoreElements()) {
	                WTObject object = (WTObject) res.nextElement();
	                list.add(object);
	            }
	        }
        }
        return list;
    }
    
    public static List<LifeCycleManaged> getChangeBefore2(WTChangeOrder2 ecn){
        List<LifeCycleManaged> result = new ArrayList<LifeCycleManaged>();
        try{
        	if(ecn!=null){
		        QueryResult qr = ChangeHelper2.service.getLatestChangeActivity(ecn);
		        logger.debug("eca size:" + qr.size());
		        while (qr.hasMoreElements()) {
		           WTChangeActivity2 eca = (WTChangeActivity2) qr.nextElement();
		            QueryResult before = ChangeHelper2.service.getChangeablesBefore(eca);
		            while (before.hasMoreElements()) {
		                LifeCycleManaged lcm = (LifeCycleManaged) before.nextElement();
		                result.add(lcm);
		            }
		        }
        	}
        }catch(Exception e){
        	logger.error(">>>>>"+e);
        }
        return result;
     }
    /**
     * get all resulting object from ECN
     * 
     * @param ecn
     * @return
     * @throws WTException
     */
    public static List<WTObject> getChangeAfter1(WTChangeOrder2 ecn){
        List<WTObject> list = new ArrayList<WTObject>();
        if(ecn!=null){
	        try{
	        	QueryResult result = ChangeHelper2.service.getChangeActivities(ecn);
		        while (result.hasMoreElements()) {
		            WTChangeActivity2 activity = (WTChangeActivity2) result.nextElement();
		            QueryResult res = ChangeHelper2.service.getChangeablesAfter(activity);
		            while (res.hasMoreElements()) {
		                WTObject object = (WTObject) res.nextElement();
		                list.add(object);
		            }
		        }
	        }catch(WTException e){
	        	logger.error(">>>>>"+e);
	        }
        }
        
        return list;
    }
    
    
    public static List<LifeCycleManaged> getChangeAfter2(WTChangeOrder2 ecn){
        List<LifeCycleManaged> result = new ArrayList<LifeCycleManaged>();
        try{
        	if(ecn!=null){
		        QueryResult qr = ChangeHelper2.service.getLatestChangeActivity(ecn);
		        logger.debug("eca size:" + qr.size());
		        while (qr.hasMoreElements()) {
		            WTChangeActivity2 eca = (WTChangeActivity2) qr.nextElement();
		            QueryResult afters = ChangeHelper2.service.getChangeablesAfter(eca);
		            while (afters.hasMoreElements()) {
		              LifeCycleManaged lcm = (LifeCycleManaged) afters.nextElement();
		              result.add(lcm);
		            }
		        }
        	}
        }catch(Exception e){
        	logger.error(">>>>>"+e);
        }
        return result;
     }
    
    /**
     * get ECA By ECN
     * @param ecn
     * @return
     */
    public static List<WTChangeActivity2> getECAByECN(WTChangeOrder2 ecn){
    	List<WTChangeActivity2> list = new ArrayList<WTChangeActivity2>();
    	if(ecn!=null){
    		try{
    			QueryResult result = ChangeHelper2.service.getChangeActivities(ecn);
    			while (result.hasMoreElements()) {
		            WTChangeActivity2 activity = (WTChangeActivity2) result.nextElement();
		            list.add(activity);
		        }
    		}catch(WTException e){
    			logger.error(">>>>>"+e);
    		}
    	}
    	return list;
    }
    
    /**
     * get ECN by ECR
     * @param ecr
     * @return
     */
    public static List<WTChangeOrder2> getECNByECR(WTChangeRequest2 ecr){
    	List<WTChangeOrder2> list = new ArrayList<WTChangeOrder2>();
    	if(ecr!=null){
    		try{
    			QueryResult result = ChangeHelper2.service.getChangeOrders(ecr);
    			while (result.hasMoreElements()) {
    				WTChangeOrder2 order = (WTChangeOrder2) result.nextElement();
		            list.add(order);
		        }
    		}catch(WTException e){
    			logger.error(">>>>>"+e);
    		}
    	}
    	return list;
    }
    
    /**
     * get ECN by ECA
     * @param eci
     * @return
     */
    public static WTChangeOrder2 getECNByECA(WTChangeActivity2 eca){
    	WTChangeOrder2 order = null;
    	if(eca!=null){
    		try{
    			QueryResult result = ChangeHelper2.service.getChangeOrder(eca);
    			while (result.hasMoreElements()) {
    				order = (WTChangeOrder2) result.nextElement();
		        }
    		}catch(WTException e){
    			logger.error(">>>>>"+e);
    		}
    	}
    	return order;
    }
    
    /**
     * get ECR by PR
     * @param issue
     * @return
     */
    public static WTChangeRequest2 getECRByPR(WTChangeIssue issue){
    	WTChangeRequest2 order = null;
    	if(issue!=null){
    		try{
    			QueryResult result = ChangeHelper2.service.getChangeRequest(issue);
    			while (result.hasMoreElements()) {
    				order = (WTChangeRequest2) result.nextElement();
		        }
    		}catch(WTException e){
    			logger.error(">>>>>"+e);
    		}
    	}
    	return order;
    }
    
    /**
     * get PR by ECR
     * @param request
     * @return
     */
    public static List<WTChangeIssue> getPRByECR(WTChangeRequest2 ecr){
    	List<WTChangeIssue> list = new ArrayList<WTChangeIssue>();
    	if(ecr!=null){
    		try{
    			QueryResult result = ChangeHelper2.service.getChangeIssues(ecr);
    			while (result.hasMoreElements()) {
    				WTChangeIssue order = (WTChangeIssue) result.nextElement();
		            list.add(order);
		        }
    		}catch(WTException e){
    			logger.error(">>>>>"+e);
    		}
    	}
    	return list;
    }
    
    public static WTChangeRequest2 getECRByECN(WTChangeOrder2 ecn){
    	WTChangeRequest2 order = null;
    	if(ecn!=null){
    		try{
    			QueryResult result = ChangeHelper2.service.getChangeRequest(ecn);
    			while (result.hasMoreElements()) {
    				order = (WTChangeRequest2) result.nextElement();
		        }
    		}catch(WTException e){
    			logger.error(">>>>>"+e);
    		}
    	}
    	return order;
    }
    
    /**
     * This method is used to get ECN by it's number.
     * 
     * @param nameORnumber
     *            ECN name or number
     * @return Engineering Change Notice
     */
    @SuppressWarnings("deprecation")
	public static WTChangeOrder2 getECNByNumber(String number){
        try {
        	if(!StringUtils.isEmpty(number)){
	            QuerySpec criteria = new QuerySpec(WTChangeOrder2.class);
	            criteria.appendSearchCondition(new SearchCondition(WTChangeOrder2.class, WTChangeOrder2.NUMBER,
	                    SearchCondition.EQUAL, number, false));
	            QueryResult results = PersistenceHelper.manager.find(criteria);
	            if (results.hasMoreElements()) {
	                return (WTChangeOrder2) results.nextElement();
	            }
        	}
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * get ECA by Number
     * @param number
     * @return
     */
    @SuppressWarnings("deprecation")
	public static WTChangeActivity2 getECAByNumber(String number){
        try {
        	if(!StringUtils.isEmpty(number)){
	            QuerySpec criteria = new QuerySpec(WTChangeActivity2.class);
	            criteria.appendSearchCondition(new SearchCondition(WTChangeActivity2.class, WTChangeActivity2.NUMBER,
	                    SearchCondition.EQUAL, number, false));
	            QueryResult results = PersistenceHelper.manager.find(criteria);
	            if (results.hasMoreElements()) {
	                return (WTChangeActivity2) results.nextElement();
	            }
        	}
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * get ECR by number
     * @param number
     * @return
     */
    @SuppressWarnings("deprecation")
	public static WTChangeRequest2 getECRByNumber(String number){
        try {
        	if(!StringUtils.isEmpty(number)){
	            QuerySpec criteria = new QuerySpec(WTChangeRequest2.class);
	            criteria.appendSearchCondition(new SearchCondition(WTChangeRequest2.class, WTChangeRequest2.NUMBER,
	                    SearchCondition.EQUAL, number, false));
	            QueryResult results = PersistenceHelper.manager.find(criteria);
	            if (results.hasMoreElements()) {
	                return (WTChangeRequest2) results.nextElement();
	            }
        	}
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    @SuppressWarnings("deprecation")
	public static WTChangeIssue getPRByNumber(String number){
        try {
        	if(!StringUtils.isEmpty(number)){
	            QuerySpec criteria = new QuerySpec(WTChangeIssue.class);
	            criteria.appendSearchCondition(new SearchCondition(WTChangeIssue.class, WTChangeIssue.NUMBER,
	                    SearchCondition.EQUAL, number, false));
	            QueryResult results = PersistenceHelper.manager.find(criteria);
	            if (results.hasMoreElements()) {
	                return (WTChangeIssue) results.nextElement();
	            }
        	}
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * set stateOfAfter by ChangeOrder2 
     * @param order
     * @param state
     */
    public static void setStateOfAfter(ChangeOrder2 order, State state) {
    	if(order!=null && state!=null){
    		try{
		        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(order);
		        WTList changeables = new WTArrayList(qr);
		        LifeCycleServerHelper.service.setState(changeables, state);
    		}catch(WTException e){
    			logger.error(">>>>>"+e);
    		}
    	}
    }
    
    /**
     * setStateOfBefore by ChangeOrder2
     * @param order
     * @param state
     * @throws WTException
     */
    public static void setStateOfBefore(ChangeOrder2 order, State state) throws WTException {
    	if(order!=null && state!=null){
    		try{
		        QueryResult qr = ChangeHelper2.service.getChangeablesBefore(order);
		        WTList changeables = new WTArrayList(qr);
		        LifeCycleServerHelper.service.setState(changeables, state);
    		}catch(WTException e){
    			logger.error(">>>>>"+e);
    		}
    	}
    }
    
    /**
     * get number by LifeCycleManager
     * @param lcm
     * @return
     */
    public static String getNumber(LifeCycleManaged lcm) {
        String number = "";
        if(lcm!=null){
	        if (lcm instanceof WTDocument) {
	           number = ((WTDocument) lcm).getNumber();
	        } else if (lcm instanceof WTPart) {
	           number = ((WTPart) lcm).getNumber();
	        } else if (lcm instanceof EPMDocument) {
	           number = ((EPMDocument) lcm).getNumber();
	        }
        }
        return number;
    }
    
    /**
     * judge whether the object has been existend in  the result item
     * @param persistable
     * @param ca
     * @return
     */
    public static boolean inChangeablesAfter(Persistable persistable, WTChangeActivity2 eca){
		boolean flag = false;
		try{
			if(persistable!=null && eca!=null){
				QueryResult caResult = ChangeHelper2.service.getChangeablesAfter(eca);
				while (caResult.hasMoreElements()) {
				    Persistable persistableAfter = (Persistable) caResult.nextElement();
				    flag = persistable.equals(persistableAfter);
				    if (flag) {
				        break;
				    }
				}
			}
		}catch(WTException e){
			logger.error(">>>>>"+e);
		}
		return flag;
	}
    
    /**
     * judge whether the object has been existend in  the affect item
     * @param persistable
     * @param ca
     * @return
     */
    public static boolean inChangeablesBefore(Persistable persistable, WTChangeActivity2 ca) {
		boolean flag = false;
		try{
			if(persistable!=null && ca!=null){
				QueryResult caResult = ChangeHelper2.service.getChangeablesBefore(ca);
				while (caResult.hasMoreElements()) {
				    Persistable persistableAfter = (Persistable) caResult.nextElement();
				    flag = persistable.equals(persistableAfter);
				    if (flag) {
				        break;
				    }
				}
			}
		}catch(WTException e){
			logger.error(">>>>>"+e);
		}
		return flag;
	}
    
	public static void test() throws RemoteException, InvocationTargetException, WTException{
		System.out.println(getNumber(PartUtil.getPartByNumber("asdf")));
		inChangeablesBefore(FolderUtil.getPersistableByOid("VR:wt.part.WTPart:171023"), getECAByNumber("00001"));
		/*List<WTChangeIssue> list = getPRByECR(getECRByNumber("00001"));
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}*/
		//System.out.println(getECRByECN(getECNByNumber("00021")));
		/*setStateOfAfter(getECNByNumber("00001"), State.RELEASED);
		setStateOfBefore(getECNByNumber("00001"), State.RELEASED);*/
		/*List<LifeCycleManaged> list = getChangeBefore2(getECNByNumber("00006"));
		for (int i = 0; i < list.size(); i++) {
			System.out.println(getNumber(list.get(i)));
			System.out.println(list.get(i));
		}*/
		/*System.out.println(inChangeablesBefore(FolderUtil.getPersistableByOid
				("OR:wt.part.WTPart:180129"),getECAByNumber("00021")));
		System.out.println(inChangeablesBefore(FolderUtil.getPersistableByOid
				("OR:wt.part.WTPart:152450"),getECAByNumber("00021")));*/
		/*WTChangeOrder2 changeOrder = createWTChangeOrder2("TestECN-0002_Number", "TestECN-0002_Name", "wt.change2.WTChangeOrder2",
				FolderUtil.getFolder(PartUtil.getPartByNumber("0000000041").getContainer(),""), "TestECN-000_Name");
		System.out.println(changeOrder);*/
		/*WTChangeOrder2 changeOrder1 = createWTChangeOrder2ByNumber("TestECN-0008_Number","TestECN-0008_Name", "wt.change2.WTChangeOrder2");
		System.out.println(changeOrder1);
		WTChangeOrder2 changeOrder2 = createWTChangeOrder2ByNumber("TestECN-0009_Number","TestECN-0009_Name", "wt.change2.WTChangeOrder2",
				FolderUtil.getFolder(PartUtil.getPartByNumber("0000000041").getContainer(),""));
		System.out.println(changeOrder2);
		WTChangeOrder2 changeOrder4 = createWTChangeOrder2ByNumber("TestECN-0010_Number","TestECN-0010_Name", "wt.change2.WTChangeOrder2", "TestECN-0010_Name");
		System.out.println(changeOrder4);*/
		
		
		//System.out.println(getECNByNumber("asdf"));
		/*System.out.println(getECNByECA(getECAByNumber("00021")));
    	List<WTChangeIssue> change = getPRByECR(getECRByNumber("00021"));
    	for (int i = 0; i < change.size(); i++) {
			System.out.println(change.get(i));
		}
    	List<WTChangeRequest2> change2 = getECRByECN(getECNByNumber("00021"));
    	for (int i = 0; i < change2.size(); i++) {
			System.out.println(change2.get(i));
		}*/
    	
    	/*System.out.println("------------------"+getECAByECN(getECNByNumber("00041")));
    	System.out.println("------------------"+getECNByECR(getECRByNumber("00021")));*/
    	//System.out.println(createECA(changeOrder, "TestECNA-001_Name"));
    	/*System.out.println(createWTChangeOrder2ByNumber("TestECN-006_Number","TestECN-005_Name", "wt.change2.WTChangeOrder2"));
    	System.out.println(createWTChangeOrder2ByNumber("TestECN-006_Number","TestECN-005_Name","wt.change2.WTChangeOrder2",
    			FolderUtil.getFolder(PartUtil.getPartByNumber("0000000061").getContainer(), "")));
    	
    	System.out.println(createWTChangeOrder2ByName("TestECN-005_Name", "wt.change2.WTChangeOrder2","sdfgg"));
    	System.out.println(createWTChangeOrder2ByName("TestECN-005_Name", "wt.change2.WTChangeOrder2"));
    	System.out.println(createWTChangeOrder2ByName("TestECN-005_Name","wt.change2.WTChangeOrder2",
    			FolderUtil.getFolder(PartUtil.getPartByNumber("0000000061").getContainer(), "")));*/
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", ChangeUtil.class.getName(), null,
					new Class[] {},
					new Object[] {});
		}
	}
}
