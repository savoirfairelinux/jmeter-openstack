# jmeter-openstack

*jmeter-openstack* allows you to easily develop performance tests for an
*OpenStack* deployment. These tests can be run as *JUnit* test (in Eclipse or
with Maven) or used directly into *JMeter* by using the Java Request sampler.
This project focus is to make the tests implementation as easy as possible.

The project is splitted in 3 main parts:

 * *core*: contains the base sampler class that handles the common JMeter setup
   operations like Keystone authentication.
 * *test-core*: allows the running of JMeter tests as JUnit tests (no JMeter
   installation is needed).
 * *example*: is an example implementation of a few JMeter samplers. When the
   tests are more mature, this project may become the reference implementation.

## Usage

You can test the library in action with the example project by using one of the
three following methods.

### Eclipse

You just need to adjust the properties in **arguments.properties** for you
*OpenStack* installation. You can also adjust JMeter tests configuration in
**configuration.properties** (iterations, threads, ...).

Once the configuration is done, you can simply run The JUnit classes.

### Maven

As for Eclipse, you need to adjust the properties in **arguments.properties**
and **configuration.properties**.

Maven tests are disabled by default.

If you want to run the tests:

```sh
mvn test -DskipTests=false
```

Or if you want to only run *Swift* test:

```sh
mvn test -DskipTests=false -DfailIfNoTests=false -Dtest=SwiftSamplerTest
```

### JMeter

First you need to generate the example library for JMeter:

```sh
mvn clean package
```

Then copy the generated jar (*dist/jmeter-openstack-example-[VERSION].jar*) to
the JMeter *lib/ext* folder. The new sampler will be available in the
*Java Request* component configuration.

## Develop new samplers

* Use the *Maven* archetype:

```sh
mvn archetype:generate \
	-DarchetypeGroupId=com.savoirfairelinux.jmeter.openstack \
	-DarchetypeArtifactId=jmeter-openstack-archetype \
	-DarchetypeVersion=1.0-SNAPSHOT \
	-DarchetypeRepository=https://nexus.savoirfairelinux.com/content/repositories/snapshots
```

* Check if everything is building

```sh
mvn clean package
```

You should be able to run your test from *Eclipse* or *Maven*. You can also
copy the generated jar in *lib/ext* folder of your JMeter installation and your
new sampler will be available in the *Java Request* component configuration.


## License

```text
Copyright (C) 2015 Savoir-Faire Linux Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

