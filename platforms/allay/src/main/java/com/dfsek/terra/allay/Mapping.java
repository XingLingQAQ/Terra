package com.dfsek.terra.allay;

import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.block.type.BlockStateSafeGetter;
import org.allaymc.api.block.type.BlockStateSafeGetter.Getter;
import org.allaymc.api.block.type.BlockTypes;
import org.allaymc.api.item.type.ItemType;
import org.allaymc.api.item.type.ItemTypeSafeGetter;
import org.allaymc.api.utils.JSONUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


/**
 * @author daoge_cmd
 */
public final class Mapping {

    private static final Map<String, Map<String, String>> JE_BLOCK_DEFAULT_PROPERTIES = new Object2ObjectOpenHashMap<>();
    private static final Map<BlockState, JeBlockState> BLOCK_STATE_BE_TO_JE = new Object2ObjectOpenHashMap<>();
    private static final Map<Integer, BlockState> BLOCK_STATE_JE_HASH_TO_BE = new Int2ObjectOpenHashMap<>();
    private static final Map<String, ItemType<?>> ITEM_ID_JE_TO_BE = new Object2ObjectOpenHashMap<>();
    private static final Map<String, Integer> BIOME_ID_JE_TO_BE = new Object2IntOpenHashMap<>();
    private static final BlockState BE_AIR_STATE = BlockTypes.AIR.getDefaultState();

    public static void init() {
        if(!initBlockStateMapping()) error();
        if(!initJeBlockDefaultProperties()) error();
        if(!initItemMapping()) error();
        if(!initBiomeMapping()) error();
    }

    public static JeBlockState blockStateBeToJe(BlockState beBlockState) {
        return BLOCK_STATE_BE_TO_JE.get(beBlockState);
    }

    public static BlockState blockStateJeToBe(JeBlockState jeBlockState) {
        BlockState result = BLOCK_STATE_JE_HASH_TO_BE.get(jeBlockState.getHash());
        if(result == null) {
            TerraAllayPlugin.INSTANCE.getPluginLogger().warn("Failed to find be block state for {}", jeBlockState);
            return BE_AIR_STATE;
        }
        return result;
    }

    public static ItemType<?> itemIdJeToBe(String jeItemId) {
        return ITEM_ID_JE_TO_BE.get(jeItemId);
    }

    // Enchantment identifiers are same in both versions

    public static String enchantmentIdBeToJe(String beEnchantmentId) {
        return beEnchantmentId;
    }

    public static String enchantmentIdJeToBe(String jeEnchantmentId) {
        return jeEnchantmentId;
    }

    public static int biomeIdJeToBe(String jeBiomeId) {
        return BIOME_ID_JE_TO_BE.get(jeBiomeId);
    }

    public static Map<String, String> getJeBlockDefaultProperties(String jeBlockIdentifier) {
        Map<String, String> defaultProperties = JE_BLOCK_DEFAULT_PROPERTIES.get(jeBlockIdentifier);
        if( defaultProperties == null) {
            TerraAllayPlugin.INSTANCE.getPluginLogger().warn("Failed to find default properties for {}", jeBlockIdentifier);
            return Map.of();
        }
        return defaultProperties;
    }

    private static void error() {
        throw new RuntimeException("Mapping not initialized");
    }

    private static boolean initBiomeMapping() {
        try (InputStream stream = Mapping.class.getClassLoader().getResourceAsStream("mapping/biomes.json")) {
            if  (stream == null) {
                TerraAllayPlugin.INSTANCE.getPluginLogger().error("biomes mapping not found");
                return false;
            }
            Set<Entry<String, Map<String, Integer>>> mappings = JSONUtils.from(stream, new TypeToken<Map<String, Map<String, Integer>>>(){}).entrySet();
            mappings.forEach(mapping -> BIOME_ID_JE_TO_BE.put(mapping.getKey(), mapping.getValue().get("bedrock_id")));
        } catch(IOException e) {
            TerraAllayPlugin.INSTANCE.getPluginLogger().error("Failed to load biomes mapping", e);
            return false;
        }
        return true;
    }

