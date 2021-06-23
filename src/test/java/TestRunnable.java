public class TestRunnable implements Runnable{
    public void run(){
        for (int i = 0; i <10 ; i++) {
            System.out.println(Thread.currentThread().getName()+":"+i);
        }
    }
    public static void main(String[] args) {
        Thread t1 = new Thread(new TestRunnable());
        t1.start();

        Thread t2 = new Thread(new TestRunnable());
        t2.start();

    }

}