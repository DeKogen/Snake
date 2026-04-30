package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

public interface CostMap {
    boolean isBlocked(Coord coord);

    int movementCost(Coord coord);
}