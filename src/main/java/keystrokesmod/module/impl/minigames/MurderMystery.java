package keystrokesmod.module.impl.minigames;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MurderMystery extends Module {
    private final ButtonSetting a;
    private final ButtonSetting b;
    private final ButtonSetting c;
    private final List<EntityPlayer> murderers = new ArrayList();
    private final List<EntityPlayer> detectives = new ArrayList();
    private final String c1 = "MURDER";
    private final String c2 = "MYSTERY";
    private final String c3 = "Role:";
    private final String c4 = "&7[&cALERT&7]";
    private final String c5 = "note.pling";
    private final String c6 = "is a murderer!";
    private final String c7 = "has a bow!";
    private boolean override;

    public MurderMystery() {
        super("Murder Mystery", category.minigames);
        this.registerSetting(a = new ButtonSetting("Alert", true));
        this.registerSetting(b = new ButtonSetting("Search detectives", true));
        this.registerSetting(c = new ButtonSetting("Announce murderer", false));
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
                            if (i instanceof ItemSword || i instanceof ItemAxe || en.getHeldItem().getDisplayName().contains("aKnife")) {
                                if (!murderers.contains(en)) {
                                    murderers.add(en);
                                    if (a.isToggled()) {
                                        mc.thePlayer.playSound(this.c5, 1.0F, 1.0F);
                                        Utils.sendMessage(this.c4 + " &e" + en.getName() + " &3" + this.c6);
                                    }

                                    if (c.isToggled()) {
                                        mc.thePlayer.sendChatMessage(en.getName() + " " + this.c6);
                                    }
                                } else if (i instanceof ItemBow && b.isToggled() && !detectives.contains(en)) {
                                    detectives.add(en);
                                    if (a.isToggled()) {
                                        Utils.sendMessage(this.c4 + " &e" + en.getName() + " &3" + this.c7);
                                    }

                                    if (c.isToggled()) {
                                        mc.thePlayer.sendChatMessage(en.getName() + " " + this.c7);
                                    }
                                }
                            }
                        }

                        override = true;

                        int rgb = Color.green.getRGB();
                        if (murderers.contains(en)) {
                            rgb = Color.red.getRGB();
                        } else if (detectives.contains(en)) {
                            rgb = Color.orange.getRGB();
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
            if (!d.contains(this.c1) && !d.contains(this.c2)) {
                return false;
            }

            Iterator var2 = Utils.gsl().iterator();

            while (var2.hasNext()) {
                String l = (String) var2.next();
                String s = Utils.stripColor(l);
                if (s.contains(this.c3)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEmpty() {
        return murderers.isEmpty() && detectives.isEmpty() && !override;
    }

    private void clear() {
        override = false;
        murderers.clear();
        detectives.clear();
    }
}
