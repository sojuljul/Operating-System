// Juliano Nguyen
// CSS 430
// Program 3 - Part 2
// Test3 - Spawns and waits for completion
// of X pairs of threads (X = 1 ~ 4)
// Calculates the elapsed time from start to end
// TestThread3a = numerical computation
// TestThread3b = read/write many blocks

import java.util.Date;

public class Test3 extends Thread
{
	private int numOfPairs; // X pairs of threads
	
	// Constructor for Test3 that takes x pairs as argument
	// l Test3 3
	// Argument is 3 = pairs of threads
	public Test3(String[] args)
	{
		numOfPairs = Integer.parseInt(args[0]);
	}
	
	// Measure time elapsed from spawn to termination
	// Executes TestThread3a and TestThread3b (CPU and Disk)
	// Outputs the elapsed time at the end
	public void run()
	{
		// get the current starting time
		long startTime = new Date().getTime(); // milliseconds
		
		String[] args1 = SysLib.stringToArgs("TestThread3a");
		String[] args2 = SysLib.stringToArgs("TestThread3b");
		
		// exec thread for CPU
		// loop based on # of pairs
		for (int i = 0; i < numOfPairs; i++)
		{
			SysLib.exec(args1);
		}
		
		// exec thread for Disk
		// loop based on # of pairs
		for (int i = 0; i < numOfPairs; i++)
		{
			SysLib.exec(args2);
		}
		
		// wait for CPU = args1
		for (int i = 0; i < numOfPairs; i++)
		{
			SysLib.join();
		}
		
		// wait for Disk = args2
		for (int i = 0; i < numOfPairs; i++)
		{
			SysLib.join();
		}
		
		// get the current ending time
		long endTime = new Date().getTime(); // milliseconds
		
		long elapsedTime = endTime - startTime;
		
		// output elapsed time
		SysLib.cout("Elapsed time = " + elapsedTime + " msec. \n");
		SysLib.exit();
	}
}
