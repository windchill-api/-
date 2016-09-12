package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
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
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.TeamException;
import wt.team.TeamHelper;
import wt.team.WfRoleHelper;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;

import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.netmarkets.role.NmRoleHelper;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.picker.principal.PrincipalBean;

public class UserUtil implements RemoteAccess {
	  private static String className = UserUtil.class.getName();
	  private static final Logger LOGGER = LogR.getLogger(UserUtil.class.getName());
 
	  public static WTPrincipalReference getCurrentUser() throws WTException {
	    return SessionHelper.manager.getPrincipalReference();
	  }
	  public static String getCurrentUserName() throws WTException {
		    WTPrincipalReference reference = getCurrentUser();
		    return reference.getName();
		  }
	  public static String getCurrentUserFullName() throws WTException {
	    WTPrincipalReference reference = getCurrentUser();
	    return reference.getFullName();
	  }
	  
	  public static WTPrincipal getWTUserByName(String userName) throws WTException {
			if (!RemoteMethodServer.ServerFlag) {
				try {
					return (WTPrincipal) RemoteMethodServer.getDefault()
							.invoke("getWTUserByName", UserUtil.class.getName(), null,
									new Class[] {String.class}, new Object[] {userName});
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					throw new WTException(e);
				}
			} else {
					boolean enforce = wt.session.SessionServerHelper.manager
							.setAccessEnforced(false);
				    if (!StringUtils.isEmpty(userName)) {
				    	SessionServerHelper.manager.setAccessEnforced(enforce);
				        return OrganizationServicesHelper.manager.getUser(userName);
				    }
		    LOGGER.error("userName is null");
		    return null;
		  }
	  }
	
	
	  public List<WTUser> findUser(String name) throws WTException {
	    if (!RemoteMethodServer.ServerFlag) {
	      try {
	        return (List<WTUser>)RemoteMethodServer.getDefault().invoke(
	          "findUser", UserUtil.class.getName(), null, 
	          new Class[] { String.class }, new Object[]{name});
	      } catch (Exception e) {
	        LOGGER.error(e.getMessage(), e);
	        throw new WTException(e);
	      }
	    }
	    boolean enforce = wt.session.SessionServerHelper.manager
				.setAccessEnforced(false);
	    WTUser currentUser = null;
	    List<WTUser>  results = new ArrayList<WTUser>();
	    try {
	      currentUser = (WTUser)SessionHelper.manager.getPrincipal();
	      SessionHelper.manager.setAdministrator();
	      DirectoryContextProvider dcp = OrganizationServicesHelper.manager.newDirectoryContextProvider((String[])null,(String[])null);
	      Enumeration enmer = OrganizationServicesHelper.manager.findLikeUsers("name", name, dcp);
	      while (enmer.hasMoreElements()) {
	        WTUser user = (WTUser)enmer.nextElement();
	        if ((!user.isDisabled()) && (!results.contains(user)))
	          results.add(user);
	      }
	    }
	    finally {
	      if (currentUser != null) {
	        SessionHelper.manager.setPrincipal(currentUser.getName());
	      }
	      SessionServerHelper.manager.setAccessEnforced(enforce);
	    }
	    return results;
	  }
	
