package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class RewardBotController implements SnakeController {
    private static final int BLOCKED_SCORE = -1_000_000;

    private static final int BONUS_MOVE_TOWARD_APPLE = 30;
    private static final int BONUS_KEEP_STRAIGHT = 8;
    private static final int BONUS_OPEN_NEIGHBOR = 6;
    private static final int BONUS_EXTRA_OPEN_NEIGHBOR = 3;

    private static final int PENALTY_MOVE_AWAY_FROM_APPLE = -12;
    private static final int PENALTY_NEAR_WALL = -10;
    private static final int PENALTY_DEAD_END = -40;

    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    public Snake.Direction chooseDirection(GameSnapshot snapshot, SnakeAgent self) {
        Snake.Direction current = self.getCurrentDirection();

        List<Snake.Direction> candidates = new ArrayList<>(List.of(
                Snake.Direction.UP,
                Snake.Direction.DOWN,
                Snake.Direction.LEFT,
                Snake.Direction.RIGHT
        ));

        candidates.remove(oppositeOf(current));

        int bestScore = Integer.MIN_VALUE;
        List<Snake.Direction> bestDirections = new ArrayList<>();

        for (Snake.Direction dir : candidates) {
            int score = scoreDirection(snapshot, self, dir);

            if (score > bestScore) {
                bestScore = score;
                bestDirections.clear();
                bestDirections.add(dir);
            } else if (score == bestScore) {
                bestDirections.add(dir);
            }
        }

        if (bestDirections.isEmpty()) {
            return current;
        }

        return bestDirections.get(random.nextInt(bestDirections.size()));
    }

    private int scoreDirection(GameSnapshot snapshot, SnakeAgent self, Snake.Direction dir) {
        Coord currentHead = self.getSnake().head();
        Coord next = self.getSnake().nextHead(dir);

        if (snapshot.isBlocked(next.x(), next.y())) {
            return BLOCKED_SCORE;
        }

        int score = 0;

        if (dir == self.getCurrentDirection()) {
            score += BONUS_KEEP_STRAIGHT;
        }

        Coord apple = snapshot.apple();
        if (apple != null) {
            int currentDistance = manhattan(currentHead, apple);
            int nextDistance = manhattan(next, apple);

            if (nextDistance < currentDistance) {
                score += BONUS_MOVE_TOWARD_APPLE;
            } else if (nextDistance > currentDistance) {
                score += PENALTY_MOVE_AWAY_FROM_APPLE;
            }
        }

        int openNeighbors = countOpenNeighbors(snapshot, next);
        score += openNeighbors * BONUS_OPEN_NEIGHBOR;

        if (openNeighbors == 0) {
            score += PENALTY_DEAD_END;
        }

        if (isNearWall(snapshot, next)) {
            score += PENALTY_NEAR_WALL;
        }

        score += scoreSecondStep(snapshot, next);

        return score;
    }

    private int scoreSecondStep(GameSnapshot snapshot, Coord next) {
        int bonus = 0;

        for (Snake.Direction dir : Snake.Direction.values()) {
            Coord afterNext = step(next, dir);

            if (!snapshot.isBlocked(afterNext.x(), afterNext.y())) {
                bonus += BONUS_EXTRA_OPEN_NEIGHBOR;
            }
        }

        return bonus;
    }

    private int countOpenNeighbors(GameSnapshot snapshot, Coord c) {
        int count = 0;

        for (Snake.Direction dir : Snake.Direction.values()) {
            Coord neighbor = step(c, dir);
            if (!snapshot.isBlocked(neighbor.x(), neighbor.y())) {
                count++;
            }
        }

        return count;
    }

    private boolean isNearWall(GameSnapshot snapshot, Coord c) {
        return c.x() <= 1
                || c.x() >= snapshot.width() - 2
                || c.y() <= 1
                || c.y() >= snapshot.height() - 2;
    }

    private Coord step(Coord from, Snake.Direction dir) {
        int x = from.x();
        int y = from.y();

        return switch (dir) {
            case UP -> new Coord(x, y - 1);
            case DOWN -> new Coord(x, y + 1);
            case LEFT -> new Coord(x - 1, y);
            case RIGHT -> new Coord(x + 1, y);
        };
    }

    private int manhattan(Coord a, Coord b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    private Snake.Direction oppositeOf(Snake.Direction dir) {
        return switch (dir) {
            case UP -> Snake.Direction.DOWN;
            case DOWN -> Snake.Direction.UP;
            case LEFT -> Snake.Direction.RIGHT;
            case RIGHT -> Snake.Direction.LEFT;
        };
    }
}