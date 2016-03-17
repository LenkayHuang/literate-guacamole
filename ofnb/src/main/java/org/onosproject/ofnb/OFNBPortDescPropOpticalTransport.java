package org.onosproject.ofnb;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.projectfloodlight.openflow.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.projectfloodlight.openflow.types.*;

/**
 * Created by Atreia on 2016/3/9.
 * For Huawei PNC northbound optical layer multipart messages
 */
public class OFNBPortDescPropOpticalTransport implements
        OFNBPortDescPropOpticalTransportInterface {
    private static final Logger logger = LoggerFactory.getLogger(OFNBPortDescPropOpticalTransport.class);
    // version: 1.3
    final static byte WIRE_VERSION = 4;
    final static int LENGTH = 8;

    private final static int DEFAULT_TYPE = 0x0;
    private final static short DEFAULT_PORT_SIGNAL_TYPE = (short) 0x0;
    private final static short DEFAULT_RESERVED = (short) 0x0;
    private final static short DEFAULT_PORT_TYPE = (short) 0x0;
    private final static int DEFAULT_NODE_ID = (int)0x0;
    private final static int DEFAULT_REMOTE_NODE_ID = (int)0x0;
    private final static int DEFAULT_REMOTE_PORT_NO = (int)0x0;

    public int getType() {
        return type;
    }
    @Override
    public short getPortSignalType() {
        return portSignalType;
    }
    @Override
    public short getReserved() {
        return reserved;
    }
    @Override
    public short getPortType() {
        return portType;
    }
    @Override
    public String getNode_id() {
        return node_id;
    }
    @Override
    public String getRemote_node_id() {
        return remote_node_id;
    }
    @Override
    public String getRemote_port_no() {
        return remote_port_no;
    }
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }


    // OF message fields
    private final int type;
    private final short portSignalType;
    private final short reserved;
    private final short portType;
    private final String node_id;
    private final String remote_node_id;
    private final String remote_port_no;

    // Immutable default instance
    OFNBPortDescPropOpticalTransport(int type, short portSignalType, short reserved, short portType, String node_id, String remote_node_id, String remote_port_no) {
        this.type = type;
        this.portSignalType = portSignalType;
        this.reserved = reserved;
        this.portType = portType;
        this.node_id = node_id;
        this.remote_node_id = remote_node_id;
        this.remote_port_no = remote_port_no;
    }

    //TO DO:
    //1.getter & setter(in builder)
    //2.builder
    public OFNBPortDescPropOpticalTransportInterface.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFNBPortDescPropOpticalTransportInterface.Builder {
        final OFNBPortDescPropOpticalTransport parentMessage;
        // OF message fields
        private boolean typeSet;
        private int type;
        private boolean portSignalTypeSet;
        private short portSignalType;
        private boolean reservedSet;
        private short reserved;
        private boolean portTypeSet;
        private short portType;
        private boolean node_idSet;
        private String node_id;
        private boolean remote_node_idSet;
        private String remote_node_id;
        private boolean remote_port_noSet;
        private String remote_port_no;

        BuilderWithParent(OFNBPortDescPropOpticalTransport parentMessage) {
            this.parentMessage = parentMessage;
        }

        @Override
        public int getType(){
            return type;
        };

        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setType(int type){
            this.type=type;
            this.typeSet = true;
            return this;
        }
        @Override
        public short getPortSignalType(){
            return portSignalType;
        };

        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setPortSignalType(short portSignalType){
            this.portSignalType=portSignalType;
            this.portSignalTypeSet = true;
            return this;
        }
        @Override
        public short getReserved(){
            return reserved;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setReserved(short reserved){
            this.reserved=reserved;
            this.reservedSet = true;
            return this;
        }
        @Override
        public short getPortType(){
            return portType;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setPortType(short portType){
            this.portType=portType;
            this.portTypeSet = true;
            return this;
        }
        @Override
        public String getNode_id(){
            return node_id;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setNode_id(String node_id){
            this.node_id=node_id;
            this.node_idSet = true;
            return this;
        }
        @Override
        public String getRemote_node_id(){
            return remote_node_id;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setRemote_node_id(String remote_node_id){
            this.remote_node_id=remote_node_id;
            this.remote_node_idSet = true;
            return this;
        }
        @Override
        public String getRemote_port_no(){
            return remote_port_no;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setRemote_port_no(String remote_port_no){
            this.remote_port_no=remote_port_no;
            this.remote_port_noSet = true;
            return this;
        }
        @Override
        public OFVersion getVersion() {
            return OFVersion.OF_13;
        }

        @Override
        public OFNBPortDescPropOpticalTransportInterface build() {
            int type = this.typeSet ? this.type : parentMessage.type;
            if(type == 0)
                throw new NullPointerException("OFNB type must not be null");
            short portSignalType = this.portSignalTypeSet ? this.portSignalType : parentMessage.portSignalType;
            if(portSignalType == 0)
                throw new NullPointerException("OFNB portSignalType must not be null");
            short reserved = this.reservedSet ? this.reserved : parentMessage.reserved;
            if(reserved == 0)
                throw new NullPointerException("OFNB reserved must not be null");
            short portType = this.portTypeSet ? this.portType : parentMessage.portType;
            if(portType == 0)
                throw new NullPointerException("OFNB portType must not be null");
            /*int node_id = this.node_idSet ? this.node_id : parentMessage.node_id;
            if(node_id == 0)
                throw new NullPointerException("OFNB node_id must not be null");
            int remote_node_id = this.remote_node_idSet ? this.remote_node_id : parentMessage.remote_node_id;
            if(remote_node_id == 0)
                throw new NullPointerException("OFNB remote_node_id must not be null");
            int remote_port_no = this.remote_port_noSet ? this.remote_port_no : parentMessage.remote_port_no;
            if(remote_port_no == 0)
                throw new NullPointerException("OFNB remote_port_no must not be null");*/

            //
            return new OFNBPortDescPropOpticalTransport(
                    type,
                    portSignalType,
                    reserved,
                    portType,
                    node_id,
                    remote_node_id,
                    remote_port_no
            );
        }

    }
    static class Builder implements OFNBPortDescPropOpticalTransportInterface.Builder {
        // OF message fields
        private boolean typeSet;
        private int type;
        private boolean portSignalTypeSet;
        private short portSignalType;
        private boolean reservedSet;
        private short reserved;
        private boolean portTypeSet;
        private short portType;
        private boolean node_idSet;
        private String node_id;
        private boolean remote_node_idSet;
        private String remote_node_id;
        private boolean remote_port_noSet;
        private String remote_port_no;


        @Override
        public int getType(){
            return type;
        };

        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setType(int type){
            this.type=type;
            this.typeSet = true;
            return this;
        }
        @Override
        public short getPortSignalType(){
            return portSignalType;
        };

        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setPortSignalType(short portSignalType){
            this.portSignalType=portSignalType;
            this.portSignalTypeSet = true;
            return this;
        }
        @Override
        public short getReserved(){
            return reserved;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setReserved(short reserved){
            this.reserved=reserved;
            this.reservedSet = true;
            return this;
        }
        @Override
        public short getPortType(){
            return portType;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setPortType(short portType){
            this.portType=portType;
            this.portTypeSet = true;
            return this;
        }
        @Override
        public String getNode_id(){
            return node_id;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setNode_id(String node_id){
            this.node_id=node_id;
            this.node_idSet = true;
            return this;
        }
        @Override
        public String getRemote_node_id(){
            return remote_node_id;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setRemote_node_id(String remote_node_id){
            this.remote_node_id=remote_node_id;
            this.remote_node_idSet = true;
            return this;
        }
        @Override
        public String getRemote_port_no(){
            return remote_port_no;
        };


        @Override
        public OFNBPortDescPropOpticalTransportInterface.Builder setRemote_port_no(String remote_port_no){
            this.remote_port_no=remote_port_no;
            this.remote_port_noSet = true;
            return this;
        }
        @Override
        public OFVersion getVersion() {
            return OFVersion.OF_13;
        }

        @Override
        public OFNBPortDescPropOpticalTransportInterface build() {
            int type = this.typeSet ? this.type : DEFAULT_TYPE;
            if(type == 0)
                throw new NullPointerException("Property type must not be null");
            short portSignalType = this.portSignalTypeSet ? this.portSignalType : DEFAULT_PORT_SIGNAL_TYPE;
            if(portSignalType == 0)
                throw new NullPointerException("Property portSignalType must not be null");
            short reserved = this.reservedSet ? this.reserved : DEFAULT_RESERVED;
            if(reserved == 0)
                throw new NullPointerException("Property reserved must not be null");
            short portType = this.portTypeSet ? this.portType : DEFAULT_PORT_TYPE;
            if(portType == 0)
                throw new NullPointerException("Property portType must not be null");
            /*int node_id = this.node_idSet ? this.node_id :DEFAULT_NODE_ID;
            if(node_id == 0)
                throw new NullPointerException("Property node_id must not be null");
            int remote_node_id = this.remote_node_idSet ? this.remote_node_id : DEFAULT_REMOTE_NODE_ID;
            if(remote_node_id == 0)
                throw new NullPointerException("Property remote_node_id must not be null");
            int remote_port_no = this.remote_port_noSet ? this.remote_port_no : DEFAULT_REMOTE_PORT_NO;
            if(remote_port_no == 0)
                throw new NullPointerException("Property remote_port_no must not be null");*/

            //
            return new OFNBPortDescPropOpticalTransport(
                    type,
                    portSignalType,
                    reserved,
                    portType,
                    node_id,
                    remote_node_id,
                    remote_port_no
            );
        }

    }
    //TO DO:
    //1.reader  &putTo  &writeTo
    //2.equals & toString()
    /*final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFNBPortDescPropOpticalTransportInterface> {
        @Override
        public OFNBPortDescPropOpticalTransportInterface readFrom(ChannelBuffer bb) throws OFParseError {
            int start = bb.readerIndex();
            int type = U16.f(bb.readShort());
            int length = U16.f(bb.readShort());
            if(length != 8)
                throw new OFParseError("Wrong length: Expected=8(8), got="+length);
            if(bb.readableBytes() + (bb.readerIndex() - start) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            if(logger.isTraceEnabled())
                logger.trace("readFrom - length={}", length);
            short portSignalType = U8.f(bb.readByte());
            short reserved = U8.f(bb.readByte());
            // pad: 1 bytes
            bb.skipBytes(1);
            short portType = U8.f(bb.readByte());
            int node_id = U16.f(bb.readShort());
            int remote_node_id = U16.f(bb.readShort());
            int remote_port_no = U16.f(bb.readShort());

            OFNBPortDescPropOpticalTransport portDescPropOpticalTransport = new OFNBPortDescPropOpticalTransport(
                    type,
                    portSignalType,
                    reserved,
                    portType,
                    node_id,
                    remote_node_id,
                    remote_port_no
            );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", portDescPropOpticalTransport);
            return portDescPropOpticalTransport;
        }
    }*/

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }
    final static OFNBPortDescPropOpticalTransportFunnel FUNNEL = new OFNBPortDescPropOpticalTransportFunnel();
    static class OFNBPortDescPropOpticalTransportFunnel implements Funnel<OFNBPortDescPropOpticalTransport> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFNBPortDescPropOpticalTransport message, PrimitiveSink sink) {
            sink.putInt(message.type);
            // FIXME: skip funnel of length
            // skip pad (2 bytes)
            sink.putShort((short) 0x8);
            sink.putShort(message.portSignalType);
            sink.putShort(message.reserved);
            sink.putShort(message.portType);
            sink.putInt(Integer.parseInt(message.node_id));
            sink.putInt(Integer.parseInt(message.remote_node_id));
            sink.putInt(Integer.parseInt(message.remote_port_no));
            // skip pad (2 bytes)
        }
    }
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }
    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFNBPortDescPropOpticalTransport> {
        @Override
        public void write(ChannelBuffer bb, OFNBPortDescPropOpticalTransport message) {
            bb.writeShort(U16.t(message.type));
            // fixed value property length = 8
            bb.writeByte(U8.t(message.portSignalType));
            bb.writeByte(U8.t(message.reserved));
            // pad: 1 bytes
            bb.writeZero(1);
            bb.writeByte(U8.t(message.portType));
            bb.writeShort(U16.t(Integer.parseInt(message.node_id)));
            bb.writeShort(U16.t(Integer.parseInt(message.remote_node_id)));
            bb.writeShort(U16.t(Integer.parseInt(message.remote_port_no)));
        }
    }
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFNBPortDescPropOpticalTransport(");
        b.append("type=").append(type);
        b.append(", ");
        b.append("portSignalType=").append(portSignalType);
        b.append(", ");
        b.append("reserved=").append(reserved);
        b.append(", ");
        b.append("portType=").append(portType);
        b.append(", ");
        b.append("node_id=").append(node_id);
        b.append(", ");
        b.append("remote_node_id=").append(remote_node_id);
        b.append(", ");
        b.append("remote_port_no=").append(remote_port_no);
        b.append(")");
        return b.toString();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFNBPortDescPropOpticalTransport other = (OFNBPortDescPropOpticalTransport) obj;

        if( type != other.type)
            return false;
        if( portSignalType != other.portSignalType)
            return false;
        if( reserved != other.reserved)
            return false;
        if( portType != other.portType)
            return false;
        if( node_id != other.node_id)
            return false;
        if( remote_node_id != other.remote_node_id)
            return false;
        if( remote_port_no != other.remote_port_no)
            return false;
        return true;
    }
    /*@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + type;
        result = prime * result + portSignalType;
        result = prime * result + reserved;
        result = prime * result + portType;
        result = prime * result + node_id;
        result = prime * result + remote_node_id;
        result = prime * result + remote_port_no;
        return result;
    }*/
}
