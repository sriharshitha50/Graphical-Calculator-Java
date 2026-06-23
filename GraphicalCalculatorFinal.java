import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class GraphicalCalculatorFinal extends JPanel {

    static JTextField funcField, startField, endField, stepField;
    static JLabel valueLabel;
    static JSlider slider;

    static double[] xValues, yValues;
    static double currentX = 0, currentY = 0;

  
    static double zoomFactor = 1.0;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Graphical Calculator");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(5,5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        funcField = new JTextField("sin(x)");
        topPanel.add(new JLabel("Function: "), BorderLayout.WEST);
        topPanel.add(funcField, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new GridLayout(6,1,10,10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        leftPanel.setPreferredSize(new Dimension(200, 0));

        startField = new JTextField("-10");
        endField = new JTextField("10");
        stepField = new JTextField("0.1");

        leftPanel.add(new JLabel("Start:"));
        leftPanel.add(startField);
        leftPanel.add(new JLabel("End:"));
        leftPanel.add(endField);
        leftPanel.add(new JLabel("Step:"));
        leftPanel.add(stepField);

        JButton plotButton = new JButton("Plot Graph");
        JButton zoomInButton = new JButton("Zoom In");
        JButton zoomOutButton = new JButton("Zoom Out");

        GraphicalCalculatorFinal graphPanel = new GraphicalCalculatorFinal();
        graphPanel.setBackground(Color.WHITE);

        JPanel bottomPanel = new JPanel(new BorderLayout(10,10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        slider = new JSlider(-10, 10, 0);
        valueLabel = new JLabel("x = 0 , f(x) = 0");

        bottomPanel.add(slider, BorderLayout.CENTER);
        bottomPanel.add(valueLabel, BorderLayout.EAST);

       
        JPanel rightPanel = new JPanel(new GridLayout(3,1,10,10));
        rightPanel.add(plotButton);
        rightPanel.add(zoomInButton);
        rightPanel.add(zoomOutButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(graphPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(rightPanel, BorderLayout.EAST);

        frame.setVisible(true);

        // Plot
        plotButton.addActionListener(e -> {
            generateData();

            double start = Double.parseDouble(startField.getText());
            double end = Double.parseDouble(endField.getText());

            slider.setMinimum((int)start);
            slider.setMaximum((int)end);
            slider.setValue((int)start);

            graphPanel.repaint();
        });

        //  Zoom IN
        zoomInButton.addActionListener(e -> {
            zoomFactor *= 1.2;
            graphPanel.repaint();
        });

        //  Zoom OUT
        zoomOutButton.addActionListener(e -> {
            zoomFactor /= 1.2;
            graphPanel.repaint();
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

            int i = 0;
            for (double x = start; x <= end; x += step) {

                double y = evaluateFunction(funcField.getText(), x);

                if (Double.isInfinite(y) || Double.isNaN(y) || Math.abs(y) > 1000) {
                    yValues[i] = Double.NaN;
                } else {
                    yValues[i] = y;
                }

                xValues[i] = x;
                i++;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid Input!");
        }
    }

    public static double evaluateFunction(String func, double x) {
        try {
            Expression e = new ExpressionBuilder(func)
                    .variable("x")
                    .build()
                    .setVariable("x", x);

            return e.evaluate();
        } catch (Exception e) {
            return 0;
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

        double range = yMax - yMin;

        if (range == 0) {
            yMax += 1;
            yMin -= 1;
            range = yMax - yMin;
        }

        double padding = range * 0.2;
        yMax += padding;
        yMin -= padding;

        if (yMin > 0) yMin = 0;
        if (yMax < 0) yMax = 0;

        //  Apply zoom
        double center = (yMax + yMin) / 2;
        double newRange = (yMax - yMin) / zoomFactor;
        yMin = center - newRange / 2;
        yMax = center + newRange / 2;

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);

        int xZero = -1, yZero = -1;

        if (yMin <= 0 && yMax >= 0) {
            yZero = (int)(height - (0 - yMin)/(yMax - yMin)*height);
            g2.drawLine(0, yZero, width, yZero);
        }

        if (xMin <= 0 && xMax >= 0) {
            xZero = (int)((0 - xMin)/(xMax - xMin)*width);
            g2.drawLine(xZero, 0, xZero, height);
        }

        g2.setColor(Color.BLUE);

        for (int i = 0; i < xValues.length - 1; i++) {
            if (Double.isNaN(yValues[i]) || Double.isNaN(yValues[i+1])) continue;

            int x1 = (int)((xValues[i] - xMin)/(xMax-xMin)*width);
            int y1 = (int)(height - (yValues[i]-yMin)/(yMax-yMin)*height);

            int x2 = (int)((xValues[i+1] - xMin)/(xMax-xMin)*width);
            int y2 = (int)(height - (yValues[i+1]-yMin)/(yMax-yMin)*height);

            g2.drawLine(x1, y1, x2, y2);
        }

        int px = (int)((currentX - xMin)/(xMax-xMin)*width);
        int py = (int)(height - (currentY-yMin)/(yMax-yMin)*height);

        px = Math.max(0, Math.min(width, px));
        py = Math.max(0, Math.min(height, py));

        g2.setColor(Color.RED);
        g2.fillOval(px-5, py-5, 10, 10);

        g2.setColor(Color.BLACK);
        String coord = String.format("(%.2f , %.2f)", currentX, currentY);
        g2.drawString(coord, px + 10, py - 10);
    }
}
