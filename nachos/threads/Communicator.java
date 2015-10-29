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
	
	private Lock lock; // ������
	private int word = 0;
	private static int speakercount = 0;// ˵��������
	private static int listenercount = 0; // ��������
	LinkedList<Integer> Wordqueue = new LinkedList<Integer>();// ����˵�߻���Ķ���
	Condition2 speaker ;// ˵����������
	Condition2 listener;// ������������

	public void speak(int word) {
		boolean intStatus = Machine.interrupt().disable();// ���ж�
		lock.acquire();// �õ���
		if (listenercount == 0) // û�����ߣ�˵��˯�ߣ����д洢˵�ߵĻ�
		{
			speakercount++;
			Wordqueue.offer(word);
			speaker.sleep();
			listener.wake();// ���Ի�������
			speakercount--;// ˵�߶���������һ
		} else {
			Wordqueue.offer(word);// ׼����Ϣ����������
			listener.wake();
		}
		lock.release();// �ͷ���
		Machine.interrupt().restore(intStatus);// ���ж�
		System.out.println(KThread.currentThread().getName()+"˵"+word);
		return;
	}

	public int listen() {
		boolean intStatus = Machine.interrupt().disable();// ���ж�
		lock.acquire();// �����
		if (speakercount != 0) {
			speaker.wake();// ��˵��
			listener.sleep();// ��������
		} else {
			listenercount++;// ����������һ
			listener.sleep();// ����˯��
			listenercount--;// ����������һ
		}
		System.out.println(KThread.currentThread().getName()+"������"+Wordqueue.getFirst());
		lock.release();// �ͷ���
		
		Machine.interrupt().restore(intStatus);// ���ж�
		
		
		return Wordqueue.poll();
	}

	private static class CommunicatorTest implements Runnable {
		CommunicatorTest(Communicator communicator) {
			this.communicator = communicator;
		}

		public void run() {
			communicator.listen();
			communicator.listen();// ��������

		}

		private Communicator communicator;
	}

	public static void selfTest() {
		Communicator communicator = new Communicator();
		new KThread(new CommunicatorTest(communicator)).setName("Communicator1").fork();
		KThread.currentThread().yield();// ���߳��ó�CPU
		communicator.speak(1);
		communicator.speak(2);// ������˵
		//KThread.currentThread().yield();// ���߳��ó�CPU
		KThread.currentThread().yield();// ���߳��ó�CPU
	}

}
