package cn.cyejing.dsync.dominate.domain;

import static org.junit.Assert.*;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class LockTest {
    private EmbeddedChannel channel = new EmbeddedChannel();

    @Test
    public void acquire() {
        Lock lock = new Lock();
        Operate operate = new Operate(1L, 1L, "key", channel);
        boolean acquire = lock.acquire(operate);
        assertEquals(true, acquire);
        boolean acquire1 = lock.acquire(new Operate(2L, 2L, "key", channel));
        assertEquals(false, acquire1);
        boolean acquire2 = lock.acquire(new Operate(2L, 2L, "key2", channel));
        assertEquals(false, acquire2);
        assertEquals(lock.getCurrentOperate(), operate);
    }


    @Test
    public void release() {
        Lock lock = new Lock();
        Operate operate = new Operate(1L, 1L, "key", channel);
        boolean acquire = lock.acquire(operate);
        assertEquals(true, acquire);
        assertEquals(lock.getCurrentOperate(), operate);
        Operate operate1 = new Operate(2L, 2L, "key", channel);
        boolean acquire1 = lock.acquire(operate1);
        assertEquals(false, acquire1);
        assertEquals(lock.getCurrentOperate(), operate);

        Operate release = lock.release(operate);
        assertEquals(release,operate1);
        assertEquals(lock.getCurrentOperate(),operate1);
    }

    @Test
    public void releaseInActive() {
        Lock lock = new Lock();
        Operate operate = new Operate(1L, 1L, "key", channel);
        boolean acquire = lock.acquire(operate);
        assertEquals(true, acquire);
        assertEquals(lock.getCurrentOperate(), operate);
        Operate operate1 = new Operate(2L, 2L, "key", channel);
        lock.acquire(operate1);
        Operate operate2 = new Operate(3L, 3L, "key", channel);
        lock.acquire(operate2);
        operate1.Inactive();
        Operate release = lock.release(operate);
        Assert.assertEquals(operate2, release);
        assertEquals(operate2, lock.getCurrentOperate());

        Operate release1 = lock.release(operate2);
        assertEquals(null,release1);
    }


}
