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

package com.savoirfairelinux.jmeter.openstack.lib;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Image;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.network.Network;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
public class OpenstackUtil {
	public static final String NAME_PREFIX = "jmeter-";

	public static Image getImageByName(OSClient os, String name) throws OpenstackException {
		List<? extends Image> images = os.compute().images().list();
		for (Image image : images) {
			if (image.getName().equals(name))
				return image;
		}
		throw new OpenstackException("image " + name + " not found");
	}

	public static Flavor getFlavorByName(OSClient os, String name) throws OpenstackException {
		List<? extends Flavor> flavors = os.compute().flavors().list();
		for (Flavor flavor : flavors) {
			if (flavor.getName().equals(name))
				return flavor;
		}
		throw new OpenstackException("flavour " + name + " not found");
	}

	public static List<String> getNetworksByName(OSClient os, String names) throws OpenstackException {
		List<? extends Network> networks = os.networking().network().list();

		List<String> networkIds = new ArrayList<String>();
		for (Network network : networks) {
			for (String networkName : names.split(",")) {
				if (network.getName().equals(networkName)) {
					networkIds.add(network.getId());
					break;
				}
			}
		}
		if (networkIds.isEmpty())
			throw new OpenstackException("no network found for " + names + " not found");

		return networkIds;
	}

	public static Tenant getTenantByName(OSClient os, String name) throws OpenstackException {
		// os.identity().tenants().getByName has a bug so we manually search
		for (Tenant tenant : os.identity().tenants().list()) {
			if (tenant.getName().equals(name))
				return tenant;
		}
		throw new OpenstackException("tenant " + name + " not found");
	}

	public static String generateName() {
		return NAME_PREFIX + RandomStringUtils.randomAlphanumeric(25);
	}
}
