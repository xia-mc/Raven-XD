package silencefix.netty;

import cn.hutool.core.codec.Base64;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

public class RSAEncoder
        extends MessageToMessageEncoder<ByteBuf> {
    private static final Key RSA_PUBLIC_KEY;

    protected void encode(ChannelHandlerContext ctx, @NotNull ByteBuf msg, @NotNull List<Object> out) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(1, RSA_PUBLIC_KEY);
        ByteBuffer encryptBuffer = ByteBuffer.allocate(4 + cipher.getOutputSize(msg.readableBytes()));
        encryptBuffer.putInt(msg.readableBytes());
        cipher.doFinal(msg.nioBuffer(), encryptBuffer);
        encryptBuffer.position(0);
        out.add(Unpooled.wrappedBuffer(encryptBuffer));
    }

    static {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(Base64.decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA61PQHqmFRPHrGfWfg/E5J52NfR2MEs+PwNr0QrhFkukgd+cnaO5QaYrT8wwrVRToSskF+l+IbNnvP/+puRYl6ZAGThip5VCZcPVn3bQV+bLYSeY9HEqQNZ9UbAhP66ko0fICVw1uNkgqho+xEs6mQKBt1w9M6EL/xrBnBChJE8D+uxfp8ZltywSI2QOgDbVUaeR307eTohlAdmVyDVodblF5b80wI8JUBqWPwQa2l9m6RaPd2eZapdKq7SoSuvG0qiUWsPnRBfOYqhn2R0gChOsoBrP5qbtiiFRpOYp0vUdojGrtcvmbpZB56VYmeDVYgsgPBpiP4hZ9ceyUqBqzUwIDAQAB"));
            RSA_PUBLIC_KEY = keyFactory.generatePublic(encodedKeySpec);
        }
        catch (Exception e) {
            throw new RuntimeException("Can't init key file", e);
        }
    }
}
