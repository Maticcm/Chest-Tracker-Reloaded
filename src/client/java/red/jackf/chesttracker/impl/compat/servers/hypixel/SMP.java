package red.jackf.chesttracker.impl.compat.servers.hypixel;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

interface SMP {
    static boolean isSMPJoinMessage(Component message) {
        // 26.x turned ClickEvent into a sealed interface with a record per action, so the
        // old getAction() enum comparison becomes an instanceof check.
        return message.getString().startsWith("SMP ID: ")
                && message.getStyle().getClickEvent() instanceof ClickEvent.SuggestCommand;
    }
}
