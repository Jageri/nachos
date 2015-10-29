package nachos.threads;

import nachos.machine.*;

public class KThread {
	public static KThread currentThread() // 静态类
	{
		Lib.assertTrue(currentThread != null);// 若当前线程不为空，则返回当前线程
		return currentThread;
	}

	public KThread() {
		boolean status = Machine.interrupt().disable();
		waitForJoin.acquire(this);
		Machine.interrupt().restore(status);
		if (currentThread != null) {
			tcb = new TCB();// 建立新的程序控制块
		} else {
			readyQueue = ThreadedKernel.scheduler.newThreadQueue(false);
			// 就绪队列
			readyQueue.acquire(this);

			currentThread = this;
			tcb = TCB.currentTCB();
			name = "main";
			restoreState();// 再次保存状态

			createIdleThread();// 闲逛线程
		}
	}

	public KThread(Runnable target) {
		this();
		this.target = target;
	}

	public KThread setTarget(Runnable target) {
		Lib.assertTrue(status == statusNew);

		this.target = target;
		return this;
	}

	public KThread setName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return (name + " (#" + id + ")");
	}

	public int compareTo(Object o) {
		KThread thread = (KThread) o;

		if (id < thread.id)
			return -1;
		else if (id > thread.id)
			return 1;
		else
			return 0;
	}

	public void fork() {
		Lib.assertTrue(status == statusNew);
		Lib.assertTrue(target != null);

		Lib.debug(dbgThread, "Forking thread: " + toString() + " Runnable: " + target);

		boolean intStatus = Machine.interrupt().disable();// 关中断

		tcb.start(new Runnable() {
			public void run() {
				runThread();
			}
		});

		ready();

		Machine.interrupt().restore(intStatus);// 开中断
	}

	private void runThread() {
		begin();
		target.run();
		finish();
	}

	private void begin() {
		Lib.debug(dbgThread, "Beginning thread: " + toString());

		Lib.assertTrue(this == currentThread);

		restoreState();// 将就绪队列清空

		Machine.interrupt().enable();
	}

	public static void finish() {
		Lib.debug(dbgThread, "Finishing thread: " + currentThread.toString());

		Machine.interrupt().disable();

		Machine.autoGrader().finishingCurrentThread();

		Lib.assertTrue(toBeDestroyed == null);
		toBeDestroyed = currentThread;

		currentThread.status = statusFinished;// 当前的状态是finished

		KThread waitThread;
		while ((waitThread = currentThread.waitForJoin.nextThread()) != null) {
			waitThread.ready();
		}

		sleep();
	}

	public static void yield() // 屈服
	{
		Lib.debug(dbgThread, "Yielding thread: " + currentThread.toString());

		Lib.assertTrue(currentThread.status == statusRunning);

		boolean intStatus = Machine.interrupt().disable();

		currentThread.ready();// 将正在运行的线程舍弃

		runNextThread();// 运行下一个线程

		Machine.interrupt().restore(intStatus);// 重新保存当前的状态
	}

	public static void sleep() {
		// System.out.println("执行sleep()");
		Lib.debug(dbgThread, "Sleeping thread: " + currentThread.toString());

		Lib.assertTrue(Machine.interrupt().disabled());

		if (currentThread.status != statusFinished)
			currentThread.status = statusBlocked;// 锁住当前线程

		runNextThread();// 运行下一个线程
	}

	public void ready() {
		Lib.debug(dbgThread, "Ready thread: " + toString());

		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(status != statusReady);

		status = statusReady;
		if (this != idleThread)
			readyQueue.waitForAccess(this);// 将线程加入到就绪队列中

		Machine.autoGrader().readyThread(this);
	}

	public void join() {

		Lib.debug(dbgThread, "Joining to thread: " + toString());

		Lib.assertTrue(this != currentThread);

		boolean intStatus = Machine.interrupt().disable();// 关中断

		if (status != statusFinished) {
			waitForJoin.waitForAccess(currentThread);// 将进程挂到等待队列上去
			sleep();
		}

		Machine.interrupt().restore(intStatus);
	}

	private static void createIdleThread() {
		Lib.assertTrue(idleThread == null);

		idleThread = new KThread(new Runnable() {
			public void run() {
				while (true) {
					// System.out.println("我是" +
					// KThread.currentThread().getName() + "现在时间是" +
					// Machine.timer().getTime());
					yield();
				}
			}
		});
		idleThread.setName("idle");

		Machine.autoGrader().setIdleThread(idleThread);

		idleThread.fork();
	}

	/**
	 * Determine the next thread to run, then dispatch the CPU to the thread
	 * using <tt>run()</tt>.
	 */
	private static void runNextThread()// 运行下一个线程
	{
		KThread nextThread = readyQueue.nextThread();
		if (nextThread == null)
			nextThread = idleThread;

		nextThread.run();
	}

	private void run() {
		Lib.assertTrue(Machine.interrupt().disabled());

		Machine.yield();

		currentThread.saveState();// 保存当前的状态

		Lib.debug(dbgThread, "Switching from: " + currentThread.toString() + " to: " + toString());

		currentThread = this;

		tcb.contextSwitch();

		currentThread.restoreState();
	}

	protected void restoreState() {
		Lib.debug(dbgThread, "Running thread: " + currentThread.toString());

		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(this == currentThread);
		Lib.assertTrue(tcb == TCB.currentTCB());

		Machine.autoGrader().runningThread(this);

		status = statusRunning;

		if (toBeDestroyed != null) {
			toBeDestroyed.tcb.destroy();
			toBeDestroyed.tcb = null;
			toBeDestroyed = null;
		}
	}

	/**
	 * Prepare this thread to give up the processor. Kernel threads do not need
	 * to do anything here.
	 */
	protected void saveState() {
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(this == currentThread);
	}

	private static class joinTest implements Runnable {
		joinTest() {
		}

		public void run() {

			System.out.println("被等待进程执行");

		}

		private int which;
	}

	private static class PingTest implements Runnable {
		PingTest(int which) {
			this.which = which;
		}

		public void run() {
			for (int i = 0; i < 5; i++) {
				System.out.println("*** thread " + which + " looped " + i + " times");
				currentThread.yield();
			}
		}

		private int which;
	}

	public static void joinTest() {
		Lib.debug(dbgThread, "Enter KThread.test");

		KThread joined = new KThread(new joinTest());
		joined.setName("new forked thread  ").fork();
		System.out.println("原线程开始等待");
		joined.join();
		System.out.println("原线程继续执行\n");
	}

	/**
	 * Tests whether this module is working.
	 */
	public static void selfTest() {
		Lib.debug(dbgThread, "Enter KThread.selfTest");
		System.out.println("KThread selfTest");
		new KThread(new PingTest(1)).setName("forked thread").fork();
		new PingTest(0).run();
	}

	private static final char dbgThread = 't';

	/**
	 * Additional state used by schedulers.
	 *
	 * @see nachos.threads.PriorityScheduler.ThreadState
	 */
	public Object schedulingState = null;

	private static final int statusNew = 0;
	private static final int statusReady = 1;
	private static final int statusRunning = 2;
	private static final int statusBlocked = 3;
	private static final int statusFinished = 4;

	/**
	 * The status of this thread. A thread can either be new (not yet forked),
	 * ready (on the ready queue but not running), running, or blocked (not on
	 * the ready queue and not running).
	 */
	private int status = statusNew;
	private String name = "(unnamed thread)";
	private Runnable target;
	private TCB tcb;

	/**
	 * Unique identifer for this thread. Used to deterministically compare
	 * threads.
	 */
	private int id = numCreated++;
	/** Number of times the KThread constructor was called. */
	private static int numCreated = 0;

	private static ThreadQueue readyQueue = null;
	private static KThread currentThread = null;
	private static KThread toBeDestroyed = null;
	private static KThread idleThread = null;

	// 实验一等待队列
	ThreadQueue waitForJoin = ThreadedKernel.scheduler.newThreadQueue(true);
}
