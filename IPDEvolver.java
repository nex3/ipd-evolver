// -*- c-basic-offset: 2 -*-

import java.awt.*;
import java.util.*;
import java.io.*;

public class IPDEvolver {
  public static final int HEIGHT = 700;
  public static final boolean LOAD = true;
  public static final int MAX_MEM = 100;
  public static final String SAVE_FILE = "ipd.txt";
  public static final String STORAGE_FILE = "ipdstorage.txt";
  public static final int TESTS = 100;
  public static final int WIDTH = 700;

  public static final int COMPRESSION_THRESHOLD = 8;
  public static final double DECREASE_MEM = 0.00001;
  public static final double INCREASE_MEM = 0.00001;
  public static final double MUTATE_PROB = 0.01;
  public static final double ERROR = 0.002;
  public static final int P = 3;
  public static final int S = 0;
  public static final int T = 5;
  public static final int R = 1;

  public static void main (String[] args) throws FileNotFoundException, IOException {
    Scanner input;
    if (LOAD)
      input = new Scanner(new File(SAVE_FILE));
    PrintStream storage = new PrintStream(new FileOutputStream(new File(STORAGE_FILE), true));
    DrawingPanel panel = new DrawingPanel(WIDTH, HEIGHT);
    Graphics g = panel.getGraphics();
    DrawingPanel stats = new DrawingPanel(WIDTH, HEIGHT);
    Graphics statG = stats.getGraphics();
    short[][][] data = new short[WIDTH][HEIGHT][];
    short[][][] aux = new short[WIDTH][HEIGHT][];
    int[][] scores = new int[WIDTH][HEIGHT];
    short[] mem1 = new short[MAX_MEM];
    short[] mem2 = new short[MAX_MEM];
    int[] scoreData = new int[2];
    Random rand = new Random();
    int p = 0;
    if (LOAD)
      p = input.nextInt();
    ArrayList<short[]> species = new ArrayList<short[]>();
    if (LOAD) {
      while (true) {
        short[] s = new short[input.nextInt()];
        for (int i = 0; i < s.length; i++)
          s[i] = (short) input.nextInt();
        species.add(s);
        if (input.next().equals("end"))
          break;
      }
    } else {
      species.add(new short[2]);
      short[] species2 = new short[2];
      species2[1] = 1;
      species.add(species2);
      species2 = new short[2];
      species2[0] = 1;
      species.add(species2);
      species2 = new short[2];
      species2[0] = 1;
      species2[1] = 1;
      species.add(species2);
    }
    ArrayList<Integer> population = new ArrayList<Integer>();
    if (LOAD) {
      for (int i = 0; i < species.size(); i++)
        population.add(input.nextInt());
    } else {
      for (int i = 0; i < 4; i++)
        population.add(0);
    }
    ArrayList<Color> colors = new ArrayList<Color>();
    if (LOAD) {
      for (int i = 0; i < species.size(); i++)
        colors.add(new Color(input.nextInt(), input.nextInt(), input.nextInt()));
    } else {
      colors.add(Color.WHITE);
      colors.add(Color.RED);
      colors.add(Color.BLUE);
      colors.add(Color.BLACK);
    }
    if (LOAD) {
      for (int i = 0; i < WIDTH; i++) {
        for (int j = 0; j < HEIGHT; j++) {
          data[i][j] = new short[input.nextInt()];
          for (int k = 0; k < data[i][j].length; k++)
            data[i][j][k] = (short) input.nextInt();
        }
      }
    } else {
      for (int i = 0; i < WIDTH; i++) {
        for (int j = 0; j < HEIGHT; j++) {
          data[i][j] = new short[2];
          data[i][j][0] = (short) rand.nextInt(2);
          data[i][j][1] = (short) rand.nextInt(2);
          population.set(data[i][j][0] * 2 + data[i][j][1], population.get(data[i][j][0] * 2 + data[i][j][1]) + 1);
        }
      }
    }
    if (LOAD) {
      draw(data, species, colors, g);
      drawStats(statG, species, population, colors, p, null);
    }
    while (true) {
      p++;
      statG.setColor(Color.WHITE);
      statG.fillRect(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
      statG.setColor(Color.BLACK);
      statG.drawString("Initializing generation " + p + "...", WIDTH / 2 + 5, 20);
      for (int i = 0; i < WIDTH; i++) {
        for (int j = 0; j < HEIGHT; j++) {
          scores[i][j] = 0;
        }
      }
      for (int i = 0; i < WIDTH; i++) {
        for (int j = 0; j < HEIGHT; j++) {
          score(scoreData, data[i][j], data[(i + WIDTH - 1) % WIDTH][j]);
          scores[i][j] += scoreData[0];
          scores[(i + WIDTH - 1) % WIDTH][j] += scoreData[1];
          score(scoreData, data[i][j], data[(i + 1) % WIDTH][j]);
          scores[i][j] += scoreData[0];
          scores[(i + 1) % WIDTH][j] += scoreData[1];
          score(scoreData, data[i][j], data[i][(j + HEIGHT - 1) % HEIGHT]);
          scores[i][j] += scoreData[0];
          scores[i][(j + HEIGHT - 1) % HEIGHT] += scoreData[1];
          score(scoreData, data[i][j], data[i][(j + 1) % HEIGHT]);
          scores[i][j] += scoreData[0];
          scores[i][(j + 1) % HEIGHT] += scoreData[1];
        }
        if (i % (WIDTH / 10) == 0)
          statG.drawString("\t" + (100 * i / WIDTH) + "% complete.", WIDTH / 2 + 5, 40 + 200 * i / WIDTH);
      }
      statG.setColor(Color.WHITE);
      statG.fillRect(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
      statG.setColor(Color.BLACK);
      statG.drawString("Scoring complete. Processing...", WIDTH / 2 + 5, 20);
      for (int i = 0; i < WIDTH; i++) {
        for (int j = 0; j < HEIGHT; j++) {
          aux[i][j] = data[i][j];
          int score = scores[i][j];
          if (scores[(i + WIDTH - 1) % WIDTH][j] > score || (scores[(i + WIDTH - 1) % WIDTH][j] == score && Math.random() < 0.5)) {
            score = scores[(i + WIDTH - 1) % WIDTH][j];
            aux[i][j] = data[(i + WIDTH - 1) % WIDTH][j];
          }
          if (scores[(i + 1) % WIDTH][j] > score || (scores[(i + 1) % WIDTH][j] == score && Math.random() < 0.5)) {
            score = scores[(i + 1) % WIDTH][j];
            aux[i][j] = data[(i + 1) % WIDTH][j];
          }
          if (scores[i][(j + HEIGHT - 1) % HEIGHT] > score || (scores[i][(j + HEIGHT - 1) % HEIGHT] == score && Math.random() < 0.5)) {
            score = scores[i][(j + HEIGHT - 1) % HEIGHT];
            aux[i][j] = data[i][(j + HEIGHT - 1) % HEIGHT];
          }
          if (scores[i][(j + 1) % HEIGHT] > score || (scores[i][(j + 1) % HEIGHT] == score && Math.random() < 0.5)) {
            score = scores[i][(j + 1) % HEIGHT];
            aux[i][j] = data[i][(j + 1) % HEIGHT];
          }
        }
      }
      statG.setColor(Color.WHITE);
      statG.fillRect(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
      statG.setColor(Color.BLACK);
      statG.drawString("Processing complete. Copying...", WIDTH / 2 + 5, 20);
      for (int i = 0; i < WIDTH; i++) {
        for (int j = 0; j < HEIGHT; j++) {
          for (int k = 0; k < species.size(); k++) {
            if (species.get(k).length == data[i][j].length) {
              boolean match = true;
              for (int n = 0; n < data[i][j].length; n++)
                match = match && data[i][j][n] == species.get(k)[n];
              if (match)
                population.set(k, population.get(k) - 1);
            }
          }
          data[i][j] = mutate(aux[i][j], rand);
          boolean found = false;
          for (int k = 0; k < species.size(); k++) {
            if (species.get(k).length == data[i][j].length) {
              boolean match = true;
              for (int n = 0; n < data[i][j].length; n++)
                match = match && data[i][j][n] == species.get(k)[n];
              if (match) {
                population.set(k, population.get(k) + 1);
                found = true;
              }
            }
          }
          if (!found) {
            species.add(data[i][j]);
            population.add(1);
            colors.add(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
          }
        }
      }
      statG.setColor(Color.WHITE);
      statG.fillRect(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
      statG.setColor(Color.BLACK);
      statG.drawString("Copy complete. Drawing...", WIDTH / 2 + 5, 20);
      draw(data, species, colors, g);
      drawStats(statG, species, population, colors, p, storage);
      statG.setColor(Color.WHITE);
      statG.fillRect(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
      statG.setColor(Color.BLACK);
      statG.drawString("Drawing complete. Saving...", WIDTH / 2 + 5, 20);
      PrintStream output = new PrintStream(new File(SAVE_FILE));
      output.println(p);
      int n = 0;
      for (short[] s : species) {
        if (n != 0)
          output.println(";");
        n++;
        output.print(s.length + " ");
        for (int k = 0; k < s.length; k++)
          output.print(s[k] + " ");
      }
      output.println("end");
      for (int pop : population)
        output.println(pop);
      for (Color c : colors)
        output.println(c.getRed() + " " + c.getGreen() + " " + c.getBlue());
      for (int i = 0; i < WIDTH; i++) {
        for (int j = 0; j < HEIGHT; j++) {
          output.print(data[i][j].length + " ");
          for (int k = 0; k < data[i][j].length; k++)
            output.print(data[i][j][k] + " ");
        }
      }
      if (p % 10 == 0) {
        statG.setColor(Color.WHITE);
        statG.fillRect(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
        statG.setColor(Color.BLACK);
        statG.drawString("Save complete. Copying file to IPD/ipd" + p + ".txt...", WIDTH / 2 + 5, 20);
        input = new Scanner(new File(SAVE_FILE));
        output = new PrintStream(new File("IPD/ipd" + p + ".txt"));
        while (input.hasNextLine())
          output.println(input.nextLine());
        statG.setColor(Color.WHITE);
        statG.fillRect(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
        statG.setColor(Color.BLACK);
        statG.drawString("File copy complete. Saving image to IPD/ipd" + p + ".png...", WIDTH / 2 + 5, 20);
        panel.save("IPD/ipd" + p + ".png");
      }
    }
  }

  public static void draw (short[][][] data, ArrayList<short[]> species, ArrayList<Color> colors, Graphics g) {
    for (int i = 0; i < WIDTH; i++) {
      for (int j = 0; j < HEIGHT; j++) {
        for (int k = 0; k < species.size(); k++) {
          if (species.get(k).length == data[i][j].length) {
            boolean match = true;
            for (int n = 0; n < data[i][j].length; n++)
              match = match && data[i][j][n] == species.get(k)[n];
            if (match)
              g.setColor(colors.get(k));
          }
        }
        g.drawLine(i, j, i, j);
      }
    }
  }

  public static short[] mutate (short[] data, Random rand) {
    short[] child = null;
    if (Math.random() < INCREASE_MEM && data.length < Math.pow(2, MAX_MEM))
      child = new short[data.length * 2];
    else if (Math.random() < DECREASE_MEM && data.length > 1)
      child = new short[data.length / 2];
    else
      child = new short[data.length];
    int k = Math.random() < 0.5 ? 0 : data.length / 2;
    for (int i = 0; i < child.length; i++)
      child[i] = data[i % data.length + ((child.length < data.length) ? k : 0)];
    while (Math.random() < MUTATE_PROB) {
      int index = rand.nextInt(child.length);
      child[index] = (short) (1 - child[index]);
    }
    return child;
  }

  public static void score (int[] scoreData, short[] data1, short[] data2) {
    int memory1 = 0;
    int memory2 = 0;
    scoreData[0] = 0;
    scoreData[1] = 0;
    for (int i = 0; i < TESTS; i++) {
      int action1 = data1[memory1];
      int action2 = data2[memory2];
      if (Math.random() < ERROR)
        action1 = 1 - action1;
      if (Math.random() < ERROR)
        action2 = 1 - action2;
      if (action1 == 0 && action2 == 0) {
        scoreData[0] += P;
        scoreData[1] += P;
      } else if (action1 == 0 && action2 == 1) {
        scoreData[0] += S;
        scoreData[1] += T;
      } else if (action1 == 1 && action2 == 0) {
        scoreData[0] += T;
        scoreData[1] += S;
      } else {
        scoreData[0] += R;
        scoreData[1] += R;
      }
      memory1 = (memory1 * 2) % data1.length + (Math.random() > ERROR ? action2 : 1 - action2);
      memory2 = (memory2 * 2) % data2.length + (Math.random() > ERROR ? action1 : 1 - action1);
    }
  }

  public static void drawStats (Graphics statG, ArrayList<short[]> species, ArrayList<Integer> population, ArrayList<Color> colors, int generation, PrintStream output) {
    statG.setColor(Color.WHITE);
    statG.fillRect(0, 0, WIDTH, HEIGHT);
    statG.setColor(Color.BLACK);
    statG.drawString("Generation " + generation + ":", 10, 20);
    int line = 0;
    int column = 0;
    ArrayList<short[]> aux = new ArrayList<short[]>();
    ArrayList<Integer> auxPop = new ArrayList<Integer>();
    ArrayList<Color> auxCol = new ArrayList<Color>();
    int n = 0;
    while (species.size() != 0) {
      int i = 0;
      for (int k = 1; k < population.size(); k++) {
        if (population.get(k) > population.get(i))
          i = k;
      }
      if (population.get(i) != 0)
        n++;
      if (population.get(i) != 0 && column < 3) {
        statG.setColor(colors.get(i));
        statG.fillRect(10 + 100 * column, line * 20 + 24, 16, 16);
        statG.setColor(Color.BLACK);
        statG.drawRect(10 + 100 * column, line * 20 + 24, 16, 16);
        String id = "";
        if (species.get(i).length < COMPRESSION_THRESHOLD) {
          for (int j = 0; j < species.get(i).length; j++) {
            id += species.get(i)[j];
          }
        } else {
          id += "C:";
          for (int j = 0; j < species.get(i).length / 4; j++) {
            int val = species.get(i)[4 * j] * 8 + species.get(i)[4 * j + 1] * 4 + species.get(i)[4 * j + 2] * 2 + species.get(i)[4 * j + 3];
            if (val < 10)
              id += val;
            else if (val == 10)
              id += "A";
            else if (val == 11)
              id += "B";
            else if (val == 12)
              id += "C";
            else if (val == 13)
              id += "D";
            else if (val == 14)
              id += "E";
            else
              id += "F";
          }
        }
        statG.drawString(id + ": " + population.get(i), 30 + 100 * column, line * 20 + 40);
        line++;
        if (line > 32) {
          line = 0;
          column++;
        }
      }
      auxCol.add(colors.get(i));
      auxPop.add(population.get(i));
      aux.add(species.get(i));
      colors.remove(i);
      population.remove(i);
      species.remove(i);
    }
    for (short[] s : aux)
      species.add(s);
    for (int p : auxPop)
      population.add(p);
    for (Color c : auxCol)
      colors.add(c);
    double stdev = 0;
    for (int p : population) {
      if (p != 0)
        stdev += Math.pow(p - (double) (WIDTH * HEIGHT) / n, 2);
    }
    stdev = Math.sqrt(stdev) / n;
    double diverse = n / stdev;
    statG.drawString("Diversity Metric: " + ((int) (diverse * 1000) / 1000.0), 110, 20);
    if (output != null)
      output.println(diverse);
  }
}
