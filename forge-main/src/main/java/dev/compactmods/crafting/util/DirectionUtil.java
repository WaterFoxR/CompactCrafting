package dev.compactmods.crafting.util;

import net.minecraft.core.Direction;

import javax.annotation.Nonnull;

public class DirectionUtil {

    @Nonnull
    public static Direction.Axis getCrossDirectionAxis(Direction.Axis originalAxis) {
        return switch (originalAxis) {
            case X -> Direction.Axis.Z;
            case Z -> Direction.Axis.X;
            default -> originalAxis;
        };
    }
}
