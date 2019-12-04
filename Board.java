import java.util.SortedSet;
import java.util.TreeSet;

public class Board {
	
	private long playerBoard;
	private long opponentBoard;
	private long[] maskArr;
	private boolean movesLeft;
	
	private static final int DIRECTIONCOUNT = 8;
	private static final int DIR_INCREMENTS[] = {8,9 ,1,-7,-8,-9,-1, 7};
	                                           //N,NW,W,SW, S,SE, E,NE
	private static final int DIR_BITS[] =       {1,2 ,4, 8,16,32,64,128};
	
	private static final long DIR_MASKS[] = {
			                                 0xFFFFFFFFFFFFFF00L, //North
			                                 0xFEFEFEFEFEFEFE00L, //NorthWest
			                                 0xFEFEFEFEFEFEFEFEL, //West
			                                 0x00FEFEFEFEFEFEFEL, //SouthWest
			                                 0x00FFFFFFFFFFFFFFL, //South
			                                 0x007F7F7F7F7F7F7FL, //SouthEast
			                                 0x7F7F7F7F7F7F7F7FL, //East
			                                 0x7F7F7F7F7F7F7F00L  //NorthEast
											};
	
	private static final long ALLBORDERMASK = 0xFF818181818181FFL;
	private static final long BORDER_MASKS[] = {
											    0xFF00000000000000L, //North
											    0x8080808080808080L, //West
											    0x00000000000000FFL, //South
											    0x0101010101010101L  //East
											   };
	
	private static final long CORNERSMASK = 0x8100000000000081L;
	private static final long XMOVES =      0x0042000000004200L;
	private static final long CMOVES =      0x4281000000008142L;
	
	/*
	 * board evaluator based on example found on github by github user expspace
	 * PositionEvaluator.java says created by NSPACE on 11/20/2016
	 * 
	 * 8 different square types and weights used in my version of the evaluation
	 * 
	 *      W7 W6 W3 W3 W3 W3 W6 W7
	 *      W6 W5 W2 W2 W2 W2 W5 W6
	 *      W3 W2 W4 W1 W1 W4 W2 W3
	 *      W3 W2 W1 W0 W0 W1 W2 W3
	 *      W3 W2 W1 W0 W0 W1 W2 W3
	 *      W3 W2 W4 W1 W1 W4 W2 W3
	 *      W6 W5 W2 W2 W2 W2 W5 W6
	 *      W7 W6 W3 W3 W3 W3 W6 W7
	 */
	private static final long EVAL_ARR[] = {
											0x0000001818000000L, //W0
											0x0000182424180000L, //W1
											0x003C424242423C00L, //W2
											0x3C0081818181003CL, //W3
											0x0000240000240000L, //W4
											0x0042000000004200L, //W5
											0x4281000000008142L, //W6
											0x8100000000000081L  //W7
	};
	private static final long EVAL_WEIGHT[] = {
											0,  //W0
											5,  //W1
											-5, //W2
											25, //W3
											10, //W4
											-30,//W5
											-15,//W6
											100 //W7
	};
	
	public Board(String player) 
	{
		//bottom right of board is LSB and the top left is the MSB
		if(player.equals("Black")) 
		{
			this.playerBoard = 0x0000000810000000L;
			this.opponentBoard = 0x0000001008000000L;
		}
		else {
			this.playerBoard = 0x0000001008000000L;
			this.opponentBoard = 0x0000000810000000L;
			
		}
		System.out.println("R "+player.charAt(0));
		
		//Build array to hold masks to check empty spaces for moves
		this.maskArr = new long[64];
		long mask = 0x8000000000000000L;
		for (int i=0; i<maskArr.length; i++)
		{
			maskArr[i] = mask;
			mask = mask>>>1;
		}
		this.movesLeft = true;
	}
	
	public Board(Board oldBoard)
	{
		this.playerBoard = oldBoard.playerBoard;
		this.opponentBoard = oldBoard.opponentBoard;
		this.maskArr = oldBoard.maskArr;
		this.movesLeft = oldBoard.movesLeft;
	}
	
	public int getBlackPieces(int player)
	{
		if(player == 1)
		{
			return bitCount(playerBoard);
		}
		else
		{
			return bitCount(opponentBoard);
		}
	}
	
	public long generateMoves(int player, long playerDisks, long opponentDisks)
	{
		long emptyMask = ~playerDisks & ~opponentDisks;
		long holdMask, dirMoveMask, moveMask = 0;
		for(int i=0; i<DIRECTIONCOUNT; i++)
		{
			holdMask = playerDisks;
			if(DIR_INCREMENTS[i]>0)
			{
				holdMask = (holdMask << DIR_INCREMENTS[i]) & DIR_MASKS[i];
			}
			else
			{
				holdMask = (holdMask >>> -DIR_INCREMENTS[i]) & DIR_MASKS[i];
			}
			holdMask = holdMask & opponentDisks;
			for(int j=0; ((j<6)&(holdMask!=0L)); j++)
			{
				if(DIR_INCREMENTS[i]>0)
				{
					holdMask = (holdMask << DIR_INCREMENTS[i]) & DIR_MASKS[i];
				}
				else
				{
					holdMask = (holdMask >>> -DIR_INCREMENTS[i]) & DIR_MASKS[i];
				}
				dirMoveMask = holdMask & emptyMask;
				moveMask = moveMask | dirMoveMask;
				holdMask = holdMask & ~dirMoveMask & opponentDisks;
			}
		}
		return moveMask;
	}
	
