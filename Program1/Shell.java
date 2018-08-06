// Author: Juliano Nguyen
// 10/12/17
// CSS 430
// Program 1 - Part 2 - Shell.java
// Assuming user input is correctly formatted such as "PingPong abc 100 ; ..."

import java.io.*;
import java.util.*;

class Shell extends Thread
{
	// global variables
	private String cmdLine; // command line

	public Shell()
	{
		cmdLine = ""; // initialize to empty string
	}

	// Run multiple commands, delimited by '&' or ';'
	// Prompt for user input; if "exit", then exit out
	public void run()
	{
		int countShell = 1;	
		
		// keep looping if "exit" has not been entered
		while (true)
		{
			// prompt user input at "shell[#]%"
			SysLib.cout("shell[" + countShell + "]% ");
			
			// read in user input
			StringBuffer buffer = new StringBuffer();
			SysLib.cin(buffer);
			String[] args = SysLib.stringToArgs(buffer.toString()); // store string into array
			
			// when user enters nothing, restart loop and prompt again
			// can NOT enter "&" or ";" as first argument
			if (args.length == 0 || args[0].equals("&") || args[0].equals(";"))
			{
				continue;
			}
			
			// get out of while loop if "exit" is entered
			if (args[0].equals("exit"))
			{
				break;
			}
			
			countShell++; //increment counter
			
			// separate array of strings for ; and &
			String[] semiColonStrings = buffer.toString().split(";");
			String[] andStrings = buffer.toString().split("&");
			
			// (PingPong ; PingPong) length is 2
			// (PingPong ; PingPong & PingPong) length is 2 for ; and &
			// (PingPong ;) length is 1
			if ((semiColonStrings.length > 1) && (andStrings.length > 1))
			{
				// First split by ;
				// then split by &
				String[] argSemiMixed = buffer.toString().split(";");
				
				for (int i = 0; i < argSemiMixed.length; i++)
				{
					String[] argAndMixed = argSemiMixed[i].split("&");
					
					// if the first split is ; then only 1 command
					// execute command for ;
					// else, execute for &
					if (argAndMixed.length == 1)
					{
						String[] argFirstSemi = SysLib.stringToArgs(argSemiMixed[i]);
						if (SysLib.exec(argFirstSemi) > 0) // child thread success
						{
							SysLib.join();
						}
					}
					else
					{
						executeAnd(argAndMixed);
					}
				}
			}
			else if (andStrings.length > 1)
			{
				// execute if string is only &
				executeAnd(andStrings);
			}
			else
			{
				// execute if string is only ;
				// also if length is 1
				executeSemiColon(semiColonStrings);
			}

		}
		
		// terminate calling thread
		SysLib.cout("Exiting \n");
		SysLib.exit();
	}
	
	// Executes commands if delimited by ;
	// Passes in a string array as argument that was delimited by ;
	// ; is sequential execution
	public void executeSemiColon(String[] tempArray)
	{
		// semi colons need to call join before executing another command
		for (int i = 0; i < tempArray.length; i++)
		{
			String[] argSemi = SysLib.stringToArgs(tempArray[i]);
			
			// SysLib.exec returns child thread ID on success, otherwise -1
			if (SysLib.exec(argSemi) > 0)
			{
				SysLib.join();
			}
			else
			{
				return; // child thread failure	
			}
		}
	}
	
	// Executes commands if delimited by &
	// Passes in a string array as argument that was delmited by &
	// & is concurrent execution
	public void executeAnd(String[] tempArray)
	{
		// keeping track of the number of child threads
		// since its a concurrent execution,
		// no need to join during its execution of commands
		int childThread = 0;
		
		for (int i = 0; i < tempArray.length; i++)
		{
			String[] argAnd = SysLib.stringToArgs(tempArray[i]);
			
			if (SysLib.exec(argAnd) > 0)
			{
				childThread += 1; // child thread success
			}
			else
			{
				childThread -= 1; // child thread failure
			}
		}

		// Repeatedly call join to return exact child thread ID
		// Otherwise, it won't prompt for user input
		for (int j = 0; j < childThread; j++)
		{
			SysLib.join();
		}		
	}
	
}
