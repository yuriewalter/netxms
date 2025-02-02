/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2024 Raden Solutions
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.netxms.nxmc.modules.objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.AbstractNode;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.Asset;
import org.netxms.client.objects.Chassis;
import org.netxms.client.objects.Cluster;
import org.netxms.client.objects.Container;
import org.netxms.client.objects.Dashboard;
import org.netxms.client.objects.DashboardGroup;
import org.netxms.client.objects.DashboardRoot;
import org.netxms.client.objects.DataCollectionTarget;
import org.netxms.client.objects.Node;
import org.netxms.client.objects.Rack;
import org.netxms.client.objects.ServiceRoot;
import org.netxms.client.objects.Subnet;
import org.netxms.client.objects.Template;
import org.netxms.nxmc.Registry;
import org.netxms.nxmc.base.jobs.Job;
import org.netxms.nxmc.base.views.View;
import org.netxms.nxmc.base.views.ViewPlacement;
import org.netxms.nxmc.base.widgets.helpers.MenuContributionItem;
import org.netxms.nxmc.localization.LocalizationHelper;
import org.netxms.nxmc.modules.agentmanagement.PackageDeployment;
import org.netxms.nxmc.modules.agentmanagement.SendUserAgentNotificationAction;
import org.netxms.nxmc.modules.agentmanagement.dialogs.PackageSelectionDialog;
import org.netxms.nxmc.modules.agentmanagement.views.AgentConfigurationEditor;
import org.netxms.nxmc.modules.agentmanagement.views.PackageDeploymentMonitor;
import org.netxms.nxmc.modules.assetmanagement.LinkAssetToObjectAction;
import org.netxms.nxmc.modules.assetmanagement.LinkObjectToAssetAction;
import org.netxms.nxmc.modules.assetmanagement.UnlinkAssetFromObjectAction;
import org.netxms.nxmc.modules.assetmanagement.UnlinkObjectFromAssetAction;
import org.netxms.nxmc.modules.dashboards.CloneDashboardAction;
import org.netxms.nxmc.modules.dashboards.ExportDashboardAction;
import org.netxms.nxmc.modules.dashboards.ImportDashboardAction;
import org.netxms.nxmc.modules.filemanager.UploadFileToAgent;
import org.netxms.nxmc.modules.networkmaps.views.IPTopologyMapView;
import org.netxms.nxmc.modules.networkmaps.views.InternalTopologyMapView;
import org.netxms.nxmc.modules.networkmaps.views.L2TopologyMapView;
import org.netxms.nxmc.modules.nxsl.views.ScriptExecutorView;
import org.netxms.nxmc.modules.objects.actions.ChangeInterfaceExpectedStateAction;
import org.netxms.nxmc.modules.objects.actions.ChangeZoneAction;
import org.netxms.nxmc.modules.objects.actions.CloneNetworkMap;
import org.netxms.nxmc.modules.objects.actions.CreateInterfaceDciAction;
import org.netxms.nxmc.modules.objects.actions.ForcedPolicyDeploymentAction;
import org.netxms.nxmc.modules.objects.actions.ObjectAction;
import org.netxms.nxmc.modules.objects.dialogs.ObjectSelectionDialog;
import org.netxms.nxmc.modules.objects.dialogs.RelatedObjectSelectionDialog;
import org.netxms.nxmc.modules.objects.dialogs.RelatedObjectSelectionDialog.RelationType;
import org.netxms.nxmc.modules.objects.dialogs.RelatedTemplateObjectSelectionDialog;
import org.netxms.nxmc.modules.objects.views.ObjectView;
import org.netxms.nxmc.modules.objects.views.RouteView;
import org.netxms.nxmc.modules.objects.views.ScreenshotView;
import org.netxms.nxmc.resources.ResourceManager;
import org.netxms.nxmc.resources.SharedIcons;
import org.netxms.nxmc.services.ObjectActionDescriptor;
import org.netxms.nxmc.tools.MessageDialogHelper;
import org.xnap.commons.i18n.I18n;

/**
 * Helper class for building object context menu
 */
public class ObjectContextMenuManager extends MenuManager
{
   private final I18n i18n = LocalizationHelper.getI18n(ObjectContextMenuManager.class);

