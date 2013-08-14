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
package ac.hw.services.socialLearning.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.comm.xmpp.datatypes.Stanza;
import org.societies.api.comm.xmpp.exceptions.CommunicationException;
import org.societies.api.comm.xmpp.interfaces.ICommManager;
import org.societies.api.context.CtxException;
import org.societies.api.context.broker.ICtxBroker;
import org.societies.api.context.model.CtxAttribute;
import org.societies.api.context.model.CtxAttributeValueType;
import org.societies.api.context.model.CtxEntity;
import org.societies.api.context.model.CtxEntityIdentifier;
import org.societies.api.context.model.CtxIdentifier;
import org.societies.api.context.model.CtxModelType;
import org.societies.api.css.devicemgmt.display.DisplayEvent;
import org.societies.api.css.devicemgmt.display.DisplayEventConstants;
import org.societies.api.css.devicemgmt.display.IDisplayDriver;
import org.societies.api.css.devicemgmt.display.IDisplayableService;
import org.societies.api.identity.IIdentity;
import org.societies.api.identity.IIdentityManager;
import org.societies.api.identity.InvalidFormatException;
import org.societies.api.identity.RequestorService;
import org.societies.api.internal.servicelifecycle.ServiceModelUtils;
import org.societies.api.osgi.event.CSSEvent;
import org.societies.api.osgi.event.CSSEventConstants;
import org.societies.api.osgi.event.EventTypes;
import org.societies.api.osgi.event.IEventMgr;
import org.societies.api.osgi.event.EventListener;
import org.societies.api.osgi.event.InternalEvent;
import org.societies.api.personalisation.model.IAction;
import org.societies.api.personalisation.model.IActionConsumer;
import org.societies.api.personalisation.model.PersonalisablePreferenceIdentifier;
import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
import org.societies.api.services.IServices;
import org.societies.api.services.ServiceMgmtEvent;
import org.societies.api.services.ServiceMgmtEventType;

import ac.hw.services.socialLearning.app.comms.CommsServerListener;
import ac.hw.services.socialLearning.app.comms.ISocialLearningServer;

import org.societies.api.sociallearning.schema.serverbean.SocialLearningMethodType;
import org.societies.api.sociallearning.schema.serverbean.SocialLearningServerBean;


/**
 * Describe your class here...
 *
 * @author Eliza
 *
 */
public class SocialLearningService extends EventListener implements IDisplayableService{

	private IDisplayDriver displayDriverService;
	private Logger logging = LoggerFactory.getLogger(this.getClass());

	private IEventMgr evMgr;
	private ICommManager commMgr;
	private IIdentityManager idMgr;

	
	private IIdentity userIdentity;
	private String myServiceName;
	private URL myServiceExeURL;
	private boolean deviceAvailable = false;
	private ICtxBroker ctxBroker;
	private RequestorService requestor;
	
	private String gameUserName = "";
	private IIdentity serverIdentity;
	private ISocialLearningServer server;
	private ServiceResourceIdentifier serverServiceId;
	private CtxAttribute gameUserCtxAttribute;
	private IServices serviceMgmt;
	
	private Thread serverThread;
	private CommsServerListener commsServerListener;
	
	private int listenerPort;
	private String listernAddress;
	
	
	public void Init(){
		//FIRST REGISTER FOR SERVICE EVENTS
		this.registerForServiceEvents();
		//THEN REGISTER WITH IDISPLAYPORTAL
		this.registerForDisplayEvents();
		
		//SET UP SOCKET TO LISTEN FROM GUI (C#)
		this.commsServerListener = new CommsServerListener();
		//GET PORT & ADDRESS
		this.listenerPort=this.commsServerListener.getSocket();
		this.listernAddress=this.commsServerListener.getAddress();
		//START LISTENING
		this.serverThread = new Thread(commsServerListener);
		this.serverThread.start();
		
		if(this.commMgr==null)
		{
			logging.debug("COMM IS NULL!");
		}
		else
		{
			logging.debug("COMM ISNT NULL");
		}
		/*
		this.registerForDisplayEvents();
		try {
			
			this.myServiceExeURL = new URL("http://www.macs.hw.ac.uk/~ceeep1/societies/services/SocialLearningGame.exe");
			this.myServiceName = "Social Learning Game";
			this.displayDriverService.registerDisplayableService(this, myServiceName, myServiceExeURL, false);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		UIManager.put("ClassLoader", ClassLoader.getSystemClassLoader());
		//myGui = new ExampleServiceGUI(this); */
	}
	

	
	/*
	 * Register for events from SLM so I can get my service parameters and finish initialising
	 */
	private void registerForServiceEvents(){
		String eventFilter = "(&" + 
				"(" + CSSEventConstants.EVENT_NAME + "="+ServiceMgmtEventType.SERVICE_STARTED+")" +
				"(" + CSSEventConstants.EVENT_SOURCE + "=org/societies/servicelifecycle)" +
				")";
		this.evMgr.subscribeInternalEvent(this, new String[]{EventTypes.SERVICE_LIFECYCLE_EVENT}, eventFilter);
		this.logging.debug("Subscribed to "+EventTypes.SERVICE_LIFECYCLE_EVENT+" events");
	}

