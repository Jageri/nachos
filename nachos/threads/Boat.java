package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat {
	static BoatGrader bg;// bg��BoatGrader(���ϵ���)��һ������
	// ��ʼ����O ��M���ϵĴ��˺ͺ�����
	static int childrenOnOahu = 0;
	static int childrenOnMolokai = 0;
	static int adultOnOahu = 0;
	static int adultOnMolokai = 0;
	static int pilot = 0;// ��ʻԱ ԭʼ��0�� O����M����С����1�� O����M���Ĵ��ˣ�2�� M����O����С����3
	static boolean done;
	static Lock lock1;
	static Condition childrenWaitOnOahu;
	static Lock lock2;
	static Condition adultWaitOnOahu;
	static Lock lock3;
	static Condition childrenReadyOnMolokai;// ��M���ϵȴ��ĺ���

	public static void selfTest() {
		BoatGrader b = new BoatGrader();

		System.out.println("\n ***Testing Boats ...........***");
		begin(2, 4, b);

	}

	public static void begin(int adults, int children, BoatGrader b) {
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;
		lock1 = new Lock();
		childrenWaitOnOahu = new Condition(lock1);
		lock2 = new Lock();
		adultWaitOnOahu = new Condition(lock2);
		lock3 = new Lock();
		childrenReadyOnMolokai = new Condition(lock3);
		for (int i = 0; i < adults; i++) {
			new KThread(new Adult(childrenWaitOnOahu, adultWaitOnOahu, childrenReadyOnMolokai)).setName("adult").fork();
		}
		for (int i = 0; i < children - 1; i++) {
			new KThread(new Child(childrenWaitOnOahu, adultWaitOnOahu, childrenReadyOnMolokai)).setName("child").fork();
		}
		KThread t = new KThread(new Child(childrenWaitOnOahu, adultWaitOnOahu, childrenReadyOnMolokai));
		t.setName("child");//
		t.fork();
		KThread.currentThread().yield();
		lock1.acquire();
		childrenWaitOnOahu.wake();
		lock1.release();
		t.join();// ���뵽�ȴ�����

	}

	static void AdultItinerary()// ���˵�·��
	{
		bg.initializeAdult();// ����һ�������Ĵ���
		// �����˵��̳߳�ʼ��

		adultOnOahu++;// �������еĻ����Ĵ���
		lock2.acquire();
		adultWaitOnOahu.sleep();
		bg.AdultRowToMolokai();
		adultOnOahu--;
		adultOnMolokai++;
		lock2.release();
		lock3.acquire();// �����
		childrenReadyOnMolokai.wake();
		lock3.release();// �ͷ���
	}

	static void ChildItinerary()// ���ӵ�·��
	{
		bg.initializeChild();// ����һ�������ĺ���

		// Required for autograder interface. Must be the first thing called.
		// DO NOT PUT ANYTHING ABOVE THIS LINE.
		childrenOnOahu++;// �������еĻ����ĺ���
		lock1.acquire();
		childrenWaitOnOahu.sleep();
		lock1.release();
		while (true) {
			if (pilot != 1) {
				if (childrenOnOahu > 1) // �к���
				{
					lock1.acquire();
					childrenWaitOnOahu.wake();
					pilot = 1;// �˺���
					bg.ChildRowToMolokai();
					childrenOnOahu--;
					childrenOnMolokai++;
					lock1.release();
					lock3.acquire();// ����һ������
					childrenReadyOnMolokai.sleep();
					lock3.release();
				} else {// ���ߴ�����
					lock2.acquire();
					adultWaitOnOahu.wake();
					lock2.release();
					lock1.acquire();
					childrenWaitOnOahu.sleep();
					lock1.release();
					continue;
				}
			} else {
				if (adultOnOahu != 0) {
					bg.ChildRideToMolokai();
					childrenOnOahu--;
					childrenOnMolokai++;
					lock3.acquire();
					childrenReadyOnMolokai.wake();
					lock3.release();
					lock3.acquire();
					childrenReadyOnMolokai.sleep();
					lock3.release();
				} else// ʣ�µĺ��Ӵ�O����M��
				{
					lock3.acquire();
					done = true;
					bg.ChildRideToMolokai();
					childrenOnOahu--;
					childrenOnMolokai++;
					childrenReadyOnMolokai.wakeAll();
					lock3.release();
				}
			}
			// �������Ƿ����Ĳ���
			if (done == true) {
				break;
			} else// ��ʾû�н���
			{
				pilot = 3;
				bg.ChildRowToOahu();
				childrenOnOahu++;
				childrenOnMolokai--;
				continue;
			}
		}
	}

	static void SampleItinerary() {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

	private static class Child implements Runnable {
		Child(Condition childrenWaitOnOahu, Condition adultWaitOnOahu, Condition childrenReadyOnMolokai) {
			this.location_now = location_now;
			this.childrenWaitOnOahu = childrenWaitOnOahu;
			this.adultWaitOnOahu = adultWaitOnOahu;
			this.childrenReadyOnMolokai = childrenReadyOnMolokai;
		}

		public void run() {
			ChildItinerary();
		}

		private int Status;// 1:��childrenWaitOnOahu������2����childrenReadyOnMolokai����
		private int location_now;// 1:Oahu,2:Molokai
		private Condition childrenWaitOnOahu;
		private Condition adultWaitOnOahu;
		private Condition childrenReadyOnMolokai;
	}

	private static class Adult implements Runnable {
		Adult(Condition childrenWaitOnOahu, Condition adultWaitOnOahu, Condition childrenReadyOnMolokai) {

			this.childrenWaitOnOahu = childrenWaitOnOahu;
			this.adultWaitOnOahu = adultWaitOnOahu;
			this.childrenReadyOnMolokai = childrenReadyOnMolokai;
		}

		public void run() {
			AdultItinerary();
		}

		private Condition childrenWaitOnOahu;
		private Condition adultWaitOnOahu;
		private Condition childrenReadyOnMolokai;
	}
}
