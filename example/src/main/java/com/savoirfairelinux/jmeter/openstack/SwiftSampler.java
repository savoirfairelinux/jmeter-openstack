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

import java.io.File;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.storage.object.SwiftObject;

import com.savoirfairelinux.jmeter.openstack.lib.AbstractOpenstackSampler;
import com.savoirfairelinux.jmeter.openstack.lib.JMeterParameter;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackException;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackTearDownUtil;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackUtil;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
@SuppressWarnings("serial")
public class SwiftSampler extends AbstractOpenstackSampler {
	@Override
	protected void run(JavaSamplerContext context) throws Exception {
		String uploadFile = context.getParameter(ExampleParameter.SWIFT_UPLOAD_FILE.name());
		int readTimeout = context.getIntParameter(ExampleParameter.SWIFT_READ_TIMEOUT.name());

		String containerName = OpenstackUtil.generateName();
		String objectName = OpenstackUtil.generateName();

		log.debug("creating container " + containerName);
		exceptionIfError(os.objectStorage().containers().create(containerName));

		log.debug("uploading file " + uploadFile);
		os.objectStorage().objects().put(containerName, objectName, Payloads.create(new File(uploadFile)));

		int waited = 0;
		SwiftObject object = null;
		while (waited < readTimeout) {
			object = os.objectStorage().objects().get(containerName, objectName);
			if (object != null)
				break;
			log.debug("waiting for object " + objectName);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
		}

		log.debug("deleting object " + objectName);
		logIfError(os.objectStorage().objects().delete(containerName, objectName));

		log.debug("deleting container " + containerName);
		logIfError(os.objectStorage().containers().delete(containerName));

		if (object == null)
			throw new OpenstackException("object " + uploadFile + " read failed");
	}

	@Override
	public void teardown(JavaSamplerContext context) throws Exception {
		OpenstackTearDownUtil.deleteSwiftContainers(os);
	}

	@Override
	protected JMeterParameter[] getExtraParameters() {
		return new JMeterParameter[] { ExampleParameter.SWIFT_UPLOAD_FILE, ExampleParameter.SWIFT_READ_TIMEOUT };
	}
}
