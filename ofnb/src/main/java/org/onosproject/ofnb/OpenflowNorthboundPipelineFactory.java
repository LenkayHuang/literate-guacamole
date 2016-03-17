package org.onosproject.ofnb;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.DeviceStore;
import org.onosproject.net.link.LinkService;

import java.util.concurrent.ThreadPoolExecutor;

public class OpenflowNorthboundPipelineFactory implements
        ChannelPipelineFactory, ExternalResourceReleasable {

    protected OpenflowNorthboundClient client;
    protected ThreadPoolExecutor pipelineExecutor;
    protected Timer timer;
    protected IdleStateHandler idleHandler;
    protected ReadTimeoutHandler readTimeoutHandler;

    protected DeviceService deviceService;
    protected DeviceStore store;
    protected LinkService linkService;

    public OpenflowNorthboundPipelineFactory(OpenflowNorthboundClient client, ThreadPoolExecutor pipelineExecutor, DeviceService deviceService, DeviceStore store, LinkService linkService) {
        super();
        this.client = client;
        this.pipelineExecutor = pipelineExecutor;
        this.timer = new HashedWheelTimer();
        this.idleHandler = new IdleStateHandler(timer, 20, 25, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(timer, 30);
        this.deviceService = deviceService;
        this.store = store;
        this.linkService = linkService;
    }

    public ChannelPipeline getPipeline() throws Exception {
        OpenflowNorthboundChannelHandler handler = new OpenflowNorthboundChannelHandler(client, deviceService, store, linkService);
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("decoder", new OpenflowNorthboundMessageDecoder());
        pipeline.addLast("encoder", new OpenflowNorthboundMessageEncoder());
        pipeline.addLast("idle", idleHandler);
        pipeline.addLast("timeout", readTimeoutHandler);
        // XXX S ONOS: was 15 increased it to fix Issue #296
        pipeline.addLast("handshaketimeout",
                new NorthboundHandshakeTimeoutHandler(handler, timer, 60));
        if (pipelineExecutor != null) {
            pipeline.addLast("pipelineExecutor",
                    new ExecutionHandler(pipelineExecutor));
        }
        pipeline.addLast("handler", handler);

        return pipeline;
    }

    @Override
    public void releaseExternalResources() {
        timer.stop();
    }
}
