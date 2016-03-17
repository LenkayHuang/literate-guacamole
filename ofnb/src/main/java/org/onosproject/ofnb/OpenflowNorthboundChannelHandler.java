package org.onosproject.ofnb;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.onosproject.net.Link;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.DeviceStore;
import org.onosproject.net.link.LinkService;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

import static org.onosproject.net.Link.Type.OPTICAL;

//import org.onosproject.openflow.controller.driver.OpenFlowSwitchDriver;
//import org.onosproject.openflow.controller.driver.SwitchStateException;

public class OpenflowNorthboundChannelHandler extends IdleStateAwareChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(OpenflowNorthboundChannelHandler.class);
    private static final String RESET_BY_PEER = "Connection reset by peer";
    private static final String BROKEN_PIPE = "Broken pipe";
    private final OpenflowNorthboundClient client;
    //private long thisdpid; // channelHandler cached value of connected switch id
    protected Channel channel;
    // State needs to be volatile because the HandshakeTimeoutHandler
    // needs to check if the handshake is complete
    private volatile ChannelState state;
    private DatapathId dpid;
    private static int missSendLen;
    protected OFVersion ofVersion;
    protected OFFactory factory13;
    protected OFFactory factory10;
    private static OFControllerRole role;
    private static long xidTemp;

    //private final DeviceListener deviceListener = new InternalDeviceListener();

    protected DeviceService deviceService;
    protected DeviceStore store;
    protected LinkService linkService;
    protected OFNBPortDesc ofnbPortDesc;
    protected OFNBPortDescPropOpticalTransport ofnbPortDescPropOpticalTransport;
    protected List<OFNBPortDescPropOpticalTransportInterface> descList;
    protected Set<OFPortState> states;
    protected Set<OFPortConfig> configs;
    protected String deviceName;

    protected NorthboundOpticalProvider provider;

    //protected NorthboundOpticalProvider OFNBProvider = new NorthboundOpticalProvider();

    /**
     * transaction Ids to use during handshake. Since only one thread
     * calls into an OFChannelHandler instance, we don't need atomic.
     * We will count down
     */
    private int handshakeTransactionIds = -1;

    OpenflowNorthboundChannelHandler(OpenflowNorthboundClient client, DeviceService deviceService, DeviceStore store, LinkService linkService) {
        this.client = client;
        this.state = ChannelState.INIT;
        factory13 = client.getOFMessageFactory13();
        factory10 = client.getOFMessageFactory10();
        this.dpid = DatapathId.of(10086L);//HLK:fix the dpid for example
        this.deviceService = deviceService;
        this.store = store;
        this.linkService = linkService;

        this.provider = new NorthboundOpticalProvider();
        this.provider.init(this);//传入Channel_handler本身，并初始化provider
    }

    enum ChannelState {
        /**
         * Initial state before channel is connected.
         */
        INIT(false) {
            @Override
            void processOFMessage(OpenflowNorthboundChannelHandler h, OFMessage m)
                    throws IOException {
                //illegalMessageReceived(h, m);
                log.info("HLK:Received illegal message {} before channel connected", m.getType());
            }

            @Override
            void processOFError(OpenflowNorthboundChannelHandler h, OFErrorMsg m)
                    throws IOException {
                // need to implement since its abstract but it will never
                // be called
                log.info("HLK:Received illegal message {} before channel connected", m.getType());
            }

            //@Override
            void processOFPortStatus(OpenflowNorthboundChannelHandler h, OFPortStatus m)
                    throws IOException {
                //unhandledMessageReceived(h, m);
                log.info("HLK:Received illegal message {} before channel connected", m.getType());
            }
        },

        WAIT_MDSC_HELLO_RETURN(false) {
            @Override
            void processOFHello(OpenflowNorthboundChannelHandler h, OFHello m)
                    throws IOException {
                // TODO We could check for the optional bitmap, but for now
                // we are just checking the version number.
                if (m.getVersion().getWireVersion() >= OFVersion.OF_13.getWireVersion()) {
                    log.info("Received {}(version) Hello from MDSC {} "
                            , m.getVersion(),
                            h.channel.getRemoteAddress());
                    //h.sendHandshakeHelloMessage();
                    h.ofVersion = OFVersion.OF_13;
                } else {
                    log.info("HLK: Received message's OFVersion error!");
                    h.channel.disconnect();
                    return;
                }
                //h.sendHandshakeFeaturesRequestMessage();
                h.setState(WAIT_FEATURES_REQUEST);
            }

            @Override
            void processOFError(OpenflowNorthboundChannelHandler h, OFErrorMsg m) {
                log.info("HLK hello succeed, and there're sth wrong!");
                //h.setState(INIT);
            }
        },

        WAIT_FEATURES_REQUEST(false) {
            @Override
            void processOFFeaturesRequest(OpenflowNorthboundChannelHandler h, OFFeaturesRequest m)
                    throws IOException {
                log.info("HLK feature_request received!");
                h.sendFeaturesReplyMessage();
                h.setState(WAIT_PORT_DESC_REQUEST);
                //log.info("HLK hello succeed");
                //h.setState(INIT);
            }

            /*@Override
            void processOFStatisticsReply(OFChannelHandler h,
                                      OFStatsReply  m)
                throws IOException, SwitchStateException {
            illegalMessageReceived(h, m);
            }*/
            @Override
            void processOFError(OpenflowNorthboundChannelHandler h, OFErrorMsg m) {
                log.info("HLK feature_request received, and there're sth wrong!");
                //h.setState(INIT);
            }
        },

        WAIT_PORT_DESC_REQUEST(false) {
            @Override
            void processOFStatisticsRequest (OpenflowNorthboundChannelHandler h, OFStatsRequest m)
            throws IOException {
                if (m.getStatsType() != OFStatsType.PORT_DESC) {
                    log.info("Expecting port description stats but received stats "
                                    + "type {} from {}. Ignoring ...", m.getStatsType(),
                            h.channel.getRemoteAddress());
                    return;
                }
                else {
                    log.info("statistics request message {} received!", m.getStatsType());
                    h.sendOFPortDescReply();
                    log.info("send complete!");
                    h.setState(WAIT_CONFIG_REQUEST);
                }
            }

            @Override
            void processOFFeaturesRequest (OpenflowNorthboundChannelHandler h, OFFeaturesRequest m)
                throws IOException {
                illegalMessageReceived(h, m);
            }

            @Override
            void processOFError (OpenflowNorthboundChannelHandler h, OFErrorMsg m) {
                log.info("HLK port_desc_request received, and there're sth wrong!");
            }
        },

        WAIT_CONFIG_REQUEST(false) {
            @Override
            void processOFSetConfig(OpenflowNorthboundChannelHandler h, OFSetConfig m)
            throws IOException {
                log.info("set config message received!");
                missSendLen = m.getMissSendLen();
                h.sendOFGetConfigReply();
                h.setState(WAIT_DESC_STAT_REQUEST);
            }

            @Override
            void processOFBarrierRequest(OpenflowNorthboundChannelHandler h, OFBarrierRequest m)
                    throws IOException {
                log.info("barrier request message received!");
                h.sendOFGetConfigReply();
                h.setState(WAIT_DESC_STAT_REQUEST);
            }


            @Override
            void processOFGetConfigRequest (OpenflowNorthboundChannelHandler h, OFGetConfigRequest m) throws IOException{
                log.info("get config request message received!");
                h.sendOFGetConfigReply();
                h.setState(WAIT_DESC_STAT_REQUEST);
            }

            @Override
            void processOFError (OpenflowNorthboundChannelHandler h, OFErrorMsg m) {
                log.info("HLK wait_config_request received, and there're sth wrong!");
                //h.setState(INIT);
            }
        },

        WAIT_DESC_STAT_REQUEST(false) {
            @Override
            void processOFStatisticsRequest (OpenflowNorthboundChannelHandler h, OFStatsRequest m)
            throws IOException {
                if (m.getStatsType() != OFStatsType.DESC) {
                    log.info("Expecting port description stats but received stats "
                                    + "type {} from {}. Ignoring ...", m.getStatsType(),
                            h.channel.getRemoteAddress());
                    return;
                }
                else {
                    log.info("statistics request message(DESC) received!");
                    h.sendOFDescReply();
                    h.setState(ACTIVE);
                }
            }

            @Override
            void processOFError (OpenflowNorthboundChannelHandler h, OFErrorMsg m){
                log.info("HLK desc_request received, and there're sth wrong!");
                //h.setState(INIT);
            }
        },

        ACTIVE(true) {
            //need to be add
            @Override
            void processOFRoleRequest(OpenflowNorthboundChannelHandler h, OFRoleRequest m)
                    throws IOException {
                log.info("Role request message received!");
                xidTemp = m.getXid();
                role = m.getRole();
                h.sendOFRoleReplyMessage();

                //TEST to send port Status
                log.info("HLK:Start to TEST");
                h.sendOFPortStatusMessage();
            }
        };

        private final boolean handshakeComplete;
        ChannelState(boolean handshakeComplete) {
            this.handshakeComplete = handshakeComplete;
        }

        /**
         * Is this a state in which the handshake has completed?
         * @return true if the handshake is complete
         */
        public boolean isHandshakeComplete() {
            return handshakeComplete;
        }

        void processOFMessage(OpenflowNorthboundChannelHandler h, OFMessage m)
                throws IOException {
            OFType ty = m.getType();
            log.info("type is : {}", ty);
            switch (ty) {
                case HELLO:
                    processOFHello(h, (OFHello) m);
                    break;
                //case BARRIER_REPLY:
                //processOFBarrierReply(h, (OFBarrierReply) m);
                //break;
                case ECHO_REPLY:
                    processOFEchoReply(h, (OFEchoReply) m);
                    break;
                case ECHO_REQUEST:
                    processOFEchoRequest(h, (OFEchoRequest) m);
                    break;
                case ERROR:
                    processOFError(h, (OFErrorMsg) m);
                    break;
                    /*
                case FEATURES_REPLY:
                    processOFFeaturesReply(h, (OFFeaturesReply) m);
                    break;
                case FLOW_REMOVED:
                    processOFFlowRemoved(h, (OFFlowRemoved) m);
                    break;
                case GET_CONFIG_REPLY:
                    processOFGetConfigReply(h, (OFGetConfigReply) m);
                    break;
                case PACKET_IN:
                    processOFPacketIn(h, (OFPacketIn) m);
                    break;
                case PORT_STATUS:
                    processOFPortStatus(h, (OFPortStatus) m);
                    break;
                case QUEUE_GET_CONFIG_REPLY:
                    processOFQueueGetConfigReply(h, (OFQueueGetConfigReply) m);
                    break;
                case STATS_REPLY: // multipart_reply in 1.3
                    processOFStatisticsReply(h, (OFStatsReply) m);
                    break;
                case EXPERIMENTER:
                    processOFExperimenter(h, (OFExperimenter) m);
                    break;
                case ROLE_REPLY:
                    processOFRoleReply(h, (OFRoleReply) m);
                    break;
                case GET_ASYNC_REPLY:
                    processOFGetAsyncReply(h, (OFAsyncGetReply) m);
                    break;*/
                // The following messages are sent to switches. The controller
                // should never receive them
                case SET_CONFIG:
                    OFSetConfig m1 = (OFSetConfig) m;
                    processOFSetConfig(h, (OFSetConfig) m1);//implementation of SetConfig status
                    break;
                /*case PACKET_OUT:
                case PORT_MOD:
                case QUEUE_GET_CONFIG_REQUEST:*/
                case BARRIER_REQUEST:
                    //if(m instanceof OFBarrierRequest) {
                        processOFBarrierRequest(h, (OFBarrierRequest) m);
                    break;
                    //};
                    /*else{
                        log.info("To check type {}", m.getClass());
                        processOFGetConfigRequest(h, (OFGetConfigRequest) m);
                    };*/
                    /*else {
                        log.info("To check type {}", m.getClass());
                        processOFSetConfig(h, (OFSetConfig) m);//implementation of SetConfig status
                    }*/
                case GET_CONFIG_REQUEST:
                    //if(m instanceof OFGetConfigRequest) {
                        processOFGetConfigRequest(h, (OFGetConfigRequest) m);
                        break;
                    //};
                    /*else if(m instanceof OFBarrierRequest) {
                        log.info("To check type {}", m.getClass());
                        processOFBarrierRequest(h, (OFBarrierRequest) m);
                    }
                    else {
                        log.info("To check type {}", m.getClass());
                        processOFSetConfig(h, (OFSetConfig) m);//implementation of SetConfig status
                    }*/
                case STATS_REQUEST:// multipart request in 1.3
                    //if(m instanceof OFStatsRequest) {
                        processOFStatisticsRequest(h, (OFStatsRequest) m);
                    break;
                    //};
                    /*else {
                        log.info("To check type {}", m.getClass());
                        processOFSetConfig(h, (OFSetConfig) m);
                    };*/
                case FEATURES_REQUEST:
//                    if(m instanceof OFFeaturesRequest) {

//                    }
                    try {
                        processOFFeaturesRequest(h, (OFFeaturesRequest) m);
                        log.info(m.toString());
                    }catch (Exception e){
                        log.info(e.getMessage());
                    }finally {
                        break;
                    }
                    //};
                    /*else {
                        log.info("To check type {}", m.getClass());
                        processOFStatisticsRequest(h, (OFStatsRequest) m);
                    };*/
                    //break;
                /*case FLOW_MOD:
                case GROUP_MOD:
                case TABLE_MOD:
                case GET_ASYNC_REQUEST:
                case SET_ASYNC:
                case METER_MOD:*/
                case ROLE_REQUEST:
                    try {
                        processOFRoleRequest(h, (OFRoleRequest) m);
                        log.info(m.toString());
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    } finally {
                        break;
                    }
                default:
                    //illegalMessageReceived(h, m);
                    log.info("PNC has received a undefined message {} in state {}", m.getType(), h.state);
                    log.info(m.toString());
                    break;
            }
        }

        void processOFError(OpenflowNorthboundChannelHandler h, OFErrorMsg m)
                throws IOException {
            log.info("Error parent method");
        }

        void processOFRoleRequest(OpenflowNorthboundChannelHandler h, OFRoleRequest m)
                throws IOException {
            log.info("Role request message");
        }

        void processOFHello(OpenflowNorthboundChannelHandler h, OFHello m)
                throws IOException {
            // we only expect hello in the WAIT_HELLO state
            log.info("Hello message");
        }

        void processOFFeaturesRequest(OpenflowNorthboundChannelHandler h, OFFeaturesRequest m)
                throws IOException {
            log.info("features request message");
        }

        void processOFStatisticsRequest(OpenflowNorthboundChannelHandler h, OFStatsRequest m)
                throws IOException {
            log.info("statistics request message");
        }

        void processOFSetConfig(OpenflowNorthboundChannelHandler h, OFSetConfig m)
                throws IOException {
            log.info("set config message");
        }

        void processOFBarrierRequest(OpenflowNorthboundChannelHandler h, OFBarrierRequest m)
                throws IOException {
            log.info("barrier request message");
        }

        void processOFGetConfigRequest(OpenflowNorthboundChannelHandler h, OFGetConfigRequest m)
                throws IOException {
            log.info("get config request message");
        }

        void processOFEchoRequest(OpenflowNorthboundChannelHandler h, OFEchoRequest m)
                throws IOException {
            if (h.ofVersion == null) {
                log.info("No OF version set for {}. Not sending Echo REPLY",
                        h.channel.getRemoteAddress());
                return;
            }
            OFFactory factory = (h.ofVersion == OFVersion.OF_13) ?
                    h.client.getOFMessageFactory13() : h.client.getOFMessageFactory10();
            OFEchoReply reply = factory
                    .buildEchoReply()
                    .setXid(m.getXid())
                    .setData(m.getData())
                    .build();
            h.channel.write(Collections.singletonList(reply));
        }

        void processOFEchoReply(OpenflowNorthboundChannelHandler h, OFEchoReply m)
                throws IOException {
            // Do nothing with EchoReplies !!
        }

        /**
         * We have an OFMessage we didn't expect given the current state and
         * we want to treat this as an error.
         * We currently throw an exception that will terminate the connection
         * However, we could be more forgiving
         * @param h the channel handler that received the message
         * @param m the message
         */
        // needs to be protected because enum members are actually subclasses
        protected void illegalMessageReceived(OpenflowNorthboundChannelHandler h, OFMessage m) {
            log.info("should not receive this message:{} in current state {} !", m.getType() ,h.state);
        }

        /**
         * We have an OFMessage we didn't expect given the current state and
         * we want to ignore the message.
         * @param h the channel handler the received the message
         * @param m the message
         */
        protected void unhandledMessageReceived(OpenflowNorthboundChannelHandler h, OFMessage m) {
            log.info("should not receive this message:{} in current state {} , and ignoring..", m.getType() ,h.state);
        }

        }


    /*
    * @Override
    * Channel Handler methods
    * */
    @Override
    public void channelConnected(ChannelHandlerContext ctx,
                                 ChannelStateEvent e) throws Exception {
        channel = e.getChannel();
        log.info("New connection from {}",
                channel.getRemoteAddress());
        /*
            hack to wait for the switch to tell us what it's
            max version is. This is not spec compliant and should
            be removed as soon as switches behave better.
         */
        //Send Hello to MDSC
        log.info("HLK send Hello to MDSC");
        sendHandshakeHelloMessage(channel);

        setState(ChannelState.WAIT_MDSC_HELLO_RETURN);
        //setState(ChannelState.WAIT_FEATURES_REQUEST);
    }

    /*@Override
    public void channelDisconnected(ChannelHandlerContext ctx,
                                    ChannelStateEvent e) throws Exception {
        log.info("MDSC disconnected callback for sw:{}. Cleaning up ...",
                getSwitchInfoString());
        if (thisdpid != 0) {
            if (!duplicateDpidFound) {
                // if the disconnected switch (on this ChannelHandler)
                // was not one with a duplicate-dpid, it is safe to remove all
                // state for it at the controller. Notice that if the disconnected
                // switch was a duplicate-dpid, calling the method below would clear
                // all state for the original switch (with the same dpid),
                // which we obviously don't want.
                log.info("{}:removal called", getSwitchInfoString());
                if (sw != null) {
                    sw.removeConnectedSwitch();
                }
            } else {
                // A duplicate was disconnected on this ChannelHandler,
                // this is the same switch reconnecting, but the original state was
                // not cleaned up - XXX check liveness of original ChannelHandler
                log.info("{}:duplicate found", getSwitchInfoString());
                duplicateDpidFound = Boolean.FALSE;
            }
        } else {
            log.warn("no dpid in channelHandler registered for "
                    + "disconnected switch {}", getSwitchInfoString());
        }
    }*/

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        if (e.getCause() instanceof ReadTimeoutException) {
            // switch timeout
            log.info("Disconnecting MDSC {} due to read timeout",
                    channel.getRemoteAddress());
            ctx.getChannel().close();
        } else if (e.getCause() instanceof NorthboundHandshakeTimeoutException) {
            log.info("Disconnecting MDSC {}: failed to complete handshake",
                    channel.getRemoteAddress());
            ctx.getChannel().close();
        } else if (e.getCause() instanceof ClosedChannelException) {
            log.info("Channel for MDSC {} already closed", channel.getRemoteAddress());
        } else if (e.getCause() instanceof IOException) {
            if (!e.getCause().getMessage().equals(RESET_BY_PEER) &&
                    !e.getCause().getMessage().equals(BROKEN_PIPE)) {
                log.info("Disconnecting MDSC {} due to IO Error: {}",
                        channel.getRemoteAddress(), e.getCause().getMessage());
                if (log.isDebugEnabled()) {
                    // still print stack trace if debug is enabled
                    log.info("StackTrace for previous Exception: ", e.getCause());
                }
            }
            ctx.getChannel().close();
        } /*else if (e.getCause() instanceof SwitchStateException) {
            log.error("Disconnecting switch {} due to switch state error: {}",
                    getSwitchInfoString(), e.getCause().getMessage());
            if (log.isDebugEnabled()) {
                // still print stack trace if debug is enabled
                log.info("StackTrace for previous Exception: ", e.getCause());
            }
            ctx.getChannel().close();
        }*/ else if (e.getCause() instanceof OFParseError) {
            log.info("Disconnecting MDSC "
                            + channel.getRemoteAddress() +
                            " due to message parse failure",
                    e.getCause());
            ctx.getChannel().close();
        } else if (e.getCause() instanceof RejectedExecutionException) {
            log.info("Could not process message: queue full");
        } else {
            log.info("Error while processing message from MDSC "
                    + channel.getRemoteAddress()
                    + "state " + this.state, e.getCause());
            ctx.getChannel().close();
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        if (e.getMessage() instanceof List) {
            @SuppressWarnings("unchecked")
            List<OFMessage> msglist = (List<OFMessage>) e.getMessage();

            for (OFMessage ofm : msglist) {
                // Do the actual packet processing
                log.info("HLK check OFmessageS type :{}", ofm.getType());
                state.processOFMessage(this, ofm);
            }
        } else {
            log.info("HLK check OFmessage type :{}", ((OFMessage) e.getMessage()).getType());
            state.processOFMessage(this, (OFMessage) e.getMessage());
        }
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
            throws Exception {
        OFFactory factory = (ofVersion == OFVersion.OF_13) ? factory13 : factory10;
        OFMessage m = factory.buildEchoRequest().build();
        log.info("Sending Echo Request on idle channel: {}",
                e.getChannel().getPipeline().getLast().toString());
        e.getChannel().write(Collections.singletonList(m));
        // XXX S some problems here -- echo request has no transaction id, and
        // echo reply is not correlated to the echo request.
    }

    /**
     * Send hello message to the switch using the handshake transactions ids.
     *
     * @throws IOException
     */
    private void sendHandshakeHelloMessage(Channel channel) throws IOException {
        // The OF protocol requires us to start things off by sending the highest
        // version of the protocol supported.

        // bitmap represents OF1.0 (ofp_version=0x01) and OF1.3 (ofp_version=0x04)
        // see Sec. 7.5.1 of the OF1.3.4 spec
        U32 bitmap = U32.ofRaw(0x00000012);
        OFHelloElem hem = factory13.buildHelloElemVersionbitmap()
                .setBitmaps(Collections.singletonList(bitmap))
                .build();
        OFMessage.Builder mb = factory13.buildHello()
                .setXid(this.handshakeTransactionIds--)
                .setElements(Collections.singletonList(hem));
        log.info("Sending OF_13 Hello to {}", channel.getRemoteAddress());
        channel.write(Collections.singletonList(mb.build()));
    }

    /**
     * Send features_reply message to the switch using the handshake transactions ids.
     *
     * @throws IOException
     */
    private void sendFeaturesReplyMessage() throws IOException {
        //For PNC to send featuresReply to MDSC like a switch does
        OFMessage.Builder mb = factory13.buildFeaturesReply()
                .setXid(this.handshakeTransactionIds--)
                .setDatapathId(dpid)
                .setNBuffers(1L);//hlk:set nBuffer for handshakeSetconfig message
        log.info("Sending OF_13 Features_reply to {}", channel.getRemoteAddress());
        channel.write(Collections.singletonList(mb.build()));
    }

    private void sendOFPortDescReply() throws IOException {
        // Send port description for 1.3 PNC
        //TO DO:
        //use for loop to send all PortDescs
        //message structure has been defined in package ofnb
        //capture message valus from core-LinkManager&store.
        log.info("start send OFNBPortDescReply");
        /*OFMessage.Builder mb = factory13
                .buildPortDescStatsReply()
                .setXid(handshakeTransactionIds--);*/ //HLK modified 3.17. This structure can be used to complete handshake with MDSC

        Iterable<Link> links = linkService.getLinks();//获取所有store中光链路

        for(Link link : links) {

            if (link.isDurable() && link.type() == OPTICAL) {
                String remote_node = link.dst().deviceId().uri().toString();
                String local_node = link.src().deviceId().uri().toString();
                String remote_port = link.dst().port().toString();

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

                OFNBPortDescInterface tempportdesc;
                states.add(OFPortState.LIVE);
                configs.add(OFPortConfig.NO_FWD);
                deviceName = link.src().deviceId().uri().toString();

                tempportdesc = ofnbPortDesc.createBuilder()
                        .setConfig(configs)
                        .setState(states)
                        .setName(deviceName)
                        .setDesc(descList).build();

                log.info("To write OFNBPortDescReply");
                channel.write(Collections.singletonList(tempportdesc));//循环发送OFNBPortDesc消息给MDSC

                //do clear
                descList.clear();
                deviceName = "";
                //TO DO:modify specific states and configs for other situation
            }
            else {
                log.info("No optical links in store, please use LINC-OE to construct Network Elements!");
            }
        }
    }

    private void sendOFDescReply() throws IOException {
        // Send description for 1.3 PNC
        log.info("start send DescReply");
        OFMessage.Builder mb = factory13
                .buildDescStatsReply()
                .setXid(handshakeTransactionIds--);
        log.info("To write DescReply");
        channel.write(Collections.singletonList(mb.build()));
    }

    private void sendOFGetConfigReply() throws IOException {
        //For PNC to send configReply to MDSC like a switch does
        log.info("start send OFGConfReply");
        OFMessage.Builder mb = factory13
                .buildGetConfigReply()
                .setMissSendLen(missSendLen)
                .setXid(this.handshakeTransactionIds--);
        log.info("To write OFGConfReply");
        channel.write(Collections.singletonList(mb.build()));
    }

    private void sendOFRoleReplyMessage() throws IOException {
        //Send role reply as Controller_role=master
        log.info("start send OFRoleReply");
        OFMessage.Builder mb = factory13
                .buildRoleReply()
                .setXid(xidTemp)
                .setRole(role);
        log.info("To write OFRoleReply");
        channel.write(Collections.singletonList(mb.build()));
    }

    private void sendOFPortStatusMessage() throws IOException {
        //Send port status for test
        log.info("start send OFPortStatus");
        OFMessage.Builder mb = factory13
                .buildPortStatus()
                .setXid(handshakeTransactionIds--)
                .setReason(OFPortReason.ADD);
        log.info("To write OFPortStatus");
        channel.write(Collections.singletonList(mb.build()));
    }

    public boolean isHandshakeComplete() {
        return this.state.isHandshakeComplete();
    }

    private void setState(ChannelState state) {
        this.state = state;
    }

}