	private void unregisterForServiceEvents()
	{
		String eventFilter = "(&" + 
				"(" + CSSEventConstants.EVENT_NAME + "="+ServiceMgmtEventType.NEW_SERVICE+")" +
				"(" + CSSEventConstants.EVENT_SOURCE + "=org/societies/servicelifecycle)" +
				")";

		this.evMgr.unSubscribeInternalEvent(this, new String[]{EventTypes.SERVICE_LIFECYCLE_EVENT}, eventFilter);
		//this.evMgr.subscribeInternalEvent(this, new String[]{EventTypes.SERVICE_LIFECYCLE_EVENT}, eventFilter);
		this.logging.debug("Unsubscribed from "+EventTypes.SERVICE_LIFECYCLE_EVENT+" events");
	}
	
	/*
	 * Register for display events
	 */
	private void registerForDisplayEvents() {
		String eventFilter = "(&" + 
				"(" + CSSEventConstants.EVENT_NAME + "=displayUpdate)" +
				"(" + CSSEventConstants.EVENT_SOURCE + "=org/societies/css/device)" +
				")";
		this.evMgr.subscribeInternalEvent(this, new String[]{EventTypes.DISPLAY_EVENT}, eventFilter);
		this.logging.debug("Subscribed to "+EventTypes.DISPLAY_EVENT+" events");

	}
	
