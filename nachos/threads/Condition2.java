package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;
/*Condition2的作用是实现对需要使用该条件资源的进程的管理，
 * 因此需要一个等待队列将在该条件资源上阻塞的进程存储起来，
  */

public class Condition2 
{
    
    public Condition2(Lock conditionLock)
    {
	this.conditionLock = conditionLock;
    }

    //sleep函数功能是将需要使用该条件资源下的当前进程阻塞,
    //并将当前进程加入到等待队列中
    public void sleep() 
    {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	conditionLock.release();//释放条件锁
	boolean intStatus = Machine.interrupt().disable();//关中断
	/*将正在运行的线程加入到等待队列*/
	waitQueue.waitForAccess(KThread.currentThread());
	KThread.sleep();//让前线程沉睡
	
	Machine.interrupt().restore(intStatus);//开中断
	conditionLock.acquire();//获得条件锁
    }

    //wake函数的功能是将在该条件资源上阻塞的进程队列中取一个进程
    //对其进行唤醒操作
    public void wake() 
    {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean intStatus = Machine.interrupt().disable();//关中断
	KThread thread = waitQueue.nextThread();
	//将等待队列中的下一个线程加入到就绪队列
	if (thread != null) {
	    thread.ready();//加入到就绪队列
	}
	Machine.interrupt().restore(intStatus);//开中断
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	while(true)
	{
		KThread thread = waitQueue.nextThread();
		//将wait的下一个队列一个一个的加入到就绪队列
		if (thread != null) {
		    thread.ready();
		}else{
			break;
		}
	}
    }

    private Lock conditionLock;
    private ThreadQueue waitQueue =
    		ThreadedKernel.scheduler.newThreadQueue(false);
}
