package cn.cyejing.dsync.dominate.domain;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import org.junit.Test;

public class LockCarrierTest {
    private EmbeddedChannel channel = new EmbeddedChannel();
    private LockCarrier lockCarrier = LockCarrier.getInstance();

    @Test
    public void testLock() {
        Operate operate = new Operate(1L, 1L, "asd", channel);
        assertTrue(lockCarrier.acquire(operate));
        assertFalse(lockCarrier.acquire(operate));
        lockCarrier.release(operate);
        lockCarrier.release(operate);
    }

    @Test
    public void testLock1() {
        Operate operate1 = new Operate(1L, 1L, "asd", channel);
        Operate operate2 = new Operate(1L, 2L, "asd", channel);
        Operate operate3 = new Operate(1L, 3L, "asd", channel);
        assertTrue(lockCarrier.acquire(operate1));
        assertFalse(lockCarrier.acquire(operate2));
        assertFalse(lockCarrier.acquire(operate3));
       assertEquals(operate2,lockCarrier.release(operate1));
       assertEquals(operate3,lockCarrier.release(operate2));
        lockCarrier.release(operate3);
    }

    @Test
    public void testLock2() {
        Process process1 = new Process(channel);
        process1.setProcessId(1L);
        ProcessCarrier.getInstance().addProcess(process1);
        Process process2 = new Process(channel);
        process2.setProcessId(2L);
        ProcessCarrier.getInstance().addProcess(process2);

        Operate operate1 = new Operate(1L, 1L, "asd", channel);
        Operate operate2 = new Operate(2L, 2L, "asd", channel);
        Operate operate3 = new Operate(3L, 3L, "asd", channel);
        assertTrue(lockCarrier.acquire( operate1));
        assertFalse(lockCarrier.acquire( operate2));
        assertFalse(lockCarrier.acquire( operate3));



        List<Operate> operates = lockCarrier.processRelease(process1);
        assertEquals(1, operates.size());
        assertEquals(operate2, operates.iterator().next());



        List<Operate> operates2 = lockCarrier.processRelease(process2);
        assertEquals(1, operates2.size());
        assertEquals(operate3, operates2.iterator().next());
        lockCarrier.release(operate3);
    }

    /**
     * 测试关闭应用时候,对应Operate是否能完全清理掉
     */

    /**
     * 测试
     */
}