   private View view;
   private ISelectionProvider selectionProvider;
   private ColumnViewer objectViewer;
   private Action actionManage;
   private Action actionUnmanage;
   private Action actionRename;
   private Action actionDelete;
   private Action actionDeployPackage;
   private Action actionProperties;
   private Action actionTakeScreenshot;
   private Action actionEditAgentConfig;
   private Action actionExecuteScript;
   private Action actionBind;
   private Action actionUnbind;
   private Action actionBindTo;
   private Action actionUnbindFrom;
   private Action actionApplyTemplate;
   private Action actionRemoveTemplate;
   private Action actionApplyNodeTemplate;
   private Action actionRemoveObjectsTemplate;
   private Action actionAddNode;
   private Action actionRemoveNode;
   private Action actionRouteFrom;
   private Action actionRouteTo;
   private Action actionLayer2Topology;
   private Action actionIPTopology;
   private Action actionInternalTopology;
   private ObjectAction<?> actionCreateInterfaceDCI;
   private ObjectAction<?> actionForcePolicyInstall;
   private ObjectAction<?> actionLinkAssetToObject;
   private ObjectAction<?> actionUnlinkAssetFromObject;
   private ObjectAction<?> actionLinkObjectToAsset;
   private ObjectAction<?> actionUnlinkObjectFromAsset;
   private ObjectAction<?> actionCloneDashboard;
   private ObjectAction<?> actionChangeZone;
   private ObjectAction<?> actionSendUserAgentNotification;
   private ObjectAction<?> actionExportDashboard;
   private ObjectAction<?> actionImportDashboard;
   private ObjectAction<?> actionCloneNetworkMap;
   private ObjectAction<?> actionChangeInterfaceExpectedState;
   private ObjectAction<?> actionUploadFileToAgent;
   private List<ObjectAction<?>> actionContributions = new ArrayList<>();

   /**
    * Create new object context menu manager.
    *
    * @param view owning view
    * @param selectionProvider selection provider
    */
   public ObjectContextMenuManager(View view, ISelectionProvider selectionProvider, ColumnViewer objectViewer)
   {
      this.view = view;
      this.selectionProvider = selectionProvider;
      this.objectViewer = objectViewer;
      setRemoveAllWhenShown(true);
      addMenuListener(new IMenuListener() {
         public void menuAboutToShow(IMenuManager mgr)
         {
            fillContextMenu();
         }
      });
      createActions();
   }

