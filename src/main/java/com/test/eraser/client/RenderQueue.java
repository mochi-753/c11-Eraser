package com.test.eraser.client;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class RenderQueue {
    private static final List<RenderEntry> entries = new ArrayList<>();

    public static void add(RenderEntry entry) {
        entries.add(entry);
    }

    public static void clear() {
        entries.clear();
    }

    public static List<RenderEntry> getEntries() {
        return entries;
    }

    public static List<BlockPos> getPositions() {
        List<BlockPos> positions = new ArrayList<>();
        for (RenderEntry entry : entries) {
            positions.add(entry.pos);
        }
        return positions;
    }


    public static class RenderEntry {
        public final BlockPos pos;
        public final int color;

        public RenderEntry(BlockPos pos, int color) {
            this.pos = pos;
            this.color = color;
        }
    }
}