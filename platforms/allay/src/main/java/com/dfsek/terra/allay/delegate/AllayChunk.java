package com.dfsek.terra.allay.delegate;

import org.allaymc.api.block.property.type.BlockPropertyTypes;
import org.allaymc.api.block.tag.BlockTags;
import org.allaymc.api.block.type.BlockTypes;
import org.allaymc.api.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import com.dfsek.terra.allay.Mapping;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.ServerWorld;

/**
 * @author daoge_cmd
 */
public record AllayChunk(ServerWorld world, Chunk allayChunk) implements com.dfsek.terra.api.world.chunk.Chunk {

    private static final org.allaymc.api.block.type.BlockState WATER = BlockTypes.WATER.ofState(BlockPropertyTypes.LIQUID_DEPTH.createValue(0));

    @Override
    public void setBlock(int x, int y, int z, BlockState data, boolean physics) {
        AllayBlockState allayBlockState = (AllayBlockState) data;
        allayChunk.setBlockState(x, y, z, allayBlockState.allayBlockState());
        boolean containsWater = allayBlockState.containsWater() || allayChunk.getBlockState(x, y, z).getBlockType().hasBlockTag(BlockTags.WATER);
        if (containsWater) {
            allayChunk.setBlockState(x, y, z, WATER, 1);
        }
    }

    @Override
    public @NotNull BlockState getBlock(int x, int y, int z) {
        org.allaymc.api.block.type.BlockState blockState = allayChunk.getBlockState(x, y, z);
        return new AllayBlockState(blockState, Mapping.blockStateBeToJe(blockState));
    }

    @Override
    public int getX() {
        return allayChunk.getX();
    }

    @Override
    public int getZ() {
        return allayChunk.getZ();
    }

    @Override
    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public Chunk getHandle() {
        return allayChunk;
    }
}
