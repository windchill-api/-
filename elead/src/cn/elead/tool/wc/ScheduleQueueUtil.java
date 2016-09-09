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
	public static boolean isScheduleItemExistByQueueName(String scheduleQueueName)
            throws WTException {
        boolean flag = false;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
            	RemoteMethodServer.getDefault().invoke("isScheduleItemExistByQueueName", 
            		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class },
            		new Object[] { scheduleQueueName});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
	        if(!StringUtils.isEmpty(scheduleQueueName)){
		        QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
		        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.QUEUE_NAME, SearchCondition.EQUAL,
		                scheduleQueueName));
		        queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
		        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
		        if (queryResult.hasMoreElements())
		            flag = true;
	        }
        }
        SessionServerHelper.manager.setAccessEnforced(enforce);
        return flag;
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
	public static boolean isScheduleItemExistByItemName(String scheduleItemName)
            throws WTException {
        boolean flag = false;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
            	RemoteMethodServer.getDefault().invoke("isScheduleItemExistByItemName", 
            		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class },
            		new Object[] { scheduleItemName});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
	        if(!StringUtils.isEmpty(scheduleItemName)){
		        QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
		        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.ITEM_NAME, SearchCondition.EQUAL,
		        		scheduleItemName));
		        queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
		
		        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
		
		        if (queryResult.hasMoreElements())
		            flag = true;
	        }
        }
        SessionServerHelper.manager.setAccessEnforced(enforce);
        return flag;
    }
    
    /**
     * judge Whether ScheduleItem is exist by ScheduleItem
     * @param scheduleItem
     * @return
     * @throws WTException
     */
    @SuppressWarnings("deprecation")
	public static boolean isScheduleItemExistByItem(ScheduleItem scheduleItem)
            throws WTException {
        boolean flag = false;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
            	RemoteMethodServer.getDefault().invoke("isScheduleItemExistByItem", 
            		ScheduleQueueUtil.class.getName(), null, new Class[] { ScheduleItem.class },
            		new Object[] { scheduleItem});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
	        if(scheduleItem!=null){
		        QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
		        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.ITEM_NAME, SearchCondition.EQUAL,
		        		scheduleItem.getQueueName()));
		        queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
		        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
		        if (queryResult.hasMoreElements())
		            flag = true;
	        }
        }
        SessionServerHelper.manager.setAccessEnforced(enforce);
        return flag;
    }
    
    /**
     * judge Whether ScheduleItem is exist by ScheduleItem
     * @param scheduleItem
     * @return
     * @throws WTException
     */
    @SuppressWarnings("deprecation")
	public static boolean isScheduleItemExist(String scheduleItemName,String scheduleQueueName)
            throws WTException {
        boolean flag = false;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
            	RemoteMethodServer.getDefault().invoke("isScheduleItemExist", 
            		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class,String.class },
            		new Object[] { scheduleItemName,scheduleQueueName});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
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
        }
        SessionServerHelper.manager.setAccessEnforced(enforce);
        return flag;
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
	public static ScheduleItem getScheduleItem(String scheduleQueueName, String scheduleItemName)
            throws WTException {
        ScheduleItem scheduleItem = null;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
                return (ScheduleItem) RemoteMethodServer.getDefault().invoke("getScheduleItem", 
                		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class, 
                	String.class},new Object[] { scheduleQueueName, scheduleItemName});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
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
		
	        if (queryResult.hasMoreElements())
	            scheduleItem = (ScheduleItem) queryResult.nextElement();
        }
        SessionServerHelper.manager.setAccessEnforced(enforce);
        return scheduleItem;
    }
	
	/**
	 * get scheduleItem by ItemName
	 * @param scheduleItemName
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static ScheduleItem getScheduleItemByItemName(String scheduleItemName)
            throws WTException {
        ScheduleItem scheduleItem = null;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
                return (ScheduleItem) RemoteMethodServer.getDefault().invoke("getScheduleItemByItemName", 
                		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class},
                		new Object[] {scheduleItemName});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
        	QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
	        if(!StringUtils.isEmpty(scheduleItemName) && isScheduleItemExistByItemName(scheduleItemName)){
		        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.ITEM_NAME, SearchCondition.EQUAL,
		                scheduleItemName));
	        }
		    queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
		
		    QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
		
	        while (queryResult.hasMoreElements())
	            scheduleItem = (ScheduleItem) queryResult.nextElement();
        }
        SessionServerHelper.manager.setAccessEnforced(enforce);
        return scheduleItem;
    }
	
	/**
	 * get sheduleIte bu QueueName
	 * @param scheduleQueueName
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static ScheduleItem getScheduleItemByQueueName(String scheduleQueueName)
            throws WTException {
        ScheduleItem scheduleItem = null;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
                return (ScheduleItem) RemoteMethodServer.getDefault().invoke("getScheduleItemByQueueName", 
                		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class},
                		new Object[] {scheduleQueueName});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
        	QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
	        if(!StringUtils.isEmpty(scheduleQueueName) && isScheduleItemExistByQueueName(scheduleQueueName)){
		        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.QUEUE_NAME, SearchCondition.EQUAL,
		        		scheduleQueueName));
	        }
		    queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
		
		    QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
		
	        while (queryResult.hasMoreElements())
	            scheduleItem = (ScheduleItem) queryResult.nextElement();
        }
        SessionServerHelper.manager.setAccessEnforced(enforce);
        return scheduleItem;
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
