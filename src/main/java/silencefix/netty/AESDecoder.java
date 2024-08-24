package silencefix.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.List;

public class AESDecoder
        extends MessageToMessageDecoder<ByteBuf> {
    private final Key key;

    public AESDecoder(Key key) {
        this.key = key;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(2, this.key);
        int rawDataSize = in.readInt();
        ByteBuffer decryptBuffer = ByteBuffer.allocate(cipher.getOutputSize(in.readableBytes()));
        cipher.doFinal(in.nioBuffer(), decryptBuffer);
        decryptBuffer.position(0);
        out.add(Unpooled.wrappedBuffer((ByteBuffer)decryptBuffer).slice(0, rawDataSize));
    }
}