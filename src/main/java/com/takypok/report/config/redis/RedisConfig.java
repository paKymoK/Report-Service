package com.takypok.report.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takypok.report.model.entity.Sla;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
  private final ObjectMapper mapper;

  @Bean
  ReactiveRedisOperations<String, Sla> redisSlaOps(ReactiveRedisConnectionFactory factory) {

    Jackson2JsonRedisSerializer<Sla> serializer =
        new Jackson2JsonRedisSerializer<>(mapper, Sla.class);

    RedisSerializationContext.RedisSerializationContextBuilder<String, Sla> builder =
        RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

    RedisSerializationContext<String, Sla> context = builder.value(serializer).build();

    return new ReactiveRedisTemplate<>(factory, context);
  }
}
