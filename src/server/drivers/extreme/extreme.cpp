/* 
** NetXMS - Network Management System
** Driver for Extreme Networks switches
** Copyright (C) 2003-2020 Victor Kirhenshtein
**
** This program is free software; you can redistribute it and/or modify
** it under the terms of the GNU Lesser General Public License as published by
** the Free Software Foundation; either version 3 of the License, or
** (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
**
** File: extreme.cpp
**/

#include "extreme.h"
#include <netxms-version.h>

/**
 * Driver name
 */
static TCHAR s_driverName[] = _T("EXTREME");

/**
 * Driver version
 */
static TCHAR s_driverVersion[] = NETXMS_VERSION_STRING;

/**
 * Get driver name
 */
const TCHAR *ExtremeDriver::getName()
{
	return s_driverName;
}

/**
 * Get driver version
 */
const TCHAR *ExtremeDriver::getVersion()
{
	return s_driverVersion;
}

/**
 * Check if given device can be potentially supported by driver
 *
 * @param oid Device OID
 */
int ExtremeDriver::isPotentialDevice(const TCHAR *oid)
{
	return (_tcsncmp(oid, _T(".1.3.6.1.4.1.1916.2."), 20) == 0) ? 255 : 0;
}

/**
 * Check if given device is supported by driver
 *
 * @param snmp SNMP transport
 * @param oid Device OID
 */
bool ExtremeDriver::isDeviceSupported(SNMP_Transport *snmp, const TCHAR *oid)
{
	return true;
}

/**
 * Get list of interfaces for given node
 *
 * @param snmp SNMP transport
 * @param node Node
 */
InterfaceList *ExtremeDriver::getInterfaces(SNMP_Transport *snmp, NObject *node, DriverData *driverData, bool useIfXTable)
{
	// Get interface list from standard MIB
	InterfaceList *ifList = NetworkDeviceDriver::getInterfaces(snmp, node, driverData, useIfXTable);
	if (ifList == nullptr)
		return nullptr;

	// Update physical port locations
   for(int i = 0; i < ifList->size(); i++)
   {
      InterfaceInfo *iface = ifList->get(i);
      if (iface->type != IFTYPE_ETHERNET_CSMACD)
         continue;

      TCHAR ifName[64];
      _tcslcpy(ifName, iface->name, 64);
      TCHAR *p = _tcschr(ifName, _T(':'));
      if (p == nullptr)
         continue;

      *p = 0;
      p++;
      TCHAR *eptr;
      int slot = _tcstol(ifName, &eptr, 10);
      if ((slot <= 0) || (*eptr != 0))
         continue;

      int port = _tcstol(p, &eptr, 10);
      if ((port <= 0) || (*eptr != 0))
         continue;

      iface->isPhysicalPort = true;
      iface->location.module = slot;
      iface->location.port = port;
   }
	return ifList;
}

/**
 * Translate LLDP port name (port ID subtype 5) to local interface id.
 *
 * @param snmp SNMP transport
 * @param node Node
 * @param driverData driver's data
 * @param lldpName port name received from LLDP MIB
 * @param id interface ID structure to be filled at success
 * @return true if interface identification provided
 */
bool ExtremeDriver::lldpNameToInterfaceId(SNMP_Transport *snmp, NObject *node, DriverData *driverData, const TCHAR *lldpName, InterfaceId *id)
{
   // Actual interface names could be in form 1:name
   id->type = InterfaceIdType::NAME;
   id->value.ifName[0] = _T('1');
   id->value.ifName[1] = _T(':');
   _tcslcpy(&id->value.ifName[2], lldpName, 190);
   return true;
}

/**
 * Get orientation of the modules in the device
 *
 * @param snmp SNMP transport
 * @param node Node
 * @param driverData driver-specific data previously created in analyzeDevice
 * @return module orientation
 */
int ExtremeDriver::getModulesOrientation(SNMP_Transport *snmp, NObject *node, DriverData *driverData)
{
   return NDD_ORIENTATION_HORIZONTAL;
}

/**
 * Get port layout of given module
 * @param snmp SNMP transport
 * @param node Node
 * @param driverData driver-specific data previously created in analyzeDevice
 * @param module Module number (starting from 1)
 * @param layout Layout structure to fill
 */
void ExtremeDriver::getModuleLayout(SNMP_Transport *snmp, NObject *node, DriverData *driverData, int module, NDD_MODULE_LAYOUT *layout)
{
   layout->numberingScheme = NDD_PN_UD_LR;
   layout->rows = 2;
}

/**
 * Driver entry point
 */
DECLARE_NDD_ENTRY_POINT(ExtremeDriver);

#ifdef _WIN32

/**
 * DLL entry point
 */
BOOL WINAPI DllMain(HINSTANCE hInstance, DWORD dwReason, LPVOID lpReserved)
{
	if (dwReason == DLL_PROCESS_ATTACH)
		DisableThreadLibraryCalls(hInstance);
	return TRUE;
}

#endif
