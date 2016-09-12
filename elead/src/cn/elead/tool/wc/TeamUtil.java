package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;

import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.log4j.LogR;
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
import wt.util.WTRuntimeException;
import wt.vc.sessioniteration.sessioniterationResource;

import com.google.gwt.rpc.client.impl.RemoteException;
import com.ptc.cat.entity.client.Exception;

public class TeamUtil implements RemoteAccess {

	private static final String CLASSNAME = TeamUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	/**
	 * 获取某上下文中某个角色下的人员
	 * 
	 * @author Seelen Chron
	 * @param ctm
	 * @param role
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public static List<WTPrincipal> getMembersOfContainerRole(
			ContainerTeamManaged ctm, Role role) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				String method = "getMembersOfContainerRole";
				Class[] types = { ContainerTeamManaged.class, Role.class };
				Object[] vals = { ctm, role };
				return (ArrayList) RemoteMethodServer.getDefault().invoke(
						method, CLASSNAME, null, types, vals);
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.setAccessEnforced(false);
				List<WTPrincipal> rtn = new ArrayList<WTPrincipal>();
				try {
					ContainerTeam containerteam = ContainerTeamHelper.service
							.getContainerTeam(ctm);
					WTGroup wtgroup = ContainerTeamHelper.service
							.findContainerTeamGroup(containerteam,
									"roleGroups", role.toString());
					if (wtgroup != null) {
						Enumeration members = wtgroup.members();
						while (members.hasMoreElements()) {
							WTPrincipal wtp = (WTPrincipal) members
									.nextElement();
							if (wtp instanceof WTUser) {
								WTUser user = (WTUser) wtp;
								rtn.add(user);
							}
						}
					}
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					SessionServerHelper.manager
							.setAccessEnforced(isAccessEnforced);
				}
				return rtn;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * getContainerTeam
	 * 
	 * @param container
	 * @return containerTeam
	 * @throws WTException
	 * 
	 */
	public static ContainerTeam getContainerTeam(WTContainer container) {
		SessionServerHelper.manager.setAccessEnforced(false);
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ContainerTeam) RemoteMethodServer.getDefault().invoke(
						"getContainerTeam", TeamUtil.class.getName(), null,
						new Class[] { WTContainer.class },
						new Object[] { container });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager
						.setAccessEnforced(false);
				ContainerTeam containerTeam = null;
				try {
					if (container != null) {
						PDMLinkProduct product = (PDMLinkProduct) container;
						containerTeam = (ContainerTeam) product
								.getContainerTeamReference().getObject();
					}
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return containerTeam;
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WTRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 刪除团队中的某个角色
	 * 
	 * @param container
	 * @param role
	 * @throws WTException
	 */
	public static void deleteTeamRole(ContainerTeamManaged ctm, String role)
			throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault()
						.invoke("deleteTeamRole",
								TeamUtil.class.getName(),
								null,
								new Class[] { ContainerTeamManaged.class,
										String.class },
								new Object[] { ctm, role });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				try {
					ContainerTeam tm = ContainerTeamHelper.service
							.getContainerTeam(ctm);
					tm.deleteRole(Role.toRole("role"));
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					SessionServerHelper.manager
							.setAccessEnforced(isAccessEnforced);
				}
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
		}

	}

	/**
	 * deleteTeamUser
	 * 
	 * @param ctm
	 * @param role
	 * @param principal
	 * @throws WTException
	 */
	public static void deleteTeamUser(ContainerTeamManaged ctm, String role,
			WTPrincipal principal) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke(
						"deleteTeamUser",
						TeamUtil.class.getName(),
						null,
						new Class[] { ContainerTeamManaged.class, String.class,
								WTPrincipal.class },
						new Object[] { ctm, role, principal });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				try {
					ContainerTeam cTeam = ContainerTeamHelper.service
							.getContainerTeam(ctm);
					cTeam.deletePrincipalTarget(Role.toRole(role), principal);
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("e>>>"+e);
				} finally {
					SessionServerHelper.manager
							.setAccessEnforced(isAccessEnforced);
				}
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
		}
	}

	/**
	 * updataTeamUser
	 * 
	 * @param ctm
	 * @param role
	 * @param principal
	 * @throws WTException
	 */
	public static void updataTeamUser(ContainerTeamManaged ctm, String role,
			WTPrincipal principal) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					RemoteMethodServer.getDefault().invoke(
							"deleteTeamUser",
							TeamUtil.class.getName(),
							null,
							new Class[] { ContainerTeamManaged.class, String.class,
									WTPrincipal.class },
							new Object[] { ctm, role, principal });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager
						.isAccessEnforced();
				try {
					ContainerTeam cTeam = ContainerTeamHelper.service
							.getContainerTeam(ctm);
					cTeam.addPrincipal(Role.toRole(role), principal);
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
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
		}
	}
}
