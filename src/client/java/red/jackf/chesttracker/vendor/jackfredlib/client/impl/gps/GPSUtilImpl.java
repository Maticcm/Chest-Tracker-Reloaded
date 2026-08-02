package red.jackf.chesttracker.vendor.jackfredlib.client.impl.gps;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.vendor.jackfredlib.client.api.gps.PlayerListSnapshot;
import red.jackf.chesttracker.vendor.jackfredlib.client.api.gps.ScoreboardSnapshot;
import red.jackf.chesttracker.vendor.jackfredlib.client.mixins.gps.GuiAccessor;
import red.jackf.chesttracker.vendor.jackfredlib.client.mixins.gps.PlayerTabOverlayAccessor;

import java.util.List;
import java.util.Optional;

public class GPSUtilImpl {
    public static PlayerListSnapshot getPlayerList() {
        PlayerTabOverlay tabList = Minecraft.getInstance().gui.hud.getTabList();
        PlayerTabOverlayAccessor accessed = (PlayerTabOverlayAccessor) Minecraft.getInstance().gui.hud.getTabList();
        Optional<Component> header = Optional.ofNullable(accessed.jflib$getHeader());
        Optional<Component> footer = Optional.ofNullable(accessed.jflib$getFooter());
        List<Component> names = accessed.jflib$getPlayerInfos().stream()
                                        .map(tabList::getNameForDisplay)
                                        .toList();
        return new PlayerListSnapshotImpl(header, footer, names);
    }

    public static Optional<ScoreboardSnapshot> getScoreboard() {
        Objective obj = getDisplayedScoreboardObjective();
        if (obj == null) return Optional.empty();
        NumberFormat numberFormat = obj.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);
        Scoreboard scoreboard = obj.getScoreboard();

        var list = obj.getScoreboard().listPlayerScores(obj).stream()
                      .filter(entry -> !entry.isHidden())
                      .sorted(GuiAccessor.getScoreDisplayOrder())
                      .limit(15)
                      .map(entry -> {
                          PlayerTeam playerTeam = scoreboard.getPlayersTeam(entry.owner());
                          Component name = entry.ownerName();
                          Component formattedName = PlayerTeam.formatNameForTeam(playerTeam, name);
                          Component formattedValue = entry.formatValue(numberFormat);
                          return Pair.of(formattedName, formattedValue);
                      }).toList();

        return Optional.of(new ScoreboardSnapshotImpl(obj.getDisplayName(), list));
    }

    private static @Nullable Objective getDisplayedScoreboardObjective() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return null;
        var scoreboard = mc.level.getScoreboard();
        Objective obj = null;
        var playerTeam = scoreboard.getPlayerTeam(mc.player.getScoreboardName());
        if (playerTeam != null) {
            // 26.x dropped DisplaySlot.teamColorToSlot and moved team colours to TeamColor.
            // The DisplaySlot constants are still named TEAM_<COLOUR>, so map by name.
            var displaySlot = playerTeam.getColor()
                    .map(colour -> DisplaySlot.valueOf("TEAM_" + colour.name()))
                    .orElse(null);
            if (displaySlot != null) obj = scoreboard.getDisplayObjective(displaySlot);
        }
        if (obj == null) obj = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        return obj;
    }
}
