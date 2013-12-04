/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.cartridge.agent.event.subscriber;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.messaging.broker.publish.TopicPublisher;
import org.apache.stratos.messaging.broker.subscribe.TopicSubscriber;
import org.apache.stratos.messaging.event.instance.status.MemberStartedEvent;
import org.apache.stratos.messaging.util.Constants;

/**
 * Event publisher main class.
 */
public class Main {
    
	private static final Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) {
    	
    	log.info("Strating cartridge agent event subscriber");
    	
    	System.setProperty(CartridgeAgentConstants.JNDI_PROPERTIES_DIR, args[0]); 
    	System.setProperty(CartridgeAgentConstants.PARAM_FILE_PATH, args[1]);
    	    	
        //initialting the subscriber
        TopicSubscriber subscriber = new TopicSubscriber(Constants.ARTIFACT_SYNCHRONIZATION_TOPIC);
        subscriber.setMessageListener(new ArtifactListener());
        Thread tsubscriber = new Thread(subscriber);
		tsubscriber.start(); 		
		
		// 
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		log.info("Sending member started event");
		// Send member activated event
		MemberStartedEvent event = new MemberStartedEvent();
		event.setServiceName(LaunchParamsUtil.readParamValueFromPayload(CartridgeAgentConstants.SERVICE_NAME));
		event.setClusterId(LaunchParamsUtil.readParamValueFromPayload(CartridgeAgentConstants.CLUSTER_ID));
		event.setMemberId(LaunchParamsUtil.readParamValueFromPayload(CartridgeAgentConstants.MEMBER_ID));
		TopicPublisher publisher = new TopicPublisher(Constants.INSTANCE_STATUS_TOPIC);
		publisher.publish(event);
		log.info("Member started event is sent");		

		
		// Start periodical file checker task
		// TODO -- start this thread only if this node configured as a commit true node
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);       
        scheduler.scheduleWithFixedDelay(new RepositoryFileListener(), 0, 10, TimeUnit.SECONDS);
		
    }
    
}
