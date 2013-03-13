package org.societies.ext3p.nzone.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.societies.api.activity.IActivity;
import org.societies.api.activity.IActivityFeed;
import org.societies.api.activity.IActivityFeedManager;
import org.societies.api.cis.directory.ICisDirectoryCallback;
import org.societies.api.cis.directory.ICisDirectoryRemote;
import org.societies.api.cis.management.ICis;
import org.societies.api.cis.management.ICisManager;
import org.societies.api.cis.management.ICisManagerCallback;
import org.societies.api.cis.management.ICisOwned;
import org.societies.api.comm.xmpp.interfaces.ICommManager;
import org.societies.api.context.CtxException;
import org.societies.api.context.broker.ICtxBroker;
import org.societies.api.context.model.CtxAttribute;
import org.societies.api.context.model.CtxAttributeTypes;
import org.societies.api.context.model.CtxAttributeValueType;
import org.societies.api.context.model.CtxEntity;
import org.societies.api.context.model.CtxEntityIdentifier;
import org.societies.api.context.model.CtxIdentifier;
import org.societies.api.context.model.CtxModelObject;
import org.societies.api.context.model.CtxModelType;
import org.societies.api.context.model.CtxEntityTypes;
import org.societies.api.ext3p.nzone.client.INZoneClient;
import org.societies.api.ext3p.nzone.model.NZonePreferences;
import org.societies.api.ext3p.nzone.model.UserPreview;
import org.societies.api.identity.IIdentity;
import org.societies.api.identity.INetworkNode;
import org.societies.api.identity.Requestor;
import org.societies.api.identity.RequestorService;
import org.societies.api.personalisation.mgmt.IPersonalisationManager;
import org.societies.api.personalisation.model.Action;
import org.societies.api.personalisation.model.IAction;
import org.societies.api.personalisation.model.IActionConsumer;
import org.societies.api.personalisation.model.PersonalisablePreferenceIdentifier;
import org.societies.api.schema.cis.community.Community;
import org.societies.api.schema.cis.community.CommunityMethods;
import org.societies.api.schema.cis.community.Criteria;
import org.societies.api.schema.cis.community.MembershipCrit;
import org.societies.api.schema.cis.community.Participant;
import org.societies.api.schema.cis.community.ParticipantRole;
import org.societies.api.schema.cis.directory.CisAdvertisementRecord;
import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
import org.societies.api.services.IServices;
import org.societies.api.useragent.monitoring.IUserActionMonitor;
import org.societies.api.ext3p.schema.nzone.UserDetails;
import org.societies.api.ext3p.schema.nzone.ZoneDetails;

public class NZoneClient implements INZoneClient, IActionConsumer {

	private static Logger log = LoggerFactory.getLogger(NZoneClient.class);

	private ICtxBroker ctxBroker;
	private ICommManager commManager;
	private NZoneClientComms nzoneClientComms;
	public ICisManager cisManager;
	public ICisDirectoryRemote cisDirectoryClient;
	private IUserActionMonitor uam;
	private IPersonalisationManager persoMgr;
	IServices serviceMgmt;
	private IActivityFeedManager actFeedMgr;
	private IActivityFeed actFeed;
	private List<IActivity> tempActList;

	private ServiceResourceIdentifier myServiceID;
	private String myServiceName;
	private String myServiceType;
	private IIdentity myIdentity;

	private String nzoneServerCssID;

	private Requestor requestor;
	private RequestorService requestorService;
	private List<CisAdvertisementRecord> localCisAds = new ArrayList<CisAdvertisementRecord>();
	private boolean bJoinResponseReceived;
	private boolean bLeaveResponseReceived;
	List<Participant> memberList;

	private List<ZoneDetails> zoneDetails;

	private ICis mainCis;
	private ICis currentZoneCis;

	private static String nzoneMemberOfCxtAttr = "nzoneMemberOf";
	private static String nzoneLocationCxtAttr = "ZONE_LOCATION_SYMBOLIC";

	private static String PREF_TAG = "taggedPreference";


	private NZonePreferences preferences;

	private List<CisAdvertisementRecord> cisDirCallbackResult;

