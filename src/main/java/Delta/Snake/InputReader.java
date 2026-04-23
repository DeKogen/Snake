package Delta.Snake;

import java.io.IOException;
import java.io.InputStream;

public final class InputReader implements Runnable, AutoCloseable {
    private final DirectionController controller;
    private volatile boolean running = true;

    public InputReader(DirectionController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            enableRawMode();

            InputStream in = System.in;

            while (running) {
                int ch = in.read();
                if (ch == -1) {
                    break;
                }

                char c = Character.toLowerCase((char) ch);

                switch (c) {
                    case 'w' -> controller.offerDirection(Snake.Direction.UP);
                    case 's' -> controller.offerDirection(Snake.Direction.DOWN);
                    case 'a' -> controller.offerDirection(Snake.Direction.LEFT);
                    case 'd' -> controller.offerDirection(Snake.Direction.RIGHT);
                    case 'q' -> running = false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Input reader failed", e);
        } finally {
            try {
                disableRawMode();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void close() {
        running = false;
        try {
            disableRawMode();
        } catch (Exception ignored) {
        }
    }

    private static void enableRawMode() throws IOException {
        runCommand("sh", "-c", "stty -icanon -echo min 1 time 0 < /dev/tty");
    }

    private static void disableRawMode() throws IOException {
        runCommand("sh", "-c", "stty sane < /dev/tty");
    }

    private static void runCommand(String... command) throws IOException {
        try {
            Process process = new ProcessBuilder(command)
                    .inheritIO()
                    .start();

            int exit = process.waitFor();
            if (exit != 0) {
                throw new IOException("Command failed, exit code: " + exit);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while running command", e);
        }
    }
}