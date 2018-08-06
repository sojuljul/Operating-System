// Juliano Nguyen
// 11/20/17
// CSS 430
// Program 4 - Test4 is used to test Cache.java
// read/write for different test cases with cache enabled/disabled
// display average read time and write time
// first call flush to clear cache before executing

// l Test4 [-enabled | -disabled] [1-4]

import java.util.*;

public class Test4 extends Thread
{
	private boolean isEnabled;
	private int testNumber;
	private long readTime;
	private long writeTime;
	private int bufferSize = 512;
	private int randomArraySize = 200;
	
	// Constructor to initialize variables
	// 1st argument: -disabled / -enabled
	// 2nd argument: one of the 4 tests from 1-4
	public Test4(String args[])
	{
		// check if enabled OR disabled
		if (args[0].equals("enabled"))
		{
			isEnabled = true;
		}
		else
		{
			isEnabled = false;
		}
		
		testNumber = Integer.parseInt(args[1]); // test number
		readTime = 0;
		writeTime = 0;
	}
	
	// Run
	// Call flush to clear cache first
	// 1) Random access, 2) Localized access
	// 3) Mixed access, 4) Adversary access
	public void run()
	{
		SysLib.flush();
		
		// find which test number was entered
		switch(testNumber)
		{
			case 1:
				randomAccess();
				break;
			case 2:
				localizedAccess();
				break;
			case 3:
				mixedAccess();
				break;
			case 4:
				adversaryAccess();
				break;
			default:
				SysLib.cout("Error: Invalid test \n"); // user did not enter 1-4
				break;
		}
		
		// sync and exit
		syncHelper();
		SysLib.exit();
	}
	
	// readHelper - determines which read to use
	// if enabled, cread
	// if disabled, rawread = no use of disk cache
	public void readHelper(int blockID, byte buffer[])
	{
		if (isEnabled == true)
		{
			SysLib.cread(blockID, buffer);
		}
		else
		{
			SysLib.rawread(blockID, buffer);
		}
	}
	
	// writeHelper - determines which write to use
	// if enabled, cwrite
	// if disabled, rawwrite = no use of disk cache
	public void writeHelper(int blockID, byte buffer[])
	{
		if (isEnabled == true)
		{
			SysLib.cwrite(blockID, buffer);
		}
		else
		{
			SysLib.rawwrite(blockID, buffer);
		}
	}
	
	// syncHelper - determines which sync to use
	// if enabled, csync
	// if disabled, sync = no use of disk cache
	public void syncHelper()
	{
		if (isEnabled == true)
		{
			SysLib.csync();
		}
		else
		{
			SysLib.sync();
		}
	}
	
	// Random
	// read and write many blocks randomly across disk
	// verify correctness of disk cache
	public void randomAccess()
	{
		// byte array to store random blocks
		// for read and write
		byte[] writeData = new byte[bufferSize];
		byte[] readData = new byte[bufferSize];
		
		// array to store the random numbers
		int[] randomData = new int[randomArraySize];
		Random rand = new Random(); // used to call .nextInt()
		
		// fill with random data
		rand.nextBytes(writeData);
		
		// writing
		long startTime = new Date().getTime(); // msec
		
		for (int i = 0; i < randomArraySize; i++)
		{
			// generate a positive, random number
			// and store it in array of numbers
			randomData[i] = Math.abs(rand.nextInt()) % 512;
			writeHelper(randomData[i], writeData);
		}
		
		long endTime = new Date().getTime();
		long elapsedTime = endTime - startTime;
		writeTime = elapsedTime / randomArraySize; // avg write time in msec
		
		// reading
		startTime = new Date().getTime();
		
		for (int i = 0; i < randomArraySize; i++)
		{
			readHelper(randomData[i], readData);
		}
		
		endTime = new Date().getTime();
		elapsedTime = endTime - startTime;
		readTime = elapsedTime / randomArraySize; // avg read time in msec
		
		// verify if write and read blocks are the same
		// if not, then print out they are not
		if (!Arrays.equals(writeData, readData))
		{
			SysLib.cout("Read and write blocks are different \n");
		}
		
		// print out results
		SysLib.cout("Random Access \n");
		SysLib.cout("Average write time: " + writeTime + " msec \n");		
		SysLib.cout("Average read time: " + readTime + " msec \n");
	}
	