	public ICtxBroker getCtxBroker() {
		return ctxBroker;
	}

	public void setCtxBroker(ICtxBroker ctxBroker) {
		this.ctxBroker = ctxBroker;
	}

	public ICommManager getCommManager() {
		return commManager;
	}

	public void setCommManager(ICommManager commManager) {
		this.commManager = commManager;
	}

	public NZoneClientComms getNzoneClientComms() {
		return nzoneClientComms;
	}

	public void setNzoneClientComms(NZoneClientComms nzoneClientComms) {
		this.nzoneClientComms = nzoneClientComms;
	}

	public ICisManager getCisManager() {
		return cisManager;
	}

	public void setCisManager(ICisManager cisManager) {
		this.cisManager = cisManager;
	}

	public ICisDirectoryRemote getCisDirectoryClient() {
		return cisDirectoryClient;
	}

	public void setCisDirectoryClient(ICisDirectoryRemote cisDirectoryClient) {
		this.cisDirectoryClient = cisDirectoryClient;
	}

	/**
	 * @return the uam
	 */
	public IUserActionMonitor getUam() {
		return uam;
	}

	/**
	 * @param uam
	 *            the uam to set
	 */
	public void setUam(IUserActionMonitor uam) {
		this.uam = uam;
	}

	/**
	 * @return the persoMgr
	 */
	public IPersonalisationManager getPersoMgr() {
		return persoMgr;
	}

	/**
	 * @param persoMgr
	 *            the persoMgr to set
	 */
	public void setPersoMgr(IPersonalisationManager persoMgr) {
		this.persoMgr = persoMgr;
	}

	public IServices getServiceMgmt() {
		return serviceMgmt;
	}

	public void setServiceMgmt(IServices serviceMgmt) {
		this.serviceMgmt = serviceMgmt;
	}

	/**
	 * @return the actFeedMgr
	 */
	public IActivityFeedManager getActFeedMgr() {
		return actFeedMgr;
	}

	/**
	 * @param actFeedMgr
	 *            the actFeedMgr to set
	 */
	public void setActFeedMgr(IActivityFeedManager actFeedMgr) {
		this.actFeedMgr = actFeedMgr;
	}

	public NZoneClient(String networkingserver) {
		this.nzoneServerCssID = new String(networkingserver);

		myServiceName = "nzoneClient";
		myServiceType = "client";

	};

	public NZoneClient() {
		this.nzoneServerCssID = new String("admin.societies.local");

		myServiceName = "nzoneClient";
		myServiceType = "client";

	};

	public void initialize() {
		log.debug("NZoneClient bundle initialized.");

		myIdentity = getCommManager().getIdManager().getThisNetworkNode();

		log.debug("NZoneClient bundle myIdentity is." + myIdentity.getJid());
		if (requestor == null)
			requestor = new Requestor(myIdentity);

		myServiceID = getServiceMgmt().getMyServiceId(this.getClass());

		// To join any nzone zone ( including main zone), we need to that the
		// memberof
		// attribute set
		if (this.getContextAtribute(nzoneMemberOfCxtAttr) == null)
			updateContextAtribute(nzoneMemberOfCxtAttr, "true");
		if (this.getContextAtribute(nzoneLocationCxtAttr) == null)
			updateContextAtribute(nzoneLocationCxtAttr, "");

		// The ICAUIPrediction prefiction bombs out if these are sets!??
		// TODO : Check why they are needed
		if (this.getContextAtribute(CtxAttributeTypes.LOCATION_SYMBOLIC
				.toString()) == null)
			updateContextAtribute(
					CtxAttributeTypes.LOCATION_SYMBOLIC.toString(), "here");
		if (this.getContextAtribute(CtxAttributeTypes.STATUS.toString()) == null)
			updateContextAtribute(CtxAttributeTypes.STATUS.toString(), "online");

		// TODO : Need to think about if we just retrieve the cis id's from the
		// NzoneServer of the actualy cis objects
		// for now we will get the cis id's
		// Check that we can talk to NZone Server . Get the cisID of the main
		// zone
		String mainCisId = getNzoneClientComms().getMainZoneCisID();

		// Now we need to check that we are a member of this cis,
		// if not we need to join it
		if (mainCisId == null) {
			log.error("NZoneClient : Invalid configuration, No Data for Main Cis");
			return;
		}

		mainCis = joinCis(mainCisId);

		zoneDetails = getZoneDetails();

		actFeed = getActFeedMgr().getOrCreateFeed(myIdentity.getBareJid(),
				"netzoneactfeed");
		tempActList = new ArrayList<IActivity>();
		

	};

