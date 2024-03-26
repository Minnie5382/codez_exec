public class Main {
    public static void main(String[] args) {
        Solution sol = new Solution();
                    
        long start = System.nanoTime();        
        System.out.println(sol.caller(args));
        long end = System.nanoTime();
        System.out.println("DONE");
        System.out.println(end - start);
    }   
}
