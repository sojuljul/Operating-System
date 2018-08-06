// Juliano Nguyen
// CSS 430
// Program 3 - Part 2
// TestThread3b - Reading/writing many blocks
// randomly across the disk

public class TestThread3b extends Thread
{
	// Read/write many blocks across disk
	// Outputs thread is done at end
	public void run()
	{
		// byte array of 512 bytes size
		byte[] block = new byte[512];
		
		// loop to read/write to block a number of times
		for (int i = 0; i < 500; i++)
		{
			// write data to disk
			SysLib.rawwrite(i, block);
			
			// read data from disk
			SysLib.rawread(i, block);
		}
		
		// output finished statement
		SysLib.cout("TestThread3b Done \n");
		SysLib.exit();
	}
}