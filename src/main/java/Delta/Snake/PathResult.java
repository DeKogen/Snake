package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PathResult {
    public static final int INF = 1_000_000_000;

    private final int width;
    private final int height;
    private final Coord start;
    private final int[][] dist;
    private final Coord[][] previous;

    public PathResult(
            int width,
            int height,
            Coord start,
            int[][] dist,
            Coord[][] previous
    ) {
        this.width = width;
        this.height = height;
        this.start = start;
        this.dist = dist;
        this.previous = previous;
    }

    public boolean reachable(Coord coord) {
        return inBounds(coord) && dist[coord.y()][coord.x()] < INF;
    }

    public int distanceTo(Coord coord) {
        if (!inBounds(coord)) {
            return INF;
        }

        return dist[coord.y()][coord.x()];
    }

    public List<Coord> pathTo(Coord goal) {
        if (!reachable(goal)) {
            return List.of();
        }

        List<Coord> path = new ArrayList<>();

        Coord current = goal;

        while (current != null) {
            path.add(current);

            if (sameCoord(current, start)) {
                break;
            }

            current = previous[current.y()][current.x()];
        }

        Collections.reverse(path);
        return path;
    }

    public Snake.Direction firstDirectionTo(Coord goal) {
        List<Coord> path = pathTo(goal);

        if (path.size() < 2) {
            return null;
        }

        return directionFromTo(path.get(0), path.get(1));
    }

    private Snake.Direction directionFromTo(Coord from, Coord to) {
        int dx = to.x() - from.x();
        int dy = to.y() - from.y();

        if (dx == 1 && dy == 0) {
            return Snake.Direction.RIGHT;
        }

        if (dx == -1 && dy == 0) {
            return Snake.Direction.LEFT;
        }

        if (dx == 0 && dy == 1) {
            return Snake.Direction.DOWN;
        }

        if (dx == 0 && dy == -1) {
            return Snake.Direction.UP;
        }

        return null;
    }

    private boolean inBounds(Coord coord) {
        return coord.x() >= 0
                && coord.x() < width
                && coord.y() >= 0
                && coord.y() < height;
    }

    private boolean sameCoord(Coord a, Coord b) {
        return a.x() == b.x() && a.y() == b.y();
    }
}