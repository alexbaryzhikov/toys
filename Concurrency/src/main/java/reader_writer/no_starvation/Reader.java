package reader_writer.no_starvation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

class Reader {
    private static final int BUFFER_SIZE = 8 * 1024;
    private static int readerCount;

    static AtomicInteger readCount = new AtomicInteger();

    private final File file;
    private final Semaphore resource;
    private final ReentrantLock serviceQueue;
    private final ReentrantLock readMutex;

    Reader(File file, Semaphore resource, ReentrantLock serviceQueue, ReentrantLock readMutex) {
        this.file = file;
        this.resource = resource;
        this.serviceQueue = serviceQueue;
        this.readMutex = readMutex;
    }

    String read() throws IOException, InterruptedException {
        // begin ENTRY section
        serviceQueue.lockInterruptibly();
        readMutex.lockInterruptibly();
        if (readerCount == 0) {
            resource.acquire();
        }
        readerCount++;
        readMutex.unlock();
        serviceQueue.unlock();
        // end ENTRY section

        // begin CRITICAL section
        String s = doRead();
        readCount.incrementAndGet();
        // end CRITICAL section

        // begin EXIT section
        readMutex.lockInterruptibly();
        readerCount--;
        if (readerCount == 0) {
            resource.release();
        }
        readMutex.unlock();
        // end EXIT section

        return s;
    }

    private String doRead() throws IOException {
        String s;
        try (FileInputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = is.read(buffer);
            if (length > 0) {
                s = new String(buffer, 0, length);
            } else {
                s = null;
            }
        }
        return s;
    }
}