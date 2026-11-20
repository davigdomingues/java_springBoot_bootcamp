package io.github.davigdomingues.paymenthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada e classe de bootstrap da aplicação Spring Boot Design Patterns Payment Hub.
 *
 * <p><strong>Estrutura e impacto:</strong> habilita a varredura de componentes a partir do
 * pacote raiz {@code io.github.davigdomingues.paymenthub}, ativando a descoberta automática
 * das implementações de {@code PaymentStrategy} (para a Factory), dos observadores de eventos
 * (para o Publisher), dos repositórios JPA, dos controladores REST e da configuração OpenAPI.</p>
 */
@SpringBootApplication
public class PaymentHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentHubApplication.class, args);
    }
}
