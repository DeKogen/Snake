package Delta.Snake;

public final class DirectionController {
    private Snake.Direction currentDirection = Snake.Direction.RIGHT;
    private Snake.Direction nextDirection = null;
    private Snake.Direction bufferedDirection = null;
    private boolean quitRequested = false;

    public synchronized void offerDirection(Snake.Direction newDirection) {
        if (newDirection == null) {
            return;
        }

        if (nextDirection == null) {
            if (!isOpposite(currentDirection, newDirection) && newDirection != currentDirection) {
                nextDirection = newDirection;
            }
            return;
        }

        if (!isOpposite(nextDirection, newDirection) && newDirection != nextDirection) {
            bufferedDirection = newDirection;
        }
    }

    public synchronized Snake.Direction consumeDirection() {
        Snake.Direction dirToUse = (nextDirection != null) ? nextDirection : currentDirection;

        currentDirection = dirToUse;
        nextDirection = bufferedDirection;
        bufferedDirection = null;

        return currentDirection;
    }



    public synchronized boolean isQuitRequested() {
        return quitRequested;
    }

    private boolean isOpposite(Snake.Direction a, Snake.Direction b) {
        return (a == Snake.Direction.UP && b == Snake.Direction.DOWN)
                || (a == Snake.Direction.DOWN && b == Snake.Direction.UP)
                || (a == Snake.Direction.LEFT && b == Snake.Direction.RIGHT)
                || (a == Snake.Direction.RIGHT && b == Snake.Direction.LEFT);
    }
}