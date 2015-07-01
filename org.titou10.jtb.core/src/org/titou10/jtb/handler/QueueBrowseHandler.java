/*
 * Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.titou10.jtb.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Browse/Refresh Queue" command
 * 
 * @author Denis Forveille
 * 
 */
public class QueueBrowseHandler {

   private static final Logger log = LoggerFactory.getLogger(QueueBrowseHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Execute
   public void execute(MApplication app,
                       EModelService modelService,
                       EPartService partService,
                       @Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                       @Optional @Named(IServiceConstants.ACTIVE_SELECTION) NodeJTBQueue nodeJTBQueue,
                       @Optional @Named(Constants.CURRENT_TAB_JTBQUEUE) JTBQueue tabJTBQueue) {
      log.debug("execute. Selection : {}", nodeJTBQueue);

      JTBQueue jtbQueue = null;
      JTBSession jtbSession = null;

      switch (context) {
         case Constants.COMMAND_CONTEXT_PARAM_QUEUE:
            jtbQueue = (JTBQueue) nodeJTBQueue.getBusinessObject();
            jtbSession = jtbQueue.getJtbSession();
            break;

         case Constants.COMMAND_CONTEXT_PARAM_MESSAGE:
            jtbQueue = tabJTBQueue;
            jtbSession = tabJTBQueue.getJtbSession();
            break;

         default:
            log.error("Invalid value : {}", context);
            return;
      }

      // Reuse or create a tab-part per Q Manager
      String partName = Constants.PART_QCONTENT_PREFIX + jtbSession.getName();
      MPart part = (MPart) modelService.find(partName, app);
      if (part == null) {

         // Create part from Part Descriptor
         part = partService.createPart(Constants.PARTDESCRITOR_MESSAGES);
         part.setLabel(jtbSession.getName());
         part.setElementId(partName);

         MPartStack stack = (MPartStack) modelService.find(Constants.PARTSTACK_QCONTENT, app);
         stack.getChildren().add(part);
      }

      // Show Part and refresh content
      partService.showPart(part, PartState.CREATE);
      eventBroker.send(Constants.EVENT_REFRESH_MESSAGES, jtbQueue);
      eventBroker.send(Constants.EVENT_FOCUS_CTABITEM, jtbQueue);
      partService.activate(part, true);
   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                             @Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object selection,
                             @Optional @Named(Constants.CURRENT_TAB_JTBQUEUE) JTBQueue tabJTBQueue,
                             @Optional MMenuItem menuItem) {
      log.debug("canExecute context={} selection={} tabJTBQueue={}", context, selection, tabJTBQueue);

      switch (context) {
         case Constants.COMMAND_CONTEXT_PARAM_QUEUE:
            // Show menu on Queues only
            if (selection instanceof NodeJTBQueue) {
               return Utils.enableMenu(menuItem);
            } else {
               return Utils.disableMenu(menuItem);
            }

         case Constants.COMMAND_CONTEXT_PARAM_MESSAGE:
            return Utils.enableMenu(menuItem);

         default:
            log.error("Invalid value : {}", context);
            return Utils.disableMenu(menuItem);
      }
   }
}
