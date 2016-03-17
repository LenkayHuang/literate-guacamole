/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.ofnb;

//import jdk.nashorn.internal.runtime.linker.Bootstrap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;

import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.DeviceStore;
import org.onosproject.net.link.LinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenflowClient implementation.
 * Entry of Openflow Northbound Bundle component.
 */
@Component(immediate = true)
public class OpenflowClientImpl {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceStore store;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    private final Logger log = LoggerFactory.getLogger(OpenflowClientImpl.class);
    private final OpenflowNorthboundClient clt = new OpenflowNorthboundClient(deviceService, store, linkService);

    @Activate
    protected void activate() {
        log.info("HLK ofNorth Started");
        clt.run();

    }

    @Deactivate
    protected void deactivate() {
        log.info("HLK ofNorth Stopped");
    }

}
