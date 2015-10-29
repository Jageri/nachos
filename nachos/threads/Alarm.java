package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

//Alarm的作用是实现可对多个进程进行阻塞，并在规定的时间将进程唤醒，
//因此要存储多个进程的进程名及其唤醒时间，

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		waitlist = new LinkedList<Waiter>();// 维护一个waiter队列，且在链表中储存，把线程和她的时间对应
		Machine.timer().setInterruptHandler(new Runnable() {// 调用回调函数
			public void run() {
				timerInterrupt();
			}// 500时钟中断一次
		});// 该类的Lctest 中有RUN 方法的的声明
			// interrupt的的声明在该类中
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	// timerInterrupt函数是实现将阻塞队列中的进程在其唤醒时间将其唤醒。
	/*
	 * timerInterrupt函数是实现将阻塞队列中的进程在其唤醒时间将其唤醒。
	 * 为了实现能够对阻塞队列中的进程进行循环判断，因此需创建Iterator的一个对象，
	 * 然后再利用Iterator的hasNext来对阻塞队列中的进程进行循环判断， 如果该进程的唤醒时间大于当前时间，则对下一个进程进行判断，否则就将
	 * 该进程唤醒，然后从阻塞队列中将其移除，再对下一个进程进行判断， 最后将当前进程由运行状态转换成就绪状态，运行下一个进程。
	 * 
	 */
	public void timerInterrupt() { // 应该在此处检查所有sleep到时间了吗，设置回调函数
		Waiter waiter;
		for (int i = 0; i < waitlist.size(); i++)// 将waterlist的对象全部遍历
		{
			waiter = waitlist.remove();
			if (waiter.wakeTime <= Machine.timer().getTime()) // 现在的时间
			{
				waiter.thread.ready();
			} // 加入就绪队列
			else
				waitlist.add(waiter);// 加入到等待队列中
		}
		KThread.currentThread().yield();// 释放cpu

	}

	public void waitUntil(long x)// 挂起函数
	{
		boolean intStatus = Machine.interrupt().disable();// 关中断
		// for now, cheat just to get something working (busy waiting is bad)
		long wakeTime = Machine.timer().getTime() + x;// 醒着的时间等于现在的时间，一个线程通过调用函数挂起自己，才会被唤醒
		Waiter waiter = new Waiter(wakeTime, KThread.currentThread());
		waitlist.add(waiter);// 来储存进程当前的信息
		System.out.println("我是" + KThread.currentThread().getName() + ",我会在" + wakeTime + "醒来，我睡去的时间是"
				+ Machine.timer().getTime());
		KThread.sleep();
		Machine.interrupt().restore(intStatus);// 开中断
	}

	private static class LcTest implements Runnable // 测试类
	{
		LcTest() {

		}

		public void run() {
			for (int i = 0; i < 2; i++) // 测试次数
			{
				ThreadedKernel.alarm.waitUntil(1000);// 调用上面的waitUntil
				// System.out.println("我是" + KThread.currentThread().getName() +
				// "现在我说" + i);
			}
		}

	}

	public void selfTest() {
		new KThread(new LcTest()).setName("test").fork();// 创建一个叫
														// lc的线程和tcb程序控制块，并且将他加入到ready队列
		waitUntil(100000);// 测试alarm类
		System.out.println("现在我醒过来了，现在时间是" + Machine.timer().getTime() + "\n");
	}

	class Waiter {
		Waiter(long wakeTime, KThread thread) {// waketime是等待时间，thread是等待队列的线程
			this.wakeTime = wakeTime;// 存储当前进程的信息
			this.thread = thread;
		}

		private long wakeTime;
		private KThread thread;
	}

	private LinkedList<Waiter> waitlist;
}
