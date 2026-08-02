package red.jackf.chesttracker.impl.search;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gets all blocks connected to a given position. Used to deduplicate results for multi-block
 * storages such as double chests.
 *
 * <p>Adapted from Where Is It's {@code red.jackf.whereisit.api.search.ConnectedBlocksGrabber},
 * with its vanilla double-chest handler folded in.</p>
 */
public interface ConnectedBlocksGrabber {
    Event<ConnectedBlocksGrabber> EVENT = EventFactory.createArrayBacked(ConnectedBlocksGrabber.class,
            handlers -> (positions, pos, level, state) -> {
                for (ConnectedBlocksGrabber handler : handlers)
                    handler.getConnected(positions, pos, level, state);
            });

    /**
     * Registers the built-in handlers. Called once during client setup.
     */
    static void setupDefaults() {
        // Double [trapped] chests
        EVENT.register((positions, pos, level, state) -> {
            if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE)
                positions.add(pos.relative(ChestBlock.getConnectedDirection(state)));
        });
    }

    /**
     * Add all blocks connected to a given position to a set.
     */
    void getConnected(Set<BlockPos> positions, BlockPos pos, Level level, BlockState state);

    /**
     * Gets all blocks linked to a given position, always including {@code pos}. The returned list is
     * stable - the order is the same each time it is called.
     */
    static List<BlockPos> getConnected(Level level, BlockState state, BlockPos pos) {
        var set = new HashSet<BlockPos>();
        set.add(pos.immutable());
        EVENT.invoker().getConnected(set, pos, level, state);
        if (set.size() == 1) return List.of(pos.immutable());
        return set.stream().sorted(Comparator.comparingLong(BlockPos::asLong)).toList();
    }
}
