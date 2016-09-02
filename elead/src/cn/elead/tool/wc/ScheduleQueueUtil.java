package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Calendar;

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
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = ScheduleQueueUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
    /**
     * Get Initial start time of schedule queue task
     *
     * @param strHourOfDay
     * @param strMinute
     * @param strSecond
     * @return
     */
    public static Timestamp getInitialStartTime(String strHourOfDay, String strMinute, String strSecond) {
    	Timestamp timestamp = null;
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
                return (Timestamp) RemoteMethodServer.getDefault().invoke("getInitialStartTime", 
                		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class, 
                	String.class, String.class },new Object[] { strHourOfDay, strMinute, strSecond });
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
        	int nHourOfDay = 0;
            int nMinute = 0;
            int nSecond = 0;
	        if (strHourOfDay != null && strHourOfDay.length() > 0)
	            nHourOfDay = Integer.valueOf(strHourOfDay).intValue();
	
	        if (strMinute != null && strMinute.length() > 0)
	            nMinute = Integer.valueOf(strMinute).intValue();
	
	        if (strSecond != null && strSecond.length() > 0)
	            nSecond = Integer.valueOf(strSecond).intValue();
	        timestamp = getInitialStartTime(nHourOfDay, nMinute, nSecond);
        }
        SessionServerHelper.manager.setAccessEnforced(enforce);
        return timestamp;
    }
    
    /**
     * Get Initial start time of schedule queue task
     *
     * @param nHourOfDay
     * @param nMinute
     * @param nSecond
     * @return
     */
    public static Timestamp getInitialStartTime(int nHourOfDay, int nMinute, int nSecond) {
    	Timestamp time = null;
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
                return (Timestamp) RemoteMethodServer.getDefault().invoke("getInitialStartTime", 
                		ScheduleQueueUtil.class.getName(), null, new Class[] { int.class, 
                	int.class, int.class },new Object[] { nHourOfDay, nMinute, nSecond });
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
	        Calendar calendar = Calendar.getInstance();
	        Calendar calendar1 = Calendar.getInstance();
	        if(Calendar.getInstance().getTimeZone().getID().equals("GMT")){
	        	calendar1.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY+8);
	        }
	        calendar.set(Calendar.HOUR_OF_DAY, nHourOfDay);
	        calendar.set(Calendar.MINUTE, nMinute);
	        calendar.set(Calendar.SECOND, nSecond);
	        if (calendar.after(calendar1)){
	        	time = new Timestamp(calendar.getTimeInMillis());
	        }else{
	        	time = new Timestamp(calendar.getTimeInMillis() + 24 * 60 * 60 * 1000);
	        }
        }
        SessionServerHelper.manager.setAccessEnforced(enforce);
        return time;
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
     * @param rescheduleIntervalInSeconds
     * @throws WTException
     */
	public static ScheduleItem createScheduleQueue(String scheduleQueueName, String scheduleItemName, 
			String scheduleItemDescription, String targetClass,
            String targetMethod, Timestamp initialStartTime, long rescheduleIntervalInSeconds){
		ScheduleItem schItem = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
                return (ScheduleItem) RemoteMethodServer.getDefault().invoke("createScheduleQueue", 
                		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class, 
                	String.class, String.class,String.class, String.class,Timestamp.class,
                	long.class},new Object[] { scheduleQueueName, scheduleItemName, scheduleItemDescription,
                	targetClass,targetMethod,initialStartTime,rescheduleIntervalInSeconds});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
			try{
				// Query schedule item
				schItem = getScheduleItem(scheduleQueueName, scheduleItemName);
				if (schItem == null && initialStartTime!=null) {
					// Create new schedule item
					schItem = ScheduleItem.newScheduleItem();
					if(!StringUtils.isEmpty(scheduleItemDescription)){
						schItem.setItemDescription(scheduleItemDescription);
					}
					if(!StringUtils.isEmpty(scheduleItemName)){
						schItem.setItemName(scheduleItemName);
					}
					if(!StringUtils.isEmpty(targetClass)){
						schItem.setTargetClass(targetClass);
					}
					if(!StringUtils.isEmpty(targetMethod)){
						schItem.setTargetMethod(targetMethod);
					}
					schItem.setImmediateMode(false);
					schItem.setPeriodicity(rescheduleIntervalInSeconds); // in seconds
					if(!StringUtils.isEmpty(scheduleQueueName)){
						schItem.setQueueName(scheduleQueueName);
					}
					schItem.setToBeRun(-1);
					schItem.setPrincipalRef(SessionHelper.manager.getPrincipalReference());
					schItem.setStartDate(initialStartTime);
					schItem.setNextTime(initialStartTime);
					SchedulingHelper.service.addItem(schItem, null);
				}
			}catch(WTException e){
				logger.error(">>>>>"+e);
			}finally{
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
        }
		return schItem;
	}
	
	/*public static ScheduleItem updateScheduleQueue(String scheduleQueueName, String scheduleItemName,
			Timestamp initialStartTime, long rescheduleIntervalInSeconds){
		ScheduleItem schItem = null;
		try{
				schItem = getScheduleItem(scheduleQueueName, scheduleItemName);
				if (schItem!=null) {
					// Update old schedule item
					if(initialStartTime!=null){
						schItem.setStartDate(initialStartTime);
						schItem.setNextTime(initialStartTime);
					}
					schItem.setPeriodicity(rescheduleIntervalInSeconds); // in seconds
					schItem.setToBeRun(-1l);
					//schItem = SchedulingHelper.service.modifyItem(schItem);
					schItem = SchedulingHelper.service.addItem(schItem, null);
					
				}
		}catch(WTException e){
			logger.error(">>>>>"+e);
		}
		return schItem;
	}*/
	
	
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
	
    /**
     * delete SchedItems by scheduleQueueName
     *
     * @throws WTException
     */
    /*public static void removeSchedItemsWithQueueName(String scheduleQueueName){
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
            	RemoteMethodServer.getDefault().invoke("removeSchedItemsWithQueueName", 
            		ScheduleQueueUtil.class.getName(), null, new Class[] { String.class },
            		new Object[] { scheduleQueueName});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
	        try {
	        	if(isScheduleItemExist(scheduleQueueName) && !StringUtils.isEmpty(scheduleQueueName)){
	        		SchedulingHelper.removeSchedItemsWithQueueName(scheduleQueueName);
	        	}
			} catch (WTException e) {
				logger.error(">>>>>"+e);
			} finally{
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
        }
    }*/
    
    /**
     * delete SchedItems by schedItem
     *
     * @throws WTException
     */
    public static void removeSchedItemsWithSchedItems(ScheduleItem schedItem){
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        if (!RemoteMethodServer.ServerFlag) {
            try {
            	RemoteMethodServer.getDefault().invoke("removeSchedItemsWithSchedItems", 
            		ScheduleQueueUtil.class.getName(), null, new Class[] { ScheduleItem.class },
            		new Object[] { schedItem});
            } catch (RemoteException e) {
                logger.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
            	logger.error(e.getMessage(),e);
            }
        } else {
	        try {
	        	if(isScheduleItemExistByItem(schedItem) && schedItem!=null){
	        		SchedulingHelper.service.removeItem(schedItem);
	        	}
			} catch (WTException e) {
				logger.error(">>>>>"+e);
			} finally{
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
        }
    }
    
    /**
     * judge Whether ScheduleItem is exist by QueueName
     * @param scheduleQueueName
     * @return
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
     * @return
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
    
    public static void test() throws RemoteException, InvocationTargetException, WTException{
    	/*System.out.println(getScheduleItem("Queue1", "Queue1"));
    	
    	System.out.println(isScheduleItemExistByItemName("Unfinished Work Flow Check Queue12"));
    	System.out.println(isScheduleItemExistByItemName("asdf"));
    	System.out.println("-----------------------------------");
    	System.out.println(isScheduleItemExistByQueueName("Unfinished Work Flow Check Queue12"));
    	System.out.println(isScheduleItemExistByQueueName("Queue1"));
    	System.out.println("-----------------------------------");*/
    	getScheduleItemByItemName("Unfinished Work Flow Check Queue12");
    	getScheduleItemByItemName("Unfinished Work Flow Check");
    	//System.out.println("-----------------------------------");
    	/*System.out.println(isScheduleItemExist("Unfinished Work Flow Check Queue12", "Unfinished Work Flow Check Queue12"));
    	System.out.println(isScheduleItemExist("asdf", "asdf"));*/
	   /*System.out.println(getInitialStartTime("25", "0", "0"));
	   System.out.println("--------------------------");
	   System.out.println(getInitialStartTime("", "0", "0"));*/
	   //removeSchedItemsWithSchedItems(getScheduleItem("Unfinished Work Flow Check Queue24", "Unfinished Work Flow Check Queue24"));
		//System.out.println(getScheduleItem(SCHEDULE_QUEUE_NAME, SCHEDULE_ITEM_NAME));
		//System.out.println("---------------------------------------------------------");
	   //System.out.println(getScheduleItem("Unfinished Work Flow Check Queue23", "Unfinished Work Flow Check Queue23"));
	   //removeSchedItemsWithSchedItems(getScheduleItem("Unfinished Work Flow Check Queue11", "Unfinished Work Flow Check Queue11"));
	   //System.out.println(getScheduleItem("Unfinished Work Flow Check Queue11", "Unfinished Work Flow Check Queue11"));
	   //System.out.println(getScheduleItem("Unfinished Work Flow Check Queue12", ""));
	   /*System.out.println("-----------------------------------------------------");
	   System.out.println(createScheduleQueue("Queue2", "Queue2",
			   "Queue2", CLASSNAME, "runTime", getInitialStartTime("6", "00", "0"), 
			   Long.valueOf(24*60).longValue() * 60));*/
	   /*System.out.println(updateScheduleQueue("Unfinished Work Flow Check Queue20", "Unfinished Work Flow Check Queue20", 
			   getInitialStartTime("10","00","0"), periodicityInSeconds));*/
	   //removeSchedItemsWithQueueName("Unfinished Work Flow Check Queue20");
	   /*System.out.println(isScheduleItemExist("Unfinished Work Flow Check Queue40"));
	   System.out.println(isScheduleItemExist("Unfinished Work Flow Check Queue30"));*/
    	
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
