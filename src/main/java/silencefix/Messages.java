package silencefix;

import cn.hutool.core.codec.Base64;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;

import javax.crypto.SecretKey;
import java.util.ArrayList;

public class Messages {
    public static String name = "";
    public static String pass = "";
    public static byte[] payload = new byte[]{0, 0, 0, 11, 0, 0, 0, 69, -27, -120, -104, -20, -95,
            -115, -23, -120, -93, -24, -70, -86, -31, -116, -92, -26, -96, -108, -17, -67, -107, 63,
            -18, -85, -116, -18, -126, -126, -25, -120, -87, -27, -114, -97, -25, -91, -98, -27, -112,
            -81, -27, -118, -88, -27, -100, -112, -20, -114, -99, -18, -89, -94, -29, -99, -110, -17,
            -105, -92, -30, -107, -94, -17, -83, -96, -51, -116, -17, -68, -116, 0, 0};

    public static ByteBuf createHandshake(SecretKey var0) {
        ArrayList<String> data = new ArrayList<>();
        data.add("dsajojidasqewpoicxznkm");
        data.add(Base64.encode(var0.getEncoded()));
        ByteBuf packetBuffer = Unpooled.buffer();
        packetBuffer.capacity(233);
        packetBuffer.writeInt(0);
        data.forEach((s) -> {
            writeString(packetBuffer, s);
        });
        return packetBuffer.copy();
    };

    public static ByteBuf createRegister(String var0, String var1, String var2, String var3, String var4) {
        ArrayList<String> data = new ArrayList<>();
        data.add(var3);
        data.add(var4);
        data.add(var0);
        data.add(var1);
        data.add(var2);
        return Messages.pack(1, data);
    };

    public static ByteBuf createLogin(String var0, String var1, String var2) {
        ArrayList<String> data = new ArrayList<>();
        data.add(var0);
        data.add(var1);
        data.add(var2);
        return Messages.pack(2, data);
    };

    public static ByteBuf createChat(String message) {
        ArrayList<String> data = new ArrayList<>();
        data.add(message);
        return Messages.pack(3, data);
    }

    public static ByteBuf createSetMinecraftProfile(String mcUUID) {
        ArrayList<String> data = new ArrayList<>();
        data.add(mcUUID);
        return Messages.pack(4, data);
    }

    public static ByteBuf createQueryPlayer(String mcUUID, int type) {
        ArrayList<String> data = new ArrayList<>();
        data.add(mcUUID);
        ByteBuf buf = Messages.pack(5, data);
        buf.writeInt(type);
        return buf;
    }

    public static ByteBuf createRequestEmailCode(String qq) {
        ArrayList<String> data = new ArrayList<>();
        data.add(qq);
        return Messages.pack(6, data);
    }

    public static ByteBuf createChatToOther(String name, String message) {
        ArrayList<String> data = new ArrayList<>();
        data.add(name);
        data.add(message);
        return Messages.pack(7, data);
    }

    public static ByteBuf createQueryClients() {
        return Messages.pack(8, new ArrayList<>());
    }

    public static ByteBuf createCommand(String command) {
        ArrayList<String> data = new ArrayList<>();
        data.add(command);
        return Messages.pack(9, data);
    }

    public static ByteBuf createQueryVersion(String clientName) {
        ArrayList<String> data = new ArrayList<>();
        data.add(clientName);
        return Messages.pack(10, data);
    }

    public static ByteBuf createVerify() {
        return Unpooled.copiedBuffer(payload);
    }

    public static void writeString(ByteBuf buffer, String string) {
        byte[] abyte = string.getBytes(Charsets.UTF_8);
        if (abyte.length > 32767) {
            throw new EncoderException("String too big (was " + string.length() + " bytes encoded, max 32767)");
        } else {
            buffer.writeInt(abyte.length);
            buffer.writeBytes(abyte);
        }
    }

    private static ByteBuf pack(int id, ArrayList<String> data) {
        ByteBuf packetBuffer = Unpooled.buffer();
        packetBuffer.capacity(1024);
        packetBuffer.writeInt(id);
        data.forEach((s) -> {
            writeString(packetBuffer, s);
        });
        return packetBuffer.copy();
    }
}