    private static boolean initItemMapping() {
        try (InputStream stream = Mapping.class.getClassLoader().getResourceAsStream("mapping/items.json")) {
            if  (stream == null) {
                TerraAllayPlugin.INSTANCE.getPluginLogger().error("items mapping not found");
                return false;
            }
            Set<Entry<String, Map<String, Object>>> mappings = JSONUtils.from(stream, new TypeToken<Map<String, Map<String, Object>>>(){}).entrySet();
            mappings.forEach(mapping -> {
                ItemType<?> item = ItemTypeSafeGetter
                    .name((String) mapping.getValue().get("bedrock_identifier"))
                    // NOTICE: should be cast to double
                    .meta(((Double) mapping.getValue().get("bedrock_data")).intValue())
                    .itemType();
                ITEM_ID_JE_TO_BE.put(mapping.getKey(), item);
            });
        } catch(IOException e) {
            TerraAllayPlugin.INSTANCE.getPluginLogger().error("Failed to load items mapping", e);
        }
        return true;
    }

    private static boolean initBlockStateMapping() {
        try (InputStream stream = Mapping.class.getClassLoader().getResourceAsStream("mapping/blocks.json")) {
            if (stream == null) {
                TerraAllayPlugin.INSTANCE.getPluginLogger().error("blocks mapping not found");
                return false;
            }
            // noinspection unchecked
            List<Map<String, Map<String, Object>>> mappings = (List<Map<String, Map<String, Object>>>) JSONUtils.from(stream, new TypeToken<Map<String, Object>>(){}).get("mappings");
            mappings.forEach(mapping -> {
                JeBlockState jeState = createJeBlockState(mapping.get("java_state"));
                BlockState beState = createBeBlockState(mapping.get("bedrock_state"));
                BLOCK_STATE_BE_TO_JE.put(beState, jeState);
                BLOCK_STATE_JE_HASH_TO_BE.put(jeState.getHash(), beState);
            });
        } catch(IOException e) {
            TerraAllayPlugin.INSTANCE.getPluginLogger().error("Failed to load blocks mapping", e);
        }
        return true;
    }

    private static boolean initJeBlockDefaultProperties() {
        try (InputStream stream = Mapping.class.getClassLoader().getResourceAsStream("je_block_default_states.json")) {
            if (stream == null) {
                TerraAllayPlugin.INSTANCE.getPluginLogger().error("je_block_default_states.json not found");
                return false;
            }
            Map<String, Map<String, String>> states = JSONUtils.from(stream, new TypeToken<Map<String, Map<String, String>>>(){});
            for(Entry<String, Map<String, String>> entry : states.entrySet()) {
                String identifier = entry.getKey();
                Map<String, String> properties = entry.getValue();
                JE_BLOCK_DEFAULT_PROPERTIES.put(identifier, properties);
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private static BlockState createBeBlockState(Map<String, Object> data) {
        Getter getter = BlockStateSafeGetter
            .name("minecraft:" + data.get("bedrock_identifier"));
        if (data.containsKey("state")) {
            // noinspection unchecked
            convertValueType((Map<String, Object>) data.get("state")).forEach(getter::property);
        }
        return getter.blockState();
    }

    private static Map<String, Object> convertValueType(Map<String, Object> data) {
        TreeMap<String, Object> result = new TreeMap<>();
        for (Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof Number number) {
                // Convert double to int because the number in json is double
                result.put(entry.getKey(), number.intValue());
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static JeBlockState createJeBlockState(Map<String, Object> data) {
        // noinspection unchecked
        return JeBlockState.create((String) data.get("Name"), new TreeMap<>((Map<String, String>) data.getOrDefault("Properties", Map.of())));
    }
}
