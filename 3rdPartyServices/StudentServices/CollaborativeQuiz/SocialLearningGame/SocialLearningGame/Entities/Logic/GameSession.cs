﻿/* 
 * Copyright (coffee) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
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

using System.Collections.Generic;
using SocialLearningGame.Entities;

namespace SocialLearningGame.Logic
{
    public class GameSession
    {

        // Global vars
        public List<UserScore> AllKnownUsers { get; private set; }
        public List<Category> AllCategories { get; private set; }
        public List<Question> AllQuestions { get; private set; }
        public List<Question> AllAnsweredQuestions { get; private set; }
        public Dictionary<Category, List<Question>> CategoryQuestionMap { get; private set; }

        // Game specific vars
        public System.Object MainUser { get; set; }
        public List<UserScore> AdditionalUsers { get; private set; }
        public Dictionary<UserScore, int> UserScoreMap { get; private set; }
     //   public List<UserAnsweredQ> answeredQ { get; private set; }
        //TODO: public static List<Challenge> challenges = new List<Challenge>();
        //TODO: public static List<Challenge> challengesFrom = new List<Challenge>();
        public GameStage Stage { get; set; }
        public Queue<QuestionRound> QuestionHistory { get; private set; }

        public QuestionRound CurrentRound { get; set; }
        public int questionCount { private set;  get; }

        public GameSession()
        {
            AdditionalUsers = new List<UserScore>();
            AllAnsweredQuestions = new List<Question>();
            UserScoreMap = new Dictionary<UserScore, int>();
            AllKnownUsers = new List<UserScore>();
            AllCategories = new List<Category>();
            AllQuestions = new List<Question>();
            CategoryQuestionMap = new Dictionary<Category, List<Question>>();
            QuestionHistory = new Queue<QuestionRound>();
            questionCount = 0;
        }

    }
}
