package sumgrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import sumgrid.linkedtotal.LeafLinkedTotal;
import sumgrid.linkedtotal.LinkedTotal;
import sumgrid.linkedtotal.MultiChildLinkedTotal;
import sumgrid.exception.AddingChildException;

/**
 *
 * @author sicovin
 */
public class Main {

    private static final int[][] SQUARE = {{50, 54, 46, 55, 45, 56, 44, 53, 47, 59, 41, 60, 40, 59, 41, 59},
    {47, 57, 46, 49, 52, 46, 53, 47, 53, 41, 59, 40, 60, 41, 59, 41},
    {56, 42, 54, 51, 48, 54, 47, 53, 53, 57, 48, 54, 49, 57, 46, 59},
    {48, 50, 52, 54, 56, 58, 57, 47, 48, 49, 48, 47, 46, 53, 52, 51},
    {50, 56, 50, 48, 49, 50, 51, 59, 42, 60, 39, 62, 38, 63, 38, 50},
    {60, 40, 50, 50, 50, 50, 60, 40, 55, 45, 55, 45, 56, 44, 56, 44},
    {60, 45, 46, 37, 56, 50, 43, 39, 50, 53, 56, 39, 50, 58, 39, 49},
    {26, 56, 54, 38, 48, 50, 67, 64, 32, 54, 50, 49, 48, 47, 46, 45},
    {28, 45, 35, 57, 54, 34, 34, 32, 64, 57, 58, 74, 24, 64, 34, 50},
    {40, 50, 60, 54, 45, 56, 46, 47, 35, 36, 39, 27, 38, 50, 51, 52},
    {29, 38, 47, 58, 48, 37, 50, 58, 37, 46, 50, 50, 50, 50, 50, 50},
    {47, 48, 49, 50, 52, 65, 64, 52, 49, 47, 43, 47, 58, 46, 30, 32},
    {59, 47, 47, 56, 65, 34, 45, 56, 75, 24, 35, 45, 56, 65, 50, 54},
    {53, 46, 35, 45, 29, 46, 46, 50, 23, 32, 40, 46, 64, 64, 64, 20},
    {53, 54, 56, 58, 60, 43, 43, 34, 34, 35, 64, 30, 50, 40, 49, 59},
    {52, 12, 17, 50, 63, 62, 62, 64, 50, 51, 52, 57, 43, 44, 42, 69}};

    private static final int[][] IDS = new int[16][16];

    private static Map<Integer, Map<Integer, LinkedTotal>> currentLength;
    private static Map<Integer, Map<Integer, LinkedTotal>> previousLength;

    private static final int WANTED_LENGTH = 50;

    private Main() {

    }

    public static void main(String[] args) {
        currentLength = new HashMap<>();
        for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
                Map<Integer, LinkedTotal> totalMap = new HashMap<>();
                IDS[j][k] = j * 16 + k;
                LinkedTotal newTotal = new LeafLinkedTotal(SQUARE[j][k], IDS[j][k]);
                totalMap.put(SQUARE[j][k], newTotal);
                currentLength.put(IDS[j][k], totalMap);
            }
        }

        for (int i = 2; i <= WANTED_LENGTH; i++) {
            System.out.println("Start " + i);
            previousLength = currentLength;
            currentLength = new HashMap<>();
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    Map<Integer, LinkedTotal> totals = new HashMap<>();
                    explorePath(j, k, j - 1, k, totals);
                    explorePath(j, k, j, k - 1, totals);
                    explorePath(j, k, j + 1, k, totals);
                    explorePath(j, k, j, k + 1, totals);
                    currentLength.put(IDS[j][k], totals);
                }
            }
            trimInvalidTotals(4);
        }

        Map<Integer, List<LinkedTotal>> totals = sortTotals();
        findValidTotal(totals);
    }

    public static void explorePath(int xTarget, int yTarget, int xSource, int ySource, Map<Integer, LinkedTotal> totals) {
        if(xSource < 0 || ySource < 0 || xSource > 15 || ySource > 15) {
            return;
        }
        
        try {
            for (Map.Entry<Integer, LinkedTotal> nextTotal : previousLength.get(IDS[xSource][ySource]).entrySet()) {
                if (!nextTotal.getValue().isToSkip()) {
                    int total = nextTotal.getKey() + SQUARE[xTarget][yTarget];
                    totals.computeIfAbsent(total, x -> new MultiChildLinkedTotal(total, IDS[xTarget][yTarget]));
                    totals.get(total).addChild(nextTotal.getValue());
                }
            }
        } catch (AddingChildException ex) {
            System.err.println(ex);
        }
    }

    public static Map<Integer, List<LinkedTotal>> sortTotals() {
        Map<Integer, List<LinkedTotal>> totals = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<Integer, Map<Integer, LinkedTotal>> currentTotals : currentLength.entrySet()) {
            for (Map.Entry<Integer, LinkedTotal> total : currentTotals.getValue().entrySet()) {
                if (!total.getValue().isToSkip()) {
                    totals.computeIfAbsent(total.getKey(), x -> new ArrayList<>());
                    totals.get(total.getKey()).add(total.getValue());
                }
            }
        }
        return totals;
    }

    public static void findValidTotal(Map<Integer, List<LinkedTotal>> sortedTotals) {
        try {
            for (Map.Entry<Integer, List<LinkedTotal>> totals : sortedTotals.entrySet()) {
                System.out.println(totals.getKey());
                ExecutorService threadExecutor = Executors.newFixedThreadPool(6);
                AtomicBoolean success = new AtomicBoolean(false);
                for (LinkedTotal total : totals.getValue()) {
                    threadExecutor.execute(() -> success.set(total.processPath(new ArrayList<>()) || success.get()));
                }
                threadExecutor.shutdown();
                threadExecutor.awaitTermination(100, TimeUnit.DAYS);
                if (success.get()) {
                    break;
                }
            }
        } catch (InterruptedException ex) {
            System.err.println("Thread exception");
            Thread.currentThread().interrupt();
        }
    }

    public static void trimInvalidTotals(int depth) {
        for (Map.Entry<Integer, Map<Integer, LinkedTotal>> totals : currentLength.entrySet()) {
            for (Map.Entry<Integer, LinkedTotal> total : totals.getValue().entrySet()) {
                total.getValue().trimPath(new ArrayList<>(), new HashSet<>(), depth);
            }
        }
    }
}
