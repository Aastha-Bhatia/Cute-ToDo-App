import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.awt.font.TextAttribute;
import javax.sound.sampled.*;
import java.io.*;
import java.util.Map;
import javax.swing.border.*;

public class TodoApp {
    private static final String TASKS_FILE = "tasks.txt";
    private static JPanel taskPanel;
    private static final String RESOURCES_FOLDER = "resources/";
    
    public static void main(String[] args) {
        // First try to load resources to verify they exist
        verifyResources();
        
        JFrame frame = new JFrame("Cute Todo App");
        frame.setSize(400, 600);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(255, 182, 193));
        titlePanel.setPreferredSize(new Dimension(frame.getWidth(), 70));
        titlePanel.setLayout(new BorderLayout());
        Border dashedBorder = BorderFactory.createDashedBorder(new Color(40, 70, 200), 3f, 6f, 4f, true);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        Border fancyBorder = new CompoundBorder(padding, dashedBorder);
        titlePanel.setBorder(fancyBorder);

        JLabel titLabel = new JLabel("Cute ToDo App");
        titLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titLabel.setForeground(new Color(40, 70, 200));
        titLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titLabel, BorderLayout.CENTER);

        NotebookPanel bottomPanel = new NotebookPanel();
        bottomPanel.setBorder(BorderFactory.createDashedBorder(new Color(65, 105, 225), 2, 5));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setPreferredSize(new Dimension(400, 350));

        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(taskPanel);
        scrollPane.setPreferredSize(new Dimension(350, 250));
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        ImageIcon gifIcon = loadImageIcon(RESOURCES_FOLDER + "working.gif");
        JLabel gifLabel = new JLabel();
        if (gifIcon != null) {
            Image scaledImage = gifIcon.getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT);
            gifLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            gifLabel.setText("GIF not found");
            System.err.println("Failed to load GIF");
        }
        gifLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel gifPanel = new JPanel();
        gifPanel.setLayout(new BorderLayout());
        gifPanel.setOpaque(false);
        gifPanel.add(gifLabel, BorderLayout.CENTER);
        gifPanel.setMaximumSize(new Dimension(400, 210));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setMaximumSize(new Dimension(360, 70));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField taskInput = new JTextField();
        taskInput.setPreferredSize(new Dimension(230, 50));
        taskInput.setFont(new Font("SansSerif", Font.BOLD, 20));
        inputPanel.add(taskInput, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Task");
        addButton.setPreferredSize(new Dimension(110, 50));
        addButton.setBackground(new Color(0, 180, 100));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        addButton.setFocusPainted(false);
        inputPanel.add(addButton, BorderLayout.EAST);

        bottomPanel.add(scrollPane);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        bottomPanel.add(gifPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        bottomPanel.add(inputPanel);

        frame.add(titlePanel, BorderLayout.NORTH);
        frame.add(bottomPanel, BorderLayout.CENTER);

        loadTasks();

        addButton.addActionListener(e -> {
            String taskText = taskInput.getText().trim();
            if (!taskText.isEmpty()) {
                playSound(RESOURCES_FOLDER + "click.wav");
                addTask(taskText, false);
                taskInput.setText("");
                saveTasks();
            }
        });

        frame.setVisible(true);
    }

    private static void verifyResources() {
        String[] resources = {
            RESOURCES_FOLDER + "working.gif", 
            RESOURCES_FOLDER + "click.wav", 
            RESOURCES_FOLDER + "tick.wav", 
            RESOURCES_FOLDER + "discard.wav"
        };
        
        for (String resource : resources) {
            File file = new File(resource);
            if (!file.exists()) {
                System.err.println("Warning: Resource not found - " + resource);
            } else {
                System.out.println("Found resource: " + resource);
            }
        }
    }

    private static ImageIcon loadImageIcon(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
            System.err.println("Couldn't find image file: " + path);
            return null;
        } catch (Exception e) {
            System.err.println("Error loading image: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private static void addTask(String taskText, boolean isCompleted) {
        JPanel singleTaskPanel = new JPanel();
        singleTaskPanel.setLayout(new BorderLayout());
        singleTaskPanel.setMaximumSize(new Dimension(350, 40));
        singleTaskPanel.setBackground(new Color(255, 255, 255, 230));

        JLabel taskLabel = new JLabel(taskText);
        taskLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        taskLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        if (isCompleted) {
            Font currentFont = taskLabel.getFont();
            Map<TextAttribute, Object> attributes = new HashMap<>(currentFont.getAttributes());
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            taskLabel.setFont(new Font(attributes));
        }

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonsPanel.setOpaque(false);

        JButton doneButton = new JButton(isCompleted ? "🔄" : "✅");
        doneButton.setPreferredSize(new Dimension(50, 30));
        doneButton.setFocusPainted(false);
        doneButton.setBackground(new Color(0, 150, 100));
        doneButton.setForeground(Color.WHITE);
        doneButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        doneButton.addActionListener(doneEvent -> {
            playSound(RESOURCES_FOLDER + "tick.wav");
            Font currentFont = taskLabel.getFont();
            Map<TextAttribute, Object> attributes = new HashMap<>(currentFont.getAttributes());
            if (!Boolean.TRUE.equals(attributes.get(TextAttribute.STRIKETHROUGH))) {
                attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                taskLabel.setFont(new Font(attributes));
                doneButton.setText("🔄");
            } else {
                attributes.remove(TextAttribute.STRIKETHROUGH);
                taskLabel.setFont(new Font(attributes));
                doneButton.setText("✅");
            }
            saveTasks();
        });

        JButton deleteButton = new JButton("❌");
        deleteButton.setPreferredSize(new Dimension(50, 30));
        deleteButton.setFocusPainted(false);
        deleteButton.setBackground(new Color(255, 102, 102));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        deleteButton.addActionListener(delEvent -> {
            playSound(RESOURCES_FOLDER + "discard.wav");
            taskPanel.remove(singleTaskPanel);
            taskPanel.revalidate();
            taskPanel.repaint();
            saveTasks();
        });

        buttonsPanel.add(doneButton);
        buttonsPanel.add(deleteButton);

        singleTaskPanel.add(taskLabel, BorderLayout.CENTER);
        singleTaskPanel.add(buttonsPanel, BorderLayout.EAST);

        taskPanel.add(singleTaskPanel);
        taskPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        taskPanel.revalidate();
        taskPanel.repaint();
    }

    private static void saveTasks() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TASKS_FILE))) {
            Component[] components = taskPanel.getComponents();
            for (Component component : components) {
                if (component instanceof JPanel) {
                    JPanel taskPanel = (JPanel) component;
                    Component[] taskComponents = taskPanel.getComponents();
                    for (Component taskComponent : taskComponents) {
                        if (taskComponent instanceof JLabel) {
                            JLabel taskLabel = (JLabel) taskComponent;
                            boolean isCompleted = taskLabel.getFont().getAttributes().containsKey(TextAttribute.STRIKETHROUGH);
                            writer.println(taskLabel.getText() + "|" + isCompleted);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadTasks() {
        File file = new File(TASKS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(TASKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    String taskText = parts[0];
                    boolean isCompleted = Boolean.parseBoolean(parts[1]);
                    addTask(taskText, isCompleted);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void playSound(String soundFileName) {
        try {
            File soundFile = new File(soundFileName);
            if (!soundFile.exists()) {
                System.err.println("Couldn't find sound file: " + soundFileName);
                return;
            }
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + soundFileName);
            e.printStackTrace();
        }
    }
}

class NotebookPanel extends JPanel {
    private final Color lineColor = new Color(200, 200, 255);
    private final int lineSpacing = 25;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(lineColor);
        for (int y = lineSpacing; y < height; y += lineSpacing) {
            g.drawLine(0, y, width, y);
        }
        g.setColor(new Color(255, 150, 150, 150));
        int marginX = 40;
        g.drawLine(marginX, 0, marginX, height);
    }
}