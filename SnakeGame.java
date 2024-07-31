import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JFrame {

    public SnakeGame() {
        this.add(new GamePanel());
        this.setTitle("Snake Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        new SnakeGame();
    }

    // Inner class for the game panel
    public class GamePanel extends JPanel implements ActionListener {

        public static final int UNIT_SIZE = 25;
        public static final int SCREEN_WIDTH = 600;
        public static final int SCREEN_HEIGHT = 600;
        public static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
        public static final int DELAY = 75;

        private boolean running = false;
        private boolean paused = false;
        private Timer timer;
        private Snake snake;
        private Food food;

        public GamePanel() {
            this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
            this.setBackground(Color.BLACK);
            this.setFocusable(true);
            this.addKeyListener(new MyKeyAdapter());
            startGame();
        }

        private void startGame() {
            snake = new Snake();
            food = new Food();
            running = true;
            timer = new Timer(DELAY, this);
            timer.start();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw(g);
        }

        private void draw(Graphics g) {
            if (running) {
                food.draw(g, UNIT_SIZE);
                snake.draw(g, UNIT_SIZE);
                g.setColor(Color.RED);
                g.setFont(new Font("Ink Free", Font.BOLD, 30));
                g.drawString("Score: " + snake.getBodyParts(), 10, g.getFont().getSize());
            } else {
                gameOver(g);
            }
        }

        private void gameOver(Graphics g) {
            g.setColor(Color.RED);
            g.setFont(new Font("Ink Free", Font.BOLD, 75));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);
            g.setFont(new Font("Ink Free", Font.BOLD, 30));
            g.drawString("Score: " + snake.getBodyParts(),
                    (SCREEN_WIDTH - metrics.stringWidth("Score: " + snake.getBodyParts())) / 2,
                    SCREEN_HEIGHT / 2 + 100);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (running && !paused) {
                try {
                    snake.move();
                    if (food.isEaten(snake)) {
                        snake.grow();
                        food.newFood();
                    }
                    if (snake.checkCollisions()) {
                        running = false;
                    }
                } catch (GameOverException ex) {
                    running = false;
                }
            }
            repaint();
        }

        // Inner class for handling key events
        public class MyKeyAdapter extends KeyAdapter {
            @Override
            public void keyPressed(KeyEvent e) {
                if (running) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            if (snake.getDirection() != 'R') {
                                snake.setDirection('L');
                            }
                            break;
                        case KeyEvent.VK_RIGHT:
                            if (snake.getDirection() != 'L') {
                                snake.setDirection('R');
                            }
                            break;
                        case KeyEvent.VK_UP:
                            if (snake.getDirection() != 'D') {
                                snake.setDirection('U');
                            }
                            break;
                        case KeyEvent.VK_DOWN:
                            if (snake.getDirection() != 'U') {
                                snake.setDirection('D');
                            }
                            break;
                        case KeyEvent.VK_P:
                            paused = !paused; // Toggle paused state
                            break;
                    }
                } else {
                    // Reset game on Game Over
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        startGame();
                    }
                }
            }
        }
    }

    // Inner class for representing the snake
    public class Snake {
        private ArrayList<Point> body;
        private char direction = 'R';
        private boolean growing = false;

        public Snake() {
            body = new ArrayList<>();
            body.add(new Point(5, 5));
            body.add(new Point(4, 5));
            body.add(new Point(3, 5));
        }

        public void draw(Graphics g, int unitSize) {
            for (Point p : body) {
                g.setColor(Color.GREEN);
                g.fillRect(p.x * unitSize, p.y * unitSize, unitSize, unitSize);
            }
        }

        public void move() throws GameOverException {
            Point head = new Point(body.get(0));

            switch (direction) {
                case 'U':
                    head.y--;
                    break;
                case 'D':
                    head.y++;
                    break;
                case 'L':
                    head.x--;
                    break;
                case 'R':
                    head.x++;
                    break;
            }

            body.add(0, head);
            if (!growing) {
                body.remove(body.size() - 1);
            } else {
                growing = false;
            }

            // Check collisions with borders
            if (head.x < 0 || head.x >= GamePanel.SCREEN_WIDTH / GamePanel.UNIT_SIZE || head.y < 0
                    || head.y >= GamePanel.SCREEN_HEIGHT / GamePanel.UNIT_SIZE) {
                throw new GameOverException("Snake hit the wall!");
            }

            // Check collisions with itself
            for (int i = 1; i < body.size(); i++) {
                if (head.equals(body.get(i))) {
                    throw new GameOverException("Snake ran into itself!");
                }
            }
        }

        public void grow() {
            growing = true;
        }

        public boolean checkCollisions() {
            return false; // Collisions are handled in the move method by throwing GameOverException
        }

        public char getDirection() {
            return direction;
        }

        public void setDirection(char direction) {
            this.direction = direction;
        }

        public int getBodyParts() {
            return body.size();
        }

        public ArrayList<Point> getBody() {
            return body;
        }
    }

    // Inner class for representing the food
    public class Food {
        private int x;
        private int y;

        public Food() {
            newFood();
        }

        public void newFood() {
            Random random = new Random();
            x = random.nextInt(GamePanel.SCREEN_WIDTH / GamePanel.UNIT_SIZE);
            y = random.nextInt(GamePanel.SCREEN_HEIGHT / GamePanel.UNIT_SIZE);
        }

        public void draw(Graphics g, int unitSize) {
            g.setColor(Color.RED);
            g.fillOval(x * unitSize, y * unitSize, unitSize, unitSize);
        }

        public boolean isEaten(Snake snake) {
            Point head = snake.getBody().get(0);
            return head.x == x && head.y == y;
        }
    }

    // Custom exception for game over
    public class GameOverException extends Exception {
        public GameOverException(String message) {
            super(message);
        }
    }
}
