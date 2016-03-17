package org.onosproject.ofnb;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.ver13.OFPortConfigSerializerVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFPortStateSerializerVer13;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U16;
import org.projectfloodlight.openflow.util.ChannelUtils;
import org.projectfloodlight.openflow.util.FunnelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Created by Atreia on 2016/3/9.
 * For Huawei PNC northbound optical layer multipart messages
 */
public class OFNBPortDesc implements OFNBPortDescInterface {
    private static final Logger logger = LoggerFactory.getLogger(OFNBPortDesc.class);
    // version: 1.3
    //final static byte WIRE_VERSION = 4;
    final static int LENGTH = 64;

    private final static OFPort DEFAULT_PORT_NO = OFPort.ANY;
    private final static MacAddress DEFAULT_HW_ADDR = MacAddress.NONE;
    private final static String DEFAULT_NAME = "";
    private final static Set<OFPortConfig> DEFAULT_CONFIG = ImmutableSet.<OFPortConfig>of();
    private final static Set<OFPortState> DEFAULT_STATE = ImmutableSet.<OFPortState>of();
    private final static List<OFNBPortDescPropOpticalTransportInterface> DEFAULT_DESC = null;
    //private final static Set<OFPortFeatures> DEFAULT_CURR = ImmutableSet.<OFPortFeatures>of();
    //private final static Set<OFPortFeatures> DEFAULT_ADVERTISED = ImmutableSet.<OFPortFeatures>of();
    //private final static Set<OFPortFeatures> DEFAULT_SUPPORTED = ImmutableSet.<OFPortFeatures>of();
    //private final static Set<OFPortFeatures> DEFAULT_PEER = ImmutableSet.<OFPortFeatures>of();
    //private final static long DEFAULT_CURR_SPEED = 0x0L;
    //private final static long DEFAULT_MAX_SPEED = 0x0L;

    @Override
    public OFPort getPortNo() {
        return portNo;
    }

