/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.calculate;

/**
 *
 * @author ADMIN
 */
// khai báo     
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Calculate extends JFrame implements ActionListener {
    private JTextField inputField, resultField, modeField, searchField;
    private JTextArea historyArea;
    private JButton searchButton, deleteButton;
    private String expression = "";
    private boolean calculated = false;
    private boolean degreeMode = true;
    private final String HISTORY_FILE = "history.txt";

    private List<String> historyList = new ArrayList<>();

    public Calculate() {
        setTitle("Calculate - Scientific with History Save");
        setSize(500, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(true);
        setLocationRelativeTo(null);

        // Panel hien thi
        JPanel displayPanel = new JPanel(new GridLayout(4, 1));
        modeField = new JTextField("Che do: Do");
        modeField.setFont(new Font("Arial", Font.BOLD, 16));
        modeField.setHorizontalAlignment(JTextField.CENTER);
        modeField.setEditable(false);

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.BOLD, 24));
        inputField.setHorizontalAlignment(JTextField.RIGHT);
        inputField.setEditable(false);

        resultField = new JTextField();
        resultField.setFont(new Font("Arial", Font.BOLD, 32));
        resultField.setHorizontalAlignment(JTextField.RIGHT);
        resultField.setEditable(false);

        // Tim kiem lich su
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchButton = new JButton("Tim kiem");
        searchButton.addActionListener(e -> searchHistory());
        deleteButton = new JButton("Xoa dong");
        deleteButton.addActionListener(e -> deleteSelectedHistory());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.add(deleteButton, BorderLayout.WEST);

        displayPanel.add(modeField);
        displayPanel.add(inputField);
        displayPanel.add(resultField);
        displayPanel.add(searchPanel);
        add(displayPanel, BorderLayout.NORTH);

        // Tao cac nut
        JPanel panel = new JPanel(new GridLayout(7, 4, 5, 5));
        String[] buttons = {
            "C", "←", "%", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", "xʸ", "√",
            "n!", "log", "ln", "sin",
            "cos", "tan", "cot", "Deg/Rad",
            "="
        };
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.addActionListener(this);
            panel.add(button);
        }
        add(panel, BorderLayout.CENTER);

        // Khu vuc hien thi lich su
        historyArea = new JTextArea(10, 20);
        historyArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(historyArea);
        add(scrollPane, BorderLayout.SOUTH);

        // Load lich su tu file
        loadHistory();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ((command.charAt(0) >= '0' && command.charAt(0) <= '9') || command.equals(".")) {
            if (calculated) {
                expression = "";
                calculated = false;
            }
            expression += command;
        } else if (command.equals("C")) {
            expression = "";
            resultField.setText("");
        } else if (command.equals("←")) {
            if (!expression.isEmpty()) {
                expression = expression.substring(0, expression.length() - 1);
            }
        } else if (command.equals("√")) {
            calculateUnary(Math::sqrt, "√");
        } else if (command.equals("%")) {
            calculateUnary(x -> x / 100, "%");
        } else if (command.equals("n!")) {
            try {
                int value = Integer.parseInt(expression);
                if (value < 0) {
                    resultField.setText("Loi");
                } else {
                    long result = factorial(value);
                    addHistory(value + "! = " + result);
                    resultField.setText(String.valueOf(result));
                    expression = "";
                    calculated = true;
                }
            } catch (Exception ex) {
                resultField.setText("Loi");
            }
        } else if (command.equals("log")) {
            calculateUnary(Math::log10, "log");
        } else if (command.equals("ln")) {
            calculateUnary(Math::log, "ln");
        } else if (command.equals("sin") || command.equals("cos") || command.equals("tan") || command.equals("cot")) {
            try {
                double value = Double.parseDouble(expression);
                if (degreeMode) {
                    value = Math.toRadians(value);
                }
                double result = switch (command) {
                    case "sin" -> Math.sin(value);
                    case "cos" -> Math.cos(value);
                    case "tan" -> Math.tan(value);
                    case "cot" -> 1.0 / Math.tan(value);
                    default -> 0;
                };
                addHistory(command + "(" + expression + ") = " + result);
                resultField.setText(String.valueOf(result));
                expression = "";
                calculated = true;
            } catch (Exception ex) {
                resultField.setText("Loi");
            }
        } else if (command.equals("Deg/Rad")) {
            degreeMode = !degreeMode;
            String mode = degreeMode ? "Do" : "Radian";
            modeField.setText("Che do: " + mode);
            addHistory("Chuyen che do: " + mode);
        } else if (command.equals("=")) {
            try {
                double result = evaluate(expression);
                if (Double.isNaN(result) || Double.isInfinite(result)) {
                    resultField.setText("Loi");
                } else {
                    addHistory(expression + " = " + result);
                    resultField.setText(String.valueOf(result));
                    expression = "";
                    calculated = true;
                }
            } catch (Exception ex) {
                resultField.setText("Loi");
            }
        } else {
            if (calculated) {
                expression = "";
                calculated = false;
            }
            expression += " " + command + " ";
        }
        inputField.setText(expression);
    }

    private void calculateUnary(UnaryOperator operator, String label) {
        try {
            double value = Double.parseDouble(expression);
            if (label.equals("√") && value < 0) {
                resultField.setText("Loi");
            } else if ((label.equals("log") || label.equals("ln")) && value <= 0) {
                resultField.setText("Loi");
            } else {
                double result = operator.apply(value);
                addHistory(label + "(" + expression + ") = " + result);
                resultField.setText(String.valueOf(result));
                expression = "";
                calculated = true;
            }
        } catch (Exception ex) {
            resultField.setText("Loi");
        }
    }

    private void addHistory(String entry) {
        historyList.add(entry);
        historyArea.append(entry + "\n");
        saveHistory();
    }

    private void searchHistory() {
        String keyword = searchField.getText();
        if (keyword.isEmpty()) {
            reloadHistory();
            return;
        }
        List<String> filtered = historyList.stream()
            .filter(s -> s.contains(keyword))
            .collect(Collectors.toList());
        historyArea.setText("");
        for (String line : filtered) {
            historyArea.append(line + "\n");
        }
    }

    private void deleteSelectedHistory() {
        String keyword = searchField.getText();
        if (keyword.isEmpty()) return;

        historyList = historyList.stream()
            .filter(s -> !s.contains(keyword))
            .collect(Collectors.toList());

        saveHistory();
        reloadHistory();
    }

    private void reloadHistory() {
        historyArea.setText("");
        for (String line : historyList) {
            historyArea.append(line + "\n");
        }
    }

    private void loadHistory() {
        try {
            if (Files.exists(Paths.get(HISTORY_FILE))) {
                historyList = Files.readAllLines(Paths.get(HISTORY_FILE));
                reloadHistory();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
            for (String line : historyList) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private double evaluate(String expression) {
        try {
            return calculate(expression);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private double calculate(String expression) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        String[] tokens = expression.split(" ");

        for (String token : tokens) {
            if (token.matches("\\d+(\\.\\d+)?")) {
                numbers.push(Double.parseDouble(token));
            } else {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token.charAt(0))) {
                    process(numbers, operators);
                }
                operators.push(token.charAt(0));
            }
        }
        while (!operators.isEmpty()) {
            process(numbers, operators);
        }
        return numbers.pop();
    }

    private int precedence(char op) {
        return switch (op) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case 'x' -> 3;
            default -> 0;
        };
    }

    private void process(Stack<Double> numbers, Stack<Character> operators) {
        if (numbers.size() < 2 || operators.isEmpty()) return;
        double b = numbers.pop();
        double a = numbers.pop();
        char op = operators.pop();
        switch (op) {
            case '+' -> numbers.push(a + b);
            case '-' -> numbers.push(a - b);
            case '*' -> numbers.push(a * b);
            case '/' -> numbers.push(b == 0 ? Double.NaN : a / b);
            case 'x' -> numbers.push(Math.pow(a, b));
        }
    }

    private long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Calculate calc = new Calculate();
            calc.setVisible(true);
        });
    }

    private interface UnaryOperator {
        double apply(double x);
    }
}