	// Localized
	// read and write small selection of blocks many times
	// to get high ratio of cache hits
	public void localizedAccess()
	{
		// byte array to store random blocks
		// for write and read
		byte[] writeData = new byte[bufferSize];
		byte[] readData = new byte[bufferSize];
		
		Random rand = new Random();
		
		// fill with random data
		rand.nextBytes(writeData);
		
		// writing
		long startTime = new Date().getTime();
		
		// access only 10 blocks repeatedly
		for (int i = 0; i < randomArraySize; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				writeHelper(j, writeData);
			}
		}
		
		long endTime = new Date().getTime();
		long elapsedTime = endTime - startTime;
		writeTime = elapsedTime / randomArraySize; // avg write time
		
		// reading
		startTime = new Date().getTime();
		
		// access only 10 blocks repeatedly
		for (int i = 0; i < randomArraySize; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				readHelper(j, readData);
			}
		}
		
		endTime = new Date().getTime();
		elapsedTime = endTime - startTime;
		readTime = elapsedTime / randomArraySize; // avg read time
		
		// verify if write and read blocks are the same
		// if not, then print out they are not
		if (!Arrays.equals(writeData, readData))
		{
			SysLib.cout("Read and write blocks are different \n");
		}
		
		// print out results
		SysLib.cout("Localized Access \n");
		SysLib.cout("Average write time: " + writeTime + " msec \n");
		SysLib.cout("Average read time: " + readTime + " msec \n");
	}
	
	// Mixed
	// 90% of total disk operation is localized
	// 10% is random access
	public void mixedAccess()
	{
		// byte array to store blocks
		// for write and read
		byte[] writeData = new byte[bufferSize];
		byte[] readData = new byte[bufferSize];
		
		// array to store blocks of data
		int[] randomData = new int[randomArraySize];
		
		Random rand = new Random(); // used to call .nextInt()
		
		// fill with random data
		rand.nextBytes(writeData);		
		
		// writing
		long startTime = new Date().getTime();
		
		// round up using % 10
		// if its 0-8, go to localized
		// if its 9, go to random
		for (int i = 0; i < randomArraySize; i++)
		{
			int localNumber = Math.abs(rand.nextInt()) % 10;
			
			if (localNumber < 9) // check if its localized
			{
				randomData[i] = Math.abs(rand.nextInt()) % 10;
			}
			else // else, its random
			{
				randomData[i] = Math.abs(rand.nextInt()) % 512;
			}
			
			writeHelper(randomData[i], writeData);
		}
		
		long endTime = new Date().getTime();
		long elapsedTime = endTime - startTime;
		writeTime = elapsedTime / randomArraySize; // avg write time
		
		// reading
		startTime = new Date().getTime();
		
		for (int i = 0; i < randomArraySize; i++)
		{
			readHelper(randomData[i], readData);
		}
		
		endTime = new Date().getTime();
		elapsedTime = endTime - startTime;
		readTime = elapsedTime / randomArraySize; // avg read time
		
		// verify if write and read blocks are the same
		// if not, then print out they are not
		if (!Arrays.equals(writeData, readData))
		{
			SysLib.cout("Read and write blocks are different \n");
		}
		
		// print out results
		SysLib.cout("Mixed Access \n");
		SysLib.cout("Average write time: " + writeTime + " msec \n");
		SysLib.cout("Average read time: " + readTime + " msec \n");
	}
	
	// Adversary
	// generate disk accesses that do not
	// make good use of disk cache
	public void adversaryAccess()
	{
		// byte array for blocks of data
		// for write and read
		byte[] writeData = new byte[bufferSize];
		byte[] readData = new byte[bufferSize];
		
		Random rand = new Random();
		
		// fill with random data
		rand.nextBytes(writeData);		
		
		// writing
		long startTime = new Date().getTime();		
		
		// choose block number not chosen in last 10 accesses
		// this makes no duplicates of a block number
		for (int i = 0; i < bufferSize; i++)
		{
			writeHelper(i, writeData);
		}
		
		long endTime = new Date().getTime();
		long elapsedTime = endTime - startTime;
		writeTime = elapsedTime / randomArraySize; // avg write time
		
		// reading
		startTime = new Date().getTime();
		
		for (int i = 0; i < bufferSize; i++)
		{
			readHelper(i, readData);
		}
		
		endTime = new Date().getTime();
		elapsedTime = endTime - startTime;
		readTime = elapsedTime / randomArraySize; // avg read time
		
		// verify if write and read blocks are the same
		// if not, then print out they are not
		if (!Arrays.equals(writeData, readData))
		{
			SysLib.cout("Read and write blocks are different \n");
		}
		
		// print results
		SysLib.cout("Adversary Access \n");
		SysLib.cout("Average write time: " + writeTime + " msec \n");
		SysLib.cout("Average read time: " + readTime + " msec \n");
	}
}
