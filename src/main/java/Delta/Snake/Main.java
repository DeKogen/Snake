package Delta.Snake;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        DirectionController controller = new DirectionController();
        Game game = new Game(30, 15, controller);
        ConsoleRenderer renderer = new ConsoleRenderer();

        Thread inputThread = new Thread(new InputReader(controller));
        inputThread.setDaemon(true);
        inputThread.start();

        int tickMs = 250;

        renderer.render(game);

        boolean extraBotAdded = false;

        while (game.isRunning()) {
            game.tick();

            if (!extraBotAdded && game.getScore() >= 5) {
                game.addRandomBot(4);
                extraBotAdded = true;
            }

            renderer.render(game);
            Thread.sleep(tickMs);
        }

        renderer.render(game);
        System.out.println("bye");
    }
}