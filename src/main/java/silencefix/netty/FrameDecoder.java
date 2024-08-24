package silencefix.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class FrameDecoder
        extends ByteToMessageDecoder {
    private int length = -1;

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (this.length == -1) {
            if (in.readableBytes() >= 4) {
                this.length = in.readInt();
            }
        } else if (in.readableBytes() >= this.length) {
            out.add(in.readSlice(this.length).retain());
            this.length = -1;
        }
    }
}
