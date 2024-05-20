package keystrokesmod.script;

import keystrokesmod.Raven;
import keystrokesmod.event.*;
import keystrokesmod.module.Module;
import keystrokesmod.script.classes.Entity;
import keystrokesmod.script.classes.PlayerState;
import keystrokesmod.script.packets.clientbound.SPacket;
import keystrokesmod.script.packets.serverbound.CPacket;
import keystrokesmod.script.packets.serverbound.PacketHandler;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ScriptEvents {
    public Module module;

    public ScriptEvents(Module module) {
        this.module = module;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        if (e.type == 2 || !Utils.nullCheck()) {
            return;
        }
        final String r = Utils.stripColor(e.message.getUnformattedText());
        if (r.isEmpty()) {
            return;
        }
        if (Raven.scriptManager.invokeBoolean("onChat", module, e.message.getUnformattedText()) == 0) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (e.isCanceled() || e.getPacket() == null) {
            return;
        }
        if (e.getPacket().getClass().getSimpleName().startsWith("S")) {
            return;
        }
        CPacket a = PacketHandler.convertServerBound(e.getPacket());
        if (a != null && Raven.scriptManager.invokeBoolean("onPacketSent", module, a) == 0) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (e.isCanceled() || e.getPacket() == null) {
            return;
        }
        SPacket a = PacketHandler.convertClientBound(e.getPacket());
        if (a != null && Raven.scriptManager.invokeBoolean("onPacketReceived", module, a) == 0) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }
        Raven.scriptManager.invoke("onRenderWorld", module, e.partialTicks);
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        Raven.scriptManager.invoke("onPreUpdate", module);
    }

    @SubscribeEvent
    public void onPostUpdate(PostUpdateEvent e) {
        Raven.scriptManager.invoke("onPostUpdate", module);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }
        Raven.scriptManager.invoke("onRenderTick", module, e.renderTickTime);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) {
        PlayerState playerState = new PlayerState(e);
        Raven.scriptManager.invoke("onPreMotion", module, playerState);
        if (e.isEquals(playerState)) {
            return;
        }
        if (e.getYaw() != playerState.yaw) {
            e.setYaw(playerState.yaw);
        }
        e.setPitch(playerState.pitch);
        e.setPosX(playerState.x);
        e.setPosY(playerState.y);
        e.setPosZ(playerState.z);
        e.setOnGround(playerState.onGround);
        e.setSprinting(playerState.isSprinting);
        e.setSneaking(playerState.isSneaking);
    }

    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent e) {
        if (e.entity == null) {
            return;
        }
        if (e.entity == Minecraft.getMinecraft().thePlayer) {
            Raven.scriptManager.invoke("onWorldJoin", module, ScriptDefaults.client.getPlayer());
            ScriptManager.localPlayer = new Entity(Minecraft.getMinecraft().thePlayer);
            return;
        }
        Raven.scriptManager.invoke("onWorldJoin", module, new Entity(e.entity));
    }

    @SubscribeEvent
    public void onPostInput(PostInputEvent e) {
        Raven.scriptManager.invoke("onPostPlayerInput", module);
    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent e) {
        Raven.scriptManager.invoke("onPostMotion", module);
    }

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (Raven.scriptManager.invokeBoolean("onMouse", module, e.button, e.buttonstate) == 0) {
            e.setCanceled(true);
        }
    }
}
