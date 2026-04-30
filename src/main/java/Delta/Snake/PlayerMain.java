package Delta.Snake;

public class PlayerMain {
    public static void main(String[] args) throws InterruptedException {
        int nextbot = 0;

        DirectionController controller = new DirectionController();
        Game game = new Game(30, 15, controller);
        ConsoleRenderer renderer = new ConsoleRenderer();

        Thread inputThread = new Thread(new InputReader(controller));
        inputThread.setDaemon(true);
        inputThread.start();

        renderer.render(game);

        while (game.isRunning()) {
            game.tick();
            renderer.render(game);

            if (!game.isRunning()) {
                break;
            }

            if (game.getScore() >= nextbot) {
                game.addRandomBot((game.getScore() /2), new SmortBotController());
                nextbot += 100;
            }

            Thread.sleep(game.getCurrentTickDelayMs());
        }

        System.out.println("bye");
    }
}