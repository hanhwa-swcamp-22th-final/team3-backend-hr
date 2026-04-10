package com.ohgiraffers.team3backendhr.infrastructure.kafka.config;

import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PerformancePointSnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PromotionCandidateEvaluatedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PromotionHistorySnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.TierConfigSnapshotEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
public class PromotionKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ProducerFactory<String, PerformancePointSnapshotEvent> performancePointSnapshotProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, PerformancePointSnapshotEvent> performancePointSnapshotKafkaTemplate() {
        return new KafkaTemplate<>(performancePointSnapshotProducerFactory());
    }

    @Bean
    public ProducerFactory<String, PromotionHistorySnapshotEvent> promotionHistorySnapshotProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, PromotionHistorySnapshotEvent> promotionHistorySnapshotKafkaTemplate() {
        return new KafkaTemplate<>(promotionHistorySnapshotProducerFactory());
    }

    @Bean
    public ProducerFactory<String, TierConfigSnapshotEvent> tierConfigSnapshotProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, TierConfigSnapshotEvent> tierConfigSnapshotKafkaTemplate() {
        return new KafkaTemplate<>(tierConfigSnapshotProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, PerformancePointCalculatedEvent> performancePointCalculatedConsumerFactory() {
        JsonDeserializer<PerformancePointCalculatedEvent> deserializer =
            new JsonDeserializer<>(PerformancePointCalculatedEvent.class);
        configureDeserializer(deserializer);
        return new DefaultKafkaConsumerFactory<>(consumerConfig(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PerformancePointCalculatedEvent>
    performancePointCalculatedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PerformancePointCalculatedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(performancePointCalculatedConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PromotionCandidateEvaluatedEvent> promotionCandidateEvaluatedConsumerFactory() {
        JsonDeserializer<PromotionCandidateEvaluatedEvent> deserializer =
            new JsonDeserializer<>(PromotionCandidateEvaluatedEvent.class);
        configureDeserializer(deserializer);
        return new DefaultKafkaConsumerFactory<>(consumerConfig(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PromotionCandidateEvaluatedEvent>
    promotionCandidateEvaluatedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PromotionCandidateEvaluatedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(promotionCandidateEvaluatedConsumerFactory());
        return factory;
    }

    private Map<String, Object> producerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return config;
    }

    private Map<String, Object> consumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

    private void configureDeserializer(JsonDeserializer<?> deserializer) {
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages(
            "com.ohgiraffers.team3backendhr.infrastructure.kafka.dto",
            "com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto"
        );
    }
}