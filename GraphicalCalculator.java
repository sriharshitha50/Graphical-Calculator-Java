import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class GraphicalCalculator extends JPanel {

    static JTextField funcField, startField, endField, stepField;
    static JLabel valueLabel;
    static JSlider slider;

    static double[] xValues, yValues;
    static double currentX = 0, currentY = 0;

    public static void main(String[] args) {

        JFrame frame = new JFrame("Graphical Calculator");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel inputPanel = new JPanel(new GridLayout(6,2));

        funcField = new JTextField("sin(x)");
        startField = new JTextField("-10");
        endField = new JTextField("10");
        stepField = new JTextField("0.1");

        JButton plotButton = new JButton("Plot Graph");

        slider = new JSlider(-10, 10, 0);
        valueLabel = new JLabel("x = 0 , f(x) = 0");

        inputPanel.add(new JLabel("Function:"));
        inputPanel.add(funcField);

        inputPanel.add(new JLabel("Start:"));
        inputPanel.add(startField);

        inputPanel.add(new JLabel("End:"));
        inputPanel.add(endField);

        inputPanel.add(new JLabel("Step:"));
        inputPanel.add(stepField);

        inputPanel.add(new JLabel("Slider:"));
        inputPanel.add(slider);

        inputPanel.add(plotButton);
        inputPanel.add(valueLabel);

        GraphicalCalculator graphPanel = new GraphicalCalculator();

        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(graphPanel, BorderLayout.CENTER);

        frame.setVisible(true);

        // Plot button
        plotButton.addActionListener(e -> {
            generateData();
            graphPanel.repaint();
            saveGraph(graphPanel);
        });

        // Slider
        slider.addChangeListener(e -> {
            currentX = slider.getValue();
            currentY = evaluateFunction(funcField.getText(), currentX);
            valueLabel.setText("x = " + currentX + " , f(x) = " + currentY);
            graphPanel.repaint();
        });
    }

    static void generateData() {
        try {
            double start = Double.parseDouble(startField.getText());
            double end = Double.parseDouble(endField.getText());
            double step = Double.parseDouble(stepField.getText());

            int size = (int)((end - start) / step) + 1;

            xValues = new double[size];
            yValues = new double[size];

            PrintWriter writer = new PrintWriter("values.csv");
            writer.println("x,y");

            int i = 0;
            for (double x = start; x <= end; x += step) {
                xValues[i] = x;
                yValues[i] = evaluateFunction(funcField.getText(), x);
                writer.println(x + "," + yValues[i]);
                i++;
            }
            writer.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid Input!");
        }
    }

    // 🔥 exp4j evaluator
    public static double evaluateFunction(String func, double x) {
        try {
            Expression e = new ExpressionBuilder(func)
                    .variable("x")
                    .build()
                    .setVariable("x", x);

            return e.evaluate();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (xValues == null) return;

        int width = getWidth();
        int height = getHeight();

        double xMin = xValues[0];
        double xMax = xValues[xValues.length - 1];

        double yMin = Double.MAX_VALUE;
        double yMax = -Double.MAX_VALUE;

        for (double y : yValues) {
            if (!Double.isNaN(y)) {
                if (y < yMin) yMin = y;
                if (y > yMax) yMax = y;
            }
        }

        // AXES
        g.setColor(Color.BLACK);

        if (yMin <= 0 && yMax >= 0) {
            int yZero = (int)(height - (0 - yMin)/(yMax - yMin)*height);
            g.drawLine(0, yZero, width, yZero);
        }

        if (xMin <= 0 && xMax >= 0) {
            int xZero = (int)((0 - xMin)/(xMax - xMin)*width);
            g.drawLine(xZero, 0, xZero, height);
        }

        // GRAPH (SMOOTH FIX)
        g.setColor(Color.BLUE);

        for (int i = 0; i < xValues.length - 1; i++) {

            double y1 = yValues[i];
            double y2 = yValues[i+1];

            if (Double.isNaN(y1) || Double.isNaN(y2) ||
                Double.isInfinite(y1) || Double.isInfinite(y2)) continue;

            if (Math.abs(y2 - y1) > 50) continue;

            int x1 = (int)((xValues[i] - xMin)/(xMax-xMin)*width);
            int y1p = (int)(height - (y1-yMin)/(yMax-yMin)*height);

            int x2 = (int)((xValues[i+1] - xMin)/(xMax-xMin)*width);
            int y2p = (int)(height - (y2-yMin)/(yMax-yMin)*height);

            g.drawLine(x1, y1p, x2, y2p);
        }

        // POINT
        int px = (int)((currentX - xMin)/(xMax-xMin)*width);
        int py = (int)(height - (currentY-yMin)/(yMax-yMin)*height);

        g.setColor(Color.RED);
        g.fillOval(px-5, py-5, 10, 10);
    }

    static void saveGraph(JPanel panel) {
        BufferedImage image = new BufferedImage(
                panel.getWidth(),
                panel.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = image.createGraphics();
        panel.paint(g2);

        try {
            ImageIO.write(image, "png", new File("graph.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
