package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.fc.Persistable;
import wt.fc.PersistenceServerHelper;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;

import com.ptc.core.lwc.server.PersistableAdapter;
import com.ptc.core.meta.common.UpdateOperationIdentifier;

public class MBAUtil implements RemoteAccess, Serializable {
    private static final long serialVersionUID = 150882489242046185L;
    private static final Logger LOGGER = LogR.getLogger(MBAUtil.class.getName());
	private static String CLASSNAME = DocUtil.class.getName();


    /**
     * @author BJJ
     * set Object Value
     * 
     * @param per
     *            : Persistable
     * @param dataMap
     *            : Map<String, Object>
     * @throws WTException
     *             : windchill exception
     */
    public static void setObjectValue(Persistable per, Map<String, Object> dataMap) throws WTException {
    	try {
			if (!RemoteMethodServer.ServerFlag) {
			    RemoteMethodServer.getDefault().invoke("setObjectValue", MBAUtil.class.getName(), null, new Class[] { 
			    	Persistable.class, Map.class },new Object[] { per, dataMap });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				try {
					PersistableAdapter obj = new PersistableAdapter(per, null, SessionHelper.getLocale(), new UpdateOperationIdentifier());
				    Iterator<String> keyIt = dataMap.keySet().iterator();
				    String key = null;
				    obj.load(dataMap.keySet());
				    while (keyIt.hasNext()) {
				        key = keyIt.next();
				        obj.set(key, dataMap.get(key));
				    }
				    obj.apply();
				    PersistenceServerHelper.manager.update(per);
					
				} catch (WTException e) {
					LOGGER.error(CLASSNAME+".setObjectValue:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
		} catch (RemoteException | InvocationTargetException e) {
			LOGGER.error(e.getMessage(),e);
		}
    }
    
    
    /**
     * @author BJJ
     * @param p
     * @param key
     * @return
     * @throws WTException
     * @throws InvocationTargetException 
     * @throws RemoteException 
     */
    public static Object getMBAValue(Persistable p, String key) throws WTException  {
    	try {
			if (!RemoteMethodServer.ServerFlag) {
			    return (Object) RemoteMethodServer.getDefault().invoke("getMBAValue", MBAUtil.class.getName(), null, new Class[] { 
			    	Persistable.class, String.class },new Object[] { p, key });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
			    Locale loc = null;
			    try {
			        loc = SessionHelper.getLocale();
			    } catch (WTException e) {
					LOGGER.error(CLASSNAME+".getMBAValue:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			    return getMBAValue(p, loc, key);
			}
		} catch (RemoteException | InvocationTargetException e) {
			LOGGER.error(e.getMessage(),e);
		}
		return key;
    }
    
    
    /**
     * @param p
     * @param key
     * @return
     * @throws WTException
     */
    public static Object getMBAValue(Persistable targetObj, Locale locale, String ibaName) throws WTException {
        try {
			Object ibaValue = null;     
			if (!RemoteMethodServer.ServerFlag) {
			        return (Object) RemoteMethodServer.getDefault().invoke("getValue", MBAUtil.class.getName(), null, new Class[] { 
			        	Persistable.class, Locale.class, String.class },new Object[] { targetObj, locale, ibaName });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
			    try {
			    	PersistableAdapter    obj = new PersistableAdapter(targetObj, null, locale, null);
			        obj.load(ibaName);
			        ibaValue = obj.get(ibaName);
			    } catch (WTException e) {
					LOGGER.error(CLASSNAME+".getMBAValue:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}			
			    return ibaValue;
        }
		} catch (RemoteException | InvocationTargetException e) {
			LOGGER.error(e.getMessage(),e);
		}
		return null;
    }


}

