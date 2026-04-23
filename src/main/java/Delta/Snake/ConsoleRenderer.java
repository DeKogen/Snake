package Delta.Snake;

import Delta.Map.SegmentStorage;
import Delta.Map.SegmentStorage.Coord;
import Delta.Map.SegmentStorage.SegmentType;

public final class ConsoleRenderer {

    public void render(Game game) {
        clearScreen();

        SegmentStorage board = game.getBoard();
        int width = game.getWidth();
        int height = game.getHeight();

        Coord apple = game.getApple();
        Coord blackHole = game.getBlackHole();

        StringBuilder sb = new StringBuilder();
        sb.append("Score: ").append(game.getScore())
                .append("  RageTicks: ").append(game.getPlayerRageTicks())
                .append("  QueuedGrowth: ").append(game.getPlayerQueuedGrowth())
                .append("  Delay: ").append(game.getCurrentTickDelayMs()).append("ms")
                .append('\n');

        sb.append('+');
        for (int x = 0; x < width; x++) {
            sb.append('-');
        }
        sb.append("+\n");

        for (int y = 0; y < height; y++) {
            sb.append('|');

            for (int x = 0; x < width; x++) {
                if (blackHole != null && blackHole.x() == x && blackHole.y() == y) {
                    sb.append('O');
                    continue;
                }

                if (apple != null && apple.x() == x && apple.y() == y && game.getAppleType() == Game.AppleType.RAGE) {
                    sb.append('!');
                    continue;
                }

                SegmentType type = board.get(x, y);
                sb.append(symbolFor(type, x, y));
            }

            sb.append("|\n");
        }

        sb.append('+');
        for (int x = 0; x < width; x++) {
            sb.append('-');
        }
        sb.append("+\n");

        if (game.isGameOver()) {
            sb.append("GAME OVER\n");
        } else if (game.isWon()) {
            sb.append("YOU WIN\n");
        }

        System.out.print(sb);

        if (game.isGameOver()) {
            printGameOver(game.getScore());
        } else if (game.isWon()) {
            printWin(game.getScore());
        }
    }

    private void printGameOver(int score) {
        System.out.println();
        System.out.println("  ██████   █████  ███    ███ ███████      █████  ██    ██ ███████ ██████  ");
        System.out.println(" ██       ██   ██ ████  ████ ██          ██   ██ ██    ██ ██      ██   ██ ");
        System.out.println(" ██   ███ ███████ ██ ████ ██ █████       ██   ██ ██    ██ █████   ██████  ");
        System.out.println(" ██    ██ ██   ██ ██  ██  ██ ██          ██   ██  ██  ██  ██      ██   ██ ");
        System.out.println("  ██████  ██   ██ ██      ██ ███████      █████    ████   ███████ ██   ██ ");
        System.out.println();
        System.out.println("Score: " + score);
    }

    private void printWin(int score) {
        System.out.println();
        System.out.println(" ██    ██  ██████  ██    ██     ██     ██ ██ ███    ██ ");
        System.out.println("  ██  ██  ██    ██ ██    ██     ██     ██ ██ ████   ██ ");
        System.out.println("   ████   ██    ██ ██    ██     ██  █  ██ ██ ██ ██  ██ ");
        System.out.println("    ██    ██    ██ ██    ██     ██ ███ ██ ██ ██  ██ ██ ");
        System.out.println("    ██     ██████   ██████       ███ ███  ██ ██   ████ ");
        System.out.println();
        System.out.println("Score: " + score);
        System.out.println("You consumed the entire system.");
    }

    private char symbolFor(SegmentType type, int x, int y) {
        return switch (type) {
            case EMPTY -> ((x + y) % 2 == 0) ? '-' : ' ';
            case WALL -> '#';
            case SNAKE_BODY -> 'o';
            case SNAKE_HEAD -> '@';
            case APPLE -> '*';
        };
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}