   /**
    * Create object actions
    */
   private void createActions()
   {
      actionManage = new Action(i18n.tr("&Manage")) {
         @Override
         public void run()
         {
            changeObjectManagementState(true);
         }
      };

      actionUnmanage = new Action(i18n.tr("&Unmanage")) {
         @Override
         public void run()
         {
            changeObjectManagementState(false);
         }
      };
      
      actionDeployPackage = new Action(i18n.tr("D&eploy package...")) {
         @Override
         public void run()
         {
            deployPackage();
         }
      };

      if (objectViewer != null)
      {
         actionRename = new Action(i18n.tr("Rename")) {
            @Override
            public void run()
            {
               Object element = null;
               Control control = objectViewer.getControl();
               if (control instanceof Tree)
               {
                  TreeItem[] selection = ((Tree)control).getSelection();
                  if (selection.length != 1)
                     return;
                  element = selection[0].getData();
               }
               else if (control instanceof Table)
               {
                  TableItem[] selection = ((Table)control).getSelection();
                  if (selection.length != 1)
                     return;
                  element = selection[0].getData();
               }
               if (element != null)
               {
                  objectViewer.editElement(element, 0);
               }
            }
         };
         view.addKeyBinding("F2", actionRename);
      }

      actionDelete = new Action(i18n.tr("&Delete"), SharedIcons.DELETE_OBJECT) {
         @Override
         public void run()
         {
            deleteObject();
         }
      };

      actionProperties = new Action(i18n.tr("&Properties..."), SharedIcons.PROPERTIES) {
         @Override
         public void run()
         {
            ObjectPropertiesManager.openObjectPropertiesDialog(getObjectFromSelection(), getShell(), view);
         }
      };

      actionTakeScreenshot = new Action(i18n.tr("&Take screenshot"), ResourceManager.getImageDescriptor("icons/screenshot.png")) {
         @Override
         public void run()
         {
            openScreenshotView();
         }
      };

      actionEditAgentConfig = new Action(i18n.tr("Edit agent configuration"), ResourceManager.getImageDescriptor("icons/object-views/agent-config.png")) {
         @Override
         public void run()
         {
            openAgentConfigEditor();
         }
      };

      actionExecuteScript = new Action(i18n.tr("E&xecute script"), ResourceManager.getImageDescriptor("icons/object-views/script-executor.png")) {
         @Override
         public void run()
         {
            executeScript();
         }
      };

      actionBind = new Action(i18n.tr("&Bind...")) {
         @Override
         public void run()
         {
            bindObjects();
         }
      };

      actionUnbind = new Action(i18n.tr("U&nbind...")) {
         @Override
         public void run()
         {
            unbindObjects();
         }
      };

      actionBindTo = new Action(i18n.tr("&Bind to container...")) {
         @Override
         public void run()
         {
            bindToObject();
         }
      };

      actionUnbindFrom = new Action(i18n.tr("U&nbind from container...")) {
         @Override
         public void run()
         {
            unbindFromObjects();
         }
      };

      actionApplyTemplate = new Action(i18n.tr("&Apply to...")) {
         @Override
         public void run()
         {
            bindObjects();
         }
      };

      actionRemoveTemplate = new Action(i18n.tr("&Remove from...")) {
         @Override
         public void run()
         {
            removeTemplate();
         }
      };

      actionApplyNodeTemplate = new Action(i18n.tr("&Apply template...")) {
         @Override
         public void run()
         {
            selectAndApplyTemplate();
         }
      };

      actionRemoveObjectsTemplate = new Action(i18n.tr("&Remove template...")) {
         @Override
         public void run()
         {
            selectTemplateToRemove();
         }
      };

      actionAddNode = new Action(i18n.tr("&Add node...")) {
         @Override
         public void run()
         {
            addNodeToCluster();
         }
      };

      actionRemoveNode = new Action(i18n.tr("&Remove node...")) {
         @Override
         public void run()
         {
            removeNodeFromCluster();
         }
      };

      actionRouteFrom = new Action(i18n.tr("Route from...")) {
         @Override
         public void run()
         {
            showRoute(true);
         }
      };

      actionRouteTo = new Action(i18n.tr("Route to...")) {
         @Override
         public void run()
         {
            showRoute(false);
         }
      };

      actionLayer2Topology = new Action(i18n.tr("&Layer 2 topology")) {
         @Override
         public void run()
         {
            view.openView(new L2TopologyMapView(getObjectIdFromSelection()));
         }
      };

      actionIPTopology = new Action(i18n.tr("&IP topology")) {
         @Override
         public void run()
         {
            view.openView(new IPTopologyMapView(getObjectIdFromSelection()));
         }
      };

      actionInternalTopology = new Action(i18n.tr("Internal &communication topology")) {
         @Override
         public void run()
         {
            view.openView(new InternalTopologyMapView(getObjectIdFromSelection()));
         }
      };

      ViewPlacement viewPlacement = new ViewPlacement(view);
      actionCreateInterfaceDCI = new CreateInterfaceDciAction(viewPlacement, selectionProvider);
      actionForcePolicyInstall = new ForcedPolicyDeploymentAction(viewPlacement, selectionProvider);
      actionLinkAssetToObject = new LinkAssetToObjectAction(viewPlacement, selectionProvider);
      actionUnlinkAssetFromObject = new UnlinkAssetFromObjectAction(viewPlacement, selectionProvider);
      actionLinkObjectToAsset = new LinkObjectToAssetAction(viewPlacement, selectionProvider);
      actionUnlinkObjectFromAsset = new UnlinkObjectFromAssetAction(viewPlacement, selectionProvider);
      actionCloneDashboard = new CloneDashboardAction(viewPlacement, selectionProvider);
      actionChangeZone = new ChangeZoneAction(viewPlacement, selectionProvider);
      actionSendUserAgentNotification = new SendUserAgentNotificationAction(viewPlacement, selectionProvider);
      actionExportDashboard = new ExportDashboardAction(viewPlacement, selectionProvider);
      actionImportDashboard = new ImportDashboardAction(viewPlacement, selectionProvider);
      actionCloneNetworkMap = new CloneNetworkMap(viewPlacement, selectionProvider);
      actionChangeInterfaceExpectedState = new ChangeInterfaceExpectedStateAction(viewPlacement, selectionProvider);
      actionUploadFileToAgent = new UploadFileToAgent(viewPlacement, selectionProvider);

      NXCSession session = Registry.getSession();
      ServiceLoader<ObjectActionDescriptor> actionLoader = ServiceLoader.load(ObjectActionDescriptor.class, getClass().getClassLoader());
      for(ObjectActionDescriptor a : actionLoader)
      {
         String componentId = a.getRequiredComponentId();
         if ((componentId == null) || session.isServerComponentRegistered(componentId))
            actionContributions.add(a.createAction(viewPlacement, selectionProvider));
      }
   }

