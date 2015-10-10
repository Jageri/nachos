package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat {
	static BoatGrader bg;// bg是BoatGrader(船上的人)的一个对象
	// 初始化在O 和M岛上的大人和孩子数
	static int childrenOnOahu = 0;
	static int childrenOnMolokai = 0;
	static int adultOnOahu = 0;
	static int adultOnMolokai = 0;
	static int pilot = 0;// 驾驶员 原始：0； O岛到M岛的小孩：1； O岛到M岛的大人：2； M岛到O岛的小孩：3
	static boolean done;
	static Lock lock1;
	static Condition childrenWaitOnOahu;
	static Lock lock2;
	static Condition adultWaitOnOahu;
	static Lock lock3;
	static Condition childrenReadyOnMolokai;// 在M岛上等待的孩子

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
		t.join();// 加入到等待队列

	}

	static void AdultItinerary()// 大人的路线
	{
		bg.initializeAdult();// 创建一个划船的大人
		// 将大人的线程初始化

		adultOnOahu++;// 创建所有的划船的大人
		lock2.acquire();
		adultWaitOnOahu.sleep();
		bg.AdultRowToMolokai();
		adultOnOahu--;
		adultOnMolokai++;
		lock2.release();
		lock3.acquire();// 获得锁
		childrenReadyOnMolokai.wake();
		lock3.release();// 释放锁
	}

	static void ChildItinerary()// 孩子的路线
	{
		bg.initializeChild();// 创建一个划船的孩子

		// Required for autograder interface. Must be the first thing called.
		// DO NOT PUT ANYTHING ABOVE THIS LINE.
		childrenOnOahu++;// 创建所有的划船的孩子
		lock1.acquire();
		childrenWaitOnOahu.sleep();
		lock1.release();
		while (true) {
			if (pilot != 1) {
				if (childrenOnOahu > 1) // 有孩子
				{
					lock1.acquire();
					childrenWaitOnOahu.wake();
					pilot = 1;// 运孩子
					bg.ChildRowToMolokai();
					childrenOnOahu--;
					childrenOnMolokai++;
					lock1.release();
					lock3.acquire();// 回来一个孩子
					childrenReadyOnMolokai.sleep();
					lock3.release();
				} else {// 该走大人了
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
				} else// 剩下的孩子从O岛到M岛
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
			// 接下来是返航的操作
			if (done == true) {
				break;
			} else// 表示没有结束
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

		private int Status;// 1:从childrenWaitOnOahu出来；2：从childrenReadyOnMolokai出来
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
