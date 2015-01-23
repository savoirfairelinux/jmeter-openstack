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

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.api.types.Facing;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.identity.Access;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.openstack.OSFactory;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
@SuppressWarnings("serial")
public abstract class AbstractOpenstackSampler extends AbstractJavaSamplerClient implements Serializable {
	protected final Logger log = LoggingManager.getLoggerFor(getClass().getName());

	protected static volatile OSClient os;
	protected static volatile Tenant tenant;

	private static volatile boolean setupTestDone = false;
	private static volatile boolean setupTestSuccess = false;
	private static volatile boolean teardownDone = false;

	protected void setup(JavaSamplerContext context) throws Exception {
	}

	protected abstract void run(JavaSamplerContext context) throws Exception;

	protected void teardown(JavaSamplerContext context) throws Exception {
	}

	protected JMeterParameter[] getExtraParameters() {
		return null;
	}

	protected final void logIfError(ActionResponse response) {
		if (!response.isSuccess())
			log.error(response.getFault());
	}

	protected final void exceptionIfError(ActionResponse response) throws OpenstackException {
		if (!response.isSuccess())
			throw new OpenstackException(response.getFault());
	}

	private void addArgument(Arguments arguments, JMeterParameter argument) {
		arguments.addArgument(argument.name(), argument.getDefaultValue());
	}

	@Override
	public final void setupTest(JavaSamplerContext context) {
		// we execute the setup only one time for everybody
		synchronized (getClass()) {
			if (setupTestDone && os != null) {
				// setup the session for this thread
				Access access = os.getAccess();
				os = OSFactory.clientFromAccess(access);
				return;
			}
			setupTestDone = true;

			String username = context.getParameter(CoreParameter.USERNAME.name());
			String password = context.getParameter(CoreParameter.PASSWORD.name());
			String tenantName = context.getParameter(CoreParameter.TENANT_NAME.name());
			String authURL = context.getParameter(CoreParameter.AUTH_URL.name());

			log.debug("authenticating");
			try {
				os = OSFactory.builder().endpoint(authURL).credentials(username, password).tenantName(tenantName)
						.perspective(Facing.PUBLIC).authenticate();

				tenant = OpenstackUtil.getTenantByName(os, tenantName);

				setup(context);

				setupTestSuccess = true;
			} catch (AuthenticationException e) {
				log.error("authentication failed");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public final SampleResult runTest(JavaSamplerContext context) {
		SampleResult result = new SampleResult();

		// we cannot throw an error in setupTest so we stop here
		if (!setupTestSuccess || os == null) {
			log.error("test setup failed");
			result.setStopTest(true);
			return result;
		}

		try {
			result.sampleStart();
			run(context);
			result.setSuccessful(true);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		result.sampleEnd();
		return result;
	}

	@Override
	public final void teardownTest(JavaSamplerContext context) {
		// we only need one teardown invocation
		if (teardownDone || os == null)
			return;
		teardownDone = true;

		log.debug("cleaning resources");

		try {
			// this method is executed from the JMeter main thread, this means we need to re-wire a token to instantiate
			// a lightweight client without re-authentication
			Access access = os.getAccess();
			os = OSFactory.clientFromAccess(access);

			// call the actual teardown
			teardown(context);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public final Arguments getDefaultParameters() {
		Arguments defaultParameters = new Arguments();

		addArgument(defaultParameters, CoreParameter.USERNAME);
		addArgument(defaultParameters, CoreParameter.PASSWORD);
		addArgument(defaultParameters, CoreParameter.TENANT_NAME);
		addArgument(defaultParameters, CoreParameter.AUTH_URL);

		JMeterParameter[] extraArguments = getExtraParameters();
		if (extraArguments != null)
			for (JMeterParameter extraArgument : extraArguments)
				addArgument(defaultParameters, extraArgument);

		return defaultParameters;
	}
}
