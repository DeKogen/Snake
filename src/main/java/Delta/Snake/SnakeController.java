package Delta.Snake;

public interface SnakeController {
    Snake.Direction chooseDirection(GameSnapshot snapshot, SnakeAgent self);
}