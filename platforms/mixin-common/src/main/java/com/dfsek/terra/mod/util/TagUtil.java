package com.dfsek.terra.mod.util;

import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagGroupLoader.RegistryTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.WorldPresetTags;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.WorldPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public final class TagUtil {
    private static final Logger logger = LoggerFactory.getLogger(TagUtil.class);

    private TagUtil() {

    }

    private static <T> Map<TagKey<T>, List<RegistryEntry<T>>> tagsToMutableMap(Registry<T> registry) {
        return registry.streamTags().collect(HashMap::new,
            (map, tag) -> map.put(tag.getTag(), tag.stream().collect(Collectors.toList())),
            HashMap::putAll);
    }

    public static void registerWorldPresetTags(Registry<WorldPreset> registry) {
        logger.info("Doing preset tag garbage....");
        Map<TagKey<WorldPreset>, List<RegistryEntry<WorldPreset>>> collect = tagsToMutableMap(registry);

        PresetUtil
            .getPresets()
            .forEach(id -> MinecraftUtil
                .getEntry(registry, id)
                .ifPresentOrElse(
                    preset -> collect
                        .computeIfAbsent(WorldPresetTags.NORMAL, tag -> new ArrayList<>())
                        .add(preset),
                    () -> logger.error("Preset {} does not exist!", id)));

        registry.startTagReload(new RegistryTags<>(registry.getKey(), collect)).apply();

        if(logger.isDebugEnabled()) {
            registry.streamEntries()
                .map(e -> e.registryKey().getValue() + ": " +
                          e.streamTags().reduce("", (s, t) -> t.id() + ", " + s, String::concat))
                .forEach(logger::debug);
        }
    }

    public static void registerBiomeTags(Registry<Biome> registry) {
        logger.info("Doing biome tag garbage....");
        Map<TagKey<Biome>, List<RegistryEntry<Biome>>> collect = tagsToMutableMap(registry);

        BiomeUtil
            .getTerraBiomeMap()
            .forEach((vb, terraBiomes) ->
                MinecraftUtil
                    .getEntry(registry, vb)
                    .ifPresentOrElse(
                        vanilla -> terraBiomes
                            .forEach(tb -> MinecraftUtil
                                .getEntry(registry, tb)
                                .ifPresentOrElse(
                                    terra -> {
                                        logger.debug(
                                            vanilla.getKey()
                                                .orElseThrow()
                                                .getValue() +
                                            " (vanilla for " +
                                            terra.getKey()
                                                .orElseThrow()
                                                .getValue() +
                                            ": " +
                                            vanilla.streamTags()
                                                .toList());

                                        vanilla.streamTags()
                                            .forEach(
                                                tag -> collect
                                                    .computeIfAbsent(
                                                        tag,
                                                        t -> new ArrayList<>())
                                                    .add(terra));
                                    },
                                    () -> logger.error(
                                        "No such biome: {}",
                                        tb))),
                        () -> logger.error("No vanilla biome: {}", vb)));

        registry.startTagReload(new RegistryTags<>(registry.getKey(), collect)).apply();

        if(logger.isDebugEnabled()) {
            registry.streamEntries()
                .map(e -> e.registryKey().getValue() + ": " +
                          e.streamTags().reduce("", (s, t) -> t.id() + ", " + s, String::concat))
                .forEach(logger::debug);
        }
    }
}
