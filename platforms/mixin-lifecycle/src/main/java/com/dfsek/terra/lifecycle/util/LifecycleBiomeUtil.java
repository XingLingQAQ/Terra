package com.dfsek.terra.lifecycle.util;

import com.dfsek.terra.mod.util.BiomeUtil;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.world.biome.Biome;
import com.dfsek.terra.mod.CommonPlatform;
import com.dfsek.terra.mod.config.PreLoadCompatibilityOptions;
import com.dfsek.terra.mod.config.ProtoPlatformBiome;
import com.dfsek.terra.mod.config.VanillaBiomeProperties;
import com.dfsek.terra.mod.mixin.access.VillagerTypeAccessor;
import com.dfsek.terra.mod.util.MinecraftUtil;


public final class LifecycleBiomeUtil {
    private static final Logger logger = LoggerFactory.getLogger(LifecycleBiomeUtil.class);

    private LifecycleBiomeUtil() {

    }

    public static void registerBiomes(Registry<net.minecraft.world.biome.Biome> biomeRegistry) {
        logger.info("Registering biomes...");
        CommonPlatform.get().getConfigRegistry().forEach(pack -> { // Register all Terra biomes.
            pack.getCheckedRegistry(Biome.class)
                .forEach((id, biome) -> registerBiome(biome, pack, id, biomeRegistry));
        });
        logger.info("Terra biomes registered.");
    }

    /**
     * Clones a Vanilla biome and injects Terra data to create a Terra-vanilla biome delegate.
     *
     * @param biome The Terra BiomeBuilder.
     * @param pack  The ConfigPack this biome belongs to.
     */
    private static void registerBiome(Biome biome, ConfigPack pack,
                                      com.dfsek.terra.api.registry.key.RegistryKey id,
                                      Registry<net.minecraft.world.biome.Biome> registry) {
        RegistryKey<net.minecraft.world.biome.Biome> vanilla = ((ProtoPlatformBiome) biome.getPlatformBiome()).get(registry);


        if(pack.getContext().get(PreLoadCompatibilityOptions.class).useVanillaBiomes()) {
            ((ProtoPlatformBiome) biome.getPlatformBiome()).setDelegate(registry.getEntry(registry.get(vanilla)));
        } else {
            VanillaBiomeProperties vanillaBiomeProperties = biome.getContext().get(VanillaBiomeProperties.class);

            net.minecraft.world.biome.Biome minecraftBiome = BiomeUtil.createBiome(Objects.requireNonNull(registry.get(vanilla)),
                vanillaBiomeProperties);

            Identifier identifier = Identifier.of("terra", BiomeUtil.createBiomeID(pack, id));

            if(registry.containsId(identifier)) {
                ((ProtoPlatformBiome) biome.getPlatformBiome()).setDelegate(MinecraftUtil.getEntry(registry, identifier)
                    .orElseThrow());
            } else {
                ((ProtoPlatformBiome) biome.getPlatformBiome()).setDelegate(Registry.registerReference(registry,
                    MinecraftUtil.registerKey(identifier)
                        .getValue(),
                    minecraftBiome));
            }

            Map<RegistryKey<net.minecraft.world.biome.Biome>, VillagerType> villagerMap = VillagerTypeAccessor.getBiomeTypeToIdMap();

            villagerMap.put(RegistryKey.of(RegistryKeys.BIOME, identifier),
                Objects.requireNonNullElse(vanillaBiomeProperties.getVillagerType(),
                    villagerMap.getOrDefault(vanilla, VillagerType.PLAINS)));

            BiomeUtil.TERRA_BIOME_MAP.computeIfAbsent(vanilla.getValue(), i -> new ArrayList<>()).add(identifier);
        }
    }

}
