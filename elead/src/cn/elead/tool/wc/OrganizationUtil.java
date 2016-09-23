package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.OrgContainer;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.OrganizationServicesHelper;
import wt.org.WTOrganization;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class OrganizationUtil implements RemoteAccess, Serializable {
	/**
	 * this OrganizationUtil includes get,create,update organization,and some things of organization
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = OrganizationUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * judge whether organization is exist by name
	 * @param name
	 * @return		if name is exist in windChill,return true
	 * 				else if name is not exist in windChill,return false
	 * 				else if name is empty or null,return false
	 */
	@SuppressWarnings("deprecation")
	public static boolean isWTOrganizationExistByName(String name) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (boolean) RemoteMethodServer.getDefault().invoke("isWTOrganizationExistByName", 
						OrganizationUtil.class.getName(), null, new Class[] { String.class }, new Object[] { name });
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (!StringUtils.isEmpty(name)) {
		        		QuerySpec criteria = new QuerySpec(WTOrganization.class);
				        criteria.appendWhere(new SearchCondition(WTOrganization.class, WTOrganization.NAME, 
				        		SearchCondition.LIKE, name, false));
				        QueryResult results = PersistenceHelper.manager.find(criteria);
				        if (results.hasMoreElements()) {
				        	flag = true;
				        }
		        	}
				} catch(WTException e) {
					logger.error(CLASSNAME+".isWTOrganizationExistByName:"+e);
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
	 * judge whether organization is exist by organization
	 * @param name
	 * @return		if org is exist in windChill,return true
	 * 				else if org is not exist inwindChill,return false
	 * 				else if org is null,return false
	 */
	@SuppressWarnings("deprecation")
	public static boolean isWTOrganizationExistByOrg(WTOrganization org) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (boolean) RemoteMethodServer.getDefault().invoke("isWTOrganizationExistByOrg", 
						OrganizationUtil.class.getName(), null, new Class[] { WTOrganization.class },new Object[] { org });
	        } else {
	        	boolean flag = false;
	            boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (org == null) {
		        		return false;
		        	}
	        		QuerySpec criteria = new QuerySpec(WTOrganization.class);
			        criteria.appendWhere(new SearchCondition(WTOrganization.class, WTOrganization.NAME,
			        		SearchCondition.LIKE, org.getName(), false));
			        QueryResult results = PersistenceHelper.manager.find(criteria);
			        if (results.hasMoreElements()) {
			            flag = true;
			        }
				} catch(WTException e) {
					logger.error(CLASSNAME+".isWTOrganizationExistByOrg:"+e);
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
	 * get organization by name
	 * @param name
	 * @return		if name is exist in windChill,return organization object
	 * 				else if name is not exist,return null
	 * 				else if name is empty or name is null,return null
	 */
	@SuppressWarnings("deprecation")
	public static WTOrganization getWTOrganization(String name) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTOrganization) RemoteMethodServer.getDefault().invoke("getWTOrganization", 
						OrganizationUtil.class.getName(), null, new Class[] { String.class }, new Object[] { name });
	        } else {
	        	WTOrganization org = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (!StringUtils.isEmpty(name) && isWTOrganizationExistByName(name)) {
				        QuerySpec criteria = new QuerySpec(WTOrganization.class);
				        criteria.appendWhere(new SearchCondition(WTOrganization.class, WTOrganization.NAME, SearchCondition.LIKE, name, false));
				        QueryResult results = PersistenceHelper.manager.find(criteria);
				        if (results.hasMoreElements()) {
				            org = (WTOrganization) results.nextElement();
				        }
		        	}
				} catch(WTException e) {
					logger.error(CLASSNAME+".getWTOrganization:"+e);
				} finally {
		            SessionServerHelper.manager.setAccessEnforced(enforce);
		        }
		        return org;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }
	
	/**
	 * get organization container by name
	 * @param name
	 * @return		if name is exist,return container
	 * 				else if name is not exist,return null
	 * 				else if name is empty or name is null,return null
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static OrgContainer getOrgContainer(String name) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (OrgContainer) RemoteMethodServer.getDefault().invoke("getOrgContainer",
						OrganizationUtil.class.getName(), null, new Class[] { String.class }, new Object[] { name });
            } else {
            	OrgContainer orgContainer = null;
                boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
                try {
                	if (!StringUtils.isEmpty(name)) {
	                    QuerySpec criteria = new QuerySpec(OrgContainer.class);
	                    criteria.appendWhere(new SearchCondition(OrgContainer.class, OrgContainer.NAME, SearchCondition.EQUAL, name, false));
	                    QueryResult results = PersistenceHelper.manager.find(criteria);
	                    if (results.hasMoreElements()) {
	                    	orgContainer = (OrgContainer) results.nextElement();
	                    }
                	}
                } catch(Exception e) {
                	logger.error(CLASSNAME+".getOrgContainer:"+e);
                } finally {
                    SessionServerHelper.manager.setAccessEnforced(enforce);
                }
                return orgContainer;
            }
        } catch (RemoteException exception) {
            logger.debug(exception.getMessage(),exception);
        } catch (InvocationTargetException exception) {
            logger.debug(exception.getMessage(),exception);
        }
        return null;
    }
	
	/**
	 * create organization by name
	 * @param name
	 * @return		if name is not exist in windChill,create organization and return this org
	 * 				else if name is exist in windChill,there is nothing to do
	 * 				else is name is empty or name is null,there is nothing to do
	 */
	@SuppressWarnings("deprecation")
	public static WTOrganization createWTOrg(String name) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTOrganization) RemoteMethodServer.getDefault().invoke("createWTOrg", 
						OrganizationUtil.class.getName(), null, new Class[] { String.class }, new Object[] { name });
	        } else {
	        	WTOrganization org = null;
	            boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	            try {
	            	if (!StringUtils.isEmpty(name)) {
	            		org = getWTOrganization(name);
				        if (org == null) {
				        	org = WTOrganization.newWTOrganization(name);
				            org.setAdministrator(SessionHelper.manager.getPrincipalReference());
				            org.setConferencingURL(null);
				            org = (WTOrganization) OrganizationServicesHelper.manager.savePrincipal(org);
				            PersistenceHelper.manager.save(org);
				        }
			        }
	            } catch(WTException e) {
	            	logger.error(CLASSNAME+".createWTOrg:"+e);
	            } finally {
	            	SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	            return org;
	        }
        } catch (RemoteException exception) {
            logger.debug(exception.getMessage(),exception);
        } catch (InvocationTargetException exception) {
            logger.debug(exception.getMessage(),exception);
        }
        return null;
    }
	
	/**
	 * update organization by name
	 * @param name
	 * @param reName
	 * 		if name is exist in windChill,reName is not exist in WindChill,and reName is not empty or null,update the org
	 * 		else there is nothing to do
	 */
	public static void updateWTOrg(String name, String reName) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("updateWTOrg", OrganizationUtil.class.getName(), null,
						new Class[] { String.class, String.class }, new Object[] { name, reName });
	        } else {
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	try {
	        		if (!StringUtils.isEmpty(name) && !StringUtil.isEmpty(reName)) {
			        	WTOrganization org = getWTOrganization(name);
			        	WTOrganization org1 = getWTOrganization(reName);
				        if (org != null && org1==null) {
				        	org.setName(reName);
					        PersistenceHelper.manager.save(org);
				        }
			        }
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".updateWTOrg:"+e);
	        	} catch (WTPropertyVetoException e) {
	        		logger.error(CLASSNAME+".updateWTOrg:"+e);
	        	} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
	        }
        } catch (RemoteException exception) {
            logger.debug(exception.getMessage(),exception);
        } catch (InvocationTargetException exception) {
            logger.debug(exception.getMessage(),exception);
        }
    }
	
	public static void test() throws RemoteException, InvocationTargetException, WTException {
//		System.out.println("/*********************isWTOrganizationExistByName********************/");
//		System.out.println(isWTOrganizationExistByName("plm-training"));
//		System.out.println(isWTOrganizationExistByName("asd123"));
//		System.out.println(isWTOrganizationExistByName(""));
//		System.out.println(isWTOrganizationExistByName(null));
//		System.out.println("/*********************isWTOrganizationExistByName********************/");
//		System.out.println(isWTOrganizationExistByOrg(getWTOrganization("plm-training")));
//		System.out.println(isWTOrganizationExistByOrg(getWTOrganization("asd123")));
//		System.out.println(isWTOrganizationExistByOrg(getWTOrganization("")));
//		System.out.println(isWTOrganizationExistByOrg(getWTOrganization(null)));
//		System.out.println("/*********************getWTOrganization********************/");
//		System.out.println(getWTOrganization("plm-training"));
//		System.out.println(getWTOrganization("asd"));
//		System.out.println(getWTOrganization(""));
//		System.out.println(getWTOrganization(null));
//		System.out.println("/*********************getOrgContainer********************/");
//		System.out.println(getOrgContainer("plm-training"));
//		System.out.println(getOrgContainer("asd"));
//		System.out.println(getOrgContainer(""));
//		System.out.println(getOrgContainer(null));
//		System.out.println("/*********************createWTOrg********************/");
//		System.out.println(createWTOrg("newOrg-name1"));
//		System.out.println(createWTOrg("plm-training"));
//		System.out.println(createWTOrg(""));
//		System.out.println(createWTOrg(null));
//		System.out.println("/*********************updateWTOrg********************/");
//		updateWTOrg("test_OR", "newTest_OR");
//		updateWTOrg("org1", "plm-training");
//		updateWTOrg("org10", "");
//		updateWTOrg(null, "newTest_OR1");
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException {
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", OrganizationUtil.class.getName(), null, new Class[] {}, new Object[] {});
		}
	}
}
