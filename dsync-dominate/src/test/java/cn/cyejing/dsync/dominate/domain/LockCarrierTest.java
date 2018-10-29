package cn.cyejing.dsync.dominate.domain;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.mockito.Mockito;

public class LockCarrierTest {
    private EmbeddedChannel channel = new EmbeddedChannel();
    private LockCarrier lockCarrier = LockCarrier.getInstance();

    @Test
    public void testLock() {
        Operate operate = new Operate(1L, 1L, "asd", channel);
        assertTrue(lockCarrier.tryLock("asd", operate));
        assertFalse(lockCarrier.tryLock("asd", operate));
        lockCarrier.unLock("asd");
        lockCarrier.unLock("asd");
    }

    @Test
    public void testLock1() {
        Operate operate1 = new Operate(1L, 1L, "asd", channel);
        Operate operate2 = new Operate(1L, 2L, "asd", channel);
        Operate operate3 = new Operate(1L, 3L, "asd", channel);
        assertTrue(lockCarrier.tryLock("asd", operate1));
        assertFalse(lockCarrier.tryLock("asd", operate2));
        assertFalse(lockCarrier.tryLock("asd", operate3));
       assertEquals(operate2,lockCarrier.unLock("asd"));
       assertEquals(operate3,lockCarrier.unLock("asd"));
        lockCarrier.unLock("asd");
    }

    @Test
    public void testLock2() {
        Operate operate1 = new Operate(1L, 1L, "asd", channel);
        Operate operate2 = new Operate(2L, 2L, "asd", channel);
        Operate operate3 = new Operate(3L, 3L, "asd", channel);
        assertTrue(lockCarrier.tryLock("asd", operate1));
        assertFalse(lockCarrier.tryLock("asd", operate2));
        assertFalse(lockCarrier.tryLock("asd", operate3));
        Process mock = Mockito.mock(Process.class);
        Mockito.when(mock.getProcessId()).thenReturn(1L);
        Mockito.when(mock.getResources()).thenReturn(new HashSet<String>(){{ add("asd");}});
        Mockito.when(mock.getChannel()).thenReturn(channel);

        Set<Operate> operates = lockCarrier.processDown(mock);
        assertEquals(1, operates.size());
        assertEquals(operate2, operates.iterator().next());

        Mockito.when(mock.getProcessId()).thenReturn(2L);

        Set<Operate> operates2 = lockCarrier.processDown(mock);
        assertEquals(1, operates2.size());
        assertEquals(operate3, operates2.iterator().next());
        lockCarrier.unLock("asd");

    }
}