   /**
    * Fill object context menu
    */
   protected void fillContextMenu()
   {
      IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
      if (selection.isEmpty())
         return;

      boolean singleObject = (selection.size() == 1);

      if (singleObject)
      {
         AbstractObject object = getObjectFromSelection();
         MenuManager createMenu = new ObjectCreateMenuManager(getShell(), view, object);
         if (!createMenu.isEmpty())
         {
            add(createMenu);
            if ((object instanceof DashboardGroup) || (object instanceof DashboardRoot))
               add(actionImportDashboard);
            add(new Separator());
         }
         if (object instanceof Asset)
         {
            add(actionLinkAssetToObject);
            add(actionUnlinkAssetFromObject);
            add(new Separator());
         }
         if ((object instanceof Container) || (object instanceof ServiceRoot))
         {
            add(actionBind);
            add(actionUnbind);
            add(new Separator());
         }
         if (object instanceof Template)
         {
            add(actionApplyTemplate);
            add(actionRemoveTemplate);
            add(actionForcePolicyInstall);
            add(new Separator());
         }
         if (object instanceof Cluster)
         {
            add(actionAddNode);
            add(actionRemoveNode);
            add(new Separator());
         }
         if (((object instanceof Rack) || (object instanceof DataCollectionTarget)) && !(object instanceof Cluster))
         {
            add(actionLinkObjectToAsset);
            if (object.getAssetId() != 0)
               add(actionUnlinkObjectFromAsset);
            add(new Separator());            
         }
         if (object instanceof Dashboard)
         {
            add(actionCloneDashboard);
            add(actionExportDashboard);
            add(new Separator());           
         }
      }
      else
      {
         if (actionUnlinkAssetFromObject.isValidForSelection(selection))
         {
            add(actionUnlinkAssetFromObject);
            add(new Separator());
         }
         else if (actionUnlinkObjectFromAsset.isValidForSelection(selection))
         {
            add(actionUnlinkObjectFromAsset);
            add(new Separator());
         }

         if (actionForcePolicyInstall.isValidForSelection(selection))
         {
            add(actionForcePolicyInstall);
            add(new Separator());
         }         
      }
      if (isBindToMenuAllowed(selection))
      {
         add(actionBindTo);
         if (singleObject)
            add(actionUnbindFrom);
         add(new Separator());
      }
      if (isTemplateManagementAllowed(selection))
      {
         add(actionApplyNodeTemplate);
         add(actionRemoveObjectsTemplate);
         add(new Separator());
      }
      if (isMaintenanceMenuAllowed(selection))
      {
         MenuManager maintenanceMenu = new MaintenanceMenuManager(view, selectionProvider);
         if (!maintenanceMenu.isEmpty())
         {
            add(maintenanceMenu);
         }
      }
      if (isManagedMenuAllowed(selection))
      {
         add(actionManage);
         add(actionUnmanage);
      }
      if (singleObject && (objectViewer != null))
      {
         add(actionRename);
      }
      if (isDeleteMenuAllowed(selection))
      {
         add(actionDelete);
      }
      if (singleObject && isChangeZoneMenuAllowed(selection))
      {
         add(actionChangeZone);
      }
      add(new Separator());
      
      if (isSendUANotificationMenuAllowed(selection))
      {
         add(actionSendUserAgentNotification);
      }

      // Agent management
      if (singleObject)
      {
         AbstractObject object = getObjectFromSelection();
         if ((object instanceof Node) && ((Node)object).hasAgent())
         {
            add(actionEditAgentConfig);
         }
      }

      // Package management
      boolean nodesWithAgent = false;
      for(Object o : selection.toList())
      {
         if (o instanceof Node)
         {
            if (((Node)o).hasAgent())
            {
               nodesWithAgent = true;
               break;
            }
         }
         else
         {
            for(AbstractObject n : ((AbstractObject)o).getAllChildren(AbstractObject.OBJECT_NODE))
            {
               if (((Node)n).hasAgent())
               {
                  nodesWithAgent = true;
                  break;
               }
            }
            if (nodesWithAgent)
               break;
         }
      }
      if (nodesWithAgent)
         add(actionDeployPackage);
      
      if (actionUploadFileToAgent.isValidForSelection(selection))
      {
         add(actionUploadFileToAgent);
      }

      // Screenshots, etc. for single node
      if (singleObject)
      {
         add(new Separator());
         AbstractObject object = getObjectFromSelection();
         if ((object instanceof Node) && ((Node)object).hasAgent() && ((Node)object).getPlatformName().startsWith("windows-"))
         {
            add(actionTakeScreenshot);
         }
         if ((object instanceof Node) && (((Node)object).getPrimaryIP().isValidUnicastAddress() || ((Node)object).isManagementServer()))
         {
            add(actionRouteFrom);
            add(actionRouteTo);
            MenuManager topologyMapMenu = new MenuManager(i18n.tr("Topology maps"));
            topologyMapMenu.add(actionLayer2Topology);
            topologyMapMenu.add(actionIPTopology);
            topologyMapMenu.add(actionInternalTopology);
            add(topologyMapMenu);
         }
         add(new Separator());
         add(actionExecuteScript);
      }

      long contextId = (view instanceof ObjectView) ? ((ObjectView)view).getObjectId() : 0;
      if (singleObject)
      {
         AbstractObject object = getObjectFromSelection();
         MenuManager logMenu = new ObjectLogMenuManager(object, contextId, new ViewPlacement(view));
         if (!logMenu.isEmpty())
         {
            add(new Separator());
            add(logMenu);
         }
      }

      final Menu toolsMenu = ObjectMenuFactory.createToolsMenu(selection, contextId, getMenu(), null, new ViewPlacement(view));
      if (toolsMenu != null)
      {
         add(new Separator());
         add(new MenuContributionItem(i18n.tr("&Tools"), toolsMenu));
      }

      final Menu pollsMenu = ObjectMenuFactory.createPollMenu(selection, contextId, getMenu(), null, new ViewPlacement(view));
      if (pollsMenu != null)
      {
         add(new Separator());
         add(new MenuContributionItem(i18n.tr("P&oll"), pollsMenu));
      }

      final Menu graphTemplatesMenu = ObjectMenuFactory.createGraphTemplatesMenu(selection, contextId, getMenu(), null, new ViewPlacement(view));
      if (graphTemplatesMenu != null)
      {
         add(new Separator());
         add(new MenuContributionItem(i18n.tr("&Graphs"), graphTemplatesMenu));
      }

      final Menu summaryTableMenu = ObjectMenuFactory.createSummaryTableMenu(selection, contextId, getMenu(), null, new ViewPlacement(view));
      if (summaryTableMenu != null)
      {
         add(new Separator());
         add(new MenuContributionItem(i18n.tr("S&ummary tables"), summaryTableMenu));
      }

      final Menu dashboardsMenu = ObjectMenuFactory.createDashboardsMenu(selection, contextId, getMenu(), null, new ViewPlacement(view));
      if (dashboardsMenu != null)
      {
         add(new Separator());
         add(new MenuContributionItem(i18n.tr("&Dashboards"), dashboardsMenu));
      }
      
      if (actionCloneNetworkMap.isValidForSelection(selection))
      {
         add(new Separator());
         add(actionCloneNetworkMap);
      }
      
      if (actionChangeInterfaceExpectedState.isValidForSelection(selection))
      {
         add(new Separator());
         add(actionChangeInterfaceExpectedState);
      }
      if (actionCreateInterfaceDCI.isValidForSelection(selection))
      {
         add(actionCreateInterfaceDCI);
      }

      if (!actionContributions.isEmpty())
      {
         boolean first = true;
         for(ObjectAction<?> a : actionContributions)
         {
            if (a.isValidForSelection(selection))
            {
               if (first)
               {
                  add(new Separator());
                  first = false;
               }
               add(a);
            }
         }
      }

      if (singleObject)
      {
         add(new Separator());
         add(actionProperties);
      }
   }

