package ext.huaqin.change.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.OrgContainer;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.OrganizationServicesHelper;
import wt.org.WTOrganization;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;


/**
 * 
 * Organization Util.
 *
 * @version 1.0
 * @author yxxing
 *
 */
public class OrganizationUtil implements RemoteAccess, Serializable{

    private static final long serialVersionUID = -9072376994987199107L;
    private static final Logger logger = Logger.getLogger(OrganizationUtil.class);
    
    /**
     * 
     * Get Organization by organization name.
     * <br> 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2016-8-17, yxxing<br>
     * <b>Comment:</b>
     *
     * @param name : String organization name
     * @return WTOrganization : search organization object
     * @throws WTException : windchill exception
     * 
     *
     */
    @SuppressWarnings("deprecation")
    public static WTOrganization getWTOrganization(String name) throws WTException {
        WTOrganization org = null;
        QuerySpec criteria = new QuerySpec(WTOrganization.class);
        criteria.appendWhere(new SearchCondition(WTOrganization.class, WTOrganization.NAME, SearchCondition.LIKE, name,
                false));
        QueryResult results = PersistenceHelper.manager.find(criteria);
        if (results.hasMoreElements()) {
            org = (WTOrganization) results.nextElement();
        }
        return org;
    }

    /**
     * 
     * Get Organization container by name.
     * <br> 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2016-8-17, yxxing<br>
     * <b>Comment:</b>
     *
     * @param name : String organization name
     * @param accessControlled : boolean true have access controlled, false no access controlled
     * @return OrgContainer : search organization container
     * @throws WTException : windchill exception
     * 
     *
     */
    @SuppressWarnings("deprecation")
    public static OrgContainer getOrgContainer(String name, boolean accessControlled) throws WTException {
        OrgContainer org = null;
        try {
            if (!RemoteMethodServer.ServerFlag) {
                return (OrgContainer) RemoteMethodServer.getDefault().invoke("getOrgContainer",
                        OrganizationUtil.class.getName(), null, new Class[] { String.class, boolean.class },
                        new Object[] { name, accessControlled });
            } else {
                boolean enforce = SessionServerHelper.manager.setAccessEnforced(accessControlled);
                try {
                    QuerySpec criteria = new QuerySpec(OrgContainer.class);
                    criteria.appendWhere(new SearchCondition(OrgContainer.class, OrgContainer.NAME,
                            SearchCondition.EQUAL, name, false));
                    QueryResult results = PersistenceHelper.manager.find(criteria);
                    if (results.hasMoreElements()) {
                        org = (OrgContainer) results.nextElement();
                    }
                } finally {
                    SessionServerHelper.manager.setAccessEnforced(enforce);
                }
            }
        } catch (RemoteException exception) {
            logger.debug(exception.getMessage(),exception);
        } catch (InvocationTargetException exception) {
            logger.debug(exception.getMessage(),exception);
        }
        return org;
    }
    
    /**
     * 
     * Get Organization container by name (if main method have Remote).
     * <br> 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2016-8-17, yxxing<br>
     * <b>Comment:</b>
     *
     * @param name : String organization name
     * @return OrgContainer : search organization container
     *
     */
    @SuppressWarnings("deprecation")
    public static OrgContainer getOrgContainer(String name) throws WTException{
        OrgContainer org = null;
        QuerySpec criteria = new QuerySpec(OrgContainer.class);
        criteria.appendWhere(new SearchCondition(OrgContainer.class, OrgContainer.NAME, SearchCondition.EQUAL, name,
                false));
        QueryResult results = PersistenceHelper.manager.find(criteria);

        if (results.hasMoreElements()) {
            org = (OrgContainer) results.nextElement();
        }
        return org;
    } 
    
    /**
     * 
     * create organization.
     * <br> 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2016-8-17, yxxing<br>
     * <b>Comment:</b>
     *
     * @param name : String organization name
     * @return WTOrganization : if specific name organization exist ,return search organization; else return create organization
     * @throws WTException : windchill exception
     * 
     *
     */
    @SuppressWarnings("deprecation")
    public static WTOrganization createWTOrg(String name) throws WTException {
        WTOrganization org = null;
        org = getWTOrganization(name);
        if (org == null) {
            org = WTOrganization.newWTOrganization(name);
            org.setAdministrator(SessionHelper.manager.getPrincipalReference());
            org.setConferencingURL(null);
            org = (WTOrganization) OrganizationServicesHelper.manager.savePrincipal(org);
            // PersistenceHelper.manager.save(org);
        }
        return org;
    }
}
