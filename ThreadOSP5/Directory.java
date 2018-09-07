// Kien Chin
// Juliano Nguyen
// Shanelee Tran
// CSS430 Project - Directory

public class Directory 
{
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.

	// directory constructor
   public Directory( int maxInumber ) 
   {
   	  SysLib.cout("maxInumber: " + maxInumber + "\n");
      fsize = new int[maxInumber];     // maxInumber = max files
      for ( int i = 0; i < maxInumber; i++ )
      {
		  fsize[i] = 0;                 // all file size initialized to 0 
	  }

      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
   }

   public void bytes2directory( byte data[] ) 
   {
      // assumes data[] received directory information from disk
      // initializes the Directory instance with this data[]

	   //SysLib.cout("START BYTES2DIR \n");
	   //SysLib.cout("fsize " + fsize.length + "\n");
	   //SysLib.cout("fnames " + fnames.length + "\n");
      int offset = 0;
      
      for (int i = 0; i < fsize.length; i++)
      {
		  fsize[i] = SysLib.bytes2int(data, offset);
		  offset += 4;
	  }
	   //SysLib.cout("AFTER FIRST LOOP \n");
	   //SysLib.cout("fsize " + fsize.length + "\n");
	  for (int i = 0; i < fnames.length; i++)
	  {
		  //SysLib.cout("BEGIN SECOND \n");
		  String tempName = new String(data, offset, (maxChars * 2));

		  //SysLib.cout("AFTER STRING INITIATION\n");
		  // copies characters from string to fnames
		  //SysLib.cout("fnames " + fnames[i] + "\n");
		  //SysLib.cout("fsize " + fsize[i] + "\n");
		  tempName.getChars(0, fsize[i], fnames[i], 0);
		  //SysLib.cout("AFTER GET CHARS\n");
		  offset += (maxChars * 2);
		  //SysLib.cout("AFTER OFFSET\n");
	  }
	   //SysLib.cout("AFTER SECOND LOOP\n");
	   //do you see my pain?
   }

   public byte[] directory2bytes( ) 
   {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.
      
      // used for total size of byte array for directory info
      int newFileSize = fsize.length * 4;
      int newNameSize = fnames.length * maxChars * 2;
      
      byte[] data = new byte[newFileSize + newNameSize];
      int offset = 0;
      
      // fsize array, get size and convert to bytes
      for (int i = 0; i < fsize.length; i++)
      {
		  SysLib.int2bytes(fsize[i], data, offset);
		  offset += 4;
	  }
	  
	  // fnames array, get name and turn into bytes
	  for (int i = 0; i < fnames.length; i++)
	  {
		  String tempName = new String(fnames[i], 0, fsize[i]);
		  byte[] buffer = tempName.getBytes();
		  
		  // copy string into data
		  System.arraycopy(buffer, 0, data, offset, buffer.length);
		  offset += (maxChars * 2);
	  }
	  
	  return data;
   }

   public short ialloc( String filename ) 
   {
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
      
      for (int i = 0; i < fsize.length; i++)
      {
		  if (fsize[i] == 0)
		  {
			  int fileSize = filename.length();
			  
			  // check if size is out of bounds
			  if (fileSize > maxChars)
			  {
				  fileSize = maxChars;
			  }
			  
			  // initialize the size
			  fsize[i] = fileSize;
			  
			  // copy characters from string to fnames
			  // getChars(sourceBegin, sourceEnd, dest, destBegin)
			  filename.getChars(0, fsize[i], fnames[i], 0);
			  return (short) i;
		  }
	  }
	  
	  // error
	  return -1;
   }

   public boolean ifree( short iNumber ) 
   {
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted.
      
      // check if negative
      if ((iNumber < 0) || (fsize[iNumber] <= 0))
      {
		  return false;
	  }
	  
	  fsize[iNumber] = 0;
	  return true;
   }

   public short namei( String filename ) 
   {
      // returns the inumber corresponding to this filename
      int fileSize = filename.length();
      
      for (int i = 0; i < fsize.length; i++)
      {
		  // decodes the string from fnames array
		  String tempName = new String(fnames[i], 0, fsize[i]);
			  
		  // check if strings are equal
		  if (tempName.equals(filename))
		  {
			  return (short) i;
		  }
	  }
	  
	  // error
	  return -1;
   }
}
