// Juliano Nguyen
// 11/20/17
// CSS 430
// Program 4 - Implementing enhanced second chance algorithm
// Includes a constructor, read/write method, sync/flush method

// (reference bit, dirty bit) = 4 cases
// (0, 0) = best page to replace
// (0, 1) = page need to be written out before replacement
// (1, 0) = probably be used again soon
// (1, 1) = probably be used again soon, page need to be written out
// reference bit = reset to 0 when searching next victim
// dirty bit = reset to 0 when block is written to disk

public class Cache
{
	private DataBlock[] mainCache;
	private int bufferSize;
	private int pointer;
	
	// Object for a cache block
	// Includes a block number, ref bit, dirty bit, buffer
	private class DataBlock
	{
		int blockNumber;
		boolean referenceBit;
		boolean dirtyBit;
		byte[] buffer;
		
		// Constructor for object
		// which initializes the variables
		public DataBlock(int blockSize)
		{
			blockNumber = -1; // not valid block info
			referenceBit = false;
			dirtyBit = false;
			buffer = new byte[blockSize];
		}
	}
	
	// Constructor for Cache
	// initializes variables
	// allocates cacheBlocks number of cache blocks,
	// containing blockSize byte data
	public Cache(int blockSize, int cacheBlocks)
	{
		mainCache = new DataBlock[cacheBlocks];
		bufferSize = blockSize;
		pointer = 0; // keep track of where we are in cache
		
		// create new cache block object in each element
		for (int i = 0; i < cacheBlocks; i++)
		{
			mainCache[i] = new DataBlock(blockSize);
		}
	}
	
	// Read - read into buffer array the cache block 
	// of blockID from disk if in cache
	// else, reads block from disk
	// upon error, return false, else true
	public synchronized boolean read(int blockID, byte buffer[])
	{		
		// check for invalid blockID
		if (blockID < 0)
		{
			return false;
		}
		
		int numOfBlocks = mainCache.length;
		
		// scan all cache entries	
		// find entry with same block ID
		// read from cache
		for (int i = 0; i < numOfBlocks; i++)
		{
			if (mainCache[i].blockNumber == blockID)
			{
				// copy data from cache to buffer
				System.arraycopy(mainCache[i].buffer, 0, buffer, 0, bufferSize);
				mainCache[i].referenceBit = true;
				return true;
			}
		}
		
		// else, find free entry in cache
		// read data from disk to block
		for (int i = 0; i < numOfBlocks; i++)
		{
			if (mainCache[i].blockNumber == -1) // empty entry
			{
				// read from disk to cache
				SysLib.rawread(blockID, mainCache[i].buffer);
				
				// copy data from cache to buffer
				System.arraycopy(mainCache[i].buffer, 0, buffer, 0, bufferSize);
				mainCache[i].blockNumber = blockID;
				mainCache[i].referenceBit = true;
				return true;
			}
		}
		
		// if cache is full and no free block,
		// then find victim with enhanced second chance
		int victim = findVictim();
		
		// if victim is dirty, write to disk
		// read new data from disk to block
		if (mainCache[victim].dirtyBit == true)
		{
			// write to disk and reset dirty bit
			SysLib.rawwrite(mainCache[victim].blockNumber, mainCache[victim].buffer);
			mainCache[victim].dirtyBit = false;
		}
		
		// read from disk to cache
		SysLib.rawread(blockID, mainCache[victim].buffer);
		
		// copy data from cache to buffer
		System.arraycopy(mainCache[victim].buffer, 0, buffer, 0, bufferSize);
		mainCache[victim].blockNumber = blockID;
		mainCache[victim].referenceBit = true;
		return true;
	}
	
