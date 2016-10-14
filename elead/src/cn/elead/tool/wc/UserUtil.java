package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.inf.container.OrgContainer;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerServerHelper;
import wt.inf.library.WTLibrary;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.inf.team.StandardContainerTeamService;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.team.WfRoleHelper;
import wt.util.WTException;
import wt.util.WTRuntimeException;
import wt.workflow.engine.WfProcess;

import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.netmarkets.role.NmRoleHelper;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.picker.principal.PrincipalBean;

@SuppressWarnings("unchecked")
public class UserUtil implements RemoteAccess {
	
	private static String CLASSNAME = UserUtil.class.getName();
	private final static Logger logger = LogR.getLogger(CLASSNAME);
	public static final long serialVersionUID = 592924258829951579L;
	public static final String role = "供应商";
	public static final String SEP_MULTIUSERNAME = ";";
	public static final String IRIS_SEP_MULTIUSERNAME = ",";
	public static String DESCRIPTION = "DESCRIPTION"; // discription 
	public static String TYPE = "TYPE"; 
	public static String FOLDER = "FOLDER"; // create folder
	
	public static Map<WTContainer, Boolean> containerMap = new HashMap<>();
	public static final Role roleMember = Role.toRole("MEMBERS"); 
	public static final WTPrincipal principal = null;
	public static final WTPrincipalReference principalReference = null;
	private static final Logger LOGGER = LogR.getLogger(UserUtil.class.getName());
 
	/**
	 * @Author: bjj
	 * @Date: 2016/9/14 pm
	 * @Description: Get Current User .
	 * @return
	 * @throws WTException
	 */
	
