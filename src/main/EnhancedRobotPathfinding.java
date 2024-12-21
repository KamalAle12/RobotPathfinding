package main;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class EnhancedRobotPathfinding extends JFrame {
    private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 50;
    private final Cell[][] grid = new Cell[GRID_SIZE][GRID_SIZE];
    private final JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
    private JButton startButton, resetButton, pauseButton, themeButton;
    private JLabel scoreLabel;
    private JSlider speedSlider;
    private Timer timer;
    private Point start = new Point(0, 0), end = new Point(GRID_SIZE - 1, GRID_SIZE - 1);
    private boolean isPaused = false;
    private int score = 100;
    private int themeIndex = 0;
    private final String[] themes = {"Default", "Space", "Jungle"};
    private ImageIcon robotImage, finishImage;

    public EnhancedRobotPathfinding() {
        setTitle("Enhanced Robot Pathfinding Simulation");
        setSize(GRID_SIZE * CELL_SIZE + 200, GRID_SIZE * CELL_SIZE + 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load images
        robotImage = new ImageIcon(new ImageIcon("res/carto1.png").getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH));
        finishImage = new ImageIcon(new ImageIcon("res/finish2.png").getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH));

        initializeGrid();
        initializeControls();

        add(gridPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void initializeGrid() {
        gridPanel.setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Cell cell = new Cell(i, j);
                cell.setBackground(Color.WHITE);
                grid[i][j] = cell;
                gridPanel.add(cell);

                cell.addActionListener(e -> toggleObstacle(cell));
            }
        }

        grid[start.x][start.y].setIcon(robotImage); // Set robot image for start point
        grid[end.x][end.y].setIcon(finishImage); // Set finish image for end point
    }

    private void initializeControls() {
        JPanel controlPanel = new JPanel(new GridLayout(2, 3));
        startButton = new JButton("Start");
        resetButton = new JButton("Reset");
        pauseButton = new JButton("Pause");
        themeButton = new JButton("Change Theme");
        scoreLabel = new JLabel("Score: " + score);
        speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 1000, 300);

        speedSlider.setMajorTickSpacing(200);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> {
            if (timer != null) {
                timer.setDelay(speedSlider.getValue());
            }
        });

        startButton.addActionListener(e -> startPathfinding());
        resetButton.addActionListener(e -> resetGrid());
        pauseButton.addActionListener(e -> togglePause());
        themeButton.addActionListener(e -> changeTheme());

        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(pauseButton);
        controlPanel.add(themeButton);
        controlPanel.add(scoreLabel);
        controlPanel.add(new JLabel("Speed:"));
        controlPanel.add(speedSlider);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void startPathfinding() {
        List<Point> path = findShortestPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "No path found!");
            return;
        }

        timer = new Timer(speedSlider.getValue(), new ActionListener() {
            Iterator<Point> iterator = path.iterator();
            Point currentPosition = start;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (iterator.hasNext()) {
                    Point next = iterator.next();

                    // Clear the robot image from the current position
                    grid[currentPosition.x][currentPosition.y].setIcon(null);

                    // Move the robot image to the next position
                    grid[next.x][next.y].setIcon(robotImage);

                    // Update the current position
                    currentPosition = next;

                } else {
                    timer.stop();
                    JOptionPane.showMessageDialog(EnhancedRobotPathfinding.this, "Path completed!");
                    scoreLabel.setText("Score: " + score);
                }
            }
        });
        timer.start();
    }


    private List<Point> findShortestPath() {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
        pq.add(new Node(start, 0));
        Map<Point, Point> cameFrom = new HashMap<>();
        Set<Point> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (visited.contains(current.point)) continue;
            visited.add(current.point);

            if (current.point.equals(end)) {
                return reconstructPath(cameFrom, end);
            }

            for (Point neighbor : getNeighbors(current.point)) {
                if (!visited.contains(neighbor)) {
                    pq.add(new Node(neighbor, current.cost + 1));
                    cameFrom.put(neighbor, current.point);
                }
            }
        }

        return null;
    }

    private List<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
        List<Point> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    private List<Point> getNeighbors(Point point) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        List<Point> neighbors = new ArrayList<>();

        for (int[] dir : directions) {
            int newX = point.x + dir[0];
            int newY = point.y + dir[1];

            if (newX >= 0 && newX < GRID_SIZE && newY >= 0 && newY < GRID_SIZE) {
                if (!grid[newX][newY].isObstacle) {
                    neighbors.add(new Point(newX, newY));
                }
            }
        }

        return neighbors;
    }

    private void toggleObstacle(Cell cell) {
        if (!cell.isStartOrEnd()) {
            cell.isObstacle = !cell.isObstacle;
            cell.setBackground(cell.isObstacle ? Color.BLACK : Color.WHITE);
        }
    }

    private void resetGrid() {
        if (timer != null) timer.stop();
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                cell.isObstacle = false;
                cell.setBackground(Color.WHITE);
                cell.setIcon(null);
            }
        }
        grid[start.x][start.y].setIcon(robotImage);
        grid[end.x][end.y].setIcon(finishImage);
    }

    private void togglePause() {
        if (timer != null) {
            if (isPaused) {
                timer.start();
                pauseButton.setText("Pause");
            } else {
                timer.stop();
                pauseButton.setText("Resume");
            }
            isPaused = !isPaused;
        }
    }

    private void changeTheme() {
        themeIndex = (themeIndex + 1) % themes.length;
        String theme = themes[themeIndex];
        JOptionPane.showMessageDialog(this, "Theme changed to: " + theme);

        for (Cell[] row : grid) {
            for (Cell cell : row) {
                if (!cell.isStartOrEnd() && !cell.isObstacle) {
                    cell.setBackground(theme.equals("Space") ? Color.DARK_GRAY :
                            theme.equals("Jungle") ? Color.GREEN : Color.WHITE);
                }
            }
        }
    }

    private static class Cell extends JButton {
        int row, col;
        boolean isObstacle = false;

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        boolean isStartOrEnd() {
            return getIcon() != null;
        }
    }

    private static class Node {
        Point point;
        int cost;

        Node(Point point, int cost) {
            this.point = point;
            this.cost = cost;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnhancedRobotPathfinding::new);
    }
}
