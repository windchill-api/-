package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.google.gwt.libideas.resources.client.CssResource.ClassName;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
/**
 * 系统组的创建， 添加/删除成员与 查找，复制组等相关操作。
 * @author zhangxj
 * @version
 *
 */
public class GroupUtil implements RemoteAccess {
	private static String CLASSNAME = GroupUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	/**
	 * 获取群组下得所有人员
	 * 
	 * @param group
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<WTUser> getGroupMembersOfUser(WTGroup group)throws WTException {
			try {
				if (!RemoteMethodServer.ServerFlag) {
						return (ArrayList<WTUser>) RemoteMethodServer.getDefault().invoke("getGroupMembersOfUser", GroupUtil.class.getName(),null, 
										new Class[] { WTGroup.class },new Object[] { group });
				} else {
					boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
					ArrayList<WTUser> memebers = new ArrayList<WTUser>();
					try {
						Enumeration member = group.members();
						while (member.hasMoreElements()) {
							WTPrincipal principal = (WTPrincipal) member.nextElement();
							if (principal instanceof WTUser)
								memebers.add((WTUser) principal);
							else if (principal instanceof WTGroup)
								memebers.addAll(getGroupMembersOfUser((WTGroup) principal));
						}
					} catch (WTException e) {
						logger.error(CLASSNAME+".getGroupMembersOfUser:"+e);
					} finally {
						SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
					}
					return memebers;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return null;
	}

	/**
	 * delteGroupByName
	 * @author zhangxj
	 * @param groupName
	 * @throws WTException
	 */
	public static void delteGroupByName(WTGroup group)  {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					RemoteMethodServer.getDefault().invoke("delteGroupByName",GroupUtil.class.getName(), null,
							new Class[] { WTGroup.class }, new Object[] { group });
			} else {
				boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
				try {
					SessionServerHelper.manager.setAccessEnforced(false);
					OrganizationServicesHelper.manager.delete(group);
				} catch (WTException e) {
					logger.error(CLASSNAME+".delteGroupByName:"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
				}
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	}

	/**
	 * 给组中添加用户
	 * 
	 * @param group
	 * @param role
	 * @throws WTException
	 */
	public static void addGroupSystem(WTGroup group, WTUser wtUser) {
			try {
				if (!RemoteMethodServer.ServerFlag) {
						RemoteMethodServer.getDefault().invoke("addGroupSystem",GroupUtil.class.getName(), null,
								new Class[] { WTGroup.class, WTUser.class },new Object[] { group, wtUser });
				} else {
					boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
					try {
						group.addMember(wtUser);
					} catch (WTException e) {
						logger.error(CLASSNAME+".addGroupSystem:"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
					}
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
	}

	/**
	 * isGroupMember 判断用戶是否在群組
	 * 
	 * @param user
	 * @param group
	 * @return
	 * @throws WTException
	 */
	public static boolean isGroupMember(WTUser user, WTGroup group) {
			try {
				if (!RemoteMethodServer.ServerFlag) {
						RemoteMethodServer.getDefault().invoke("isGroupMember",GroupUtil.class.getName(), null,
								new Class[] { WTUser.class, WTGroup.class },new Object[] { user, group });
				} else {
					boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
						boolean flag = false;
						try {
							Enumeration enu = group.members();
							if (enu == null)
								return false;
							while (enu.hasMoreElements()) {
								Object obj = (Object) enu.nextElement();
								if (obj != null) {
									if (obj instanceof WTUser) {
										WTUser tempUser = (WTUser) obj;
										if (user.equals(tempUser)) {
											flag = true;
											break;
										}
									} else if (obj instanceof WTGroup) {
										WTGroup tempGroup = null;
										tempGroup = (WTGroup) obj;
										isGroupMember(user, tempGroup);
									}
								}
							}
						} catch (WTException e) {
							logger.error(CLASSNAME+".isGroupMember:"+e);
						}finally{
							SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
						}
						return flag;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return false;
	}

	/**
	 * 精确查找系统非站点下组
	 *
	 * @param groupName
	 * @return ArrayList<WTGroup>
	 */
	public static WTGroup getNonSiteGroupByName(String groupName) {
			try {
				if (!RemoteMethodServer.ServerFlag) {
						RemoteMethodServer.getDefault().invoke("getNonSiteGroupByName", GroupUtil.class.getName(), null,
								new Class[] { String.class }, new Object[] { groupName });
				} else {
					boolean isAccessEnforced = SessionServerHelper.manager.isAccessEnforced();
					WTGroup group = null;
					try {
						if ((groupName == null) || (groupName.trim().isEmpty()))
							return null;
						QuerySpec qs = new QuerySpec(WTGroup.class);
						int[] indx0 = new int[] { 0 };
						SearchCondition sc = new SearchCondition(WTGroup.class,WTGroup.NAME, SearchCondition.EQUAL, groupName, false);
						qs.appendWhere(sc, indx0);
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(WTGroup.class,WTGroup.INTERNAL, SearchCondition.IS_FALSE), indx0);
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(WTGroup.class,WTGroup.DISABLED, SearchCondition.IS_FALSE), indx0);
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(WTGroup.class,"containerReference.key.classname",SearchCondition.EQUAL,
								"wt.inf.container.OrgContainer"), indx0);
						QueryResult qr = PersistenceHelper.manager .find((StatementSpec) qs);
						if (qr.hasMoreElements()) {
							WTGroup wtGroup = (WTGroup) qr.nextElement();
							if (!(wtGroup instanceof WTOrganization))
								group = wtGroup;
						}
					} catch (WTException e) {
						logger.error(CLASSNAME+".getNonSiteGroupByName:"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
					}
					return group;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return null;
	}

	/**
	 * 获取群组
	 * 
	 * @param groupName
	 * @return
	 * @throws WTException
	 */
	public static ArrayList<WTGroup> fuzzySearchNonSiteGroupsByName(String groupName){
			try {
				if (!RemoteMethodServer.ServerFlag) {
						RemoteMethodServer.getDefault().invoke("fuzzySearchNonSiteGroupsByName",GroupUtil.class.getName(), null,
								new Class[] { String.class },new Object[] { groupName });
				}else{
					boolean flagAccess = SessionServerHelper .manager. setAccessEnforced(false);
					ArrayList<WTGroup> list = new ArrayList<WTGroup>();
					try {
						QuerySpec qs = new QuerySpec(WTGroup.class);
						int[] indx0 = new int[] { 0 };
						SearchCondition sc = new SearchCondition(WTGroup.class,WTGroup.NAME, SearchCondition.LIKE, "%" + groupName + "%",false);
						qs.appendWhere(sc, indx0);
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(WTGroup.class, WTGroup.INTERNAL,SearchCondition.IS_FALSE), indx0);
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(WTGroup.class, WTGroup.DISABLED,SearchCondition.IS_FALSE), indx0);
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(WTGroup.class,"containerReference.key.classname", SearchCondition.EQUAL,
								"wt.inf.container" + ".OrgContainer"), indx0);
						QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
						while (qr.hasMoreElements()) {
							WTGroup wtGroup = (WTGroup) qr.nextElement();
							if (!(wtGroup instanceof WTOrganization))
								list.add(wtGroup);
						}
					} catch (WTException e) {
						logger.error(CLASSNAME+".fuzzySearchNonSiteGroupsByName:"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(flagAccess);
					}
					return list;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return null;
	}

	/**
	 * createGroup
	 * @param group_name
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static WTGroup createGroup(String group_name, String path)throws WTPropertyVetoException {
			try {
				if (!RemoteMethodServer.ServerFlag) {
						RemoteMethodServer.getDefault().invoke("createGroup",GroupUtil.class.getName(), null,
								new Class[] { String.class, String.class },new Object[] { group_name, path });
				}else{
					boolean flagAccess = SessionServerHelper.manager.setAccessEnforced(false);
					WTGroup group =null;
					try {
						group = WTGroup.newWTGroup(group_name);
						group.setDn("cn="+ group_name+ ",cn=Public,o=huaqin,ou=people,cn=AdministrativeLdap,cn=Windchill_10.2,o=huaqin");
						WTContainer wtcontainer = WTContainerHelper.service.getByPath(path).getContainer();
						group.setContainer(wtcontainer);
						OrganizationServicesHelper.manager.createPrincipal(group);
					} catch (WTException e) {
						logger.error(CLASSNAME+".createGroup:"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(flagAccess);
					}
					return group;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return null;
	}
}
