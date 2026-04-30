package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

import java.util.List;

public final class SmortBotController implements SnakeController {
    private static final int APPLE_BASE_SCORE = 1_000;
    private static final int RAGE_APPLE_BASE_SCORE = 1_300;
    private static final int ATTACK_BASE_SCORE = 650;

    private static final int DISTANCE_PENALTY = 12;
    private static final int BLACK_HOLE_NEAR_PENALTY = 300;
    private static final int ENEMY_HEAD_NEAR_PENALTY = 120;
    private static final int WALL_NEAR_PENALTY = 12;

    private final Pathfinder pathfinder = new Pathfinder();
    private final SmortBotController fallback = new SmortBotController();

    @Override
    public Snake.Direction chooseDirection(GameSnapshot snapshot, SnakeAgent self) {
        Coord myHead = self.getSnake().head();

        CostMap costMap = new StrategicCostMap(snapshot, self.getId());

        PathResult myPaths = pathfinder.dijkstra(
                snapshot,
                myHead,
                costMap,
                self.getCurrentDirection()
        );

        Target bestTarget = chooseBestTarget(snapshot, self, myPaths);

        if (bestTarget == null) {
            return fallback.chooseDirection(snapshot, self);
        }

        Snake.Direction direction = myPaths.firstDirectionTo(bestTarget.coord());

        if (direction == null) {
            return fallback.chooseDirection(snapshot, self);
        }

        return direction;
    }

    private Target chooseBestTarget(
            GameSnapshot snapshot,
            SnakeAgent self,
            PathResult myPaths
    ) {
        Target best = null;

        Target appleTarget = evaluateApple(snapshot, myPaths);
        best = better(best, appleTarget);

        for (GameSnapshot.SnakeInfo enemy : snapshot.snakes()) {
            if (!enemy.alive()) {
                continue;
            }

            if (enemy.id() == self.getId()) {
                continue;
            }

            Target attackTarget = evaluateAttackCell(snapshot, self, enemy, myPaths);
            best = better(best, attackTarget);
        }

        return best;
    }

    private Target evaluateApple(GameSnapshot snapshot, PathResult myPaths) {
        Coord apple = snapshot.apple();

        if (apple == null) {
            return null;
        }

        if (!myPaths.reachable(apple)) {
            return null;
        }

        int distance = myPaths.distanceTo(apple);

        int score = APPLE_BASE_SCORE;
        score -= distance * DISTANCE_PENALTY;
        score -= dangerPenalty(snapshot, apple);

        return new Target(apple, score, "apple");
    }

    private Target evaluateAttackCell(
            GameSnapshot snapshot,
            SnakeAgent self,
            GameSnapshot.SnakeInfo enemy,
            PathResult myPaths
    ) {
        Coord attackCell = Pathfinder.step(enemy.head(), enemy.direction());

        if (!inBounds(snapshot, attackCell)) {
            return null;
        }

        if (snapshot.isBlocked(attackCell)) {
            return null;
        }

        if (!myPaths.reachable(attackCell)) {
            return null;
        }

        if (isEnemyBodyCell(enemy, attackCell)) {
            return null;
        }

        int myDistance = myPaths.distanceTo(attackCell);

        if (myDistance > 4) {
            return null;
        }

        int score = ATTACK_BASE_SCORE;
        score -= myDistance * 25;
        score -= dangerPenalty(snapshot, attackCell);

        if (myDistance == 1) {
            score += 120;
        }

        if (snapshot.blackHole() != null && manhattan(attackCell, snapshot.blackHole()) <= 2) {
            score -= 500;
        }

        return new Target(attackCell, score, "front of enemy head");
    }

