package cn.elead.tool.wc;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.State;
import wt.maturity.MaturityBaseline;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.baseline.BaselineHelper;

import com.ptc.core.lwc.server.PersistableAdapter;
import com.ptc.core.meta.common.UpdateOperationIdentifier;

public class PromotionUtil
{
  public PromotionNotice createPromotionRequest(String number, String name, String objType, String desciption, WTContainer container, Folder folder, String targetState, Map<String, Object> mbaMap)
    throws WTException
  {
    PromotionNotice promotion = null;
    try {
      promotion = PromotionNotice.newPromotionNotice(name);

      if (StringUtils.isNotBlank(number)) {
        promotion.setNumber(number);
      }

      promotion.setName(name);

      if (StringUtils.isNotBlank(objType)) {
        TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(objType);
        promotion.setTypeDefinitionReference(typeDefinitionRef);
      }

      promotion.setContainer(container);

      if (StringUtils.isNotBlank(desciption)) {
        promotion.setDescription(desciption);
      }

      String folderpath = "";
      if (folder == null) {
        folderpath = "/Default";
        folder = getFolder(getWTContainerref(container), folderpath);
      }
      if (folder != null) {
        FolderHelper.assignLocation(promotion, folder);
      }

      if (StringUtils.isNotBlank(targetState)) {
        promotion.setMaturityState(State.toState(targetState));
      }

      MaturityBaseline maturityBaseline = MaturityBaseline.newMaturityBaseline();
      maturityBaseline.setContainer(container);
      maturityBaseline = (MaturityBaseline)PersistenceHelper.manager.save(maturityBaseline);
      promotion.setConfiguration(maturityBaseline);
      promotion = MaturityHelper.service.savePromotionNotice(promotion);
      promotion = (PromotionNotice)PersistenceHelper.manager.refresh(promotion);
      if (mbaMap != null) {
        PromotionUtil.setObjectValue(promotion, mbaMap);
      }
    } catch (Exception e) {
      throw new WTException(e, e.getLocalizedMessage());
    }
    return promotion;
  }
  public static void setObjectValue(Persistable per, Map<String, Object> dataMap) throws WTException {
      PersistableAdapter obj = new PersistableAdapter(per, null, SessionHelper.getLocale(), new UpdateOperationIdentifier());
      Iterator<String> keyIt = dataMap.keySet().iterator();
      String key = null;
      obj.load(dataMap.keySet());
      while (keyIt.hasNext()) {
          key = keyIt.next();
          obj.set(key, dataMap.get(key));
      }
      obj.apply();
      PersistenceServerHelper.manager.update(per);
  }

  public PromotionNotice addPromotable(PromotionNotice promotion, WTArrayList promotableList)
    throws WTException
  {
    boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
    try {
      MaturityBaseline maturityBaseline = null;
      maturityBaseline = promotion.getConfiguration();
      maturityBaseline = (MaturityBaseline)BaselineHelper.service.addToBaseline(promotableList, maturityBaseline);
      promotion.setConfiguration(maturityBaseline);
      PersistenceHelper.manager.save(promotion);

      WTSet promotableSet = new WTHashSet();
      promotableSet.addAll(promotableList);
      MaturityHelper.service.savePromotionTargets(promotion, promotableSet);

      promotion = (PromotionNotice)PersistenceHelper.manager.refresh(promotion);
    } catch (WTPropertyVetoException e) {
      throw new WTException(e, e.getLocalizedMessage());
    } catch (WTException e) {
      throw new WTException(e, e.getLocalizedMessage());
    } finally {
      SessionServerHelper.manager.setAccessEnforced(enforce);
    }
    return promotion;
  }

  public WTContainerRef getWTContainerref(WTContainer wtcontainer) throws WTException
  {
    return WTContainerRef.newWTContainerRef(wtcontainer);
  }

  public Folder getFolder(WTContainerRef containerRef, String folder1)
    throws WTException
  {
    String folder = folder1;
    if (!folder.startsWith("/")) {
      folder = "/" + folder;
    }
    if (folder.indexOf("Default") == -1) {
      folder = "/Default" + folder;
    }
    return FolderHelper.service.getFolder(folder, containerRef);
  }
}