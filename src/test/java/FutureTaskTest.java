import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class FutureTaskTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<String> futureTask = new FutureTask<>(new run());

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(futureTask);

        System.out.println(futureTask.get());

        executorService.submit(futureTask);

        System.out.println(futureTask.get());

        executorService.submit(futureTask);

        System.out.println(futureTask.get());

        executorService.submit(futureTask);

        System.out.println(futureTask.get());

        executorService.submit(futureTask);

        System.out.println(futureTask.get());

        executorService.submit(futureTask);

        System.out.println(futureTask.get());

        executorService.submit(futureTask);

        System.out.println(futureTask.get());

        executorService.submit(futureTask);

        System.out.println(futureTask.get());


    }
}

class run implements Callable<String>{
    static int num = 0;

    @Override
    public String call() throws Exception {
        return "Done: " + get();
    }

    private int get(){
        num += 1;
        return num;
    }
}