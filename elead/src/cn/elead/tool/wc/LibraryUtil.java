package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.container.WTContainerTemplateRef;
import wt.inf.library.WTLibrary;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class LibraryUtil implements RemoteAccess, Serializable {
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = LibraryUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * judge whether exist library in windChill
	 * @param name
	 * @return		if name is exist,return true
	 * 				else if name is not exist,name is empty or name is null,return false
	 */
	@SuppressWarnings("deprecation")
	public static Boolean isLibraryExist(String name) {
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	return (Boolean)RemoteMethodServer.getDefault().invoke("isLibraryExist", LibraryUtil.class.getName(), null,
	            			new Class[] { String.class }, new Object[] { name });
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	if (!StringUtils.isEmpty(name)) {
		        	try {
		        		QuerySpec queryspec = new QuerySpec(WTLibrary.class);
				        queryspec.appendWhere(new SearchCondition(WTLibrary.class, WTLibrary.NAME, SearchCondition.EQUAL, name));
				        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
				        if (queryResult.hasMoreElements()) {
				        	flag = true;
				        }
		        	} catch(WTException e) {
		        		logger.error(CLASSNAME+".isLibraryExist:"+e);
		        	} finally {
		        		SessionServerHelper.manager.setAccessEnforced(enforce);
		        	}
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
	 * get library by name
	 * @param name
	 * @return		if name is exist in windChill,return WTLibrary object
	 * 				else if name is not exist,return null
	 * 				else if name is empty or name is null,return null
	 */
	@SuppressWarnings("deprecation")
	public static WTLibrary getWTLibrary(String name) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTLibrary) RemoteMethodServer.getDefault().invoke("getWTLibrary", LibraryUtil.class.getName(), null, 
						new Class[] { String.class},
	                		new Object[] { name });
	        } else {
	        	WTLibrary library = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (!StringUtils.isEmpty(name) && isLibraryExist(name)) {
		        		QuerySpec criteria = new QuerySpec(WTLibrary.class);
				        criteria.appendWhere(new SearchCondition(WTLibrary.class, WTLibrary.NAME, SearchCondition.LIKE, name, false));
				        QueryResult results = PersistenceHelper.manager.find(criteria);
				        if (results.hasMoreElements()) {
				        	library = (WTLibrary) results.nextElement();
				        }
		        	}
				} catch(WTException e) {
					logger.error(CLASSNAME+".getWTLibrary:"+e);
				} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
		        return library;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
        return null;
    }
	
	/**
	 * create library by name
	 * @param name
	 * @return		if name is not exist in windChill,create WTLibrary and return this library
	 * 				else if name is exist in windChill,return this library
	 * 				else is name is empty or name is null and wtContainerTemplateRef is null,wtContainerRef is null there is nothing to do
	 */
	public static WTLibrary createLibrary(String name, WTContainerTemplateRef wtContainerTemplateRef, WTContainerRef wtContainerRef, String desc){
		try{
        	if (!RemoteMethodServer.ServerFlag) {
        		return (WTLibrary) RemoteMethodServer.getDefault().invoke("createLibrary", LibraryUtil.class.getName(), null, new Class[] { 
        			String.class, WTContainerTemplateRef.class, WTContainerRef.class, String.class }, new Object[] { name, wtContainerTemplateRef, wtContainerRef, desc });
	        } else {
	        	WTLibrary library = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		if (StringUtils.isEmpty(name) || wtContainerTemplateRef == null || wtContainerRef == null) {
	        			return null;
	        		}
	        		library = getWTLibrary(name);
	        		if (library == null) {
						library= WTLibrary.newWTLibrary();
						library.setName(name);
						if (!StringUtils.isEmpty(desc)) {
							library.setDescription(desc);	
						}
						library.setContainerReference(wtContainerRef);
						library.setContainerTemplateReference(wtContainerTemplateRef);
						WTContainerHelper.service.create(library);
	        		}
		        } catch(WTException e) {
	        		logger.error(CLASSNAME+".createLibrary:"+e);
	        	} catch (WTPropertyVetoException e) {
	        		logger.error(CLASSNAME+".createLibrary:"+e);
	        	} finally {
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
	        	return library;
	        }
        } catch (RemoteException exception) {
            logger.debug(exception.getMessage(),exception);
        } catch (InvocationTargetException exception) {
            logger.debug(exception.getMessage(),exception);
        }
        return null;
    }
	
	
	
	
	public static void test() throws RemoteException, InvocationTargetException, WTException{
//		System.out.println("/*********************isLibraryExist********************/");
//		System.out.println(isLibraryExist("library1"));
//		System.out.println(isLibraryExist("asd"));
//		System.out.println(isLibraryExist(""));
//		System.out.println(isLibraryExist(null));
//		System.out.println("/*********************getWTLibrary********************/");
//		System.out.println(getWTLibrary("library1"));
//		System.out.println(getWTLibrary("asd"));
//		System.out.println(getWTLibrary(""));
//		System.out.println(getWTLibrary(null));
//		System.out.println(createLibrary("library3", getWTLibrary("library1").getContainerTemplateReference(), 
//				OrganizationUtil.getOrgContainer("plm-training").getContainerReference()));
//		System.out.println(createLibrary("library3", ContainerTemplateHelper.service.getContainerTemplateRef(OrganizationUtil.getOrgContainer("plm-training").
//				getContainerReference(),"library1", WTLibrary.class), OrganizationUtil.getOrgContainer("plm-training").getContainerReference()));
//		createLibrary("library3", "library1", "plm-training", "asdf");
//		System.out.println(createLibrary("library6", WCUtil.getWTContainerTemplateRef(LibraryUtil.getWTLibrary("library1")),
//				WCUtil.getWTContainerref(OrganizationUtil.getWTOrganization("plm-training")), "asd"));
//		System.out.println(createLibrary("library2", WCUtil.getWTContainerTemplateRef(LibraryUtil.getWTLibrary("library1")),
//				WCUtil.getWTContainerref(OrganizationUtil.getWTOrganization("plm-training")), "asd"));
//		System.out.println(createLibrary("library7", WCUtil.getWTContainerTemplateRef(LibraryUtil.getWTLibrary("library1")),
//				WCUtil.getWTContainerref(OrganizationUtil.getWTOrganization("plm-training")), ""));
//		System.out.println(createLibrary("", WCUtil.getWTContainerTemplateRef(LibraryUtil.getWTLibrary("library1")),
//				WCUtil.getWTContainerref(OrganizationUtil.getWTOrganization("plm-training")), "asd"));
//		System.out.println(createLibrary("", null, null,"asd"));
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", LibraryUtil.class.getName(), null, new Class[] {}, new Object[] {});
		}
	}
	
}
