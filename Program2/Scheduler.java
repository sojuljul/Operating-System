// Juliano Nguyen
// CSS 430
// 10/22/2017
// Program 2 - Part 2 - Multilevel Feedback Scheduler (MFQS)
//			Q0 runs 500ms, if not complete move TCB to Q1
//			Q1 runs 1000ms, if not complete move TCB to Q2
//			Q2 runs 2000ms, if not complete move TCB to Q2
//			Q1 - check if Q0 has threads, suspend and run Q0
//			Q2 - check if Q0 and Q1 has threads, suspend and run Q0, Q1

import java.util.*;

public class Scheduler extends Thread
{
	// 3 queues for MFQS, Q0, Q1, Q2
    private Vector queue; // Q0
	private Vector queueOne; // Q1
	private Vector queueTwo; // Q2
	
	// change default time slice to 500ms
    private int timeSlice;
    private static final int DEFAULT_TIME_SLICE = 1000;

    // New data added to p161 
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;

    // A new feature added to p161 
    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;
    private void initTid( int maxThreads ) 
	{
		tids = new boolean[maxThreads];
		for ( int i = 0; i < maxThreads; i++ )
		{
			tids[i] = false;
		}
    }

    // A new feature added to p161 
    // Search an available thread ID and provide a new thread with this ID
    private int getNewTid( ) 
	{
		for ( int i = 0; i < tids.length; i++ ) 
		{
			int tentative = ( nextId + i ) % tids.length;
			if ( tids[tentative] == false ) 
			{
				tids[tentative] = true;
				nextId = ( tentative + 1 ) % tids.length;
				return tentative;
			}
		}
		return -1;
    }

    // A new feature added to p161 
    // Return the thread ID and set the corresponding tids element to be unused
    private boolean returnTid( int tid ) 
	{
		if ( tid >= 0 && tid < tids.length && tids[tid] == true ) 
		{
			tids[tid] = false;
			return true;
		}
		return false;
    }

    // A new feature added to p161 
    // Retrieve the current thread's TCB from the queue
    public TCB getMyTcb( ) 
	{
		Thread myThread = Thread.currentThread( ); // Get my thread object
		// Lock Q0 so no new threads can go in until done
		synchronized( queue ) 
		{
			for ( int i = 0; i < queue.size( ); i++ ) 
			{
				TCB tcb = ( TCB )queue.elementAt( i );
				Thread thread = tcb.getThread( );
				if ( thread == myThread ) // if this is my TCB, return it
				{
					return tcb;
				}
			}
		}
		
		// Lock Q1, so no new threads can go in until done
		synchronized (queueOne)
		{
			// if not in Q0, get TCB in Q1
			for (int i = 0; i < queueOne.size(); i++)
			{
				TCB tcb = (TCB) queueOne.elementAt(i);
				Thread thread = tcb.getThread();
				if (thread == myThread)
				{
					return tcb;
				}
			}
		}
		
		// Lock Q2, so no new threads can go in until done
		synchronized (queueTwo)
		{
			// if not in Q1, get TCB in Q2
			for (int i = 0; i < queueTwo.size(); i++)
			{
				TCB tcb = (TCB) queueTwo.elementAt(i);
				Thread thread = tcb.getThread();
				if (thread == myThread)
				{
					return tcb;
				}
			}
		}
		
		return null;
    }

    // A new feature added to p161 
    // Return the maximal number of threads to be spawned in the system
    public int getMaxThreads( ) 
	{
		return tids.length;
    }

    public Scheduler( ) 
	{
		timeSlice = DEFAULT_TIME_SLICE;
		// include 3 queues for MFQS
		queue = new Vector( );
		queueOne = new Vector();
		queueTwo = new Vector();
		initTid( DEFAULT_MAX_THREADS );
    }

    public Scheduler( int quantum ) 
	{
		timeSlice = quantum;
		// include 3 queues for MFQS
		queue = new Vector( );
		queueOne = new Vector();
		queueTwo = new Vector();
		initTid( DEFAULT_MAX_THREADS );
    }

    // A new feature added to p161 
    // A constructor to receive the max number of threads to be spawned
    public Scheduler( int quantum, int maxThreads ) 
	{
		timeSlice = quantum;
		// include 3 queues for MFQS
		queue = new Vector( );
		queueOne = new Vector();
		queueTwo = new Vector();
		initTid( maxThreads );
    }

    private void schedulerSleep( ) 
	{
		try 
		{
			Thread.sleep( timeSlice );
		} 
		catch ( InterruptedException e ) 
		{
		}
    }

    // A modified addThread of p161 example
    public TCB addThread( Thread t ) 
	{
		//t.setPriority( 2 );
		TCB parentTcb = getMyTcb( ); // get my TCB and find my TID
		int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
		int tid = getNewTid( ); // get a new TID
		if ( tid == -1)
			return null;
		TCB tcb = new TCB( t, tid, pid ); // create a new TCB
		queue.add( tcb );
		return tcb;
    }

    // A new feature added to p161
    // Removing the TCB of a terminating thread
    public boolean deleteThread( ) 
	{
		TCB tcb = getMyTcb( ); 
		if ( tcb!= null )
			return tcb.setTerminated( );
		else
			return false;
    }

    public void sleepThread( int milliseconds ) 
	{
		try 
		{
			sleep( milliseconds );
		} 
		catch ( InterruptedException e ) { }
    }
    
