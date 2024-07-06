import java.util.LinkedList;
import java.util.Scanner;

public class Game
{
    private static final byte R_SIZE = 6;
    private static final byte C_SIZE = 7;

    public char[][] board = new char[R_SIZE][C_SIZE];
    private byte[] curLevel = new byte[C_SIZE];
    public int curScore = 0;
    public char player = 'X';
    public int score = 0;
    public char state = ' ';
    public int nVisited = 0;
    public double tVal = 0;
    public double avg = 0;
    public int lastMove = -1;
    public static int iterations = 0;

    public static long startTime;
	private static long timeElapsed = 0;

    public static LinkedList<Sequence> sequenceList = new LinkedList<>();

    public static final Scanner in = new Scanner(System.in);

    /* Constructor */
    public Game()
    {
        for(int i = 0; i < R_SIZE; i++)
        {
            for(int j = 0; j < C_SIZE; j++)
            {
                board[i][j] = '-';
                curLevel[j] = R_SIZE - 1;
            }
        }
    }
    

    /* ======================================================================================= 
    ** BOARD
    ** Board-Related Code
    */
    public void printBoard()
    {
        System.out.println("  1234567");
        for(int i = 0; i < R_SIZE; i++)
        {
            System.out.print(i + 1 + " ");
            for(int j = 0; j < C_SIZE; j++)
                System.out.print(board[i][j]);
            System.out.println();
        }
        System.out.println();
    }

    public void printBoard(char player)
    {
        printBoard();
        if(state == ' ')
        {
            System.out.println("It is now " + player + "'s turn.");
            System.out.println("Make a move by choosing your coordinates to play.");
        }
    }

    public static Game readBoard()
    {
        Game game = new Game();
        int xNum = 0;
        int oNum = 0;
        String[] lines = new String[R_SIZE];
        for (int i = R_SIZE - 1; i >= 0; i--)
            lines[i] = in.next();

        for(int i = 0; i < R_SIZE; i++)
        {
            for(int j = 0; j < C_SIZE; j++)
            {
                char c = lines[i].charAt(j);
                if(c == 'O' || c == 'X')
                    game.insertReadBoard(c,j);
                if(c == 'O')
                    oNum++;
                if(c == 'X')
                    xNum++;
            }
        }
        if(xNum == oNum)
            game.player = 'X';
        else
            game.player = 'O';
        game.updateState();
        return game;
    }

    public void insert(int col)
    {
        lastMove = col;
        board[curLevel[col]][col] = player;
        curLevel[col]--;
        nextTurn();
        updateState();
    }

    public Game insertClone(int col)
    {
        Game clone = myClone();
        clone.insert(col);
        return clone;
    }

    public void insertReadBoard(char player, int col)
    {
        lastMove = col;
        board[curLevel[col]][col] = player;
        curLevel[col]--;
    }

    public Game insertReadBoardClone(char player, int col) 
    {
        Game clone = myClone();
        clone.insertReadBoard('X', col);
        return clone;
    }

    public boolean canInsert(int col)
    {
        return !(col >= C_SIZE || col < 0 || curLevel[col] < 0);
    }

    public Game myClone()
    {
        Game clone = new Game();
        for (int i = 0; i < R_SIZE; ++i)
            for (int j = 0; j < C_SIZE; ++j)
            {
                clone.board[i][j] = board[i][j];
                clone.curLevel[j] = curLevel[j];
            }
        clone.player = player;
            
        return clone;
    }

    public boolean isFull()
    {
        for(int j = 0; j < C_SIZE; j++)
            if(board[0][j] == '-')
                return false;
        return true;
    }

    public boolean isOver()
    {
        return state != ' ';
    }

    public static void spawnSequences()
    {
        for(int i = 0; i < R_SIZE; i++)
            for(int j = 0; j < C_SIZE - 3; j++)
                sequenceList.add(new Sequence(j, i, 'H'));
        for(int i = 0; i < R_SIZE - 3; i++)
            for(int j = 0; j < C_SIZE; j++)
                sequenceList.add(new Sequence(j, i, 'V'));
        for(int i = R_SIZE - 3; i < R_SIZE; i++)
            for(int j = 0; j < C_SIZE - 3; j++)
                sequenceList.add(new Sequence(j, i, 'U'));
        for(int i = 0; i < R_SIZE - 3; i++)
            for(int j = 0; j < C_SIZE - 3; j++)
                sequenceList.add(new Sequence(j, i, 'D'));
    }

