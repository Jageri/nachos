package nachos.threads;

import nachos.machine.*;



public class Communicator
{
    
    public Communicator() 
    {
    	lock = new Lock();//newһ��������
    	con = new Condition(lock);//����������������ʵ���ˣ��ͱ�����,
    }

   
    public void speak(int word) 
    {
    lock.acquire();//�����
    
    if(speaknum>0||listennum==0)//˵�ߴ����������ߵ���0
        {
    	speaknum++;
    	con.sleep(); //��˵��һ��һ������������
    	speaknum--;          //��˵��һ��һ������
      	}
    if(listennum>0)
        {
    	//System.out.println(listennum);
    	con.wakeAll();
    	listennum=0;//�������ߣ���˵��˵
    	}
    this.word=word;
    //this.speaker_name=KThread.currentThread().getName();
    System.out.println(KThread.currentThread().getName()+"�߳�˵"+this.word);
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
    lock.acquire();//�����
    while(listennum>0||speaknum==0)
    {
    	listennum++;
    	con.sleep();//��˵��һ��һ��sleep
    	listennum--;//��������
    	}
    if(speaknum>0)
       {
    	con.wake();//��˵��һ��һ���Ļ��ѣ�
    	speaknum--;//��˵˵��
    	}
    KThread.currentThread().yield();//���߳��ó�cpu
    System.out.println(KThread.currentThread().getName()+"�߳�����"+this.word);
    listennum=0;//����Ϊ0�����û��������
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
    	    communicator.listen();//��������
    	  
    	}
    	private Communicator communicator;
        }
    public static void selfTest() 
    {
    	Communicator communicator=new Communicator();
    	new KThread(new CommunicatorTest(communicator)).setName("Communicator1").fork();
    	
    	KThread.currentThread().yield();//���߳��ó�CPU
    	communicator.speak(1);
    	communicator.speak(2);//������˵�����˸�����
    	//communicator.listen(1);
    	KThread.currentThread().yield();//���߳��ó�CPU
        }
    
    private Lock lock;
    private Condition con;
    private int word;
    private static int speaknum;//speak������
    private static int listennum;//listen������
    
}
