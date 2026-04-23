package Delta.Snake;

import Delta.Map.SegmentStorage;
import Delta.Map.SegmentStorage.Coord;
import Delta.Map.SegmentStorage.SegmentType;

import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

public final class Game {

    public enum State {
        RUNNING,
        GAME_OVER,
        WIN
    }

    public enum AppleType {
        NORMAL,
        RAGE
    }

    private final int width;
    private final int height;
    private final SegmentStorage board;

    private static final int RAGE_APPLE_CHANCE = 5;
    private static final int RAGE_BASE_TICKS = 5;
    private static final int RAGE_EXTRA_TICKS_PER_APPLE = 1;
    private static final int RAGE_BUFFERED_GROWTH = 3;

    private static final int NORMAL_TICK_MS = 250;
    private static final int RAGE_TICK_MS = 90;

    private static final int BLACK_HOLE_PULL_RADIUS = 4;
    private static final int BLACK_HOLE_RELOCATE_EVERY = 18;

    private final java.util.List<SnakeAgent> snakes = new java.util.ArrayList<>();

    private Coord apple;
    private AppleType appleType = AppleType.NORMAL;
    private Coord blackHole;

    private State state = State.RUNNING;
    private int score = 0;
    private int blackHoleTicksLeft = BLACK_HOLE_RELOCATE_EVERY;
    private int nextSnakeId = 1;

    private static final int BOT_SPAWN_EVERY_SCORE = 5;
    private static final int BASE_BOT_LENGTH = 3;

    private int nextBotSpawnScore = BOT_SPAWN_EVERY_SCORE;

    public Game(int width, int height, DirectionController playerInput) {
        if (width < 7 || height < 7) {
            throw new IllegalArgumentException("Board is too small");
        }

        this.width = width;
        this.height = height;
        this.board = new SegmentStorage(width, height);

        placeBorderWalls();

        Snake playerSnake = new Snake(width / 2, height / 2, 3, Snake.Direction.RIGHT);
        SnakeAgent playerAgent = new SnakeAgent(
                nextSnakeId++,
                playerSnake,
                new PlayerController(playerInput),
                Snake.Direction.RIGHT,
                true
        );
        snakes.add(playerAgent);
        placeAgentOnBoard(playerAgent);

        if (!spawnApple()) {
            state = State.WIN;
        }

        spawnBlackHole();
        rebuildBoard();
    }

    public boolean tick() {
        if (state != State.RUNNING) {
            return false;
        }

        if (score >= nextBotSpawnScore) {
            int botLength = BASE_BOT_LENGTH + (score / 10);
            addRandomBot(botLength, new RewardBotController());
            nextBotSpawnScore += BOT_SPAWN_EVERY_SCORE;
        }

        for (SnakeAgent agent : snakes) {
            if (agent.isAlive()) {
                agent.tickStatuses();
            }
        }

        updateBlackHole();

        java.util.Map<SnakeAgent, Snake.Direction> chosenDirections = new java.util.HashMap<>();
        java.util.Map<SnakeAgent, Coord> nextHeads = new java.util.HashMap<>();
        java.util.Map<Long, Integer> headTargets = new java.util.HashMap<>();

        GameSnapshot snapshot = buildSnapshot();

        for (SnakeAgent agent : snakes) {
            if (!agent.isAlive()) {
                continue;
            }

            Snake.Direction requested = agent.getController().chooseDirection(snapshot, agent);
            Snake.Direction actual = sanitizeDirection(agent.getCurrentDirection(), requested);
            actual = applyBlackHolePull(agent, actual);
            agent.setCurrentDirection(actual);

            Coord next = agent.getSnake().nextHead(actual);

            chosenDirections.put(agent, actual);
            nextHeads.put(agent, next);

            long key = SegmentStorage.pack(next.x(), next.y());
            headTargets.merge(key, 1, Integer::sum);
        }

        java.util.Set<Long> occupiedNow = buildOccupiedSet();
        boolean appleEaten = false;

        for (SnakeAgent agent : snakes) {
            if (!agent.isAlive()) {
                continue;
            }

            Coord next = nextHeads.get(agent);
            long nextKey = SegmentStorage.pack(next.x(), next.y());

            if (!isInside(next.x(), next.y())) {
                agent.kill();
                continue;
            }

            if (isBlackHole(next.x(), next.y())) {
                agent.kill();
                continue;
            }

            SegmentType cell = board.get(next.x(), next.y());
            if (cell == SegmentType.WALL) {
                agent.kill();
                continue;
            }

            if (headTargets.getOrDefault(nextKey, 0) > 1) {
                agent.kill();
                continue;
            }

            long ownTail = SegmentStorage.pack(agent.getSnake().tail().x(), agent.getSnake().tail().y());
            boolean ateApple = apple != null && next.equals(apple);
            boolean growsThisTurn = ateApple || agent.hasQueuedGrowth();

            boolean hitsBody = occupiedNow.contains(nextKey) && !(nextKey == ownTail && !growsThisTurn);
            if (hitsBody) {
                agent.kill();
            }
        }

        for (SnakeAgent agent : snakes) {
            if (!agent.isAlive()) {
                continue;
            }

            Coord next = nextHeads.get(agent);

            boolean ateApple = apple != null && next.equals(apple);
            boolean bufferedGrowth = agent.consumeQueuedGrowth();
            boolean grows = ateApple || bufferedGrowth;

            agent.getSnake().move(chosenDirections.get(agent), grows);

            if (ateApple) {
                appleEaten = true;

                if (appleType == AppleType.RAGE) {
                    agent.startOrExtendRage(RAGE_BASE_TICKS, RAGE_EXTRA_TICKS_PER_APPLE);
                    agent.addQueuedGrowth(RAGE_BUFFERED_GROWTH);
                }

                if (agent.isPlayer()) {
                    score += (appleType == AppleType.RAGE) ? 3 : 1;
                }
            }
        }

        boolean playerAlive = snakes.stream().anyMatch(a -> a.isPlayer() && a.isAlive());
        if (!playerAlive) {
            state = State.GAME_OVER;
            rebuildBoard();
            return false;
        }

        rebuildBoard();

        if (appleEaten) {
            apple = null;
            if (!spawnApple()) {
                state = State.WIN;
                rebuildBoard();
                return false;
            }
            rebuildBoard();
        }

        return true;
    }