   /**
    * Check if maintenance menu is allowed.
    *
    * @param selection current object selection
    * @return true if maintenance menu is allowed
    */
   private static boolean isMaintenanceMenuAllowed(IStructuredSelection selection)
   {
      for(Object o : selection.toList())
      {
         if (!(o instanceof AbstractObject))
            return false;
         int objectClass = ((AbstractObject)o).getObjectClass();
         if ((objectClass == AbstractObject.OBJECT_BUSINESSSERVICE) || (objectClass == AbstractObject.OBJECT_BUSINESSSERVICEPROTOTYPE) || (objectClass == AbstractObject.OBJECT_BUSINESSSERVICEROOT) ||
             (objectClass == AbstractObject.OBJECT_DASHBOARD) || (objectClass == AbstractObject.OBJECT_DASHBOARDGROUP) || (objectClass == AbstractObject.OBJECT_DASHBOARDROOT) ||
             (objectClass == AbstractObject.OBJECT_NETWORKMAP) || (objectClass == AbstractObject.OBJECT_NETWORKMAPGROUP) || (objectClass == AbstractObject.OBJECT_NETWORKMAPROOT) ||
             (objectClass == AbstractObject.OBJECT_TEMPLATE) || (objectClass == AbstractObject.OBJECT_TEMPLATEGROUP) || (objectClass == AbstractObject.OBJECT_TEMPLATEROOT) ||
             (objectClass == AbstractObject.OBJECT_ASSET) || (objectClass == AbstractObject.OBJECT_ASSETGROUP) || (objectClass == AbstractObject.OBJECT_ASSETROOT) ||
             (objectClass == AbstractObject.OBJECT_NETWORKSERVICE))
            return false;
      }
      return true;
   }

   /**
    * Check if manage/unmanage menu items are allowed.
    *
    * @param selection current object selection
    * @return true if manage/unmanage menu items are allowed
    */
   private static boolean isManagedMenuAllowed(IStructuredSelection selection)
   {
      for(Object o : selection.toList())
      {
         if (!(o instanceof AbstractObject))
            return false;
         int objectClass = ((AbstractObject)o).getObjectClass();
         if ((objectClass == AbstractObject.OBJECT_ASSET) || (objectClass == AbstractObject.OBJECT_ASSETGROUP) || (objectClass == AbstractObject.OBJECT_ASSETROOT))
            return false;
      }
      return true;
   }

   /**
    * Check if delete menu item is allowed.
    *
    * @param selection current object selection
    * @return true if maintenance menu is allowed
    */
   private static boolean isDeleteMenuAllowed(IStructuredSelection selection)
   {
      for(Object o : selection.toList())
      {
         if (!(o instanceof AbstractObject))
            return false;
         int objectClass = ((AbstractObject)o).getObjectClass();
         if ((objectClass == AbstractObject.OBJECT_BUSINESSSERVICEROOT) || (objectClass == AbstractObject.OBJECT_DASHBOARDROOT) || (objectClass == AbstractObject.OBJECT_NETWORKMAPROOT) || 
               (objectClass == AbstractObject.OBJECT_TEMPLATEROOT) || (objectClass == AbstractObject.OBJECT_ASSETROOT))
            return false;
      }
      return true;
   }

