package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;

public class TestRemote implements RemoteAccess, Serializable {
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = TestRemote.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	public static void testRemoteLog4J(){
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("testRemoteLog4J", TestRemote.class.getName(), null,
						new Class[] {},
						new Object[] {});
			} else {
				logger.debug("Enter testRemoteLog4J......");
				System.out.println("----------------------SYSO");
			}
		}catch (RemoteException re) {
			//re.printStackTrace();
			logger.error(re.getMessage(),re);
		}catch (InvocationTargetException ine){
			logger.error(ine.getMessage(),ine);
			//ine.printStackTrace();
		}
	}
	public static void main(String[] args) {
		TestRemote.testRemoteLog4J();
	}
}
