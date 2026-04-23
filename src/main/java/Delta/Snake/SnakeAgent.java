package Delta.Snake;

public final class SnakeAgent {
    private final int id;
    private final Snake snake;
    private final SnakeController controller;
    private Snake.Direction currentDirection;
    private final boolean player;
    private boolean alive = true;

    private int rageTicks = 0;
    private int queuedGrowth = 0;

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

    public void startOrExtendRage(int baseTicks, int extraTicksIfAlreadyRaging) {
        if (rageTicks > 0) {
            rageTicks += extraTicksIfAlreadyRaging;
        } else {
            rageTicks = baseTicks;
        }
    }

    public void tickStatuses() {
        if (rageTicks > 0) {
            rageTicks--;
        }
    }

    public void addQueuedGrowth(int amount) {
        if (amount > 0) {
            queuedGrowth += amount;
        }
    }

    public boolean hasQueuedGrowth() {
        return queuedGrowth > 0;
    }

    public boolean consumeQueuedGrowth() {
        if (queuedGrowth > 0) {
            queuedGrowth--;
            return true;
        }
        return false;
    }

    public int getQueuedGrowth() {
        return queuedGrowth;
    }
}