package Delta.Snake;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Game game = new Game(30, 15);
        ConsoleRenderer renderer = new ConsoleRenderer();
        DirectionController controller = new DirectionController();

        Thread inputThread = new Thread(new InputReader(controller));
        inputThread.setDaemon(true);
        inputThread.start();

        int tickMs = 250;

        renderer.render(game);

        while (!game.isGameOver()) {
            Snake.Direction dir = controller.consumeDirection();
            game.tick(dir);
            renderer.render(game);
            Thread.sleep(tickMs);
        }

        renderer.render(game);
        System.out.println("bye");
    }
}