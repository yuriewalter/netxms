/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2022 Victor Kirhenshtein
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
package org.netxms.nxmc.resources;

import org.eclipse.swt.graphics.RGB;

/**
 * Default dark theme
 */
public class DefaultDarkTheme extends Theme
{
   /**
    * Create default dark theme
    */
   public DefaultDarkTheme()
   {
      super("Dark [built-in]");
      elements.put(".", new ThemeElement(null, null, null, 0));
      elements.put("Card.Title", new ThemeElement(new RGB(53, 80, 9), new RGB(240, 240, 240)));
      elements.put("Dashboard", new ThemeElement(new RGB(53, 53, 53), null));
      elements.put("DeviceView.Port", new ThemeElement(new RGB(64, 64, 64), null));
      elements.put("DeviceView.PortHighlight", new ThemeElement(new RGB(39, 96, 138), null));
      elements.put("JSON.Builtin", new ThemeElement(null, new RGB(86, 124, 113)));
      elements.put("JSON.Key", new ThemeElement(null, new RGB(132, 217, 254)));
      elements.put("JSON.Number", new ThemeElement(null, new RGB(181, 176, 101)));
      elements.put("JSON.String", new ThemeElement(null, new RGB(160, 143, 120)));
      elements.put("List.DisabledItem", new ThemeElement(null, new RGB(96, 96, 96)));
      elements.put("List.Error", new ThemeElement(null, new RGB(220, 35, 61)));
      elements.put("Map.GroupBox", new ThemeElement(null, new RGB(255, 255, 255)));
      elements.put("Map.LastValues", new ThemeElement(null, new RGB(0, 64, 0)));
      elements.put("Map.ObjectTooltip", new ThemeElement(new RGB(131, 122, 53), null));
      elements.put("MessageArea.Error", new ThemeElement(new RGB(120, 62, 52), new RGB(169, 25, 0)));
      elements.put("MessageArea.Info", new ThemeElement(new RGB(21, 94, 122), new RGB(26, 143, 189)));
      elements.put("MessageArea.Success", new ThemeElement(new RGB(67, 116, 24), new RGB(77, 164, 0)));
      elements.put("MessageArea.Warning", new ThemeElement(new RGB(157, 126, 0), new RGB(241, 196, 14)));
      elements.put("MessageBar", new ThemeElement(new RGB(138, 148, 47), new RGB(0, 0, 0)));
      elements.put("MibExplorer.Header", new ThemeElement(new RGB(64, 64, 64), new RGB(153, 180, 209)));
      elements.put("ObjectTab.Header", new ThemeElement(new RGB(64, 64, 64), new RGB(153, 180, 209)));
      elements.put("ObjectTree.Maintenance", new ThemeElement(null, new RGB(136, 136, 204)));
      elements.put("Rack", new ThemeElement(new RGB(53, 53, 53), new RGB(240, 240, 240)));
      elements.put("Rack.Border", new ThemeElement(new RGB(92, 92, 92), new RGB(92, 92, 92)));
      elements.put("Rack.EmptySpace", new ThemeElement(new RGB(64, 64, 64), null));
      elements.put("RuleEditor", new ThemeElement(new RGB(53, 53, 53), new RGB(240, 240, 240)));
      elements.put("RuleEditor.Title.Disabled", new ThemeElement(new RGB(98, 110, 99), null));
      elements.put("RuleEditor.Title.Normal", new ThemeElement(new RGB(80, 84, 87), null));
      elements.put("RuleEditor.Title.Selected", new ThemeElement(new RGB(113, 115, 48), null));
      elements.put("RuleEditor.Border.Action", new ThemeElement(new RGB(90, 85, 97), null));
      elements.put("RuleEditor.Border.Condition", new ThemeElement(new RGB(94, 102, 82), null));
      elements.put("RuleEditor.Border.Rule", new ThemeElement(new RGB(56, 66, 77), null));
      elements.put("ServiceAvailability.Legend", new ThemeElement(null, new RGB(240, 240, 240)));
      elements.put("Status.Normal", new ThemeElement(null, new RGB(0, 192, 0)));
      elements.put("Status.Warning", new ThemeElement(null, new RGB(0, 255, 255)));
      elements.put("Status.Minor", new ThemeElement(null, new RGB(231, 226, 0)));
      elements.put("Status.Major", new ThemeElement(null, new RGB(255, 128, 0)));
      elements.put("Status.Critical", new ThemeElement(null, new RGB(192, 0, 0)));
      elements.put("Status.Unknown", new ThemeElement(null, new RGB(0, 0, 128)));
      elements.put("Status.Unmanaged", new ThemeElement(null, new RGB(192, 192, 192)));
      elements.put("Status.Disabled", new ThemeElement(null, new RGB(128, 64, 0)));
      elements.put("Status.Testing", new ThemeElement(null, new RGB(255, 128, 255)));
      elements.put("StatusMap.Text", new ThemeElement(null, new RGB(0, 0, 0)));
      elements.put("TextInput.Error", new ThemeElement(new RGB(48, 0, 0), null));
      elements.put("Window.Header", new ThemeElement(new RGB(17, 60, 81), new RGB(192, 192, 192), "Metropolis Medium,Segoe UI,Liberation Sans,Verdana,Helvetica", 13));
      elements.put("Window.Header.Highlight", new ThemeElement(new RGB(71, 113, 134), new RGB(192, 192, 192)));
      elements.put("Window.PerspectiveSwitcher", new ThemeElement(new RGB(0, 54, 77), new RGB(240, 240, 240), "Metropolis Medium,Segoe UI,Liberation Sans,Verdana,Helvetica", 14));
   }
}
