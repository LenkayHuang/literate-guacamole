package org.onosproject.ofnb;

import com.google.common.collect.ImmutableSet;
import org.jboss.netty.buffer.ChannelBuffer;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public interface OFNBPortDescInterface extends OFObject {
    OFPort getPortNo();
    MacAddress getHwAddr();
    String getName();
    Set<OFPortConfig> getConfig();
    Set<OFPortState> getState();
    List<OFNBPortDescPropOpticalTransportInterface> getDesc();
    OFVersion getVersion();


    void writeTo(ChannelBuffer channelBuffer);

    Builder createBuilder();
    public interface Builder  {
        OFNBPortDescInterface build();
        OFPort getPortNo();
        Builder setPortNo(OFPort portNo);
        MacAddress getHwAddr();
        Builder setHwAddr(MacAddress hwAddr);
        String getName();
        Builder setName(String name);
        Set<OFPortConfig> getConfig();
        Builder setConfig(Set<OFPortConfig> config);
        Set<OFPortState> getState();
        Builder setState(Set<OFPortState> state);
        List<OFNBPortDescPropOpticalTransportInterface> getDesc();
        Builder setDesc(List<OFNBPortDescPropOpticalTransportInterface> desc);
        OFVersion getVersion();
    }
}