package cn.elead.tool.wc;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.google.gwt.rpc.client.impl.RemoteException;

public class ContentUtil implements RemoteAccess {
	private static final Logger LOGGER = LogR.getLogger(ContentUtil.class
			.getName());
	/**
	 *  get AttachmentName in WTDocument or EPMDocument
	 * @author BaiJuanjuan.
	 * @param obj
	 * @return
	 * @throws WTException
	 */
	public static String getAttachmentName(WTObject obj) throws WTException {
		boolean isAccessEnforced = SessionServerHelper.manager
 				.isAccessEnforced();
				String name = "";
		try {
			if (!RemoteMethodServer.ServerFlag) {
			
					return (String) RemoteMethodServer.getDefault().invoke(
							"getAttachmentName", ContentUtil.class.getName(),
							null, new Class[] { WTObject.class},
							new Object[] { obj});

			} else{
				if (obj instanceof WTDocument) {
					WTDocument doc = (WTDocument) obj;
					QueryResult qr = ContentHelper.service.getContentsByRole(doc,
							ContentRoleType.SECONDARY);
					while (qr.hasMoreElements()) {
						ApplicationData applicationdata = (ApplicationData) qr
								.nextElement();
						String filename = applicationdata.getFileName();
						name += filename;
					}
				  }else if (obj instanceof EPMDocument) {
					EPMDocument epm = (EPMDocument) obj;
					QueryResult qr = ContentHelper.service.getContentsByRole(epm,
							ContentRoleType.SECONDARY);
					while (qr.hasMoreElements()) {
						ApplicationData applicationdata = (ApplicationData) qr
								.nextElement();
						String filename = applicationdata.getFileName();
						name += filename;
					}
				}
			}
		} catch (java.rmi.RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			SessionServerHelper.manager.setAccessEnforced(isAccessEnforced);
		}
		return name;
	}
	
