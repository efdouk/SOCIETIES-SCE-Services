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
package org.societies.thirdparty.sharedCalendar.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.comm.xmpp.datatypes.Stanza;
import org.societies.api.comm.xmpp.datatypes.XMPPInfo;
import org.societies.api.comm.xmpp.exceptions.CommunicationException;
import org.societies.api.comm.xmpp.exceptions.XMPPError;
import org.societies.api.comm.xmpp.interfaces.ICommCallback;
import org.societies.api.comm.xmpp.interfaces.ICommManager;
import org.societies.api.css.devicemgmt.IDevice;
import org.societies.api.css.devicemgmt.display.DisplayEvent;
import org.societies.api.css.devicemgmt.display.DisplayEventConstants;
import org.societies.api.css.devicemgmt.display.IDisplayDriver;
import org.societies.api.css.devicemgmt.display.IDisplayableService;
import org.societies.api.ext3p.schema.sharedcalendar.Event;
import org.societies.api.ext3p.schema.sharedcalendar.MethodType;
import org.societies.api.ext3p.schema.sharedcalendar.SharedCalendarBean;
import org.societies.api.identity.IIdentity;
import org.societies.api.identity.IIdentityManager;
import org.societies.api.identity.InvalidFormatException;
import org.societies.api.osgi.event.CSSEvent;
import org.societies.api.osgi.event.CSSEventConstants;
import org.societies.api.osgi.event.EventListener;
import org.societies.api.osgi.event.EventTypes;
import org.societies.api.osgi.event.IEventMgr;
import org.societies.api.osgi.event.InternalEvent;
import org.societies.api.services.IServices;
import org.societies.thirdparty.sharedCalendar.api.ICalendarResultCallback;
import org.societies.thirdparty.sharedCalendar.api.ISharedCalendarClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * This class implement functionalities to interact with the server part of the SharedCalendar service
 * 
 * @author solutanet
 * 
 */
public class SharedCalendarClientRich extends EventListener implements ICommCallback, ISharedCalendarClient, IDisplayableService {
	
	private static final Logger log = LoggerFactory.getLogger(SharedCalendarClientRich.class);
	private ICommManager commManager;
	private IIdentityManager idMgr;
	private IEventMgr evtMgr;
	private IServices serviceMgmt;
	//70n1 variables used for the integration with screen device.
	private IDisplayDriver displayDriverService;
	private String myServiceName;
	private URL myServiceExeURL;
	private boolean deviceAvailable = false;
	
	
	/**
	 * This is the set of all available instances of the IDevice interface.
	 * A DeviceListener bean instance tracks whenever a new IDevice is bound or unbound.
	 * See http://static.springsource.org/osgi/docs/1.2.1/reference/html-single/#service-registry:refs:collection:dynamics
	 */
	private Set<IDevice> availableDevices;
	
	private static final List<String> NAMESPACES = Collections
			.unmodifiableList(Arrays
					.asList("http://societies.org/api/ext3p/schema/sharedCalendar"));
	private static final List<String> PACKAGES = Collections
			.unmodifiableList(Arrays
					.asList("org.societies.api.ext3p.schema.sharedcalendar"));

	/** The JID of the server running the calendar service */
	private String serviceServer;
	
	public SharedCalendarClientRich() {
		super();
	}
	
	public SharedCalendarClientRich(String serviceServer) {
		super();
		this.serviceServer = serviceServer;
	}

	// PROPERTIES
	public ICommManager getCommManager() {
		return commManager;
	}

	public void setCommManager(ICommManager commManager) {
		this.commManager = commManager;
	}
	
	public IServices getServiceMngmt() {
		return serviceMgmt;
	}

	public void setServiceMngmt(IServices serviceMgmt) {
		this.serviceMgmt = serviceMgmt;
	}
	
	/**
	 * This method is used to retrieve the jid of the Server where the 3rd party
	 * service is deployed. In a future version this property is loaded
	 * dynamically from xml file.
	 * 
	 * @return the jid of the server where the service is deployed
	 */
	public String getServiceServer() {
		
		return getServiceMngmt().getServer(getServiceMngmt().getMyServiceId(SharedCalendarClientRich.class)).getJid();
	}

