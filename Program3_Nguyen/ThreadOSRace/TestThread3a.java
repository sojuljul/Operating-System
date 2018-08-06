// Juliano Nguyen
// CSS 430
// Program 3 - Part 2
// TestThread3a - Conducts only numerical computation

public class TestThread3a extends Thread
{
	// Calculates a sum using a loop
	// Outputs thread is done at end
	public void run()
	{
		int sum = 0;
		
		// adds i to the sum after each iteration
		for (int i = 0; i < 500; i++)
		{
			sum = sum + i;
		}
		
		// output finished statement
		SysLib.cout("TestThread3a Done \n");
		SysLib.exit();
	}
}