	// Runs for Q0 if not empty
	// Total time slice is 500ms
	// If thread not done, add to Q1
	public void queueRun()
	{
		Thread current = null;
		
		// Keep looping until Q0 is empty
		while (queue.size() > 0)
		{
			TCB currentTCB = (TCB) queue.firstElement();
			
			if ( currentTCB.getTerminated( ) == true ) 
			{
				queue.remove( currentTCB );
				returnTid( currentTCB.getTid( ) );
				continue;
			}
			
			current = currentTCB.getThread( );
			
			if ( current != null ) {
				if ( current.isAlive( ) )
				{
					current.resume();
				}
				else 
				{
					// Spawn must be controlled by Scheduler
					// Scheduler must start a new thread
					current.start( ); 
				}
			}
			
			// 500ms
			sleepThread(DEFAULT_TIME_SLICE / 2);
			
			// System.out.println("* * * Context Switch * * * ");

			// lock Q0, so only one thread at a time
			// Add to Q1 if not done
			synchronized ( queue ) 
			{
				if ( current != null && current.isAlive( ) )
				{
					current.suspend();
					queue.remove( currentTCB ); // rotate this TCB to the end
					queueOne.add( currentTCB );
				}
			}
		}
	}
	
	// Runs for Q1 if not empty
	// Total time slice is 1000ms
	// If thread not done, add to Q2
	public void queueOneRun()
	{
		Thread current = null;
		
		// Keep looping until Q1 is empty
		while (queueOne.size() > 0)
		{
			TCB currentTCB = (TCB) queueOne.firstElement();
			
			if ( currentTCB.getTerminated( ) == true ) 
			{
				queueOne.remove( currentTCB );
				returnTid( currentTCB.getTid( ) );
				continue;
			}
			
			current = currentTCB.getThread( );
			
			if ( current != null ) {
				if ( current.isAlive( ) )
				{
					current.resume();
				}
				else 
				{
					// Spawn must be controlled by Scheduler
					// Scheduler must start a new thread
					current.start( ); 
				}
			}
			
			// 500 ms
			sleepThread(DEFAULT_TIME_SLICE / 2);
			
			// check if Q0 is NOT empty
			// interrupt current thread
			// execute threads in Q0
			// resume interrupted thread
			if (queue.size() != 0)
			{
				current.suspend();
				queueRun();
				current.resume();
			}
			
			// 1000 ms
			sleepThread(DEFAULT_TIME_SLICE / 2);
			
			// System.out.println("* * * Context Switch * * * ");

			// lock Q1
			// Add to Q2 if not done
			synchronized ( queueOne ) 
			{
				if ( current != null && current.isAlive( ) )
				{
					current.suspend();
					queueOne.remove( currentTCB ); // rotate this TCB to the end
					queueTwo.add( currentTCB );
				}
			}
		}
	}
	
	// Runs for Q2 if not empty
	// Total time slice is 2000ms
	// If thread not done, add to Q2
	public void queueTwoRun()
	{
		Thread current = null;
		
		// Keep looping until Q2 is empty
		while (queueTwo.size() > 0)
		{
			TCB currentTCB = (TCB) queueTwo.firstElement();
			
			if ( currentTCB.getTerminated( ) == true ) 
			{
				queueTwo.remove( currentTCB );
				returnTid( currentTCB.getTid( ) );
				continue;
			}
			
			current = currentTCB.getThread( );
			
			if ( current != null ) {
				if ( current.isAlive( ) )
				{
					current.resume();
				}
				else 
				{
					// Spawn must be controlled by Scheduler
					// Scheduler must start a new thread
					current.start( ); 
				}
			}
			
			// loop 4 times: 500ms * 4 = 2000ms
			for (int i = 0; i < 4; i++)
			{
				sleepThread(DEFAULT_TIME_SLICE / 2); // 500ms
				
				// check if Q0 is NOT empty
				// check if Q1 is NOT empty
				if (queue.size() != 0 || queueOne.size() != 0)
				{
					current.suspend();
					
					// Execute all threads in Q0
					queueRun();
					
					// Execute all threads in Q1
					queueOneRun();					
					
					// resume only when Q0 and Q1 are finished
					current.resume();
				}
			}
			
			// System.out.println("* * * Context Switch * * * ");

			// lock Q2
			// Add to tail of Q2 if not done
			synchronized ( queueTwo ) 
			{
				if ( current != null && current.isAlive( ) )
				{
					//current.setPriority( 2 );
					current.suspend();
					queueTwo.remove( currentTCB ); // rotate this TCB to the end
					queueTwo.add( currentTCB );
				}
			}
		}
	}
	
    // A modified run of p161
    public void run( ) 
	{
		//Thread current = null;
		//this.setPriority( 6 );
		
		while ( true ) 
		{
			try 
			{
				// get the next TCB and its thread
				// check for Q0, Q1, Q2 size == 0
				if (queue.size( ) == 0 && queueOne.size() == 0 && queueTwo.size() == 0)
				{
					continue;
				}
				
				// if Q0 is NOT empty, run Q0
				if (queue.size() != 0)
				{
					queueRun();
				}
				
				// if Q0 is empty, run Q1
				if (queue.size() == 0)
				{
					queueOneRun();
				}
				
				// if Q0 and Q1 are empty, run Q2
				if (queue.size() == 0 && queueOne.size() == 0)
				{
					queueTwoRun();
				}
				
			} catch ( NullPointerException e3 ) { };
		}
    }
}
