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

import com.savoirfairelinux.jmeter.openstack.lib.JMeterParameter;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
public enum ExampleParameter implements JMeterParameter {
	NOVA_FLAVOR("m1.tiny"), NOVA_IMAGE("cirros"), NOVA_BOOT_TIMEOUT("60000"), CINDER_VOLUME_SIZE("20"), CINDER_CREATE_TIMEOUT(
			"20000"), NOVA_NETWORKS("private"), SWIFT_READ_TIMEOUT("2000"), SWIFT_UPLOAD_FILE("/bin/bash"), GLANCE_UPLOAD_IMAGE(
			"/bin/bash"), GLANCE_READ_TIMEOUT("2000");

	private final String defaultValue;

	private ExampleParameter(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}
}
