// Kien Chin
// Juliano Nguyen
// Shanelee Tran
// CSS430 Project - FileSystem

public class FileSystem
{
	private SuperBlock mainSuperBlock;
	private Directory mainDirectory;
	private FileTable mainTable;
	
	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	// constructor for FileSysem
	// create superblock, directory, and file table
	public FileSystem(int diskBlocks)
	{
		mainSuperBlock = new SuperBlock(diskBlocks);
		mainDirectory = new Directory(mainSuperBlock.totalInodes);
		mainTable = new FileTable(mainDirectory);

		// read the "/" file from disk
		FileTableEntry dirEntry = open("/", "r");
        //SysLib.cout("OPEN DONE \n");
        int dirSize = fsize(dirEntry);
        //SysLib.cout("1. Before If dirSize: " + dirSize + "\n" );
        if (dirSize > 0)
		{
			// directory has some data
			byte[] dirData = new byte[dirSize];
			read(dirEntry, dirData);
            //SysLib.cout("2. After read: " + dirData.length + "\n");
            mainDirectory.bytes2directory(dirData);
            //SysLib.cout("3. Aftr bytes2dir\n");
        }
		close(dirEntry);
	}
	
	// open - returns a file table entry object
	// given a file name and the access mode
	public FileTableEntry open(String filename, String mode)
	{
		// allocate a file table entry given filename and mode
		FileTableEntry ftEntry = mainTable.falloc(filename, mode);

		//SysLib.cout("OPEN FALLOC DONE \n");

		// if writing
		// then check if blocks within entry are not allocated
		if (mode.equals("w"))
		{
			if (deallocAllBlocks(ftEntry) == false)
			{
				return null;
			}
		}
		
		return ftEntry;
	}
	
	// close - close file given the file table entry
	// commits all file transactions on this file
	// unregisters fd from user fd table
	public synchronized boolean close(FileTableEntry ftEntry)
    {
		// decrement # of processes
        ftEntry.count--;
        
        // if no more processes, remove the entry from table
        if (ftEntry.count == 0)
        {
			return mainTable.ffree(ftEntry);
		}
        return false;
    }

