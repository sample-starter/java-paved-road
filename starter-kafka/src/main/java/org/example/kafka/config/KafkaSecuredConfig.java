package org.example.kafka.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaSecuredConfig {

	private static final String PLAINTEXT = "PLAINTEXT";
	private static final String SSL = "SSL";
	private static final String JKS = "JKS";

	@Value("${spring.kafka.ssl-key-password}")
	private String sslKeyPassword;

	@Value("${spring.kafka.ssl-cert-location}")
	private String sslCertLocation;

	@Value("${spring.kafka.enable-ssl}")
	private boolean isSSLEnabled;

	public Map<String, Object> getSecuredConfig() {
		Map<String, Object> secureConfig = new HashMap<>();
		var protocol = isSSLEnabled ? SSL : PLAINTEXT;
		secureConfig.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, protocol);
		secureConfig.put(SslConfigs.SSL_PROTOCOL_CONFIG, protocol);

		if (isSSLEnabled) {
			secureConfig.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKeyPassword);
			secureConfig.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslCertLocation);
			secureConfig.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeyPassword);
			secureConfig.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, JKS);
			secureConfig.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslCertLocation);
			secureConfig.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslKeyPassword);
			secureConfig.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, JKS);
		}
		return secureConfig;
	}
}
