package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

//Alarm��������ʵ�ֿɶԶ�����̽������������ڹ涨��ʱ�佫���̻��ѣ�
//���Ҫ�洢������̵Ľ��������份��ʱ�䣬

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
		waitlist = new LinkedList<Waiter>();// ά��һ��waiter���У����������д��棬���̺߳�����ʱ���Ӧ
		Machine.timer().setInterruptHandler(new Runnable() {// ���ûص�����
			public void run() {
				timerInterrupt();
			}// 500ʱ���ж�һ��
		});// �����Lctest ����RUN �����ĵ�����
			// interrupt�ĵ������ڸ�����
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	// timerInterrupt������ʵ�ֽ����������еĽ������份��ʱ�佫�份�ѡ�
	/*
	 * timerInterrupt������ʵ�ֽ����������еĽ������份��ʱ�佫�份�ѡ�
	 * Ϊ��ʵ���ܹ������������еĽ��̽���ѭ���жϣ�����贴��Iterator��һ������
	 * Ȼ��������Iterator��hasNext�������������еĽ��̽���ѭ���жϣ� ����ý��̵Ļ���ʱ����ڵ�ǰʱ�䣬�����һ�����̽����жϣ�����ͽ�
	 * �ý��̻��ѣ�Ȼ������������н����Ƴ����ٶ���һ�����̽����жϣ� ��󽫵�ǰ����������״̬ת���ɾ���״̬��������һ�����̡�
	 * 
	 */
	public void timerInterrupt() { // Ӧ���ڴ˴��������sleep��ʱ���������ûص�����
		Waiter waiter;
		for (int i = 0; i < waitlist.size(); i++)// ��waterlist�Ķ���ȫ������
		{
			waiter = waitlist.remove();
			if (waiter.wakeTime <= Machine.timer().getTime()) // ���ڵ�ʱ��
			{
				waiter.thread.ready();
			} // �����������
			else
				waitlist.add(waiter);// ���뵽�ȴ�������
		}
		KThread.currentThread().yield();// �ͷ�cpu

	}

	public void waitUntil(long x)// ������
	{
		boolean intStatus = Machine.interrupt().disable();// ���ж�
		// for now, cheat just to get something working (busy waiting is bad)
		long wakeTime = Machine.timer().getTime() + x;// ���ŵ�ʱ��������ڵ�ʱ�䣬һ���߳�ͨ�����ú��������Լ����Żᱻ����
		Waiter waiter = new Waiter(wakeTime, KThread.currentThread());
		waitlist.add(waiter);// ��������̵�ǰ����Ϣ
		System.out.println("����" + KThread.currentThread().getName() + ",�һ���" + wakeTime + "��������˯ȥ��ʱ����"
				+ Machine.timer().getTime());
		KThread.sleep();
		Machine.interrupt().restore(intStatus);// ���ж�
	}

	private static class LcTest implements Runnable // ������
	{
		LcTest() {

		}

		public void run() {
			for (int i = 0; i < 2; i++) // ���Դ���
			{
				ThreadedKernel.alarm.waitUntil(1000);// ���������waitUntil
				// System.out.println("����" + KThread.currentThread().getName() +
				// "������˵" + i);
			}
		}

	}

	public void selfTest() {
		new KThread(new LcTest()).setName("test").fork();// ����һ����
														// lc���̺߳�tcb������ƿ飬���ҽ������뵽ready����
		waitUntil(100000);// ����alarm��
		System.out.println("�������ѹ����ˣ�����ʱ����" + Machine.timer().getTime() + "\n");
	}

	class Waiter {
		Waiter(long wakeTime, KThread thread) {// waketime�ǵȴ�ʱ�䣬thread�ǵȴ����е��߳�
			this.wakeTime = wakeTime;// �洢��ǰ���̵���Ϣ
			this.thread = thread;
		}

		private long wakeTime;
		private KThread thread;
	}

	private LinkedList<Waiter> waitlist;
}