    public boolean addRandomBot(int length, SnakeController controller) {
        if (state != State.RUNNING) {
            return false;
        }

        final int maxAttempts = 10_000;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Snake.Direction dir = randomDirection();
            Coord head = randomEmptyCell();

            if (head == null) {
                return false;
            }

            if (!canPlaceSnake(head.x(), head.y(), length, dir)) {
                continue;
            }

            Snake snake = new Snake(head.x(), head.y(), length, dir);
            SnakeAgent agent = new SnakeAgent(
                    nextSnakeId++,
                    snake,
                    controller,
                    dir,
                    false
            );

            snakes.add(agent);
            placeAgentOnBoard(agent);
            rebuildBoard();
            return true;
        }

        return false;
    }

    public Coord getApple() {
        return apple;
    }

    public AppleType getAppleType() {
        return appleType;
    }

    public Coord getBlackHole() {
        return blackHole;
    }

    public int getPlayerRageTicks() {
        for (SnakeAgent agent : snakes) {
            if (agent.isPlayer()) {
                return agent.getRageTicks();
            }
        }
        return 0;
    }

    public int getCurrentTickDelayMs() {
        return getPlayerRageTicks() > 0 ? RAGE_TICK_MS : NORMAL_TICK_MS;
    }

    public int getPlayerQueuedGrowth() {
        for (SnakeAgent agent : snakes) {
            if (agent.isPlayer()) {
                return agent.getQueuedGrowth();
            }
        }
        return 0;
    }

    public boolean addRandomBot(int length) {
        return addRandomBot(length, new RewardBotController());
    }

    private Snake.Direction randomDirection() {
        Snake.Direction[] dirs = Snake.Direction.values();
        return dirs[ThreadLocalRandom.current().nextInt(dirs.length)];
    }

    private Coord randomEmptyCell() {
        return board.getRandomByType(SegmentType.EMPTY);
    }

    private boolean canPlaceSnake(int headX, int headY, int length, Snake.Direction dir) {
        for (int i = 0; i < length; i++) {
            int x = headX;
            int y = headY;

            switch (dir) {
                case RIGHT -> x = headX - i;
                case LEFT -> x = headX + i;
                case UP -> y = headY + i;
                case DOWN -> y = headY - i;
            }

            if (!isInside(x, y)) {
                return false;
            }

            if (board.get(x, y) != SegmentType.EMPTY) {
                return false;
            }
        }

        return true;
    }

    private void placeAgentOnBoard(SnakeAgent agent) {
        java.util.List<Coord> segments = agent.getSnake().segments();

        for (int i = 0; i < segments.size(); i++) {
            Coord c = segments.get(i);
            board.put(c.x(), c.y(), i == 0 ? SegmentType.SNAKE_HEAD : SegmentType.SNAKE_BODY);
        }
    }


    private void rebuildBoard() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    board.put(x, y, SegmentType.WALL);
                } else {
                    board.put(x, y, SegmentType.EMPTY);
                }
            }
        }

        if (apple != null) {
            board.put(apple.x(), apple.y(), SegmentType.APPLE);
        }

        for (SnakeAgent agent : snakes) {
            if (!agent.isAlive()) {
                continue;
            }

            java.util.List<Coord> segments = agent.getSnake().segments();
            for (int i = 0; i < segments.size(); i++) {
                Coord c = segments.get(i);
                board.put(c.x(), c.y(), i == 0 ? SegmentType.SNAKE_HEAD : SegmentType.SNAKE_BODY);
            }
        }
    }

    private GameSnapshot buildSnapshot() {
        return new GameSnapshot(width, height, apple, blackHole, buildBlockedSet());
    }

    private java.util.Set<Long> buildBlockedSet() {
        java.util.Set<Long> blocked = new java.util.HashSet<>();

        for (int x = 0; x < width; x++) {
            blocked.add(SegmentStorage.pack(x, 0));
            blocked.add(SegmentStorage.pack(x, height - 1));
        }

        for (int y = 0; y < height; y++) {
            blocked.add(SegmentStorage.pack(0, y));
            blocked.add(SegmentStorage.pack(width - 1, y));
        }

        blocked.addAll(buildOccupiedSet());

        if (blackHole != null) {
            blocked.add(SegmentStorage.pack(blackHole.x(), blackHole.y()));
        }

        return blocked;
    }

    private java.util.Set<Long> buildOccupiedSet() {
        java.util.Set<Long> occupied = new java.util.HashSet<>();

        for (SnakeAgent agent : snakes) {
            if (!agent.isAlive()) {
                continue;
            }

            for (Coord c : agent.getSnake().segments()) {
                occupied.add(SegmentStorage.pack(c.x(), c.y()));
            }
        }

        return occupied;
    }

    private Snake.Direction sanitizeDirection(Snake.Direction current, Snake.Direction requested) {
        if (requested == null) {
            return current;
        }

        boolean opposite =
                (current == Snake.Direction.UP && requested == Snake.Direction.DOWN)
                        || (current == Snake.Direction.DOWN && requested == Snake.Direction.UP)
                        || (current == Snake.Direction.LEFT && requested == Snake.Direction.RIGHT)
                        || (current == Snake.Direction.RIGHT && requested == Snake.Direction.LEFT);

        return opposite ? current : requested;
    }

    private void placeBorderWalls() {
        for (int x = 0; x < width; x++) {
            board.put(x, 0, SegmentType.WALL);
            board.put(x, height - 1, SegmentType.WALL);
        }

        for (int y = 0; y < height; y++) {
            board.put(0, y, SegmentType.WALL);
            board.put(width - 1, y, SegmentType.WALL);
        }
    }

    private boolean spawnApple() {
        Coord c = board.getRandomByType(SegmentType.EMPTY);
        if (c == null) {
            return false;
        }

        apple = c;
        appleType = ThreadLocalRandom.current().nextInt(RAGE_APPLE_CHANCE) == 0
                ? AppleType.RAGE
                : AppleType.NORMAL;

        board.put(c.x(), c.y(), SegmentType.APPLE);
        return true;
    }

    private void spawnBlackHole() {
        final int maxAttempts = 2_000;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Coord c = randomEmptyCell();
            if (c == null) {
                blackHole = null;
                return;
            }

            if (apple != null && c.equals(apple)) {
                continue;
            }

            blackHole = c;
            blackHoleTicksLeft = BLACK_HOLE_RELOCATE_EVERY;
            return;
        }

        blackHole = null;
    }

    private void updateBlackHole() {
        if (blackHole == null) {
            spawnBlackHole();
            return;
        }

        blackHoleTicksLeft--;

        if (blackHoleTicksLeft <= 0) {
            spawnBlackHole();
        }
    }

    private boolean isBlackHole(int x, int y) {
        return blackHole != null && blackHole.x() == x && blackHole.y() == y;
    }

    private Snake.Direction applyBlackHolePull(SnakeAgent agent, Snake.Direction dir) {
//        if (agent.isPlayer()) {
//            return dir;
//        }

        if (blackHole == null || agent.isRaging()) {
            return dir;
        }

        Coord head = agent.getSnake().head();
        int dx = blackHole.x() - head.x();
        int dy = blackHole.y() - head.y();
        int distance = Math.abs(dx) + Math.abs(dy);

        if (distance == 0 || distance > BLACK_HOLE_PULL_RADIUS) {
            return dir;
        }

        Snake.Direction pullDirection = directionToward(dx, dy);
        if (pullDirection == null) {
            return dir;
        }

        if (isOpposite(dir, pullDirection)) {
            return dir;
        }

        return pullDirection;
    }

    private Snake.Direction directionToward(int dx, int dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            if (dx > 0) return Snake.Direction.RIGHT;
            if (dx < 0) return Snake.Direction.LEFT;
        }

        if (dy > 0) return Snake.Direction.DOWN;
        if (dy < 0) return Snake.Direction.UP;

        return null;
    }

    private boolean isOpposite(Snake.Direction a, Snake.Direction b) {
        return (a == Snake.Direction.UP && b == Snake.Direction.DOWN)
                || (a == Snake.Direction.DOWN && b == Snake.Direction.UP)
                || (a == Snake.Direction.LEFT && b == Snake.Direction.RIGHT)
                || (a == Snake.Direction.RIGHT && b == Snake.Direction.LEFT);
    }

    private boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public SegmentStorage getBoard() {
        return board;
    }

    public java.util.List<SnakeAgent> getSnakes() {
        return java.util.Collections.unmodifiableList(snakes);
    }

    public int getScore() {
        return score;
    }

    public State getState() {
        return state;
    }

    public boolean isGameOver() {
        return state == State.GAME_OVER;
    }

    public boolean isWon() {
        return state == State.WIN;
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
//blackhole should not spawn all the time (be a bit random), dont spawn to close to player, pull not each tick but once every 2 ticks
//blackhole breaks self direction, should not kill but decrease size by 1