	public void cleanUp() {
		log.debug("NZoneClient bundle initialized.");

		if (currentZoneCis != null)
			leaveCis(currentZoneCis.getCisId());
		if (mainCis != null)
			leaveCis(mainCis.getCisId());

	};

	private ICis joinCis(String cisJid) {

		ICis joinedCis;
		log.debug("joinCis Start");

		// We can't join cis's we are already a member of, so check that first
		// if we are a member already, no need to do anything
		joinedCis = getCisManager().getCis(cisJid);

		if (joinedCis != null) {
			log.debug("joinCis already a member");
			return joinedCis;
		}

		CisAdvertisementRecord adRec = getCisAdvert(cisJid);

		// First we need to frig out location so we can join
		// Find necessary localtion

		List<Criteria> crit = adRec.getMembershipCrit().getCriteria();
		for (Criteria checkCrit : crit) {
			if (checkCrit.getAttrib().contentEquals(nzoneLocationCxtAttr)) {
				log.debug("joinZoneCis checkCrit.getValue1()"
						+ checkCrit.getValue1());
				updateContextAtribute(nzoneLocationCxtAttr,
						checkCrit.getValue1());
				break;
			}
		}
		;

		NZoneCisCallback cisCallback = new NZoneCisCallback();

		getCisManager().joinRemoteCIS(adRec, cisCallback.iCisManagerCallback);

		try {
			cisCallback.cisManagerCallbackSignal.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		if (cisCallback.bResponseReceived)
			joinedCis = getCisManager().getCis(cisJid);

		log.debug("joinZoneCis finsihed");
		if (joinedCis != null)
			return joinedCis;

		log.error("joinZoneCis problem");
		return null; // problem joining
	}

	private void leaveCis(String cisJid) {

		log.debug("leaveCis Start");
		ICis cisToLeave;

		// We can't leave cis's we are not a member of, so check that first
		// if we are not a member , no need to do anything
		cisToLeave = getCisManager().getCis(cisJid);

		if (cisToLeave == null)
			return;

		NZoneCisCallback cisCallback = new NZoneCisCallback();
		getCisManager().leaveRemoteCIS(cisJid, cisCallback.iCisManagerCallback);
		try {
			cisCallback.cisManagerCallbackSignal.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cisToLeave = getCisManager().getCis(cisJid);

		if (cisToLeave != null) {
			// Problem leave cis
			log.error("Problem leaving Cis");
		}
		log.debug("joinZoneCis End");
		return;
	}

	@Override
	public boolean bJoinZone(String zoneID) {
		if (currentZoneCis != null) {
			if (currentZoneCis.getCisId().contentEquals(zoneID) == true) {
				// We are already a member of this zone, just return true
				return true;
			}
			// if we get this far, we need to leave current zone before entered
			// new zone
			this.leaveCis(currentZoneCis.getCisId());
		}
		// ok, now we can go ahead a join the new zone
		// Until we are connected up to the localtion management system, we
		// need to set our location manually in context

		// find the zonedetails
		if (zoneDetails == null) // should never happen as we call it on itin,
									// but just in case
			zoneDetails = getZoneDetails();
		for (int i = 0; i < zoneDetails.size(); i++) {
			if (zoneDetails.get(i).getCisid().contentEquals(zoneID) == true)
				updateContextAtribute(nzoneLocationCxtAttr, zoneDetails.get(i)
						.getZonelocation());
		}

		// Now we are ready to join
		currentZoneCis = this.joinCis(zoneID);

		if (currentZoneCis == null)
			return false;

		return true;

	}

	@Override
	public List<UserPreview> getSuggestedList(boolean bMainZone) {
		// Return a list of suggest contacts from the current zone,
		// if no current zone, return a list from main zone
		List<UserPreview> sortedlist = new ArrayList<UserPreview>();
		List<UserPreview> list = new ArrayList<UserPreview>();

		// TODO start: This need to change when css mnager suggest friends
		// working
		if ((bMainZone == false) && (currentZoneCis != null)) {
			memberList = null;
			NZoneCisCallback cisCallback = new NZoneCisCallback();

			currentZoneCis.getListOfMembers(requestor,
					cisCallback.iCisManagerCallback);

			try {
				cisCallback.cisManagerCallbackSignal.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			memberList = cisCallback.memberList;

			if (memberList == null) {
				log.error("Problem getting member list for cis"
						+ currentZoneCis.getName());
				return null;
			}

			// TODO end: This need to change when css mnager suggest friends
			// working
			// otherwise, we now have list, get there details
			List<String> suggestionsIDs = new ArrayList<String>();

			for (int i = 0; i < memberList.size(); i++) {
				suggestionsIDs.add(memberList.get(i).getJid());
			}

			List<UserDetails> userDets = this.getNzoneClientComms()
					.getUserDetailsList(suggestionsIDs);
			List<String> tags = new ArrayList<String>();
			tags.add("Learned Preference");

			// Now populate UserPreview
			for (int i = 0; i < memberList.size(); i++) {
				if ((memberList.get(i).getRole() != ParticipantRole.OWNER)
						&& !memberList.get(i).getJid()
								.contains(myIdentity.getBareJid())) {
					UserPreview userPre = new UserPreview();
					userPre.setUserid(memberList.get(i).getJid());
					log.debug("userPre.getUserid[" + i + "] id is : "
							+ userPre.getUserid());
					if (userDets != null) {
						for (int j = 0; j < userDets.size(); j++) {
							log.debug("checking userDets.getUserid[" + j
									+ "] : " + userDets.get(j).getUserid());

							if ((userDets.get(j) != null)
									&& (userDets.get(j).getUserid() != null)) {
								if (userPre.getUserid().contentEquals(
										userDets.get(j).getUserid())) {
									userPre.setCompany(userDets.get(j)
											.getCompany());
									userPre.setDisplayName(userDets.get(j)
											.getDisplayName());
									if (userDets.get(j).getCompany() != "") {
										// Check is we have 'tagged' this
										// company
										// get UserPrefences
										if (preferences != null) {
											if (preferences.isPreferred(
													"company", userDets.get(j)
															.getCompany())) {
												log.debug("Tagging user as preferred");
												userPre.setTags(tags);
											}
										}
									}
								}
							}
						}
					}
					list.add(userPre);
				}
			}
		} else {
			memberList = null;
			NZoneCisCallback cisCallback = new NZoneCisCallback();
			mainCis.getListOfMembers(requestor, cisCallback.iCisManagerCallback);

			try {
				cisCallback.cisManagerCallbackSignal.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			memberList = cisCallback.memberList;
			if (memberList == null) {
				log.error("Problem getting member list for cis"
						+ mainCis.getName());
				return null;
			}

			// TODO end: This need to change when css mnager suggest friends
			// working
			// otherwise, we now have list, get there details
			List<String> suggestionsIDs = new ArrayList<String>();

			for (int i = 0; i < memberList.size(); i++) {
				suggestionsIDs.add(memberList.get(i).getJid());
			}

			List<UserDetails> userDets = this.getNzoneClientComms()
					.getUserDetailsList(suggestionsIDs);
			List<String> tags = new ArrayList<String>();
			tags.add("Learned Preference");

			// Now populate UserPreview
			for (int i = 0; i < memberList.size(); i++) {
				if ((memberList.get(i).getRole() != ParticipantRole.OWNER)
						&& !memberList.get(i).getJid()
								.contains(myIdentity.getBareJid())) {
					UserPreview userPre = new UserPreview();
					userPre.setUserid(memberList.get(i).getJid());

					log.debug("userPre.setUserid[" + i + "] id is : "
							+ userPre.getUserid());

					if (userDets != null) {
						for (int j = 0; j < userDets.size(); j++) {
							log.debug("checking userDets.getUserid[" + j
									+ "] : " + userDets.get(j).getUserid());

							if ((userDets.get(j) != null)
									&& (userDets.get(j).getUserid() != null)) {
								if (userPre.getUserid().contentEquals(
										userDets.get(j).getUserid())) {
									userPre.setCompany(userDets.get(j)
											.getCompany());
									userPre.setDisplayName(userDets.get(j)
											.getDisplayName());
									if (userDets.get(j).getCompany() != "") {
										// Check is we have 'tagged' this
										// company
										// get UserPrefences
										if (preferences != null) {
											if (preferences.isPreferred(
													"company", userDets.get(j)
															.getCompany())) {
												log.debug("Tagging user as preferred");
												userPre.setTags(tags);
											}
										}
									}
								}
							}
						}
					}
					list.add(userPre);
				}
			}
		}

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getTags().size() > 0) {
				sortedlist.add(list.get(i));
			}
		}

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getTags().size() == 0) {
				sortedlist.add(list.get(i));
			}
		}

		return sortedlist;
	}

