import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

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
    int x1;
    int y1;
    int y2;
    int x2;

    int snake_head = 8;

    private JFrame frame;
    private BufferedImage image;
    private int[] pixels;
    private int scale;

    private Thread thread;
    private boolean running = false;
    private int fps = 1;
    private int ups = 1;

    private Sprite square1;
    private Sprite square2;

    public Graphics(int w, int h, int scale) {
        this.width = w;
        this.height = h;
        this.scale = scale;

        int[][] grid = new int[16][16];

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

        square1 = new Sprite(16,16,0xFF00FF);
        square2 = new Sprite(16,16,0x00FF00);

        x2 = (int) (Math.random() * width);
        y2 = (int) (Math.random() * height);


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
        // The mario sprite

        /* Parametric curve (a circle) see https://en.wikipedia.org/wiki/Parametric_equation
           t controls the coordinates as (x(t),y(t)). Here t is increased by 2 degrees (pi/180 rad)
           each timestep.
        */

        x1 += vx;
        y1 += vy;

        if (x1 >= width-square2.getWidth() ||  x1 == 0) {
            vx *= -1;
        }
        if (y1 >= height-square2.getHeight() || y1 == 0){
            vy *= -1;
        }

        if (vy == 0) {
            for (int i = 0; i < square1.getHeight(); i++) {
                for (int j = 0; j < square1.getWidth(); j++) {
                    pixels[(y1 + i) * width + x1 + j] = square1.getPixels()[i * square1.getWidth() + j];
                }
            }
        } else {
            for (int i = 0; i < square1.getHeight(); i++) {
                for (int j = 0; j < square1.getWidth(); j++) {
                    pixels[(y1 + i) * width + x1 + j] = square1.getPixels()[i * square1.getWidth() + j];
                }
            }
        }

        if (x2 >= width-square2.getWidth() ||  x2 == 0) {
            running = false;
        }
        if (y2 >= height-square2.getHeight() || y2 == 0){
            running = false;
        }

        for (int i = 0; i < square2.getHeight(); i++) {
            for (int j = 0; j < square2.getWidth(); j++) {
                pixels[(y2 + i) * width + x2 + j] = square2.getPixels()[i * square2.getWidth() + j];
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
                vx = -square2.getWidth();
                vy = 0;
            } else if (keyEvent.getKeyChar()=='d') {
                vx = square2.getWidth();
                vy = 0;
            } else if (keyEvent.getKeyChar()=='w') {
                vy = -square2.getHeight();
                vx = 0;
            } else if (keyEvent.getKeyChar()=='s') {
                vy = square2.getHeight();
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
        x1 = width/2;
        y1 = height/2;
    }
}