	  public static List<WTPrincipal> getMembersOfContainerRole(ContainerTeamManaged ctm, Role role) {
		  try{
		        if (!RemoteMethodServer.ServerFlag) {
		                return (List<WTPrincipal>) RemoteMethodServer.getDefault().invoke("getMembersOfContainerRole", 
		                		UserUtil.class.getName(), null, new Class[] { ContainerTeamManaged.class,Role.class},
		                		new Object[] { ctm, role});
		        } else {
		        	WTOrganization org = null;
		        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				    List rtn = new ArrayList();
				    boolean accessFlag = SessionServerHelper.manager.setAccessEnforced(false);
					if (ctm != null) {
				          try {
				    		 ContainerTeam containerteam = ContainerTeamHelper.service
				    			        .getContainerTeam(ctm);
				    		 WTGroup wtgroup = ContainerTeamHelper.service
				    				        .findContainerTeamGroup(containerteam, "roleGroups", 
				    				        role.toString());
				    		      if (wtgroup != null) {
				    			        Enumeration members = wtgroup.members();
				    			        while (members.hasMoreElements()) {
				    			          WTPrincipal wtp = (WTPrincipal)members.nextElement();
				    			          if ((wtp instanceof WTUser)) {
				    			            WTUser user = (WTUser)wtp;
				    			            rtn.add(user);
				    			          }
				    			        }
				    			      }
						   
						    } catch (WTException wte) {
						      wte.printStackTrace();
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
	    public static void addUserTORole(WTContainer wtContainer, Role role, WTPrincipal wtPrincipal)
	    {
		    	if (!RemoteMethodServer.ServerFlag) {
					try {
						  RemoteMethodServer.getDefault()
								.invoke("addUserTORole", DocUtil.class.getName(), null,
										new Class[] {WTContainer.class,Role.class}, new Object[] {wtContainer,role,wtPrincipal});
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						try {
							throw new WTException(e);
						} catch (WTException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				} else {
					boolean enforce = wt.session.SessionServerHelper.manager
							.setAccessEnforced(false);
		        try
		        {
		            ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) wtContainer);
		            ContainerTeamHelper.service.addMember(containerTeam, role, wtPrincipal);
		        } catch (WTException e)
		        {
		            e.printStackTrace();
		        }finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
		    }
		}

		/**
		 * @author BaiJuanjuan
		 * add Principal
		 * @param cb
		 * @return
		 * @throws WTException
		 */	
	    public static FormResult addPrincipal(NmCommandBean cb) throws WTException {
	    	try {   
	            if (!RemoteMethodServer.ServerFlag) {   
	                return (FormResult) RemoteMethodServer.getDefault().invoke("addPrincipal", UserUtil.class.getName(), null,   
	                        new Class[] {String.class},   
	                        new Object[] {cb});   
	            } else {   
	                   
	                boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
			        FormResult result = new FormResult(FormProcessingStatus.SUCCESS);
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
			        ArrayList result1 = NmRoleHelper.service.addUsersToRole(cb, role, selectedPrincipals);
			        SessionServerHelper.manager.setAccessEnforced(enforce);
			        return result;
	            }
            } catch (Exception e) {   
	            e.printStackTrace();   
	        }   
	        return null;  
			        
	     }

	    public static OrgContainer searchOrgContainer(String orgName){
	    	try {   
	            if (!RemoteMethodServer.ServerFlag) {   
	                return (OrgContainer) RemoteMethodServer.getDefault().invoke("searchOrgContainer", UserUtil.class.getName(), null,   
	                        new Class[] {String.class},   
	                        new Object[] {orgName});   
	            } else {   
	                   
	                boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				    OrgContainer org = null;
				    try {
				      QuerySpec qs = new QuerySpec(OrgContainer.class);
				      qs.appendWhere(new SearchCondition(OrgContainer.class, "containerInfo.name", "=", orgName), new int[1]);
				      QueryResult qr = PersistenceHelper.manager.find(qs);
				      if (qr.hasMoreElements())
				        org = (OrgContainer)qr.nextElement();
				    }
				    catch (QueryException e) {
				      e.printStackTrace();
				    } catch (WTException e) {
				      e.printStackTrace();
				    }finally {
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
				    return org;
	            }
	            } catch (Exception e) {   
		            e.printStackTrace();   
		        }   
		        return null;  
		  }
		  public static boolean isOrgAdmin(WTUser user){
			  try {   
		            if (!RemoteMethodServer.ServerFlag) {   
		                return (boolean) RemoteMethodServer.getDefault().invoke("isOrgAdmin", UserUtil.class.getName(), null,   
		                        new Class[] {WTUser.class},   
		                        new Object[] {user});   
		            } else {   
		                   
		                boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
					    try
					    {
					      WTOrganization org = user.getOrganization();
					      if (org == null)
					        return false;
					      OrgContainer orgContainer = searchOrgContainer(org.getName());
					      if (orgContainer == null)
					        return false;
					      return WTContainerServerHelper.getAdministratorsReadOnly(orgContainer).isMember(user);
					    } catch (WTException e) {
					      e.printStackTrace();
					    }finally {
							SessionServerHelper.manager.setAccessEnforced(enforce);
						}
					    return true;
		            }
		    } catch (Exception e) {   
	            e.printStackTrace();   
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
		                        new Class[] {WTGroup.class},   
		                        new Object[] {group});   
		            } else {   
		                   
		                boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				        ArrayList<WTUser> memebers = new ArrayList<WTUser>();
		
				        Enumeration member = group.members();
				        while (member.hasMoreElements()) {
				            WTPrincipal principal = (WTPrincipal) member.nextElement();
				            if (principal instanceof WTUser)
				                memebers.add((WTUser) principal);
				            else if (principal instanceof WTGroup)
				                memebers.addAll(getGroupMembersOfUser((WTGroup) principal));
				        }
				        return memebers;
		            }   
		        } catch (Exception e) {   
		            e.printStackTrace();   
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
		                        new Class[] {String.class},   
		                        new Object[] {userName});   
		            } else {   
		                   
		                boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				        ArrayList<WTUser> list = new ArrayList<WTUser>();
		
				        QuerySpec qs = new QuerySpec(WTUser.class);
				        int[] indx0 = new int[]{0};
		
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
						SessionServerHelper.manager.setAccessEnforced(enforce);
				        return list;
		            }   
		        } catch (Exception e) {   
		            e.printStackTrace();   
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
			public static void setProcessRoleHolder(Role role, WTPrincipal principal,
					ObjectReference self) {
				
				if (!RemoteMethodServer.ServerFlag) {
    				try {
    	            	RemoteMethodServer.getDefault().invoke("addUserToContainer", 
    	            		UserUtil.class.getName(), null, new Class[] {Role.class, WTPrincipal.class, ObjectReference.class},
    	            		new Object[] { role,principal,self });
    				} catch (RemoteException e) {
                        LOGGER.error(e.getMessage(),e);
                    } catch (InvocationTargetException e) {
                    	LOGGER.error(e.getMessage(),e);
                    }
    	        } else{ 
    	        	    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
						WfProcess wfProcess = (WfProcess) self.getObject();
						try {
							Team team = TeamHelper.service.getTeam(wfProcess);
							TeamHelper.service.addRolePrincipalMap(role, principal, team);
						} catch (TeamException e) {
							e.printStackTrace();
						} catch (WTException e) {
							e.printStackTrace();
						}
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
						}
			}
			
			/**
			 * @author BaiJuanjuan
			 * @Description:add user to container team.
			 * @param map
			 * @param contained
			 * @throws WTException
			 */
		    public static void addUserToContainer(WTContained contained) throws WTException {
		    	if (!RemoteMethodServer.ServerFlag) {
    				try {
    	            	RemoteMethodServer.getDefault().invoke("addUserToContainer", 
    	            		UserUtil.class.getName(), null, new Class[] { WTContained.class },
    	            		new Object[] { contained });
    				} catch (RemoteException e) {
                        LOGGER.error(e.getMessage(),e);
                    } catch (InvocationTargetException e) {
                    	LOGGER.error(e.getMessage(),e);
                    }
    	        } else{ 
					    boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
					    
				    	if (!(contained instanceof WTContained))
				    		throw new WTException("The argument is not the type of WTContained");
				    	
						WTContainer container = contained.getContainer();
						if(!Constant.containerMap.containsKey(container)) {
							ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged)container);
							ArrayList<?> listPrincipals = containerTeam.getAllPrincipalsForTarget(Constant.roleMember);
							boolean flag = false;
							if(!listPrincipals.contains(Constant.principalReference)) {
								containerTeam.addPrincipal(Constant.roleMember, Constant.principal);
								flag = true;
							}
							Constant.containerMap.put(container, flag);
							
						}
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
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
				List<WTPrincipal> admins = new ArrayList<WTPrincipal>();
				try {   
		            if (!RemoteMethodServer.ServerFlag) {   
		                return (List<WTPrincipal>) RemoteMethodServer.getDefault().invoke("findOrgAdmins", UserUtil.class.getName(), null,   
		                        new Class[] {WTOrganization.class},   
		                        new Object[] {org});   
		            } else {  
						boolean accessEnforced = SessionServerHelper.manager
								.setAccessEnforced(false);
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
									if (principal instanceof WTUser)
										admins.add(principal);
								}
							}
						} finally {
							SessionServerHelper.manager.setAccessEnforced(accessEnforced);
						}
						return admins;
		            }
		         }catch (Exception e) {   
				            e.printStackTrace();   
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
		                        new Class[] {ContainerTeamManaged.class},   
		                        new Object[] {teammanaged});   
		            } else {   
		                ArrayList<Role> result = new ArrayList<Role>();   
		                   
		                boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);   
		                try {   
		                    try {
		                    	if (teammanaged != null){
			                        ContainerTeam team = ContainerTeamHelper.service.getContainerTeam(teammanaged); 
			                        Vector v = team.getRoles();
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
		                    } catch (TeamException e) {   
		                        e.printStackTrace();   
		                    } catch (WTException e) {   
		                        e.printStackTrace();   
		                    }   
		                } catch (Exception e) {   
		                    // TODO: handle exception   
		                    e.printStackTrace();       
		                } finally {   
		                    SessionServerHelper.manager.setAccessEnforced(enforce);   
		                }   
		   
		                return result;   
		            }   
		        } catch (Exception e) {   
		            e.printStackTrace();   
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
		                    try {
			                    if(teammanaged != null){
			                        ContainerTeam team = ContainerTeamHelper.service.getContainerTeam(teammanaged);            
			                        StandardContainerTeamService scts = StandardContainerTeamService.newStandardContainerTeamService();   
			                           
			                        WTGroup wtgroup = scts.findContainerTeamGroup(team, ContainerTeamHelper.ROLE_GROUPS, role.toString());   
			                        if (wtgroup != null) {   
			                            Enumeration enumeration = OrganizationServicesHelper.manager.members(wtgroup, false, true);   
			                            while(enumeration.hasMoreElements()){   
			                                WTPrincipalReference wtprincipalreference = WTPrincipalReference   
			                                        .newWTPrincipalReference((WTPrincipal) enumeration   
			                                                .nextElement());   
			                                if(!result.contains(wtprincipalreference)){   
			                                    result.add(wtprincipalreference);   
			                                }                                  
			                            }   
			                        }   
			                     }
		                    } catch (TeamException e) {   
		                        e.printStackTrace();   
		                    } catch (WTException e) {   
		                        e.printStackTrace();   
		                    }   
		                } catch (Exception e) {   
		                    // TODO: handle exception   
		                    e.printStackTrace();       
		                } finally {   
		                    SessionServerHelper.manager.setAccessEnforced(enforce);   
		                }   
		   
		                return result;   
		            }   
		        } catch (Exception e) {   
		            e.printStackTrace();   
		        }   
		        return null;        
		    }   

		    /**  
		     * 通过名称查找OrgContainer组织对象  
		     * @param name  查询OrgContainer名称条件  
		     * @param accessControlled  是否受到权限制约  
		     * @return 返回查询到的OrgContainer对象  
		     */   
		    public static OrgContainer getOrgContainer(String name, boolean accessControlled) throws WTException{   
		        try {   
		            if (!RemoteMethodServer.ServerFlag) {   
		                return (OrgContainer) RemoteMethodServer.getDefault().invoke("getOrgContainer", UserUtil.class.getName(), null,   
		                        new Class[] {String.class, boolean.class},   
		                        new Object[] {name, accessControlled});   
		            } else {   
		                OrgContainer org = null;   
		                boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(accessControlled);   
		                try {   
		                    QuerySpec criteria = new QuerySpec(OrgContainer.class);   
		                    criteria.appendWhere(new SearchCondition(OrgContainer.class,OrgContainer.NAME,SearchCondition.EQUAL,name,false));   
		                    QueryResult results = PersistenceHelper.manager.find(criteria);   
		                       
		                    if(results.hasMoreElements())  {   
		                        org = (OrgContainer)results.nextElement();   
		                    }else{   
		                        return null;   
		                    }   
		                } catch (Exception e) {   
		                    // TODO: handle exception   
		                    e.printStackTrace();   
		                } finally {   
		                    SessionServerHelper.manager.setAccessEnforced(enforce);   
		                }   
		   
		                return org;   
		            }   
		        } catch (Exception e) {   
		            e.printStackTrace();   
		        }   
		        return null;   
		    }    

			/**
			 * 给角色设置/重新设置参与者
			 * 
			 * @param wfRoleInvl
			 * @param roleArr
			 * @param self
			 * @param object
			 * @throws WTException 
			 */
			public static void setDocRolePrincipal(String wfRoleInvl, String rolesStr,
					ObjectReference self, WTObject object) throws WTException {    
				        if (!RemoteMethodServer.ServerFlag) {
				                 try {
									RemoteMethodServer.getDefault().invoke("setDocRolePrincipal", 
											OrganizationUtil.class.getName(), null, new Class[] { String.class,String.class},
											new Object[] { wfRoleInvl,rolesStr });
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				        } else {
				        	WTOrganization org = null;
				        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
							WfProcess wfProcess = (WfProcess) self.getObject();
							Role wfRole = Role.toRole(wfRoleInvl);
							Team team = null;
							try {
								team = TeamHelper.service.getTeam(wfProcess);
							} catch (TeamException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (WTException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							ContainerTeamManaged containerTeamManaged = null;
							WTContainer wtContainer = wfProcess.getContainer();
							if (wtContainer instanceof PDMLinkProduct){
								containerTeamManaged = (PDMLinkProduct)wtContainer;
							}
							if (wtContainer instanceof WTLibrary){
								containerTeamManaged = (WTLibrary)wtContainer;
							}
							ArrayList<Role> roles = new ArrayList<Role>();
							try {
								roles = WfRoleHelper.getRoleList();
							} catch (WTException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
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
						}
			}
							


			
		  public static <T> void test() throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException
		  {    /*
			    String role = "研发代表";
			    Role roles = null;
			    String oid = "OR:wt.pdmlink.PDMLinkProduct:190633";
			    PDMLinkProduct wc = (PDMLinkProduct)new ReferenceFactory().getReference(oid).getObject();
			    if (StringUtils.isNotEmpty(role)) {
			      roles = Role.toRole(role);
			      System.out.println(getMembersOfContainerRole(wc, roles));
			      WTPrincipal principal = getWTUserByName("baijunjuan");
			
			      ObjectReference localObjectReference = new ObjectReference();
			
			      localObjectReference.getObject();
			      ObjectReference docRef = ObjectReference.newObjectReference(DocUtil.getDocumentByNumber("1001"));
			      setProcessRoleHolder(Role.toRole(role), principal, docRef);
			    }
			    */
				/*String ContainerOid = "OR%3Awt.inf.container.OrgContainer%3A49401".replace("%3A",":");
				WTContainer container = (WTContainer) new ReferenceFactory().getReference(ContainerOid).getObject();
		        System.out.println("-------"+container);
		        addUserToContainer(container);*/
			  
			  	/*WTOrganization org = getWTOrganization("huaqin");
			    System.out.println("#####coming1######"+org);
			    List<WTPrincipal> ad = findOrgAdmins(org);
			    System.out.println("#####coming2######"+ad);
			    */
			  
			    WTContainer wtContainer = DocUtil.getWtContainerByName("测试产品");
			    ContainerTeamManaged product1 = (PDMLinkProduct) wtContainer;  
				//************************ Test of getContainerTeamRoles ********************************   
		        ArrayList<Role> roles = UserUtil.getContainerTeamRoles(product1);   
		        for (int i = 0; i < roles.size(); i++) {   
		            System.out.println("Find Role = " + roles.get(i));   
		        }   
		           
				//************************ Test of getContainerPrincipalByRole ********************************   
		        PDMLinkProduct product = ProductUtil.getPDMLinkProductByName("bjj8", false); 
		        if(product != null){
			        ArrayList<Role> roles1 = UserUtil.getContainerTeamRoles(product);   
			        /*if(roles1 != null){*/
				        for (int i = 0; i < roles1.size(); i++) {   
				            System.out.println("Find Role = " + (roles1.get(i)).getDisplay());   
				            ArrayList<WTPrincipalReference> principals = UserUtil.getContainerPrincipalByRole(product, roles1.get(i));   
				            for (int j = 0; j < principals.size(); j++) {   
				                System.out.println("--> " + (principals.get(j)).getDisplayName());   
				            }   
				        } 
			        /*}*/
		        }     

			    
		  }
		
		  public static void main(String[] args)throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException
		  {
		    if (!RemoteMethodServer.ServerFlag)
		      try {
		    	    RemoteMethodServer server = RemoteMethodServer.getDefault();
					server.setUserName("wcadmin");
					server.setPassword("wcadmin");
		            RemoteMethodServer.getDefault().invoke("test", UserUtil.class.getName(), null, new Class[0], new Object[0]);
		      }
		      catch (java.rmi.RemoteException e)
		      {
		        e.printStackTrace();
		      }
		  }
		  
			
			
	}