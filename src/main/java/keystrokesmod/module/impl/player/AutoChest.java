package keystrokesmod.module.impl.player;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.RenderContainerEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AutoChest extends Module {
    private static final Set<Item> ITEMS = new HashSet<>(Arrays.asList(Items.iron_ingot, Items.gold_ingot, Items.diamond, Items.emerald));

    private final SliderSetting minDelay;
    private final SliderSetting maxDelay;
    private final ButtonSetting silent;
    private final SliderSetting debug;

    private int clickDelay = 0;

    public AutoChest() {
        super("AutoChest", category.experimental, "Auto put resources into chest. (Hypixel)");
        this.registerSetting(minDelay = new SliderSetting("Min delay", 50, 0, 500, 10));
        this.registerSetting(maxDelay = new SliderSetting("Max delay", 50, 0, 500, 10));
        this.registerSetting(silent = new ButtonSetting("Silent", false));
        this.registerSetting(debug = new SliderSetting("Debug", 0, -48, 48, 1));
    }

    @SubscribeEvent
    public void onRenderContainer(RenderContainerEvent event) {
        if (silent.isToggled())
            event.setCanceled(true);
    }

    @Override
    public void onEnable() {
        clickDelay = 0;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!(mc.currentScreen instanceof GuiContainer)) {
            clickDelay = Utils.randomizeInt(minDelay.getInput() / 50, maxDelay.getInput() / 50);
            return;
        }

        if (clickDelay > 0) {
            clickDelay--;
            return;
        }

        ItemStack[] inventory = mc.thePlayer.inventory.mainInventory;
        for (int i = 0; i < inventory.length; i++) {
            ItemStack stack = inventory[i];
            if (stack != null && ITEMS.contains(stack.getItem())) {
                ContainerUtils.click(i + (int) debug.getInput());
                clickDelay = Utils.randomizeInt(minDelay.getInput() / 50, maxDelay.getInput() / 50);
                onPreMotion(event);
            }
        }
    }
}
