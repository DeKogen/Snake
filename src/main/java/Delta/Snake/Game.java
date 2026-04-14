package Delta.Snake;

import Delta.Map.SegmentStorage;
import Delta.Map.SegmentStorage.Coord;
import Delta.Map.SegmentStorage.SegmentType;

import java.util.List;

public final class Game {

    public enum State {
        RUNNING,
        GAME_OVER,
        WIN
    }

    private final int width;
    private final int height;
    private final SegmentStorage board;
    private final Snake snake;

    private State state = State.RUNNING;
    private int score = 0;

    public Game(int width, int height) {
        if (width < 7 || height < 7) {
            throw new IllegalArgumentException("Board is too small");
        }

        this.width = width;
        this.height = height;
        this.board = new SegmentStorage(width, height);

        int headX = width / 2;
        int headY = height / 2;

        this.snake = new Snake(headX, headY, 3, Snake.Direction.RIGHT);

        placeBorderWalls();
        placeInitialSnake();

        if (!spawnApple()) {
            state = State.WIN;
        }
    }

    public boolean tick(Snake.Direction dir) {
        if (state != State.RUNNING) {
            return false;
        }

        Coord next = snake.nextHead(dir);

        if (!isInside(next.x(), next.y())) {
            state = State.GAME_OVER;
            return false;
        }

        SegmentType nextCell = board.get(next.x(), next.y());
        boolean grow = nextCell == SegmentType.APPLE;

        if (nextCell == SegmentType.WALL) {
            state = State.GAME_OVER;
            return false;
        }

        if (snake.wouldHitItself(next.x(), next.y(), grow)) {
            state = State.GAME_OVER;
            return false;
        }

        Snake.MoveResult move = snake.move(dir, grow);

        Coord oldHead = move.oldHead();
        Coord newHead = move.newHead();
        Coord removedTail = move.removedTail();

        if (removedTail != null && !removedTail.equals(newHead)) {
            board.put(removedTail.x(), removedTail.y(), SegmentType.EMPTY);
        }

        if (!oldHead.equals(newHead)) {
            if (snake.occupies(oldHead.x(), oldHead.y())) {
                board.put(oldHead.x(), oldHead.y(), SegmentType.SNAKE_BODY);
            } else {
                board.put(oldHead.x(), oldHead.y(), SegmentType.EMPTY);
            }
        }

        board.put(newHead.x(), newHead.y(), SegmentType.SNAKE_HEAD);

        if (grow) {
            score++;

            if (!spawnApple()) {
                state = State.WIN;
                return false;
            }
        }

        return true;
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

    private void placeInitialSnake() {
        List<Coord> segments = snake.segments();

        for (int i = 0; i < segments.size(); i++) {
            Coord c = segments.get(i);

            board.put(
                    c.x(),
                    c.y(),
                    i == 0 ? SegmentType.SNAKE_HEAD : SegmentType.SNAKE_BODY
            );
        }
    }

    private boolean spawnApple() {
        Coord c = board.getRandomByType(SegmentType.EMPTY);
        if (c == null) {
            return false;
        }

        board.put(c.x(), c.y(), SegmentType.APPLE);
        return true;
    }

    private boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public SegmentStorage getBoard() {
        return board;
    }

    public Snake getSnake() {
        return snake;
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