// Kien Chin
// Juliano Nguyen
// Shanelee Tran
// CSS430 Project - Superblock

class SuperBlock 
{
   public int totalBlocks; // the number of disk blocks
   public int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head

   private final int defaultInodeBlocks = 64;
   
   public SuperBlock( int diskSize ) 
   {
	   // read superblock from disk
	   byte[] superBlock = new byte[Disk.blockSize];
	   SysLib.rawread(0, superBlock);
	   
	   totalBlocks = SysLib.bytes2int(superBlock, 0);
	   totalInodes = SysLib.bytes2int(superBlock, 4);
	   freeList = SysLib.bytes2int(superBlock, 8);

	   if ((totalBlocks == diskSize) && (totalInodes > 0) 
			&& (freeList >= 2))
	   {
		   // disk contents are valid
		   //SysLib.cout("SuperBlock HIHIHI: " + tempInodes + "\n");
		   return;
	   }
	   else
	   {
		   // need to format disk
		   totalBlocks = diskSize;
		   format(defaultInodeBlocks);
		   //SysLib.cout("SuperBlock Format \n");
	   }
   }
   
   // write totalBlocks, totalInodes, freeList to disk
   public void sync()
   {
	   byte[] buffer = new byte[Disk.blockSize];
	   
	   // convert superblock info to bytes
	   SysLib.int2bytes(totalBlocks, buffer, 0);
	   SysLib.int2bytes(totalInodes, buffer, 4);
	   SysLib.int2bytes(freeList, buffer, 8);
	   
	   // write to disk
	   SysLib.rawwrite(0, buffer);
   }
   
   // formats the superblock info
   // updates the freelist
   // writes to disk the total blocks/inodes/freelist
   public void format(int numBlocks)
   {
	   totalInodes = numBlocks;
	   
	   // reset inodes info and write to disk
	   for (int i = 0; i < totalInodes; i++)
	   {
		   Inode tempInode = new Inode();
		   tempInode.flag = 0;
		   tempInode.toDisk((short) i);
	   }
	   
	   // the next free block
	   freeList = 2 + (totalInodes / 16);
	   
	   // next element that is free in current block
	   for (int i = freeList; i < 1000; i++)
	   {
		   byte[] buffer = new byte[Disk.blockSize];
		   SysLib.int2bytes(i + 1, buffer, 0);
		   SysLib.rawwrite(i, buffer);
	   }
	   
	   // write superblock to disk
	   byte[] newBuffer = new byte[Disk.blockSize];
	   
	   // convert superblock info to bytes
	   SysLib.int2bytes(totalBlocks, newBuffer, 0);
	   SysLib.int2bytes(totalInodes, newBuffer, 4);
	   SysLib.int2bytes(freeList, newBuffer, 8);
	   
	   // write to disk
	   SysLib.rawwrite(0, newBuffer);
   }
   
   // dequeue top block from free list
   public int getFreeBlock()
   {
	   if (freeList < 0 || freeList > totalBlocks)
	   {
		   // error
		   return -1;
	   }
	   
	   // get the current free block
	   int freeBlock = freeList;
	   
	   // get the next free block
	   byte[] buffer = new byte[Disk.blockSize];
	   
	   // reads from disk the next free block
	   SysLib.rawread(freeList, buffer);
	   freeList = SysLib.bytes2int(buffer, 0);
	   
	   return freeBlock;
   }
   
   // enqueue given block to end of free list
   public boolean returnBlock(int blockNumber)
   {
	   if (blockNumber < 0 || blockNumber > totalBlocks)
	   {
		   return false;
	   }
	   
	   byte[] buffer = new byte[Disk.blockSize];
	   
	   // reset byte array to 0
	   for (int i = 0; i < Disk.blockSize; i++)
	   {
		   buffer[i] = 0;
	   }
	   
	   SysLib.int2bytes(freeList, buffer, 0);
	   SysLib.rawwrite(blockNumber, buffer);
	   freeList = blockNumber;
	   
	   return true;
   }
   
}
