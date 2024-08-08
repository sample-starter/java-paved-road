package org.example.kafkaproducer.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private static final String SSL = "SSL";
    private static final String PLAINTEXT = "PLAINTEXT";
    private static final String JKS = "JKS";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapAddress;

    @Value("${spring.kafka.enable-ssl}")
    private boolean enableSSL;

    @Value("${spring.kafka.ssl-key-password}")
    private String sslKeyPassword;

    @Value("${spring.kafka.ssl-cert-location}")
    private String sslCertLocation;

    @Bean
    public ProducerFactory<String, String> defaultKafkaProducerFactory() {
        return new DefaultKafkaProducerFactory<>(getConfig());
    }

    private Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapAddress);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var protocol = enableSSL ? SSL : PLAINTEXT;
        config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, protocol);
        config.put(SslConfigs.SSL_PROTOCOL_CONFIG, protocol);

        if (enableSSL) {
            config.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKeyPassword);
            config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslCertLocation);
            config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeyPassword);
            config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, JKS);
            config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslCertLocation);
            config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslKeyPassword);
            config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, JKS);
        }
        return config;
    }

    @Bean
    public KafkaTemplate<String, String> defaultKafkaTemplate() {
        return new KafkaTemplate<>(defaultKafkaProducerFactory());
    }
}
