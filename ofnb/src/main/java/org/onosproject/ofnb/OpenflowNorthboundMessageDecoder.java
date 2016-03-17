package org.onosproject.ofnb;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;

/**
 * Decode an openflow message from a Channel, for use in a netty pipeline.
 */
public class OpenflowNorthboundMessageDecoder extends FrameDecoder {

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
                            ChannelBuffer buffer) throws Exception {
        if (!channel.isConnected()) {
            // In testing, I see decode being called AFTER decode last.
            // This check avoids that from reading corrupted frames
            return null;
        }

        // Note that a single call to decode results in reading a single
        // OFMessage from the channel buffer, which is passed on to, and processed
        // by, the controller (in OFChannelHandler).
        // This is different from earlier behavior (with the original openflowj),
        // where we parsed all the messages in the buffer, before passing on
        // a list of the parsed messages to the controller.
        // The performance *may or may not* not be as good as before.
        OFMessageReader<OFMessage> reader = OFFactories.getGenericReader();
        OFMessage message = reader.readFrom(buffer);

        return message;
    }

}