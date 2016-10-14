package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.container.WTContainerTemplateRef;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.template.ContainerTemplateHelper;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.pdmlink.PDMLinkProduct;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class ProductUtil implements RemoteAccess {
	
	private static final long serialVersionUID = 1L;
	private final static Logger LOGGER = LogR.getLogger(ProductUtil.class.getName());
	private static String CLASSNAME = ProductUtil.class.getName();
	
	 /**
	 * 
	 * @param name
	 *            Product name
	 * @param containerTemplateName
	 *            container Template Name
	 * @param orgName
	 *            org Name
	 * @param Desc
	 *            describe
	 * @return user (product manager)
	 * @Exception
	 */
	@SuppressWarnings("rawtypes")
	public static void createProductContainer(String name,String containerTemplateName, String orgName, String Desc,
			WTPrincipal user) throws WTException, WTPropertyVetoException {
		try {   
            if (!RemoteMethodServer.ServerFlag) {   
                 RemoteMethodServer.getDefault().invoke("createPromotionRequest", UserUtil.class.getName(), null,   
                        new Class[] {String.class,String.class,String.class,String.class,WTPrincipal.class},   
                        new Object[] {name,containerTemplateName,orgName,Desc,user});   
            } else {   
					boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
					WTPrincipal currentPrincipal = SessionHelper.manager.getPrincipal();
					try {
						WTOrganization wtOrg = OrganizationUtil.getWTOrganization(orgName);
						WTContainerRef orgContainerRef = WTContainerHelper.service.getOrgContainerRef(wtOrg);
						List orgAdmins = UserUtil.findOrgAdmins(wtOrg);
						if (orgAdmins == null || orgAdmins.size() == 0) {
							SessionHelper.manager.setAdministrator();
						} else {
							WTUser admin = (WTUser) orgAdmins.get(0);
							SessionHelper.manager.setAuthenticatedPrincipal(admin.getAuthenticationName());
						}
						WTContainerTemplateRef containerTemplateRef = ContainerTemplateHelper.service
								.getContainerTemplateRef(orgContainerRef, containerTemplateName, PDMLinkProduct.class);
						if (containerTemplateRef == null) {
							LOGGER.error(" >>>>>>>> createProductContainer()  <<<<<< : could not find the product template["+ containerTemplateName + "]!");
							throw new WTException(
									" >>>>>>>> createProductContainer()  <<<<<< : could not find the product template["
											+ containerTemplateName + "]!");
						}
						PDMLinkProduct prod = PDMLinkProduct.newPDMLinkProduct();
						prod.setName(name);
						prod.setDescription(Desc);
						prod.setContainerReference(orgContainerRef);
						prod.setContainerTemplateReference(containerTemplateRef);
						prod.setCreator(user);
						prod.setOwner(user);
						prod = (PDMLinkProduct) WTContainerHelper.service.create(prod);
			
						initProductManager(prod, user);
					} finally {
						SessionHelper.manager.setAuthenticatedPrincipal(((WTUser) currentPrincipal).getAuthenticationName());
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					}
            }
        } catch (Exception e) {   
        	LOGGER.error(e.getMessage(),e); 
        }   
	}
	
	/**PRODUCT MANAGER"要写成参数
	 * 此方法
	 * <一句话功能简述>
	 * <功能详细描述>
	 * @author  zhangxj
	 * @see [类、类#方法、类#成员]
	 */
	public  static void initProductManager(PDMLinkProduct prod, WTPrincipal prodCreator) throws WTException {
		try {   
            if (!RemoteMethodServer.ServerFlag) {   
                 RemoteMethodServer.getDefault().invoke("createPromotionRequest", UserUtil.class.getName(), null,   
                        new Class[] {PDMLinkProduct.class,WTPrincipal.class},   
                        new Object[] {prod,prodCreator});   
            } else {   
				boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam(prod);
				Role pm = Role.toRole("PRODUCT MANAGER");
				try {
				
				ContainerTeamHelper.service.addMember(containerTeam, pm, prodCreator);
		
				Enumeration<?> enumeration = containerTeam.getPrincipalTarget(pm);
				WTPrincipal principal;
				while (enumeration.hasMoreElements()) {
					principal = ((WTPrincipalReference) enumeration.nextElement()).getPrincipal();
					System.out.println("@@@@FrTime"+principal);
					if (!principal.equals(prodCreator)){
						ContainerTeamHelper.service.removeMember(containerTeam, pm,principal);
					}
				}
				} catch (WTException e) {
					LOGGER.error(CLASSNAME+".initProductManager:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
           }
		} catch (Exception e) {
        	LOGGER.error(e.getMessage(),e); 
		}
	}
	
	public static Boolean isPDMLinkProductExist(String name){
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (Boolean) RemoteMethodServer.getDefault().invoke("isPDMLinkProductExist",
	            		ProductUtil.class.getName(), null, new Class[] { String.class},
	            		new Object[] { name });
		    } else {
		    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				PDMLinkProduct pdmLinkProduct = null;
				try {
					if(!StringUtils.isEmpty(name)){
						pdmLinkProduct =getPDMLinkProductByName(name, false);
					}
					if(pdmLinkProduct == null){
						return false;
					}else{
					    return true;
					}
				} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
				}
		    }
		} catch (RemoteException e) {
		    LOGGER.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getMessage(),e);
		}
		return false;
	}
	
	/**
	 * 参数的为空判断没有进行 第二个参数是否可以去掉
	 * <一句话功能简述>
	 * <功能详细描述>
	 * @author  zhangxj
	 * @see [类、类#方法、类#成员]
	 */
	@SuppressWarnings("deprecation")
	public static PDMLinkProduct getPDMLinkProductByName(String name,boolean accessControlled){
		try {   
	        if (!RemoteMethodServer.ServerFlag) {   
	            return (PDMLinkProduct) RemoteMethodServer.getDefault().invoke("getPDMLinkProduct", PDMLinkProduct.class.getName(), null,   
	                    new Class[] {String.class, boolean.class},   
	                    new Object[] {name, accessControlled});   
	        } else {   
	            	boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(accessControlled);   
		            PDMLinkProduct pdm = null;
					try {
						QuerySpec qs = new QuerySpec(PDMLinkProduct.class);
						name.toUpperCase();
						WhereExpression searchCondition = new SearchCondition(PDMLinkProduct.class,PDMLinkProduct.NAME,
								SearchCondition.LIKE,PublicUtil.queryLikeValueFormat(name),false);
						qs.appendWhere(searchCondition);
						QueryResult qr;
						qr = PersistenceHelper.manager.find(qs);
								while(qr.hasMoreElements()){
										PDMLinkProduct pdmlp = (PDMLinkProduct) qr.nextElement();
										return pdmlp;
								}
						} catch (WTException e) {
							LOGGER.error(CLASSNAME+".getPDMLinkProductByName:" + e);
						}  finally {   
		                    SessionServerHelper.manager.setAccessEnforced(enforce);   
		                }  
						return pdm;   
	              }
		} catch (Exception e) {
		    LOGGER.error(e.getMessage(),e);
	    }
        return null;
	}
	
			  

	
	public static <T> void test() throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException {   
		 //createProductContainer();各个参数是否为空的判断
		System.out.println("111");
		System.out.println(isPDMLinkProductExist("A1511"));
		 PDMLinkProduct pd=getPDMLinkProductByName("A1511",false);
		 System.out.println(pd);
	}
	
	public static void main(String[] args)throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException {
	    if (!RemoteMethodServer.ServerFlag)
	      try {
	    	    RemoteMethodServer server = RemoteMethodServer.getDefault();
				server.setUserName("wcadmin");
				server.setPassword("wcadmin");
				RemoteMethodServer.getDefault().invoke("test", ProductUtil.class.getName(), null, new Class[0], new Object[0]);
	      }
	      catch (java.rmi.RemoteException e)
	      {
	        e.printStackTrace();
	      }
	  }
		
}
