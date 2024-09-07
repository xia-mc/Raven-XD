package keystrokesmod.utility.gapple;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static keystrokesmod.Raven.mc;

public class GappleUtlis2 {
    private static final Map<Class<?>, Consumer<Packet<?>>> cancelAction = new HashMap();
    private static final Map<Class<?>, Consumer<Packet<?>>> releaseAction = new HashMap();
    public static boolean storing = false;
    private static final List<Class<?>> blackList = new ArrayList();
    private static Map<Class<?>, Predicate<Packet<?>>> addReturnMap = new HashMap();
    private static final Map<Class<?>, Predicate<Packet<?>>> cancelPacketMap = new HashMap();
    private static final Map<Class<?>, Predicate<Packet<?>>> releaseMap = new HashMap();
    private static final List<Class<?>> whitList = new ArrayList();
    public static LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue();
    public static boolean noEvt = false;

    static {
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onTickEvent(TickEvent event) {
                if (storing) {
                    packets.add(new S00PacketKeepAlive());
                }
                if (mc.getNetHandler() == null) {
                    GappleUtlis2.Method8();
                }
            }
        });
    }

    public static void Method1(Packet<?> packet, boolean event) {
        if (event) {
            SendPacketEvent packetSendEvent = new SendPacketEvent(packet);
            MinecraftForge.EVENT_BUS.register(packetSendEvent);
            if (packetSendEvent.isCanceled()) {
                return;
            }
        }
        noEvt = true;
        PacketUtils.sendPacketNoEvent(packet);
        noEvt = false;
    }

    public static void Method2(Class<?> ... fliterPackets) {
        if (storing) {
            return;
        }
        Arrays.asList(fliterPackets).forEach(e -> {
            blackList.add(e);
            cancelPacketMap.put(e, f -> true);
        });
        storing = true;
    }

    public static void Method3(Class<?> clazz, Consumer<Packet<?>> packetConsumer) {
        boolean isIN = false;
        for (Class<?> classes : cancelAction.keySet()) {
            if (classes != clazz) continue;
            isIN = true;
            break;
        }
        if (isIN) {
            cancelAction.replace(clazz, packetConsumer);
        } else {
            cancelAction.put(clazz, packetConsumer);
        }
    }

    public static void Method4(Class<?> clazz, Predicate<Packet<?>> predicate) {
        boolean isIN = false;
        for (Class<?> classes : cancelPacketMap.keySet()) {
            if (classes != clazz) continue;
            isIN = true;
            break;
        }
        if (isIN) {
            cancelPacketMap.replace(clazz, predicate);
        } else {
            cancelPacketMap.put(clazz, predicate);
        }
    }

    public static void Method5() {
        blackList.clear();
    }

    public static void setReleaseAction(Class<?> clazz, Consumer<Packet<?>> packetConsumer) {
        boolean isIN = false;
        for (Class<?> classes : releaseAction.keySet()) {
            if (classes != clazz) continue;
            isIN = true;
            break;
        }
        if (isIN) {
            releaseAction.replace(clazz, packetConsumer);
        } else {
            releaseAction.put(clazz, packetConsumer);
        }
    }

    public static void Method6(Class<?> clazz, Predicate<Packet<?>> predicate) {
        boolean isIN = false;
        for (Class<?> classes : releaseMap.keySet()) {
            if (classes != clazz) continue;
            isIN = true;
            break;
        }
        if (isIN) {
            releaseMap.replace(clazz, predicate);
        } else {
            releaseMap.put(clazz, predicate);
        }
    }

    public static void Method7(int sendPackets, boolean noEvent) {
        GappleUtlis2.Method7(sendPackets, noEvent, false);
    }

    public static void Method7(boolean sendOneTick) {
        GappleUtlis2.Method7(packets.size(), true, sendOneTick);
    }

    public static void Method7() {
        GappleUtlis2.Method7(packets.size(), true);
    }

    public static void Method7(int sendPackets, boolean noEvent, boolean sendOneTick) {
        int sends = 0;
        try {
            block2: while (!packets.isEmpty()) {
                Packet<?> packet = packets.take();
                if (packet instanceof S00PacketKeepAlive) {
                    if (!sendOneTick) continue;
                } else {
                    for (Map.Entry<Class<?>, Predicate<Packet<?>>> entries : releaseMap.entrySet()) {
                        if (!entries.getKey().isAssignableFrom(packet.getClass()) || !entries.getValue().test(packet)) continue;
                        continue block2;
                    }
                    releaseAction.forEach((key, value) -> {
                        if (key.isAssignableFrom(packet.getClass())) {
                            value.accept(packet);
                        }
                    });
                    ++sends;
                    if (noEvent) {
                        noEvt = true;
                        PacketUtils.sendPacketNoEvent(packet);
                        noEvt = false;
                    } else {
                        noEvt = true;
                        mc.getNetHandler().addToSendQueue(packet);
                        noEvt = false;
                    }
                    if (sends < sendPackets) continue;
                }
                break;
            }
        } catch (Exception e) {
            Utils.sendMessage(e.getMessage());
        }
    }

    public static void Method8() {
        storing = false;
        noEvt = false;
        GappleUtlis2.Method7();
        blackList.clear();
        cancelPacketMap.clear();
        cancelAction.clear();
        releaseAction.clear();
        releaseMap.clear();
        whitList.clear();
        packets.clear();
    }

    public static boolean Method9(Packet<?> packet) {
        if (storing && !noEvt) {
            cancelAction.forEach((aClass, packetConsumer) -> {
                if (aClass.isAssignableFrom(packet.getClass())) {
                    packetConsumer.accept(packet);
                }
            });
            for (Class<?> clazz : blackList) {
                if (!clazz.isAssignableFrom(packet.getClass())) continue;
                return true;
            }
            for (Map.Entry entry : cancelPacketMap.entrySet()) {
                if (!((Class)entry.getKey()).isAssignableFrom(packet.getClass()) || !((Predicate)entry.getValue()).test(packet)) continue;
                return true;
            }
            if (!whitList.isEmpty() && !whitList.contains(packet.getClass())) {
                return true;
            }
            boolean needAdd = true;
            for (Map.Entry<Class<?>, Predicate<Packet<?>>> entries : addReturnMap.entrySet()) {
                if (!entries.getKey().isAssignableFrom(packet.getClass()) || !entries.getValue().test(packet)) continue;
                needAdd = false;
            }
            if (needAdd) {
                packets.add(packet);
            }
            return false;
        }
        return true;
    }

    public static void setAddReturnMap(Map<Class<?>, Predicate<Packet<?>>> addReturnMap) {
        GappleUtlis2.addReturnMap = addReturnMap;
    }
}

