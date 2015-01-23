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

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.openstack4j.api.Builders;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Subnet;

import com.savoirfairelinux.jmeter.openstack.lib.AbstractOpenstackSampler;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackTearDownUtil;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackUtil;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
@SuppressWarnings("serial")
public class NeutronSampler extends AbstractOpenstackSampler {
	@Override
	protected void run(JavaSamplerContext context) throws Exception {
		String networkName = OpenstackUtil.generateName();
		String subnetName = OpenstackUtil.generateName();

		log.debug("creating network " + networkName);
		Network network = os.networking().network()
				.create(Builders.network().name(networkName).tenantId(tenant.getId()).build());

		log.debug("creating subnet " + subnetName);
		Subnet subnet = os
				.networking()
				.subnet()
				.create(Builders.subnet().name(subnetName).networkId(network.getId()).tenantId(tenant.getId())
						.ipVersion(IPVersionType.V4).cidr("192.168.0.0/24").build());

		subnet = os.networking().subnet().get(subnet.getId());

		log.debug("deleting subnet " + subnetName);
		logIfError(os.networking().subnet().delete(subnet.getId()));

		log.debug("deleting network " + networkName);
		logIfError(os.networking().network().delete(network.getId()));
	}

	@Override
	protected void teardown(JavaSamplerContext context) throws Exception {
		OpenstackTearDownUtil.deleteNeutronSubnets(os);
		OpenstackTearDownUtil.deleteNeutronNetworks(os);
	}
}