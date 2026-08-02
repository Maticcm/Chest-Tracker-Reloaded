package red.jackf.chesttracker.vendor.jackfredlib.client.impl.toasts.icon;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.vendor.jackfredlib.api.base.Args;
import red.jackf.chesttracker.vendor.jackfredlib.client.api.toasts.CustomToast;
import red.jackf.chesttracker.vendor.jackfredlib.client.api.toasts.ToastIcon;

import java.util.List;

public class ItemStackIcon implements ToastIcon {
    private static final int INSET = 2;

    private final List<ItemStack> items;
    private final int sizeInSlots;

    public ItemStackIcon(List<ItemStack> items, int sizeInSlots) {
        Args.check(!items.isEmpty(), "Items can't be empty");
        Args.check(sizeInSlots >= 1 && sizeInSlots <= 5, "sizeInSlots must be between 1 and 5");
        this.items = ImmutableList.copyOf(items);
        this.sizeInSlots = sizeInSlots;
    }

    @Override
    public void render(CustomToast toast, GuiGraphicsExtractor graphics, int x, int y) {
        int scale = 2 * sizeInSlots - 1;

        int index = Mth.clamp((int) (toast.getProgress() * this.items.size()), 0, this.items.size() - 1);
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + INSET, y + INSET);
        graphics.pose().scale(scale, scale);
        graphics.fakeItem(this.items.get(index), 0, 0);
        graphics.pose().popMatrix();
    }

    @Override
    public int width() {
        return ToastIcon.slotsToHeight(sizeInSlots);
    }

    @Override
    public int height() {
        return ToastIcon.slotsToHeight(sizeInSlots);
    }
}