	/**
	 * @author BaiJuanjuan get attachment by file name.
	 * @param fileName
	 * @param WTDocument
	 * @return
	 * @throws WTException
	 */
	public static ApplicationData getAttachmentByName(WTObject obj,
			String fileName) throws WTException, PropertyVetoException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("getAttachmentByName", ContentUtil.class.getName(),
								null,
								new Class[] { WTObject.class, String.class },
								new Object[] { obj, fileName });
				 } catch (Exception e) {
					 throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			ContentHolder contentHolder = null;
			ApplicationData applicationdata = null;
			try {
				if(obj instanceof WTDocument){
					WTDocument wtdocument = (WTDocument) obj;
					contentHolder = ContentHelper.service
							.getContents((ContentHolder) wtdocument);
				}
				if(obj instanceof EPMDocument){
					EPMDocument epm = (EPMDocument) obj;
					contentHolder = ContentHelper.service
							.getContents((ContentHolder) epm);
				}
				QueryResult qr = ContentHelper.service.getContentsByRole(
						contentHolder, ContentRoleType.SECONDARY);
				while (qr.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) qr
							.nextElement();
					String appDataName = appData.getFileName();
					if (appDataName.indexOf(fileName) >= 0) {
						applicationdata = appData;
						break;
					}
				}
			} catch (WTException e1) {
				throw new WTException(e1);
			} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return applicationdata;
		}
	}
	
	/**
	 * only get Primary Get Attachment by pre-fix name
	 * 
	 * @author BaiJuanjuan
	 * @param doc
	 * @param preFix
	 * @return
	 * @throws WTException
	 */
	public static ApplicationData getPrimaryContent(WTDocument doc,
			String preFix) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("getPrimaryContent", ContentUtil.class.getName(),
								null,
								new Class[] { WTDocument.class, String.class },
								new Object[] { doc, preFix });
			} catch (Exception e) {
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			QueryResult qr;
			try {
				qr = ContentHelper.service.getContentsByRole(doc,
						ContentRoleType.PRIMARY);
				while (qr.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) qr
							.nextElement();
					if (appData != null) {
						String fileName = appData.getFileName();
						if (fileName.startsWith(preFix)) {
							return appData;
						}
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return null;
		}
	}
	
	/**
	 * deleteContents delete all contents about the doc include PrimaryContent
	 * 
	 * @author BaiJuanjuan
	 * @param WTDocument
	 * @return WTDocument
	 * @throws WTException
	 * @throws PropertyVetoException 
	 */
	public static WTDocument deleteContents(WTDocument doc) throws WTException, PropertyVetoException {
		FormatContentHolder holder = null;
		boolean enforce = wt.session.SessionServerHelper.manager
				.setAccessEnforced(false);
	    SessionServerHelper.manager.setAccessEnforced(false);
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"deleteContents", ContentUtil.class.getName(), null,
						new Class[] { WTDocument.class }, new Object[] { doc });
			} catch (Exception e) {
				throw new WTException(e);
			}
		} else {
				holder = (FormatContentHolder) ContentHelper.service
						.getContents(doc);
				List<?> items = ContentHelper.getContentListAll(holder);
				for (int i = 0; i < items.size(); i++) {
					ContentItem item = (ContentItem) items.get(i);
					ContentServerHelper.service.deleteContent(holder, item);
				}
				holder = (FormatContentHolder) PersistenceHelper.manager
						.refresh(holder);
				SessionServerHelper.manager.setAccessEnforced(enforce);
			return (WTDocument) holder;
		}
	}
	
	
	/**
	 * updatePrimaryContent
	 * @param doc
	 * @param filePath
	 * @param fileName
	 * @return
	 * @throws WTException
	 */
	public static WTDocument updatePrimaryContent(WTDocument doc,
			String filePath, String fileName) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"updatePrimaryContent", DocUtil.class.getName(), null,
						new Class[] {WTDocument.class, String.class, String.class }, new Object[] {doc,filePath,fileName});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			ApplicationData applicationdata = ApplicationData
					.newApplicationData(doc);
			ContentHolder ch = null;
			try {
				applicationdata.setFileName(fileName);
				applicationdata.setUploadedFromPath(filePath);
				applicationdata.setRole(ContentRoleType.PRIMARY);
				ch = ContentHelper.service.getContents(doc);
				ContentServerHelper.service.deleteContent(doc,
						ContentHelper.getPrimary((FormatContentHolder) ch));
				ch = (FormatContentHolder) PersistenceHelper.manager
						.refresh(ch);
				ContentServerHelper.service.updateContent(ch, applicationdata,
						filePath);
			} catch (WTPropertyVetoException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			} catch (PropertyVetoException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			} catch (FileNotFoundException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			}finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return (WTDocument) ch;
		}
	}
	
	/**
	 * addAttachments
	 * @param holder
	 * @param filePath
	 * @param fileName
	 * @throws WTException
	 */
	public static void addAttachments(ContentHolder holder, String filePath, String fileName) throws WTException{
		try{
			QueryResult qr = ContentHelper.service.getContentsByRole(holder, ContentRoleType.SECONDARY);
			while (qr.hasMoreElements()){
				ApplicationData oAppData = (ApplicationData) qr.nextElement();
				String strFileName = oAppData.getFileName();
				if (strFileName != null && strFileName.trim().equalsIgnoreCase(fileName)){
					ContentServerHelper.service.deleteContent(holder, oAppData);
				}
			}
			addContent(holder, filePath, fileName, ContentRoleType.SECONDARY);
		} catch (Exception e){
			LOGGER.error(e.getLocalizedMessage());
			throw new WTException(e);
		}
	}
	/**
	 * 
	 * @param holder
	 * @param filePath
	 * @param fileName
	 * @param contentType
	 * @throws WTException
	 */
	private static void addContent(ContentHolder holder, String filePath, String fileName, ContentRoleType contentType) throws WTException
	{
		ApplicationData applicationData = ApplicationData.newApplicationData(holder);
		try{
			applicationData.setRole(contentType);
			applicationData = (ApplicationData) PersistenceHelper.manager.save(applicationData);
			applicationData = ContentServerHelper.service.updateContent(holder, applicationData, filePath);
			applicationData.setFileName(fileName);
			applicationData = (ApplicationData) PersistenceHelper.manager.modify(applicationData);
		} catch (IOException e){
			LOGGER.error(e.getLocalizedMessage());
			throw new WTException(e);
		} catch (PropertyVetoException e){
			LOGGER.error(e.getLocalizedMessage());
			throw new WTException(e);
		}
	}
	
	/**
	 * deleteContentByFileName
	 *  @author BaiJuanjuan
	 * @param ContentHolder
	 * @param String
	 * @return
	 * @throws WTException
	 */
	public static ContentHolder deleteContentByFileName(ContentHolder holder1,
			String fileName) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ContentHolder) RemoteMethodServer.getDefault().invoke(
						"deleteContentByFileName", ContentUtil.class.getName(), null,
						new Class[] { ContentHolder.class, String.class },
						new Object[] { holder1,fileName });
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			} else {
					boolean enforce = wt.session.SessionServerHelper.manager
								.setAccessEnforced(false);
					ContentHolder holder = holder1;
					if (fileName == null) {
						return holder;
					}
					try {
						holder = ContentHelper.service.getContents(holder);
						List<?> vContentList = ContentHelper.getContentList(holder);
						int iSize = vContentList.size();
						for (int j = 0; j < iSize; j++) {
							ContentItem contentitem = (ContentItem) vContentList.get(j);
							String strFileName = ((ApplicationData) contentitem)
									.getFileName();
							if (fileName.equals(strFileName)) {
								ContentServerHelper.service.deleteContent(holder,
										contentitem);
							}
						}
						holder = (FormatContentHolder) PersistenceHelper.manager
								.refresh(holder);
					} catch (PropertyVetoException e) {
						LOGGER.error(e.getLocalizedMessage());
						throw new WTException(e);
					} finally {
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
			}
			return holder1;
	}

}
