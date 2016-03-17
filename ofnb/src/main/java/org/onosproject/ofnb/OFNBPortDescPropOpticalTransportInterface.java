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

public interface OFNBPortDescPropOpticalTransportInterface extends OFObject{
    public int getType() ;

    public short getPortSignalType();

    public short getReserved();

    public short getPortType();

    public String getNode_id();

    public String getRemote_node_id();

    public String getRemote_port_no();

    void writeTo(ChannelBuffer channelBuffer);
    Builder createBuilder();
    public interface Builder  {
        OFNBPortDescPropOpticalTransportInterface build();
        int getType() ;
        Builder setType(int type) ;
        short getPortSignalType();
        Builder setPortSignalType(short portSignalType);
        short getReserved();
        Builder setReserved(short reserved);
        short getPortType();
        Builder setPortType(short portType);
        String getNode_id();
        Builder setNode_id(String node_id);
        String getRemote_node_id();
        Builder setRemote_node_id(String remote_node_id);
        String getRemote_port_no();
        Builder setRemote_port_no(String remote_port_no);
        OFVersion getVersion();
    }

}