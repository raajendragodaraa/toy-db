package toydb;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DatabaseShowcase {
    public static void main(String[] args) throws IOException {
        File dataFile = new File("showcase_data.db");
        File indexFile = new File("showcase_index.db");

        if (dataFile.exists()) dataFile.delete();
        if (indexFile.exists()) indexFile.delete();

        try {
            System.out.println("=== 1. INSERTING DATA (TRIGGERS DISK PAGE SPLITS) ===");
            try (DatabaseEngine db = new DatabaseEngine(dataFile, indexFile, 10)) {
                db.insert(10, "{\"id\": 10, \"name\": \"Alice\", \"role\": \"Admin\"}");
                db.insert(20, "{\"id\": 20, \"name\": \"Bob\", \"role\": \"Engineer\"}");
                db.insert(30, "{\"id\": 30, \"name\": \"Charlie\", \"role\": \"Designer\"}");
                db.insert(40, "{\"id\": 40, \"name\": \"Diana\", \"role\": \"Manager\"}"); // Triggers split!
                db.insert(50, "{\"id\": 50, \"name\": \"Edward\", \"role\": \"QA\"}");
                db.insert(60, "{\"id\": 60, \"name\": \"Fiona\", \"role\": \"DevOps\"}"); // Triggers another split!

                System.out.println("Total nodes allocated on disk: " + db.getIndexStorage().getHeader().getTotalNodes());

                System.out.println("\n=== 2. POINT LOOKUP (INDEX -> RECORD) ===");
                System.out.println("Lookup ID 30: " + db.get(30));
                System.out.println("Lookup ID 50: " + db.get(50));
                System.out.println("Lookup ID 99 (Non-existent): " + db.get(99));

                System.out.println("\n=== 3. RANGE QUERY (LEAF LINKED-LIST SCAN) ===");
                List<String> range = db.rangeScan(20, 50);
                for (String record : range) {
                    System.out.println("  -> " + record);
                }

                System.out.println("\n=== 4. LRU BUFFER POOL CACHE METRICS ===");
                LRUCache<Long, byte[]> cache = db.getIndex().getCache();
                System.out.println("Cache Hits: " + cache.getHitCount());
                System.out.println("Cache Misses: " + cache.getMissCount());
                System.out.printf("Cache Hit Ratio: %.2f%%\n", cache.getHitRatio() * 100);
            }

            System.out.println("\n=== 5. PERSISTENCE VERIFICATION (RESTART DATABASE) ===");
            try (DatabaseEngine reopenedDb = new DatabaseEngine(dataFile, indexFile, 10)) {
                System.out.println("Reopened database from disk!");
                System.out.println("Reading ID 40 after restart: " + reopenedDb.get(40));
                System.out.println("Total nodes restored from header: " + reopenedDb.getIndexStorage().getHeader().getTotalNodes());
            }

        } finally {
            if (dataFile.exists()) dataFile.delete();
            if (indexFile.exists()) indexFile.delete();
        }
    }
}
