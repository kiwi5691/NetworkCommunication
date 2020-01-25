package aio.ReentrantLock;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {
    public static void main(String[] args) throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();

        for (int i = 1; i <= 3; i++) {
            System.out.println("i:"+i+"lock");

            lock.lock();
        }

        for(int i=1;i<=3;i++){
            try {

            } finally {
                System.out.println("i:"+i+"unlock");
                lock.unlock();
            }
        }
    }
    //通过lock()方法先获取锁三次，然后通过unlock()方法释放锁3次，程序可以正常退出。
}
