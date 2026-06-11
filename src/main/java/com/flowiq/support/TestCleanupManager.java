package com.flowiq.support;

import com.flowiq.services.NotificationService;
import com.flowiq.services.TaskService;
import com.flowiq.services.TransactionService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public final class TestCleanupManager {

    private static final ThreadLocal<List<Runnable>> CLEANUPS = ThreadLocal.withInitial(ArrayList::new);

    private TestCleanupManager() {
    }

    public static void register(Runnable cleanup) {
        CLEANUPS.get().add(cleanup);
    }

    public static void registerTransactionCleanup(TransactionService service, long transactionId) {
        register(() -> {
            try {
                service.deleteById(transactionId);
            } catch (Exception e) {
                log.warn("Failed to delete transaction {} during cleanup", transactionId, e);
            }
        });
    }

    public static void registerTaskCleanup(TaskService service, long taskId) {
        register(() -> {
            try {
                service.deleteById(taskId);
            } catch (Exception e) {
                log.warn("Failed to delete task {} during cleanup", taskId, e);
            }
        });
    }

    public static void registerNotificationCleanup(NotificationService service, long notificationId) {
        register(() -> {
            try {
                service.deleteById(notificationId);
            } catch (Exception e) {
                log.warn("Failed to delete notification {} during cleanup", notificationId, e);
            }
        });
    }

    public static void runAll() {
        List<Runnable> cleanups = CLEANUPS.get();
        Collections.reverse(cleanups);
        for (Runnable cleanup : cleanups) {
            try {
                cleanup.run();
            } catch (Exception e) {
                log.warn("Cleanup action failed", e);
            }
        }
        cleanups.clear();
    }

    public static void clear() {
        CLEANUPS.get().clear();
    }
}
