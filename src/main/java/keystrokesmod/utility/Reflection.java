package keystrokesmod.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.*;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.*;

public class Reflection {
    public static Field button;
    public static Field buttonstate;
    public static Field buttons;
    public static Field leftClickCounter;
    public static Field jumpTicks;
    public static Field rightClickDelayTimerField;
    public static Field curBlockDamageMP;
    public static Field blockHitDelay;
    public static Method clickMouse;
    public static Method rightClickMouse;
    public static Field shaderResourceLocations;
    public static Field useShader;
    public static Field shaderIndex;
    public static Method loadShader;
    public static Method getPlayerInfo;
    public static Field inGround;
    public static Field itemInUseCount;
    public static Field S08PacketPlayerPosLookYaw;
    public static Field S08PacketPlayerPosLookPitch;
    public static Field C02PacketUseEntityEntityId;
    public static Field bookContents;
    public static HashMap<Class, Field> containerInventoryPlayer = new HashMap<>();
    private static List<Class> containerClasses = new ArrayList<>();
    public static boolean sendMessage = false;
    public static Map<KeyBinding, String> keyBindings = new HashMap<>();

    public static void getFields() {
        try {

            containerClasses.add(GuiFurnace.class);
            containerClasses.add(GuiBrewingStand.class);
            containerClasses.add(GuiEnchantment.class);
            containerClasses.add(ContainerHopper.class);
            containerClasses.add(GuiDispenser.class);
            containerClasses.add(ContainerWorkbench.class);
            containerClasses.add(ContainerMerchant.class);
            containerClasses.add(ContainerHorseInventory.class);

            button = MouseEvent.class.getDeclaredField("button");
            buttonstate = MouseEvent.class.getDeclaredField("buttonstate");
            buttons = Mouse.class.getDeclaredField("buttons");

            leftClickCounter = ReflectionHelper.findField(Minecraft.class, "field_71429_W", "leftClickCounter");

            if (leftClickCounter != null) {
                leftClickCounter.setAccessible(true);
            }

            jumpTicks = ReflectionHelper.findField(EntityLivingBase.class, "field_70773_bE", "jumpTicks");

            if (jumpTicks != null) {
                jumpTicks.setAccessible(true);
            }

            rightClickDelayTimerField = ReflectionHelper.findField(Minecraft.class, "field_71467_ac", "rightClickDelayTimer");

            if (rightClickDelayTimerField != null) {
                rightClickDelayTimerField.setAccessible(true);
            }

            curBlockDamageMP = ReflectionHelper.findField(PlayerControllerMP.class, "field_78770_f", "curBlockDamageMP"); // fastmine and mining related stuff
            if (curBlockDamageMP != null) {
                curBlockDamageMP.setAccessible(true);
            }

            blockHitDelay = ReflectionHelper.findField(PlayerControllerMP.class, "field_78781_i", "blockHitDelay");
            if (blockHitDelay != null) {
                blockHitDelay.setAccessible(true);
            }

            shaderResourceLocations = ReflectionHelper.findField(EntityRenderer.class, "shaderResourceLocations", "field_147712_ad");
            if (shaderResourceLocations != null) {
                shaderResourceLocations.setAccessible(true);
            }

            useShader = ReflectionHelper.findField(EntityRenderer.class, "useShader", "field_175083_ad");
            if (useShader != null) {
                useShader.setAccessible(true);
            }

            shaderIndex = ReflectionHelper.findField(EntityRenderer.class, "field_147713_ae", "shaderIndex"); // for shaders
            if (shaderIndex != null) {
                shaderIndex.setAccessible(true);
            }

            inGround = ReflectionHelper.findField(EntityArrow.class, "field_70254_i", "inGround"); // for indicators
            if (inGround != null) {
                inGround.setAccessible(true);
            }

            itemInUseCount = ReflectionHelper.findField(EntityPlayer.class, "field_71072_f", "itemInUseCount"); // for fake block
            if (itemInUseCount != null) {
                itemInUseCount.setAccessible(true);
            }

            S08PacketPlayerPosLookYaw = ReflectionHelper.findField(S08PacketPlayerPosLook.class, "field_148936_d", "yaw");
            if (S08PacketPlayerPosLookYaw != null) {
                S08PacketPlayerPosLookYaw.setAccessible(true);
            }

            S08PacketPlayerPosLookPitch = ReflectionHelper.findField(S08PacketPlayerPosLook.class, "field_148937_e", "pitch");
            if (S08PacketPlayerPosLookPitch != null) {
                S08PacketPlayerPosLookPitch.setAccessible(true);
            }

            C02PacketUseEntityEntityId = ReflectionHelper.findField(C02PacketUseEntity.class, "entityId", "field_149567_a");
            if (C02PacketUseEntityEntityId != null) {
                C02PacketUseEntityEntityId.setAccessible(true);
            }

            bookContents = ReflectionHelper.findField(GuiScreenBook.class, "field_175386_A");
            if (bookContents != null) {
                bookContents.setAccessible(true);
            }

            for (Class clazz : containerClasses) {
                for (Field field : clazz.getDeclaredFields()) {
                    addToMap(clazz, field);
                }
            }

        } catch (Exception var2) {
            System.out.println("There was an error, relaunch the game.");
            var2.printStackTrace();
            sendMessage = true;
        }
    }

