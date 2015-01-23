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

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.junit.BeforeClass;

/**
 * @author Julien Bonjean <julien.bonjean@savoirfairelinux.com>
 */
public class AbstractOpenstackSamplerTest {
	protected final Logger log = LoggingManager.getLoggerFor(getClass().getName());
	private static Properties configuration;
	private static Arguments arguments;

	protected void runJMeter(String className) throws Exception {
		int iterations = Integer.valueOf(getTestConfiguration(className, "iterations", "1"));
		int threads = Integer.valueOf(getTestConfiguration(className, "threads", "1"));

		log.info("starting test " + StringUtils.substringAfterLast(className, "."));

		// loop controller
		LoopController loopController = new LoopController();
		loopController.setLoops(iterations);
		loopController.setFirst(true);
		loopController.initialize();

		// thread group
		ThreadGroup threadGroup = new ThreadGroup();
		threadGroup.setNumThreads(threads);
		threadGroup.setRampUp(1);
		threadGroup.setSamplerController(loopController);

		// JavaSampler
		JavaSampler javaSampler = new JavaSampler();
		javaSampler.setClassname(className);
		javaSampler.setArguments(arguments);

		// summary
		Summariser summariser = new Summariser("summary");
		ResultCollector logger = new ResultCollector(summariser);

		// simple listener to track if at least one test failed
		GlobalResult globalResult = new GlobalResult();

		// create test plan
		TestPlan testPlan = new TestPlan();

		// assemble the test plan from previously initialized elements
		HashTree testPlanHashTree = new HashTree();
		testPlanHashTree.add(testPlan);
		HashTree threadGroupHashTree = new HashTree();
		threadGroupHashTree = testPlanHashTree.add(testPlan, threadGroup);
		HashTree javaSamplerHashTree = new HashTree();
		javaSamplerHashTree = threadGroupHashTree.add(javaSampler);
		javaSamplerHashTree.add(logger);
		javaSamplerHashTree.add(globalResult);

		// start JMeter test
		StandardJMeterEngine jmeter = new StandardJMeterEngine();
		jmeter.configure(testPlanHashTree);
		jmeter.run();

		// increase log level to display the final summary
		LoggingManager.setPriority(Priority.INFO, "jmeter.reporters.Summariser");
		logger.testEnded();
		LoggingManager.setPriority(Priority.WARN, "jmeter.reporters.Summariser");

		// set the JUnit test result
		assertTrue(!globalResult.isFailed());
	}

	private String getTestConfiguration(String className, String key, String defaultValue) {
		return configuration.getProperty("sampler." + StringUtils.substringAfterLast(className, ".") + "." + key,
				defaultValue);
	}

	@BeforeClass
	public static void prepare() throws Exception {
		// load configuration
		configuration = new Properties();
		InputStream inputStream = AbstractOpenstackSamplerTest.class.getClassLoader().getResourceAsStream(
				"configuration.properties");

		if (inputStream != null)
			configuration.load(inputStream);

		// load arguments
		arguments = new Arguments();
		Properties argumentsProps = new Properties();
		inputStream = AbstractOpenstackSamplerTest.class.getClassLoader().getResourceAsStream("arguments.properties");
		if (inputStream != null)
			argumentsProps.load(inputStream);
		for (Object key : argumentsProps.keySet())
			arguments.addArgument((String) key, argumentsProps.getProperty((String) key));

		// setup JMeter
		JMeterUtils.loadJMeterProperties("");

		// handle user defined global configuration
		for (Object key : configuration.keySet()) {
			String keyStr = (String) key;
			// set JVM properties
			if (keyStr.startsWith("jvm."))
				System.setProperty(keyStr.replaceFirst("^jvm\\.", ""), configuration.getProperty(keyStr));
			// set JMeter properties
			else if (keyStr.startsWith("jmeter."))
				JMeterUtils.setProperty(keyStr.replaceFirst("^jmeter\\.", ""), configuration.getProperty(keyStr));
		}

		// disable stdout before the logging is correctly setup
		CustomStdOut customStdOut = new CustomStdOut(System.out);
		customStdOut.setEnabled(false);
		System.setOut(customStdOut);

		// initialize logging and locale
		JMeterUtils.initLogging();
		JMeterUtils.initLocale();

		// workaround to disable Jersey warning
		java.util.logging.Logger.getLogger("").setLevel(Level.SEVERE);

		// re-enable stdout
		customStdOut.setEnabled(true);
	}

	private static class CustomStdOut extends PrintStream {
		private boolean enabled = true;

		public CustomStdOut(OutputStream out) {
			super(out);
		}

		@Override
		public void write(byte[] buf, int off, int len) {
			if (enabled)
				super.write(buf, off, len);
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}
