package com.direwolf20.buildinggadgets.common.building.view;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.util.spliterator.MappingSpliterator;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * A simple {@link IBuildSequence} backed by a {@link Map Map<BlockPos, BlockData>}. {@link PlacementTarget PlacementTargets} will be created
 * lazily when iterating over this {@link IBuildSequence}. You can supply this with a mutable {@link Map} via {@link #createUnsafe(Map, Region)}
 * for efficiency reasons, note however that you will encounter undefined behaviour if the {@link Map} is modified after this {@link IBuildSequence} was
 * created.
 */
public final class PositionalBuildSequence implements IBuildSequence {
    private Map<BlockPos, BlockData> map;
    private Region boundingBox;
    private BlockPos translation;

    public static <T> PositionalBuildSequence ofIterable(Iterable<T> iterable, Function<? super T, ? extends BlockPos> keyExtractor, Function<? super T, BlockData> dataExtractor) {
        ImmutableMap.Builder<BlockPos, BlockData> builder = ImmutableMap.builder();
        Region.Builder regBuilder = iterable.iterator().hasNext() ? Region.enclosingBuilder() : Region.builder();
        for (T target : iterable) {
            BlockPos pos = keyExtractor.apply(target);
            BlockData data = dataExtractor.apply(target);
            builder.put(pos, data);
            regBuilder.enclose(pos);
        }
        return createUnsafe(builder.build(), regBuilder.build());
    }

    public static PositionalBuildSequence ofIterable(Iterable<PlacementTarget> iterable) {
        return ofIterable(iterable, PlacementTarget::getPos, PlacementTarget::getData);
    }

    public static PositionalBuildSequence create(Map<BlockPos, BlockData> map) {
        if (map instanceof ImmutableMap) {
            Region.Builder regBuilder = map.isEmpty() ? Region.builder() : Region.enclosingBuilder();
            for (BlockPos pos : map.keySet()) {
                regBuilder.enclose(pos);
            }
            return createUnsafe(map, regBuilder.build());
        } else
            return ofIterable(map.entrySet(), Map.Entry::getKey, Map.Entry::getValue);
    }

    public static PositionalBuildSequence createUnsafe(Map<BlockPos, BlockData> map, Region boundingBox) {
        return new PositionalBuildSequence(
                Objects.requireNonNull(map, "Cannot have a PositionalBuildSequence without position to data map!"),
                Objects.requireNonNull(boundingBox, "Cannot have a PositionalBuildSequence without a boundingBox!")
        );
    }

    private PositionalBuildSequence(Map<BlockPos, BlockData> map, Region boundingBox) {
        this.map = map;
        this.boundingBox = boundingBox;
        this.translation = BlockPos.ZERO;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        BlockPos translation = this.translation;
        return new MappingSpliterator<>(map.entrySet().spliterator(), e -> new PlacementTarget(e.getKey().add(translation), e.getValue()));
    }

    @Override
    public PositionalBuildSequence translateTo(BlockPos pos) {
        boundingBox = boundingBox.translate(pos.subtract(translation));//translate the bounding box to the correct position
        this.translation = pos;
        return this;
    }

    @Override
    public int estimateSize() {
        return map.size();
    }


    @Override
    public PositionalBuildSequence copy() {
        return new PositionalBuildSequence(map, boundingBox);
    }


    @Override
    public Region getBoundingBox() {
        return boundingBox;
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return map.containsKey(new BlockPos(x, y, z));
    }

    public ImmutableMap<BlockPos, BlockData> getMap() {
        return ImmutableMap.copyOf(map);
    }
}