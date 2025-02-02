/* 
** NetXMS - Network Management System
** Driver for D-Link switches
** Copyright (C) 2003-2023 Victor Kirhenshtein
**
** This program is free software; you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation; either version 2 of the License, or
** (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
**
** File: dlink.h
**
**/

#ifndef _dlink_h_
#define _dlink_h_

#include <nddrv.h>

/**
 * Driver's class
 */
class DLinkDriver : public NetworkDeviceDriver
{
public:
	virtual const TCHAR *getName() override;
	virtual const TCHAR *getVersion() override;

	virtual int isPotentialDevice(const TCHAR *oid) override;
	virtual bool isDeviceSupported(SNMP_Transport *snmp, const TCHAR *oid) override;
	virtual void analyzeDevice(SNMP_Transport *snmp, const TCHAR *oid, NObject *node, DriverData **driverData) override;
	virtual InterfaceList *getInterfaces(SNMP_Transport *snmp, NObject *node, DriverData *driverData, bool useIfXTable) override;
	virtual VlanList *getVlans(SNMP_Transport *snmp, NObject *node, DriverData *driverData) override;
};

#endif