	public static WTPrincipal getCurrentUser() throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTPrincipal) RemoteMethodServer.getDefault()
						.invoke("getCurrentUser", UserUtil.class.getName(), null,new Class[] { }, new Object[] { });
			} else {
				WTPrincipal principal = null;
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);	
				try {
					principal = SessionHelper.manager.getPrincipal();
					} catch (Exception e) {
						logger.error(CLASSNAME+".getCurrentUser:" + e);
					} finally {
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
				return principal;
			}
		} catch (RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @author BaiJuanjuan
	 * get Current User Full Name.
	 * @throws WTExcetion
	 */
	public static String getCurrentUserFullName() throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke("getCurrentUserFullName",
						UserUtil.class.getName(), null,new Class[] { }, new Object[] { });
			} else {
				String rtename = null;
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);	
				try {
					WTPrincipal reference = getCurrentUser();
					rtename = reference.getName();
				} catch (WTException e) {
					logger.error(CLASSNAME+".getCurrentUserFullName:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return rtename;
			}
		} catch (RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @author BaiJuanjuan
	 * get WTUser By Name
	 * @param userName
	 * @throws WTExcetion
	 */
	@SuppressWarnings("deprecation")
	public static WTPrincipal getWTUserByName(String userName) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
							return (WTPrincipal) RemoteMethodServer.getDefault()
									.invoke("getWTUserByName", UserUtil.class.getName(), null,new Class[] {String.class}, new Object[] {userName});
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				WTUser ss = null;
				try{
				    if (!StringUtils.isEmpty(userName)) {
				    	ss = OrganizationServicesHelper.manager.getUser(userName);		
				    }
				} catch (WTException e) {
					logger.error(CLASSNAME+".getWTUserByName:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			    return ss;
			}
		} catch (RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	    return null;
	}
	
	/**
	 * @author BaiJuanjuan
	 * find User
	 * @param name
	 * @throws WTExcetion
	 */
	public List<WTUser> findUser(String name) throws WTException {
	    try {
			if (!RemoteMethodServer.ServerFlag) {
			    	return (List<WTUser>)RemoteMethodServer.getDefault().invoke( "findUser", UserUtil.class.getName(), null, 
			          new Class[] { String.class }, new Object[]{name});
			} else {
				    boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				    WTUser currentUser = null;
				    List<WTUser>  results = new ArrayList<WTUser>();
				    try {
				      currentUser = (WTUser)SessionHelper.manager.getPrincipal();
				      SessionHelper.manager.setAdministrator();
				      DirectoryContextProvider dcp = OrganizationServicesHelper.manager.newDirectoryContextProvider((String[])null,(String[])null);
				      Enumeration<?> enmer = OrganizationServicesHelper.manager.findLikeUsers("name", name, dcp);
					      while (enmer.hasMoreElements()) {
					        WTUser user = (WTUser)enmer.nextElement();
					        if ((!user.isDisabled()) && (!results.contains(user)))
					          results.add(user);
					      }
					  } finally {
					      if (currentUser != null) {
					        SessionHelper.manager.setPrincipal(currentUser.getName());
					      }
					      SessionServerHelper.manager.setAccessEnforced(enforce);
					    }
			         return results;
			    }
		} catch (RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	    return null;
	}
	
	/**
	 * @author BaiJuanjuan
	 * get Members Of Container Role
	 * @param ctm
	 * @param role
	 */
	public static List<WTPrincipal> getMembersOfContainerRole(ContainerTeamManaged ctm, Role role) {
		try{
	        if (!RemoteMethodServer.ServerFlag) {
	            return (List<WTPrincipal>) RemoteMethodServer.getDefault().invoke("getMembersOfContainerRole", 
	    		        UserUtil.class.getName(), null, new Class[] { ContainerTeamManaged.class,Role.class}, new Object[] { ctm, role});
		    } else {
			    List<WTPrincipal> rtn = new ArrayList<WTPrincipal>();
			    boolean accessFlag = SessionServerHelper.manager.setAccessEnforced(false);
				if (ctm != null) {
		           try {
		    		  ContainerTeam containerteam = ContainerTeamHelper.service.getContainerTeam(ctm);
		    		  WTGroup wtgroup = ContainerTeamHelper.service.findContainerTeamGroup(containerteam, "roleGroups", role.toString());
	    		      if (wtgroup != null) {
	    			        Enumeration<?> members = wtgroup.members();
	    			        while (members.hasMoreElements()) {
		    			          WTPrincipal wtp = (WTPrincipal)members.nextElement();
		    			          if ((wtp instanceof WTUser)) {
			    			            WTUser user = (WTUser)wtp;
			    			            rtn.add(user);
	    			              }
	    			        }
	    			    }
				    } catch (WTException e) {
						logger.error(CLASSNAME+".getMembersOfContainerRole:" + e);
				    } finally {
				        SessionServerHelper.manager.setAccessEnforced(accessFlag);
				    }
				}
				return   rtn;
			}
		} catch (RemoteException e) {
            LOGGER.error(e.getMessage(),e);
	    } catch (InvocationTargetException e) {
	    	LOGGER.error(e.getMessage(),e);
	    }
        return null;
	}
	
	/**
	 * @author BaiJuanjuan
	 * add user to role.
	 * eg: Add wcadmin to LPDT.
	 * @param wtContainer
	 * @param role
	 * @param wtPrincipal
	 */				
	public static void addUserTORole(WTContainer wtContainer, Role role, WTPrincipal wtPrincipal) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					  RemoteMethodServer.getDefault().invoke("addUserTORole", UserUtil.class.getName(), null,
								new Class[] {WTContainer.class,Role.class,WTPrincipal.class}, new Object[] {wtContainer,role,wtPrincipal });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
			    try{
			        ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) wtContainer);
			        ContainerTeamHelper.service.addMember(containerTeam, role, wtPrincipal);
			    } catch (WTException e){
					logger.error(CLASSNAME+".addUserTORole:" + e);
			    }finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
		} catch (RemoteException | InvocationTargetException e) {
	    	LOGGER.error(e.getMessage(),e);
		}
	}
	
	/**
	 * @author BaiJuanjuan
	 * add Principal
	 * @param cb
	 * @return
	 * @throws WTException
	 */	
	@SuppressWarnings("deprecation")
	public static FormResult addPrincipal(NmCommandBean cb) throws WTException {
		try {   
	        if (!RemoteMethodServer.ServerFlag) {   
	            return (FormResult) RemoteMethodServer.getDefault().invoke("addPrincipal", UserUtil.class.getName(), null,   
	                new Class[] {String.class}, new Object[] {cb});   
	    } else {   
	        boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
	        FormResult result = new FormResult(FormProcessingStatus.SUCCESS);
	        try {
		        result.setNextAction(FormResultAction.REFRESH_OPENER);
		        String principals = cb.getTextParameter(PrincipalBean.PARAM_SELECTED_PRINCIPALS);
		        if (principals == null) {
		           result.setStatus(FormProcessingStatus.FAILURE);
		           return result;
		        }
		        ArrayList<String> selectedPrincipals = new ArrayList<String>();
		        int start = 0;
		        int pos = principals.indexOf("#", start);
		        while (pos != -1) {
		           String principal = principals.substring(start, pos);
		           selectedPrincipals.add(principal);
		           start = pos + 1;
		           pos = principals.indexOf("#", start);
		        }

		        String role = cb.getTextParameter(PrincipalBean.PARAM_ASSOCIATION);
		        ArrayList<?> result1 = NmRoleHelper.service.addUsersToRole(cb, role, selectedPrincipals);
		    	
			} catch (Exception e) {
				logger.error(CLASSNAME+".addPrincipal:" + e);
			} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
		    return result;
	      }
	    } catch (Exception e) {   
	    	LOGGER.error(e.getMessage(),e);
	    }   
	    return null;  
	}
	
	/**
	 * @author BaiJuanjuan
	 * Whether or not org administrators.
	 * @param user
	 * @return boolean
	 */
	public static Boolean isOrgAdmin(WTUser user){
		try {   
	        if (!RemoteMethodServer.ServerFlag) {   
	            return (Boolean) RemoteMethodServer.getDefault().invoke("isOrgAdmin", UserUtil.class.getName(), null,
	                    new Class[] {WTUser.class},   
	                    new Object[] {user});   
	        } else {   
	            boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
			    try {
			      WTOrganization org = user.getOrganization();
			      if (org == null){
			        return false;
			      }
			      OrgContainer orgContainer = WCUtil.searchOrgContainer(org.getName());
			      if (orgContainer == null){
			        return false;
			      }
			      return WTContainerServerHelper.getAdministratorsReadOnly(orgContainer).isMember(user);
			    } catch (WTException e) {
					logger.error(CLASSNAME+".isOrgAdmin:" + e);
			    } finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			    return true;
	        }
		} catch (Exception e) {   
	    	LOGGER.error(e.getMessage(),e);
		}   
		return false;   
	}
	
	/**
	 * @author BaiJuanjuan
	 * GET all user in system group.
	 * @param group
	 * @return ArrayList<WTUser>
	 */
	public static ArrayList<WTUser> getGroupMembersOfUser(WTGroup group) throws WTException {
		
		try {   
	        if (!RemoteMethodServer.ServerFlag) {   
	            return (ArrayList<WTUser>) RemoteMethodServer.getDefault().invoke("getGroupMembersOfUser", UserUtil.class.getName(), null,   
	                    new Class[] {WTGroup.class}, new Object[] {group});   
	        } else {   
	            boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
		        ArrayList<WTUser> memebers = new ArrayList<WTUser>();
	            try {
			        Enumeration<?> member = group.members();
			        while (member.hasMoreElements()) {
			            WTPrincipal principal = (WTPrincipal) member.nextElement();
			            if (principal instanceof WTUser)
			                memebers.add((WTUser) principal);
			            else if (principal instanceof WTGroup)
			                memebers.addAll(getGroupMembersOfUser((WTGroup) principal));
			        }
		    	} catch (WTException e) {
					logger.error(CLASSNAME+".getGroupMembersOfUser:" + e);
			    } finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
		        return memebers;
	        }   
	    } catch (Exception e) {   
	    	LOGGER.error(e.getMessage(),e); 
	    }   
	    return null; 
	}
	
	/**
	 *  @author BaiJuanjuan
	 *  The fuzzy query system users
	 *  @param userName
	 *  @return ArrayList<WTUser>
	 */
	public static ArrayList<WTUser> fuzzySearchUserByName(String userName) throws WTException {
		try {   
	        if (!RemoteMethodServer.ServerFlag) {   
	            return (ArrayList<WTUser>) RemoteMethodServer.getDefault().invoke("fuzzySearchUserByName", UserUtil.class.getName(), null,   
	                    new Class[] {String.class}, new Object[] {userName});   
	        } else {   
	               
	            boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
		        ArrayList<WTUser> list = new ArrayList<WTUser>();
		        
		        QuerySpec qs = new QuerySpec(WTUser.class);
		        int[] indx0 = new int[]{0};
	            try {
				
			        SearchCondition sc = new SearchCondition(WTUser.class, WTUser.NAME,SearchCondition.LIKE,PublicUtil.queryLikeValueFormat(userName),false);
			        qs.appendWhere(sc, indx0);
			        qs.appendAnd();
			        qs.appendWhere(new SearchCondition(WTUser.class, WTUser.DISABLED, SearchCondition.IS_FALSE), indx0);
		
			        QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
			        while (qr.hasMoreElements()) {
			            WTUser user = (WTUser) qr.nextElement();
			            if (!"Administrator".equals(user.getName()))
			                list.add(user);
			        }
			    	
				} catch (WTException e) {
					logger.error(CLASSNAME+".fuzzySearchUserByName:" + e);
			    } finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
		        return list;
	        }   
	    } catch (Exception e) {   
	    	LOGGER.error(e.getMessage(),e); 
	    }   
	    return null; 
	}
	
	/**
	 * 
	 * @author BaiJuanjuan
	 * @param role
	 * @param principal
	 * @param self
	 */
	public static void setProcessRoleHolder(Role role, WTPrincipal principal, ObjectReference self) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
			    	RemoteMethodServer.getDefault().invoke("addUserToContainer", UserUtil.class.getName(), null, 
			    		new Class[] {Role.class, WTPrincipal.class, ObjectReference.class}, new Object[] { role,principal,self });
			} else{ 
			    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				WfProcess wfProcess = (WfProcess) self.getObject();
				try {
					Team team = TeamHelper.service.getTeam(wfProcess);
					TeamHelper.service.addRolePrincipalMap(role, principal, team);
				} catch (WTException e) {
					logger.error(CLASSNAME+".setProcessRoleHolder:" + e);
			    } finally {
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
			 }
		} catch (RemoteException | InvocationTargetException | WTRuntimeException e) {
	    	LOGGER.error(e.getMessage(),e); 
		}
	}
	
	/**
	 * @author BaiJuanjuan
	 * @Description:add user to container team.
	 * @param map
	 * @param contained
	 * @throws WTException
	 */
	public static void addUserToContainer(WTContained contained,String role) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
		    	RemoteMethodServer.getDefault().invoke("addUserToContainer", UserUtil.class.getName(), null, 
		    			new Class[] { WTContained.class }, new Object[] { contained });
			} else{ 
			    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
			    
				if (!(contained instanceof WTContained)){
					throw new WTException("The argument is not the type of WTContained");
				}
				WTContainer container = contained.getContainer();
				try {
					if(!containerMap.containsKey(container)) {
						ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged)container);
						ArrayList<?> listPrincipals = containerTeam.getAllPrincipalsForTarget(Role.toRole(role));
						boolean flag = false;
						if(!listPrincipals.contains(principalReference)) {
							containerTeam.addPrincipal(roleMember, principal);
							flag = true;
						}
						containerMap.put(container, flag);
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+".addUserToContainer:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
			}
		} catch (RemoteException | InvocationTargetException e) {
	    	LOGGER.error(e.getMessage(),e); 
		}
	}
	    
	            
	/**
	 * @author BaiJuanjuan
	 * findOrgAdmins 
	 * @param org
	 * @param
	 * @return
	 * @throws WTException
	 */
	public static List<WTPrincipal> findOrgAdmins(WTOrganization org) throws WTException {

		try {   
	        if (!RemoteMethodServer.ServerFlag) {   
	            return (List<WTPrincipal>) RemoteMethodServer.getDefault().invoke("findOrgAdmins", UserUtil.class.getName(), null,   
	                    new Class[] {WTOrganization.class}, new Object[] {org});   
	        } else {  
				boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				List<WTPrincipal> admins = new ArrayList<WTPrincipal>();
				try {
					if (org == null) {
						org = SessionHelper.getPrincipal().getOrganization();
					}
					OrgContainer orgCont = WTContainerHelper.service
							.getOrgContainer(org);
					if (orgCont != null) {
						WTGroup grp = orgCont.getAdministrators();
						for (Enumeration<?> en = grp.members(); en.hasMoreElements();) {
							WTPrincipal principal = (WTPrincipal) en.nextElement();
							if (principal instanceof WTUser) { 
								admins.add(principal);
							}
						}
					}
				} finally {
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
				return admins;
	        }
	     }catch (Exception e) {   
	    	 LOGGER.error(e.getMessage(),e); 
		 }  
		 return null;
	}
	
	/**
	 * @author baijj
	 * 20160908_pm
	 *               begin
	 *               get a list of Container object team roles  
	 * @param teammanaged  
	 *               The Container which contains team.  
	 * @return  Container    
	 *               team role list
	 */   
	public static ArrayList<Role> getContainerTeamRoles(ContainerTeamManaged teammanaged){   
	    try {   
	        if (!RemoteMethodServer.ServerFlag) {   
	            return (ArrayList<Role>) RemoteMethodServer.getDefault().invoke("getContainerTeamRoles", UserUtil.class.getName(), null,   
	                new Class[] {ContainerTeamManaged.class}, new Object[] { teammanaged });   
		    } else {   
		        ArrayList<Role> result = new ArrayList<Role>();   
		        boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);   
		        try {
		            if (teammanaged != null){
	                    ContainerTeam team = ContainerTeamHelper.service.getContainerTeam(teammanaged); 
	                    Vector<?> v = team.getRoles();
	                    if(v == null){   
	                        return result;   
	                    }   
	                    for (int i = 0; i < v.size(); i++) {   
	                        Object obj = v.get(i);   
	                        if(obj instanceof Role){   
	                            Role role = (Role)obj;   
	                            if(!result.contains(role))   
	                                result.add(role);              
	                        }   
	                    }
		            }
		        	
				} catch (WTException e) {
					logger.error(CLASSNAME+".getContainerTeamRoles:" + e);
			    } finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
		        return result;   
	        }   
	    } catch (Exception e) {   
	    	 LOGGER.error(e.getMessage(),e);  
	    }   
	    return null;        
	}   
	       
	/**  
	 * @author baijj
	 *              Get the Container object of the specified role member list
	 * @param teammanaged   
	 *              The Container object can contain team
	 * @param role  The designated team roles
	 * @return  Specify the Role member list of the Container.(WTPrincipalReference)  
	 */   
	public static ArrayList<WTPrincipalReference> getContainerPrincipalByRole(ContainerTeamManaged teammanaged, Role role){   
	    try {   
	        if (!RemoteMethodServer.ServerFlag) {   
	            return (ArrayList<WTPrincipalReference>) RemoteMethodServer.getDefault().invoke("getContainerPrincipalByRole", UserUtil.class.getName(), null,   
	                new Class[] {ContainerTeamManaged.class, Role.class},   
	                new Object[] {teammanaged, role});   
	    } else {   
	        ArrayList<WTPrincipalReference> result = new ArrayList<WTPrincipalReference>();   
	        boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);   
	        try {   
                if(teammanaged != null){
                    ContainerTeam team = ContainerTeamHelper.service.getContainerTeam(teammanaged);            
                    StandardContainerTeamService scts = StandardContainerTeamService.newStandardContainerTeamService();   
                       
                    WTGroup wtgroup = scts.findContainerTeamGroup(team, ContainerTeamHelper.ROLE_GROUPS, role.toString());   
                    if (wtgroup != null) {   
                        Enumeration<?> enumeration = OrganizationServicesHelper.manager.members(wtgroup, false, true);   
                        while(enumeration.hasMoreElements()){   
                            WTPrincipalReference wtprincipalreference = WTPrincipalReference.newWTPrincipalReference((WTPrincipal)enumeration.nextElement());   
                            if(!result.contains(wtprincipalreference)){   
                                result.add(wtprincipalreference);   
                            }                                  
                        }   
                    }   
                 }
	        } catch (Exception e) {   
				logger.error(CLASSNAME+".getContainerPrincipalByRole:" + e);     
            } finally {   
                SessionServerHelper.manager.setAccessEnforced(enforce);   
            }   
            return result;   
            }   
	    } catch (Exception e) {   
	       LOGGER.error(e.getMessage(),e);  
	    }   
	    return null;        
	}   

	/**
	 * @author bjj
	 * 给角色设置/重新设置参与者
	 * @param wfRoleInvl
	 * @param roleArr
	 * @param self
	 * @param object
	 * @throws WTException 
	 */
	public static void setDocRolePrincipal(String wfRoleInvl, String rolesStr, ObjectReference self, WTObject object) throws WTException {    
        try {
			if (!RemoteMethodServer.ServerFlag) {
					RemoteMethodServer.getDefault().invoke("setDocRolePrincipal", 
							UserUtil.class.getName(), null, new Class[] { String.class, String.class, ObjectReference.class, WTObject.class },
							new Object[] { wfRoleInvl, rolesStr, self, object });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				WfProcess wfProcess = (WfProcess) self.getObject();
				Role wfRole = Role.toRole(wfRoleInvl);
				Team team = null;
				try {
					team = TeamHelper.service.getTeam(wfProcess);
					ContainerTeamManaged containerTeamManaged = null;
					WTContainer wtContainer = wfProcess.getContainer();
					if (wtContainer instanceof PDMLinkProduct){
						containerTeamManaged = (PDMLinkProduct)wtContainer;
					}
					if (wtContainer instanceof WTLibrary){
						containerTeamManaged = (WTLibrary)wtContainer;
					}
					ArrayList<Role> roles = new ArrayList<Role>();
						roles = WfRoleHelper.getRoleList();
					String[] roleArr = rolesStr.split("、");
					for (int i = 0; i < roleArr.length; i++) {
						String roleStr = roleArr[i];
						for (int j = 0; j < roles.size(); j++) {
							Role teamRole = roles.get(j);
							String roleDispName = teamRole.getDisplay(Locale.CHINA);
							if (roleDispName != null && roleDispName.equals(roleStr)) {
								// 获取签审对象所在上下文，当前团队角色下的人员
								List<WTPrincipal> principals = UserUtil
										.getMembersOfContainerRole(containerTeamManaged, teamRole);
								// 考虑到返工、不同审批节点具有相同角色的情况，重置角色人员
								@SuppressWarnings("rawtypes")
								Enumeration penum = team.getPrincipalTarget(wfRole);
								while (penum.hasMoreElements()) {
									WTPrincipalReference wtPrincipalReference = (WTPrincipalReference) penum
											.nextElement();
									WTPrincipal wtPrincipal = wtPrincipalReference
											.getPrincipal();
									TeamHelper.service.deleteRolePrincipalMap(wfRole,
											wtPrincipal, team);
								}
								for (int k = 0; k < principals.size(); k++) {
									WTPrincipal principal = principals.get(k);
									UserUtil.setProcessRoleHolder(wfRole,
											principal, self);
								}
							}
						}
					}
				} catch (Exception e) {   
					logger.error(CLASSNAME+".setDocRolePrincipal:" + e);   
	            } finally {   
	                SessionServerHelper.manager.setAccessEnforced(enforce);   
	            }   
			}
		} catch (RemoteException | InvocationTargetException | WTRuntimeException e) {
	 	       LOGGER.error(e.getMessage(),e);
		}
	}
	
	/**
	 * @author bjj
	 * add Principal For  Role
	 * @param teamManaged
	 * @param role
	 * @param pricipalList
	 */
	public static void addPrincipalForRole(TeamManaged teamManaged, Role role, List<WTPrincipal> pricipalList) {
		
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("setDocRolePrincipal", 
						UserUtil.class.getName(), null, new Class[] { TeamManaged.class, Role.class, List.class }, new Object[] { teamManaged, role, pricipalList });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false); 
				try {
					Team team = TeamHelper.service.getTeam(teamManaged);
					for (int i = 0; i < pricipalList.size(); i++) {
						WTPrincipal principal = pricipalList.get(i);
						team.addPrincipal(role, principal);
					}
				} catch (Exception e) {   
					logger.error(CLASSNAME+".addPrincipalForRole:" + e);   
			    } finally {   
			        SessionServerHelper.manager.setAccessEnforced(enforce);   
			    }
			}
		} catch (RemoteException | InvocationTargetException e) {
	 	       LOGGER.error(e.getMessage(),e);
		}
		
	}
	
		
	public static <T> void test() throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException {  
		
		NmCommandBean localNmCommandBean = new NmCommandBean();
	    Map<String, String> m= new HashMap<String, String>();
	    m.put("BB", "基带工程师");
	    boolean a = true;
	    localNmCommandBean.addToMap((HashMap<String, String>) m,"BB","基带工程师",a);
	    
	    System.out.println("17---------------------------" + UserUtil.addPrincipal(localNmCommandBean));
	    System.out.println("-----------------------------");
	    localNmCommandBean.getContainer();
	    System.out.println("18-----------------------" + UserUtil.addPrincipal(localNmCommandBean));
	    
	}
	
	public static void main(String[] args)throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException{
	    if (!RemoteMethodServer.ServerFlag)
	      try {
	    	    RemoteMethodServer server = RemoteMethodServer.getDefault();
				server.setUserName("wcadmin");
			server.setPassword("wcadmin");
	        RemoteMethodServer.getDefault().invoke("test", UserUtil.class.getName(), null, new Class[0], new Object[0]);
	      } catch (java.rmi.RemoteException e) {
	        e.printStackTrace();
	      }
	}
	  
		
	
}