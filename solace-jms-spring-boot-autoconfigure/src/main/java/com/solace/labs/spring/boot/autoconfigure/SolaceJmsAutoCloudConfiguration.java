/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.solace.labs.spring.boot.autoconfigure;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.solace.labs.spring.cloud.core.SolaceMessagingInfo;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolConnectionFactoryImpl;
import com.solacesystems.jms.property.JMSProperties;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@ConditionalOnClass({ ConnectionFactory.class, SolConnectionFactory.class })
@ConditionalOnMissingBean(ConnectionFactory.class)
@Conditional(CloudCondition.class)
public class SolaceJmsAutoCloudConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SolaceJmsAutoCloudConfiguration.class);


    @Bean
    public SolConnectionFactoryImpl connectionFactory() {
        try {
            JMSProperties props;
            props = new JMSProperties((Hashtable<?, ?>) null);
            props.initialize();
            SolConnectionFactoryImpl cf = new SolConnectionFactoryImpl(props);
            CloudFactory cloudFactory = new CloudFactory();
			Cloud cloud = cloudFactory.getCloud();
			SolaceMessagingInfo solacemessaging = (SolaceMessagingInfo) cloud
					.getServiceInfo("solace-messaging-demo-instance");
			System.out.println(solacemessaging);
            cf.setHost(solacemessaging.getSmfHost());
            cf.setUsername(solacemessaging.getClientUsername());
            cf.setPassword(solacemessaging.getClientPassword());
            cf.setVPN(solacemessaging.getMsgVpnName());
            cf.setDirectTransport(false);

            return cf;
        } catch (Exception ex) {

            logger.error("Exception found during Solace Connection Factory creation.", ex);

            throw new IllegalStateException("Unable to create Solace "
                    + "connection factory, ensure that the sol-jms-<version>.jar " + "is the classpath", ex);
        }
    }

}