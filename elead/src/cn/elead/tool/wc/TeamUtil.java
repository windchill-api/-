package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import wt.clients.prodmgmt.PartHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.inf.library.WTLibrary;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.pdmlink.PDMLinkProduct;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.util.WTException;

import wt.util.WTInvalidParameterException;
import wt.util.WTRuntimeException;
import com.cambridgedocs.drivers.cdocspublishpdf.layout.Container;
import com.google.gwt.rpc.client.impl.RemoteException;
import com.ibm.icu.text.MessagePattern.Part;
import com.ptc.windchill.collector.api.part.partCollectorResource;










public class TeamUtil implements RemoteAccess{
	
	private static final String CLASSNAME = TeamUtil.class.getName();
	public static void main(String[] args) {
		try {
			RemoteMethodServer server = RemoteMethodServer.getDefault();
			server.setUserName("wcadmin");
			server.setPassword("wcadmin");
			try {
				// server.invoke("setPartTenclosure",
				// Test.class.getName(), null, new Class[]
				// {String.class},
				// new Object[] {args[0]});
				server.invoke("test", TeamUtil.class.getName(), null,
						new Class[] {}, new Object[] {});
			} catch (java.rmi.RemoteException e) {
				e.printStackTrace();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	
	public static void test() throws WTException{
		/*WTContainerRef result =getContainerByName("A1511");
		result.getKey();*/
	//	WTContainerRef container=getContainerByName("A1032");
		WTPart wtPart=PartUtil.getPartByNumber("HQ11100005000");
	//Container container=	(Container) wtPart.getContainer();
	Team team= TeamHelper.service.getTeam(wtPart);
	team.deleteRole(Role.toRole("RD"));
		//addPrincipalForRole(container,);
//		Team team= TeamHelper.service.copyTeam(null, null, arg2, )
			
		
	}
	
	// WTPrincipal 参与者：群主、用户
	public static void addPrincipalForRole(TeamManaged teamManaged, Role role,
			List<WTPrincipal> pricipalList) {
		 
		try {
			Team team = TeamHelper.service.getTeam(teamManaged);
			for (int i = 0; i < pricipalList.size(); i++) {
				WTPrincipal principal = pricipalList.get(i);
				team.addPrincipal(role, principal);
			}
		} catch (WTInvalidParameterException e) {
			e.printStackTrace();
		} catch (WTRuntimeException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 根据名称找到上下文
	 * @param name
	 * @return
	 */
	public static WTContainerRef getContainerByName(String name) {
        WTContainerRef result = null;
        ContainerTeamManaged containerTeamManaged = null;
        try {
            QuerySpec select = new QuerySpec(WTContainer.class);
            select.appendWhere(new SearchCondition(WTContainer.class, WTContainer.NAME, SearchCondition.EQUAL, name));
            QueryResult queryResult = PersistenceHelper.manager.find(select);
            if (queryResult.hasMoreElements()) {
                WTContainer container = (WTContainer) queryResult.nextElement();
                result = WTContainerRef.newWTContainerRef(container);
           /*    System.out.println("$"+result+"$");
                if (container instanceof PDMLinkProduct) {
     			   containerTeamManaged = (PDMLinkProduct) container;
     		   } else if (container instanceof WTLibrary) {
     			   containerTeamManaged = (WTLibrary) container;
     		   }
                List<WTPrincipal> zjgygcsprincipals =     getMembersOfContainerRole((ContainerTeamManaged) container,Role.toRole("PQA"));
                for (WTPrincipal principal : zjgygcsprincipals) {
					System.out.println("******"+principal+"**");
			   }*/
            }
        } catch (WTException ex) {
        }
        return result;
    }
	
	/**
	 * 获取某上下文中某个角色下的人员
	 * 
	 * @author Seelen Chron
	 * @param ctm
	 * @param role
	 * @return
	 */
	public static List<WTPrincipal> getMembersOfContainerRole(  
			ContainerTeamManaged ctm, Role role) {
		if (!RemoteMethodServer.ServerFlag) {
			String method = "getMembersOfContainerRole";
			Class[] types = { ContainerTeamManaged.class, Role.class };
			Object[] vals = { ctm, role };
			try {
				return (ArrayList) RemoteMethodServer.getDefault().invoke(
						method, CLASSNAME, null, types, vals);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new ArrayList();
		}
		List<WTPrincipal> rtn = new ArrayList<WTPrincipal>();
		boolean accessFlag = SessionServerHelper.manager
				.setAccessEnforced(false);
		try {
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
		} catch (WTException wte) {
			wte.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(accessFlag);
		}
		return rtn;
	}
}
