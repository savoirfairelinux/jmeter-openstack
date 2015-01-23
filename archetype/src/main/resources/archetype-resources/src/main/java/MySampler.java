#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/**
 * Copyright (C) 2015 Savoir-Faire Linux Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ${package};

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.openstack4j.model.compute.Server;

import com.savoirfairelinux.jmeter.openstack.lib.AbstractOpenstackSampler;
import com.savoirfairelinux.jmeter.openstack.lib.JMeterParameter;

/*
 * OpenStack4j API: http://www.openstack4j.com/learn/
 */
@SuppressWarnings("serial")
public class MySampler extends AbstractOpenstackSampler {
	@Override
	protected void run(JavaSamplerContext context) throws Exception {
		String myParameter = context.getParameter(MyParameter.MY_PARAMETER.name());
		log.debug(myParameter);
		log.info("instances:");
		for (Server instance : os.compute().servers().list())
			log.info(instance.getName());
	}

	@Override
	protected void setup(JavaSamplerContext context) throws Exception {
	}

	@Override
	protected void teardown(JavaSamplerContext context) throws Exception {
	}

	@Override
	protected JMeterParameter[] getExtraParameters() {
		return new JMeterParameter[] { MyParameter.MY_PARAMETER };
	}
}
