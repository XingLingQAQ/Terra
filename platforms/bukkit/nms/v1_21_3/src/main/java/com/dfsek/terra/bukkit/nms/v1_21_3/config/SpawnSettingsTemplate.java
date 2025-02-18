package com.dfsek.terra.bukkit.nms.v1_21_3.config;

import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import java.util.List;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SpawnSettingsTemplate implements ObjectTemplate<MobSpawnSettings> {

    private static final Logger logger = LoggerFactory.getLogger(SpawnTypeConfig.class);
    private static boolean used = false;

    @Value("spawns")
    @Default
    private List<SpawnTypeConfig> spawns = null;
    
    @Value("costs")
    @Default
    private List<SpawnCostConfig> costs = null;
    
    @Value("probability")
    @Default
    private Float probability = null;
    
    @Override
    public MobSpawnSettings get() {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        for(SpawnTypeConfig spawn : spawns) {
            MobCategory group = spawn.getGroup();
            for(SpawnerData entry : spawn.getEntries()) {
                builder.addSpawn(group, entry);
            }
        }
        for(SpawnCostConfig cost : costs) {
            builder.addMobCharge(cost.getType(), cost.getMass(), cost.getGravity());
        }
        if(probability != null) {
            builder.creatureGenerationProbability(probability);
        }
        
        return builder.build();
    }
}
