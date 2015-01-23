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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.model.storage.block.Volume.Status;
import org.openstack4j.model.storage.object.SwiftContainer;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.model.storage.object.options.ContainerListOptions;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
public class OpenstackTearDownUtil {
	private final static Logger log = LoggingManager.getLoggerForClass();

	private static void logIfError(ActionResponse response) {
		if (!response.isSuccess())
			log.error(response.getFault());
	}

	public static void deleteCinderVolumes(OSClient os) {
		for (Volume volume : os.blockStorage().volumes().list())
			if (volume.getName().startsWith(OpenstackUtil.NAME_PREFIX) && !volume.getStatus().equals(Status.DELETING))
				logIfError(os.blockStorage().volumes().delete(volume.getId()));
	}

	public static void deleteGlanceImages(OSClient os) {
		Map<String, String> filter = new HashMap<String, String>();

		filter.put("name", "^" + OpenstackUtil.NAME_PREFIX + "*");
		for (Image images : os.images().list(filter))
			logIfError(os.images().delete(images.getId()));
	}

	public static void deleteNeutronSubnets(OSClient os) {
		for (Subnet subnet : os.networking().subnet().list()) {
			if (subnet.getName().startsWith(OpenstackUtil.NAME_PREFIX))
				logIfError(os.networking().subnet().delete(subnet.getId()));
		}
	}

	public static void deleteNeutronNetworks(OSClient os) {
		for (Network network : os.networking().network().list()) {
			if (network.getName().startsWith(OpenstackUtil.NAME_PREFIX))
				logIfError(os.networking().network().delete(network.getId()));
		}
	}

	public static void deleteNovaServers(OSClient os) {
		Map<String, String> filter = new HashMap<String, String>();
		filter.put("name", "^" + OpenstackUtil.NAME_PREFIX + "*");
		for (Server server : os.compute().servers().list(filter))
			logIfError(os.compute().servers().delete(server.getId()));
	}

	public static void deleteSwiftContainers(OSClient os) {
		List<? extends SwiftContainer> containers = os.objectStorage().containers()
				.list(ContainerListOptions.create().startsWith(OpenstackUtil.NAME_PREFIX));

		for (SwiftContainer container : containers) {
			for (SwiftObject object : os.objectStorage().objects().list(container.getName()))
				logIfError(os.objectStorage().objects().delete(container.getName(), object.getName()));
			logIfError(os.objectStorage().containers().delete(container.getName()));
		}
	}

	public static void deleteNovaServersAndWait(OSClient os, int timeout) {
		deleteNovaServers(os);

		Map<String, String> filter = new HashMap<String, String>();
		filter.put("name", "^" + OpenstackUtil.NAME_PREFIX + "*");

		int waited = 0;
		while (waited < timeout) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				break;
			}
			waited += 500;

			if (os.compute().servers().list(filter).isEmpty())
				break;
		}
	}
}