	@Override
	public void saveShareInfo() {
	};

	@Override
	public void getActivityFeed(boolean bMainZone) {
	};

	@Override
	public void sendSocFR() {
	};

	@Override
	public void getShareInfo() {
	};

	@Override
	public void saveMyProfile() {
	};

	@Override
	public void saveExtraInfo() {
	};

	@Override
	public void posttoSN() {
	};

	@Override
	public void setAsPreferred(String type, String value) {

		IActivity act = actFeed.getEmptyIActivity();
		act.setActor("User");
		act.setVerb("added");
		act.setObject(value + " as a preferred" + type);
		// actFeed.addActivity(act, null);
		// Temporary activty list until activty fed usable
		tempActList.add(act);

		// get UserPrefences
		if (preferences == null) {
			 preferences = new NZonePreferences(getUserPreference(PREF_TAG));
			// TODO : TEmp, bypassing Personalisation Manager
			//preferences = new NZonePreferences(getNzoneClientComms()
			//		.getPreferences());
			log.debug("Loaded prefererces are " + preferences.toString());
		}
		preferences.addPreferred(type, value);
		log.debug("New prefererces are " + preferences.toString());
		processUserPreference(PREF_TAG, preferences.toString());
		getNzoneClientComms().savePreferences(preferences.toString());

		NZonePreferences temppreferences = new NZonePreferences(
				getUserPreference(PREF_TAG));
		log.debug("Loaded temppreferences are " + temppreferences.toString());

		// TODO : tempo Save to database

	}

