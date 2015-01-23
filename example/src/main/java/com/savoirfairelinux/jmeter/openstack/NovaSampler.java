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

package com.savoirfairelinux.jmeter.openstack;

import java.util.List;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.openstack4j.api.Builders;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Image;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.Server.Status;
import org.openstack4j.model.compute.ServerCreate;

import com.savoirfairelinux.jmeter.openstack.lib.AbstractOpenstackSampler;
import com.savoirfairelinux.jmeter.openstack.lib.JMeterParameter;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackException;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackTearDownUtil;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackUtil;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
@SuppressWarnings("serial")
public class NovaSampler extends AbstractOpenstackSampler {
	@Override
	protected void run(JavaSamplerContext context) throws Exception {
		String flavorName = context.getParameter(ExampleParameter.NOVA_FLAVOR.name());
		String imageName = context.getParameter(ExampleParameter.NOVA_IMAGE.name());
		String networkNames = context.getParameter(ExampleParameter.NOVA_NETWORKS.name());
		int timeout = context.getIntParameter(ExampleParameter.NOVA_BOOT_TIMEOUT.name());

		Flavor flavor = OpenstackUtil.getFlavorByName(os, flavorName);
		Image image = OpenstackUtil.getImageByName(os, imageName);
		List<String> networks = OpenstackUtil.getNetworksByName(os, networkNames);

		String name = OpenstackUtil.generateName();

		ServerCreate sc = Builders.server().name(name).flavor(flavor).image(image).networks(networks).build();

		log.debug("booting server " + name);
		Server server = os.compute().servers().bootAndWaitActive(sc, timeout);

		Status status = server.getStatus();

		log.debug("deleting server " + name);
		logIfError(os.compute().servers().delete(server.getId()));

		if (!status.equals(Status.ACTIVE))
			throw new OpenstackException("server " + name + " creation failed (" + status + ")");
	}

	@Override
	protected void teardown(JavaSamplerContext context) throws Exception {
		OpenstackTearDownUtil.deleteNovaServers(os);
	}

	@Override
	protected JMeterParameter[] getExtraParameters() {
		return new JMeterParameter[] { ExampleParameter.NOVA_FLAVOR, ExampleParameter.NOVA_IMAGE,
				ExampleParameter.NOVA_NETWORKS, ExampleParameter.NOVA_BOOT_TIMEOUT };
	}
}