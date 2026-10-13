import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> implements ConcurrentQueue<T>{
    private static class Node<T>{
        public T value;
        public AtomicReference<Node<T>> next;

        public Node(T value){
            this.value = value;
            this.next = new AtomicReference<>(null);
        }
    }

    private final AtomicReference<Node<T>> head;
    private final AtomicReference<Node<T>> tail;

    public LockFreeQueue(){
        Node<T> sentinel = new Node<>(null);
        head = new AtomicReference<>(sentinel);
        tail = new AtomicReference<>(sentinel);
    }

    @Override
    public void enq(T item){
        Node<T> node = new Node<>(item);
        while(true){
            Node<T> last = tail.get();
            Node<T> next = last.next.get();
            if(last == tail.get()){
                if(next == null){
                    if(last.next.compareAndSet(null, node)){
                        tail.compareAndSet(last, node);
                        return;
                    }
                }
                else{
                    //Help advance the lagging tail pointer
                    tail.compareAndSet(last, next);
                }
            }
        }
    }

    @Override
    public T deq(){
        while(true){
            Node<T> first = head.get();
            Node<T> last = tail.get();
            Node<T> next = first.next.get();
            
            if(first == head.get()){
                if(first == last){
                    if(next == null){
                        return null; //Queue empty
                    }
                    //Help advance tail if it's lagging behind head
                    tail.compareAndSet(last, next);
                }
                else{
                    T value = next.value;
                    if(head.compareAndSet(first, next)){
                        return value;
                    }
                }
            }
        }
    }
}