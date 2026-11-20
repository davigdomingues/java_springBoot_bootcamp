# Java & Spring Boot - Design Patterns (GoF)

Repositório dedicado ao "Desafio de Projeto: Explorando Padrões de Projetos na Prática com Java e Spring Boot", desenvolvido durante o Bootcamp Java & Spring Boot promovido pelo Itaú em parceria com a Digital Innovation One (DIO).

## Estrutura do Repositório

```text
.
├── README.md
└── springFramework
    ├── enunciado.md                       # Transcrição do desafio e resumo da solução
    └── design-patterns-payment-hub/       # Projeto principal (API REST Payment Hub)
```

## Projetos e Documentação

- [Enunciado do Desafio](springFramework/enunciado.md) - Contexto do desafio proposto pela DIO e resumo da abordagem escolhida.
- [Design Patterns Payment Hub](springFramework/design-patterns-payment-hub/README.md) - Hub modular de processamento de pagamentos (`PIX`, `CREDIT_CARD` e `BOLETO`) com 9 padrões GoF (Singleton, Builder, Factory Method, Strategy, Chain of Responsibility, Adapter, Decorator, Observer e Facade), Java 21+, Spring Boot 3.5.16, Spring Data JPA, H2, Bean Validation, OpenAPI/Swagger e suíte de testes JUnit 5/Mockito.

## Tecnologias Principais

- **Linguagem:** Java 21+
- **Framework:** Spring Boot 3.5.16 (Web, Data JPA, Validation)
- **Banco de Dados:** H2 (em memória)
- **Documentação da API:** SpringDoc OpenAPI 2.10.0 / Swagger UI
- **Testes:** JUnit 5 & Mockito (`spring-boot-starter-test`)

## Início Rápido

> Não é necessário instalar o Maven globalmente - o projeto utiliza o Maven Wrapper (`./mvnw` / `.\mvnw.cmd`).

```bash
cd springFramework/design-patterns-payment-hub

# Executar os testes unitários e de integração
./mvnw test

# Iniciar a aplicação (http://localhost:8080)
./mvnw spring-boot:run
```

Para detalhes completos da arquitetura, diagramas, regras de negócio, endpoints e exemplos `curl`, consulte o [README do Design Patterns Payment Hub](springFramework/design-patterns-payment-hub/README.md).
