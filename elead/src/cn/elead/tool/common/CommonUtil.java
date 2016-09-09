package cn.elead.tool.common;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.log4j.Logger;

import cn.elead.tool.wc.ChangeUtil;
import cn.elead.tool.wc.PartUtil;
import cn.elead.tool.wc.ScheduleQueueUtil;
import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.session.SessionServerHelper;
import wt.util.WTException;

public class CommonUtil implements RemoteAccess, Serializable {
	/**
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = CommonUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	public static Persistable getPersistableByOid(String strOid)
			throws WTException {
		Persistable per = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (Persistable) RemoteMethodServer.getDefault().invoke("getPersistableByOid", 
	                		ChangeUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] { strOid});
	        } else {
				if (strOid != null && strOid.trim().length() > 0) {
					ReferenceFactory referencefactory = new ReferenceFactory();
					WTReference wtreference = referencefactory.getReference(strOid);
					per = wtreference != null ? wtreference
							.getObject() : null;
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return per;
	}
	
	/**
	 * get WTContainer by Part
	 * @param wtPart
	 * @return
	 * @throws WTException
	 */
	public static WTContainer getWTContainerByPart(WTPart wtPart)
            throws WTException {
		WTContainer wtContainer = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainer) RemoteMethodServer.getDefault().invoke("getWTContainerByPart", 
	                		ChangeUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { wtPart});
	        } else {
				if(wtPart!=null && PartUtil.isPartExist(wtPart.getNumber())){
					wtContainer =  wtPart.getContainer();
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return wtContainer;
    }
	
	/**
	 * get WTContainer by document
	 * @param wtDoc
	 * @return
	 * @throws WTException
	 */
	public static WTContainer getWTContainerByDoc(WTDocument wtDoc)
            throws WTException {
		WTContainer wtContainer = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainer) RemoteMethodServer.getDefault().invoke("getWTContainerByDoc", 
	                		ChangeUtil.class.getName(), null, new Class[] { WTDocument.class},
	                		new Object[] { wtDoc});
	        } else {
				if(wtDoc!=null && PartUtil.isPartExist(wtDoc.getNumber())){
					wtContainer =  wtDoc.getContainer();
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return wtContainer;
    }
	
	/**
	 * get wtcontainerRef by WTContainer
	 * @param wtcontainer
	 * @return
	 * @throws WTException
	 */
	public static WTContainerRef getWTContainerref(WTContainer wtcontainer)
            throws WTException {
		WTContainerRef wtContainerRef = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainerRef) RemoteMethodServer.getDefault().invoke("getWTContainerref", 
	                		ChangeUtil.class.getName(), null, new Class[] { WTContainer.class},
	                		new Object[] { wtcontainer});
	        } else {
				if(wtcontainer!=null){
					wtContainerRef =  WTContainerRef.newWTContainerRef(wtcontainer);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return wtContainerRef;
    }
    
	/**
	 * get WTContainerRef by part
	 * @param wtPart
	 * @return
	 * @throws WTException
	 */
	public static WTContainerRef getWTContainerrefByPart(WTPart wtPart)
            throws WTException {
		WTContainerRef wtContainerRef = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainerRef) RemoteMethodServer.getDefault().invoke("getWTContainerrefByPart", 
	                		ChangeUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { wtPart});
	        } else {
				if(wtPart!=null && PartUtil.isPartExist(wtPart.getNumber())){
					WTContainer wtcontainer = getWTContainerByPart(wtPart);
					wtContainerRef = WTContainerRef.newWTContainerRef(wtcontainer);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return wtContainerRef;
    }
	
	/**
	 * get WTContainerRef by document
	 * @param wtDoc
	 * @return
	 * @throws WTException
	 */
	public static WTContainerRef getWTContainerrefByDoc(WTDocument wtDoc)
            throws WTException {
		WTContainerRef wtContainerRef = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainerRef) RemoteMethodServer.getDefault().invoke("getWTContainerrefByDoc", 
	                		ChangeUtil.class.getName(), null, new Class[] { WTDocument.class},
	                		new Object[] { wtDoc});
	        } else {
				if(wtDoc!=null && PartUtil.isPartExist(wtDoc.getNumber())){
					WTContainer wtcontainer = getWTContainerByDoc(wtDoc);
					wtContainerRef = WTContainerRef.newWTContainerRef(wtcontainer);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return wtContainerRef;
    }
	
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
    
	
}
