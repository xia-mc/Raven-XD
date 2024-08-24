package silencefix.netty;

import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class StringToJsonDecoder
        extends MessageToMessageDecoder<String> {
    private final JsonParser parser = new JsonParser();

    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) {
        out.add(this.parser.parse(msg));
    }
}
