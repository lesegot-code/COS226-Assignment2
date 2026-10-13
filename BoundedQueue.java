import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedQueue<T> implements ConcurrentQueue<T>{
    private static class Node<T>{
        public T value;
        public Node<T> next;

        public Node(T value){
            this.value = value;
            this.next = null;
        }
    }

    private final int capacity;
    private final AtomicInteger size;
    private Node<T> head;
    private Node<T> tail;

    private final ReentrantLock enqLock = new ReentrantLock();
    private final ReentrantLock deqLock = new ReentrantLock();
    // private final ReentrantLock lock = new ReentrantLock();

    private final Condition notFullCondition = enqLock.newCondition();
    private final Condition notEmptyCondition = deqLock.newCondition();

    public BoundedQueue(int capacity){
        this.capacity = capacity;
        this.size = new AtomicInteger(0);

        //Sentinel node initialization
        head = new Node<>(null);
        tail = head;
    }

    @Override
    public void enq(T item){
        boolean mustWake = false;
        enqLock.lock();

        try{
            while(size.get() == capacity){
                notFullCondition.await();
            }

            Node<T> node = new Node<>(item);
            tail.next = node;
            tail = node;

            if (size.getAndIncrement() == 0) {
                mustWake = true;
            }
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        finally{
            enqLock.unlock();
        }

        if (mustWake) {
            deqLock.lock();

            try {
                notEmptyCondition.signalAll();
            } finally {
                deqLock.unlock();
            }
        }
    }

    @Override
    public T deq(){
        boolean mustWake = false;
        T result = null;
        deqLock.lock();

        try{
            while(size.get() == 0){
                notEmptyCondition.await(100, java.util.concurrent.TimeUnit.MILLISECONDS);

                if (size.get() == 0) {
                    return null;
                }

                /*
                    not all waiting consumer threads are always woken up, so some get stuck in the await()

                */

                // notEmptyCondition.await();
            }

            result = head.next.value;
            head = head.next;

            if (size.getAndDecrement() == capacity) {
                mustWake = true;
            }

            // return result;
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            // return null;
        }
        finally{
            deqLock.unlock();
        }

        if (mustWake) {
            enqLock.lock();
            try {
                notFullCondition.signalAll();
            }
            finally {
                enqLock.unlock();
            }
        }

        return result;
    }
}