	// read - read up to buffer.length bytes
	// start at position pointed by seek pointer
	// if bytes btw seek ptr and end of file < buffer.length
	// then SysLib.read as many bytes
	// increments seek pointer by # of bytes read
	// return number of bytes that have been read
	public int read(FileTableEntry ftEntry, byte[] buffer)
    {
        // error if write/append
        if (ftEntry.mode.equals("w") || ftEntry.mode.equals("a"))
        {
			return -1;
		}

		int fileSize = fsize(ftEntry); // size of files in bytes
		int bufferSize = buffer.length;
		int totalReads = 0; // total # of bytes read
		int currentReads = 0; // # of bytes that have been read
		
		boolean isSeekLess = ftEntry.seekPtr < fileSize;
		boolean isBufferGreater = bufferSize > 0;
		
        synchronized(ftEntry)
        {
			/*
			 * keeps reading bytes of data to the buffer
			 * moves the seek pointer how much we read
			 * updates the values how much we read
			 */
            while (isSeekLess && isBufferGreater)
            {
				int blockNum = ftEntry.inode.findTargetBlock(ftEntry.seekPtr);
				
				// invalid block
				if (blockNum < 0)
				{
					break;
				}
				
				// read data to buffer from block pointed by pointer
				byte[] blockBuffer = new byte[512];
				SysLib.rawread(blockNum, blockBuffer);
				
				int offset = ftEntry.seekPtr % 512;
				
				// remaining blocks left to read
				int remVal = 512 - totalReads;
				
				// update seek pointer within the file
				int fileLocation = fileSize - ftEntry.seekPtr;
				
				// if remaining blocks is less than current file location
				// still have to read those remaining blocks
				// otherwise, its at the file location
				if (remVal < fileLocation)
				{
					totalReads = remVal;
				}
				else
				{
					totalReads = fileLocation;
				}
				
				// check if greater than the buffer length
				if (remVal > bufferSize)
				{
					remVal = bufferSize;
				}
				
				// copy from buffer of block to the buffer passed in
				System.arraycopy(blockBuffer, offset, buffer, currentReads, totalReads);
				
				// update the seek pointer, buffer length, current bytes read
				ftEntry.seekPtr += totalReads;
				bufferSize = bufferSize - totalReads;
				currentReads = currentReads + totalReads;
			}
			
        }
        
        return currentReads;
	}
		/*
        int remVal = (buffer.length > ftEntry.inode.length) ? ftEntry.inode.length : buffer.length;
        int retVal = remVal;
        int posCount = 0;

        while(posCount < buffer.length && posCount < fsize(ftEntry))
        {
            int blockAddr = ftEntry.inode.findTargetBlock(ftEntry.seekPtr);
            byte[] tempBuffer = new byte[Disk.blockSize];
            SysLib.rawread(blockAddr, tempBuffer);
            if(tempBuffer.length < remVal)
            {
                System.arraycopy(tempBuffer, 0, buffer, posCount, Disk.blockSize);
                posCount += tempBuffer.length;
                ftEntry.seekPtr++;
                remVal -= tempBuffer.length;
            }
            else
            {
                System.arraycopy(tempBuffer, 0, buffer, posCount, remVal);
                ftEntry.seekPtr++;
                return retVal;
            }
        }
        return -1;
        * */
    }

	// write - write contents of buffer to file
	// start at position pointed by seek pointer
	// can overwrite existing data / append to end of file
	// SysLib.write increments seek ptr by # of bytes written
	// return number of bytes that have been written
    /*
	public int write(FileTableEntry ftEntry, byte[] buffer)
    {
        int remVal = buffer.length;
        int retVal = remVal;
        int posCount = 0;

        //clear all then write
        if(ftEntry.mode.equals("w"))
        {
            ftEntry.seekPtr = 0;
            byte[] tempBuffer = new byte[Disk.blockSize];
            while(ftEntry.seekPtr < ftEntry.inode.length)
            {
                SysLib.rawwrite(ftEntry.seekPtr, tempBuffer);
            }

            ftEntry.seekPtr = 0;
            while(true)
            {
                System.arraycopy(buffer, posCount, tempBuffer, 0, Disk.blockSize);
                SysLib.rawwrite(ftEntry.seekPtr, tempBuffer);

                if(tempBuffer.length > remVal)
                {
                    ftEntry.seekPtr += remVal;
                    return retVal;
                }
                else
                {
                    ftEntry.seekPtr += tempBuffer.length;
                }

                posCount += tempBuffer.length;
                remVal -= tempBuffer.length;
            }
        }

        //overwrite
        if(ftEntry.mode.equals("w+"))
        {
            while(true)
            {
                byte[] tempBuffer = new byte[Disk.blockSize];

                System.arraycopy(buffer, posCount, tempBuffer, 0, Disk.blockSize);
                SysLib.rawwrite(ftEntry.seekPtr, tempBuffer);

                if(tempBuffer.length > remVal)
                {
                    ftEntry.seekPtr += remVal;
                    return retVal;
                }
                else
                {
                    ftEntry.seekPtr += tempBuffer.length;
                }

                posCount += tempBuffer.length;
                remVal -= tempBuffer.length;
            }
        }

        //append
        if(ftEntry.mode.equals("a"))
        {
            ftEntry.seekPtr += ftEntry.inode.length;
            while(true)
            {
                byte[] tempBuffer = new byte[Disk.blockSize];

                System.arraycopy(buffer, posCount, tempBuffer, 0, Disk.blockSize);
                SysLib.rawwrite(ftEntry.seekPtr, tempBuffer);

                if(tempBuffer.length > remVal)
                {
                    ftEntry.seekPtr += remVal;
                    return retVal;
                }
                else
                {
                    ftEntry.seekPtr += tempBuffer.length;
                }

                posCount += tempBuffer.length;
                remVal -= tempBuffer.length;
            }
        }
        return -1;
    }*/
    public int write(FileTableEntry entry, byte[] buffer){
        int bytesWritten = 0;
        int bufferSize = buffer.length;
        int blockSize = 512;

        if (entry == null || entry.mode == "r")
        {
            return -1;
        }

        synchronized (entry)
        {
            while (bufferSize > 0)
            {
                int location = entry.inode.findTargetBlock(entry.seekPtr);

                // if current block null
                if (location == -1)
                {
                    short newLocation = (short) mainSuperBlock.getFreeBlock();

                    int testPtr = entry.inode.getIndexBlockNumber(entry.seekPtr, newLocation);

                    if (testPtr == -3)
                    {
                        short freeBlock = (short) this.mainSuperBlock.getFreeBlock();

                        // indirect pointer is empty
                        if (!entry.inode.setIndexBlock(freeBlock))
                        {
                            return -1;
                        }

                        // check block pointer error
                        if (entry.inode.getIndexBlockNumber(entry.seekPtr, newLocation) != 0)
                        {
                            return -1;
                        }

                    }
                    else if (testPtr == -2 || testPtr == -1)
                    {
                        return -1;
                    }

                    location = newLocation;
                }

                byte [] tempBuff = new byte[blockSize];
                SysLib.rawread(location, tempBuff);

                int tempPtr = entry.seekPtr % blockSize;
                int diff = blockSize - tempPtr;

                if (diff > bufferSize)
                {
                    System.arraycopy(buffer, bytesWritten, tempBuff, tempPtr, bufferSize);
                    SysLib.rawwrite(location, tempBuff);

                    entry.seekPtr += bufferSize;
                    bytesWritten += bufferSize;
                    bufferSize = 0;
                }
                else {
                    System.arraycopy(buffer, bytesWritten, tempBuff, tempPtr, diff);
                    SysLib.rawwrite(location, tempBuff);

                    entry.seekPtr += diff;
                    bytesWritten += diff;
                    bufferSize -= diff;
                }
            }

            // update inode length if seekPtr larger

            if (entry.seekPtr > entry.inode.length)
            {
                entry.inode.length = entry.seekPtr;
            }
            entry.inode.toDisk(entry.iNumber);
            return bytesWritten;
        }
    }



	// fsize - returns size in bytes of file
	public synchronized int fsize(FileTableEntry ftEntry)
	{
		// make a temp inode object
		// and return the size of it
        synchronized(ftEntry)
        {
            Inode tempInode = ftEntry.inode;
            return tempInode.length;
        }

	}
	
	// delete file specified by filename
	// all blocks are freed
	// if file is currently open, not delete & returns false
	// else, return true
	public boolean delete(String filename)
	{
		// FileTableEntry file = open(filename, "w");
		
		// find inumber of the inode from filename
		short temp = mainDirectory.namei(filename);
		
		// if successful, makes a free block
		if (mainDirectory.ifree(temp))
		{
			return true;
		}
		
		return false;
	}
	
	// seek - updates the seek pointer
	// depending on the cases for whence
	public int seek(FileTableEntry ftEntry, int offset, int whence)
    {
		// if = 0, set to offset
        if(whence == SEEK_SET)
        {
            ftEntry.seekPtr = offset;
        }
        
        // if = 1, set to current value + offset
        if(whence == SEEK_CUR)
        {
            ftEntry.seekPtr += offset;
        }
        
        // if = 2, set to size of file + offset
        if(whence == SEEK_END)
        {
            ftEntry.seekPtr = ftEntry.inode.length + offset;
        }
        
        // Check if negative, set to 0
        if(ftEntry.seekPtr < 0)
        {
            ftEntry.seekPtr = 0;
        }
        
        // check if beyond file size, set to end of file
        if(ftEntry.seekPtr > ftEntry.inode.length)
        {
            ftEntry.seekPtr = ftEntry.inode.length;
        }
        
        return ftEntry.seekPtr;
    }

	// format the disk and its contents
	// creates brand new superblock, directory, file table
    public boolean format(int files)
    {
        try
        {
			// call superblock format to write data to disk
            mainSuperBlock.format(files);
            
            // create a brand new directory and file table
            mainDirectory = new Directory(mainSuperBlock.totalInodes);
            mainTable = new FileTable(mainDirectory);
        }
        catch (Exception e)
        {
            return false;
        }
        
        return true;
    }

	// sync - write the directory to disk at root
	// also writes superblock data to disk
    public void sync()
    {
		// open at root directory "/"
		FileTableEntry ftEntry = open("/", "w");
		
		// get directory info into byte array
		byte[] buffer = mainDirectory.directory2bytes();
		
		// write directory to disk
		write(ftEntry, buffer);
		close(ftEntry);
		
		// write superblock to disk
		mainSuperBlock.sync();
    }

	// deallocAllBlocks
	// check if inode blocks are valid
	// checks all direct pointers
	// check indirect pointers
	// write inodes to disk using toDisk
    /*
	private boolean deallocAllBlocks(FileTableEntry ftEntry)
    {
        //for(int i = 0; i < ftEntry.inode.length)

		ftEntry.inode.toDisk(ftEntry.iNumber);
        return true;
    }
    */

    private boolean deallocAllBlocks(FileTableEntry fileTableEntry){
        short invalid = -1;
        if (fileTableEntry.inode.count != 1)
        {
            SysLib.cerr("Null Pointer");
            return false;
        }

        for (short blockId = 0; blockId < fileTableEntry.inode.getDirectSize(); blockId++)
        {
            if (fileTableEntry.inode.direct[blockId] != invalid)
            {
                mainSuperBlock.returnBlock(blockId);
                fileTableEntry.inode.direct[blockId] = invalid;
            }
        }

        byte [] data = fileTableEntry.inode.freeIndirectBlock();

        if (data != null)
        {
            short blockId;
            while((blockId = SysLib.bytes2short(data, 0)) != invalid)
            {
                mainSuperBlock.returnBlock(blockId);
            }
        }
        fileTableEntry.inode.toDisk(fileTableEntry.iNumber);
        return true;
    }
}
