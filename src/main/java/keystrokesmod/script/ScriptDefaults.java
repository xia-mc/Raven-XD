package keystrokesmod.script;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.interfaces.InputSetting;
import keystrokesmod.script.classes.*;
import keystrokesmod.script.packets.serverbound.CPacket;
import keystrokesmod.script.packets.serverbound.PacketHandler;
import keystrokesmod.utility.*;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class ScriptDefaults {
    private static Minecraft mc = Minecraft.getMinecraft();
    private static World world = new World();
    public final Bridge bridge = new Bridge();
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    public static class client {
        public static final String colorSymbol = "§";
        public static boolean allowFlying() {
            return mc.thePlayer.capabilities.allowFlying;
        }

        public static void async(Runnable method) {
            executor.execute(method);
        }

        public static int getSlot() {
            return mc.thePlayer.inventory.currentItem;
        }

        public static int getFPS() {
            return Minecraft.getDebugFPS();
        }

        public static void chat(String message) {
            mc.thePlayer.sendChatMessage(message);
        }

        public static void print(String string) {
            Utils.sendRawMessage(string);
        }

        public static int getFontHeight() {
            return mc.fontRendererObj.FONT_HEIGHT;
        }

        public static void setTimer(float timer) {
            Utils.getTimer().timerSpeed = timer;
        }

        public static int getFontWidth(String text) {
            return mc.fontRendererObj.getStringWidth(text);
        }

        public static boolean isCreative() {
            return mc.thePlayer.capabilities.isCreativeMode;
        }

        public static boolean isFlying() {
            return mc.thePlayer.capabilities.isFlying;
        }

        public static void attack(Entity entity) {
            Utils.attackEntity(entity.entity, true);
        }

        public static boolean isSinglePlayer() {
            return mc.isSingleplayer();
        }

        public static void setFlying(boolean flying) {
            mc.thePlayer.capabilities.isFlying = flying;
        }

        public static void setJump(boolean jump) {
            mc.thePlayer.setJumping(jump);
        }

        public static void jump() {
            mc.thePlayer.jump();
        }

        public static void log(String message) {
            System.out.println(message);
        }

        public static boolean isMouseDown(int button) {
            return Mouse.isButtonDown(button);
        }

        public static boolean isKeyDown(int key) {
            return Keyboard.isKeyDown(key);
        }

        public static void setSneak(boolean sneak) {
            mc.thePlayer.setSneaking(sneak);
        }

        public static Entity getPlayer() {
            if (ScriptManager.localPlayer == null || mc.thePlayer == null || ScriptManager.localPlayer.entity != mc.thePlayer) {
                ScriptManager.localPlayer = new Entity(mc.thePlayer);
            }
            return ScriptManager.localPlayer;
        }

        public static Object[] raycastBlock(double distance) {
            return raycastBlock(distance, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        }

        public static Object[] raycastBlock(double distance, float yaw, float pitch) {
            MovingObjectPosition hit = RotationUtils.rayCast(distance, yaw, pitch);
            if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                return null;
            }
            return new Object[]{Vec3.convert(hit.getBlockPos()), new Vec3(hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord), hit.sideHit.name()};
        }

        public static Object[] raycastEntity(double distance) {
            return raycastEntity(distance, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        }

        public static Object[] raycastEntity(double distance, float yaw, float pitch) {
            MovingObjectPosition hit = RotationUtils.rayCast(distance, yaw, pitch);
            if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                return null;
            }
            return new Object[]{new Entity(hit.entityHit), new Vec3(hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord), mc.thePlayer.getDistanceSqToEntity(hit.entityHit)};
        }

        public static Vec3 getMotion() {
            return new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
        }

        public static void ping() {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
        }

        public static void playSound(String name, float volume, float pitch) {
            mc.thePlayer.playSound(name, volume, pitch);
        }

        public static boolean isMoving() {
            return Utils.isMoving();
        }

        public static boolean isJump() {
            return mc.thePlayer.movementInput.jump;
        }

        public static float getStrafe() {
            return mc.thePlayer.moveStrafing;
        }

        public static void sleep(int ms) {
            try {
                Thread.sleep(ms);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static float getForward() {
            return mc.thePlayer.moveForward;
        }

        public static void closeScreen() {
            mc.thePlayer.closeScreen();
        }

        public static String getScreen() {
            return mc.currentScreen == null ? "" : mc.currentScreen.getClass().getSimpleName();
        }

        public static float[] getRotationsToEntity(Entity entity) {
            return RotationUtils.getRotations(entity.entity);
        }

        public static void sendPacket(CPacket packet) {
            Packet packet1 = PacketHandler.convertCPacket(packet);
            if (packet1 == null) {
                return;
            }
            mc.thePlayer.sendQueue.addToSendQueue(packet1);
        }

        public static void sendPacketNoEvent(CPacket packet) {
            Packet packet1 = PacketHandler.convertCPacket(packet);
            if (packet1 == null) {
                return;
            }
            PacketUtils.sendPacketNoEvent(packet1);
        }

        public static void dropItem(boolean dropStack) {
            mc.thePlayer.dropOneItem(dropStack);
        }

        public static void setMotion(double x, double y, double z) {
            mc.thePlayer.motionX = x;
            mc.thePlayer.motionY = y;
            mc.thePlayer.motionZ = z;
        }

        public static void setSpeed(double speed) {
            Utils.setSpeed(speed);
        }

        public static void setSlot(int slot) {
            mc.thePlayer.inventory.currentItem = slot;
        }

        public static void setForward(float forward) {
            mc.thePlayer.moveForward = forward;
        }

        public static void setStrafe(float strafe) {
            mc.thePlayer.moveStrafing = strafe;
        }

        public static String getServerIP() {
            return mc.getCurrentServerData().serverIP;
        }

        public static int[] getDisplaySize() {
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            return new int[]{scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight()};
        }

        public static float[] getRotationsToBlock(Vec3 position) {
            return RotationUtils.getRotations(new BlockPos(position.x, position.y, position.z));
        }

        public static void setSprinting(boolean sprinting) {
            mc.thePlayer.setSprinting(sprinting);
        }

        public static void swing() {
            mc.thePlayer.swingItem();
        }

        public static World getWorld() {
            return world;
        }

        public static long time() {
            return System.currentTimeMillis();
        }

        public static boolean isFriend(Entity entity) {
            return Utils.isFriended(entity.getName());
        }

        public static boolean isEnemy(Entity entity) {
            return Utils.isEnemy(entity.getName());
        }

        public static class inventory {
            public static void click(int slot, int button, int mode) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot, button, mode, mc.thePlayer);
            }

            public static List<String> getBookContents() {
                if (mc.currentScreen instanceof GuiScreenBook) {
                    try {
                        List<String> contents = new ArrayList<>();
                        int max = Math.min(128 / mc.fontRendererObj.FONT_HEIGHT, ((List<IChatComponent>) Reflection.bookContents.get(mc.currentScreen)).size());
                        for (int line = 0; line < max; ++line) {
                            IChatComponent lineStr = ((List<IChatComponent>) Reflection.bookContents.get(mc.currentScreen)).get(line);
                            contents.add(lineStr.getUnformattedText());
                            Utils.sendMessage(lineStr.getUnformattedText());
                        }
                        if (!contents.isEmpty()) {
                            return contents;
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            public static String getChest() {
                if (mc.thePlayer.openContainer instanceof ContainerChest) {
                    ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                    if (chest == null) {
                        return "";
                    }
                    return chest.getLowerChestInventory().getDisplayName().getUnformattedText();
                }
                return "";
            }

            public static String getContainer() {
                if (mc.currentScreen instanceof GuiContainerCreative) {
                    CreativeTabs creativetabs = CreativeTabs.creativeTabArray[((GuiContainerCreative) mc.currentScreen).getSelectedTabIndex()];
                    if (creativetabs != null) {
                        return I18n.format(creativetabs.getTranslatedTabLabel());
                    }
                }
                else if (mc.currentScreen != null) {
                    try {
                        return ((IInventory) Reflection.containerInventoryPlayer.get(mc.currentScreen.getClass()).get(mc.currentScreen)).getDisplayName().getUnformattedText();
                    } catch (Exception e) {
                    }
                }
                return "";
            }

            public static int getSize() {
                return mc.thePlayer.inventory.getSizeInventory();
            }

            public static int getChestSize() {
                if (mc.thePlayer.openContainer instanceof ContainerChest) {
                    ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                    if (chest == null) {
                        return -1;
                    }
                    return chest.getLowerChestInventory().getSizeInventory();
                }
                return -1;
            }

            public static ItemStack getStackInSlot(int slot) {
                if (mc.thePlayer.inventory.getStackInSlot(slot) == null) {
                    return null;
                }
                return new ItemStack(mc.thePlayer.inventory.getStackInSlot(slot));
            }

            public static ItemStack getStackInChestSlot(int slot) {
                if (mc.thePlayer.openContainer instanceof ContainerChest) {
                    ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                    if (chest.getLowerChestInventory().getStackInSlot(slot) == null) {
                        return null;
                    }
                    return new ItemStack(chest.getLowerChestInventory().getStackInSlot(slot));
                }
                return null;
            }

            public static void open() {
                mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
            }
        }

        public static class keybinds {
            public static boolean isPressed(String key) {
                for (Map.Entry<KeyBinding, String> map : Reflection.keyBindings.entrySet()) {
                    if (map.getValue().equals(key)) {
                        return map.getKey().isKeyDown();
                    }
                }
                return false;
            }

            public static void setPressed(String key, boolean pressed) {
                for (Map.Entry<KeyBinding, String> map : Reflection.keyBindings.entrySet()) {
                    if (map.getValue().equals(key)) {
                        KeyBinding.setKeyBindState(map.getKey().getKeyCode(), pressed);
                    }
                }
            }

            public static int getKeycode(String key) {
                return Keyboard.getKeyIndex(key);
            }
            public static boolean isMouseDown(int mouseButton) {
                return Mouse.isButtonDown(mouseButton);
            }

            public static boolean isKeyDown(int key) {
                return Keyboard.isKeyDown(key);
            }
        }

        public static class render {

            public static void block(Vec3 position, int color, boolean outline, boolean shade) {
                RenderUtils.renderBlock(new BlockPos(position.x, position.y, position.z), color, outline, shade);
            }

            public static void text(String text, float x, float y, int color, boolean shadow) {
                mc.fontRendererObj.drawString(text, x, y, color, shadow);
            }

            public static void text(String text, float x, float y, double scale, int color, boolean shadow) {
                GlStateManager.pushMatrix();
                GL11.glScaled(scale, scale, scale);
                mc.fontRendererObj.drawString(text, x, y, color, shadow);
                GlStateManager.popMatrix();
            }

            public static void player(Entity entity, int color, float partialTicks, boolean outline, boolean shade) {
                net.minecraft.entity.Entity e = entity.entity;
                if (e instanceof EntityLivingBase) {
                    double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
                    double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
                    double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;
                    GlStateManager.pushMatrix();
                    float a = (float) (color >> 24 & 255) / 255.0F;
                    float r = (float) (color >> 16 & 255) / 255.0F;
                    float g = (float) (color >> 8 & 255) / 255.0F;
                    float b = (float) (color & 255) / 255.0F;
                    AxisAlignedBB bbox = e.getEntityBoundingBox().expand(0.1D, 0.1D, 0.1D);
                    AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - e.posX + x, bbox.minY - e.posY + y, bbox.minZ - e.posZ + z, bbox.maxX - e.posX + x, bbox.maxY - e.posY + y, bbox.maxZ - e.posZ + z);
                    GL11.glBlendFunc(770, 771);
                    GL11.glEnable(3042);
                    GL11.glDisable(3553);
                    GL11.glDisable(2929);
                    GL11.glDepthMask(false);
                    GL11.glLineWidth(2.0F);
                    GL11.glColor4f(r, g, b, a);
                    if (outline) {
                        RenderGlobal.drawSelectionBoundingBox(axis);
                    }
                    if (shade) {
                        RenderUtils.drawBoundingBox(axis, r, g, b);
                    }
                    GL11.glEnable(3553);
                    GL11.glEnable(2929);
                    GL11.glDepthMask(true);
                    GL11.glDisable(3042);
                    GlStateManager.popMatrix();
                }
            }

            public static void rect(int startX, int startY, int endX, int endY, int color) {
                Gui.drawRect(startX, startY, endX, endY, color);
            }

            public static void line2D(double startX, double startY, double endX, double endY, float lineWidth, int color) {
                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                RenderUtils.glColor(color);
                GL11.glLineWidth(lineWidth);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex2d(startX, startY);
                GL11.glVertex2d(endX, endY);
                GL11.glEnd();
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glPopMatrix();
            }
        }

        public static class util {
            public static String strip(String string) {
                return Utils.stripColor(string);
            }

            public static double round(double value, int decimals) {
                return Utils.rnd(value, decimals);
            }

            public static int randomInt(int min, int max) {
                return Utils.randomizeInt(min, max);
            }

            public static double randomDouble(double min, double max) {
                return Utils.randomizeDouble(min, max);
            }
        }
    }

    public static class modules {
        private String superName;

        public modules(String superName) {
            this.superName = superName;
        }

        public ModuleManager getModuleManager() {
            return Raven.getModuleManager();
        }

        public ScriptManager getScriptManager() {
            return Raven.scriptManager;
        }

        public Module getModule(String moduleName) {
            boolean found = false;
            for (Module module : getModuleManager().getModules()) {
                if (module.getName().equals(moduleName)) {
                    return module;
                }
            }
            if (!found) {
                for (Module module : getScriptManager().scripts.values()) {
                    if (module.getName().equals(moduleName)) {
                        return module;
                    }
                }
            }
            return null;
        }

        public Module getScript(String name) {
            for (Module module : getScriptManager().scripts.values()) {
                if (module.getName().equals(name)) {
                    return module;
                }
            }
            return null;
        }

        public Setting getSetting(Module module, String settingName) {
            if (module == null) {
                return null;
            }
            for (Setting setting : module.getSettings()) {
                if (setting.getName().equals(settingName)) {
                    return setting;
                }
            }
            return null;
        }

        public void enable(String moduleName) {
            if (getModule(moduleName) == null) {
                return;
            }
            getModule(moduleName).enable();
        }

        public void disable(String moduleName) {
            if (getModule(moduleName) == null) {
                return;
            }
            getModule(moduleName).disable();
        }

        public boolean isEnabled(String moduleName) {
            if (getModule(moduleName) == null) {
                return false;
            }
            return getModule(moduleName).isEnabled();
        }

        public Entity getKillAuraTarget() {
            if (KillAura.target == null) {
                return null;
            }
            return new Entity(KillAura.target);
        }

        public Vec3 getBedAuraPosition() {
            BlockPos blockPos = ModuleManager.bedAura.currentBlock;
            if (ModuleManager.bedAura == null || !ModuleManager.bedAura.isEnabled() || ModuleManager.bedAura.currentBlock == null) {
                return null;
            }
            return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        public float[] getBedAuraProgress() {
            if (ModuleManager.bedAura != null && ModuleManager.bedAura.isEnabled()) {
                return new float[]{(float) ModuleManager.bedAura.breakProgress, ModuleManager.bedAura.vanillaProgress};
            }
            return new float[]{0, 0};
        }

        public void registerButton(String name, boolean defaultValue) {
            getScript(this.superName).registerSetting(new ButtonSetting(name, defaultValue));
        }

        public void registerButton(String name, boolean defaultValue, Supplier<Boolean> visibleCheck) {
            getScript(this.superName).registerSetting(new ButtonSetting(name, defaultValue, visibleCheck));
        }

        public void registerSlider(String name, double defaultValue, double minimum, double maximum, double interval) {
            getScript(this.superName).registerSetting(new SliderSetting(name, defaultValue, minimum, maximum, interval));
        }

        public void registerSlider(String name, double defaultValue, double minimum, double maximum, double interval, Supplier<Boolean> visibleCheck) {
            getScript(this.superName).registerSetting(new SliderSetting(name, defaultValue, minimum, maximum, interval, visibleCheck));
        }

        @Deprecated
        public void registerSlider(String name, int defaultValue, String[] stringArray) {
            getScript(this.superName).registerSetting(new SliderSetting(name, stringArray, defaultValue));
        }

        @Deprecated
        public void registerSlider(String name, int defaultValue, String[] stringArray, Supplier<Boolean> visibleCheck) {
            getScript(this.superName).registerSetting(new SliderSetting(name, stringArray, defaultValue, visibleCheck));
        }

        public void registerMode(String name, int defaultValue, String[] stringArray) {
            getScript(this.superName).registerSetting(new ModeSetting(name, stringArray, defaultValue));
        }

        public void registerMode(String name, int defaultValue, String[] stringArray, Supplier<Boolean> visibleCheck) {
            getScript(this.superName).registerSetting(new ModeSetting(name, stringArray, defaultValue, visibleCheck));
        }

        public boolean getButton(String moduleName, String name) {
            ButtonSetting setting = (ButtonSetting) getSetting(getModule(moduleName), name);
            if (setting == null) {
                return false;
            }
            return setting.isToggled();
        }

        public double getSlider(String moduleName, String name) {
            InputSetting setting = (InputSetting) getSetting(getModule(moduleName), name);
            if (setting == null) {
                return 0;
            }
            return setting.getInput();
        }

        public double getMode(String moduleName, String name) {
            return getSlider(moduleName, name);
        }

        public void setButton(String moduleName, String name, boolean value) {
            ButtonSetting setting = (ButtonSetting) getSetting(getModule(moduleName), name);
            if (setting == null) {
                return;
            }
            setting.setEnabled(value);
        }

        public void setSlider(String moduleName, String name, double value) {
            InputSetting setting = ((InputSetting) getSetting(getModule(moduleName), name));
            if (setting == null) {
                return;
            }
            setting.setValue(value);
        }

        public void setMode(String moduleName, String name, double value) {
            setSlider(moduleName, name, value);
        }
    }
}
