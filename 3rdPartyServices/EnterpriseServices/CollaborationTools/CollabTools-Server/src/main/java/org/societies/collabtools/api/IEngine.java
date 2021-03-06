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
package org.societies.collabtools.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.societies.collabtools.acquisition.LongTermCtxTypes;
import org.societies.collabtools.acquisition.Person;
import org.societies.collabtools.acquisition.ShortTermCtxTypes;
import org.societies.collabtools.runtime.Operators;
import org.societies.collabtools.runtime.Rule;

/**
 * Interface for Rule engine
 *
 * @author Chris Lima
 *
 */
public interface IEngine {
	/**
	 * Insert a individual rule
	 **/
	public void insertRule(Rule rule);
	
	/**
	 * Delete a individual rule
	 **/
	public void deleteRule(Rule rule);
	
	/** 
	 * Handles the initialization 
	 * 
	 * @param rules The rules which define the system.
	 * 
	 * */
	public void setRules(final Collection<Rule> rules);

	/**
	 * @return a List of rules
	 */
	public abstract List<Rule> getRules();

	public abstract Hashtable<String, HashSet<Person>> getMatchingResultsByPriority();

	/**
	 * @param operator Filter operators available in {@link Operators}
	 * @param ctx Context information 
	 * @param value Value if wants to compare. Null for SAME or DIFFERENT operators
	 * @param ctxType Context type. Can be {@link ShortTermCtxTypes} or {@link LongTermCtxTypes}
	 * @return hashtable of persons
	 */
	public abstract Hashtable<String, HashSet<Person>> evaluateRule(Operators operator, final String ctxAttribute, String value, final String ctxType, HashSet<Person> primarySet);

	/**
	 * @param ruleName Name of the rule to evaluate
	 * @return hashtable of persons
	 */
	Hashtable<String, HashSet<Person>> evaluateRule(String ruleName);

}