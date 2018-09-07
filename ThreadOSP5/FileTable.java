import java.util.*;

public class FileTable {
	
	private Vector <FileTableEntry> table;
	private Directory dir;
	
	public FileTable(Directory directory){
		table = new Vector<FileTableEntry>();
		dir = directory;
	}
	
	public synchronized FileTableEntry falloc(String fname, String mode){
		short iNumber = -1;
		Inode inode = null;
		
		while (true) {
			iNumber = (fname.equals( "/" ) ? 0 : dir.namei(fname));
			if(iNumber >= 0) {
				inode = new Inode(iNumber);
				if(mode.equals("r")) {
					if (inode.flag == 2 || inode.flag == 1 || inode.flag == 0) {//if inode.flag is "read"
						//inode.flag = 2; //set to read
						break;
					} else if (inode.flag == 3 ) {// inode.flag is "write"
						try {
							wait();
						} catch (InterruptedException e){}
							 
					//} else if () { //inode.flag is "to be deleted"
					//	iNumber = -1;
					//	return null
					}
				} else if (mode.equals("w")) { //
						if(inode.flag == 1 || inode.flag == 0){
							inode.flag = 3; // set to write
							break;
						}else{
							try {
								wait();
							} catch (InterruptedException e){}
						}
				} 
				
			} else if (!mode.equals("r")) { // create new
				iNumber = dir.ialloc(fname);
				inode = new Inode(iNumber);
				inode.flag = 3; //write
				break;
			} else return null;
		}
		inode.count++;
		inode.toDisk(iNumber );
		FileTableEntry e = new FileTableEntry( inode, iNumber, mode);
		table.addElement (e);
		return e;
	}
	/*
	 * Unused = 0
	 * Used = 1
	 * Read = 2
	 * Write = 3
	 * 
	 */
	public synchronized boolean ffree(FileTableEntry e){
		Inode inode = new Inode(e.iNumber);
		
		if(table.remove(e)){
			if (inode.flag == 2 && inode.count == 1){
				notify();
				inode.flag = 1;
				
			} else if(inode.flag == 3){
				inode.flag = 1;
				notifyAll();
			}
			
			inode.count--;
			
			inode.toDisk(e.iNumber);
			return true;
			
		}
		return false;
		
	}
	
	public synchronized boolean fempty() {
		return table.isEmpty();
	}
	
}
