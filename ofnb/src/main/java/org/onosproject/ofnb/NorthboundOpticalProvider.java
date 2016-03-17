package org.onosproject.ofnb;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.MacAddress;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.*;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.ui.topo.BaseLinkMap;
import org.projectfloodlight.openflow.protocol.OFPortConfig;
import org.projectfloodlight.openflow.protocol.OFPortState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.onosproject.net.Link.Type.OPTICAL;


/**
 * Huawei OFNB optical elements provider.
 * To get links and nodes.
 */
@Component(immediate = true)
public class NorthboundOpticalProvider extends AbstractProvider implements LinkProvider {

    private static final Logger log = LoggerFactory.getLogger(NorthboundOpticalProvider.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkProviderRegistry registry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    private LinkProviderService providerService;
    private DeviceListener deviceListener = new InternalDeviceListener();
    private LinkListener linkListener = new InternalLinkListener();

    private OpenflowNorthboundChannelHandler handler;
    BaseLinkMap linkMap = new BaseLinkMap();

    protected OFNBPortDesc ofnbPortDesc;
    protected OFNBPortDescPropOpticalTransport ofnbPortDescPropOpticalTransport;
    protected List<OFNBPortDescPropOpticalTransportInterface> descList;
    protected Set<OFPortState> states;
    protected Set<OFPortConfig> configs;
    protected Port port;
    protected String deviceName;
    protected MacAddress address;

    public NorthboundOpticalProvider() {
        super(new ProviderId("NorthboundOptical", "org.onosproject.ofnb"));
    }


    protected void init(OpenflowNorthboundChannelHandler handler) {
        deviceService.addListener(deviceListener);
        linkService.addListener(linkListener);
        providerService = registry.register(this);
        this.handler = handler;

        log.info("OFNB provider Started");
    }


    protected void drop() {
        deviceService.removeListener(deviceListener);
        linkService.removeListener(linkListener);
        registry.unregister(this);
        log.info("OFNB provider Stopped");
    }

    //Listens to device events and processes their links.
    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            log.info("HLK OFNB:recieved device event!");
            DeviceEvent.Type type = event.type();
            Device device = event.subject();
            port = event.port();
            deviceName = event.subject().id().toString();
            //address = event.subject();
            if (type == DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED ||
                    type == DeviceEvent.Type.DEVICE_ADDED ||
                    type == DeviceEvent.Type.DEVICE_UPDATED) {
                processDeviceLinks(device);
            } else if (type == DeviceEvent.Type.PORT_UPDATED) {
                processPortLinks(device, event.port());
            }
        }
    }

    //Listens to link events and processes the link additions.
    private class InternalLinkListener implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            log.info("HLK OFNB:received link event!");
            if (event.type() == LinkEvent.Type.LINK_ADDED) {
                Link link = event.subject();
                if (link.providerId().scheme().equals("cfg")) {
                    processLink(event.subject());
                }
            }
        }
    }

    private void processDeviceLinks(Device device) {
        for (Link link : linkService.getDeviceLinks(device.id())) {
            if (link.isDurable() && link.type() == OPTICAL) {
                String remote_node;
                String local_node;
                if(link.src().deviceId()==device.id()) {
                    remote_node = link.dst().deviceId().uri().toString();
                    local_node = link.src().deviceId().uri().toString();
                }
                else{
                    remote_node = link.src().deviceId().uri().toString();
                    local_node = link.dst().deviceId().uri().toString();
                }

                String remote_port;
                if(link.src().deviceId()==device.id()) {
                    remote_port = link.dst().port().toString();
                }
                else {
                    remote_port = link.src().port().toString();
                }

                short signal_type = 5;
                short port_type = 1;
                short reserved = 0;
                short type = 1;

                OFNBPortDescPropOpticalTransportInterface tempdesc;
                tempdesc = ofnbPortDescPropOpticalTransport.createBuilder()
                .setType(type)
                .setReserved(reserved)
                .setPortType(port_type)
                .setPortSignalType(signal_type)
                .setNode_id(local_node)
                .setRemote_node_id(remote_node)
                .setRemote_port_no(remote_port).build();

                descList.add(tempdesc);

                //processLink(link);
            }
        }

        OFNBPortDescInterface tempportdesc;
        states.add(OFPortState.LIVE);
        configs.add(OFPortConfig.NO_FWD);

        tempportdesc = ofnbPortDesc.createBuilder()
                .setConfig(configs)
                .setState(states)
                //.setPortNo(port)
                .setName(deviceName)
                //.setHwAddr()
                .setDesc(descList).build();

        handler.channel.write(Collections.singletonList(tempportdesc));//通过channelHandler发送OFNBPortDesc消息给MDSC
    }

    private void processPortLinks(Device device, Port port) {
        ConnectPoint connectPoint = new ConnectPoint(device.id(), port.number());
        for (Link link : linkService.getLinks(connectPoint)) {
            if (link.isDurable() && link.type() == OPTICAL) {
                processLink(link);
            }
        }
    }

    private void processLink(Link link) {
        DeviceId srcId = link.src().deviceId();
        DeviceId dstId = link.dst().deviceId();
        Port srcPort = deviceService.getPort(srcId, link.src().port());
        Port dstPort = deviceService.getPort(dstId, link.dst().port());

        if (srcPort == null || dstPort == null) {
            return; //FIXME remove this in favor of below TODO
        }

        boolean active = deviceService.isAvailable(srcId) &&
                deviceService.isAvailable(dstId) &&
                // TODO: should update be queued if src or dstPort is null?
                //srcPort != null && dstPort != null &&
                srcPort.isEnabled() && dstPort.isEnabled();

        LinkDescription desc = new DefaultLinkDescription(link.src(), link.dst(), OPTICAL);
        if (active) {
            providerService.linkDetected(desc);
        } else {
            providerService.linkVanished(desc);
        }
    }
}
