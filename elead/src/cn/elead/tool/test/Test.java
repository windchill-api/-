package cn.elead.tool.test;

import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;

public class Test implements RemoteAccess {

	public static void main(String[] args) throws Exception {

		GatewayAuthenticator auth = new GatewayAuthenticator();
		auth.setRemoteUser("wcadmin");
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setAuthenticator(auth);
		rms.invoke("getPart", Test.class.getName(), null,
				new Class[] { String.class }, new Object[] { args[0] });

	}

}