   /**
    * Check if "bind to" / "unbind from" menu is allowed.
    *
    * @param selection current object selection
    * @return true if "bind to" / "unbind from" menu is allowed
    */
   private static boolean isBindToMenuAllowed(IStructuredSelection selection)
   {
      for(Object o : selection.toList())
      {
         if (!(o instanceof AbstractObject))
            return false;
         int objectClass = ((AbstractObject)o).getObjectClass();
         if ((objectClass != AbstractObject.OBJECT_CHASSIS) && (objectClass != AbstractObject.OBJECT_CLUSTER) && (objectClass != AbstractObject.OBJECT_MOBILEDEVICE) &&
             (objectClass != AbstractObject.OBJECT_NODE) && (objectClass != AbstractObject.OBJECT_RACK) && (objectClass != AbstractObject.OBJECT_SENSOR) &&
             (objectClass != AbstractObject.OBJECT_SUBNET))
            return false;
      }
      return true;
   }

   /**
    * Check if template remove/apply available
    *
    * @param selection current object selection
    * @return true template management is allowed
    */
   private static boolean isTemplateManagementAllowed(IStructuredSelection selection)
   {
      for(Object o : selection.toList())
      {
         if (!(o instanceof DataCollectionTarget))
            return false;
      }
      return true;
   }

   /**
    * Check if change zone menu items are allowed.
    *
    * @param selection current object selection
    * @return true if change zone menu items are allowed
    */
   private static boolean isChangeZoneMenuAllowed(IStructuredSelection selection)
   {
      for(Object o : selection.toList())
      {
         if (!(o instanceof AbstractObject))
            return false;
         int objectClass = ((AbstractObject)o).getObjectClass();
         if ((objectClass == AbstractObject.OBJECT_NODE) || (objectClass == AbstractObject.OBJECT_CLUSTER))
            return true;
      }
      return false;
   }

   /**
    * Check if send user agent notification menu items are allowed.
    *
    * @param selection current object selection
    * @return true if send user agent notification menu items are allowed
    */
   private static boolean isSendUANotificationMenuAllowed(IStructuredSelection selection)
   {
      if (!selection.isEmpty())
      {
         for (Object obj : ((IStructuredSelection)selection).toList())
         {
            if (!(((obj instanceof AbstractNode) && ((AbstractNode)obj).hasAgent()) || 
                (obj instanceof Container) || (obj instanceof ServiceRoot) || (obj instanceof Rack) ||
                (obj instanceof Cluster)))
            {
               return false;
            }
         }
      }
      else
      {
         return false;
      }
      return true;
   }

   /**
    * Get parent shell for dialog windows.
    *
    * @return parent shell for dialog windows
    */
   protected Shell getShell()
   {
      return view.getWindow().getShell();
   }

   /**
    * Get object from current selection
    *
    * @return object or null
    */
   protected AbstractObject getObjectFromSelection()
   {
      IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
      if (selection.size() != 1)
         return null;
      return (AbstractObject)selection.getFirstElement();
   }

   /**
    * Get object ID from selection
    *
    * @return object ID or 0
    */
   protected long getObjectIdFromSelection()
   {
      IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
      if (selection.size() != 1)
         return 0;
      return ((AbstractObject)selection.getFirstElement()).getObjectId();
   }

