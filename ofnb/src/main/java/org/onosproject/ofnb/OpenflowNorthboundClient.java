package org.onosproject.ofnb;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

//import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.DeviceStore;
import org.onosproject.net.driver.DefaultDriverData;
import org.onosproject.net.driver.DefaultDriverHandler;
import org.onosproject.net.driver.Driver;
import org.onosproject.net.driver.DriverService;
import org.onosproject.net.link.LinkService;
import org.projectfloodlight.openflow.protocol.OFDescStatsReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.groupedThreads;

public class OpenflowNorthboundClient {

	private final Logger log = LoggerFactory.getLogger(OpenflowNorthboundClient.class);
    private ChannelGroup cg;
    private NioClientSocketChannelFactory execFactory;

    protected static final OFFactory FACTORY13 = OFFactories.getFactory(OFVersion.OF_13);
    protected static final OFFactory FACTORY10 = OFFactories.getFactory(OFVersion.OF_10);

    public OFFactory getOFMessageFactory10() {
        return FACTORY10;
    }
    public OFFactory getOFMessageFactory13() {
        return FACTORY13;
    }

    protected DeviceService deviceService;
    protected DeviceStore store;
    protected LinkService linkService;

    public OpenflowNorthboundClient(DeviceService deviceService, DeviceStore store, LinkService linkService) {
        this.deviceService = deviceService;
        this.store = store;
        this.linkService = linkService;
    }

    public void run() {
        try {
			log.info("HLK into client run");
            String host = "192.168.1.50";
            int port = 6633;

            //HLK 12.9:Number of workerThreads is ignored, need to be check
            ClientBootstrap bootstrap = createClientBootStrap();
            bootstrap.setPipelineFactory(new OpenflowNorthboundPipelineFactory(this, null, deviceService, store, linkService));
			log.info("HLK bootstrap start");
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
			log.info("HLK connected");
            //need to be check bellow
            future.getChannel().getCloseFuture().awaitUninterruptibly();
            bootstrap.releaseExternalResources();
            //future.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
            log.info("===========");
            throw new RuntimeException(e);
        }
    }

    public void stop() {

    }

    private ClientBootstrap createClientBootStrap() {
            execFactory = new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/of", "clientBoss-%d")),
                    Executors.newCachedThreadPool(groupedThreads("onos/of", "clientWorker-%d")));
            return new ClientBootstrap(execFactory);
    }
}
