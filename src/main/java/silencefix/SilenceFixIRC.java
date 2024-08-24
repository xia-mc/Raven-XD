package silencefix;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import keystrokesmod.Raven;
import keystrokesmod.module.impl.client.Notifications;
import lombok.Getter;
import net.minecraft.util.EnumChatFormatting;
import silencefix.netty.FrameDecoder;
import silencefix.netty.FrameEncoder;
import silencefix.netty.RSAEncoder;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SilenceFixIRC {
    public static SilenceFixIRC Instance;
    public IRCUser ircUser;
    public int reconnectTimes;
    public final Map<UUID, IRCUser> ircUserMap = new HashMap<>();
    @Getter
    public final Map<String, BaseCallback<?>> callbackMap = new HashMap<>();
    EventLoopGroup workerGroup;
    @Getter
    Channel channel;
    SecretKey aesKey;
    public String hwid;

    public static void init() {
        Instance = new SilenceFixIRC();
        Instance.callbackMap.put("log", new silencefix.callback.LogCallback());
        Instance.callbackMap.put("auth_callback", new silencefix.callback.AuthCallback());
        Instance.callbackMap.put("exception_callback", new silencefix.callback.ExceptionCallback());
    }

    public void connect() throws Exception {
        hwid = generateHardwareId();
        final Bootstrap bootstrap = new Bootstrap();
        workerGroup = new NioEventLoopGroup();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(workerGroup);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) {
                channel.pipeline().addLast("frame_decoder", new FrameDecoder());
                channel.pipeline().addLast("frame_encoder", new FrameEncoder());
                channel.pipeline().addLast("rsa_encoder", new RSAEncoder());
                channel.pipeline().addLast(new MessageHandler(SilenceFixIRC.this));
                SilenceFixIRC.this.channel = channel;
            }
        });
        bootstrap.connect("cn-wx.kuangmoge.xyz", 41201).sync().addListener(future ->
                Raven.getExecutor().execute(() -> {
                    if (future.isSuccess()) {
                        this.channel = ((ChannelFuture) future).channel();
                        try {
                            this.aesKey = SilenceFixIRC.genAESKey();
                            this.sendPacket(Messages.createHandshake(this.aesKey));
                            this.sendPacket(Messages.createVerify());
                        }
                        catch (Exception e) {
                            this.channel.close();
                            ExceptionCallback callback = (ExceptionCallback) this.callbackMap.get("exception_callback");
                            if (callback != null) {
                                if (future.cause() != null) {
                                    callback.callback(future.cause());
                                } else {
                                    callback.callback(e);
                                }
                            }
                        }
                    } else {
                        ExceptionCallback callback = (ExceptionCallback)this.callbackMap.get("exception_callback");
                        if (callback != null) {
                            callback.callback(future.cause());
                        }
                    }}
                )
        );
    }

    public void shutdown() {
        if (this.channel != null && this.channel.isOpen()) {
            this.channel.close();
        }
        if (this.workerGroup != null && !this.workerGroup.isShutdown()) {
            this.workerGroup.shutdownGracefully();
        }
    }

    public void sendPacket(Object data) {
        Channel channel = this.channel;
        if (channel != null && channel.isOpen()) {
            if (channel.eventLoop().inEventLoop()) {
                channel.writeAndFlush(data).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            } else {
                channel.eventLoop().submit(() -> this.sendPacket(data));
            }
        } else {
            Notifications.sendNotification(Notifications.NotificationTypes.WARN, "silencefix irc not connected");
        }
    }

    private static SecretKey genAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // for example
        return keyGen.generateKey();
    };

    public static String generateHardwareId() throws Exception {
        return "sb";
    }

    public String getUserTag(UUID uuid) {
        try {
            IRCUser userName = SilenceFixIRC.Instance.ircUserMap.get(uuid);
            String[] rank = new String[]{"免费", "付费", "管理"};
            return EnumChatFormatting.BLUE + "(SF|" + rank[userName.level.getPriority()] + "|" + userName.name + ") " + EnumChatFormatting.RESET;
        } catch (Exception ignored) {
            return "";
        }
    }

    public static interface BaseCallback<T> {
        public void callback(T t);
    }

    public static interface ExceptionCallback extends BaseCallback<Throwable> {}

    public static interface LogCallback extends BaseCallback<String> {}

    public static interface AuthCallback extends BaseCallback<String> {}
}
