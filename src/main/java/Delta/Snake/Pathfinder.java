package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class Pathfinder {
    private static final int INF = PathResult.INF;

    private record Node(Coord coord, int cost) {
    }

    public PathResult dijkstra(
            GameSnapshot snapshot,
            Coord start,
            CostMap costMap,
            Snake.Direction currentDirection
    ) {
        int width = snapshot.width();
        int height = snapshot.height();

        int[][] dist = new int[height][width];
        Coord[][] previous = new Coord[height][width];

        for (int[] row : dist) {
            Arrays.fill(row, INF);
        }

        PriorityQueue<Node> queue = new PriorityQueue<>(
                Comparator.comparingInt(Node::cost)
        );

        dist[start.y()][start.x()] = 0;
        queue.add(new Node(start, 0));

        while (!queue.isEmpty()) {
            Node node = queue.poll();

            Coord current = node.coord();
            int currentCost = node.cost();

            if (currentCost != dist[current.y()][current.x()]) {
                continue;
            }

            for (Snake.Direction dir : Snake.Direction.values()) {
                if (sameCoord(current, start)
                        && currentDirection != null
                        && dir == oppositeOf(currentDirection)) {
                    continue;
                }

                Coord next = step(current, dir);

                if (!inBounds(snapshot, next)) {
                    continue;
                }

                if (costMap.isBlocked(next)) {
                    continue;
                }

                int newCost = currentCost + costMap.movementCost(next);

                if (newCost < dist[next.y()][next.x()]) {
                    dist[next.y()][next.x()] = newCost;
                    previous[next.y()][next.x()] = current;
                    queue.add(new Node(next, newCost));
                }
            }
        }

        return new PathResult(width, height, start, dist, previous);
    }

    public List<Coord> findPath(
            GameSnapshot snapshot,
            Coord start,
            Coord goal,
            Snake.Direction currentDirection
    ) {
        CostMap costMap = new BasicCostMap(snapshot);

        PathResult result = dijkstra(
                snapshot,
                start,
                costMap,
                currentDirection
        );

        return result.pathTo(goal);
    }

    public Snake.Direction firstDirection(List<Coord> path) {
        if (path == null || path.size() < 2) {
            return null;
        }

        return directionFromTo(path.get(0), path.get(1));
    }

    public static Coord step(Coord from, Snake.Direction dir) {
        int x = from.x();
        int y = from.y();

        return switch (dir) {
            case UP -> new Coord(x, y - 1);
            case DOWN -> new Coord(x, y + 1);
            case LEFT -> new Coord(x - 1, y);
            case RIGHT -> new Coord(x + 1, y);
        };
    }

    public static Snake.Direction oppositeOf(Snake.Direction dir) {
        return switch (dir) {
            case UP -> Snake.Direction.DOWN;
            case DOWN -> Snake.Direction.UP;
            case LEFT -> Snake.Direction.RIGHT;
            case RIGHT -> Snake.Direction.LEFT;
        };
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

    private boolean inBounds(GameSnapshot snapshot, Coord coord) {
        return coord.x() >= 0
                && coord.x() < snapshot.width()
                && coord.y() >= 0
                && coord.y() < snapshot.height();
    }

    private boolean sameCoord(Coord a, Coord b) {
        return a.x() == b.x() && a.y() == b.y();
    }

    private static final class BasicCostMap implements CostMap {
        private final GameSnapshot snapshot;

        private BasicCostMap(GameSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public boolean isBlocked(Coord coord) {
            return snapshot.isBlocked(coord);
        }

        @Override
        public int movementCost(Coord coord) {
            int cost = 1;

            if (isNearWall(coord)) {
                cost += 5;
            }

            return cost;
        }

        private boolean isNearWall(Coord coord) {
            return coord.x() <= 1
                    || coord.x() >= snapshot.width() - 2
                    || coord.y() <= 1
                    || coord.y() >= snapshot.height() - 2;
        }
    }
}