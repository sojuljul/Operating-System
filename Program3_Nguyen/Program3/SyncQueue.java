// Juliano Nguyen
// CSS 430
// Program 3 - Part 1
// SyncQueue will have enqueueAndSleep and dequeueAndWakeup
// It will sleep/wake threads based on condition (thread ID)
// Uses QueueNode to create objects
// Assuming condition is not a negative number

public class SyncQueue
{
	private QueueNode[] queue; // array of QueueNode objects
	public static int DEFAULT_CONDITION = 10;
	
	// Constructor that creates a queue
	// allow threads to wait for a default condition number = 10
	public SyncQueue()
	{
		queue = new QueueNode[DEFAULT_CONDITION];
		
		// initialize each element in queue
		for (int i = 0; i < DEFAULT_CONDITION; i++)
		{
			queue[i] = new QueueNode();
		}
	}
	
	// Constructor that creates a queue
	// allow threads to wait for a condMax number
	public SyncQueue(int condMax)
	{
		queue = new QueueNode[condMax];
		
		// initialized each element in queue
		for (int i = 0; i < condMax; i++)
		{
			queue[i] = new QueueNode();
		}
	}
	
	// enqueues calling thread and waits for condition
	// returns ID of child thread that woke calling thread
	public int enqueueAndSleep(int condition)
	{
		return queue[condition].sleep();
	}
	
	// dequeues and wakes up thread waiting for condition
	// only 1 thread is dequeued and resumed
	// regard tid as 0
	public void dequeueAndWakeup(int condition)
	{
		queue[condition].wakeup(0);
	}
	
	// dequeues and wakes up thread waiting for condition
	// only 1 thread is dequeued and resumed
	// tid passed to thread that woke up from enqueueAndSleep
	public void dequeueAndWakeup(int condition, int tid)
	{
		queue[condition].wakeup(tid);
	}
}