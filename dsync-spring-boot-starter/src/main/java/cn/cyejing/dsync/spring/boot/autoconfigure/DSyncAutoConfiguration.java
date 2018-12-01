package cn.cyejing.dsync.spring.boot.autoconfigure;

import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Born
 */
@Configuration
public class DSyncAutoConfiguration {


    @ConfigurationProperties(prefix = "dsync")
    @Bean
    public Config config() {
        return new Config();
    }

    @Bean
    public DSync dSync(Config config) {
        return new DSync(config);
    }

    @Bean
    public DLock dLock(DSync dSync) {
        return dSync.getLock();
    }
}
