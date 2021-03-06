package main;

import java.io.*;
import java.net.Socket;

public class QuizClient implements Runnable {
    private final Socket socket;
    private final String uniqueID;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public QuizClient(Socket socket) {
        this.socket = socket;
        this.uniqueID = "QC-" + System.currentTimeMillis();
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void close() {
        writer.close();
        try {
            reader.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String task;
        try {
            while (!this.socket.isClosed()) {
                task = this.reader.readLine();
                if (task.equalsIgnoreCase("*-*")) {
                    System.out.println(task);
                    close();
                    break;
                }
                System.out.println(task);

                if (task.endsWith("?")) {
                    String answer = console.readLine();
                    this.writer.write(answer);
                    this.writer.println();
                } else if (task.endsWith("=")) {
                    String answer = autoAnswer(task);
                    System.out.println(answer);
                    this.writer.write(answer);
                    this.writer.println();
                }
            }
            System.out.println("Stopping Client...");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String autoAnswer(String task) {
        String replaceSpace = task.replace(" ", "");
        String replaceLastChar = replaceSpace.replace("=", "");
        String[] numbers = replaceLastChar.split("[+\\-*/]");
        int n1 = Integer.parseInt(numbers[0]);
        int n2 = Integer.parseInt(numbers[1]);
        if (task.contains("+")) {
            return String.valueOf((double)(n1 + n2));
        } else if (task.contains("-")) {
            return String.valueOf((double)(n1 - n2));
        } else if (task.contains("*")) {
            return String.valueOf((double)(n1 * n2));
        } else if (task.contains("/")) {
            return String.valueOf((double)(n1 / n2));
        }
        return "Failed - Critical Server Error!";
    }
}
