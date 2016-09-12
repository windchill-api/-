package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.scheduler.ScheduleItem;
import wt.session.SessionServerHelper;
import wt.util.WTException;

public class ScheduleQueueUtil implements RemoteAccess, Serializable {

	/**
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = ScheduleQueueUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	 
    /**
     * judge Whether ScheduleItem is exist by QueueName
     * @param scheduleQueueName
     * @return		if scheduleQueueName is exist,return true
     * 				else if scheduleQueueName is not exist,return false
     * 				else if scheduleQueueName is empty or null,return false
     * @throws WTException
     */
    @SuppressWarnings("deprecation")
	public static boolean isScheduleItemExistByQueueName(String scheduleQueueName){
    	try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	RemoteMethodServer.getDefault().invoke("isScheduleItemExistByQueueName", 
	            		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class },
	            		new Object[] { scheduleQueueName});
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
			        if(!StringUtils.isEmpty(scheduleQueueName)){
				        QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.QUEUE_NAME, SearchCondition.EQUAL,
				                scheduleQueueName));
				        queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
				        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
				        if (queryResult.hasMoreElements())
				            flag = true;
			        }
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally{
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
     * judge Whether ScheduleItem is exist by scheduleItemName
     * @param scheduleItemName
     * @return		if scheduleItemName is exist,return true
     * 				else if scheduleItemName is not exist,return false
     * 				else if scheduleItemName is empty or null,return false
     * @throws WTException
     */
    @SuppressWarnings("deprecation")
	public static boolean isScheduleItemExistByItemName(String scheduleItemName){
    	try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	RemoteMethodServer.getDefault().invoke("isScheduleItemExistByItemName", 
	            		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class },
	            		new Object[] { scheduleItemName});
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
			        if(!StringUtils.isEmpty(scheduleItemName)){
				        QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.ITEM_NAME, SearchCondition.EQUAL,
				        		scheduleItemName));
				        queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
				
				        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
				
				        if (queryResult.hasMoreElements())
				            flag = true;
			        }
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally{
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
     * judge Whether ScheduleItem is exist by ScheduleItem
     * @param scheduleItem
     * @return
     * @throws WTException
     */
    @SuppressWarnings("deprecation")
	public static boolean isScheduleItemExistByItem(ScheduleItem scheduleItem){
    	try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	RemoteMethodServer.getDefault().invoke("isScheduleItemExistByItem", 
	            		ScheduleQueueUtil.class.getName(), null, new Class[] { ScheduleItem.class },
	            		new Object[] { scheduleItem});
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
			        if(scheduleItem!=null){
				        QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.ITEM_NAME, SearchCondition.EQUAL,
				        		scheduleItem.getQueueName()));
				        queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
				        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
				        if (queryResult.hasMoreElements())
				            flag = true;
			        }
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally{
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
     * judge Whether ScheduleItem is exist by ScheduleItem
     * @param scheduleItem
     * @return
     * @throws WTException
     */
    @SuppressWarnings("deprecation")
	public static boolean isScheduleItemExist(String scheduleItemName,String scheduleQueueName){
    	try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	RemoteMethodServer.getDefault().invoke("isScheduleItemExist", 
	            		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class,String.class },
	            		new Object[] { scheduleItemName,scheduleQueueName});
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
			        if(!StringUtils.isEmpty(scheduleItemName) && !StringUtils.isEmpty(scheduleQueueName)){
				        QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.ITEM_NAME, SearchCondition.EQUAL,
				        		scheduleItemName));
				        queryspec.appendAnd();
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.QUEUE_NAME, SearchCondition.EQUAL,
				        		scheduleQueueName));
				        queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
				        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
				        if (queryResult.hasMoreElements())
				            flag = true;
			        }
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally{
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
     * Query schedule queue item by schedule queue name and schedule queue item name
     *
     * @param scheduleQueueName
     * @param scheduleItemName
     * @return
     * @throws WTException
     */
	@SuppressWarnings("deprecation")
	public static ScheduleItem getScheduleItem(String scheduleQueueName, String scheduleItemName){
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	                return (ScheduleItem) RemoteMethodServer.getDefault().invoke("getScheduleItem", 
	                		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class, 
	                	String.class},new Object[] { scheduleQueueName, scheduleItemName});
	        } else {
	        	ScheduleItem scheduleItem = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
		        	QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
			        if(!StringUtils.isEmpty(scheduleItemName) && isScheduleItemExistByItemName(scheduleItemName)
			        		&& !StringUtils.isEmpty(scheduleQueueName) && isScheduleItemExistByQueueName(scheduleQueueName)){
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.ITEM_NAME, SearchCondition.EQUAL,
				                scheduleItemName));
				        queryspec.appendAnd();
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.QUEUE_NAME, SearchCondition.EQUAL,
				                scheduleQueueName));
			        }
				    queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
				    QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
			        if (queryResult.hasMoreElements()){
			            scheduleItem = (ScheduleItem) queryResult.nextElement();
			        }
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally{
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
	        	return scheduleItem;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
        return null;
    }
	
	/**
	 * get scheduleItem by ItemName
	 * @param scheduleItemName
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static ScheduleItem getScheduleItemByItemName(String scheduleItemName){
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	                return (ScheduleItem) RemoteMethodServer.getDefault().invoke("getScheduleItemByItemName", 
	                		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] {scheduleItemName});
	        } else {
	        	ScheduleItem scheduleItem = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
		        	QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
			        if(!StringUtils.isEmpty(scheduleItemName) && isScheduleItemExistByItemName(scheduleItemName)){
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.ITEM_NAME, SearchCondition.EQUAL,
				                scheduleItemName));
			        }
				    queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
				
				    QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
				
			        while (queryResult.hasMoreElements()){
			            scheduleItem = (ScheduleItem) queryResult.nextElement();
			        }
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally{
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
	        	return scheduleItem;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
        return null;
    }
	
	/**
	 * get sheduleIte by QueueName
	 * @param scheduleQueueName
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static ScheduleItem getScheduleItemByQueueName(String scheduleQueueName){
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	                return (ScheduleItem) RemoteMethodServer.getDefault().invoke("getScheduleItemByQueueName", 
	                		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] {scheduleQueueName});
	        } else {
	        	ScheduleItem scheduleItem = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
		        	QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
			        if(!StringUtils.isEmpty(scheduleQueueName) && isScheduleItemExistByQueueName(scheduleQueueName)){
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.QUEUE_NAME, SearchCondition.EQUAL,
				        		scheduleQueueName));
			        }
				    queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
				    QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
			        while (queryResult.hasMoreElements()){
			            scheduleItem = (ScheduleItem) queryResult.nextElement();
			        }
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally{
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
		        return scheduleItem;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
        return null;
    }
	
    public static void test() throws RemoteException, InvocationTargetException, WTException{
//    	System.out.println("/*********************isScheduleItemExistByQueueName********************/");
//    	System.out.println(isScheduleItemExistByQueueName("commonScheduleQueue"));
//    	System.out.println(isScheduleItemExistByQueueName("asd"));
//    	System.out.println(isScheduleItemExistByQueueName(""));
//    	System.out.println(isScheduleItemExistByQueueName(null));
//    	System.out.println("/*********************isScheduleItemExistByItemName********************/");
//    	System.out.println(isScheduleItemExistByItemName("Unfinished Work Flow Check Queue1"));
//    	System.out.println(isScheduleItemExistByItemName("asd"));
//    	System.out.println(isScheduleItemExistByItemName(""));
//    	System.out.println(isScheduleItemExistByItemName(null));
//    	System.out.println("/*********************isScheduleItemExistByItemName********************/");
//    	System.out.println(isScheduleItemExistByItem(getScheduleItemByQueueName("commonScheduleQueue")));
//    	System.out.println(isScheduleItemExistByItem(getScheduleItemByQueueName("asd")));
//    	System.out.println(isScheduleItemExistByItem(null));
//    	System.out.println("/*********************isScheduleItemExist********************/");
//    	System.out.println(isScheduleItemExist("Unfinished Work Flow Check Queue1", "Unfinished Work Flow Check Queue1"));
//    	System.out.println(isScheduleItemExist("asd", "asd"));
//    	System.out.println(isScheduleItemExist("", ""));
//    	System.out.println(isScheduleItemExist(null, null));
//    	System.out.println("/*********************getScheduleItem********************/");
//    	System.out.println(getScheduleItem("Queue3", "Queue3"));
//    	System.out.println(getScheduleItem("asd", "asd"));
//    	System.out.println(getScheduleItem("", ""));
//    	System.out.println(getScheduleItem(null, null));
//    	System.out.println("/*********************getScheduleItemByItemName********************/");
//    	System.out.println(getScheduleItemByItemName("Queue3"));
//    	System.out.println(getScheduleItemByItemName("asd"));
//    	System.out.println(getScheduleItemByItemName(""));
//    	System.out.println(getScheduleItemByItemName(null));
//    	System.out.println("/*********************getScheduleItemByQueueName********************/");
//    	System.out.println(getScheduleItemByQueueName("Queue3"));
//    	System.out.println(getScheduleItemByQueueName("asd"));
//    	System.out.println(getScheduleItemByQueueName(""));
//    	System.out.println(getScheduleItemByQueueName(null));
    	
    }
	   
   public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", ScheduleQueueUtil.class.getName(), null,
					new Class[] {},
					new Object[] {});
		}
	}
	
}
