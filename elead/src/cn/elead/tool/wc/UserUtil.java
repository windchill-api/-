package cn.elead.tool.wc;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.auth.SimpleAuthenticator;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.inf.container.OrgContainer;
import wt.inf.container.WTContainerServerHelper;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.inf.team.ContainerTeamService;
import wt.log4j.LogR;
import wt.method.MethodContext;
import wt.method.MethodServerException;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.OrganizationServicesManager;
import wt.org.WTGroup;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.pdmlink.PDMLinkProduct;
import wt.pom.UnsupportedPDSException;
import wt.pom.WTConnection;
import wt.project.Role;
import wt.query.FromClause;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionManager;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.TeamException;
import wt.team.TeamHelper;
import wt.team.TeamService;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;

public class UserUtil
  implements RemoteAccess
{
  private static String className = UserUtil.class.getName();
  private static final Logger LOGGER = LogR.getLogger(UserUtil.class.getName());

  public static WTPrincipalReference getCurrentUser()
    throws WTException
  {
    return SessionHelper.manager.getPrincipalReference();
  }
  public static String getCurrentUserFullName() throws WTException {
    WTPrincipalReference reference = getCurrentUser();
    return reference.getFullName();
  }
  public static String getCurrentUserName() throws WTException {
    WTPrincipalReference reference = getCurrentUser();
    return reference.getName();
  }

  public static WTPrincipal getWTUserByName(String userName)
    throws WTException
  {
    if (!RemoteMethodServer.ServerFlag) {
      try {
        return (WTPrincipal)RemoteMethodServer.getDefault().invoke(
          "getWTUserByName", UserUtil.class.getName(), null, 
          new Class[] { String.class }, new Object[0]);
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        throw new WTException(e);
      }
    }
    if (!StringUtils.isEmpty(userName)) {
      return OrganizationServicesHelper.manager.getUser(userName);
    }
    LOGGER.error("userName is null");
    return null;
  }

  public static WTUser getWTUserByName1(String strUserName)
  {
    boolean flagAccess = SessionServerHelper.manager.setAccessEnforced(false);
    if ((strUserName == null) || (strUserName.trim().isEmpty()))
      return null;
    try
    {
      QuerySpec qs = new QuerySpec(WTUser.class);
      int index = qs.getFromClause().getPosition(WTUser.class);
      SearchCondition sc = new SearchCondition(WTUser.class, "name", "=", strUserName);
      qs.appendWhere(sc, new int[] { index });
      QueryResult qr = PersistenceHelper.manager.find(qs);
      if (qr.hasMoreElements())
        return (WTUser)qr.nextElement();
    }
    catch (WTException e) {
      e.printStackTrace();

      SessionServerHelper.manager.setAccessEnforced(flagAccess);
    }return null;
  }

  public WTGroup getGroup(String groupName)
    throws WTException
  {
    if (!RemoteMethodServer.ServerFlag) {
      try {
        return (WTGroup)RemoteMethodServer.getDefault().invoke(
          "getGroup", UserUtil.class.getName(), null, 
          new Class[] { String.class }, new Object[0]);
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        throw new WTException(e);
      }
    }
    WTUser currentUser = null;
    WTGroup group = null;
    try {
      currentUser = (WTUser)SessionHelper.manager.getPrincipal();
      SessionHelper.manager.setAdministrator();
      DirectoryContextProvider dcp = OrganizationServicesHelper.manager.newDirectoryContextProvider((String[])null,(String[])null);
      group = OrganizationServicesHelper.manager.getGroup(groupName, dcp);
    } finally {
      if (currentUser != null) {
        SessionHelper.manager.setPrincipal(currentUser.getName());
      }
    }
    return group;
  }

  public List<WTUser> findUser(String name)
    throws WTException
  {
    if (!RemoteMethodServer.ServerFlag) {
      try {
        return (List)RemoteMethodServer.getDefault().invoke(
          "findUser", UserUtil.class.getName(), null, 
          new Class[] { String.class }, new Object[0]);
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        throw new WTException(e);
      }
    }
    WTUser currentUser = null;
    List results = new ArrayList();
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
    }
    return results;
  }

  public boolean isGroupMenber(WTPrincipal wtPrincipal, String groupName)
    throws WTException
  {
    WTGroup group = OrganizationServicesHelper.manager.getGroup(groupName);

    return (group != null) && (group.isMember(wtPrincipal));
  }

  public static String getRoleNamebyDisplayName(String displayName)
  {
    String roleName = "";
    String sql = "select  role from wtroleprincipal where name='" + displayName + "'";
    String temp = getRoleNameBySql(sql);
    if ((temp != null) || (!"".equals(temp))) {
      if ("供应商".equals(displayName))
        roleName = "RD";
      else {
        roleName = temp;
      }
    }
    return roleName;
  }

  public static String getRoleNameBySql(String sql)
  {
    boolean checkflag = SessionServerHelper.manager.setAccessEnforced(false);
    ResultSet rs = null;
    WTConnection connection = null;
    String roleName = "";
    try {
      connection = (WTConnection)getMethodContext().getConnection();
      if (connection != null) {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        rs = pstmt.executeQuery();
        while (rs.next())
          roleName = rs.getString("ROLE");
      }
    }
    catch (UnsupportedPDSException e)
    {
      e.printStackTrace();
    }
    catch (UnknownHostException e) {
      e.printStackTrace();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(checkflag);
    }
    return roleName;
  }

  public static MethodContext getMethodContext()
    throws UnsupportedPDSException, UnknownHostException
  {
    MethodContext methodcontext = null;
    try {
      methodcontext = MethodContext.getContext();
    } catch (MethodServerException methodserverexception) {
      RemoteMethodServer.ServerFlag = true;
      InetAddress inetaddress = InetAddress.getLocalHost();
      String s = inetaddress.getHostName();
      if (s == null) {
        s = inetaddress.getHostAddress();
      }
      SimpleAuthenticator simpleauthenticator = new SimpleAuthenticator();
      methodcontext = new MethodContext(s, simpleauthenticator);
      methodcontext.setThread(Thread.currentThread());
    }
    return methodcontext;
  }

  public static List<WTPrincipal> getMembersOfContainerRole(ContainerTeamManaged ctm, Role role)
  {
    if (!RemoteMethodServer.ServerFlag) {
      String method = "getMembersOfContainerRole";
      Class[] types = { ContainerTeamManaged.class, Role.class };
      Object[] vals = { ctm, role };
      try {
        return (ArrayList)RemoteMethodServer.getDefault().invoke(
          method, className, null, types, vals);
      } catch (Exception e) {
        LOGGER.debug("getMembersOfContainerRole() Exception:" + e);
        e.printStackTrace();

        return new ArrayList();
      }
    }
    List rtn = new ArrayList();
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
    return rtn;
  }

  public static String ootb2irisFullName(String ootbFullname)
    throws WTException
  {
    return ootbFullname.replace(", ", " ");
  }

  public static WTPrincipal getUserPrincipalByFullName(String ootbFullName)
    throws WTException
  {
    return getUserPrincipalByFullName(ootbFullName);
  }

  public static WTPrincipal getUserPrincipalByName(String ootbName)
    throws WTException
  {
    if ((ootbName == null) || ("".equals(ootbName.trim()))) return null;
    Enumeration enu = OrganizationServicesHelper.manager.findUser("name", ootbName);
    if (enu.hasMoreElements()) {
      WTPrincipal principal = (WTPrincipal)enu.nextElement();
      if ((!principal.isDisabled()) && (!principal.isRepairNeeded())) {
        return principal;
      }
    }
    return null;
  }

  public static <T> T getWTPrincipals(String nameOrFullName, Class<T> returnClz, boolean ootb) throws WTException {
      boolean isaccess = SessionServerHelper.manager.isAccessEnforced();
      try {
          SessionServerHelper.manager.setAccessEnforced(false);
          Map<String, WTPrincipal> map = null;
          List<WTPrincipal> list = null;

          if (returnClz.equals(Map.class)) {
              map = new HashMap<String, WTPrincipal>();
          } else if (returnClz.equals(List.class)) {
              list = new ArrayList<WTPrincipal>();
          }
          String ootbNames = ootb ? nameOrFullName : irisFullName2OOTBFullName(nameOrFullName);
          String sep = ootb ? Constant.IRIS_SEP_MULTIUSERNAME : Constant.SEP_MULTIUSERNAME;
          if (ootbNames.indexOf(sep) != -1) {//多个逗号链接的用户，表示批量查询多个
              String[] names = ootbNames.split(sep);
              for (int i = 0; i < names.length; i++) {
                  String name = names[i];
                  String irisName = ootb2irisFullName(name);
                  WTPrincipal principal = getUserPrincipalByFullName(name);
                  if (principal == null) {//如果全名找不到，拿用户名找，即工号
                      principal = getUserPrincipalByName(name);
                  }
                  if (map != null) {
                      map.put(irisName, principal);
                  }
                  if (list != null) {
                      list.add(principal);
                  }
              }
          } else {//单个用户查询
              WTPrincipal principal = getUserPrincipalByFullName(ootbNames);
              if (principal == null) {//如果全名找不到，拿用户名找，即工号
                  principal = getUserPrincipalByName(ootbNames);
              }
              if (map != null) {
                  map.put(nameOrFullName, principal);
              } else if (list != null) {
                  list.add(principal);
              } else {
                  return (T) principal;
              }
          }
          T t = null;
          if (returnClz.equals(Map.class)) {
              t = (T) map;
          } else if (returnClz.equals(List.class)) {
              t = (T) list;
          }
          return t;
      } catch (Exception e) {
          e.printStackTrace();
          throw new WTException(e);
      } finally {
          SessionServerHelper.manager.setAccessEnforced(isaccess);
      }
  }

  public static String irisFullName2OOTBFullName(String irisFullname)
    throws WTException
  {
    StringBuffer rs = null;
    if (irisFullname.endsWith(",")) {
      irisFullname = irisFullname.substring(0, irisFullname.length() - 1);
    }
    if (irisFullname.indexOf(",") != -1) {
      String[] names = irisFullname.split(",");
      for (int i = 0; i < names.length; i++) {
        String name = names[i].trim();
        if (!"".equals(name)) {
          String newname = name.replace(" ", ",");
          if (rs == null) {
            rs = new StringBuffer();
            rs.append(newname);
          } else {
            rs.append(Constant.SEP_MULTIUSERNAME + newname);
          }
        }
      }
    } else {
      return irisFullname.replace(" ", ", ");
    }
    return rs == null ? null : rs.toString();
  }

  public static OrgContainer searchOrgContainer(String orgName)
  {
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
    }
    return org;
  }

  public static boolean isOrgAdmin(WTUser user)
  {
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
    }
    return false;
  }

  public static void setProcessRoleHolder(Role role, WTPrincipal principal, ObjectReference self)
  {
    WfProcess wfProcess = (WfProcess)self.getObject();
    try {
      Team team = TeamHelper.service.getTeam(wfProcess);
      TeamHelper.service.addRolePrincipalMap(role, principal, team);
    } catch (TeamException e) {
      e.printStackTrace();
    } catch (WTException e) {
      e.printStackTrace();
    }
  }

  public static void setProcessRoleHolder1(ObjectReference self) {
    WfProcess wfProcess = (WfProcess)self.getObject();
    try {
      Team team = TeamHelper.service.getTeam(wfProcess);
      WTPrincipal currentUser = SessionHelper.manager.getPrincipal();
      Role roles = Role.toRole("LPDT");
      WTPrincipal currentUser1 = getWTUserByName("user01");
      String oid = "OR:wt.pdmlink.PDMLinkProduct:190633";
      PDMLinkProduct wc = (PDMLinkProduct)new ReferenceFactory().getReference(oid).getObject();
      getMembersOfContainerRole(wc, roles);
      setProcessRoleHolder(roles, currentUser1, self);
    }
    catch (TeamException e)
    {
      e.printStackTrace();
    } catch (WTException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * 获取系统群组的所有用户（含嵌套群组的用户）
   *
   * @param group
   * @return ArrayList<WTUser>
   */
  public static ArrayList<WTUser> getGroupMembersOfUser(WTGroup group) throws WTException {
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


  public static <T> void test()
    throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException
  {
    String oid = "OR:wt.pdmlink.PDMLinkProduct:190633";
    PDMLinkProduct wc = (PDMLinkProduct)new ReferenceFactory().getReference(oid).getObject();
    String sql = "select  role from wtroleprincipal where name='研发代表'";

    String role = getRoleNameBySql(sql);
    Role roles = null;
    if (StringUtils.isNotEmpty(role)) {
      roles = Role.toRole(role);
      System.out.println(getMembersOfContainerRole(wc, roles));

      WTPrincipal principal = getWTUserByName("user01");
      WTDocument doc = null;

      ObjectReference localObjectReference = new ObjectReference();

      localObjectReference.getObject();
      ObjectReference docRef = ObjectReference.newObjectReference(DocUtil.getDocumentByNumber("1001"));
      setProcessRoleHolder(Role.toRole(role), principal, docRef);
    }

    System.out.println(ootb2irisFullName("user01,user02,user03"));
    WTPrincipal wp = getUserPrincipalByFullName("user01");
    System.out.println(wp);
    System.out.println(getUserPrincipalByName("user01"));
    System.out.println(irisFullName2OOTBFullName("user01"));
    System.out.println(getWTPrincipals("user", WTUser.class, true));
    OrgContainer or = searchOrgContainer("PDMLinkProduct");
    System.out.println(or);
  }

  public static void main(String[] args)
    throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException
  {
    if (!RemoteMethodServer.ServerFlag)
      try {
        RemoteMethodServer.getDefault().invoke("test", 
          UserUtil.class.getName(), null, new Class[0], 
          new Object[0]);
      }
      catch (java.rmi.RemoteException e)
      {
        e.printStackTrace();
      }
  }
}