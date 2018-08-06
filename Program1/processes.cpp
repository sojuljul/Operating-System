// Author: Juliano Nguyen
// 10/10/17
// CSS 430
// Program 1 - Part 1 - processes.cpp

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <wait.h>
#include <iostream>
#include <string>
using namespace std;


// This program demonstrates the same behavior as ps -A | grep argv[1] | wc -l
// Creates processes for parent, child, grand child, great grand child
// Creates a pipe for ggc and gc; gc and child
// Parent waits until child is done
int main(int argc, char *argv[])
{
	enum {READ, WRITE};
	int pipeFD[2]; 		// pipe one
	int pipeFDTwo[2];	// pipe two
	
	int pid;	// process identifier

	// Check if there are arguments
	// processes ? = 2 argc
	if (argc < 2)
	{
		perror("few arguments");
		return 0;
	}
	
	// Checks for pipe errors
	if (pipe(pipeFD) < 0)		// creates first pipe
	{
		perror("pipe error");
	}
	if (pipe(pipeFDTwo) < 0)	// creates second pipe
	{
		perror("pipe error");
	}
	
	// ps -A | grep argv[1] | wc -l
	// child = wc -l
	// grand child = grep argv[1]
	// great grand child = ps -A
	
	// Create processes
	// Check for fork errors
	if ((pid = fork()) < 0) // creates child from pid = fork()
	{
		perror("fork error");
		return EXIT_FAILURE;
	}
	else if (pid == 0) // child
	{
		if ((pid = fork()) < 0) // creates grand child
		{
			perror("fork error");
			return EXIT_FAILURE;
		}
		else if (pid == 0) // grand child
		{
			if ((pid = fork()) < 0) // creates great grand child
			{
				perror("fork error");
				return EXIT_FAILURE;
			}
			else if (pid == 0) // great grand child
			{
				// close pipe 2
				close(pipeFDTwo[READ]);
				close(pipeFDTwo[WRITE]);
				
				// close pipe 1's read
				close(pipeFD[READ]);
				dup2(pipeFD[WRITE], 1);
				
				// execute ps -A
				execlp("ps", "ps", "-A", NULL);
			}	// end of great grand child
			else // grand child process
			{	
				// close pipe 1's write
				close(pipeFD[WRITE]);
				dup2(pipeFD[READ], 0);
				
				// close pipe 2's read
				close(pipeFDTwo[READ]);
				dup2(pipeFDTwo[WRITE], 1);
				
				// execute grep argv[1]
				execlp("grep", "grep", argv[1], NULL);
			}
		}	// end of grand child
		else // child process
		{
			// close pipe 1
			close(pipeFD[READ]);
			close(pipeFD[WRITE]);
			
			// close pipe 2's write
			close(pipeFDTwo[WRITE]);
			dup2(pipeFDTwo[READ], 0);
			
			// execute wc -l
			execlp("wc", "wc", "-l", NULL);
		}
	} 	// end of child
	else // parent process
	{
		// close both pipes
		close(pipeFD[READ]);
		close(pipeFD[WRITE]);
			
		close(pipeFDTwo[READ]);
		close(pipeFDTwo[WRITE]);
		
		wait(NULL); // wait until child is done processing
	}
	
	return 0;
}