    @Override
    public MacAddress getHwAddr() {
        return hwAddr;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<OFPortConfig> getConfig() {
        return config;
    }

    @Override
    public Set<OFPortState> getState() {
        return state;
    }

    @Override
    public List<OFNBPortDescPropOpticalTransportInterface> getDesc() {
        return desc;
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

    // OF message fields
    private final OFPort portNo;
    private final MacAddress hwAddr;
    private final String name;
    private final Set<OFPortConfig> config;
    private final Set<OFPortState> state;
    private final List<OFNBPortDescPropOpticalTransportInterface> desc;
    //private final Set<OFPortFeatures> curr;
    //private final Set<OFPortFeatures> advertised;
    //private final Set<OFPortFeatures> supported;
    //private final Set<OFPortFeatures> peer;
    //private final long currSpeed;
    //private final long maxSpeed;
    //
    // Immutable default instance
    /*final static OFNBPortDesc DEFAULT = new OFNBPortDesc(
            DEFAULT_PORT_NO, DEFAULT_HW_ADDR, DEFAULT_NAME, DEFAULT_CONFIG, DEFAULT_STATE);*/


    OFNBPortDesc(OFPort portNo, MacAddress hwAddr, String name, Set<OFPortConfig> config, Set<OFPortState> state, List<OFNBPortDescPropOpticalTransportInterface> desc) {
        if(portNo == null) {
            throw new NullPointerException("OFNBPortDesc: property portNo cannot be null");
        }
        if(hwAddr == null) {
            throw new NullPointerException("OFNBPortDesc: property hwAddr cannot be null");
        }
        if(name == null) {
            throw new NullPointerException("OFNBPortDesc: property name cannot be null");
        }
        if(config == null) {
            throw new NullPointerException("OFNBPortDesc: property config cannot be null");
        }
        if(state == null) {
            throw new NullPointerException("OFNBPortDesc: property state cannot be null");
        }
        if(desc == null) {
            throw new NullPointerException("OFNBPortDesc: property desc cannot be null");
        }

        this.portNo = portNo;
        this.hwAddr = hwAddr;
        this.name = name;
        this.config = config;
        this.state = state;
        this.desc = desc;

    }

    //TO DO:
    //1.getter & setter(in builder)
    //2.builder
    public OFNBPortDescInterface.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFNBPortDescInterface.Builder {
        final OFNBPortDesc parentMessage;

        // OF message fields
        private boolean portNoSet;
        private OFPort portNo;
        private boolean hwAddrSet;
        private MacAddress hwAddr;
        private boolean nameSet;
        private String name;
        private boolean configSet;
        private Set<OFPortConfig> config;
        private boolean stateSet;
        private Set<OFPortState> state;
        private boolean descSet;
        private List<OFNBPortDescPropOpticalTransportInterface> desc;

        BuilderWithParent(OFNBPortDesc parentMessage) {
            this.parentMessage = parentMessage;
        }

        @Override
        public OFPort getPortNo() {
            return portNo;
        }

        @Override
        public OFNBPortDescInterface.Builder setPortNo(OFPort portNo) {
            this.portNo = portNo;
            this.portNoSet = true;
            return this;
        }
        @Override
        public MacAddress getHwAddr() {
            return hwAddr;
        }

        @Override
        public OFNBPortDescInterface.Builder setHwAddr(MacAddress hwAddr) {
            this.hwAddr = hwAddr;
            this.hwAddrSet = true;
            return this;
        }
        @Override
        public String getName() {
            return name;
        }

        @Override
        public OFNBPortDescInterface.Builder setName(String name) {
            this.name = name;
            this.nameSet = true;
            return this;
        }
        @Override
        public Set<OFPortConfig> getConfig() {
            return config;
        }

        @Override
        public OFNBPortDescInterface.Builder setConfig(Set<OFPortConfig> config) {
            this.config = config;
            this.configSet = true;
            return this;
        }
        @Override
        public Set<OFPortState> getState() {
            return state;
        }

        @Override
        public OFNBPortDescInterface.Builder setState(Set<OFPortState> state) {
            this.state = state;
            this.stateSet = true;
            return this;
        }
        @Override
        public List<OFNBPortDescPropOpticalTransportInterface> getDesc() {
            return desc;
        }

        @Override
        public OFNBPortDescInterface.Builder setDesc(List<OFNBPortDescPropOpticalTransportInterface> desc) {
            this.desc = desc;
            this.descSet = true;
            return this;
        }
        @Override
        public OFVersion getVersion() {
            return OFVersion.OF_13;
        }



        @Override
        public OFNBPortDescInterface build() {
            OFPort portNo = this.portNoSet ? this.portNo : parentMessage.portNo;
            if(portNo == null)
                throw new NullPointerException("Property portNo must not be null");
            MacAddress hwAddr = this.hwAddrSet ? this.hwAddr : parentMessage.hwAddr;
            if(hwAddr == null)
                throw new NullPointerException("Property hwAddr must not be null");
            String name = this.nameSet ? this.name : parentMessage.name;
            if(name == null)
                throw new NullPointerException("Property name must not be null");
            Set<OFPortConfig> config = this.configSet ? this.config : parentMessage.config;
            if(config == null)
                throw new NullPointerException("Property config must not be null");
            Set<OFPortState> state = this.stateSet ? this.state : parentMessage.state;
            if(state == null)
                throw new NullPointerException("Property state must not be null");
            List<OFNBPortDescPropOpticalTransportInterface> desc = this.descSet ? this.desc : parentMessage.desc;
            if(desc == null)
                throw new NullPointerException("Property desc must not be null");

            //
            return new OFNBPortDesc(
                    portNo,
                    hwAddr,
                    name,
                    config,
                    state,
                    desc
            );
        }
    }

    static class Builder implements OFNBPortDescInterface.Builder {
        // OF message fields
        private boolean portNoSet;
        private OFPort portNo;
        private boolean hwAddrSet;
        private MacAddress hwAddr;
        private boolean nameSet;
        private String name;
        private boolean configSet;
        private Set<OFPortConfig> config;
        private boolean stateSet;
        private Set<OFPortState> state;
        private boolean descSet;
        private List<OFNBPortDescPropOpticalTransportInterface> desc;

        @Override
        public OFPort getPortNo() {
            return portNo;
        }

        @Override
        public OFNBPortDescInterface.Builder setPortNo(OFPort portNo) {
            this.portNo = portNo;
            this.portNoSet = true;
            return this;
        }
        @Override
        public MacAddress getHwAddr() {
            return hwAddr;
        }

        @Override
        public OFNBPortDescInterface.Builder setHwAddr(MacAddress hwAddr) {
            this.hwAddr = hwAddr;
            this.hwAddrSet = true;
            return this;
        }
        @Override
        public String getName() {
            return name;
        }

        @Override
        public OFNBPortDescInterface.Builder setName(String name) {
            this.name = name;
            this.nameSet = true;
            return this;
        }
        @Override
        public Set<OFPortConfig> getConfig() {
            return config;
        }

        @Override
        public OFNBPortDescInterface.Builder setConfig(Set<OFPortConfig> config) {
            this.config = config;
            this.configSet = true;
            return this;
        }
        @Override
        public Set<OFPortState> getState() {
            return state;
        }

        @Override
        public OFNBPortDescInterface.Builder setState(Set<OFPortState> state) {
            this.state = state;
            this.stateSet = true;
            return this;
        }
        @Override
        public List<OFNBPortDescPropOpticalTransportInterface> getDesc() {
            return desc;
        }

        @Override
        public OFNBPortDescInterface.Builder setDesc(List<OFNBPortDescPropOpticalTransportInterface> desc) {
            this.desc = desc;
            this.descSet = true;
            return this;
        }
        @Override
        public OFVersion getVersion() {
            return OFVersion.OF_13;
        }

        //
        @Override
        public OFNBPortDescInterface build() {
            OFPort portNo = this.portNoSet ? this.portNo : DEFAULT_PORT_NO;
            if(portNo == null)
                throw new NullPointerException("Property portNo must not be null");
            MacAddress hwAddr = this.hwAddrSet ? this.hwAddr : DEFAULT_HW_ADDR;
            if(hwAddr == null)
                throw new NullPointerException("Property hwAddr must not be null");
            String name = this.nameSet ? this.name : DEFAULT_NAME;
            if(name == null)
                throw new NullPointerException("Property name must not be null");
            Set<OFPortConfig> config = this.configSet ? this.config : DEFAULT_CONFIG;
            if(config == null)
                throw new NullPointerException("Property config must not be null");
            Set<OFPortState> state = this.stateSet ? this.state : DEFAULT_STATE;
            if(state == null)
                throw new NullPointerException("Property state must not be null");
            List<OFNBPortDescPropOpticalTransportInterface> desc = this.descSet ? this.desc : DEFAULT_DESC;
            if(desc == null)
                throw new NullPointerException("Property desc must not be null");


            return new OFNBPortDesc(
                    portNo,
                    hwAddr,
                    name,
                    config,
                    state,
                    desc
            );
        }
    }

    //TO DO:
    //1.reader  &putTo  &writeTo
    //2.equals & toString()
    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFNBPortDescFunnel FUNNEL = new OFNBPortDescFunnel();
    static class OFNBPortDescFunnel implements Funnel<OFNBPortDesc> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFNBPortDesc message, PrimitiveSink sink) {
            message.portNo.putTo(sink);
            // FIXME: skip funnel of length
            // skip pad (2 bytes)
            message.hwAddr.putTo(sink);
            // skip pad (2 bytes)
            sink.putUnencodedChars(message.name);
            OFPortConfigSerializerVer13.putTo(message.config, sink);
            OFPortStateSerializerVer13.putTo(message.state, sink);
            FunnelUtils.putList(message.desc, sink);
        }
    }

    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFNBPortDesc> {
        @Override
        public void write(ChannelBuffer bb, OFNBPortDesc message) {
            int startIndex = bb.writerIndex();
            message.portNo.write4Bytes(bb);
            // length is length of variable message, will be updated at the end
            int lengthIndex = bb.writerIndex();
            bb.writeShort(U16.t(0));

            // pad: 2 bytes
            bb.writeZero(2);
            message.hwAddr.write6Bytes(bb);
            // pad: 2 bytes
            bb.writeZero(2);
            ChannelUtils.writeFixedLengthString(bb, message.name, 16);
            OFPortConfigSerializerVer13.writeTo(bb, message.config);
            OFPortStateSerializerVer13.writeTo(bb, message.state);
            ChannelUtils.writeList(bb, message.desc);

            // update length field
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);

        }
    }
}
