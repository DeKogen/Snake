package Delta.Snake;

import Delta.Map.SegmentStorage.Coord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

public final class RandomBotController implements SnakeController {
    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    public Snake.Direction chooseDirection(GameSnapshot snapshot, SnakeAgent self) {
        List<Snake.Direction> dirs = new ArrayList<>(List.of(
                Snake.Direction.UP,
                Snake.Direction.DOWN,
                Snake.Direction.LEFT,
                Snake.Direction.RIGHT
        ));

        Snake.Direction opposite = oppositeOf(self.getCurrentDirection());
        dirs.remove(opposite);
        Collections.shuffle(dirs);

        for (Snake.Direction dir : dirs) {
            Coord next = self.getSnake().nextHead(dir);
            if (!snapshot.isBlocked(next.x(), next.y())) {
                return dir;
            }
        }

        return self.getCurrentDirection();
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