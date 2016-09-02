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
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = OrganizationUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * judge whether organization is exist by name
	 * @param name
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static boolean isWTOrganizationExistByName(String name) {
        boolean flag = false;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (boolean) RemoteMethodServer.getDefault().invoke("isWTOrganizationExistByName", 
	                		OrganizationUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] { name });
	        } else {
		        try{
		        	if(!StringUtils.isEmpty(name)){
				        QuerySpec criteria = new QuerySpec(WTOrganization.class);
				        criteria.appendWhere(new SearchCondition(WTOrganization.class, WTOrganization.NAME, SearchCondition.LIKE, name,
				                false));
				        QueryResult results = PersistenceHelper.manager.find(criteria);
				        if (results.hasMoreElements()) {
				            flag = true;
				        }
		        	}
				}catch(WTException e){
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return flag;
    }
	
	/**
	 * judge whether organization is exist by organization
	 * @param name
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static boolean isWTOrganizationExistByOrg(WTOrganization org) {
        boolean flag = false;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (boolean) RemoteMethodServer.getDefault().invoke("isWTOrganizationExistByOrg", 
	                		OrganizationUtil.class.getName(), null, new Class[] { WTOrganization.class},
	                		new Object[] { org });
	        } else {
		        try{
		        	if(org!=null){
				        QuerySpec criteria = new QuerySpec(WTOrganization.class);
				        criteria.appendWhere(new SearchCondition(WTOrganization.class, WTOrganization.NAME,
				        		SearchCondition.LIKE, org.getName(),false));
				        QueryResult results = PersistenceHelper.manager.find(criteria);
				        if (results.hasMoreElements()) {
				            flag = true;
				        }
		        	}
				}catch(WTException e){
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return flag;
    }
	
	/**
	 * get organization by name
	 * @param name
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static WTOrganization getWTOrganization(String name) {
        WTOrganization org = null;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTOrganization) RemoteMethodServer.getDefault().invoke("getWTOrganization", 
	                		OrganizationUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] { name });
	           
	        } else {
		        try{
		        	if(!StringUtils.isEmpty(name) && isWTOrganizationExistByName(name)){
				        QuerySpec criteria = new QuerySpec(WTOrganization.class);
				        criteria.appendWhere(new SearchCondition(WTOrganization.class, WTOrganization.NAME, SearchCondition.LIKE, name,
				                false));
				        QueryResult results = PersistenceHelper.manager.find(criteria);
				        if (results.hasMoreElements()) {
				            org = (WTOrganization) results.nextElement();
				        }
		        	}
				}catch(WTException e){
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return org;
    }
	
	/**
	 * get organization container by name
	 * @param name
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static OrgContainer getOrgContainer(String name) throws WTException {
        OrgContainer org = null;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try {
            if (!RemoteMethodServer.ServerFlag) {
                return (OrgContainer) RemoteMethodServer.getDefault().invoke("getOrgContainer",
                        OrganizationUtil.class.getName(), null, new Class[] { String.class },
                        new Object[] { name });
            }else {
                try {
                	if(!StringUtils.isEmpty(name)){
	                    QuerySpec criteria = new QuerySpec(OrgContainer.class);
	                    criteria.appendWhere(new SearchCondition(OrgContainer.class, OrgContainer.NAME,
	                            SearchCondition.EQUAL, name, false));
	                    QueryResult results = PersistenceHelper.manager.find(criteria);
	                    if (results.hasMoreElements()) {
	                        org = (OrgContainer) results.nextElement();
	                    }
                	}
                } catch(Exception e) {
                    logger.error(">>>>>"+e);
                }
            }
        } catch (RemoteException exception) {
            logger.debug(exception.getMessage(),exception);
        } catch (InvocationTargetException exception) {
            logger.debug(exception.getMessage(),exception);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return org;
    }
	
	/**
	 * create organization by name
	 * @param name
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static WTOrganization createWTOrg(String name) {
        WTOrganization org = null;
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (WTOrganization) RemoteMethodServer.getDefault().invoke("createWTOrg", 
	                		OrganizationUtil.class.getName(), null, new Class[] { String.class},
	                		new Object[] { name });
	          
	        } else {
		        if(!StringUtils.isEmpty(name)){
			        org = getWTOrganization(name);
			        if (org == null) {
			        	try{
					            org = WTOrganization.newWTOrganization(name);
					            org.setAdministrator(SessionHelper.manager.getPrincipalReference());
					            org.setConferencingURL(null);
					            org = (WTOrganization) OrganizationServicesHelper.manager.savePrincipal(org);
					            PersistenceHelper.manager.save(org);
			        	}catch(WTException e){
			        		logger.error(">>>>>"+e);
			        	}
			        }
		        }
		        SessionServerHelper.manager.setAccessEnforced(enforce);
	        }
        } catch (RemoteException exception) {
            logger.debug(exception.getMessage(),exception);
        } catch (InvocationTargetException exception) {
            logger.debug(exception.getMessage(),exception);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return org;
    }
	
	/**
	 * update organization by name
	 */
	public static void updateWTOrg(String name,String reName) {
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                RemoteMethodServer.getDefault().invoke("updateWTOrg", 
	                		OrganizationUtil.class.getName(), null, new Class[] { String.class,String.class},
	                		new Object[] { name,reName });
	          
	        } else {
		        if(!StringUtils.isEmpty(name) && isWTOrganizationExistByName(name)){
		        	WTOrganization org = getWTOrganization(name);
			        if (org != null) {
			        	try{
							org.setName(reName);
				            PersistenceHelper.manager.save(org);
			        	}catch(WTException e){
			        		logger.error(">>>>>"+e);
			        	} catch (WTPropertyVetoException e) {
			        		logger.error(">>>>>"+e);
						}
			        }
		        }
	        }
        }catch (RemoteException exception) {
            logger.debug(exception.getMessage(),exception);
        } catch (InvocationTargetException exception) {
            logger.debug(exception.getMessage(),exception);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
    }
	
	public static void test() throws RemoteException, InvocationTargetException, WTException{
		//updateWTOrg("plm-training","org1");
		/*System.out.println(getOrgContainer("org1"));
		System.out.println(getOrgContainer("asdga"));*/
		/*System.out.println(isWTOrganizationExistByName("org1"));
		System.out.println(isWTOrganizationExistByName("plm-training"));
		System.out.println(isWTOrganizationExistByName("asdf"));*/
		
		/*System.out.println(isWTOrganizationExistByOrg(getWTOrganization("org1")));
		System.out.println(isWTOrganizationExistByOrg(getWTOrganization("plm-training")));
		System.out.println(isWTOrganizationExistByOrg(getWTOrganization("asdf")));*/
		
		System.out.println(createWTOrg("org10"));
		System.out.println(createWTOrg("plm-training"));
		
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", OrganizationUtil.class.getName(), null,
					new Class[] {},
					new Object[] {});
		}
	}
	
}
