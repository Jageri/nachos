package nachos.threads;

import nachos.machine.*;



public class Communicator
{
    
    public Communicator() 
    {
    	lock = new Lock();//new一个锁变量
    	con = new Condition(lock);//条件变量，条件被实现了，就被唤醒,
    }

   
    public void speak(int word) 
    {
    lock.acquire();//获得锁
    
    if(speaknum>0||listennum==0)//说者大于零且听者等于0
        {
    	speaknum++;
    	con.sleep(); //将说者一个一个的阻塞掉，
    	speaknum--;          //将说者一个一个减少
      	}
    if(listennum>0)
        {
    	//System.out.println(listennum);
    	con.wakeAll();
    	listennum=0;//阻塞听者，让说者说
    	}
    this.word=word;
    //this.speaker_name=KThread.currentThread().getName();
    System.out.println(KThread.currentThread().getName()+"线程说"+this.word);
    lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen()
    {
    lock.acquire();//获得锁
    while(listennum>0||speaknum==0)
    {
    	listennum++;
    	con.sleep();//将说者一个一个sleep
    	listennum--;//挂起听者
    	}
    if(speaknum>0)
       {
    	con.wake();//将说者一个一个的唤醒，
    	speaknum--;//让说说者
    	}
    KThread.currentThread().yield();//让线程让出cpu
    System.out.println(KThread.currentThread().getName()+"线程听到"+this.word);
    listennum=0;//听者为0，则就没有人听，
    lock.release();
	return this.word;
    }
    
    private static class CommunicatorTest implements Runnable {
    	CommunicatorTest(Communicator communicator) 
    	{
    	this.communicator=communicator;
    	}
    	public void run() 
    	{
    	    communicator.listen();
    	    communicator.listen();//两个听者
    	  
    	}
    	private Communicator communicator;
        }
    public static void selfTest() 
    {
    	Communicator communicator=new Communicator();
    	new KThread(new CommunicatorTest(communicator)).setName("Communicator1").fork();
    	
    	KThread.currentThread().yield();//让线程让出CPU
    	communicator.speak(1);
    	communicator.speak(2);//两个人说两个人个人听
    	//communicator.listen(1);
    	KThread.currentThread().yield();//让线程让出CPU
        }
    
    private Lock lock;
    private Condition con;
    private int word;
    private static int speaknum;//speak的数量
    private static int listennum;//listen的数量
    
}
