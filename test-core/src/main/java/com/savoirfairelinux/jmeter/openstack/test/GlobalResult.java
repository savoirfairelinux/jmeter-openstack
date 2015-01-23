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

package com.savoirfairelinux.jmeter.openstack.test;

import java.io.Serializable;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.AbstractTestElement;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
@SuppressWarnings("serial")
public class GlobalResult extends AbstractTestElement implements Serializable, SampleListener {
	private static boolean failed;

	public GlobalResult() {
		failed = false;
	}

	@Override
	public void sampleOccurred(SampleEvent sampleEvent) {
		if (!sampleEvent.getResult().isSuccessful())
			failed = true;
	}

	@Override
	public void sampleStarted(SampleEvent sampleEvent) {
	}

	@Override
	public void sampleStopped(SampleEvent sampleEvent) {
	}

	public boolean isFailed() {
		return failed;
	}
}
