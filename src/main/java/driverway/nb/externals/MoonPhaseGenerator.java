
package driverway.nb.externals;

/**
 *
 * @author john & Claude AI 4.  
 * 99% Claude; I just tweaked the Waning gibbous to new phases which were reversed.
 */
public class MoonPhaseGenerator {
    
    public static class MoonPhaseResult {
        public final String path;
        public final int illumination;
        //public final String phaseName;
        
        public MoonPhaseResult(String path, int illumination){ //, String phaseName) {
            this.path = path;
            this.illumination = illumination;
            //this.phaseName = phaseName;
        }
    }
    
    /**
     * Generate SVG path for moon phase
     * @param phase Lunar day (0.0 to 29.53)
     * @param cx Center X coordinate
     * @param cy Center Y coordinate  
     * @param r Radius
     * @return MoonPhaseResult containing SVG path, illumination %, and phase name
     */
    public static MoonPhaseResult generateMoonPath(double phase, double cx, double cy, double r) {
        // Convert phase (0-29.53 days) to angle (0-2π)
        double angle = (phase / 29.53) * 2 * Math.PI;
        
        // Calculate illumination percentage
        int illum = (int) Math.round((1 - Math.cos(angle)) * 50);
        //System.out.println("Illum "+illum);
        
        // Determine if we're waxing or waning
        boolean isWaxing = angle < Math.PI;
        
        // Calculate the terminator curve compression factor
        double k = Math.cos(angle);
        
        String path = "";
        
        if (Math.abs(k) < 0.001) {
            // Full moon or new moon (avoid division by zero)
            if (illum > 95) {
                // Full moon - show complete circle
                path = String.format("M %.1f,%.1f A %.1f,%.1f 0 1,1 %.1f,%.1f A %.1f,%.1f 0 1,1 %.1f,%.1f Z",
                    cx - r, cy, r, r, cx + r, cy, r, r, cx - r, cy);
            } else {
                // New moon - show nothing
                path = "";
            }
        } else {
            // Calculate the illuminated portion
            double absK = Math.abs(k);
            double ellipseA = r * absK; // Semi-major axis of terminator ellipse
            
            if (isWaxing) {
                if (k > 0) {
                    // Waxing crescent to first quarter
                    int sweepFlag = k > 0 ? 1 : 0;
                    path = String.format("M %.1f,%.1f A %.1f,%.1f 0 0,%d %.1f,%.1f A %.1f,%.1f 0 0,0 %.1f,%.1f Z",
                        cx, cy - r, ellipseA, r, sweepFlag, cx, cy + r, r, r, cx, cy - r);
                } else {
                    // First quarter to full
                    path = String.format("M %.1f,%.1f A %.1f,%.1f 0 0,1 %.1f,%.1f A %.1f,%.1f 0 0,1 %.1f,%.1f Z",
                        cx, cy - r, r, r, cx, cy + r, ellipseA, r, cx, cy - r);
                }
            } else {
                if (k < 0) {
                    // Waning gibbous to last quarter
                    path = String.format("M %.1f,%.1f A %.1f,%.1f 0 0,0 %.1f,%.1f A %.1f,%.1f 0 0,0 %.1f,%.1f Z",
                        cx, cy - r, r, r, cx, cy + r, ellipseA, r, cx, cy - r);
                } else {
                    // Last quarter to new
                    path = String.format("M %.1f,%.1f A %.1f,%.1f 0 0,0 %.1f,%.1f A %.1f,%.1f 0 0,1 %.1f,%.1f Z",
                        cx, cy - r, ellipseA, r, cx, cy + r, r, r, cx, cy - r);

                }
            }
        }
        
        return new MoonPhaseResult(path, illum);
    }
    
    
    
    /**
     * (Only used by test code in main)
     * Calculate lunar phase from date
     * @param year Year
     * @param month Month (1-12)
     * @param day Day of month
     * @return Lunar day (0.0 to 29.53)
     */
    public static double calculateLunarPhase(int year, int month, int day) {
        // Known new moon: January 1, 2000 at 18:14 UTC
        // Julian day number for this reference
        double referenceJD = 2451545.26; // Jan 1, 2000, 18:14 UTC
        
        // Calculate Julian day for input date
        double a = (14 - month) / 12;
        double y = year - a;
        double m = month + 12 * a - 3;
        double jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 + 1721119;
        
        // Calculate days since reference new moon
        double daysSinceReference = jd - referenceJD;
        
        // Lunar cycle is approximately 29.530588853 days
        double lunarCycle = 29.530588853;
        
        // Calculate current phase
        double phase = daysSinceReference % lunarCycle;
        if (phase < 0) phase += lunarCycle;
        
        return phase;
    }
    
    // Example usage and test method
    public static void main(String[] args) {
        // Test with various phases
        double[] testPhases = {0, 3, 7.38, 8,11, 14.77, 18, 22.15, 25, 29.53};
        
        System.out.println("Moon Phase Generator Test");
        System.out.println("========================");
        
        for (int i = 0; i < testPhases.length; i++) {
            MoonPhaseResult result = generateMoonPath(testPhases[i], 100, 100, 90);
            System.out.printf("Phase: %.2f days%n", testPhases[i]);
            //System.out.printf("Name: %s%n", result.phaseName);
            System.out.printf("Illumination: %d%%%n", result.illumination);
            System.out.printf("SVG Path: %s%n", result.path);
            System.out.println("---");
        }
        
        // Test with current date (example)
        double currentPhase = calculateLunarPhase(2025, 5, 25);
        MoonPhaseResult current = generateMoonPath(currentPhase, 100, 100, 90);
        System.out.printf("Current phase (May 25, 2025): %.2f days%n", currentPhase);
        //System.out.printf("Current phase name: %s%n", current.phaseName);
        System.out.printf("Current illumination: %d%%%n", current.illumination);
    }
}