package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import wt.change2.ChangeActivity2;
import wt.change2.ChangeHelper2;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.team.Team;
import wt.team.TeamException;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.util.WTException;

public class TeamUtil implements RemoteAccess, Serializable {

	/**
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = TeamUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	
	public static void getPromotionTeam(WTObject object) throws TeamException,
		WTException {
		// NmOid nmOids = new NmOid("OR:wt.maturity.PromotionNotice:174527");
		// WTObject object22 = (WTObject) nmOids.getRefObject();
		// TeamCopyUtil.getPromotionTeam(object22);
		if (object != null && object instanceof PromotionNotice) {
			PromotionNotice promotionNotice = (PromotionNotice) object;
			Team promotionNoticeTeam = TeamHelper.service
					.getTeam(promotionNotice);
			List<Object> objList = getPromotionObjects(promotionNotice);
			if (objList.size() > 0) {
				for (Object obj : objList) {
					if (obj != null && obj instanceof TeamManaged) {
						TeamManaged teamManaged = (TeamManaged) obj;
						//copyTeam(promotionNoticeTeam, teamManaged);
					}
				}
			}
		} else if (object != null && object instanceof ChangeActivity2) {
			ChangeActivity2 changeActivity2 = (ChangeActivity2) object;
			Team ecaTeam = TeamHelper.service
					.getTeam(changeActivity2);
			/*List<Object> objList = getPromotionObjects(changeActivity2);
			if (objList.size() > 0) {
				for (Object obj : objList) {
					if (obj != null && obj instanceof TeamManaged) {
						TeamManaged teamManaged = (TeamManaged) obj;
						//copyTeam(ecaTeam, teamManaged);
					}
				}
			}*/
		}
	}
	
	/**get Team by ECA
	 * 
	 * @param eca
	 * @return
	 */
	public static Team getTeamByECA(ChangeActivity2 eca){
		Team ecaTeam = null;
		if (eca != null) {
			try {
				ecaTeam = TeamHelper.service.getTeam(eca);
			} catch (TeamException e) {
				logger.error(">>>>>"+e);
			} catch (WTException e) {
				logger.error(">>>>>"+e);
			}
		}
		return ecaTeam;
	}
	
	public static List<Object> getPromotionObjects(Object object)
			throws MaturityException, WTException {
		List<Object> objList = new ArrayList<Object>();
		if (object != null && object instanceof PromotionNotice) {
			PromotionNotice promotionNotice = (PromotionNotice) object;
			QueryResult qr = MaturityHelper.service
					.getPromotionTargets(promotionNotice);
			while (qr.hasMoreElements()) {
				objList.add(qr.nextElement());
			}
		} else if (object != null && object instanceof ChangeActivity2) {
			ChangeActivity2 changeActivity2 = (ChangeActivity2) object;
			// EC请求对象
			QueryResult qrAfter = ChangeHelper2.service
					.getChangeablesAfter(changeActivity2);
			// EC受影响对象
			QueryResult qrBefore = ChangeHelper2.service
					.getChangeablesBefore(changeActivity2);
			while (qrAfter.hasMoreElements()) {
				objList.add(qrAfter.nextElement());
			}
			while (qrBefore.hasMoreElements()) {
				objList.add(qrBefore.nextElement());
			}
		}
		return objList;
	}
	
	public static void test() throws RemoteException, InvocationTargetException, WTException{
		/*List<Object> list = getPromotionObjects(ChangeUtil.getECAByNumber("00021"));
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}*/
		System.out.println(getTeamByECA(ChangeUtil.getECAByNumber("00021")));
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", TeamUtil.class.getName(), null,
					new Class[] {},
					new Object[] {});
		}
	}
	
}
