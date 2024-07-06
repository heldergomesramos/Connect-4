public class Main 
{
    public static void main(String[] args)
    {
        clrScreen();
        printMainScreen();
        while(true)
        {
            Game game = new Game();
            Game.spawnSequences();
            String input = Game.in.next();
            switch(input)
            {
                case "1": case "2": case "3": Game.pve(game,input.charAt(0)); printMainScreen(); break;
                case "0": clrScreen(); return;
                default: clrScreen(); printMainScreen(); System.out.println("Invalid input, make sure you type a number between 0 and 3."); break;
            }
        }
    }

    public static void printMainScreen()
    {
        System.out.println("CONNECT FOUR");
        System.out.println("Choose an algorithm:");
        System.out.println("[1] Minimax");
        System.out.println("[2] Alpha-Beta Pruning");
        System.out.println("[3] Monte Carlo Tree Search");
        System.out.println("[0] Exit");
    }

    public static void clrScreen()
    {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}