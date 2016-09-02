package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;

import com.google.gwt.rpc.client.impl.RemoteException;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.OrganizationServicesManager;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.util.WTException;

public class GroupUtil implements RemoteAccess{
				
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
				server.invoke("test", GroupUtil.class.getName(), null,
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
	
	
	public  static void test() throws WTException{
		WTGroup npiMaterialAdminGroup=	getSystemGroup("NPI资料管理员组");
		System.out.println(npiMaterialAdminGroup);
	}
	 /**
     * 查询系统中的群组
     *
     * @param groupName
     * @return WTGroup
     */
    public static WTGroup getSystemGroup(String groupName) throws WTException {
        OrganizationServicesManager orgServiceMng = OrganizationServicesHelper.manager;
        String[] services = orgServiceMng.getDirectoryServiceNames();
        // for site groups:
        DirectoryContextProvider dc_provider = orgServiceMng.newDirectoryContextProvider(services, null);
        return OrganizationServicesHelper.manager.getGroup(groupName, dc_provider);
    }
    
    
    
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
}
