package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

public class Communicator {
	
	  public Communicator() 
	    {
	    	lock = new Lock();
	    	speaker = new Condition2(lock);
	    	listener = new Condition2(lock);
	    }
	
	private Lock lock; // 互斥锁
	private int word = 0;
	private static int speakercount = 0;// 说话者数量
	private static int listenercount = 0; // 听者数量
	LinkedList<Integer> Wordqueue = new LinkedList<Integer>();// 保存说者话语的队列
	Condition2 speaker ;// 说者条件变量
	Condition2 listener;// 听者条件变量

	public void speak(int word) {
		boolean intStatus = Machine.interrupt().disable();// 关中断
		lock.acquire();// 拿到锁
		if (listenercount == 0) // 没有听者，说者睡眠，队列存储说者的话
		{
			speakercount++;
			Wordqueue.offer(word);
			speaker.sleep();
			listener.wake();// 尝试唤醒听者
			speakercount--;// 说者队列人数减一
		} else {
			Wordqueue.offer(word);// 准备消息，唤醒听者
			listener.wake();
		}
		lock.release();// 释放锁
		Machine.interrupt().restore(intStatus);// 开中断
		System.out.println(KThread.currentThread().getName()+"说"+word);
		return;
	}

	public int listen() {
		boolean intStatus = Machine.interrupt().disable();// 关中断
		lock.acquire();// 获得锁
		if (speakercount != 0) {
			speaker.wake();// 有说者
			listener.sleep();// 唤醒听者
		} else {
			listenercount++;// 听者数量加一
			listener.sleep();// 听者睡眠
			listenercount--;// 听者数量减一
		}
		System.out.println(KThread.currentThread().getName()+"听到了"+Wordqueue.getFirst());
		lock.release();// 释放锁
		
		Machine.interrupt().restore(intStatus);// 开中断
		
		
		return Wordqueue.poll();
	}

	private static class CommunicatorTest implements Runnable {
		CommunicatorTest(Communicator communicator) {
			this.communicator = communicator;
		}

		public void run() {
			communicator.listen();
			communicator.listen();// 两个听者

		}

		private Communicator communicator;
	}

	public static void selfTest() {
		Communicator communicator = new Communicator();
		new KThread(new CommunicatorTest(communicator)).setName("Communicator1").fork();
		KThread.currentThread().yield();// 让线程让出CPU
		communicator.speak(1);
		communicator.speak(2);// 两个人说
		//KThread.currentThread().yield();// 让线程让出CPU
		KThread.currentThread().yield();// 让线程让出CPU
	}

}
