package red.jackf.chesttracker.impl.gui.widget;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.mixins.AbstractWidgetAccessor;

/**
 * Wrapper that draws a widget above the rest of the screen.
 *
 * <p>Before 26.x this applied a Z translation, hence the name. The 26.x GUI pose stack is a 2D
 * {@code Matrix3x2fStack} with no Z, so layering is now expressed by advancing to the next
 * stratum instead.</p>
 */
public class WidgetZOffsetWrapper<T extends AbstractWidget> extends AbstractWidget {
    private final T baseWidget;

    public WidgetZOffsetWrapper(T baseWidget) {
        super(baseWidget.getX(), baseWidget.getY(), baseWidget.getWidth(), baseWidget.getHeight(), baseWidget.getMessage());
        this.baseWidget = baseWidget;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 26.x pose is a 2D Matrix3x2fStack with no Z, so depth ordering is expressed with
        // strata instead of a Z translation.
        guiGraphics.nextStratum();
        ((AbstractWidgetAccessor) baseWidget).extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        // TODO try fix
        // this crashes with a non-descriptive IllegalAccessException when used on AutoComplete
        // ((AbstractWidgetAccessor) baseWidget).updateWidgetNarration(output);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        baseWidget.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return baseWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();
        return baseWidget.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();
        return baseWidget.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char codePoint = (char) event.codepoint();
        int modifiers = 0;
        return baseWidget.charTyped(event);
    }

    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        return baseWidget.getCurrentFocusPath();
    }

    @Override
    public void setPosition(int x, int y) {
        baseWidget.setPosition(x, y);
    }
}
