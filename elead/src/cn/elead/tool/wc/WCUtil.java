package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.log4j.Logger;

import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;

public class WCUtil implements RemoteAccess, Serializable {
	/**
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = WCUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	public static Persistable getPersistableByOid(String strOid){
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (Persistable) RemoteMethodServer.getDefault().invoke("getPersistableByOid", 
	                		WCUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] { strOid});
	        } else {
	        	Persistable per = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
					if (strOid != null && strOid.trim().length() > 0) {
						ReferenceFactory referencefactory = new ReferenceFactory();
						WTReference wtreference = referencefactory.getReference(strOid);
						per = wtreference != null ? wtreference
								.getObject() : null;
					}
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return per;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get WTContainer by Part
	 * @param wtPart
	 * @return
	 * @throws WTException
	 */
	public static WTContainer getWTContainerByPart(WTPart wtPart){
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainer) RemoteMethodServer.getDefault().invoke("getWTContainerByPart", 
	                		WCUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { wtPart});
	        } else {
	        	WTContainer wtContainer = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
					if(wtPart!=null && PartUtil.isPartExist(wtPart.getNumber())){
						wtContainer =  wtPart.getContainer();
					}
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return wtContainer;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
    }
	
	/**
	 * get WTContainer by document
	 * @param wtDoc
	 * @return
	 * @throws WTException
	 */
	public static WTContainer getWTContainerByDoc(WTDocument wtDoc)
            throws WTException {
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainer) RemoteMethodServer.getDefault().invoke("getWTContainerByDoc", 
	                		WCUtil.class.getName(), null, new Class[] { WTDocument.class},
	                		new Object[] { wtDoc});
	        } else {
	        	WTContainer wtContainer = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
					if(wtDoc!=null && PartUtil.isPartExist(wtDoc.getNumber())){
						wtContainer =  wtDoc.getContainer();
					}
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return wtContainer;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
    }
	
	/**
	 * get wtcontainerRef by WTContainer
	 * @param wtcontainer
	 * @return
	 * @throws WTException
	 */
	public static WTContainerRef getWTContainerref(WTContainer wtcontainer){
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainerRef) RemoteMethodServer.getDefault().invoke("getWTContainerref", 
	                		WCUtil.class.getName(), null, new Class[] { WTContainer.class},
	                		new Object[] { wtcontainer});
	        } else {
	        	WTContainerRef wtContainerRef = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
					if(wtcontainer!=null){
						wtContainerRef =  WTContainerRef.newWTContainerRef(wtcontainer);
					}
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return wtContainerRef;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
    }
    
	/**
	 * get WTContainerRef by part
	 * @param wtPart
	 * @return
	 * @throws WTException
	 */
	public static WTContainerRef getWTContainerrefByPart(WTPart wtPart){
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainerRef) RemoteMethodServer.getDefault().invoke("getWTContainerrefByPart", 
	                		WCUtil.class.getName(), null, new Class[] { WTPart.class},
	                		new Object[] { wtPart});
	        } else {
	        	WTContainerRef wtContainerRef = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
					if(wtPart!=null && PartUtil.isPartExist(wtPart.getNumber())){
						WTContainer wtcontainer = getWTContainerByPart(wtPart);
						wtContainerRef = WTContainerRef.newWTContainerRef(wtcontainer);
					}
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return wtContainerRef;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
    }
	
	/**
	 * get WTContainerRef by document
	 * @param wtDoc
	 * @return
	 * @throws WTException
	 */
	public static WTContainerRef getWTContainerrefByDoc(WTDocument wtDoc) {
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTContainerRef) RemoteMethodServer.getDefault().invoke("getWTContainerrefByDoc", 
	                		WCUtil.class.getName(), null, new Class[] { WTDocument.class},
	                		new Object[] { wtDoc});
	        } else {
	        	WTContainerRef wtContainerRef = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
					if(wtDoc!=null && PartUtil.isPartExist(wtDoc.getNumber())){
						WTContainer wtcontainer = getWTContainerByDoc(wtDoc);
						wtContainerRef = WTContainerRef.newWTContainerRef(wtcontainer);
					}
	        	} catch(WTException e){
	        		logger.error(">>>>>"+e);
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        	return wtContainerRef;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
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
    	try {
	        if (!RemoteMethodServer.ServerFlag) {
	                return (Timestamp) RemoteMethodServer.getDefault().invoke("getInitialStartTime", 
	                		WCUtil.class.getName(), null, new Class[] { String.class, 
	                	String.class, String.class },new Object[] { strHourOfDay, strMinute, strSecond });
	        } else {
	        	Timestamp timestamp = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try{
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
	        	} finally{
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
	        	return timestamp;
	        }
    	} catch (RemoteException e) {
    		logger.error(e.getMessage(),e);
    	} catch (InvocationTargetException e) {
    		logger.error(e.getMessage(),e);
    	}
        return null;
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
    	try {
        if (!RemoteMethodServer.ServerFlag) {
                return (Timestamp) RemoteMethodServer.getDefault().invoke("getInitialStartTime", 
                		WCUtil.class.getName(), null, new Class[] { int.class, 
                	int.class, int.class },new Object[] { nHourOfDay, nMinute, nSecond });
        } else {
        	Timestamp time = null;
        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        	try{
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
        	} finally{
        		SessionServerHelper.manager.setAccessEnforced(enforce);
        	}
        	return time;
        }
    	} catch (RemoteException e) {
    		logger.error(e.getMessage(),e);
    	} catch (InvocationTargetException e) {
    		logger.error(e.getMessage(),e);
    	}
        return null;
    }
    
	/**
	 * getWTContainerByName
	 * 
	 * @author zhangxj
	 * @param name
	 * @return WTContainer
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static WTContainer getWTContainerByName(String name)
			throws WTException {
		QuerySpec qs = new QuerySpec(WTContainer.class);
		SearchCondition sc = new SearchCondition(WTContainer.class,
				WTContainer.NAME, SearchCondition.EQUAL, name);
		qs.appendSearchCondition(sc);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr != null && qr.size() == 1) {
			return (WTContainer) qr.nextElement();
		} else {
			throw new WTException("WTContainer name:" + name
					+ " has more than one container or no container!");
		}
	}
}
