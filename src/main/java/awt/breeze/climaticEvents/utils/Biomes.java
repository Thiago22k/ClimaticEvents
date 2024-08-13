package awt.breeze.climaticEvents.utils;

import org.bukkit.block.Biome;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum Biomes {
    DESERT(Biome.DESERT, BiomeType.WARM),
    SAVANNA(Biome.SAVANNA, BiomeType.WARM),
    BADLANDS(Biome.BADLANDS, BiomeType.WARM),
    ERODED_BADLANDS(Biome.ERODED_BADLANDS, BiomeType.WARM),
    WOODED_BADLANDS(Biome.WOODED_BADLANDS, BiomeType.WARM),
    SAVANNA_PLATEAU(Biome.SAVANNA_PLATEAU, BiomeType.WARM),
    WINDSWEPT_SAVANNA(Biome.WINDSWEPT_SAVANNA, BiomeType.WARM),

    SNOWY_TAIGA(Biome.SNOWY_TAIGA, BiomeType.COLD),
    SNOWY_PLAINS(Biome.SNOWY_PLAINS, BiomeType.COLD),
    ICE_SPIKES(Biome.ICE_SPIKES, BiomeType.COLD),
    SNOWY_BEACH(Biome.SNOWY_BEACH, BiomeType.COLD),
    SNOWY_SLOPES(Biome.SNOWY_SLOPES, BiomeType.COLD),
    FROZEN_PEAKS(Biome.FROZEN_PEAKS, BiomeType.COLD),
    JAGGED_PEAKS(Biome.JAGGED_PEAKS, BiomeType.COLD),
    FROZEN_OCEAN(Biome.FROZEN_OCEAN, BiomeType.COLD),
    DEEP_FROZEN_OCEAN(Biome.DEEP_FROZEN_OCEAN, BiomeType.COLD);

    private final Biome biome;
    private final BiomeType type;

    Biomes(Biome biome, BiomeType type) {
        this.biome = biome;
        this.type = type;
    }

    public Biome getBiome() {
        return biome;
    }

    public BiomeType getType() {
        return type;
    }
    public static Set<Biome> getNoRainBiomes() {
        return EnumSet.allOf(Biomes.class).stream()
                .map(Biomes::getBiome)
                .collect(Collectors.toSet());
    }
    public static Set<Biome> getWarmBiomes() {
        return EnumSet.allOf(Biomes.class).stream()
                .filter(b -> b.getType() == BiomeType.WARM)
                .map(Biomes::getBiome)
                .collect(Collectors.toSet());
    }
    public static Set<Biome> getColdBiomes() {
        return EnumSet.allOf(Biomes.class).stream()
                .filter(b -> b.getType() == BiomeType.COLD)
                .map(Biomes::getBiome)
                .collect(Collectors.toSet());
    }
}

