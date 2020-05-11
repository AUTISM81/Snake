import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

/**
 * This is a class
 * Created 2020-03-25
 *
 * @author Magnus Silverdal
 */
public class Graphics extends Canvas implements Runnable {
    private String title = "Graphics";
    private int width;
    private int height;

    int vx = 0;
    int vy = 0;
    ArrayList <Integer> x1 = new ArrayList<Integer>(10);
    ArrayList <Integer> y1 = new ArrayList<Integer>(10);
    int y2;
    int x2;


    int length;

    private JFrame frame;
    private BufferedImage image;
    private int[] pixels;
    private int scale;

    private Thread thread;
    private boolean running = false;
    private int fps = 1;
    private int ups = 1;

    private Sprite snake_head;
    private Sprite Fruit;
    private Sprite snake_body;

    public Graphics(int w, int h, int scale) {
        this.width = w;
        this.height = h;
        this.scale = scale;

        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        Dimension size = new Dimension(scale*width, scale*height);
        setPreferredSize(size);
        frame = new JFrame();
        frame.setTitle(title);
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        this.addKeyListener(new MyKeyListener());
        this.requestFocus();

        Fruit = new Sprite(16,16,0xFFFFFF);
        snake_head = new Sprite(16,16,0xFF00FF);
        snake_body = new Sprite(16,16,0xFF00FF);


        int rand = random();

        x2 = (int) (width-(rand* snake_head.getWidth()));
        y2 = (int) (height-(rand* snake_head.getHeight()));
    }

    private void draw() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        java.awt.Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g.dispose();
        bs.show();
    }

    private void update() {
        for (int i = 0 ; i < pixels.length ; i++) {
            pixels[i] = 0;
        }

        length = x1.size();
        // The mario sprite

        /* Parametric curve (a circle) see https://en.wikipedia.org/wiki/Parametric_equation
           t controls the coordinates as (x(t),y(t)). Here t is increased by 2 degrees (pi/180 rad)
           each timestep.
        */


        if (x2 >= width- Fruit.getWidth() ||  x2 == 0){
            running = false;
        }
        if (y2 >= height- Fruit.getHeight() || y2 == 0){
            running = false;
        }

        for (int i = 0; i < Fruit.getHeight(); i++) {
            for (int j = 0; j < Fruit.getWidth(); j++) {
                pixels[(y2 + i) * width + x2 + j] = Fruit.getPixels()[i * Fruit.getWidth() + j];
            }
        }

        if (length >= 2) {
            System.out.println(x1.size());
            for (int i = x1.size(); i >= 1;){
                i--;
                    x1.set(i, x1.get(i));
                    y1.set(i, y1.get(i));

                for (int j = 0; j < snake_body.getHeight(); j++) {
                    for (int q = 0; q < snake_body.getWidth(); q++) {
                        pixels[(y1.get(1) + j) * width + x1.get(1) + q] = snake_head.getPixels()[j * snake_head.getWidth() + q];
                    }
                }
            }
        }

        if (x2 == x1.get(0) && y2 == y1.get(0)) {
            int rand = random();

            x2 = (int) (width-(rand* snake_head.getWidth()));
            y2 = (int) (height-(rand* snake_head.getHeight()));

            length++;
        }

        if (length >= 2) {
            x1.add(x1.get(0));
            y1.add(y1.get(0));
        }

        x1.set(0, x1.get(0) +vx);
        y1.set(0, y1.get(0) + vy);

        if (x1.get(0) >= width- Fruit.getWidth() ||  x1.get(0) == 0) {
            vx *= -1;
        }
        if (y1.get(0) >= height- Fruit.getHeight() || y1.get(0) == 0){
            vy *= -1;
        }

        //förmodligen fel här
        if (vy == 0) {
            for (int i = 0; i < snake_head.getHeight(); i++) {
                for (int j = 0; j < snake_head.getWidth(); j++) {
                    pixels[(y1.get(0) + i) * width + x1.get(0) + j] = snake_head.getPixels()[i * snake_head.getWidth() + j];
                }
            }
        } else {
            for (int i = 0; i < snake_head.getHeight(); i++) {
                for (int j = 0; j < snake_head.getWidth(); j++) {
                    pixels[(y1.get(0) + i) * width + x1.get(0) + j] = snake_head.getPixels()[i * snake_head.getWidth() + j];
                }
            }
        }
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        double frameUpdateinteval = 1000000000.0 / fps;
        double stateUpdateinteval = 1000000000.0 / ups;
        double deltaFrame = 0;
        double deltaUpdate = 0;
        long lastTime = System.nanoTime();
        spawn();

        while (running) {
            long now = System.nanoTime();
            deltaFrame += (now - lastTime) / frameUpdateinteval;
            deltaUpdate += (now - lastTime) / stateUpdateinteval;
            lastTime = now;

            while (deltaUpdate >= 1) {
                update();
                deltaUpdate--;
            }

            while (deltaFrame >= 1) {
                draw();
                deltaFrame--;
            }
        }
        stop();
    }

    private class MyKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent keyEvent) {

        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (keyEvent.getKeyChar()=='a') {
                vx = -snake_head.getWidth();
                vy = 0;
            } else if (keyEvent.getKeyChar()=='d') {
                vx = snake_head.getWidth();
                vy = 0;
            } else if (keyEvent.getKeyChar()=='w') {
                vy = -snake_head.getHeight();
                vx = 0;
            } else if (keyEvent.getKeyChar()=='s') {
                vy = snake_head.getHeight();
                vx = 0;
            }
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            if (keyEvent.getKeyChar()=='a' || keyEvent.getKeyChar()=='d') {

            } else if (keyEvent.getKeyChar()=='w' || keyEvent.getKeyChar()=='s') {

            }
        }
    }

    public void spawn(){
        int rand = random();
        x1.add(width- snake_head.getWidth()*rand);
        y1.add(height- snake_head.getHeight()*rand);
    }

    public static int random(){
        int rand = (int) (Math.random()*9+1);
        return rand;
    }
}