	@Override
	public void handleInternalEvent(InternalEvent event) {
		logging.debug("Received internal event: "+event.geteventName());

	//	if(event.geteventName().equalsIgnoreCase("NEW_SERVICE")){
			//logging.debug("Received SLM event");
			ServiceMgmtEvent slmEvent = (ServiceMgmtEvent) event.geteventInfo();
			logging.debug("EventBundle: " + slmEvent.getBundleSymbolName());
			if (slmEvent.getBundleSymbolName().equalsIgnoreCase("CollabQuizServer")){
				this.logging.debug("Received SLM event for my bundle");
				if (slmEvent.getEventType().equals(ServiceMgmtEventType.SERVICE_STARTED)){
					
					//GET ID OF SERVICE FOR XMPP COMMUNICATION
					this.serverIdentity = serviceMgmt.getServer(slmEvent.getServiceId());
					logging.debug("Got my servers Identity: " + serverIdentity);
					//SEND MESSAGE TO GET REMOTE SOCKET LISTENER INFO
					SocialLearningServerBean serverBean = new SocialLearningServerBean();
					logging.debug("BEAN INIT!");
					serverBean.setMethod(SocialLearningMethodType.SERVER_SOCKET_INFO_REQUEST);
					logging.debug("BEAN METHOD CHANGED");
					Stanza stanza = new Stanza(serverIdentity);
					try {
						logging.debug("ABOUT TO SEND MESSAGE!");
						this.commMgr.sendMessage(stanza, serverBean);
						logging.debug("SENT MESSAGE TO SERVER");
					} catch (CommunicationException e) {
						StackTraceElement[] x = e.getStackTrace();
						logging.debug(e.toString());
						for(int i=0;i < x.length; i++)
						{
						logging.debug(x[i].toString());	
						}
						// TODO Auto-generated catch block
						//logging.debug(e.getStackTrace());
					}
					
					
			
				}
			
		}else if(event.geteventName().equalsIgnoreCase("displayUpdate")){
			logging.debug("Received DisplayPortal event");
		}else{
			logging.debug("Received unknown event with name: "+event.geteventName());
		}
		
			if (event.geteventInfo() instanceof DisplayEvent){
				DisplayEvent eventObj  = (DisplayEvent) event.geteventInfo();
				if (eventObj.getDisplayStatus().equals(DisplayEventConstants.DEVICE_AVAILABLE)){
					this.deviceAvailable = true;
				//	this.getDataFromContext();
				}else{
					this.deviceAvailable  = false;
				}
			}
			// TODO Auto-generated method stub
			//this.displayDriverService.sendNotification(myServiceName, "Hello, I am an example service and I wanted to notify you that I can send you notifications!");
		}
			
	
	
	
	/*private void getDataFromContext() {
		
		if (this.requestor==null){
			//TODO: replace this with method to service registry when it becomes available
			this.requestServerIdentity();
			this.serverServiceId  = this.server.getServerServiceId(serverIdentity);
			this.logging.debug("Server ID: " + serverIdentity);
			this.requestor = new RequestorService(serverIdentity, serverServiceId);
		}
		try {
			
			
			Future<List<CtxIdentifier>> futureUserNames = this.ctxBroker.lookup(requestor, userIdentity, CtxModelType.ATTRIBUTE, gameUserName);
			List<CtxIdentifier> usernames = futureUserNames.get();
			
			if (usernames.size()==0){
				String username = "";
				while (username==""){
					username = JOptionPane.showInputDialog("Please select a username to use with the Social Learning application", "");
					if (username==""){
						JOptionPane.showMessageDialog(null, "Empty username not accepted");
					}
				}
				CtxEntityIdentifier ctxEntityId= this.ctxBroker.retrieveIndividualEntityId(requestor, userIdentity).get();
				gameUserCtxAttribute = this.ctxBroker.createAttribute(requestor, ctxEntityId, this.gameUserName).get();
				gameUserCtxAttribute.setStringValue(username);
				gameUserCtxAttribute.setValueType(CtxAttributeValueType.STRING);
				ctxBroker.update(requestor, gameUserCtxAttribute);
				
				
			}else{
				gameUserCtxAttribute = (CtxAttribute) this.ctxBroker.retrieve(requestor, usernames.get(0)).get();
			}
		} catch (CtxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/


	

	
	@Override
	public void handleExternalEvent(CSSEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	
	/**
	 * @return the displayDriverService
	 */
	public IDisplayDriver getDisplayDriverService() {
		return displayDriverService;
	}

	/**
	 * @param displayDriverService the displayDriverService to set
	 */
	public void setDisplayDriverService(IDisplayDriver displayDriverService) {
		this.displayDriverService = displayDriverService;
	}


	/**
	 * @return the evMgr
	 */
	public IEventMgr getEvMgr() {
		return evMgr;
	}


	/**
	 * @param evMgr the evMgr to set
	 */
	public void setEvMgr(IEventMgr evMgr) {
		this.evMgr = evMgr;
	}


	/**
	 * @return the commMgr
	 */
	public ICommManager getCommMgr() {
		return commMgr;
	}


	/**
	 * @param commMgr the commMgr to set
	 */
	public void setCommMgr(ICommManager commMgr) {
		this.commMgr = commMgr;
		this.idMgr = commMgr.getIdManager();
		this.userIdentity = this.idMgr.getThisNetworkNode();
	}

	@Override
	public void serviceStarted(String ipAddr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serviceStopped(String ipAddr) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the ctxBroker
	 */
	public ICtxBroker getCtxBroker() {
		return ctxBroker;
	}

	/**
	 * @param ctxBroker the ctxBroker to set
	 */
	public void setCtxBroker(ICtxBroker ctxBroker) {
		this.ctxBroker = ctxBroker;
	}

	/**
	 * @return the server
	 */
//	public ISocialLearningServer getServer() {
//		return server;
//	}

	/**
	 * @param server the server to set
	 */
//	public void setServer(ISocialLearningServer server) {
//		this.server = server;
//	}

	/**
	 * @return the serviceMgmt
	 */
	public IServices getServiceMgmt() {
		return serviceMgmt;
	}

	/**
	 * @param serviceMgmt the serviceMgmt to set
	 */
	public void setServiceMgmt(IServices serviceMgmt) {
		this.serviceMgmt = serviceMgmt;
	}

	




}
