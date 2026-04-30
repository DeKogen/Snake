package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

public record Target(
        Coord coord,
        int score,
        String reason
) {
}