	public IIdentityManager getIdMgr() {
		return idMgr;
	}

	public void setIdMgr(IIdentityManager idMgr) {
		this.idMgr = idMgr;
	}

	public void InitService() {
		// REGISTER OUR ServiceManager WITH THE XMPP Communication Manager
		try {
			getCommManager().register(this);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		idMgr = commManager.getIdManager();
		
		if (this.displayDriverService != null) {
			// register for display events
			this.registerForDisplayEvents();
//			try {
				try {
					this.myServiceExeURL = new URL("http://www.macs.hw.ac.uk/~ceeep1/societies/services/MockWindowsExecutable.exe");
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.myServiceName = "Shared Calendar Client";
				this.displayDriverService.registerDisplayableService(null, myServiceName, myServiceExeURL, false);
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}else{
			this.log.debug("Display Driver Service not available");
		}
	}
	//70n1
	//Register for display events
	private void registerForDisplayEvents() {
		String eventFilter = "(&" + 
				"(" + CSSEventConstants.EVENT_NAME + "=displayUpdate)" +
				"(" + CSSEventConstants.EVENT_SOURCE + "=org/societies/css/device)" +
				")";
		this.evtMgr.subscribeInternalEvent(this, new String[]{EventTypes.DISPLAY_EVENT}, eventFilter);
		this.log.debug("Subscribed to "+EventTypes.DISPLAY_EVENT+" events");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.api.comm.xmpp.interfaces.ICommCallback#getXMLNamespaces()
	 */
	@Override
	public List<String> getXMLNamespaces() {
		
		return NAMESPACES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.api.comm.xmpp.interfaces.ICommCallback#getJavaPackages()
	 */
	@Override
	public List<String> getJavaPackages() {
		
		return PACKAGES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.api.comm.xmpp.interfaces.ICommCallback#receiveResult(org
	 * .societies.api.comm.xmpp.datatypes.Stanza, java.lang.Object)
	 */
	@Override
	public void receiveResult(Stanza stanza, Object payload) {
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.api.comm.xmpp.interfaces.ICommCallback#receiveError(org
	 * .societies.api.comm.xmpp.datatypes.Stanza,
	 * org.societies.api.comm.xmpp.exceptions.XMPPError)
	 */
	@Override
	public void receiveError(Stanza stanza, XMPPError error) {
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.api.comm.xmpp.interfaces.ICommCallback#receiveInfo(org.
	 * societies.api.comm.xmpp.datatypes.Stanza, java.lang.String,
	 * org.societies.api.comm.xmpp.datatypes.XMPPInfo)
	 */
	@Override
	public void receiveInfo(Stanza stanza, String node, XMPPInfo info) {
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.api.comm.xmpp.interfaces.ICommCallback#receiveItems(org
	 * .societies.api.comm.xmpp.datatypes.Stanza, java.lang.String,
	 * java.util.List)
	 */
	@Override
	public void receiveItems(Stanza stanza, String node, List<String> items) {
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.api.comm.xmpp.interfaces.ICommCallback#receiveMessage(org
	 * .societies.api.comm.xmpp.datatypes.Stanza, java.lang.Object)
	 */
	@Override
	public void receiveMessage(Stanza stanza, Object payload) {
	}
	
	private IIdentity retrieveTargetIdentity(){
		IIdentity result = null;

		if (idMgr != null){
			result = getServiceMngmt().getServer(getServiceMngmt().getMyServiceId(SharedCalendarClientRich.class));
		}else{
				log.error("Identity Manager is NOT available.");
		}

		return result;
	}

	//START CIS CALENDAR IMPLEMENTATION METHODS//
	
	public void retrieveCISCalendars(
			ICalendarResultCallback returnedResultCallback, String CISId) {
		
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean.setMethod(MethodType.RETRIEVE_CIS_CALENDAR_LIST);
		calendarBean.setCISId(CISId);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for retrieve all public calendar.");
	}
	
	private void sendMsg(Stanza stanza, SharedCalendarBean calendarBean, SharedCalendarCallBack callback, String explanation){
		try {
			// SEND INFORMATION QUERY - RESPONSE WILL BE IN
			// "callback.RecieveMessage()"
			commManager.sendIQGet(stanza, calendarBean, callback);
			log.info(explanation);
		} catch (CommunicationException e) {
			log.error("ERROR: " + e.getStackTrace()[0].getMethodName());
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#createCISCalendar(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback, java.lang.String, java.lang.String)
	 */
	@Override
	public void createCISCalendar(
			ICalendarResultCallback returnedResultCallback,
			String calendarSummary, String CISId) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean.setMethod(MethodType.CREATE_CIS_CALENDAR);
		calendarBean.setCalendarSummary(calendarSummary);
		calendarBean.setCISId(CISId);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for create a CSS calendar with summary: "
					+ calendarSummary + ".");
		if (this.deviceAvailable) this.displayDriverService.sendNotification(this.getClass().getSimpleName(), "Created CIS Calendar '"+calendarSummary+"' with ID:"+CISId);
	}

	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#deleteCISCalendar(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback, java.lang.String)
	 */
	@Override
	public void deleteCISCalendar(
			ICalendarResultCallback returnedResultCallback, String calendarId) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean
				.setMethod(MethodType.DELETE_CIS_CALENDAR);
		calendarBean.setCalendarId(calendarId);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for delete a CIS calendar with id: "
				+ calendarId + ".");			
	}

	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#retrieveCISCalendarEvents(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback, java.lang.String)
	 */
	@Override
	public void retrieveCISCalendarEvents(
			ICalendarResultCallback returnedResultCallback, String calendarId) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean
				.setMethod(MethodType.RETRIEVE_CIS_CALENDAR_EVENTS);
		calendarBean.setCalendarId(calendarId);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for retrieve CIS calendar events in calendar with id: "
				+ calendarId + ".");		
	}

	
	
	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#createEventOnCISCalendar(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback, Event, java.lang.String)
	 */
	@Override
	public void createEventOnCISCalendar(
			ICalendarResultCallback returnedResultCallback, Event newEvent,
			String calendarId) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean
				.setMethod(MethodType.CREATE_EVENT_ON_CIS_CALENDAR);
		calendarBean.setCalendarId(calendarId);
		calendarBean.setNewEvent(newEvent);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for create an event on CIS calendar with id: "
				+ calendarId + ".");	
		if (this.deviceAvailable) this.displayDriverService.sendNotification(this.getClass().getSimpleName(), "Created event '"+newEvent.getEventSummary()+"' on Calendar with ID:"+calendarId);
	}

	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#deleteEventOnCISCalendar(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback, java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteEventOnCISCalendar(
			ICalendarResultCallback returnedResultCallback, String eventId,
			String calendarId) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean
				.setMethod(MethodType.DELETE_EVENT_ON_CIS_CALENDAR);
		calendarBean.setCalendarId(calendarId);
		calendarBean.setEventId(eventId);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for delete an event with id: "+eventId+" on CIS calendar with id: "
				+ calendarId + ".");
		if (this.deviceAvailable) this.displayDriverService.sendNotification(this.getClass().getSimpleName(), "Deleted event with id '"+eventId+"' on Calendar with ID:"+calendarId);
	}
	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#subscribeToEvent(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void subscribeToEvent(
			ICalendarResultCallback returnedResultCallback, String calendarId,
			String eventId, String subscriberId) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean
				.setMethod(MethodType.SUBSCRIBE_TO_EVENT);
		calendarBean.setCalendarId(calendarId);
		calendarBean.setEventId(eventId);
		calendarBean.setSubscriberId(subscriberId);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for subscribe to an event with id: "+eventId+" on CIS calendar with id: "
				+ calendarId + " and subscriber id: "+subscriberId+".");
		if (this.deviceAvailable) this.displayDriverService.sendNotification(this.getClass().getSimpleName(), "Subscribed to event with id '"+eventId+" on CIS calendar with id: "
				+ calendarId + " and subscriber id: "+subscriberId+".");
	}

	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#findEvents(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback, java.lang.String, java.lang.String)
	 */
	@Override
	public void findEvents(
			ICalendarResultCallback returnedResultCallback, String calendarId,
			String keyWord) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean
				.setMethod(MethodType.FIND_EVENTS);
		calendarBean.setCalendarId(calendarId);
		calendarBean.setKeyWord(keyWord);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for retrieve events on CIS calendar with id: "
				+ calendarId + " using keyword: "+keyWord+".");
		
	}

	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#unsubscribeFromEvent(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void unsubscribeFromEvent(
			ICalendarResultCallback returnedResultCallback, String calendarId,
			String eventId, String subscriberId) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean
				.setMethod(MethodType.UNSUBSCRIBE_FROM_EVENT);
		calendarBean.setCalendarId(calendarId);
		calendarBean.setEventId(eventId);
		calendarBean.setSubscriberId(subscriberId);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for unsubscribe to an event with id: "+eventId+" on CIS calendar with id: "
				+ calendarId + " and subscriber id: "+subscriberId+".");
		if (this.deviceAvailable) this.displayDriverService.sendNotification(this.getClass().getSimpleName(), "Unsubscribed from event with id '"+eventId+" on CIS calendar with id: "
				+ calendarId + " and subscriber id: "+subscriberId+".");
	}
	
	//START CSS CALENDAR IMPLEMENTATION METHODS//
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich
	 * #
	 * retrieveCSSCalendarEvents(org.societies.thirdparty.sharedCalendar.client.interfaces
	 * .ICalendarResultCallback)
	 */
	@Override
	public void retrieveEventsPrivateCalendar(
			ICalendarResultCallback returnedResultCallback) {
		
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean.setMethod(MethodType.RETRIEVE_EVENTS_ON_PRIVATE_CALENDAR);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for retrieve CSS calendar events.");
	}

	public void createPrivateCalendar(
			ICalendarResultCallback returnedResultCallback,
			String calendarSummary) {
		
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean.setMethod(MethodType.CREATE_PRIVATE_CALENDAR);
		calendarBean.setCalendarSummary(calendarSummary);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for create a CSS calendar with summary: "
				+ calendarSummary + ".");
		if (this.deviceAvailable) this.displayDriverService.sendNotification(this.getClass().getSimpleName(), "Created Private (CSS) Calendar with summary '"+calendarSummary+"'");
	}
	


	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#deletePrivateCalendar(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback)
	 */
	@Override
	public void deletePrivateCalendar(
			ICalendarResultCallback returnedResultCallback) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean.setMethod(MethodType.DELETE_PRIVATE_CALENDAR);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for delete a CSS calendar.");
		if (this.deviceAvailable) this.displayDriverService.sendNotification(this.getClass().getSimpleName(), "Deleted Private (CSS) Calendar");
	}

	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#createEventOnPrivateCalendar(org.societies.thirdparty.sharedCalendar.client.interfaces.ICalendarResultCallback, Event)
	 */
	@Override
	public void createEventOnPrivateCalendar(
			ICalendarResultCallback returnedResultCallback, Event newEvent) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean.setMethod(MethodType.CREATE_EVENT_ON_PRIVATE_CALENDAR);
		calendarBean.setNewEvent(newEvent);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for create a CSS calendar events.");
		if (this.deviceAvailable) this.displayDriverService.sendNotification(this.getClass().getSimpleName(), "Created event on Private (CSS) Calendar with summary '"+newEvent.getEventSummary()+"'");
	}
	
	/* (non-Javadoc)
	 * @see org.societies.thirdparty.sharedCalendar.client.interfaces.ISharedCalendarClientRich#deleteEventOnPrivateCalendar(java.lang.String)
	 */
	@Override
	public void deleteEventOnPrivateCalendar(ICalendarResultCallback returnedResultCallback,String eventId) {
		Stanza stanza = new Stanza(retrieveTargetIdentity());

		// SETUP CALENDAR CLIENT RETURN STUFF
		SharedCalendarCallBack callback = new SharedCalendarCallBack(
				stanza.getId(), returnedResultCallback);

		// CREATE MESSAGE BEAN
		SharedCalendarBean calendarBean = new SharedCalendarBean();

		calendarBean.setMethod(MethodType.DELETE_EVENT_ON_PRIVATE_CALENDAR);
		calendarBean.setEventId(eventId);
		this.sendMsg(stanza, calendarBean, callback, "The message was sent to XMPP server for delete a CSS calendar event.");
		if (this.deviceAvailable) this.displayDriverService.sendNotification(this.getClass().getSimpleName(), "Deleted event on Private (CSS) Calendar with id '"+eventId+"'");
	}
	
	//UTILITY METHODS//
	/**
	 * This method create the list JSON objects (compatible with the presentation framewok jquery-weekcalendar-1.2.2) starting from a list of events.
	 * @param eventListToRender
	 * @return the String that represent the Json array
	 */
	public String createJSONOEvents(List<Event> eventListToRender){
		/*
	  "id":10182,
      "start":"2009-05-03T14:00:00.000+10:00",
      "end":"2009-05-03T15:00:00.000+10:00",
      "title":"Dev Meeting"
      */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ");
		Gson gson = new GsonBuilder().create();
		String result = "";//gson.toJson(eventListToRender);
		//log.debug("JSON Representation1:"+result);
		
		JsonArray jsonArray=new JsonArray();
		JsonObject object= null;
		
		for (Event event : eventListToRender) {
			object=new JsonObject();
			object.addProperty("id", event.getEventId());
			object.addProperty("start", sdf.format(XMLGregorianCalendarConverter.asDate(event.getStartDate())));
			object.addProperty("end", sdf.format(XMLGregorianCalendarConverter.asDate(event.getEndDate())));
			object.addProperty("title", event.getEventDescription());
			jsonArray.add(object);
		}
		result = jsonArray.toString();
		log.debug("JSON Representation2:"+result);
		return result;
	}

	public IEventMgr getEvtMgr() {
		return evtMgr;
	}

	public void setEvtMgr(IEventMgr evtMgr) {
		this.evtMgr = evtMgr;
	}

	public Set<IDevice> getAvailableDevices() {
		return availableDevices;
	}

	public void setAvailableDevices(Set<IDevice> availableDevices) {
		this.availableDevices = availableDevices;
	}

	/* (non-Javadoc)
	 * @see org.societies.api.osgi.event.EventListener#handleInternalEvent(org.societies.api.osgi.event.InternalEvent)
	 */
	@Override
	public void handleInternalEvent(InternalEvent event) {
		if (event.geteventInfo() instanceof DisplayEvent){			
			DisplayEvent eventObj  = (DisplayEvent) event.geteventInfo();
			log.debug("Handling display event:"+eventObj);
			if (eventObj.getDisplayStatus().equals(DisplayEventConstants.DEVICE_AVAILABLE)){
				this.deviceAvailable = true;
			}else{
				this.deviceAvailable  = false;
			}
		}
		// TODO Auto-generated method stub
		//this.displayDriverService.sendNotification(myServiceName, "Hello, I am an example service and I wanted to notify you that I can send you notifications!");		
	}

	/* (non-Javadoc)
	 * @see org.societies.api.osgi.event.EventListener#handleExternalEvent(org.societies.api.osgi.event.CSSEvent)
	 */
	@Override
	public void handleExternalEvent(CSSEvent event) {
		if(log.isDebugEnabled())
			log.debug("handleExternalEvent: " + event.geteventName());
		
	}

	public IDisplayDriver getDisplayDriverService() {
		return displayDriverService;
	}

	public void setDisplayDriverService(IDisplayDriver displayDriverService) {
		this.displayDriverService = displayDriverService;
	}

	public URL getMyServiceExeURL() {
		return myServiceExeURL;
	}

	public void setMyServiceExeURL(URL myServiceExeURL) {
		if(log.isDebugEnabled()) 
			log.debug("myServiceExeURL: " + myServiceExeURL);
		this.myServiceExeURL = myServiceExeURL;
	}

	@Override
	public void serviceStarted(String arg0) {
		if(log.isDebugEnabled()) 
			log.debug("serviceStarted: " + arg0);
		
	}

	@Override
	public void serviceStopped(String arg0) {
		if(log.isDebugEnabled()) 
			log.debug("serviceStopped: " + arg0);
		
	}
	
}
