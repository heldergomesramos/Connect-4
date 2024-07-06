public class Sequence
{
    private int x, y;
    private int vx, vy;
    private static final int BIG_POSITIVE_NUMBER = 141414;
    private static final int BIG_NEGATIVE_NUMBER = -141414;
	
    /* Constructor */
    Sequence(int x, int y, char dir)
    {
        this.x = x;
        this.y = y;
        if(dir == 'H')
        {
            vx = 1;
            vy = 0;
        }
        else if(dir == 'V')
        {
            vx = 0;
            vy = 1;
        }
        else if(dir == 'U')
        {
            vx = 1;
            vy = -1;
        }
        else
        {
            vx = 1;
            vy = 1;
        }
    }

    public int getSequenceScore(Game game)
    {
        int counter = 0;
        char symbol = '-';
        int tx = x;
        int ty = y;
        for(int i = 0; i < 4; ++i, tx+=vx, ty+=vy)
        {                
            if(game.board[ty][tx] == '-')
                continue;
            if(game.board[ty][tx] == 'X')
            {
                if(symbol == 'O')
                    return 0;
                symbol = 'X';
                --counter;
            }
            else
            {
                if(symbol == 'X')
                    return 0;
                symbol = 'O';
                ++counter;
            }
        }
        return scoreFormula(counter);
    }

    public int scoreFormula(int counter)
    {
        switch(counter)
        {
            case -4: return BIG_NEGATIVE_NUMBER;
            case -3: return -50;
            case -2: return -10;
            case -1: return -1;
            case 0: return 0;
            case 1: return 1;
            case 2: return 10;
            case 3: return 50;
            case 4: return BIG_POSITIVE_NUMBER;
        }
        return 0;
    }
}