	public boolean movesLeft()
	{
		long playerMoves = this.generateMoves(1, this.playerBoard, this.opponentBoard);
		long opponentMoves = this.generateMoves(0, this.opponentBoard, this.playerBoard);
		if(playerMoves == 0x0L && opponentMoves == 0x0L)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public int bitCount(long mask)
	{
		int count = 0;
		long holdMask = mask;
		for(int i=0; i<64; i++)
		{
			if((holdMask & 1L) != 0)
			{
				count++;
			}
			holdMask = holdMask >>> 1;
		}
		return count;
	}
	
	public long computeFlipMask(long currentMoveMask, long playerDisks, long opponentDisks)
	{
		long holdMask, dirFlipMask, flipMask;
		boolean atLeastOneFlip;
		flipMask = 0L;
		for(int j=0; j<DIRECTIONCOUNT; j++)
		{
			holdMask = currentMoveMask;
			dirFlipMask = 0L;
			if(DIR_INCREMENTS[j] > 0)
			{
				holdMask = (holdMask << DIR_INCREMENTS[j]) & DIR_MASKS[j];
			}
			else
			{
				holdMask = (holdMask >>> -DIR_INCREMENTS[j]) & DIR_MASKS[j];
			}
			atLeastOneFlip = false;
			while((holdMask != 0L) && ((holdMask & opponentDisks) != 0L))
			{
				atLeastOneFlip = true;
				dirFlipMask = dirFlipMask | holdMask;
				if(DIR_INCREMENTS[j] > 0)
				{
					holdMask = (holdMask << DIR_INCREMENTS[j]) & DIR_MASKS[j];
				}
				else
				{
					holdMask = (holdMask >>> -DIR_INCREMENTS[j]) & DIR_MASKS[j];
				}
				if(((holdMask & playerDisks) != 0L) && atLeastOneFlip)
				{
					flipMask = flipMask | dirFlipMask;
				}
			}
		}
		return flipMask;
	}
	
	public SortedSet<Move> seperateMoves(long moveMask, long playerDisks, long opponentDisks)
	{
		SortedSet<Move> moveList = new TreeSet();
		Move currentMove;
		long currentMoveMask = 1L, flipMask;
		boolean atLeastOneFlip;
		
		for(int i=0; i<64; i++)
		{
			if((currentMoveMask & moveMask) != 0L)
			{
				flipMask = computeFlipMask(currentMoveMask, playerDisks, opponentDisks);
				currentMove = new Move(currentMoveMask, flipMask);
				currentMove.diskCount = bitCount(flipMask);
				if((currentMoveMask & CORNERSMASK) != 0L)
				{
					currentMove.rank = i;
				}
				else
				{
					if((currentMoveMask & XMOVES) != 0L)
					{
						currentMove.rank = 100000 + i;
					}
					else
					{
						if((currentMoveMask & CMOVES) != 0L)
						{
							currentMove.rank = 10000 + i;
						}
						else
						{
							if((currentMoveMask & ALLBORDERMASK) != 0)
							{
								currentMove.rank = 1000 + i;
							}
							else
							{
								currentMove.rank = 100 + i;
							}
						}
					}
				}
				moveList.add(currentMove);
			}
			currentMoveMask = currentMoveMask << 1;
		}
		//this.printBitBoard(moveMask);
		return moveList;
	}
	
	public SortedSet<Move> generateMoves(int player, boolean sortByValue)
	{
		long playerDisks, opponentDisks;
		
		if(player == 1)
		{
			playerDisks = playerBoard;
			opponentDisks = opponentBoard;
		}
		else
		{
			playerDisks = opponentBoard;
			opponentDisks = playerBoard;
		}
		
		Move.sortByValue = sortByValue;
		long moveMask = generateMoves(player, playerDisks, opponentDisks);
		return seperateMoves(moveMask, playerDisks, opponentDisks);
	}
	
	public void applyMove(int player, Move move)
	{
		if(player == 1)
		{
			this.playerBoard = playerBoard | (move.getPosition()|move.getFlip());
			this.opponentBoard = opponentBoard & ~(move.getFlip());
		}
		else
		{
			this.opponentBoard = opponentBoard | move.getPosition()|move.getFlip();
			this.playerBoard = playerBoard & ~(move.getFlip());
		}
	}
	
	public double evaluate()
	{
		double totalValue = 0;
		for(int i=0; i<EVAL_ARR.length; i++)
		{
			int n = Long.bitCount(EVAL_ARR[i]&this.playerBoard);
			totalValue += n*EVAL_WEIGHT[i];
			n = Long.bitCount(EVAL_ARR[i]&this.opponentBoard);
			totalValue -= n*EVAL_WEIGHT[i];
		}
		return totalValue;
	}
	
	public Move alphaBeta(Board currentBoard, int ply,int player, double alpha, double beta, int maxDepth)
	{   
		if (Game.timeUP) {
			Move returnMove = Move.PASSMOVE;
			returnMove.value = alpha;
		}
			
			
		if(ply >= maxDepth)
		{
			Move returnMove = Move.PASSMOVE;
			returnMove.value = currentBoard.evaluate();
			return returnMove;
		}
		else
		{
			SortedSet<Move> moves = currentBoard.generateMoves(player, true);
			if(moves.size() == 0) {
				Move pass = Move.PASSMOVE;
				moves.add(pass);
			}
			Move bestMove = moves.first();
			for(Move move : moves)
			{
				Board newBoard = new Board(currentBoard);
				newBoard.applyMove(player, move);
				Move tempMove = alphaBeta(newBoard, ply+1, -player, -beta, -alpha, maxDepth);
				if (Game.timeUP && maxDepth >2) {
					Move returnMove = Move.PASSMOVE;
					returnMove.value = alpha;
				}
				move.value = -tempMove.value;
				if(move.value > alpha)
				{
					bestMove = move;
					alpha = move.value;
					if(alpha > beta)
					{
						return bestMove;
					}
				}
			}
			return bestMove;
		}
	}
	
	public Move chooseMove()
	{
		double alpha = Double.MIN_VALUE;
		double beta = Double.MAX_VALUE;
		Move holdMove = null;
		Move  prevMove = null;
		int maxDepth = 2;
		while(!Game.timeUP) {
			holdMove = alphaBeta(this, 0, 1, alpha, beta, maxDepth);
			if (Game.timeUP && maxDepth > 2) {
				return prevMove;
			}
			prevMove = holdMove;	
			maxDepth += 2;
		}
		
		Move move = Move.PASSMOVE;
		return move;
	}
	
	public Move getOpponentMove(String str)
	{
		if(str.length() == 1)
		{
			return Move.PASSMOVE;
		}
		else
		{
			int col = 0;
			if(str.charAt(2) == 'a')
			{
				col = 1;
			}
			else if(str.charAt(2) == 'b')
			{
				col = 2;
			}
			else if(str.charAt(2) == 'c')
			{
				col = 3;
			}
			else if(str.charAt(2) == 'd')
			{
				col = 4;
			}
			else if(str.charAt(2) == 'e')
			{
				col = 5;
			}
			else if(str.charAt(2)=='f')
			{
				col = 6;
			}
			else if(str.charAt(2)=='g')
			{
				col = 7;
			}
			else if(str.charAt(2)=='h')
			{
				col = 8;
			}
		
			int row = Character.getNumericValue(str.charAt(4));
			int position = (col-1) + ((row-1)*8);
			//System.out.println("Position of Move "+position);
			//System.out.println("Move bitboard: "+String.format("%16x", maskArr[position]));
			long pos = maskArr[position];
		
			long flipped = computeFlipMask(pos, opponentBoard, playerBoard);
		
			Move move = new Move(pos,flipped);
			return move;
		}
	}

	public long getPlayerBoard()
	{
		return playerBoard;
	}
	public long getOpponentBoard()
	{
		return opponentBoard;
	}
	
	public void printBitBoard(long bitboard) 
	{
		int index = 1;
		StringBuilder sb = new StringBuilder();
		sb.append("C   a b c d e f g h \n");
		sb.append("C "+index+"|");
		index++;
		for(int i=0; i<maskArr.length; i++) 
		{                        
			if((bitboard & maskArr[i]) != 0)
			{
				sb.append("1 ");
			}
			else
			{
				sb.append("- ");
			}
			
			if((i+1)%8 == 0)
			{
				if(index <= 8)
				{
					sb.append("\nC "+index+"|");
					index++;
				}
			}
		}
		System.out.println(sb);
	}
	
	public void printBoard(String color) 
	{
		int index = 1;
		StringBuilder sb = new StringBuilder();
		sb.append("C   a b c d e f g h \n");
		sb.append("C "+index+"|");
		index++;
		for(int i=0; i<maskArr.length; i++) 
		{
			if(color.equals("Black"))
			{                         
				if((playerBoard & maskArr[i]) != 0)
				{
					sb.append("B ");
				}
				else if((opponentBoard & maskArr[i]) != 0)
				{
					sb.append("W ");
				}
				else
				{
					sb.append("- ");
				}
			}
			else
			{
				if((playerBoard & maskArr[i]) != 0)
				{
					sb.append("W ");
				}
				else if((opponentBoard & maskArr[i]) != 0)
				{
					sb.append("B ");
				}
				else
				{
					sb.append("- ");
				}
			}
			
			if((i+1)%8 == 0)
			{
				if(index <= 8)
				{
					sb.append("\nC "+index+"|");
					index++;
				}
			}
		}
		//System.out.println("C Player board:  "+String.format("%16x", playerBoard));
		//System.out.println("C Opponent board:  "+String.format("%16x", opponentBoard));
		System.out.println(sb);
	}
}
