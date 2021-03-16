import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 测试多线程调用接口的时候-请求数据是否超时
 */
public class TestFuture {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(4);//创建一个可容纳40个线程的线程池
        //final List<Future> threadList = new ArrayList<Future>();


        List<String> list = new ArrayList<>();
        list.add("h1");
        list.add("h2");
        list.add("h3");
        list.add("h4");
        list.add("h5");
        for (String s : list) {

        //for(int i=0;i<4;i++){

            //System.out.println(i+"开始时间："+System.currentTimeMillis());
            Future future = pool.submit(new Runnable(){
                @Override
                public void run() {
                    try {
                        if (s.equals("h2")){
                            getStr(s);
                        }

                    } catch (Exception e) {

                    }
                    //System.out.println("结束时间："+System.currentTimeMillis());
                }

            });
            //get(future);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        future.get(200, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        future.cancel(true);
                    } catch (ExecutionException e) {
                        future.cancel(true);
                    } catch (TimeoutException e) {
                        future.cancel(true);
                        System.out.println("方法超时:"+s);
                    }
                }
            });
            t.start();
        }

       /* for(Future future:threadList){
            final Future futureTemp = future;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        futureTemp.get(200, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        futureTemp.cancel(true);
                    } catch (ExecutionException e) {
                        futureTemp.cancel(true);
                    } catch (TimeoutException e) {
                        futureTemp.cancel(true);
                    }
                }
            });
            t.start();
        }*/
    }

    public static void get(Future future){
        final Future futureTemp = future;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    futureTemp.get(200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    futureTemp.cancel(true);
                } catch (ExecutionException e) {
                    futureTemp.cancel(true);
                } catch (TimeoutException e) {
                    futureTemp.cancel(true);
                }
            }
        });
        t.start();
    }

    private static String getStr(String s){
        try {
            Thread.currentThread().sleep(300);
        } catch (InterruptedException e) {

        }
        return "h1";
    }
}