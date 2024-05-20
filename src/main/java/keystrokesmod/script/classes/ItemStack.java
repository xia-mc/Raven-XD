package keystrokesmod.script.classes;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;

import java.util.List;

public class ItemStack {
    public String type;
    public String name;
    public String displayName;
    public int stackSize;
    public int maxStackSize;
    public int durability;
    public int maxDurability;
    public boolean isBlock;
    public net.minecraft.item.ItemStack itemStack;

    public ItemStack(net.minecraft.item.ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        this.itemStack = itemStack;
        this.type = itemStack.getItem().getClass().getSimpleName();
        this.name = itemStack.getItem().getRegistryName().substring(10); // substring 10 to remove "minecraft:"
        this.displayName = itemStack.getDisplayName();
        this.stackSize = itemStack.stackSize;
        this.maxStackSize = itemStack.getMaxStackSize();
        this.durability = itemStack.getItemDamage();
        this.maxDurability = itemStack.getMaxDamage();
        this.isBlock = itemStack.getItem() instanceof ItemBlock;
    }

    public List<String> getToolTip() {
        if (this.itemStack == null || Minecraft.getMinecraft().thePlayer == null) {
            return null;
        }
        return this.itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
    }

    public static ItemStack convert(net.minecraft.item.ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        return new ItemStack(itemStack);
    }
}
