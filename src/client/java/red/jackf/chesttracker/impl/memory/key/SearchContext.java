package red.jackf.chesttracker.impl.memory.key;

import net.minecraft.world.phys.Vec3;
import red.jackf.chesttracker.impl.memory.metadata.Metadata;
import red.jackf.chesttracker.impl.search.SearchRequest;

public record SearchContext(SearchRequest request,
                            Vec3 rootPosition,
                            Metadata metadata) {
}