    public int getScore()
    {
        int sc = 0;
        for(Sequence sequence:sequenceList)
            sc+= sequence.getSequenceScore(this);
        return sc;
    }

    public void updateAvg()
    {
        avg = (double) tVal / (double) nVisited;
    }

    public void updateState()
    {
        if(isFull())
            state = '-';
        score = getScore();
        if(score >= 4000)
            state = 'O';
        else if(score <= -4000)
            state = 'X';
    }

    public void nextTurn()
    {
        player = player == 'X' ? 'O' : 'X';
    }

    public void printResults()
    {
        if(state == ' ')
            return;
        if(state == '-')
            System.out.println("Board is full, game ended in draw!");
        else
            System.out.println(state + " wins!");
    }
    /* END OF BOARD
    ** =======================================================================================
    */

    /* =======================================================================================
    ** VERSUS
    ** Reserved for whether you want to play against a player or the AI
    */

    public static void pvp(Game game)
    {
        game.printBoard(game.player);
        while(!game.isOver())
        {
            int move = in.nextInt() - 1;
            if(game.canInsert(move))
            {
                game.insert(move);
                game.printBoard(game.player);
            }
            else
            {
                game.printBoard(game.player);
                System.out.println("Can't insert!");
            }
        }
        game.printResults();
    }

    public static void printFirstPlayer()
    {
        System.out.println("Choose who plays first:");
        System.out.println("[X] You");
        System.out.println("[O] AI");
    }

    public static void pve(Game game, char algorithm)
    {
        Main.clrScreen();
        printFirstPlayer();
        while(true)
        {
            String input = in.next().toLowerCase();
            if(input.equals("x"))
            {
                game.player = 'X'; 
                break;
            }
            else if(input.equals("o"))
            {
                game.player = 'O'; 
                break;
            }
            else
            {
                Main.clrScreen();
                printFirstPlayer();
                System.out.println("Error: Invalid input, type either 'X' or 'O'");
            }
        }
        Main.clrScreen();
        game.printBoard(game.player);
        while(!game.isOver())
        {
            int move;
            if(game.player == 'X') move = in.nextInt() - 1;
            else 
            {
                Main.clrScreen();
                System.out.println("Thinking...");
                startTime = System.currentTimeMillis();
                Algorithm.expandedNodes = 0;
                switch(algorithm)
                {
                    case '1': move = Algorithm.miniMax(game, false); break;
                    case '2': move = Algorithm.miniMax(game, true); break;
                    case '3': move = Algorithm.mcts(game); break;
                    default: System.out.println("Error: Invalid algorithm"); return;
                }

                timeElapsed = System.currentTimeMillis() - startTime;
                Main.clrScreen();
                if(algorithm == '3')
                    System.out.println("Iterations: " + iterations);
                System.out.println("Time Elapsed: " + timeElapsed + "ms");
                System.out.println("Expanded nodes: " + Algorithm.expandedNodes);
            }
            if(game.canInsert(move))
            {
                game.insert(move);
                game.printBoard(game.player);
            }
            else
            {
                if(move == 13) //Read Board Mode for Debugging
                {
                    Main.clrScreen();
                    System.out.println("DEBUGGING MODE");
                    System.out.println("Insert game to read");
                    game = Game.readBoard();
                    Main.clrScreen();
                    game.printBoard(game.player);
                }
                else
                {
                    game.printBoard(game.player);
                    System.out.println("Error: Can't insert on column (0-6): " + move);
                }
            }
        }
        Main.clrScreen();
        game.printBoard(game.player);
        game.printResults();
        System.out.println("Type anything to continue.");
        in.next();
        Main.clrScreen();
    }

    /* END OF VERSUS
    ** =======================================================================================
    */ 
}