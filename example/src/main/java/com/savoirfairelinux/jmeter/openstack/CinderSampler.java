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
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.model.storage.block.Volume.Status;

import com.savoirfairelinux.jmeter.openstack.lib.AbstractOpenstackSampler;
import com.savoirfairelinux.jmeter.openstack.lib.JMeterParameter;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackException;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackTearDownUtil;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackUtil;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
@SuppressWarnings("serial")
public class CinderSampler extends AbstractOpenstackSampler {
	@Override
	protected void run(JavaSamplerContext context) throws Exception {
		int size = context.getIntParameter(ExampleParameter.CINDER_VOLUME_SIZE.name());
		int timeout = context.getIntParameter(ExampleParameter.CINDER_CREATE_TIMEOUT.name());

		String name = OpenstackUtil.generateName();

		if (log.isDebugEnabled())
			log.debug("creating volume " + name);
		Volume volume = os.blockStorage().volumes().create(Builders.volume().name(name).size(size).build());

		// wait for the creation, there is no creation blocking call
		int waited = 0;
		while (waited < timeout) {
			volume = os.blockStorage().volumes().get(volume.getId());
			if (!volume.getStatus().equals(Status.CREATING))
				break;
			try {
				Thread.sleep(500);
				waited += 500;
			} catch (InterruptedException e) {
				break;
			}
		}

		Status status = volume.getStatus();

		log.debug("deleting volume " + name);
		logIfError(os.blockStorage().volumes().delete(volume.getId()));

		if (!status.equals(Status.AVAILABLE))
			throw new OpenstackException("volume " + name + " creation failed (" + status + ")");
	}

	@Override
	protected void teardown(JavaSamplerContext context) throws Exception {
		OpenstackTearDownUtil.deleteCinderVolumes(os);
	}

	@Override
	protected JMeterParameter[] getExtraParameters() {
		return new JMeterParameter[] { ExampleParameter.CINDER_VOLUME_SIZE, ExampleParameter.CINDER_CREATE_TIMEOUT };
	}
}