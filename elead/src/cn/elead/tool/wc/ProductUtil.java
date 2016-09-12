package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.container.WTContainerTemplateRef;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.template.ContainerTemplateHelper;
import wt.log4j.LogR;
import wt.maturity.PromotionNotice;
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
					boolean accessEnforced = SessionServerHelper.manager
							.setAccessEnforced(false);
					WTPrincipal currentPrincipal = SessionHelper.manager.getPrincipal();
					try {
						
						WTOrganization wtOrg = OrganizationUtil.getWTOrganization(orgName);
						WTContainerRef orgContainerRef = WTContainerHelper.service
								.getOrgContainerRef(wtOrg);
			
						List orgAdmins = UserUtil.findOrgAdmins(wtOrg);
						if (orgAdmins == null || orgAdmins.size() == 0) {
							SessionHelper.manager.setAdministrator();
						} else {
							WTUser admin = (WTUser) orgAdmins.get(0);
							SessionHelper.manager.setAuthenticatedPrincipal(admin
									.getAuthenticationName());
						}
			
						WTContainerTemplateRef containerTemplateRef = ContainerTemplateHelper.service
								.getContainerTemplateRef(orgContainerRef,
										containerTemplateName, PDMLinkProduct.class);
						if (containerTemplateRef == null) {
							LOGGER.error(" >>>>>>>> createProductContainer()  <<<<<< : could not find the product template["
									+ containerTemplateName + "]!");
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
			
						//initProductManager(prod, user);
					} finally {
						SessionHelper.manager
								.setAuthenticatedPrincipal(((WTUser) currentPrincipal)
										.getAuthenticationName());
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					}
            }
        } catch (Exception e) {   
            e.printStackTrace();   
        }   
	}
	public  static void initProductManager(PDMLinkProduct prod,
			WTPrincipal prodCreator) throws WTException {
		try {   
            if (!RemoteMethodServer.ServerFlag) {   
                 RemoteMethodServer.getDefault().invoke("createPromotionRequest", UserUtil.class.getName(), null,   
                        new Class[] {PDMLinkProduct.class,WTPrincipal.class},   
                        new Object[] {prod,prodCreator});   
            } else {   
				boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				ContainerTeam containerTeam = ContainerTeamHelper.service
						.getContainerTeam(prod);
				System.out.println("@@@@@firstTime"+containerTeam);
				Role pm = Role.toRole("PRODUCT MANAGER");
		        System.out.println("@@@@SeTime"+pm);
				ContainerTeamHelper.service.addMember(containerTeam, pm, prodCreator);
		
				Enumeration<?> enumeration = containerTeam.getPrincipalTarget(pm);
				System.out.println("@@@@ThreeTime"+enumeration);
				WTPrincipal principal;
				while (enumeration.hasMoreElements()) {
					principal = ((WTPrincipalReference) enumeration.nextElement())
							.getPrincipal();
					System.out.println("@@@@FrTime"+principal);
					if (!principal.equals(prodCreator))
						ContainerTeamHelper.service.removeMember(containerTeam, pm,principal);
				}
                SessionServerHelper.manager.setAccessEnforced(accessEnforced);   
           }

		} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}

	public static boolean isPDMLinkProductExist(String name){
		 try{
		        if (!RemoteMethodServer.ServerFlag) {
		                return (boolean) RemoteMethodServer.getDefault().invoke("isPDMLinkProductExist", 
		                		ProductUtil.class.getName(), null, new Class[] { String.class},
		                		new Object[] { name });
		        } else {
		        	WTOrganization org = null;
		        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					PDMLinkProduct pdmLinkProduct = null;
					if(!StringUtils.isEmpty(name)){
						pdmLinkProduct =getPDMLinkProductByName(name, false);
					}
					if(pdmLinkProduct == null){
						return false;
					}else{
					    return true;
					}
		        }
	        } catch (RemoteException e) {
	            LOGGER.error(e.getMessage(),e);
	        } catch (InvocationTargetException e) {
	        	LOGGER.error(e.getMessage(),e);
	        }
	        return false;
	}
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
									// TODO Auto-generated catch block
									e.printStackTrace();
								}  finally {   
				                    SessionServerHelper.manager.setAccessEnforced(enforce);   
				                }  
								return pdm;   
		                  }
		            
				} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			 
		        return null;
        }
	
			  

	
	public static <T> void test() throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException
	{   
		 /*PDMLinkProduct prod = PDMLinkProduct.newPDMLinkProduct();
	// ObjectReference ObjectReference = wt.fc.ObjectReference.newObjectReference(DocUtil.getDocumentByNumber("1001").getMaster());
	 @SuppressWarnings("deprecation")
	ObjectReference ObjectReference1 = wt.fc.ObjectReference.newObjectReference();
	 try {
		prod.setProductReference(ObjectReference1);
		System.out.println("&&&&&&&&&&&"+prod);
	} catch (WTPropertyVetoException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 WTPrincipal prodCreator = SessionHelper.manager.getPrincipal();
	 System.out.println("&&&&&&&&&&&"+prodCreator);
	 initProductManager(prod, prodCreator);*/
	 WTPrincipal prodCreator = SessionHelper.manager.getPrincipal();
	  try {
		createProductContainer("bjj3", "产品模板", "huaqin", "firstTimeCreate", prodCreator);
		System.out.println("_________________________");
		/*createProductContainer("bjj3", "heihei", "huaqin", "firstTimeCreate", prodCreator);
		System.out.println("###############################");
		WTPrincipal prodCreator1 = SessionHelper.manager.setPrincipal("prod");
		System.out.println(prodCreator1);
		createProductContainer("bjj4", "Sourced Product", "huaqin", "firstTimeCreate", prodCreator1);	*/		
		
		
	  	} catch (WTPropertyVetoException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
	 /*  System.out.println("___________"+getPDMLinkProductByName("测试产品",false));
	   System.out.println("------"+isPDMLinkProductExist("bjj1"));
	   //************************ Test of getPDMLinkProduct ********************************   
       PDMLinkProduct product = ProductUtil.getPDMLinkProductByName("GOLF_CART",  false);   
       System.out.println("Find Product = " + product.getContainerName());   
*/
	  }
	
	  public static void main(String[] args)throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException
	  {
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
