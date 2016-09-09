package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.pdmlink.PDMLinkProduct;
import wt.project.Role;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;

import com.google.gwt.rpc.client.impl.RemoteException;
import com.ptc.cat.entity.client.Exception;

public class TeamUtil implements RemoteAccess {

	private static final String CLASSNAME = TeamUtil.class.getName();
	/**
	 * 获取某上下文中某个角色下的人员
	 * 
	 * @author Seelen Chron
	 * @param ctm
	 * @param role
	 * @return
	 */
	@SuppressWarnings({"unchecked"})
	public static List<WTPrincipal> getMembersOfContainerRole(
			ContainerTeamManaged ctm, Role role) {
		boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
    	SessionServerHelper.manager.setAccessEnforced(false);
		List<WTPrincipal> rtn = new ArrayList<WTPrincipal>();
		try {
			if (!RemoteMethodServer.ServerFlag) {
				String method = "getMembersOfContainerRole";
				Class[] types = { ContainerTeamManaged.class, Role.class };
				Object[] vals = { ctm, role };
			
						return (ArrayList) RemoteMethodServer.getDefault().invoke(
								method, CLASSNAME, null, types, vals);
			}
					ContainerTeam containerteam = ContainerTeamHelper.service
							.getContainerTeam(ctm);
					WTGroup wtgroup = ContainerTeamHelper.service
							.findContainerTeamGroup(containerteam, "roleGroups",
									role.toString());
					if (wtgroup != null) {
						Enumeration members = wtgroup.members();
						while (members.hasMoreElements()) {
							WTPrincipal wtp = (WTPrincipal) members.nextElement();
							if (wtp instanceof WTUser) {
								WTUser user = (WTUser) wtp;
								rtn.add(user);
							}
						}
					}
		
		
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
		}
		return rtn;
	}

	/**
	 * getContainerTeam
	 * @param container
	 * @return containerTeam
	 * @throws WTException
	 * 
	 */
	public static ContainerTeam getContainerTeam(WTContainer container) {
		boolean isAccessEnforced = SessionServerHelper.manager
				.isAccessEnforced();
		SessionServerHelper.manager.setAccessEnforced(false);
		try {
			if (!RemoteMethodServer.ServerFlag) {

				return (ContainerTeam) RemoteMethodServer.getDefault().invoke(
						"getContainerTeam", TeamUtil.class.getName(), null,
						new Class[] { WTContainer.class },
						new Object[] { container });

			} else {
				if (container != null) {
					PDMLinkProduct product = (PDMLinkProduct) container;
					ContainerTeam containerTeam = (ContainerTeam) product
							.getContainerTeamReference().getObject();
					return containerTeam;
				} else {
					throw new Exception("容器不存在");
				}
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
		}
		return null;
	}

	/**
	 * 刪除团队中的某个角色
	 * @param container
	 * @param role
	 * @throws WTException
	 */
	public static void deleteTeamRole(ContainerTeamManaged ctm, String role)
			throws WTException {
		boolean isAccessEnforced = SessionServerHelper.manager
				.isAccessEnforced();
		SessionServerHelper.manager.setAccessEnforced(false);
		try {
			if (!RemoteMethodServer.ServerFlag) {
					RemoteMethodServer.getDefault().invoke("deleteTeamRole",
							TeamUtil.class.getName(), null,
							new Class[] { ContainerTeamManaged.class, String.class },
							new Object[] { ctm, role });
			} else {
				ContainerTeam  tm= ContainerTeamHelper.service.getContainerTeam(ctm);
				tm.deleteRole(Role.toRole("role"));
			}
		} catch (WTInvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
		}
	}
      /**
       * deleteTeamUser
       * @param ctm
       * @param role
       * @param principal
       * @throws WTException
       */
     public static  void deleteTeamUser(ContainerTeamManaged ctm, String role,WTPrincipal principal) throws WTException{
    	 boolean isAccessEnforced = SessionServerHelper.manager
 				.isAccessEnforced();
 		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("deleteTeamUser",
						TeamUtil.class.getName(), null,
						new Class[] { ContainerTeamManaged.class, String.class ,WTPrincipal.class},
						new Object[] { ctm, role ,principal});
			}
			 ContainerTeam cTeam = ContainerTeamHelper.service.getContainerTeam(ctm);
			 cTeam.deletePrincipalTarget(Role.toRole(role), principal);
		} catch (WTInvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
		}
     }
	
     /**
      * updataTeamUser
      * @param ctm
      * @param role
      * @param principal
      * @throws WTException
      */
     public static  void updataTeamUser(ContainerTeamManaged ctm, String role,WTPrincipal principal) throws WTException{
    	 boolean isAccessEnforced = SessionServerHelper.manager
 				.isAccessEnforced();
 		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("deleteTeamUser",
						TeamUtil.class.getName(), null,
						new Class[] { ContainerTeamManaged.class, String.class ,WTPrincipal.class},
						new Object[] { ctm, role ,principal});
			}
			 ContainerTeam cTeam = ContainerTeamHelper.service.getContainerTeam(ctm);
			 cTeam.addPrincipal(Role.toRole(role), principal);
		} catch (WTInvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
		}
     }
}
