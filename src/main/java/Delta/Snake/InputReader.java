package Delta.Snake;

import java.util.Scanner;

public final class InputReader implements Runnable {
    private final DirectionController controller;

    public InputReader(DirectionController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);

        while (!controller.isQuitRequested()) {
            String line = sc.nextLine();

            if (line == null || line.isEmpty()) {
                continue;
            }

            char c = Character.toLowerCase(line.charAt(0));

            switch (c) {
                case 'w' -> controller.offerDirection(Snake.Direction.UP);
                case 's' -> controller.offerDirection(Snake.Direction.DOWN);
                case 'a' -> controller.offerDirection(Snake.Direction.LEFT);
                case 'd' -> controller.offerDirection(Snake.Direction.RIGHT);
            }
        }
    }
}