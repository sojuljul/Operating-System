// Juliano Nguyen
// CSS 430
// Program 3 - Part 1
// QueueNode will be utilized by SyncQueue
// to put threads to sleep/wake them up

import java.util.*;

public class QueueNode
{
	private Vector<Integer> childQueue; // vector of child IDs
	
	// Constructor that creates a new vector
	public QueueNode()
	{
		childQueue = new Vector<Integer>();
	}
	
	// use wait() to make a thread sleep
	// checks if Vector queue is empty
	// if so, put itself to sleep
	// upon wakeup, pick first ID from queue as return value
	public synchronized int sleep()
	{
		if (childQueue.size() == 0)
		{
			try
			{
				wait(); // thread put to sleep
			}
			catch (InterruptedException e)
			{
				SysLib.cerr("Error \n");
			}
			
			// return first ID from queue
			return childQueue.remove(0);
		}
		else
		{
			// fail to get ID from queue
			return -1;
		}
	}
	
	// use notify() to wake up a thread
	// enqueues child TID in vector queue
	public synchronized void wakeup(int tid)
	{
		childQueue.add(tid);
		notify(); // notifies a waiting thread
	}
}