    private int dangerPenalty(GameSnapshot snapshot, Coord coord) {
        int penalty = 0;

        if (isNearWall(snapshot, coord)) {
            penalty += WALL_NEAR_PENALTY;
        }

        Coord blackHole = snapshot.blackHole();
        if (blackHole != null) {
            int d = manhattan(coord, blackHole);

            if (d == 0) {
                penalty += 10_000;
            } else if (d == 1) {
                penalty += BLACK_HOLE_NEAR_PENALTY;
            } else if (d == 2) {
                penalty += BLACK_HOLE_NEAR_PENALTY / 2;
            } else if (d == 3) {
                penalty += BLACK_HOLE_NEAR_PENALTY / 3;
            }
        }

        for (GameSnapshot.SnakeInfo snake : snapshot.snakes()) {
            if (!snake.alive()) {
                continue;
            }

            int d = manhattan(coord, snake.head());

            if (d == 1) {
                penalty += ENEMY_HEAD_NEAR_PENALTY;
            } else if (d == 2) {
                penalty += ENEMY_HEAD_NEAR_PENALTY / 2;
            }
        }

        return penalty;
    }

    private Target better(Target currentBest, Target candidate) {
        if (candidate == null) {
            return currentBest;
        }

        if (currentBest == null) {
            return candidate;
        }

        return candidate.score() > currentBest.score()
                ? candidate
                : currentBest;
    }

    private boolean isEnemyBodyCell(GameSnapshot.SnakeInfo enemy, Coord coord) {
        List<Coord> segments = enemy.segments();

        for (int i = 1; i < segments.size(); i++) {
            Coord body = segments.get(i);

            if (sameCoord(body, coord)) {
                return true;
            }
        }

        return false;
    }

    private boolean isNearWall(GameSnapshot snapshot, Coord coord) {
        return coord.x() <= 1
                || coord.x() >= snapshot.width() - 2
                || coord.y() <= 1
                || coord.y() >= snapshot.height() - 2;
    }

    private boolean inBounds(GameSnapshot snapshot, Coord coord) {
        return coord.x() >= 0
                && coord.x() < snapshot.width()
                && coord.y() >= 0
                && coord.y() < snapshot.height();
    }

    private int manhattan(Coord a, Coord b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    private boolean sameCoord(Coord a, Coord b) {
        return a.x() == b.x() && a.y() == b.y();
    }

    private final class StrategicCostMap implements CostMap {
        private final GameSnapshot snapshot;
        private final int selfId;

        private StrategicCostMap(GameSnapshot snapshot, int selfId) {
            this.snapshot = snapshot;
            this.selfId = selfId;
        }

        @Override
        public boolean isBlocked(Coord coord) {
            if (snapshot.isBlocked(coord)) {
                return true;
            }

            Coord blackHole = snapshot.blackHole();
            return blackHole != null && sameCoord(coord, blackHole);
        }

        @Override
        public int movementCost(Coord coord) {
            int cost = 1;

            if (isNearWall(snapshot, coord)) {
                cost += 8;
            }

            Coord blackHole = snapshot.blackHole();
            if (blackHole != null) {
                int d = manhattan(coord, blackHole);

                if (d == 1) {
                    cost += 120;
                } else if (d == 2) {
                    cost += 50;
                } else if (d == 3) {
                    cost += 20;
                }
            }

            for (GameSnapshot.SnakeInfo snake : snapshot.snakes()) {
                if (!snake.alive()) {
                    continue;
                }

                if (snake.id() == selfId) {
                    continue;
                }

                int d = manhattan(coord, snake.head());

                if (d == 1) {
                    cost += 80;
                } else if (d == 2) {
                    cost += 30;
                }
            }

            int openNeighbors = countOpenNeighbors(coord);
            if (openNeighbors <= 1) {
                cost += 100;
            } else if (openNeighbors == 2) {
                cost += 20;
            }

            return cost;
        }

        private int countOpenNeighbors(Coord coord) {
            int count = 0;

            for (Snake.Direction dir : Snake.Direction.values()) {
                Coord neighbor = Pathfinder.step(coord, dir);

                if (!inBounds(snapshot, neighbor)) {
                    continue;
                }

                if (!snapshot.isBlocked(neighbor)) {
                    count++;
                }
            }

            return count;
        }
    }
}