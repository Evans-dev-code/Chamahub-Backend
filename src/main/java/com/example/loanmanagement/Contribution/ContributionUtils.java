package com.example.loanmanagement.Contribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

public class ContributionUtils {

    private static final DateTimeFormatter MONTHLY_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter WEEKLY_FORMATTER = DateTimeFormatter.ofPattern("'Week' w yyyy");

    public static String generateCycle(ChamaRulesEntity.CycleType cycleType, LocalDate date) {
        if (cycleType == ChamaRulesEntity.CycleType.MONTHLY) {
            return date.format(MONTHLY_FORMATTER);
        } else {
            return date.format(WEEKLY_FORMATTER);
        }
    }

    public static String getCurrentCycle(ChamaRulesEntity.CycleType cycleType) {
        return generateCycle(cycleType, LocalDate.now());
    }

    public static LocalDate calculateDueDate(String cycle, ChamaRulesEntity rules) {
        LocalDate baseDate = parseCycleToDate(cycle);

        if (rules.getCycleType() == ChamaRulesEntity.CycleType.MONTHLY) {
            int dayOfMonth = Math.min(rules.getDayOfCycle(), baseDate.lengthOfMonth());
            return baseDate.withDayOfMonth(dayOfMonth);
        } else {
            // Weekly: add days to get to the specified day of week
            int targetDayOfWeek = rules.getDayOfCycle();
            int currentDayOfWeek = baseDate.getDayOfWeek().getValue();
            int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7;
            return baseDate.plusDays(daysToAdd);
        }
    }

    public static LocalDate parseCycleToDate(String cycle) {
        try {
            if (cycle.contains("Week")) {
                // Handle "Week X YYYY" format
                String[] parts = cycle.split(" ");
                if (parts.length >= 3) {
                    int week = Integer.parseInt(parts[1]);
                    int year = Integer.parseInt(parts[2]);
                    return LocalDate.of(year, 1, 1).plusWeeks(week - 1);
                }
            } else {
                // Handle "Month YYYY" format
                return LocalDate.parse("01 " + cycle, DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            }
        } catch (Exception e) {
            // Fallback to current date
            return LocalDate.now();
        }
        return LocalDate.now();
    }

    public static ContributionEntity.ContributionStatus determineStatus(LocalDate contributionDate, LocalDate dueDate, int gracePeriodDays) {
        LocalDate gracePeriodEnd = dueDate.plusDays(gracePeriodDays);

        if (contributionDate.isAfter(gracePeriodEnd)) {
            return ContributionEntity.ContributionStatus.LATE;
        } else {
            return ContributionEntity.ContributionStatus.ON_TIME;
        }
    }

    public static BigDecimal calculatePenalty(LocalDate contributionDate, LocalDate dueDate, int gracePeriodDays, BigDecimal penaltyAmount) {
        LocalDate gracePeriodEnd = dueDate.plusDays(gracePeriodDays);

        if (contributionDate.isAfter(gracePeriodEnd)) {
            long daysLate = ChronoUnit.DAYS.between(gracePeriodEnd, contributionDate);
            // Simple penalty calculation - could be enhanced with progressive penalties
            return penaltyAmount;
        }

        return BigDecimal.ZERO;
    }

    public static BigDecimal calculateCollectionRate(BigDecimal totalCollected, BigDecimal expectedTotal) {
        if (expectedTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalCollected
                .divide(expectedTotal, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static List<String> generateCyclesForYear(ChamaRulesEntity.CycleType cycleType, int year) {
        List<String> cycles = new ArrayList<>();

        if (cycleType == ChamaRulesEntity.CycleType.MONTHLY) {
            for (int month = 1; month <= 12; month++) {
                LocalDate date = LocalDate.of(year, month, 1);
                cycles.add(date.format(MONTHLY_FORMATTER));
            }
        } else {
            // Weekly cycles for the year
            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            LocalDate current = startOfYear;
            int weekNumber = 1;

            while (current.isBefore(endOfYear) || current.isEqual(endOfYear)) {
                cycles.add("Week " + weekNumber + " " + year);
                current = current.plusWeeks(1);
                weekNumber++;
            }
        }

        return cycles;
    }

    public static boolean isCycleComplete(String cycle, ChamaRulesEntity.CycleType cycleType) {
        LocalDate cycleDate = parseCycleToDate(cycle);
        LocalDate now = LocalDate.now();

        if (cycleType == ChamaRulesEntity.CycleType.MONTHLY) {
            // Check if we're past the end of the cycle month
            LocalDate endOfMonth = cycleDate.withDayOfMonth(cycleDate.lengthOfMonth());
            return now.isAfter(endOfMonth);
        } else {
            // For weekly, check if we're past the end of that week
            LocalDate endOfWeek = cycleDate.plusDays(6);
            return now.isAfter(endOfWeek);
        }
    }

    public static String getNextCycle(String currentCycle, ChamaRulesEntity.CycleType cycleType) {
        LocalDate currentDate = parseCycleToDate(currentCycle);

        if (cycleType == ChamaRulesEntity.CycleType.MONTHLY) {
            LocalDate nextMonth = currentDate.plusMonths(1);
            return nextMonth.format(MONTHLY_FORMATTER);
        } else {
            LocalDate nextWeek = currentDate.plusWeeks(1);
            return nextWeek.format(WEEKLY_FORMATTER);
        }
    }

    public static String getPreviousCycle(String currentCycle, ChamaRulesEntity.CycleType cycleType) {
        LocalDate currentDate = parseCycleToDate(currentCycle);

        if (cycleType == ChamaRulesEntity.CycleType.MONTHLY) {
            LocalDate previousMonth = currentDate.minusMonths(1);
            return previousMonth.format(MONTHLY_FORMATTER);
        } else {
            LocalDate previousWeek = currentDate.minusWeeks(1);
            return previousWeek.format(WEEKLY_FORMATTER);
        }
    }
}