	@Override
	public boolean isPreferred(String type, String value) {

		// get UserPrefences
		if (preferences == null) {
			return false;
		}

		return preferences.isPreferred(type, value);

	}

	@Override
	public void removeAsPreferred(String type, String value) {
		IActivity act = actFeed.getEmptyIActivity();
		act.setActor("User");
		act.setVerb("removed");
		act.setObject(value + " removed as a preferred" + type);
		// actFeed.addActivity(act, null);

		// Temporary activty list until activty fed usable
		tempActList.add(act);

		// get UserPrefences
		if (preferences == null) {
			 preferences = new NZonePreferences(getUserPreference(PREF_TAG));
			// TODO : TEmp, bypassing Personalisation Manager
			preferences = new NZonePreferences(getNzoneClientComms()
					.getPreferences());
			log.debug("Loaded prefererces are " + preferences.toString());
		}
		preferences.removePreferred(type, value);
		log.debug("New prefenreces are " + preferences.toString());

		processUserPreference(PREF_TAG, preferences.toString());

		getNzoneClientComms().savePreferences(preferences.toString());

		NZonePreferences temppreferences = new NZonePreferences(
				getUserPreference(PREF_TAG));
		log.debug("Loaded temppreferences are " + temppreferences.toString());

	}

	/*
	 * @Override public List<ZoneDetails> getZoneDetails(){
	 * 
	 * return getNzoneClientComms().getZoneDetails(); };
	 */
	private void updateContextAtribute(String ctxAttribName, String value) {
		log.debug("updateContextAtribute Start");

		try {
			// retrieve the CtxEntityIdentifier of the CSS owner context entity
			CtxEntityIdentifier ownerEntityId = this
					.getCtxBroker()
					.retrieveIndividualEntityId(
							requestor,
							getCommManager().getIdManager()
									.getThisNetworkNode()).get();
			// create a context attribute under the CSS owner context entity

			Future<List<CtxIdentifier>> ctxIdentLookupFut = this.getCtxBroker()
					.lookup(requestor, ownerEntityId, CtxModelType.ATTRIBUTE,
							ctxAttribName);
			// Thread.sleep(1000);
			List<CtxIdentifier> ctxIdentLookup = ctxIdentLookupFut.get();
			CtxIdentifier ctxIdent = null;
			CtxAttribute ctxAttr = null;

			if ((ctxIdentLookup != null) && (ctxIdentLookup.size() > 0))
				ctxIdent = ctxIdentLookup.get(0);

			if (ctxIdent == null) {
				ctxAttr = this
						.getCtxBroker()
						.createAttribute(requestor, ownerEntityId,
								ctxAttribName).get();
			} else {
				Future<CtxModelObject> netUserAttrFut = this.getCtxBroker()
						.retrieve(requestor, ctxIdent);
				// Thread.sleep(1000);
				ctxAttr = (CtxAttribute) netUserAttrFut.get();
			}
			// assign a String value to the attribute
			ctxAttr.setStringValue(value);
			ctxAttr.setValueType(CtxAttributeValueType.STRING);

			// update the attribute in the Context DB
			ctxAttr = (CtxAttribute) this.getCtxBroker()
					.update(requestor, ctxAttr).get();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CtxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getContextAtribute(String ctxAttribName) {
		log.debug("getContextAtribute Start");
		CtxAttribute ctxAttr = null;

		try {
			// retrieve the CtxEntityIdentifier of the CSS owner context entity
			CtxEntityIdentifier ownerEntityId = this
					.getCtxBroker()
					.retrieveIndividualEntityId(
							requestor,
							getCommManager().getIdManager()
									.getThisNetworkNode()).get();
			// create a context attribute under the CSS owner context entity

			Future<List<CtxIdentifier>> ctxIdentLookupFut = this.getCtxBroker()
					.lookup(requestor, ownerEntityId, CtxModelType.ATTRIBUTE,
							ctxAttribName);
			// Thread.sleep(1000);
			List<CtxIdentifier> ctxIdentLookup = ctxIdentLookupFut.get();
			CtxIdentifier ctxIdent = null;

			if ((ctxIdentLookup != null) && (ctxIdentLookup.size() > 0))
				ctxIdent = ctxIdentLookup.get(0);

			if (ctxIdent == null) {
				ctxAttr = this
						.getCtxBroker()
						.createAttribute(requestor, ownerEntityId,
								ctxAttribName).get();
			} else {
				Future<CtxModelObject> ctxAttrFut = this.getCtxBroker()
						.retrieve(requestor, ctxIdent);
				// Thread.sleep(1000);
				ctxAttr = (CtxAttribute) ctxAttrFut.get();
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CtxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.debug("getContextAtribute End");

		return ctxAttr.getStringValue();

	}

	private void loadCisAdverts(List<String> cisIdToRetrieve) {

		cisDirCallbackResult = null;

		NZoneCisDirCallback cisDirCb = new NZoneCisDirCallback();
		
		getCisDirectoryClient().searchByIDS(cisIdToRetrieve,
				cisDirCb.iCisDirectoryCallback);

		try {
			cisDirCb.cisDirCallbackSignal.await();
		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cisDirCallbackResult = 	cisDirCb.adList;
			
		if (cisDirCallbackResult != null) {
			for (int index = 0; index < cisDirCallbackResult.size(); index++) {
				// Todo : add check in here to see if should replace
				localCisAds.add(cisDirCallbackResult.get(index));
			}
		}
	}

	private CisAdvertisementRecord getCisAdvert(String cisIdToRetrieve) {

		// Check if we have a copy locally
		for (int i = 0; i < localCisAds.size(); i++) {
			if (localCisAds.get(i).getId().compareTo(cisIdToRetrieve) == 0)
				return localCisAds.get(i);
		}
		int newStartPos = localCisAds.size();

		List<String> lIdList = new ArrayList<String>();
		lIdList.add(cisIdToRetrieve);
		loadCisAdverts(lIdList);

		// Check if we have a copy locally, don't bother checking ones we have
		// checked already
		for (; newStartPos < localCisAds.size(); newStartPos++) {
			if (localCisAds.get(newStartPos).getId().compareTo(cisIdToRetrieve) == 0)
				return localCisAds.get(newStartPos);
		}

		return null;

	}

	@Override
	public List<ZoneDetails> getZoneDetails() {
		return getNzoneClientComms().getZoneDetails();

		/*
		 * @Override public List<ZoneDetails> getZoneDetails(){
		 * 
		 * return getNzoneClientComms().getZoneDetails(); };
		 */

	}

	@Override
	public ServiceResourceIdentifier getServiceIdentifier() {
		return myServiceID;
	}

	@Override
	public String getServiceType() {
		return myServiceType;
	}

	@Override
	public List<String> getServiceTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setIAction(IIdentity arg0, IAction arg1) {
		log.info("..............setIAction called ................");
		// TODO Auto-generated method stub
		return true;
	}

	private void processUserPreference(String parameterName, String value) {
		log.debug("processUserPreference: " + parameterName + " = " + value);

		if (myServiceID == null)
			myServiceID = getServiceMgmt().getMyServiceId(this.getClass());

		// create action object and send to uam
		IAction action = new Action(myServiceID, myServiceType, parameterName,
				value, true, false,true);
		if (getUam() != null) {
			log.info("processUserPreference calling uam start ");
			getUam().monitor(myIdentity, action);
			log.info("processUserPreference calling uam end");
		}
	}

	private String getUserPreference(String parameterName) {
		String result = "";
		try {
			if (myServiceID == null)
				myServiceID = getServiceMgmt().getMyServiceId(this.getClass());
			log.info("getUserPreference : calling getPersoMgr().getPreference start");
			Future<IAction> futureOutcome = getPersoMgr().getPreference(
					requestor, myIdentity, myServiceType, myServiceID,
					parameterName);
			IAction outcome = futureOutcome.get();
			log.info("getUserPreference : calling getPersoMgr().getPreference end");
			if (outcome != null) {
				log.info("Successfully retrieved preference outcome: "
						+ outcome.getvalue());
				result = outcome.getvalue();
			} else {
				log.info("No preference was found");
			}
		} catch (Exception e) {
			log.error("Error retrieving preference");
			e.printStackTrace();
		}
		log.debug("Preference request result = " + result);
		return result;
	}

	// Put in to test implicit decision making
	@Override
	public void recordActionShowProfile() {
		String parameterName = new String("nzuseraction");
		String value = new String("showprofile");

		if (myServiceID == null)
			myServiceID = getServiceMgmt().getMyServiceId(this.getClass());

		// create action object and send to uam
		IAction action = new Action(myServiceID, myServiceType, parameterName,
				value,true,false,true);

		log.info("recordActionShowProfile : Sending action to UAM: "
				+ action.toString());
		getUam().monitor(myIdentity, action);
		log.info("recordActionShowProfile : Sent action to UAM: "
				+ action.toString());
	}

	@Override
	public void recordActionEnterZone() {
		String parameterName = new String("nzuseraction");
		String value = new String("enterzone");

		if (myServiceID == null)
			myServiceID = getServiceMgmt().getMyServiceId(this.getClass());
		// create action object and send to uam
		IAction action = new Action(myServiceID, myServiceType, parameterName,
				value,true,false,true);
		log.info("recordActionEnterZone : Sending action to UAM: "
				+ action.toString());
		getUam().monitor(myIdentity, action);
		log.info("recordActionEnterZone : Sent action to UAM: "
				+ action.toString());
	}

	@Override
	public UserDetails getMyProfile() {
		// check if we have a profile saved, if not create a new profile with
		// info from css record in context

		UserDetails myDets = getNzoneClientComms().getMyDetails();

		if (myDets == null)
			myDets = new UserDetails();

		if (myDets.getDisplayName() == null
				|| (myDets.getDisplayName().length() == 0)) {
			// profile not populated from context yet
			myDets.setDisplayName(getContextAtribute(CtxAttributeTypes.NAME
					.toString()));
			myDets.setCompany(getContextAtribute(CtxAttributeTypes.WORK_POSITION
					.toString()));
			myDets.setPosition(getContextAtribute(CtxAttributeTypes.OCCUPATION
					.toString()));
			myDets.setEmail(getContextAtribute(CtxAttributeTypes.EMAIL
					.toString()));

			getNzoneClientComms().updateMyDetails(myDets);
		}

		return myDets;

		// getContextAtribute(CtxAttributeTypes.NAME.toString());
		// getContextAtribute(CtxAttributeTypes.EMAIL.toString());

	};

	@Override
	public int getCurrentZone() {
		// check if we have a profile saved, if not create a new profile with
		// info from css record in context
		List<ZoneDetails> fullList = getNzoneClientComms().getZoneDetails();

		if (currentZoneCis != null && fullList != null) {
			for (int i = 0; i < fullList.size(); i++) {
				// Find our one
				if (fullList.get(i).getCisid()
						.contains(currentZoneCis.getCisId()))
					return (i);
			}
		}

		return 0;

	};

	@Override
	public UserDetails getUserProfile(String userID) {
		return getNzoneClientComms().getUserDetails(userID);
	}

	@Override
	public boolean isProfileSetup() {
		// check if we have a profile saved, if not create a new profile with
		// info from css record in context

		UserDetails userDets = getNzoneClientComms().getMyDetails();
		if ((userDets != null) && (userDets.getDisplayName() != null)) {
			if (userDets.getDisplayName().length() > 0)
				return true;
		}
		return false;
	};
	
	@Override
	public void getSnsData() {
		
		log.debug("getSnsData Start");
		CtxAttribute ctxAttr = null;
		
		if (requestorService == null)
		{
			if (myServiceID == null)
				myServiceID = getServiceMgmt().getMyServiceId(this.getClass());
			requestorService = new RequestorService(myIdentity, myServiceID);
		}
		
		try {
			
			
			Future<List<CtxEntityIdentifier>> ctxIdentLookupFut = this.getCtxBroker().lookupEntities(this.requestorService, getCommManager().getIdManager().getThisNetworkNode(),
					CtxEntityTypes.SOCIAL_NETWORK, CtxAttributeTypes.TYPE, "facebook", "facebook");
			
			
			
			// Thread.sleep(1000);
			List<CtxEntityIdentifier> ctxIdentLookup = ctxIdentLookupFut.get();
			CtxEntityIdentifier ctxIdent = null;
			if ((ctxIdentLookup != null) && (ctxIdentLookup.size() > 0))
				ctxIdent = ctxIdentLookup.get(0);

			 // the retrieved identifier is used in order to retrieve the context model object (CtxEntity)
			 CtxEntity retrievedCtxEntity = (CtxEntity) this.getCtxBroker().
					 retrieve(requestorService, ctxIdent).get();


			
			 // Retrieve CtxAttributes assigned to retrievedCtxEntity 
			 Set<CtxAttribute> ctxAttrSet = retrievedCtxEntity.getAttributes(CtxAttributeTypes.ID);
			 
			 CtxAttribute ctxAttr2;
			 
			 if( ctxAttrSet.size()>0 ){
			         List<CtxAttribute>     ctxAttrList = new ArrayList<CtxAttribute>(ctxAttrSet);
			         ctxAttr2 = ctxAttrList.get(0);
			 }
			 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CtxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.debug("getSnsData End");

		return ;

		

	}

	@Override
	public List<PersonalisablePreferenceIdentifier> getPersonalisablePreferences() {
		log.info("..............setIAction called ................");
		// TODO Auto-generated method stub
		return null;
	};

}
