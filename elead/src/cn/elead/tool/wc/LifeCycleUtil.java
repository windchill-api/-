package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.drools.core.util.StringUtils;

import wt.change2.VersionableChangeItem;
import wt.enterprise.Managed;
import wt.enterprise.RevisionControlled;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplate;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.session.SessionServerHelper;
import wt.util.WTException;

public class LifeCycleUtil implements RemoteAccess, Serializable {
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = LifeCycleUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * get object lifeCycle
	 * @param part
	 * @return		if object is exist in windChill,return state
	 * 				else if object is not exist in windChill or object is null,return null
	 */
	public static State getLifeCycleState(WTObject object) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (State)RemoteMethodServer.getDefault().invoke("getLifeCycleState", LifeCycleUtil.class.getName(), null,
						new Class[] { LifeCycleUtil.class }, new Object[] { object });
	        } else {
	        	State state = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (object == null) {
						return null;
					}
					if (object instanceof RevisionControlled) {
						state = ((RevisionControlled) object).getLifeCycleState();
					} else if (object instanceof VersionableChangeItem) {
						state = ((VersionableChangeItem) object).getLifeCycleState();
					} else if (object instanceof Managed) {
						state = ((Managed) object).getLifeCycleState();
					}
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return state;
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get part lifeCycleTemplate by part
	 * @param part
	 * @return		if part is exist in windChill,return lifeCycleTemplate
	 * 				else if part is not exist in windChill or part is null,return null
	 */
	@SuppressWarnings("deprecation")
	public static LifeCycleTemplate getLifeCycleTemplate(WTObject object) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (LifeCycleTemplate)RemoteMethodServer.getDefault().invoke("getLifeCycleTemplate", 
						LifeCycleUtil.class.getName(), null, new Class[] { WTPart.class }, new Object[] { object });
	        } else {
	        	LifeCycleTemplate lt = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (object == null) {
						return null;
					}
					lt = LifeCycleHelper.service.getLifeCycleTemplate((LifeCycleManaged) object);
				} catch (WTException e) {
					logger.error(CLASSNAME+".getLifeCycleTemplate:"+e);
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return lt;
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * get part lifeCycleTemplate by part
	 * @param part
	 * @return		if part is exist in windChill,return lifeCycleTemplate
	 * 				else if part is not exist in windChill or part is null,return null
	 */
	@SuppressWarnings("deprecation")
	public static LifeCycleTemplate createLifeCycleTemplate(WTObject object) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (LifeCycleTemplate)RemoteMethodServer.getDefault().invoke("createLifeCycleTemplate", 
						LifeCycleUtil.class.getName(), null, new Class[] { WTPart.class }, new Object[] { object });
	        } else {
	        	LifeCycleTemplate lt = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (object == null) {
						return null;
					}
					lt = LifeCycleHelper.service.getLifeCycleTemplate((LifeCycleManaged) object);
				} catch (WTException e) {
					logger.error(CLASSNAME+".createLifeCycleTemplate:"+e);
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
				return lt;
	        }
		} catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * reSetLifeCycle
	 * @param part
	 * @return		if object is exist in windChill,reSet state
	 * 				else if object is not exist in windChill, object is null,state is empty or state is null,there is nothing to do
	 */
	public static void reSetLifeCycle(WTObject object,String state) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("reSetLifeCycle", LifeCycleUtil.class.getName(), null, new Class[] { LifeCycleUtil.class, String.class },
						new Object[] { object, state });
	        } else {
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (object == null || StringUtils.isEmpty("state")) {
						return;
					}
					object = (WTObject)LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) object,State.toState(state));
					PersistenceHelper.manager.refresh(object);
				} catch (WTException e) {
					logger.error(CLASSNAME+".reSetLifeCycle:"+e);
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
	
	public static void test() throws RemoteException, InvocationTargetException, WTException{
//		System.out.println("/*********************getLifeCycleState********************/");
//		System.out.println(getLifeCycleState(PartUtil.getPartByNumber("GC000001")));
//		System.out.println(getLifeCycleState(ChangeUtil.getPRByNumber("00041")));
//		System.out.println(getLifeCycleState(PromotionUtil.getPromotionNoticeByNumber("00001")));
//		System.out.println(getLifeCycleState(null));
//		System.out.println("/*********************getLifeCycleTemplate********************/");
//		System.out.println(getLifeCycleTemplate(PartUtil.getPartByNumber("GC000001")));
//		System.out.println(getLifeCycleTemplate(ChangeUtil.getPRByNumber("00041")));
//		System.out.println(getLifeCycleTemplate(PromotionUtil.getPromotionNoticeByNumber("00001")));
//		System.out.println(getLifeCycleTemplate(null));
//		System.out.println("/*********************reSetLifeCycle********************/");
//		reSetLifeCycle(PartUtil.getPartByNumber("GC000001"),"RELEASED");
//		reSetLifeCycle(ChangeUtil.getPRByNumber("00041"),"UNDERREVIEW");
//		reSetLifeCycle(null,"");
//		reSetLifeCycle(null,null);
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException {
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", LifeCycleUtil.class.getName(), null, new Class[] {}, new Object[] {});
		}
	}
}
