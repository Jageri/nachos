package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;
/*Condition2��������ʵ�ֶ���Ҫʹ�ø�������Դ�Ľ��̵Ĺ���
 * �����Ҫһ���ȴ����н��ڸ�������Դ�������Ľ��̴洢������
  */

public class Condition2 
{
    
    public Condition2(Lock conditionLock)
    {
	this.conditionLock = conditionLock;
    }

    //sleep���������ǽ���Ҫʹ�ø�������Դ�µĵ�ǰ��������,
    //������ǰ���̼��뵽�ȴ�������
    public void sleep() 
    {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	conditionLock.release();//�ͷ�������
	boolean intStatus = Machine.interrupt().disable();//���ж�
	/*���������е��̼߳��뵽�ȴ�����*/
	waitQueue.waitForAccess(KThread.currentThread());
	KThread.sleep();//��ǰ�̳߳�˯
	
	Machine.interrupt().restore(intStatus);//���ж�
	conditionLock.acquire();//���������
    }

    //wake�����Ĺ����ǽ��ڸ�������Դ�������Ľ��̶�����ȡһ������
    //������л��Ѳ���
    public void wake() 
    {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean intStatus = Machine.interrupt().disable();//���ж�
	KThread thread = waitQueue.nextThread();
	//���ȴ������е���һ���̼߳��뵽��������
	if (thread != null) {
	    thread.ready();//���뵽��������
	}
	Machine.interrupt().restore(intStatus);//���ж�
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	while(true)
	{
		KThread thread = waitQueue.nextThread();
		//��wait����һ������һ��һ���ļ��뵽��������
		if (thread != null) {
		    thread.ready();
		}else{
			break;
		}
	}
    }

    private Lock conditionLock;
    private ThreadQueue waitQueue =
    		ThreadedKernel.scheduler.newThreadQueue(false);
}
