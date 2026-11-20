package io.github.davigdomingues.paymenthub.pattern.singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários do Singleton {@link TransactionCodeGenerator}.
 *
 * Valida unicidade da instância {@code enum INSTANCE}, ordenação monotônica em UTC e ausência de
 * colisões sob concorrência real com múltiplas threads sincronizadas.
 */
class TransactionCodeGeneratorTest {

    @BeforeEach
    void setUp() {
        // Isolamento obrigatório por se tratar de contador global em memória
        TransactionCodeGenerator.INSTANCE.resetSequence();
    }

    @Test
    void shouldMaintainSingleEnumInstanceAndGenerateMonotonicFormattedCodes() {
        TransactionCodeGenerator firstRef = TransactionCodeGenerator.INSTANCE;
        TransactionCodeGenerator secondRef = TransactionCodeGenerator.valueOf("INSTANCE");

        assertThat(firstRef).isSameAs(secondRef);

        String code1 = firstRef.nextCode();
        String code2 = secondRef.nextCode();

        assertThat(code1).matches("TXN-\\d{8}-000001");
        assertThat(code2).matches("TXN-\\d{8}-000002");
        assertThat(firstRef.currentSequence()).isEqualTo(2L);
    }

    @Test
    void shouldGenerateUniqueCodesAcrossConcurrentThreads() throws InterruptedException {
        int threads = 8;
        int codesPerThread = 50;
        Set<String> generatedCodes = ConcurrentHashMap.newKeySet();
        // Barreiras garantem disputa simultânea pelo AtomicLong em todas as threads
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        for (int j = 0; j < codesPerThread; j++) {
                            generatedCodes.add(TransactionCodeGenerator.INSTANCE.nextCode());
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(generatedCodes).hasSize(threads * codesPerThread);
        assertThat(TransactionCodeGenerator.INSTANCE.currentSequence()).isEqualTo(threads * codesPerThread);
    }
}
