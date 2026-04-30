package Delta.Snake;

public class BotOnlyMain {
    public static void main(String[] args) throws InterruptedException {
        Game game = new Game(30, 15, 5);
        ConsoleRenderer renderer = new ConsoleRenderer();

        renderer.render(game);

        while (game.isRunning()) {
            game.tick();
            renderer.render(game);

            if (!game.isRunning()) {
                break;
            }

            Thread.sleep(game.getCurrentTickDelayMs());
        }

        System.out.println("bot-only simulation ended");
    }
}