   /**
    * Change management status for selected objects
    *
    * @param managed true to manage objects
    */
   private void changeObjectManagementState(final boolean managed)
   {
      final Object[] objects = ((IStructuredSelection)selectionProvider.getSelection()).toArray();
      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Change object management status"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            for(Object o : objects)
            {
               if (o instanceof AbstractObject)
                  session.setObjectManaged(((AbstractObject)o).getObjectId(), managed);
               else if (o instanceof ObjectWrapper)
                  session.setObjectManaged(((ObjectWrapper)o).getObjectId(), managed);
            }
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot change object management status");
         }
      }.start();
   }
   
   /**
    * Deploy package on node
    */
   private void deployPackage()
   {
      final PackageSelectionDialog dialog = new PackageSelectionDialog(view.getWindow().getShell());
      if (dialog.open() != Window.OK)
         return;
      
      final Object[] objectList = ((IStructuredSelection)selectionProvider.getSelection()).toArray();      
      final Set<Long> objects = new HashSet<Long>();
      for(Object o : objectList)
      {
         if (o instanceof AbstractObject)
         {
            objects.add(((AbstractObject)o).getObjectId());
         }
      }

      PackageDeploymentMonitor monitor = new PackageDeploymentMonitor();  
      monitor.setPackageId(dialog.getSelectedPackageId());
      monitor.setApplicableObjects(objects);       
      PackageDeployment deployment = new PackageDeployment(monitor);
      monitor.setPackageDeploymentListener(deployment);
      view.openView(monitor);

      final NXCSession session = Registry.getSession();
      Job job = new Job(i18n.tr("Deploy agent package"), monitor) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            session.deployPackage(dialog.getSelectedPackageId(), objects.toArray(new Long[objects.size()]), deployment);
         }
         
         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot start package deployment");
         }
      };
      job.setUser(false);
      job.start();
   }

   /**
    * Delete selected objects
    */
   private void deleteObject()
   {
      final Object[] objects = ((IStructuredSelection)selectionProvider.getSelection()).toArray();  
      String question = (objects.length == 1) ?
         String.format(i18n.tr("Are you sure you want to delete \"%s\"?"), ((AbstractObject)objects[0]).getObjectName()) :
         String.format(i18n.tr("Are you sure you want to delete %d objects?"), objects.length);
      if (!MessageDialogHelper.openConfirm(view.getWindow().getShell(), i18n.tr("Confirm Delete"), question))
         return;

      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Delete objects"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            for(int i = 0; i < objects.length; i++)
               session.deleteObject(((AbstractObject)objects[i]).getObjectId());
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot delete object");
         }
      }.start();
   }

   /**
    * Open screenshot view
    */
   private void openScreenshotView()
   {
      AbstractObject object = getObjectFromSelection();
      if (!(object instanceof Node))
         return;

      long contextId = (view instanceof ObjectView) ? ((ObjectView)view).getObjectId() : 0;
      view.openView(new ScreenshotView((Node)object, null, null, contextId));
   }

   /**
    * Open agent configuration editor
    */
   private void openAgentConfigEditor()
   {
      AbstractObject object = getObjectFromSelection();
      if (!(object instanceof Node))
         return;

      long contextId = (view instanceof ObjectView) ? ((ObjectView)view).getObjectId() : 0;
      view.openView(new AgentConfigurationEditor((Node)object, contextId));
   }

   /**
    * Execute script on object
    */
   private void executeScript()
   {
      AbstractObject object = getObjectFromSelection();
      long contextId = (view instanceof ObjectView) ? ((ObjectView)view).getObjectId() : 0;
      view.openView(new ScriptExecutorView(object.getObjectId(), contextId));
   }

   /**
    * Apply selected in dialog object to selected in tree data collection targets
    */
   private void selectAndApplyTemplate()
   {
      final List<Long> targetsId = new ArrayList<>();
      for(Object o : ((IStructuredSelection)selectionProvider.getSelection()).toList())
      {
         if (o instanceof DataCollectionTarget)
            targetsId.add(((AbstractObject)o).getObjectId());
      }
      
      final ObjectSelectionDialog dlg = new ObjectSelectionDialog(view.getWindow().getShell(), ObjectSelectionDialog.createTemplateSelectionFilter());
      dlg.enableMultiSelection(false);
      if (dlg.open() != Window.OK)
         return;

      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Binding objects"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            List<AbstractObject> objects = dlg.getSelectedObjects();
            for(Long target : targetsId)
               session.bindObject(objects.get(0).getObjectId(), target);
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot bind objects");
         }
      }.start();
   }


   /**
    * Remove selected in dialog templates from selected in tree data collection targets
    */
   private void selectTemplateToRemove()
   {
      final Set<Long> targetsId = new HashSet<Long>();
      for(Object o : ((IStructuredSelection)selectionProvider.getSelection()).toList())
      {
         if (o instanceof DataCollectionTarget)
            targetsId.add(((AbstractObject)o).getObjectId());
      }
      
      final RelatedTemplateObjectSelectionDialog dlg = new RelatedTemplateObjectSelectionDialog(view.getWindow().getShell(), targetsId, RelationType.DIRECT_SUPERORDINATES, ObjectSelectionDialog.createTemplateSelectionFilter());
      dlg.setShowObjectPath(true);
      if (dlg.open() != Window.OK)
         return;

      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Binding objects"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            List<AbstractObject> objects = dlg.getSelectedObjects();
            for (AbstractObject object : objects)
               for(Long target : targetsId)
                  session.removeTemplate(object.getObjectId(), target, dlg.isRemoveDci());
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot bind objects");
         }
      }.start();
   }

   /**
    * Bind objects to selected object
    */
   private void bindObjects()
   {
      final long parentId = getObjectIdFromSelection();
      if (parentId == 0)
         return;

      final ObjectSelectionDialog dlg = new ObjectSelectionDialog(view.getWindow().getShell(), ObjectSelectionDialog.createDataCollectionTargetSelectionFilter());
      if (dlg.open() != Window.OK)
         return;

      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Binding objects"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            List<AbstractObject> objects = dlg.getSelectedObjects();
            for(AbstractObject o : objects)
               session.bindObject(parentId, o.getObjectId());
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot bind objects");
         }
      }.start();
   }

   /**
    * Remove template from data collection target
    */
   private void removeTemplate()
   {
      final long parentId = getObjectIdFromSelection();
      if (parentId == 0)
         return;

      final RelatedTemplateObjectSelectionDialog dlg = new RelatedTemplateObjectSelectionDialog(view.getWindow().getShell(), parentId, RelatedObjectSelectionDialog.RelationType.DIRECT_SUBORDINATES, null);
      if (dlg.open() != Window.OK)
         return;

      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Remove template"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            List<AbstractObject> objects = dlg.getSelectedObjects();
            for(AbstractObject o : objects)
               session.removeTemplate(parentId, o.getObjectId(), dlg.isRemoveDci());
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot remove template");
         }
      }.start();
   }

   /**
    * Unbind objects from selected object
    */
   private void unbindObjects()
   {
      final long parentId = getObjectIdFromSelection();
      if (parentId == 0)
         return;

      final RelatedObjectSelectionDialog dlg = new RelatedObjectSelectionDialog(view.getWindow().getShell(), parentId, RelatedObjectSelectionDialog.RelationType.DIRECT_SUBORDINATES, null);
      if (dlg.open() != Window.OK)
         return;

      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Unbinding objects"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            List<AbstractObject> objects = dlg.getSelectedObjects();
            for(AbstractObject o : objects)
               session.unbindObject(parentId, o.getObjectId());
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot unbind objects");
         }
      }.start();
   }

   /**
    * Bind selected objects to another object
    */
   private void bindToObject()
   {
      final List<Long> childIdList = new ArrayList<>();
      for(Object o : ((IStructuredSelection)selectionProvider.getSelection()).toList())
      {
         if ((o instanceof DataCollectionTarget) || (o instanceof Rack) || (o instanceof Chassis) || (o instanceof Subnet))
            childIdList.add(((AbstractObject)o).getObjectId());
      }

      final ObjectSelectionDialog dlg = new ObjectSelectionDialog(view.getWindow().getShell(), ObjectSelectionDialog.createContainerSelectionFilter());
      if (dlg.open() != Window.OK)
         return;

      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Binding objects"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            List<AbstractObject> parents = dlg.getSelectedObjects();
            for(AbstractObject o : parents)
            {
               for(Long childId : childIdList)
                  session.bindObject(o.getObjectId(), childId);
            }
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot bind objects");
         }
      }.start();
   }

   /**
    * Unbind selected objects from one or more containers
    */
   private void unbindFromObjects()
   {
      final long childId = getObjectIdFromSelection();
      if (childId == 0)
         return;

      final RelatedObjectSelectionDialog dlg = new RelatedObjectSelectionDialog(view.getWindow().getShell(), childId, RelatedObjectSelectionDialog.RelationType.DIRECT_SUPERORDINATES,
            ObjectSelectionDialog.createContainerSelectionFilter());
      if (dlg.open() != Window.OK)
         return;

      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Unbinding objects"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            List<AbstractObject> objects = dlg.getSelectedObjects();
            for(AbstractObject o : objects)
               session.unbindObject(o.getObjectId(), childId);
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot unbind objects");
         }
      }.start();
   }

   /**
    * Add node(s) to cluster
    */
   private void addNodeToCluster()
   {
      final long clusterId = getObjectIdFromSelection();
      if (clusterId == 0)
         return;

      final ObjectSelectionDialog dlg = new ObjectSelectionDialog(view.getWindow().getShell(), ObjectSelectionDialog.createNodeSelectionFilter(false));
      if (dlg.open() != Window.OK)
         return;

      final NXCSession session = Registry.getSession();      
      new Job(i18n.tr("Add node to cluster"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            List<AbstractObject> objects = dlg.getSelectedObjects();
            for(AbstractObject o : objects)
            {
               session.addClusterNode(clusterId, o.getObjectId());          
            }
         }
         
         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot add node to cluster");
         }
      }.start();
   }

   /**
    * Remove node(s) from cluster
    */
   private void removeNodeFromCluster()
   {
      final long clusterId = getObjectIdFromSelection();
      if (clusterId == 0)
         return;

      final RelatedObjectSelectionDialog dlg = new RelatedObjectSelectionDialog(view.getWindow().getShell(), clusterId, RelatedObjectSelectionDialog.RelationType.DIRECT_SUBORDINATES,
            RelatedObjectSelectionDialog.createClassFilter(AbstractObject.OBJECT_NODE));
      if (dlg.open() != Window.OK)
         return;

      final NXCSession session = Registry.getSession();
      new Job(i18n.tr("Remove cluster node"), view) {
         @Override
         protected void run(IProgressMonitor monitor) throws Exception
         {
            List<AbstractObject> objects = dlg.getSelectedObjects();
            for(int i = 0; i < objects.size(); i++)
               session.removeClusterNode(clusterId, objects.get(i).getObjectId());
         }

         @Override
         protected String getErrorMessage()
         {
            return i18n.tr("Cannot remove node from cluster");
         }
      }.start();
   }

   /**
    * Show route between current node and another node selected by user
    * 
    * @param swap true to swap source and destination (current node is source by default)
    */
   private void showRoute(boolean swap)
   {
      AbstractObject source = getObjectFromSelection();
      if (!(source instanceof Node))
         return;

      final ObjectSelectionDialog dlg = new ObjectSelectionDialog(view.getWindow().getShell(), ObjectSelectionDialog.createNodeSelectionFilter(false));
      dlg.enableMultiSelection(false);
      if (dlg.open() != Window.OK)
         return;

      AbstractObject destination = dlg.getSelectedObjects().get(0);
      if (!(destination instanceof Node))
         return;

      long contextId = (view instanceof ObjectView) ? ((ObjectView)view).getObjectId() : source.getObjectId();
      view.openView(
            swap ?
               new RouteView((Node)destination, (Node)source, contextId) :
               new RouteView((Node)source, (Node)destination, contextId));
   }
}
