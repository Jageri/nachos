package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

public class PriorityScheduler extends Scheduler {
	/**
	 * Allocate a new priority scheduler.
	 */
	public PriorityScheduler() {
	}

	public ThreadQueue newThreadQueue(boolean transferPriority)
	// 新建一个线程队列

	{
		return new PriorityQueue(transferPriority);
	}

	public int getPriority(KThread thread)// 获得优先级
	{
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getPriority();
	}

	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getEffectivePriority();
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= priorityMinimum && priority <= priorityMaximum);

		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMaximum)
			return false;

		setPriority(thread, priority + 1);
		// 实现当前线程加一

		Machine.interrupt().restore(intStatus);// 重新保存线程状态
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMinimum)
			return false;

		setPriority(thread, priority - 1);// 实现当前线程减一

		Machine.interrupt().restore(intStatus);
		return true;
	}

	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 0;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = 7;

	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	protected class PriorityQueue extends ThreadQueue {
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me
			if (waitQueue.isEmpty())
				return null;
			int max_priority = 0;
			int index = 0;
			int m = 0;
			for (Iterator i = waitQueue.iterator(); i.hasNext();) {
				ThreadState o = (ThreadState) i.next();
				if (o.priority > max_priority) {
					index = m;
					max_priority = o.priority;

				}
				m++;
			}
			ThreadState nextState = waitQueue.remove(index);
			if (holder != null) {
				holder = nextState;
			}

			return nextState.thread;
		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return the next thread that <tt>nextThread()</tt> would return.
		 */
		protected ThreadState pickNextThread() {
			// implement me
			return null;
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me (if you want)
		}

		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;
		private LinkedList<ThreadState> waitQueue = new LinkedList<ThreadState>();
		private ThreadState holder;
	}

	/**
	 * The scheduling state of a thread. This should include the thread's
	 * priority, its effective priority, any objects it owns, and the queue it's
	 * waiting for, if any.
	 *
	 * @see nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState {
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param thread
		 *            the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			this.thread = thread;

			setPriority(priorityDefault);
		}

		/**
		 * Return the priority of the associated thread.
		 *
		 * @return the priority of the associated thread.
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Return the effective priority of the associated thread.
		 *
		 * @return the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {
			// implement me

			return priority;
		}

		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param priority
		 *            the new priority.
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;

			this.priority = priority;

			// implement me
		}

		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the resource
		 * guarded by <tt>waitQueue</tt>. This method is only called if the
		 * associated thread cannot immediately obtain access.
		 *
		 * @param waitQueue
		 *            the queue that the associated thread is now waiting on.
		 *
		 * @see nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
			// implement me
			if (waitQueue.holder != null)
				if (this.priority > waitQueue.holder.priority) {
					waitQueue.holder.priority = this.priority;
				}
			waitQueue.waitQueue.add(this);
		}

		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see nachos.threads.ThreadQueue#acquire
		 * @see nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			// implement me
			Lib.assertTrue(Machine.interrupt().disabled());
			Lib.assertTrue(waitQueue.waitQueue.isEmpty());
			waitQueue.holder = this;
		}

		/** The thread with which this object is associated. */
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority;
	}

	private static class PriorityTest1 implements Runnable {
		public PriorityTest1(Lock lock) {
			this.lock = lock;
		}

		public void run() {
			boolean intStatus = Machine.interrupt().disable();
			ThreadedKernel.scheduler.setPriority(2);
			Machine.interrupt().restore(intStatus);

			System.out.println("我是*" + KThread.currentThread().getName());

			lock.acquire();

			System.out.println("我是**" + KThread.currentThread().getName());

		}

		private Lock lock;
	}

	private static class PriorityTest2 implements Runnable {

		public void run() {
			boolean intStatus = Machine.interrupt().disable();
			ThreadedKernel.scheduler.setPriority(2);
			Machine.interrupt().restore(intStatus);

			System.out.println("我是***" + KThread.currentThread().getName());

		}

	}

	public static void selfTest() {
		// Lib.assertTrue(increasePriority());
		// KThread.currentThread().schedulingState = new ThreadState(null);
		Lock lock = new Lock();
		lock.acquire();
		new KThread(new PriorityTest1(lock)).setName("Priority1").fork();
		new KThread(new PriorityTest2()).setName("Priority2").fork();
		KThread.currentThread().yield();

		System.out.println("我是****" + KThread.currentThread().getName());
		// KThread.currentThread().yield();

		lock.release();
		boolean intStatus = Machine.interrupt().disable();
		ThreadedKernel.scheduler.setPriority(1);
		Machine.interrupt().restore(intStatus);
		KThread.currentThread().yield();
		System.out.println("现在回来，即将结束，我是" + KThread.currentThread().getName());
	}
}
