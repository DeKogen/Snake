package Delta.Snake;

import Delta.Map.SegmentStorage;
import Delta.Map.SegmentStorage.SegmentType;

public final class ConsoleRenderer {

    public void render(Game game) {
        clearScreen();

        SegmentStorage board = game.getBoard();
        int width = game.getWidth();
        int height = game.getHeight();

        StringBuilder sb = new StringBuilder();
        sb.append("Score: ").append(game.getScore()).append('\n');

        sb.append('+');
        for (int x = 0; x < width; x++) {
            sb.append('-');
        }
        sb.append("+\n");

        for (int y = 0; y < height; y++) {
            sb.append('|');

            for (int x = 0; x < width; x++) {
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
        }

        System.out.print(sb);

        if (game.isGameOver()) {
            sb.append("GAME OVER\n");
        }

        System.out.print(sb);

        if (game.isGameOver()) {
            printGameOver(game.getScore());
        }

    }

    private void printGameOver(int score) {
        System.out.println();
        System.out.println("  ██████   █████  ███    ███ ███████     ██████  ██    ██ ███████ ██████  ");
        System.out.println(" ██       ██   ██ ████  ████ ██          ██   ██ ██    ██ ██      ██   ██ ");
        System.out.println(" ██   ███ ███████ ██ ████ ██ █████       ██   ██ ██    ██ █████   ██████  ");
        System.out.println(" ██    ██ ██   ██ ██  ██  ██ ██          ██   ██  ██  ██  ██      ██   ██ ");
        System.out.println("  ██████  ██   ██ ██      ██ ███████     ██████    ████   ███████ ██   ██ ");
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
//        for (int i = 0; i < 20; i++) System.out.println();
    }
}