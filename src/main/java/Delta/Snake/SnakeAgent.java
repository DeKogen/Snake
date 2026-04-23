package Delta.Snake;

public final class SnakeAgent {
    private final int id;
    private final Snake snake;
    private final SnakeController controller;
    private Snake.Direction currentDirection;
    private final boolean player;
    private boolean alive = true;

    private int rageTicks = 0;

    public SnakeAgent(
            int id,
            Snake snake,
            SnakeController controller,
            Snake.Direction initialDirection,
            boolean player
    ) {
        this.id = id;
        this.snake = snake;
        this.controller = controller;
        this.currentDirection = initialDirection;
        this.player = player;
    }

    public int getId() {
        return id;
    }

    public Snake getSnake() {
        return snake;
    }

    public SnakeController getController() {
        return controller;
    }

    public Snake.Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Snake.Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public boolean isPlayer() {
        return player;
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        this.alive = false;
    }

    public int getRageTicks() {
        return rageTicks;
    }

    public boolean isRaging() {
        return rageTicks > 0;
    }

    public void activateRage(int ticks) {
        rageTicks += ticks;
    }

    public void tickStatuses() {
        if (rageTicks > 0) {
            rageTicks--;
        }
    }
}