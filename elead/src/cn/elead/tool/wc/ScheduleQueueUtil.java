package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;

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
import wt.scheduler.SchedulingHelper;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;

public class ScheduleQueueUtil implements RemoteAccess, Serializable {

	/**
	 * this ScheduleQueueUtil includes create,get scheduleQueue and some basic things of ScheduleQueue
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
	public static Boolean isScheduleItemExistByQueueName(String scheduleQueueName) {
    	try {
    		if (!RemoteMethodServer.ServerFlag) {
    			return (Boolean)RemoteMethodServer.getDefault().invoke("isScheduleItemExistByQueueName", ScheduleQueueUtil.class.getName(), null,
    					new Class[] { String.class }, new Object[] { scheduleQueueName });
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		if (!StringUtils.isEmpty(scheduleQueueName)) {
	        			QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
				        queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.QUEUE_NAME, SearchCondition.EQUAL, scheduleQueueName));
				        queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
				        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
				        if (queryResult.hasMoreElements()) {
				            flag = true;
				        }
			        }
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".isScheduleItemExistByQueueName:"+e);
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
	 * get sheduleIte by QueueName
	 * @param scheduleQueueName
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static ScheduleItem getScheduleItemByQueueName(String scheduleQueueName) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ScheduleItem) RemoteMethodServer.getDefault().invoke("getScheduleItemByQueueName", 
						ScheduleQueueUtil.class.getName(), null, new Class[] { String.class }, new Object[] { scheduleQueueName });
	        } else {
	        	ScheduleItem scheduleItem = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
			        if (!StringUtils.isEmpty(scheduleQueueName) && isScheduleItemExistByQueueName(scheduleQueueName)) {
			        	QuerySpec queryspec = new QuerySpec(ScheduleItem.class);
			        	queryspec.appendWhere(new SearchCondition(ScheduleItem.class, ScheduleItem.QUEUE_NAME, 
			        			SearchCondition.EQUAL, scheduleQueueName));
			        	queryspec.appendOrderBy(ScheduleItem.class, ScheduleItem.START_DATE, true);
					    QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
				        while (queryResult.hasMoreElements()) {
				            scheduleItem = (ScheduleItem) queryResult.nextElement();
				        }
			        }
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".getScheduleItemByQueueName:"+e);
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
     * Create schedule queue
     *
     * @param scheduleQueueName
     * @param scheduleItemName
     * @param scheduleItemDescription
     * @param targetClass
     * @param targetMethod
     * @param initialStartTime
     * @param initialStartTime
     * @throws WTException
     */
	public static ScheduleItem createScheduleQueue(String scheduleQueueName, String scheduleItemName, String scheduleItemDescription,
			String targetClass, String targetMethod, Timestamp initialStartTime){
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ScheduleItem) RemoteMethodServer.getDefault().invoke("createScheduleQueue", ScheduleQueueUtil.class.getName(), null, 
						new Class[] { String.class, String.class, String.class, String.class, String.class, Timestamp.class }, new Object[]
								{ scheduleQueueName, scheduleItemName, scheduleItemDescription, targetClass, targetMethod, initialStartTime });
	        } else {
	        	ScheduleItem schItem = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (initialStartTime == null) {
						return null;
					}
					schItem = getScheduleItemByQueueName(scheduleQueueName);
					if (schItem == null) {
						schItem = ScheduleItem.newScheduleItem();
						if (!StringUtils.isEmpty(scheduleItemDescription)) {
							schItem.setItemDescription(scheduleItemDescription);
						}
						if (!StringUtils.isEmpty(scheduleItemName)) {
							schItem.setItemName(scheduleItemName);
						}
						if (!StringUtils.isEmpty(targetClass)) {
							schItem.setTargetClass(targetClass);
						}
						if (!StringUtils.isEmpty(targetMethod))  {
							schItem.setTargetMethod(targetMethod);
						}
						schItem.setImmediateMode(false);
						if (!StringUtils.isEmpty(scheduleQueueName)) {
							schItem.setQueueName(scheduleQueueName);
						}
						schItem.setToBeRun(-1);
						schItem.setPrincipalRef(SessionHelper.manager.getPrincipalReference());
						schItem.setStartDate(initialStartTime);
						schItem.setNextTime(initialStartTime);
						SchedulingHelper.service.addItem(schItem, null);
					}
				} catch(WTException e) {
					logger.error(">>>>>"+e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return schItem;
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
//    	System.out.println("/*********************getScheduleItemByQueueName********************/");
//    	System.out.println(getScheduleItemByQueueName("Queue3"));
//    	System.out.println(getScheduleItemByQueueName("asd"));
//    	System.out.println(getScheduleItemByQueueName(""));
//    	System.out.println(getScheduleItemByQueueName(null));
//    	System.out.println("/*********************createScheduleQueue********************/");
//    	System.out.println(createScheduleQueue("scheduleQueue-name2", "scheduleItem-name2", "schedule-discription2", "ScheduleQueueUtil", 
//    			"getScheduleItem", WCUtil.getInitialStartTime("8", "00", "0")));
//    	System.out.println(createScheduleQueue("scheduleQueue-name2", "scheduleItem-name2", "schedule-discription2", "ScheduleQueueUtil",
//    			"getScheduleItem", WCUtil.getInitialStartTime("8", "00", "0")));
//    	System.out.println(createScheduleQueue("scheduleQueue", "scheduleItem-name2", "schedule-discription2", "ScheduleQueueUtil", "",
//    			WCUtil.getInitialStartTime("8", "00", "0")));
//    	System.out.println(createScheduleQueue("", "scheduleItem-name2", "schedule-discription2", "ScheduleQueueUtil", "getScheduleItem",
//    			WCUtil.getInitialStartTime("8", "00", "0")));
//    	System.out.println(createScheduleQueue("", "scheduleItem-name2", "schedule-discription2", "", "getScheduleItem", null));
//    	System.out.println(createScheduleQueue("scheduleQueue-name3", "scheduleItem-name3", "", "", "getScheduleItem", null));
    }
	   
   public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException {
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", ScheduleQueueUtil.class.getName(), null, new Class[] {}, new Object[] {});
		}
	}
	
}
