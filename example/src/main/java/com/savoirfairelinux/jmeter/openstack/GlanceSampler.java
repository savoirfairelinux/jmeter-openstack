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
import org.openstack4j.api.Builders;
import org.openstack4j.model.common.Payload;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.image.ContainerFormat;
import org.openstack4j.model.image.DiskFormat;
import org.openstack4j.model.image.Image;

import com.savoirfairelinux.jmeter.openstack.lib.AbstractOpenstackSampler;
import com.savoirfairelinux.jmeter.openstack.lib.JMeterParameter;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackException;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackTearDownUtil;
import com.savoirfairelinux.jmeter.openstack.lib.OpenstackUtil;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
@SuppressWarnings("serial")
public class GlanceSampler extends AbstractOpenstackSampler {
	@Override
	protected void run(JavaSamplerContext context) throws Exception {
		String uploadImage = context.getParameter(ExampleParameter.GLANCE_UPLOAD_IMAGE.name());
		int readTimeout = context.getIntParameter(ExampleParameter.GLANCE_READ_TIMEOUT.name());

		String imageName = OpenstackUtil.generateName();

		log.debug("uploading image " + uploadImage);
		Payload<File> payload = Payloads.create(new File(uploadImage));
		Image image = os.images().create(
				Builders.image().name(imageName).isPublic(true).containerFormat(ContainerFormat.BARE)
						.diskFormat(DiskFormat.QCOW2).build(), payload);

		int waited = 0;
		String imageId = image.getId();
		image = null;
		while (waited < readTimeout) {
			image = os.images().get(imageId);
			if (image != null)
				break;
			log.debug("waiting for image " + uploadImage);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
		}

		log.debug("deleting image " + imageName);
		logIfError(os.images().delete(imageId));

		if (image == null)
			throw new OpenstackException("image " + uploadImage + " read failed");
	}

	@Override
	protected void teardown(JavaSamplerContext context) throws Exception {
		OpenstackTearDownUtil.deleteGlanceImages(os);
	}

	@Override
	protected JMeterParameter[] getExtraParameters() {
		return new JMeterParameter[] { ExampleParameter.GLANCE_UPLOAD_IMAGE, ExampleParameter.GLANCE_READ_TIMEOUT };
	}
}