    public static void setKeyBindings() {
        for (KeyBinding keyBinding : Minecraft.getMinecraft().gameSettings.keyBindings) {
            keyBindings.put(keyBinding, keyBinding.getKeyDescription().substring(4));
        }
    }

    public static void getMethods() {
        try {
            try {
                rightClickMouse = Minecraft.getMinecraft().getClass().getDeclaredMethod("func_147121_ag");
            } catch (NoSuchMethodException var4) {
                try {
                    rightClickMouse = Minecraft.getMinecraft().getClass().getDeclaredMethod("rightClickMouse");
                } catch (NoSuchMethodException var3) {
                }
            }

            if (rightClickMouse != null) {
                rightClickMouse.setAccessible(true);
            }

            loadShader = ReflectionHelper.findMethod(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, new String[]{"func_175069_a", "loadShader"}, ResourceLocation.class);

            if (loadShader != null) {
                loadShader.setAccessible(true);
            }

            try {
                clickMouse = Minecraft.getMinecraft().getClass().getDeclaredMethod("clickMouse");
            } catch (NoSuchMethodException var4) {
                try {
                    clickMouse = Minecraft.getMinecraft().getClass().getDeclaredMethod("func_147116_af");
                } catch (NoSuchMethodException var3) {
                }
            }

            if (clickMouse != null) {
                clickMouse.setAccessible(true);
            }

            try {
                getPlayerInfo = AbstractClientPlayer.class.getDeclaredMethod("getPlayerInfo");
            } catch (NoSuchMethodException var4) {
                try {
                    getPlayerInfo =

                            AbstractClientPlayer.class.getDeclaredMethod("func_175155_b");
                } catch (NoSuchMethodException var3) {
                }
            }

            if (getPlayerInfo != null) {
                getPlayerInfo.setAccessible(true);
            }
        }
        catch (Exception e) {
            System.out.println("There was an error, relaunch the game.");
            e.printStackTrace();
            sendMessage = true;
        }
    }

    public static void setButton(int t, boolean s) {
        if (button != null && buttonstate != null && buttons != null) {
            MouseEvent m = new MouseEvent();

            try {
                button.setAccessible(true);
                button.set(m, t);
                buttonstate.setAccessible(true);
                buttonstate.set(m, s);
                MinecraftForge.EVENT_BUS.post(m);
                buttons.setAccessible(true);
                ByteBuffer bf = (ByteBuffer) buttons.get(null);
                buttons.setAccessible(false);
                bf.put(t, (byte) (s ? 1 : 0));
            } catch (IllegalAccessException var4) {
            }
        }
    }

    private static void addToMap(Class clazz, Field field) {
        if (field == null || field.getType() != IInventory.class) {
            return;
        }
        field = ReflectionHelper.findField(clazz, field.getName());
        field.setAccessible(true);
        containerInventoryPlayer.put(clazz, field);
    }

    public static void rightClick() {
        try {
            Reflection.rightClickMouse.invoke(Minecraft.getMinecraft());
        }
        catch (InvocationTargetException ex) {}
        catch (IllegalAccessException ex2) {}
    }

    public static void clickMouse() {
        if (clickMouse != null) {
            try {
                clickMouse.invoke(Minecraft.getMinecraft());
            }
            catch (InvocationTargetException ex) {}
            catch (IllegalAccessException ex2) {}
        }
    }

    public static boolean setBlocking(boolean blocking) {
        try {
            itemInUseCount.set(Minecraft.getMinecraft().thePlayer, blocking ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendMessage("§cFailed to set block state client-side.");
            return false;
        }
        return blocking;
    }
}
