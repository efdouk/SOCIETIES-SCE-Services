/**
 * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
 * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
 * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
 * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
 * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
 * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ac.hw.rfid.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Timer;


import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.css.devicemgmt.IAction;
import org.societies.api.css.devicemgmt.IDevice;
import org.societies.api.css.devicemgmt.IDriverService;
import org.societies.api.css.devicemgmt.model.DeviceActionsConstants;
import org.societies.api.css.devicemgmt.model.DeviceMgmtConstants;
import org.societies.api.css.devicemgmt.model.DeviceMgmtDriverServiceNames;
import org.societies.api.css.devicemgmt.model.DeviceMgmtEventConstants;
import org.societies.api.css.devicemgmt.model.DeviceStateVariableConstants;
import org.societies.api.css.devicemgmt.model.DeviceTypeConstants;
import org.societies.api.css.devicemgmt.rfid.IRfidDriver;
import org.societies.api.osgi.event.CSSEvent;
import org.societies.api.osgi.event.CSSEventConstants;
import org.societies.api.osgi.event.EventListener;
import org.societies.api.osgi.event.EventTypes;
import org.societies.api.osgi.event.IEventMgr;
import org.societies.api.osgi.event.InternalEvent;
import org.springframework.osgi.context.BundleContextAware;

import ac.hw.rfid.client.api.remote.IRfidClient;
import ac.hw.rfid.server.api.IRfidServer;
import ac.hw.rfid.server.gui.ServerGUIFrame;

public class RfidServer extends EventListener implements IRfidServer, ServiceTrackerCustomizer, BundleContextAware {

	private Logger logging = LoggerFactory.getLogger(this.getClass());
	//private ServiceResourceIdentifier myServiceId;
	//private List<String> myServiceTypes = new ArrayList<String>();
	
	private Hashtable<String, String> tagToPasswordTable;

	private Hashtable<String, String> tagtoIdentityTable;
	
	private Hashtable<String, String> wUnitToSymlocTable;
	private ServerGUIFrame frame;
	
	private IEventMgr eventMgr;
	
	//private Hashtable<String, String> dpiToServiceID;
	Hashtable<String, RFIDUpdateTimerTask> tagToTimerTable = new Hashtable<String, RFIDUpdateTimerTask>();

	private IRfidClient rfidClientRemote;
	
	private BundleContext bundleContext;
	
	private ServiceTracker serviceTracker;
	
	private IDevice iDevice;
	
	
	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	/**
	 * @return the eventMgr
	 */
	public IEventMgr getEventMgr() {
		return eventMgr;
	}

	/**
	 * @param eventMgr the eventMgr to set
	 */
	public void setEventMgr(IEventMgr eventMgr) {
		this.eventMgr = eventMgr;
	}
	
	
	public void initialiseRFIDServer(){
		
		
		String stringFilter = "(&("+ Constants.OBJECTCLASS +"="+ IDevice.class.getName()+")("+DeviceMgmtConstants.DEVICE_TYPE+"="+DeviceTypeConstants.RFID_READER+"))";
		
		Filter filter = null;
		try {
			filter = bundleContext.createFilter(stringFilter);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		
		this.serviceTracker = new ServiceTracker(bundleContext, filter, this);
		this.serviceTracker.open();
		
		

		this.tagtoIdentityTable = new Hashtable<String, String>();
		this.tagToPasswordTable = new Hashtable<String, String>();
		//this.dpiToServiceID = new Hashtable<String, String>();
		RFIDConfig rfidConfig = new RFIDConfig();
		this.wUnitToSymlocTable = rfidConfig.getUnitToSymloc();
		if (this.wUnitToSymlocTable==null){
			this.wUnitToSymlocTable = new Hashtable<String, String>();
			
		}
		this.registerRFIDReaders();
		
		frame = new ServerGUIFrame(this);
	}
	
	
	private void registerRFIDReaders(){
		String[] options = new String[]{"0.localhost","1.University addresses"};
		//String str = (String) JOptionPane.showInputDialog(null, "Select Configuration", "Configuration", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		String str = options[1];
		if (str.equalsIgnoreCase(options[0])){
			
			if (null != iDevice) {
				
				this.registerForRFIDEvents(iDevice.getDeviceId());
				
				IDriverService iDriverService = iDevice.getService(DeviceMgmtDriverServiceNames.RFID_READER_DRIVER_SERVICE);
				IAction iAction = iDriverService.getAction(DeviceActionsConstants.RFID_CONNECT_ACTION);
				
				Dictionary<String, Object> dic = new Hashtable<String, Object>();
				
				dic.put(DeviceStateVariableConstants.IP_ADDRESS_STATE_VARIABLE, "127.0.0.1");
				iAction.invokeAction(dic);
			}
					

			//this.rfidDriver.connect("127.0.0.1");
		}else{

				if (null != iDevice) 
				{
					this.registerForRFIDEvents(iDevice.getDeviceId());
					
					IDriverService iDriverService = iDevice.getService(DeviceMgmtDriverServiceNames.RFID_READER_DRIVER_SERVICE);
					IAction iAction = iDriverService.getAction(DeviceActionsConstants.RFID_CONNECT_ACTION);
					
					Dictionary<String, Object> dic = new Hashtable<String, Object>();
					
					dic.put(DeviceStateVariableConstants.IP_ADDRESS_STATE_VARIABLE, "137.195.27.197");
					iAction.invokeAction(dic);
					
					dic = new Hashtable<String, Object>();
					dic.put(DeviceStateVariableConstants.IP_ADDRESS_STATE_VARIABLE, "137.195.27.198");
					iAction.invokeAction(dic);
				}
		
			//this.rfidDriver.connect("137.195.27.197");
			//this.rfidDriver.connect("137.195.27.198");
		}
		
	}
	public RfidServer(){
		UIManager.put("ClassLoader", ClassLoader.getSystemClassLoader());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




	private void registerForRFIDEvents(String deviceId){
		String eventFilter = "(&" + 
				"(" + CSSEventConstants.EVENT_NAME + "=" + DeviceMgmtEventConstants.RFID_READER_EVENT + ")" +
				"(" + CSSEventConstants.EVENT_SOURCE + "=" + deviceId + ")" +
				")";
		this.logging.debug("Registering for RFIDEvent: "+eventFilter);
		
		this.eventMgr.subscribeInternalEvent(this, new String[]{EventTypes.RFID_UPDATE_EVENT}, eventFilter);
	}
	
	
	/*
	 * This method is called by the RFIDUpdateTimerTask every minute to 
	 * send the symbolic locations to the appropriate user
	 */
	public void sendRemoteUpdate(String symLoc, String rfidTagNumber){
		if (this.tagtoIdentityTable.containsKey(rfidTagNumber)){
			String jid = this.tagtoIdentityTable.get(rfidTagNumber);
			//String clientServiceID = this.dpiToServiceID.get(dpi);
			//this.sendUpdateMessage(dpi, clientServiceID, rfidTagNumber, symLoc);
			this.rfidClientRemote.sendUpdate(jid, symLoc, rfidTagNumber);
		}
			
	}
	
	
	
	/*
	 * Method called when an RFID_UPDATE_EVENT is received
	 */
	public void sendUpdate(String wUnit, String rfidTagNumber) {
		
		if (this.wUnitToSymlocTable.containsKey(wUnit)){
			this.logging.debug("wUnit: "+wUnit+" matches symloc: "+wUnitToSymlocTable.get(wUnit));
			if (this.tagToTimerTable.containsKey(rfidTagNumber)){
				
				this.tagToTimerTable.get(rfidTagNumber).setSymLoc(this.wUnitToSymlocTable.get(wUnit));
				this.logging.debug("setting symloc: "+this.wUnitToSymlocTable.get(wUnit)+" to: "+rfidTagNumber);
				this.frame.addRow(this.tagtoIdentityTable.get(rfidTagNumber), rfidTagNumber, wUnit, this.wUnitToSymlocTable.get(wUnit));
			}else{
				if (this.tagtoIdentityTable.containsKey(rfidTagNumber)){
					this.logging.debug("tag "+rfidTagNumber+" registered to identity "+tagtoIdentityTable.get(rfidTagNumber));
					RFIDUpdateTimerTask task = new RFIDUpdateTimerTask(this.rfidClientRemote, rfidTagNumber, this.wUnitToSymlocTable.get(wUnit), this.tagtoIdentityTable.get(rfidTagNumber));
					this.tagToTimerTable.put(rfidTagNumber, task);
					Timer timer = new Timer();
					timer.schedule(task, new Date(), 5000);
					this.logging.debug("Created timer");
					this.frame.addRow(this.tagtoIdentityTable.get(rfidTagNumber), rfidTagNumber, wUnit, this.wUnitToSymlocTable.get(wUnit));
				}
			}
			
		}else{
			this.logging.debug("wUnit :"+wUnit+" doesn't match any symLoc");
			this.frame.addRow("Unregistered", rfidTagNumber, wUnit, "Unknown");
		}
		
		
		//OLD CODE BELOW
		/*String symLoc = "";
		if (this.wUnitToSymlocTable.containsKey(wUnit)){
			symLoc = this.wUnitToSymlocTable.get(wUnit);
			if (this.tagToTimerTable.containsKey(rfidTagNumber)){
				this.tagToTimerTable.get(rfidTagNumber).cancel();
			}
			
		}else{
			logging.debug("wUnit: "+wUnit+" not found, wUnit length: "+wUnit.length());
			Enumeration<String> e = this.wUnitToSymlocTable.keys();
			logging.debug("Existing wUnits: ");
			while (e.hasMoreElements()){
				String u = e.nextElement();
				logging.debug(u+" size: "+u.length());
			}
			
		}
		if (!symLoc.equalsIgnoreCase("")){
			Timer timer = new Timer();
			RFIDUpdateTimerTask task = new RFIDUpdateTimerTask(this, rfidTagNumber);
			timer.schedule(task, 1000);
			this.tagToTimerTable.put(rfidTagNumber, timer);
		}		
		if (this.tagtoIdentityTable.containsKey(rfidTagNumber)){
			String dpi = this.tagtoIdentityTable.get(rfidTagNumber);
			//String clientServiceID = this.dpiToServiceID.get(dpi);
			//this.sendUpdateMessage(dpi, clientServiceID, rfidTagNumber, symLoc);
			this.rfidClientRemote.sendUpdate(dpi, symLoc, rfidTagNumber);
			this.frame.addRow(dpi, rfidTagNumber, wUnit, symLoc);

		}else{
			//JOptionPane.showMessageDialog(null, "Tag: "+rfidTagNumber+" in location "+symLoc+" not registered to a DPI");
			logging.debug("Tag: "+rfidTagNumber+" in location "+symLoc+" not registered to a DPI");
			this.frame.addRow("Unregistered", rfidTagNumber, wUnit, symLoc);
		}*/
		
		
	}

	@Override
	public void registerRFIDTag(String tagNumber, String dpiAsString, String serviceID, String password) {
		logging.debug("Received request to register RFID tag: "+tagNumber+" from identity: "+dpiAsString+" and serviceID: "+serviceID);
			if (this.tagToPasswordTable.containsKey(tagNumber)){
				String myPass = this.tagToPasswordTable.get(tagNumber);
				logging.debug("Tag exists");
				if (myPass.equalsIgnoreCase(password)){
					
					this.removeOldRegistration(dpiAsString);
					
					this.tagtoIdentityTable.put(tagNumber, dpiAsString);
					//this.dpiToServiceID.put(dpiAsString, serviceID);
					this.rfidClientRemote.acknowledgeRegistration(dpiAsString, 0);
					this.frame.setNewDPIRegistered(tagNumber, dpiAsString);
					logging.debug("Registration successfull. Sent Acknowledgement 0");

				}else{
					this.rfidClientRemote.acknowledgeRegistration(dpiAsString, 1);
					logging.debug("Registration unsuccessfull. Sent Ack 1");
				}
			}else{
				
				this.rfidClientRemote.acknowledgeRegistration(dpiAsString, 2);
				logging.debug("Registration unsuccessfull. Sent Ack 2");
			}
		

		
	}

	

	private void removeOldRegistration( String dpiAsString){
		if (this.tagtoIdentityTable.contains(dpiAsString)){
			
			Enumeration<String> tags = this.tagtoIdentityTable.keys();
			
			while (tags.hasMoreElements()){
				String tag = tags.nextElement();
				String dpi = this.tagtoIdentityTable.get(tag);
				if (dpi.equalsIgnoreCase(dpiAsString)){
					this.tagtoIdentityTable.remove(tag);
					/*if (this.dpiToServiceID.containsKey(dpiAsString)){
						this.dpiToServiceID.remove(dpiAsString);
					}*/
					this.frame.setNewDPIRegistered(tag, "");
					return;
				}
			}
		}
		
		/*if (this.dpiToServiceID.containsKey(dpiAsString)){
			this.dpiToServiceID.remove(dpiAsString);
		}*/
	

	}
	public String getPassword() {
		int n = 4;
		char[] pw = new char[n];
		int c  = 'A';
		int  r1 = 0;
		for (int i=0; i < n; i++)
		{
			r1 = (int)(Math.random() * 3);
			switch(r1) {
			case 0: c = '0' +  (int)(Math.random() * 10); break;
			case 1: c = 'a' +  (int)(Math.random() * 26); break;
			case 2: c = 'A' +  (int)(Math.random() * 26); break;
			}
			pw[i] = (char)c;
		}
		return new String(pw);
	}

	public void storePassword(String tagNumber, String password){
		this.tagToPasswordTable.put(tagNumber, password);
		this.tagtoIdentityTable.remove(tagNumber);
	}
	



	/**
	 * @return the rfidClient
	 */
	public IRfidClient getRfidClientRemote() {
		return rfidClientRemote;
	}



	/**
	 * @param rfidClientRemote the rfidClient to set
	 */
	public void setRfidClientRemote(IRfidClient rfidClientRemote) {
		this.rfidClientRemote = rfidClientRemote;
	}

//	/**
//	 * @return the rfidDriver
//	 */
//	public IRfidDriver getRfidDriver() {
//		return rfidDriver;
//	}
//
//	/**
//	 * @param rfidDriver the rfidDriver to set
//	 */
//	public void setRfidDriver(IRfidDriver rfidDriver) {
//		this.rfidDriver = rfidDriver;
//	}

	@Override
	public void handleExternalEvent(CSSEvent arg0) {
		
	}

	@Override
	public void handleInternalEvent(InternalEvent event) {
//		if (event.geteventInfo() instanceof RFIDUpdateEvent){
//			RFIDUpdateEvent rfidEvent = (RFIDUpdateEvent) event.geteventInfo();
//			this.sendUpdate(rfidEvent.getWakeupUnit(), rfidEvent.getRFIDTagNumber());
//			this.logging.debug("Received RFIDUpdateEvent: "+rfidEvent.getWakeupUnit()+" - "+rfidEvent.getRFIDTagNumber() );
//		}
		
		
		HashMap<String, String> payload = (HashMap<String, String>)event.geteventInfo();
		this.logging.debug("Received RFIDUpdateEvent: "+payload.get("wakeupUnit")+" - "+ payload.get("tagNumber"));
		this.sendUpdate(payload.get("wakeupUnit"), payload.get("tagNumber"));
		this.logging.debug("Sent rfid update WU:"+payload.get("wakeupUnit")+" tag: "+payload.get("tagNumber"));
		
		
	}





	@Override
	public Object addingService(ServiceReference reference) {
		
		iDevice = (IDevice) bundleContext.getService(reference);

		
		return iDevice;
	}

	@Override
	public void modifiedService(ServiceReference reference, Object service) {

		
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		
	}
}