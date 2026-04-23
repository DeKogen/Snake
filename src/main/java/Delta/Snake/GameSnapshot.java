package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

import java.util.Set;

import static Delta.Map.SegmentStorage.pack;

public record GameSnapshot(
        int width,
        int height,
        Coord apple,
        Coord blackHole,
        Set<Long> blockedCells
) {
    public boolean isBlocked(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true;
        }
        return blockedCells.contains(pack(x, y));
    }
}