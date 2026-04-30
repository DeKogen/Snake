package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

import java.util.List;

public final class DijkstraBotController implements SnakeController {
    private final Pathfinder pathfinder = new Pathfinder();
    private final RewardBotController fallback = new RewardBotController();

    @Override
    public Snake.Direction chooseDirection(GameSnapshot snapshot, SnakeAgent self) {
        Coord head = self.getSnake().head();
        Coord apple = snapshot.apple();

        if (apple == null) {
            return fallback.chooseDirection(snapshot, self);
        }

        List<Coord> path = pathfinder.findPath(
                snapshot,
                head,
                apple,
                self.getCurrentDirection()
        );

        Snake.Direction nextDirection = pathfinder.firstDirection(path);

        if (nextDirection != null) {
            return nextDirection;
        }

        return fallback.chooseDirection(snapshot, self);
    }
}