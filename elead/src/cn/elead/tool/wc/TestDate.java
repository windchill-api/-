package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import com.ptc.cipjava.intdict;
import com.ptc.windchill.uwgm.common.prefs.res.newCadDocPrefsResource;

import nitidus.util.xml.ser.test;
import wt.doc.WTDocument;
import wt.epm.navigator.filter.ClassAttribute;
import wt.fc.collections.WTArrayList;
import wt.maturity.PromotionNotice;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.util.WTException;

public class TestDate implements RemoteAccess{

	  public static   void selectPartTest() throws QueryException{
		  QuerySpec querySpec = new QuerySpec();    
		  int a = querySpec.addClassList(WTPartMaster.class, false);
		  int  b = querySpec.addClassList(WTPartMaster.class, true);
		  System.out.println("<><><><><><><><><><>");
		  System.out.println("a>>>"+a);
		  System.out.println("b>>>"+b);
		  System.out.println("!@!@!@!@!@!@!@!@@!@!@");
	  }
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException {
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", TestDate.class.getName(), null, new Class[] {}, new Object[] {});
		}
	}
	public static void test() throws WTException{
//		selectPartTest();
//	System.out.println(DocUtil.getDocument("", "", "INWORK"));	
		WTDocument wtDocument = DocUtil.getDocumentByNumber("CPJY00000542");
		WTPart wtPart =PartUtil.getPartByNumber("HQ11120003000");
//	    boolean falg =DocUtil. documentRename("",wtDocument);
//	    System.out.println(falg);
//			DocUtil.reviseWTDocument(wtDocument,"AAAA");
//			System.out.println(DocUtil.getDocumentByNumberSuffix("ck012","*544"));
//			System.out.println(DocUtil.getDocumentByNamePrefix("ck*"));
		PromotionNotice pNotice=	PromotionUtil.getPromotionNoticeByNumber("00061");
//			System.out.println(PromotionUtil.getPromotionNoticeByNumber("00008"));
//			WTArrayList arrylist = new WTArrayList();
//			 arrylist.add(wtPart);
//			PromotionUtil. addPromotable(pNotice,arrylist);
//			PromotionUtil.renamePromotion("ceshishengji",pNotice)
//			PromotionUtil.findRelatedPromotion(wtDocument);
//			System.out.println(PromotionUtil.findRelatedPromotion(wtDocument));
			WTPartMaster wtPartMaster=PartUtil.getPartMasterByNumber("HQ11120003000");
			VersionUtil.getLatestObject(wtPartMaster);
			System.out.println(VersionUtil.getLatestObject(wtPartMaster));
			
			VersionUtil.getLatestByMaster(wtPartMaster);
			System.out.println(VersionUtil.getLatestByMaster(wtPartMaster)+"@@@@@@@@@@@");
			
	}
}
