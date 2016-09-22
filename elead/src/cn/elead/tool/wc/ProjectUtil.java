package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.drools.core.util.StringUtils;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.container.WTContainerTemplateRef;
import wt.inf.template.ContainerTemplateHelper;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.projmgmt.admin.Project2;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
	
	
	@SuppressWarnings("deprecation")
	public class ProjectUtil implements RemoteAccess{
		private static  String  CLASSNAME = ProjectUtil.class.getName();			
		private  static Logger logger= LogR.getLogger(CLASSNAME);
		/**
		 * 判断项目是否存在
		 * @param name
		 * @return
		 */
		public static  boolean  isExistProject(String name ){
			 try {
				if (!RemoteMethodServer.ServerFlag) {
				    	return (boolean)RemoteMethodServer.getDefault().invoke("isExistProject", ProjectUtil.class.getName(), null,
				    			new Class[] { String.class }, new Object[] { name });
				} else{
					boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					boolean falg = false ;
					if(!StringUtils.isEmpty(name)){
						try {
							QuerySpec querySpec = new QuerySpec(Project2.class);
							querySpec.appendWhere(new SearchCondition(Project2.class,Project2.NAME,SearchCondition.EQUAL,name));
							QueryResult queryResult=PersistenceHelper.manager.find(querySpec);
							if(queryResult.hasMoreElements()){
								falg = true;
							}
						} catch (QueryException e) {
							logger.error(CLASSNAME+".isExistProject:"+e);
						} catch (WTException e) {
							logger.error(CLASSNAME+".isExistProject:"+e);
						}finally{
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}
						return falg;
					}
					}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return false;
		}
		
		/**
		 * 通过名称获取项目
		 * @param name
		 * @return
		 */
		 public static Project2 getProjectByName(String name){
			 try {
				if(!RemoteMethodServer.ServerFlag){
					 return (Project2) RemoteMethodServer.getDefault().invoke("getProjectByName", ProjectUtil.class.getName(), null,  new Class[] {String.class}, new Object [] {name});
				 }else {
					 boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					 Project2 project = null ;
					 if(!StringUtil.isEmpty(name)&& isExistProject(name)){
						 try {
							 if(!StringUtil.isEmpty(name)){
								 QuerySpec querySpec= new QuerySpec(Project2.class);
								 querySpec.appendWhere (new SearchCondition(Project2.class,Project2.NAME,SearchCondition.LIKE,name,false));
								 QueryResult results = PersistenceHelper.manager.find(querySpec);
								 if(results.hasMoreElements()){
									 project = (Project2) results.nextElement();
								 }
								 return project;
							 }
						 } catch (QueryException e) {
							 logger.error(CLASSNAME+".getProjectByNmae:"+e);
						 } catch (WTException e) {
							 logger.error(CLASSNAME+".getProjectByNmae:"+e);
						 }finally{
							 SessionServerHelper.manager.setAccessEnforced(enforce);
						 }
					 }
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
			}
			return null;
		 }
		 
		 public static void createProject(String name, String containerTemplateName, String orgName, String Desc,WTPrincipal user){
			     boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
			 try {
				WTOrganization wtOrg = OrganizationUtil.getWTOrganization(orgName);
					WTContainerRef orgContainerRef = WTContainerHelper.service.getOrgContainerRef(wtOrg);
					WTContainerTemplateRef containerTemplateRef = ContainerTemplateHelper.service.getContainerTemplateRef(orgContainerRef,containerTemplateName, 
							Project2.class);
					Project2 project2= Project2.newProject2();
					project2.setName(name);
					project2.setDescription(Desc);	
					project2.setContainerReference(orgContainerRef);
					project2.setContainerTemplateReference(containerTemplateRef);
					project2.setCreator(SessionHelper.manager.getPrincipal());
					project2.setOwner(SessionHelper.manager.getPrincipal());
					System.out.println("orgContainerRef>>"+orgContainerRef+"containerTemplateRef>>>"+containerTemplateRef+"SessionHelper.manager.getPrincipal()>"+SessionHelper.manager.getPrincipal());
				WTContainerHelper.service.create(project2);
			} catch (WTPropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
		 }
		 
		public static void test() throws WTException{
//			System.out.println("isExistProject>>>"+isExistProject("测试项目"));
//			System.out.println(getProjectByName("测试项目"));
		Project2 project2 = getProjectByName("测试项目01");
//			project2.getContainer().getContainerTemplate();
//			System.out.println("project2.getContainer().getContainerTemplate()>>>"+project2.getContainer().getContainerTemplate());
//			project2.getDisplayIdentifier();
//			project2.getDisplayIdentity();
			 WTPrincipal prodCreator = SessionHelper.manager.getPrincipal();
//			System.out.println("project2.getDisplayIdentifier()>>>"+project2.getDisplayIdentifier());
//			System.out.println("project2.getDisplayIdentity()>>>"+project2.getDisplayIdentity());
		createProject("项目测试111","Custom","huaqin","1111111",prodCreator);
		}
		
		public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
			RemoteMethodServer r = RemoteMethodServer.getDefault();
			r.setUserName("orgadmin");
			r.setPassword("123");
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("test", ProjectUtil.class.getName(), null, new Class[] {}, new Object[] {});
			}
		}
	}
