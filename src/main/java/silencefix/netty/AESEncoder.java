package silencefix.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.List;

public class AESEncoder
        extends MessageToMessageEncoder<ByteBuf> {
    private final Key key;

    public AESEncoder(Key key) {
        this.key = key;
    }

    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, this.key);
        ByteBuffer encryptBuffer = ByteBuffer.allocate(4 + cipher.getOutputSize(msg.readableBytes()));
        encryptBuffer.putInt(msg.readableBytes());
        cipher.doFinal(msg.nioBuffer(), encryptBuffer);
        encryptBuffer.position(0);
        out.add(Unpooled.wrappedBuffer((ByteBuffer)encryptBuffer));
    }
}
