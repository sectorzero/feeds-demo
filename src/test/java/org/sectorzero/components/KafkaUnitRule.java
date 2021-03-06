/*
 * Copyright (C) 2014 Christopher Batey
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
package org.sectorzero.components;

import org.junit.rules.ExternalResource;

public class KafkaUnitRule extends ExternalResource {

    private final int zkPort;
    private final int kafkaPort;
    private final KafkaUnit kafkaUnit;

    public KafkaUnitRule(int zkPort, int kafkaPort) {
        this.zkPort = zkPort;
        this.kafkaPort = kafkaPort;
        this.kafkaUnit = new KafkaUnit(zkPort, kafkaPort);
    }

    @Override
    protected void before() throws Throwable {
        kafkaUnit.startup();
    }

    @Override
    protected void after() {
        kafkaUnit.shutdown();
    }

    public int getZkPort() {
        return zkPort;
    }

    public int getKafkaPort() {
        return kafkaPort;
    }

    public KafkaUnit getKafkaUnit() {
        return kafkaUnit;
    }
}
