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
    private final Condition notFullCondition = enqLock.newCondition();

    private final ReentrantLock deqLock = new ReentrantLock();
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
        enqLock.lock();
        try{
            while(size.get() == capacity){
                notFullCondition.await();
            }

            Node<T> node = new Node<>(item);
            tail.next = node;
            tail = node;
            
            int c = size.getAndIncrement();
            if(c == 0){
                deqLock.lock();
                try{
                    notEmptyCondition.signalAll();
                }
                finally{
                    deqLock.unlock();
                }
            }
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        finally{
            enqLock.unlock();
        }
    }

    @Override
    public T deq(){
        T result;
        deqLock.lock();
        try{
            while(size.get() == 0){
                notEmptyCondition.await();
            }

            result = head.next.value;
            head = head.next;
            
            int c = size.getAndDecrement();
            if(c == capacity){
                enqLock.lock();
                try{
                    notFullCondition.signalAll();
                }
                finally{
                    enqLock.unlock();
                }
            }
            return result;
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            return null;
        }
        finally{
            deqLock.unlock();
        }
    }
}