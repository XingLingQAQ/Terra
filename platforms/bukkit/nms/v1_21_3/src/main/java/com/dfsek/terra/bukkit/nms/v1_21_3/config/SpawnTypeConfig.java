package com.dfsek.terra.bukkit.nms.v1_21_3.config;

import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import java.util.List;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;


public class SpawnTypeConfig implements ObjectTemplate<SpawnTypeConfig> {
    @Value("group")
    @Default
    private MobCategory group = null;

    @Value("entries")
    @Default
    private List<SpawnerData> entries = null;

    public MobCategory getGroup() {
        return group;
    }

    public List<SpawnerData> getEntries() {
        return entries;
    }

    @Override
    public SpawnTypeConfig get() {
        return this;
    }
}