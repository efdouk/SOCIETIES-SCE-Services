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
package org.societies.thirdPartyServices.disasterManagement.wantToHelp.ctxDataInitiator;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.comm.xmpp.interfaces.ICommManager;
import org.societies.api.context.CtxException;
import org.societies.api.context.broker.ICtxBroker;
import org.societies.api.context.model.CtxAttribute;
import org.societies.api.context.model.CtxAttributeTypes;
import org.societies.api.context.model.CtxEntityIdentifier;
import org.societies.api.context.model.CtxIdentifier;
import org.societies.api.context.model.CtxModelType;
import org.societies.api.identity.IIdentity;
import org.societies.api.identity.IIdentityManager;
import org.societies.api.identity.INetworkNode;
import org.societies.api.identity.InvalidFormatException;
import org.societies.api.identity.Requestor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * This class is used to add initial context data.
 * 
 * @author <a href="mailto:nikosk@cn.ntua.gr">Nikos Kalatzis</a> (ICCS)
 * @since 0.4
 */
@Service
@Lazy(false)
public class CtxDataInitiator {

	/** The logging facility. */
	private static final Logger LOG = LoggerFactory
			.getLogger(CtxDataInitiator.class);

	/** The internal BaseUser Broker service. */
	private ICtxBroker ctxBroker;

	/** The Comm Mgr service. */
	private ICommManager commMgr;

	/** The Identity Mgr service. */
	private IIdentityManager idMgr;

	private INetworkNode cssNodeId;
	private IIdentity cssOwnerId;

	private Requestor requestor;
	private static CtxEntityIdentifier ownerCtxId;

	@Autowired(required = true)
	public CtxDataInitiator(ICtxBroker ctxBroker, ICommManager commMgr,
			Requestor requestor) {

		if (LOG.isInfoEnabled())
			LOG.info(this.getClass() + " instantiated");

		this.commMgr = commMgr;
		this.ctxBroker = ctxBroker;
		this.requestor = requestor;

		this.idMgr = commMgr.getIdManager();

		this.cssOwnerId = this.getLocalIdentity();
		// cssOwnerId =
		// commMgr.getIdManager().fromJid(commMgr.getIdManager().getThisNetworkNode().getBareJid());

		System.out
				.println("************ CtxDataInitiator: ownerCtxId.getOwnerId()="
						+ ownerCtxId.getOwnerId());

		try {

			ownerCtxId = ctxBroker.retrieveIndividualEntityId(requestor,
					cssOwnerId).get();

			if (ownerCtxId.getOwnerId().equals("john.societies.local")) {
				BaseUser john = new John();
				addContext(john);
			} else if (ownerCtxId.getOwnerId().equals("jane.societies.local")) {
				BaseUser jane = new Jane();
				addContext(jane);

			} else if (ownerCtxId.getOwnerId().equals("user1.societies.local")) {
				BaseUser user1 = new User1();
				addContext(user1);

			} else if (ownerCtxId.getOwnerId().equals("user2.societies.local")) {
				BaseUser user2 = new User2();
				addContext(user2);

			} else if (ownerCtxId.getOwnerId().equals("user3.societies.local")) {
				BaseUser user3 = new User3();
				addContext(user3);

			} else if (ownerCtxId.getOwnerId().equals("user4.societies.local")) {
				BaseUser user4 = new User4();
				addContext(user4);

			} else if (ownerCtxId.getOwnerId().equals("user5.societies.local")) {
				BaseUser user5 = new User5();
				addContext(user5);
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
	}

	private void addContext(BaseUser user) {

		if (LOG.isInfoEnabled()) // TODO debug
			LOG.info("Updating initial context values");

		String value;

		try {

			// AGE
			value = user.getAge();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId, CtxAttributeTypes.AGE,
						value);

			// BIRTHDAY
			value = user.getBirthday();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId, CtxAttributeTypes.BIRTHDAY,
						value);

			// EMAIL
			value = user.getEmail();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId, CtxAttributeTypes.EMAIL,
						value);

			// FRIENDS
			value = user.getFriends();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId, CtxAttributeTypes.FRIENDS,
						value);

			// INTERESTS
			value = user.getInterests();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId,
						CtxAttributeTypes.INTERESTS, value);

			// LANGUAGES
			value = user.getLanguages();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId,
						CtxAttributeTypes.LANGUAGES, value);

			// LOCATION_COORDINATES
			value = user.getLocationCoordinates();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId,
						CtxAttributeTypes.LOCATION_COORDINATES, value);

			// LOCATION_SYMBOLIC
			value = user.getLocationSymbolic();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId,
						CtxAttributeTypes.LOCATION_SYMBOLIC, value);

			// MOVIES
			value = user.getMovies();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId, CtxAttributeTypes.MOVIES,
						value);

			// NAME
			value = user.getName();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId, CtxAttributeTypes.NAME,
						value);

			// OCCUPATION
			value = user.getOccupation();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId,
						CtxAttributeTypes.OCCUPATION, value);

			// POLITICAL_VIEWS
			value = user.getPoliticalViews();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId,
						CtxAttributeTypes.POLITICAL_VIEWS, value);

			// SEX
			value = user.getSex();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId, CtxAttributeTypes.SEX,
						value);

			// STATUS
			value = user.getStatus();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId, CtxAttributeTypes.STATUS,
						value);

			// SKILLS
			value = user.getSkills();
			if (value != null && !value.isEmpty())
				this.updateCtxAttribute(ownerCtxId, CtxAttributeTypes.SKILLS,
						value);

		} catch (Exception e) {
			LOG.info("error when initializing context data: "
					+ e.getLocalizedMessage());
		}

	}

	private void updateCtxAttribute(CtxEntityIdentifier ownerCtxId,
			String type, String value) throws Exception {

		if (LOG.isInfoEnabled()) // TODO debug
			LOG.info("Updating '" + type + "' of entity " + ownerCtxId
					+ " to '" + value + "'");

		final List<CtxIdentifier> ctxIds = this.ctxBroker.lookup(requestor,
				ownerCtxId, CtxModelType.ATTRIBUTE, type).get();
		if (!ctxIds.isEmpty()) {
			CtxAttribute attr = (CtxAttribute) ctxBroker.retrieve(requestor,
					ctxIds.get(0)).get();
			attr.setStringValue(value);
			this.ctxBroker.update(requestor, attr);
		} else {
			final CtxAttribute attr = this.ctxBroker.createAttribute(requestor,
					ownerCtxId, type).get();
			attr.setStringValue(value);
			this.ctxBroker.update(requestor, attr);
		}
	}

	private IIdentity getLocalIdentity() {

		IIdentity cssOwnerId = null;
		INetworkNode cssNodeId = this.idMgr.getThisNetworkNode();
		try {
			cssOwnerId = this.idMgr.fromJid(cssNodeId.getBareJid());
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}

		return cssOwnerId;
	}
}