import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;

public class Algorithm
{
    private static final byte C_SIZE = 7;
    private static final byte MINIMAX_DEPTH_LIMIT = 7;

    public static int expandedNodes;

    /* Exclusive for MCTS */
    private static final byte MCTS_CONSTANT = 2;
    private static final int MCTS_TIME_LIMIT = 1414;

    /* Exclusive for MiniMax */
    private static int bestMove;
    private static int bestValue;

    /* ======================================================================================= 
    ** MINIMAX
    */
    public static int miniMax(Game game, boolean pruning)
    {
        bestValue = Integer.MIN_VALUE;
        if(pruning) maxPruning(game, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        else        max(game, 0);
        return bestMove;
    }
    public static int max(Game game, int depth)
    {
        if(depth == MINIMAX_DEPTH_LIMIT || game.isOver()) 
            return game.score;
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < C_SIZE; i++)
        {
            if(game.canInsert(i))
            {
                expandedNodes++;
                Game clone = game.insertClone(i);
                bestScore = Math.max(bestScore, min(clone, depth + 1));
                if(depth == 0 && bestScore > bestValue)
                {
                    bestMove = clone.lastMove;
                    bestValue = bestScore;
                }
            }
        }
        return bestScore;
    }
    public static int min(Game game, int depth)
    {
        if(depth == MINIMAX_DEPTH_LIMIT || game.isOver()) 
            return game.score;
        int bestScore = Integer.MAX_VALUE;
        for (int i = 0; i < C_SIZE; i++)
        {
            if(game.canInsert(i))
            {
                expandedNodes++;
                Game clone = game.insertClone(i);
                bestScore = Math.min(bestScore, max(clone, depth + 1));
            }
        }
        return bestScore;
    }
    public static int maxPruning(Game game, int depth, int alpha, int beta)
    {
        if(depth == MINIMAX_DEPTH_LIMIT || game.isOver()) 
            return game.score;
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < C_SIZE; i++)
        {
            if(game.canInsert(i))
            {
                expandedNodes++;
                Game clone = game.insertClone(i);
                bestScore = Math.max(bestScore, minPruning(clone, depth + 1, alpha, beta));
                if(bestScore >= beta) 
                    return bestScore;
                alpha = Math.max(bestScore, alpha);
                if(depth == 0 && bestScore > bestValue)
                {
                    bestMove = clone.lastMove;
                    bestValue = bestScore;
                }
            }
        }
        return bestScore;
    }
    public static int minPruning(Game game, int depth, int alpha, int beta)
    {
        if(depth == MINIMAX_DEPTH_LIMIT || game.isOver()) 
            return game.score;
        int bestScore = Integer.MAX_VALUE;
        for (int i = 0; i < C_SIZE; i++)
        {
            if(game.canInsert(i))
            {
                expandedNodes++;
                Game clone = game.insertClone(i);
                bestScore = Math.min(bestScore, maxPruning(clone, depth + 1, alpha, beta));
                if(bestScore <= alpha)
                    return bestScore;
                beta = Math.min(bestScore, beta);
            }
        }
        return bestScore;
    }

    /* END OF MINIMAX
    ** =======================================================================================
    */

    /* =======================================================================================
    ** MONTE CARLO TREE SEARCH
    **/
    public static int mcts(Game game)
    {
        if(game.nVisited == 0)
            game.nVisited++;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(game);
        DefaultMutableTreeNode cur = root;
        int i = 0;
        for(; System.currentTimeMillis() - Game.startTime < MCTS_TIME_LIMIT; i++) //Condição para parar o mcts (pode ser tempo ou numero de iterações)
        {
            cur = root;
            Game curGame = (Game) cur.getUserObject();
            while(true) //Loop até achar folha
            {
                if(cur.isLeaf())
                {
                    curGame = (Game) cur.getUserObject();
                    if(curGame.nVisited != 0 && !curGame.isFull())
                    {
                        expandTree(cur);
                        cur = (DefaultMutableTreeNode) cur.getChildAt(0);
                    }
                    backPropagation(cur, rollout(cur));
                    break;
                }
                else
                    cur = getBestChild(cur);
            }
        }
        int bestMove = getBestMoveIndexMCTS(root);
        Game.iterations = i;
        Game gameTest = game.insertClone(bestMove);
        if(inDanger(gameTest))
            bestMove = emergencyDefense(game);
        return bestMove;
    }

    public static void expandTree(DefaultMutableTreeNode root)
    {
        for (int i = 0; i < C_SIZE; i++)
        {
            Game curGame = (Game) root.getUserObject();
            curGame = curGame.myClone();
            if (!curGame.canInsert(i)) continue;
            expandedNodes++;
            curGame.insert(i);
            DefaultMutableTreeNode temp = new DefaultMutableTreeNode(curGame);
            root.add(temp);
        }
    }

    public static double rollout(DefaultMutableTreeNode cur)
    {
        int index;
        Game curGame = (Game) cur.getUserObject();
        DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode(curGame); //Auxiliary tree
        while(!curGame.isOver())
        {
            index = getRandomIndex(curGame);
            Game childGame = curGame.myClone();
            childGame.insert(index);
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(childGame);
            tempNode.add(child);
            tempNode = (DefaultMutableTreeNode) tempNode.getChildAt(0);
            curGame = childGame;
        }
        if(curGame.state == 'X')
            return 0;
        if(curGame.state == 'O')
            return 1;
        return 0.5;
    }

    public static void backPropagation(DefaultMutableTreeNode cur, double value)
    {
        while(true)
        {
            Game curGame = (Game) cur.getUserObject();
            curGame.tVal += value;
            ++curGame.nVisited;
            curGame.updateAvg();
            if(cur.getParent() != null)
                cur = (DefaultMutableTreeNode) cur.getParent();
            else
                return;
        }
    }

    public static DefaultMutableTreeNode getBestChild(DefaultMutableTreeNode cur)
    {  
        double max = 0;
        int index = 0;
        for (int i = 0; i < cur.getChildCount(); i++)
        {
            double ucb = ucb((DefaultMutableTreeNode) cur.getChildAt(i));
            if (ucb > max)
            {
                max = ucb;
                index = i; // In this case it gets the first child with maximum ucb
            }
        }
        return (DefaultMutableTreeNode) cur.getChildAt(index);
    }

    public static int getBestMoveIndexMCTS(DefaultMutableTreeNode root)
    {
        double max = 0;
        int index = 0;
        for (int i = 0; i < root.getChildCount(); i++) 
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            Game childGame = (Game) child.getUserObject();
            if(childGame.avg > max)
            {
                index = childGame.lastMove;
                max = childGame.avg;
            }
        }
        return index;
    }

    public static int getRandomIndex(Game game)
    {
        LinkedList<Integer> available = new LinkedList<>();
        for (int i = 0; i < C_SIZE; i++)
            if(game.canInsert(i))
                available.add(i);
        return available.get((int) (Math.random() * available.size()));
    }

    public static double ucb(DefaultMutableTreeNode cur)
    {
        Game game = (Game) cur.getUserObject();
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) cur.getParent();
        Game parent = (Game) parentNode.getUserObject();
        game.updateAvg();
        parent.updateAvg();
        if(game.nVisited == 0)
            return Double.POSITIVE_INFINITY;
        return game.avg + MCTS_CONSTANT * (Math.sqrt(Math.log((double) parent.nVisited / (double) game.nVisited)));
    }

    public static boolean inDanger(Game game)
    {
        for (int i = 0; i < C_SIZE; i++)
            if(game.canInsert(i))
                if(game.insertClone(i).state == 'X')
                    return true;
        return false;
    }

    public static int emergencyDefense(Game game)
    {
        for (int i = 0; i < C_SIZE; i++)
            if(game.canInsert(i))
                if(!inDanger(game.insertClone(i)))
                    return i;
        return -1;
    }

    /* END OF MONTE CARLO TREE SEARCH
    ** =======================================================================================
    */
}