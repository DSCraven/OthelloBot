/*
 * author: Dustin Craven
 * date: 9/27/2019
 * class: CSCI 312 Artificial Intelligence
 */

import java.util.Scanner;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;

public class Game 
{	
	//Instance Variables for the game
	public static int ME = 1;
	public static int OPPONENT = -1;
	public static Scanner scan = new Scanner(System.in);
	public static int endGame;
	public static Board gameBoard;
	public static String color;
	public static boolean blackPiecesMatch;;
	public static int turnsPassed;
	public static int blackPieces;
	public static int moveNumber;
	
	//Instance Variables for the timer
	//array to represent percentage of time remaining
	static double timeAllocation[] = {0.015, 0.015, 0.015, 0.015, 0.025, 0.025, 0.025, 0.025, 0.025, 0.025,
            0.048,  0.048, 0.048, 0.048, 0.048, 0.048, 0.050, 0.051, 0.052, 0.053,
            0.044,  0.045, 0.049, 0.049, 0.049, 0.051, 0.053, 0.055, 0.057, 0.059,
            0.060, 0.060, 0.061, 0.062, 0.063, 0.064, 0.065, 0.065, 0.065, 0.065,
            0.167, 0.168, 0.169, 0.169, 0.171, 0.172, 0.173, 0.175, 0.180, 0.180,
            0.181, 0.187, 0.196, 0.199, 0.220, 0.220, 0.220, 0.220, 0.220, 0.220,
            0.220, 0.250, 0.250, 0.250, 0.250, 0.250, 0.250, 0.250, 0.250, 0.250
          };
	public static int timeRemaining;
	static Timer timer;
	public static boolean timeUP;
	
	public static boolean isInt(String str)
	{
		return str.matches("-?\\d+");
	}
	
	public static String input()
	{
		String str = scan.nextLine();
		Character controller;
		String myColor;
		if(isInt(str))
		{
			try
			{
				endGame = Integer.parseInt(str);
			}
			catch(NumberFormatException nfe)
			{
				System.out.println("C Invalid input.");
				input();
			}
			controller = 'n';
		}
		else
		{
			controller = str.charAt(0);
		}
		
		switch(controller)
		{
		case 'I':
			if(str.equals("I B")) 
			{
				myColor = "Black";
			}
			else {
				myColor = "White";
			}
			gameBoard = new Board(myColor);
			return myColor;
		case 'B':
			return str;
		case 'W':
			return str;
		case 'n':
			if(color.equals("Black"))
			{
				blackPieces = gameBoard.getBlackPieces(ME);
			}
			else
			{
				blackPieces = gameBoard.getBlackPieces(OPPONENT);
			}
			
			if(blackPieces == endGame)
			{
				blackPiecesMatch = true;
			}
			return null;
		}
		return null;
	}
	
	public static boolean gameOver()
	{
		if(!gameBoard.movesLeft() || turnsPassed > 2)
		{
			blackPieces = gameBoard.getBlackPieces(ME);
			System.out.println("n "+blackPieces);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static void main(String[] args) 
	{
		int currentPlayer;
		turnsPassed = 0;
		moveNumber = 0;
		timeRemaining = 600;
		Move move;

		String myColor = input();
		color = myColor;
		
		if(myColor.equals("Black"))
		{
			currentPlayer = ME;
		}
		else
		{
			currentPlayer = OPPONENT;
		}
		
		while(!gameOver())
		{
			if(currentPlayer == ME)
			{
				timeUP = false;
				timer = new Timer();
				
				int timeForMove = (int)(timeAllocation[moveNumber]*(double)timeRemaining);
				System.out.println("C Move Time: "+timeForMove);
				timer.schedule(new InterruptTask(), timeForMove*1000);
				
				move = gameBoard.chooseMove();
				if(move == null)
				{
					move = Move.PASSMOVE;
				}
				if(move.equals(Move.PASSMOVE))
				{
					System.out.println(myColor.charAt(0));
				}
				else
				{
					System.out.println(myColor.charAt(0)+""+move.toString());
				}
				
				if(!timeUP)
				{
					timer.cancel();
				}
				timeRemaining -= timeForMove;
				System.out.println("C Remaining Time: "+timeRemaining);
				
				moveNumber++;
			}
			else
			{
				String input = input();
				System.out.println("C I'm the input "+input);
				move = gameBoard.getOpponentMove(input);
				
			}
			if(move == Move.PASSMOVE)
			{
				turnsPassed++;
			}
			else
			{
				turnsPassed = 0;
			}
			gameBoard.applyMove(currentPlayer, move);
			gameBoard.printBoard(myColor);
			currentPlayer = -1*currentPlayer;
		}
	}
}

class InterruptTask extends TimerTask {
	@Override
	public void run() {
		System.out.println("C ****>timeup");
		Game.timeUP = true;
		Game.timer.cancel();
	}
}
