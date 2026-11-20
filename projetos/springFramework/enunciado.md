# Desafio de Projeto: Padrões de Projeto na Prática (DIO / Bootcamp Itaú) -> transcrição do enunciado

---

## 1. Descrição do Desafio

Chegamos à última etapa da nossa jornada! Agora é o momento de consolidar tudo o que aprendemos sobre Padrões de Projeto (*Design Patterns*).

O objetivo principal é explorar esses conceitos na prática, aplicando os padrões GoF (*Gang of Four* — Criacionais, Estruturais e Comportamentais) com Java puro ou aproveitando os recursos e a produtividade do Spring Framework, entregando uma solução completa que agregue valor real ao seu portfólio.

---

## 2. O Que Entregar?

Cada desenvolvedor possui seu próprio ritmo e estilo de aprendizagem. Por isso, a DIO disponibiliza diferentes abordagens para conclusão do desafio:

- **Reproduzir e Evoluir:** Replicar o projeto desenvolvido durante as aulas práticas e incluir melhorias, novos recursos ou toques pessoais para deixá-lo com a sua cara.
- **Criar do Zero:** Desenvolver uma nova ideia ou API REST totalmente nova aplicando os padrões estudados (seja em Java puro ou com Spring Boot).
- **Focar em um Padrão:** Escolher um padrão de projeto específico (como *Chain of Responsibility*, *Strategy*, *Observer*, etc.) para aprofundar ou reproduzir os exemplos em Java puro para fixar a base teórica.

---

## 3. Dicas e Links Úteis

- **Pesquise referências:** Digite no Google termos como `"java design patterns github"` ou `"java design patterns examples"`. Isso trará diversos exemplos e implementações de referência para estudo.
- **Repositórios Oficiais da DIO:**
  - [lab-padroes-projeto-java (GitHub)](https://github.com/digitalinnovationone/lab-padroes-projeto-java): Implementações de referência utilizando apenas Java puro.
  - [lab-padroes-projeto-spring (GitHub)](https://github.com/digitalinnovationone/lab-padroes-projeto-spring): Implementação de referência integrada ao ecossistema Spring Boot, Spring Data JPA e OpenFeign.

---

## 4. Solução Desenvolvida Neste Repositório

- **Abordagem escolhida:** Criar do Zero (Spring Boot 3.5 + Java 21+)
- **Localização do projeto:** [`projetos/springFramework/design-patterns-payment-hub`](design-patterns-payment-hub/README.md)

### Domínio: Payment Hub (Motor de Pagamentos)
API REST modular projetada para processar múltiplos métodos de pagamento (`PIX`, `CREDIT_CARD` e `BOLETO`), realizar validações de risco e conformidade regulatória (*compliance*), liquidar transações em canal externo legado, bonificar clientes com cashback, registrar auditoria e métricas em tempo real.

### 9 Padrões GoF Implementados na Prática:

#### 1. Criacionais
- **Singleton:** `TransactionCodeGenerator` — implementado via `enum INSTANCE` *thread-safe* para emissão sequencial e monotônica de identificadores únicos (`TXN-YYYYMMDD-NNNNNN`).
- **Builder:** `PaymentReceiptBuilder` — construção fluente e imutável de recibos e resultados de execução, suportando enriquecimento via `from(existingResult)` em decoradores.
- **Factory Method / Registry:** `PaymentStrategyFactory` — injeção desacoplada de estratégias em um `EnumMap`, eliminando condicionais acopladas.

#### 2. Estruturais
- **Adapter:** `ExternalSettlementPort` + `LegacyMainframeSettlementAdapter` — adapta a comunicação com o mainframe legado posicional delimitado por pipes (`|`) em centavos para a interface moderna da aplicação.
- **Decorator:** `PaymentExecutionPipeline` (`CorePaymentExecutionPipeline` enriquecido com `CashbackBenefitDecorator` e `ExecutionTelemetryDecorator`) — agrega cálculo de cashback e telemetria de latência dinamicamente.
- **Facade:** `PaymentOrchestrationFacade` — interface unificada de alto nível que orquestra todo o ciclo de vida do pagamento para os controladores REST.

#### 3. Comportamentais
- **Strategy:** `PaymentStrategy` (`PixPaymentStrategy`, `CreditCardPaymentStrategy`, `BoletoPaymentStrategy`) — encapsula regras de validação, cálculo tarifário, status inicial e prazos de liquidação por modalidade.
- **Chain of Responsibility:** `ComplianceValidationHandler` (`AccountSanctionValidationHandler` $\rightarrow$ `AmountLimitValidationHandler` $\rightarrow$ `VelocityRiskValidationHandler`) — checagens em cadeia para sanções, limites e volumetria suspeita.
- **Observer:** `PaymentEventPublisher` com múltiplos observadores desacoplados (`AuditLogObserver`, `NotificationDispatchObserver` e `MetricsCollectorObserver`).
