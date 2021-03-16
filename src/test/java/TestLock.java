import com.qianlima.offline.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestLock {
    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for(int i=0;i<10 ;i++) {

            executorService.submit(new Runnable() {
                public void run() {
                    try {
                        String date = getDate();
                        System.out.println("得到的时间："+date+"---"+System.currentTimeMillis());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        countDownLatch.countDown();

    }

    private static String getDate(){
        Lock lock = new ReentrantLock();
        try{
            lock.lock();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            String date = sdf.format(new Date());

            String formatDateStr = DateUtils.getFormatDateStr(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss:SSS");
            //System.out.println(date);
            return date+"--->"+formatDateStr;
        }catch (Exception e){
            e.getMessage();
        }finally {
            lock.unlock();
        }
        return null;
    }
}