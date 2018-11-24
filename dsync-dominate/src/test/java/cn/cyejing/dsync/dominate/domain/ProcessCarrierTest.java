package cn.cyejing.dsync.dominate.domain;

import static org.junit.Assert.*;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

public class ProcessCarrierTest {

    @Test
    public  void removeProcessCarrier() {
        EmbeddedChannel channel = new EmbeddedChannel();

        ProcessCarrier carrier = ProcessCarrier.getInstance();
        Process process = new Process(channel);
        carrier.addProcess(process);
        assertEquals(1, carrier.peekProcessMap().size());

        carrier.removeProcess(process);

        System.out.println(carrier.peekProcessMap().size());
        assertEquals(0, carrier.peekProcessMap().size());
    }
}
