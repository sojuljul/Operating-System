// Kien Chin
// Juliano Nguyen
// Shanelee Tran
// CSS430 Project - Inode
import java.util.Vector;

public class Inode 
{
   private final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers
   private final static int maxInodes = 16;

   /*
   private int origlength;
   private short origCount;
   private short origFlag;
   private short origDirect[];
   private short origIndirect;
   */

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = used, ...
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

   public Inode( )
   {                                     // a default constructor
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
      {
		  direct[i] = -1;
	  }
      indirect = -1;
   }

   public Inode( short iNumber ) // retrieving inode from disk
   {                       
      // finds the block number and fills the data array with inode properties from the disk
      int blockNumber = 1 + iNumber / maxInodes;
      byte[] data = new byte[Disk.blockSize];
      SysLib.rawread(blockNumber, data);
      int offset = (iNumber % maxInodes) * 32;
      
      length = SysLib.bytes2int(data, offset);
      offset += 4;
      count = SysLib.bytes2short(data, offset);
      offset += 2;
      flag = SysLib.bytes2short(data, offset);
      offset += 2;
   }

   public int toDisk( short iNumber ) // save to disk as the i-th inode
   {
      //write inode properties back to disk
      int blockNumber = 1 + iNumber / maxInodes;
      byte[] data = new byte[Disk.blockSize];
      int offset = (iNumber % maxInodes) * 32;

      SysLib.int2bytes(length, data, offset);
      offset += 4;
      SysLib.short2bytes(count, data, offset);
      offset += 2;
      SysLib.short2bytes(flag, data, offset);
      offset += 2;

      SysLib.rawwrite(blockNumber, data);

      if(blockNumber < 0)
      {
         return -1;
      }
      else
      {
         return 0;
      }
   }

   /*
   private bool isDiskChanged( short iNumber )
   {
      int blockNumber = 1 + iNumber / maxInodes;
      byte[] data = new byte[Disk.blockSize];
      SysLib.rawread(blockNumber, data);
      int offset = (iNumber % maxInodes) * 32;

      if(SysLib.bytes2int(data, offset) != length);
      {
         return false;
      }
      offset += 4;

      if(SysLib.bytes2short(data, offset) != count)
      {
         return false;
      }
      offset += 2;

      if(SysLib.bytes2short(data, offset) != flag)
      {
         return false;
      }
      offset += 2;

      return true;
   }
   */
   //private short getIndexBlockNumber()
   //private boolean setIndexBlock(short indexBlockNumber)
   //private short findTargetBlock(int offset)
}
