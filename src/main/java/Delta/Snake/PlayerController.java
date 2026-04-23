package Delta.Snake;

public final class PlayerController implements SnakeController {
    private final DirectionController directionController;

    public PlayerController(DirectionController directionController) {
        this.directionController = directionController;
    }

    @Override
    public Snake.Direction chooseDirection(GameSnapshot snapshot, SnakeAgent self) {
        return directionController.consumeDirection();
    }
}