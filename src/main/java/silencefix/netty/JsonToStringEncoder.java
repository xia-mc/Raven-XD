package silencefix.netty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JsonToStringEncoder
        extends MessageToMessageEncoder<JsonElement> {
    private static final Gson GSON = new GsonBuilder().create();

    protected void encode(ChannelHandlerContext ctx, JsonElement msg, @NotNull List<Object> out) {
        out.add(GSON.toJson(msg));
    }
}
