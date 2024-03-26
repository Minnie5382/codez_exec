import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(4000);
        
        while (true) {
            Socket socket = serverSocket.accept(); 
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String cmd = in.readLine();
            if (cmd.equals("COMPILE")) {
                String solution = streamToSrc("Solution.java", in);
                String compileResult = isCompilationFailed(solution) ? "COMPILE_FAILED" : "COMPILE_SUCCESS";
                closeConnection(socket, in, out, compileResult);
            }
            else if (cmd.equals("EXECUTE")) {
                String[] testCases = streamToTC(in);
                for (int i = 0; i < testCases.length; i++) {
                    String output = executeTestCase(testCases[i]);
                    out.println(output);
                }    
                closeConnection(socket, in, out, "");
                fileClear(); // remove all .class file and Solution.java
            }
            else {
                closeConnection(socket, in, out, "[Unknown Command] : " + cmd);
                break;
            }    
        }
        serverSocket.close();
    }

    public static void fileClear() {
        try {
            Process p = Runtime.getRuntime().exec("rm Main.class Solution.class Solution.java");
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public static String executeTestCase(String testCase) throws IOException {
        String output = "";
        Process p = Runtime.getRuntime().exec("java Main " + testCase.replaceAll(",", ""));
        try {
            long timeoutInMillis = 1000;
            Thread processThread = new Thread(() -> {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            processThread.start();
            long startTime = System.currentTimeMillis();            
            while (processThread.isAlive()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= timeoutInMillis) {
                    p.destroyForcibly();
                    output = "TIMED_OUT\nEOF"; 
                    return output;
                }
                Thread.sleep(100);    
            }
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String error = "";
            for (String s = null;(s = stdError.readLine()) != null;)
                error += s;
            if (error.equals("")) {
                output = "STDOUT\n";
                for (String s = null;(s = stdOut.readLine()) != null;)
                    output += (s + "\n");
            } else {
                output = "RUNTIME_ERROR\n";
                output += (error + "\n");
            }
            output += "EOF";
            stdError.close();
            stdOut.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }                
        return output;
    }

    public static String streamToSrc(String filename, BufferedReader in) throws IOException {
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        for (String s = null; (s = in.readLine()) != null && !s.equals("EOF");) {
            writer.println(s);
        }
        writer.close();
        return filename;
    }

    public static boolean isCompilationFailed(String code) throws IOException {
        Process compileCode = Runtime.getRuntime().exec("javac " + code);
        try {
            compileCode.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BufferedReader codeCompileError = new BufferedReader(new InputStreamReader(compileCode.getErrorStream()));
        String errorString = codeCompileError.readLine();
        codeCompileError.close();
        if (errorString == null) {
            Process compileMain = Runtime.getRuntime().exec("javac Main.java");
            try {
                compileMain.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BufferedReader mainCompileError = new BufferedReader(new InputStreamReader(compileMain.getErrorStream()));
            errorString = mainCompileError.readLine();
            mainCompileError.close();    
            if (errorString == null)
                return false;        
        }
        return true;
    }
    
    public static String[] streamToTC(BufferedReader in) throws IOException {
        String s;
        List<String> testCases = new ArrayList<>();
        while ((s = in.readLine()) != null && !s.equals("EOF")) {        
            String trimmedStr = s.trim().replaceAll("^\\[|\\]$", "").trim();
            testCases.add(trimmedStr);
        }
        return testCases.toArray(new String[0]);
    }
    
    public static void closeConnection(Socket socket, BufferedReader in, PrintWriter out, String terminal) throws IOException {
        if (terminal != null && !terminal.equals(""))
            out.println(terminal);
        out.close();
        in.close();
        socket.close();
    }
}