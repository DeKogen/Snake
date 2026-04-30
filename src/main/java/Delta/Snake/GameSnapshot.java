package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

import java.util.List;
import java.util.Set;

import static Delta.Map.SegmentStorage.pack;

public record GameSnapshot(
        int width,
        int height,
        Coord apple,
        Coord blackHole,
        Set<Long> blockedCells,
        List<SnakeInfo> snakes
) {
    public GameSnapshot {
        blockedCells = Set.copyOf(blockedCells);
        snakes = List.copyOf(snakes);
    }

    public boolean isBlocked(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true;
        }

        return blockedCells.contains(pack(x, y));
    }

    public boolean isBlocked(Coord coord) {
        return isBlocked(coord.x(), coord.y());
    }

    public record SnakeInfo(
            int id,
            Coord head,
            Coord tail,
            List<Coord> segments,
            Snake.Direction direction,
            boolean player,
            boolean alive
    ) {
        public SnakeInfo {
            segments = List.copyOf(segments);
        }
    }
}