	// Write - write buffer array to cache block of blockID from disk
	// else find free block and writes buffer on it
	// upon error, return false, else true
	// Writing marks entry as dirty
	public synchronized boolean write(int blockID, byte buffer[])
	{		
		// check if invalid blockID
		if (blockID < 0)
		{
			return false;
		}
		
		int numOfBlocks = mainCache.length;
		
		// scan cache for block
		// find entry with same blockID
		// write new data to block
		for (int i = 0; i < numOfBlocks; i++)
		{
			if (mainCache[i].blockNumber == blockID)
			{
				// copies data from buffer to cache
				System.arraycopy(buffer, 0, mainCache[i].buffer, 0, bufferSize);
				mainCache[i].referenceBit = true;
				mainCache[i].dirtyBit = true;
				return true;
			}
		}
		
		// else, find free entry
		// write data to block
		for (int i = 0; i < numOfBlocks; i++)
		{
			if (mainCache[i].blockNumber == -1)
			{
				// copies data from buffer to cache
				System.arraycopy(buffer, 0, mainCache[i].buffer, 0, bufferSize);
				mainCache[i].referenceBit = true;
				mainCache[i].dirtyBit = true;
				mainCache[i].blockNumber = blockID;
				return true;
			}
		}
		
		// if cannot find free block,
		// find victim using enhanced second chance
		int victim = findVictim();
		
		// if victim is dirty
		// write to disk
		// write new data to block
		if (mainCache[victim].dirtyBit == true)
		{
			// write to disk
			SysLib.rawwrite(mainCache[victim].blockNumber, mainCache[victim].buffer);
		}
		
		// copies data from buffer to cache
		System.arraycopy(buffer, 0, mainCache[victim].buffer, 0, bufferSize);
		mainCache[victim].blockNumber = blockID;
		mainCache[victim].referenceBit = true;
		mainCache[victim].dirtyBit = true;
		return true;
	}
	
	// Sync - writes back all dirty blocks to disk
	// maintains valid clean copies of block
	// clear dirty bit, do not invalidate
	public synchronized void sync()
	{
		for (int i = 0; i < mainCache.length; i++)
		{
			// check if invalid block
			if (mainCache[i].blockNumber == -1)
			{
				continue; // go to next block
			}
			
			if (mainCache[i].dirtyBit == true)
			{
				// write to disk
				SysLib.rawwrite(mainCache[i].blockNumber, mainCache[i].buffer);
				mainCache[i].dirtyBit = false;
			}
		}
		
		SysLib.sync();
	}
	
	// Flush - writes back all dirty blocks to disk
	// invalidates all cached blocks
	public synchronized void flush()
	{
		for (int i = 0; i < mainCache.length; i++)
		{
			// check if invalid block
			if (mainCache[i].blockNumber == -1)
			{
				continue; // go to next block
			}
			
			if (mainCache[i].dirtyBit == true)
			{
				// write to disk
				SysLib.rawwrite(mainCache[i].blockNumber, mainCache[i].buffer);
				mainCache[i].dirtyBit = false;
			}
			
			// invalidate the entry
			// clear reference bit
			mainCache[i].referenceBit = false;
			mainCache[i].blockNumber = -1;
		}
		
		SysLib.sync();
	}
	
	// findVictim - to find a victim block
	// Keeps looping to find a (0, 0) block
	// If cannot find (0, 0), then find a (0, 1) block
	// returns a victim block number
	public int findVictim()
	{
		int count = 0;
		boolean isLoopTwice = false; // check for looping cache twice
		
		while (true)
		{
			boolean tempRefBit = mainCache[pointer].referenceBit;
			boolean tempDirtyBit = mainCache[pointer].dirtyBit;
			int doubleSize = mainCache.length * 2;
			
			// checks if r = 0, d = 0
			if (tempRefBit == false && tempDirtyBit == false)
			{
				int victimBlock = pointer;
				
				// move pointer to next block
				// % length to reset pointer to beginning
				pointer = (pointer + 1) % mainCache.length;
				return victimBlock;
			}
			
			// check if we looped the cache twice
			// to ensure we did not find r = 0, d = 0
			if (count == doubleSize)
			{
				isLoopTwice = true;
			}
			
			// if we looped twice,
			// checks if r = 0, d = 1
			if (isLoopTwice == true)
			{
				if (tempRefBit == false && tempDirtyBit == true)
				{
					int victimBlock = pointer;
				
					// move pointer to next block
					// % length to reset pointer to beginning
					pointer = (pointer + 1) % mainCache.length;
					return victimBlock;
				}
			}
			
			// current block given a second chance
			// reset reference bit
			mainCache[pointer].referenceBit = false;
			
			// move pointer to next block
			// % length to reset pointer to beginning
			pointer = (pointer + 1) % mainCache.length;
			count++;
		}
	}
}
