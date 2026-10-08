package cl.duoc.dsy1107.pedidos360.mensajeria;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Exchanges
    public static final String DIRECT_EXCHANGE = "pedidos360.direct.exchange";
    public static final String TOPIC_EXCHANGE = "pedidos360.topic.exchange";
    public static final String DLX_EXCHANGE = "pedidos360.dlx";

    // Colas Principales
    public static final String NOTIFICACIONES_QUEUE = "pedidos360.notificaciones.queue";
    public static final String COCINA_QUEUE = "pedidos360.cocina.queue";
    public static final String DOCUMENTOS_QUEUE = "pedidos360.documentos.queue";

    // Colas de Reintento (TTL 5000 ms)
    public static final String NOTIFICACIONES_RETRY = "pedidos360.notificaciones.retry";
    public static final String COCINA_RETRY = "pedidos360.cocina.retry";
    public static final String DOCUMENTOS_RETRY = "pedidos360.documentos.retry";

    // Dead Letter Queues (DLQ)
    public static final String NOTIFICACIONES_DLQ = "pedidos360.notificaciones.dlq";
    public static final String COCINA_DLQ = "pedidos360.cocina.dlq";
    public static final String DOCUMENTOS_DLQ = "pedidos360.documentos.dlq";

    // Cola de Monitoreo
    public static final String MONITOREO_QUEUE = "pedidos360.monitoreo.queue";

    // Routing keys
    public static final String ROUTING_KEY_NOTIFICACIONES = "notificaciones";
    public static final String ROUTING_KEY_COCINA = "cocina";
    public static final String ROUTING_KEY_DOCUMENTOS = "documentos";

    public static final String ROUTING_KEY_NOTIFICACIONES_FAILED = "notificaciones.failed";
    public static final String ROUTING_KEY_COCINA_FAILED = "cocina.failed";
    public static final String ROUTING_KEY_DOCUMENTOS_FAILED = "documentos.failed";

    public static final String ROUTING_KEY_NOTIFICACIONES_RETRY = "notificaciones.retry";
    public static final String ROUTING_KEY_COCINA_RETRY = "cocina.retry";
    public static final String ROUTING_KEY_DOCUMENTOS_RETRY = "documentos.retry";

    public static final String ROUTING_KEY_MONITOREO_TOPIC = "pedidos.#";

    // ==========================================
    // EXCHANGES
    // ==========================================
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ==========================================
    // COLAS PRINCIPALES (Con argumentos Dead Letter a pedidos360.dlx)
    // ==========================================
    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(NOTIFICACIONES_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY_NOTIFICACIONES_FAILED)
                .build();
    }

    @Bean
    public Queue cocinaQueue() {
        return QueueBuilder.durable(COCINA_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY_COCINA_FAILED)
                .build();
    }

    @Bean
    public Queue documentosQueue() {
        return QueueBuilder.durable(DOCUMENTOS_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY_DOCUMENTOS_FAILED)
                .build();
    }

    // ==========================================
    // COLAS DE REINTENTO (TTL 5000 ms -> reencaminan a pedidos360.direct.exchange)
    // ==========================================
    @Bean
    public Queue notificacionesRetryQueue() {
        return QueueBuilder.durable(NOTIFICACIONES_RETRY)
                .ttl(5000)
                .deadLetterExchange(DIRECT_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY_NOTIFICACIONES)
                .build();
    }

    @Bean
    public Queue cocinaRetryQueue() {
        return QueueBuilder.durable(COCINA_RETRY)
                .ttl(5000)
                .deadLetterExchange(DIRECT_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY_COCINA)
                .build();
    }

    @Bean
    public Queue documentosRetryQueue() {
        return QueueBuilder.durable(DOCUMENTOS_RETRY)
                .ttl(5000)
                .deadLetterExchange(DIRECT_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY_DOCUMENTOS)
                .build();
    }

    // ==========================================
    // DEAD LETTER QUEUES (DLQ)
    // ==========================================
    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(NOTIFICACIONES_DLQ).build();
    }

    @Bean
    public Queue cocinaDlq() {
        return QueueBuilder.durable(COCINA_DLQ).build();
    }

    @Bean
    public Queue documentosDlq() {
        return QueueBuilder.durable(DOCUMENTOS_DLQ).build();
    }

    // ==========================================
    // COLA MONITOREO
    // ==========================================
    @Bean
    public Queue monitoreoQueue() {
        return QueueBuilder.durable(MONITOREO_QUEUE).build();
    }

    // ==========================================
    // BINDINGS: Colas Principales a Direct Exchange
    // ==========================================
    @Bean
    public Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(directExchange())
                .with(ROUTING_KEY_NOTIFICACIONES);
    }

    @Bean
    public Binding cocinaBinding() {
        return BindingBuilder.bind(cocinaQueue())
                .to(directExchange())
                .with(ROUTING_KEY_COCINA);
    }

    @Bean
    public Binding documentosBinding() {
        return BindingBuilder.bind(documentosQueue())
                .to(directExchange())
                .with(ROUTING_KEY_DOCUMENTOS);
    }

    // ==========================================
    // BINDINGS: Colas de Reintento a Direct Exchange
    // ==========================================
    @Bean
    public Binding notificacionesRetryBinding() {
        return BindingBuilder.bind(notificacionesRetryQueue())
                .to(directExchange())
                .with(ROUTING_KEY_NOTIFICACIONES_RETRY);
    }

    @Bean
    public Binding cocinaRetryBinding() {
        return BindingBuilder.bind(cocinaRetryQueue())
                .to(directExchange())
                .with(ROUTING_KEY_COCINA_RETRY);
    }

    @Bean
    public Binding documentosRetryBinding() {
        return BindingBuilder.bind(documentosRetryQueue())
                .to(directExchange())
                .with(ROUTING_KEY_DOCUMENTOS_RETRY);
    }

    // ==========================================
    // BINDINGS: DLQ a Dead Letter Exchange (DLX)
    // ==========================================
    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq())
                .to(dlxExchange())
                .with(ROUTING_KEY_NOTIFICACIONES_FAILED);
    }

    @Bean
    public Binding cocinaDlqBinding() {
        return BindingBuilder.bind(cocinaDlq())
                .to(dlxExchange())
                .with(ROUTING_KEY_COCINA_FAILED);
    }

    @Bean
    public Binding documentosDlqBinding() {
        return BindingBuilder.bind(documentosDlq())
                .to(dlxExchange())
                .with(ROUTING_KEY_DOCUMENTOS_FAILED);
    }

    // ==========================================
    // BINDING: Monitoreo a Topic Exchange
    // ==========================================
    @Bean
    public Binding monitoreoBinding() {
        return BindingBuilder.bind(monitoreoQueue())
                .to(topicExchange())
                .with(ROUTING_KEY_MONITOREO_TOPIC);
    }

    // ==========================================
    // JSON CONVERTER Y TEMPLATES
    // ==========================================
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        return factory;
    }
}
