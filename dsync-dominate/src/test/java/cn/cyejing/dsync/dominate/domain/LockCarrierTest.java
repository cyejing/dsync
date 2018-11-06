package cn.cyejing.dsync.dominate.domain;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Test;
import org.mockito.Mockito;

public class LockCarrierTest {
    private EmbeddedChannel channel = new EmbeddedChannel();
    private LockCarrier lockCarrier = LockCarrier.getInstance();

    @Test
    public void testLock() {
        Operate operate = new Operate(1L, 1L, "asd", channel);
        assertTrue(lockCarrier.tryLock(operate));
        assertFalse(lockCarrier.tryLock(operate));
        lockCarrier.unLock(operate);
        lockCarrier.unLock(operate);
    }

    @Test
    public void testLock1() {
        Operate operate1 = new Operate(1L, 1L, "asd", channel);
        Operate operate2 = new Operate(1L, 2L, "asd", channel);
        Operate operate3 = new Operate(1L, 3L, "asd", channel);
        assertTrue(lockCarrier.tryLock(operate1));
        assertFalse(lockCarrier.tryLock(operate2));
        assertFalse(lockCarrier.tryLock(operate3));
       assertEquals(operate2,lockCarrier.unLock(operate1));
       assertEquals(operate3,lockCarrier.unLock(operate2));
        lockCarrier.unLock(operate3);
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
        assertTrue(lockCarrier.tryLock( operate1));
        assertFalse(lockCarrier.tryLock( operate2));
        assertFalse(lockCarrier.tryLock( operate3));



        List<Operate> operates = lockCarrier.processDown(process1);
        assertEquals(1, operates.size());
        assertEquals(operate2, operates.iterator().next());



        List<Operate> operates2 = lockCarrier.processDown(process2);
        assertEquals(1, operates2.size());
        assertEquals(operate3, operates2.iterator().next());
        lockCarrier.unLock(operate3);
    }
}
