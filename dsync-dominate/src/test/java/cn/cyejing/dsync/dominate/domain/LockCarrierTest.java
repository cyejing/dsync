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
    public void testAcquire() {
        Operate operate = new Operate(1L, 1L, "asd", channel);
        assertTrue(lockCarrier.acquire(operate));
        assertFalse(lockCarrier.acquire(operate));
        lockCarrier.release(operate);
        lockCarrier.release(operate);
    }

    @Test
    public void testRelease() {
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
    public void testProcessRelease() {
        Process process1 = new Process(channel);
        process1.setProcessId(1L);
        ProcessCarrier.getInstance().addProcess(process1);
        Process process2 = new Process(channel);
        process2.setProcessId(2L);
        ProcessCarrier.getInstance().addProcess(process2);

        Operate operate1 = new Operate(1L, 1L, "asd", channel);
        Operate operate2 = new Operate(2L, 2L, "asd", channel);
        Operate operate21 = new Operate(2L, 3L, "asd", channel);
        Operate operate3 = new Operate(3L, 3L, "asd", channel);
        assertTrue(lockCarrier.acquire( operate1));
        assertFalse(lockCarrier.acquire( operate2));
        assertFalse(lockCarrier.acquire( operate21));
        assertFalse(lockCarrier.acquire( operate3));

        assertEquals(process1.getOperates().get(0),operate1);
        assertEquals(process2.getOperates().get(0),operate2);
        assertEquals(process2.getOperates().get(1),operate21);
        List<Operate> operates = lockCarrier.processRelease(process1);
        assertEquals(operate2, operates.get(0));
        assertFalse(process1.isActive());
        assertFalse(operate1.isActive());




        List<Operate> operates2 = lockCarrier.processRelease(process2);
        assertEquals(operate3, operates2.get(0));
        assertFalse(process2.isActive());
        assertFalse(operate2.isActive());
        assertFalse(operate21.isActive());

        assertEquals(lockCarrier.peekLockMap().get("asd").getCurrentOperate(),operate3);
        Operate release = lockCarrier.release(operate3);
        assertEquals(null,release);
        assertEquals(lockCarrier.peekLockMap().get("asd").getCurrentOperate(),null);
    }


}
