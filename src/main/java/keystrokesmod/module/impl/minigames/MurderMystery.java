package keystrokesmod.module.impl.minigames;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MurderMystery extends Module {
    private final ButtonSetting alert;
    private final ButtonSetting highlightMurderer;
    private final ButtonSetting highlightBow;
    private final ButtonSetting highlightInnocent;
    private final List<EntityPlayer> murderers = new ArrayList<>();
    private final List<EntityPlayer> hasBow = new ArrayList<>();
    private boolean override;

    private static final Set<Item> MURDER_ITEMS = new HashSet<>(Arrays.asList(
            Items.wooden_sword,
            Items.stone_sword,
            Items.golden_sword,
            Items.iron_sword,
            Items.diamond_sword,
            Items.wooden_axe,
            Items.stone_axe,
            Items.golden_axe,
            Items.iron_axe,
            Items.diamond_axe,
            Items.stick,
            Items.blaze_rod,
            Items.deadbush,   
            Items.stone_shovel,
            Items.diamond_shovel,
            Items.quartz,
            Items.pumpkin_pie,
            Items.golden_pickaxe,
            Items.apple,
            Items.name_tag,
            Items.sponge,
            Items.carrot_on_a_stick,
            Items.bone,
            Items.carrot,
            Items.golden_carrot,
            Items.cookie,
            Items.diamond_axe,
            Items.double_plant,
            Items.prismarine,
            Items.cooked_beef,
            Items.netherbrick,
            Items.cooked_chicken,
            Items.record_blocks,
            Items.golden_hoe,
            Items.diamond_hoe,
            Items.shears,
            Items.cooked_fish,
            Items.boat,
            Items.speckled_melon,
            Items.book,
            Items.sapling
    ));

    public MurderMystery() {
        super("Murder Mystery", category.minigames);
        this.registerSetting(alert = new ButtonSetting("Alert", true));
        this.registerSetting(highlightMurderer = new ButtonSetting("Highlight murderer", true));
        this.registerSetting(highlightBow = new ButtonSetting("Highlight bow", true));
        this.registerSetting(highlightInnocent = new ButtonSetting("Highlight innocent", true));
    }

    public void onDisable() {
        this.clear();
    }

    @SubscribeEvent
    public void o(RenderWorldLastEvent e) {
        if (Utils.nullCheck()) {
            if (!this.isMurderMystery()) {
                this.clear();
            } else {
                override = false;
                for (EntityPlayer en : mc.theWorld.playerEntities) {
                    if (en != mc.thePlayer && !en.isInvisible()) {
                        if (en.getHeldItem() != null && en.getHeldItem().hasDisplayName()) {
                            Item i = en.getHeldItem().getItem();
                            if (MURDER_ITEMS.contains(i) || en.getHeldItem().getDisplayName().contains("knife")) {
                                if (!murderers.contains(en)) {
                                    murderers.add(en);
                                    if (alert.isToggled()) {
                                        mc.thePlayer.playSound("note.pling", 1.0F, 1.0F);
                                        Utils.sendMessage("&7[&cALERT&7]" + " &e" + en.getName() + " &3" + "is a murderer!");
                                    }
                                } else if (i instanceof ItemBow && highlightMurderer.isToggled() && !hasBow.contains(en)) {
                                    hasBow.add(en);
                                    if (alert.isToggled()) {
                                        mc.thePlayer.playSound("note.pling", 1.0F, 1.0F);
                                        Utils.sendMessage("&7[&cALERT&7]" + " &e" + en.getName() + " &3" + "has a bow!");
                                    }
                                }
                            }
                        }
                        override = true;
                        int rgb = Color.green.getRGB();
                        if (murderers.contains(en) && highlightMurderer.isToggled()) {
                            rgb = Color.red.getRGB();
                        }
                        else if (hasBow.contains(en) && highlightBow.isToggled()) {
                            rgb = Color.orange.getRGB();
                        }
                        else if (!highlightInnocent.isToggled()) {
                            continue;
                        }
                        RenderUtils.renderEntity(en, 2, 0.0D, 0.0D, rgb, false);
                    }
                }
            }
        }
    }

    private boolean isMurderMystery() {
        if (Utils.isHypixel()) {
            if (mc.thePlayer.getWorldScoreboard() == null || mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1) == null) {
                return false;
            }

            String d = mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1).getDisplayName();
            String c1 = "MURDER";
            String c2 = "MYSTERY";
            if (!d.contains(c1) && !d.contains(c2)) {
                return false;
            }

            Iterator var2 = Utils.gsl().iterator();

            while (var2.hasNext()) {
                String l = (String) var2.next();
                String s = Utils.stripColor(l);
                String c3 = "Role:";
                if (s.contains(c3)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEmpty() {
        return murderers.isEmpty() && hasBow.isEmpty() && !override;
    }

    private void clear() {
        override = false;
        murderers.clear();
        hasBow.clear();
    }
}
