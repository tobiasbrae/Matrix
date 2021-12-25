import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.net.ServerSocket;
import java.net.Socket;

public class Matrix extends JFrame implements KeyListener
{
	private final int MAX_LINES = 300;
	private final int MIN_SPEED = 2;
	private final int MAX_SPEED = 10;
	private final int MIN_SIZE = 14;
	private final int MAX_SIZE = 30;
	private final int MIN_LENGTH = 7;
	private final int MAX_LENGTH = 30;
	private final double CHANCE_NEW_LINE = 0.0075;
	private final int NUM_CHANGE_CHARS = 10;
	
	private final int CLOCK_SIZE = 200;
	private final float CLOCK_ALPHA = 0.3F;
	private final int CLOCK_COLOR_DELAY = 1000;
	private final int CLOCK_TICK_DELAY = 500;
	
	private static final int port = 4000;
	
	private boolean keepRunning, keepMoving = true;
	
	private int sizeX, sizeY;
	private Color bgColor;
	private float colorR, colorG, colorB, startR, startG, startB;
	private boolean changeColors;
	
	private Renderer lineRenderer;
	private Graphics mainGraphics, bufferGraphics;
	private Image bufferImage;
	
	private boolean showClock = true;
	private boolean clockVisible = true;
	private boolean clockTick = false;
	private Color clockColor;
	private long lastTime;
	private SimpleDateFormat sdf;
	
	private boolean showMatrix = true;
	
	private Line[] lines;
	
	public static void main(String[] args)
	{
		new Matrix(args);
		
		{
		/*try
			ServerSocket testSocket = new ServerSocket(port);
			new Matrix(args);
		}
		catch(Exception e)
		{
			System.out.println("Fehler. Entweder laeuft das Programm schon, oder der Port ist belegt.");
		}*/
	}
	
	public Matrix(String[] args)
	{
		super("Matrix");
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		sizeX = dim.getSize().width;
		sizeY = dim.getSize().height;		
		setSize(sizeX, sizeY);
		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setUndecorated(true);
		setVisible(true);
		
		mainGraphics = getGraphics();
		bufferImage = createImage(sizeX, sizeY);
		bufferGraphics = bufferImage.getGraphics();
		
		startR = 0.0F;
		startG = 1.0F;
		startB = 0.1F;
		
		colorR = 0.0F;
		colorG = 1.0F;
		colorB = 0.0F;
		
		if(args != null)
		{
			for(int i = 0; i < args.length; i++)
			{
				if(args[i].equals("-noclock"))
				{
					showClock = false;
				}
				else if(args[i].startsWith("-R"))
				{
					startR = filterValue(args[i]);
					colorR = startR;
				}
				else if(args[i].startsWith("-G"))
				{
					startG = filterValue(args[i]);
					colorG = startG;
				}
				else if(args[i].startsWith("-B"))
				{
					startB = filterValue(args[i]);
					colorB = startB;
				}
			}
		}
		
		bgColor = Color.black;
		
		clockColor = getColor(CLOCK_ALPHA);
		sdf = new SimpleDateFormat("HH:mm");
		lastTime = System.currentTimeMillis();
		
		lines = new Line[MAX_LINES];
		
		keepRunning = true;
		
		lineRenderer = new Renderer();
		lineRenderer.start();
		
		addKeyListener(this);
	}
	
	public void keyTyped(KeyEvent e)
	{
	
	}
	
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_C)
		{
			showClock = !showClock; 
		}
		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			keepRunning = false;
			System.exit(0);
		}
		else if(e.getKeyCode() == KeyEvent.VK_R)
		{
			colorR = 1.0F;
			colorG = 0.0F;
			colorB = 0.0F;
		}
		else if(e.getKeyCode() == KeyEvent.VK_G)
		{
			colorR = 0.0F;
			colorG = 1.0F;
			colorB = 0.0F;
		}
		else if(e.getKeyCode() == KeyEvent.VK_B)
		{
			colorR = 0.0F;
			colorG = 0.0F;
			colorB = 1.0F;
		}
		else if(e.getKeyCode() == KeyEvent.VK_S)
		{
			colorR = startR;
			colorG = startG;
			colorB = startB;
		}
		else if(e.getKeyCode() == KeyEvent.VK_Z)
		{
			changeColors = !changeColors;
		}
		else if(e.getKeyCode() == KeyEvent.VK_T)
		{
			clockTick = !clockTick;
		}
		else if(e.getKeyCode() == KeyEvent.VK_M)
		{
			showMatrix = !showMatrix;
		}
		else if(e.getKeyCode() == KeyEvent.VK_P)
		{
			keepMoving = !keepMoving;
		}
	}
	
	public void keyReleased(KeyEvent e)
	{
	
	}
	
	private boolean getBoolean(double chance)
	{
		double x = Math.random();
		if(x < chance)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private int getInt(int min, int max)
	{
		double x = Math.random() * (max - min);
		int erg = (int) x;
		erg += min;
		return erg;
	}
	
	private char getChar()
	{
		double x = Math.random();
		if(x < 0.5)
		{
			return '1';
		}
		else
		{
			return '0';
		}
	}
	
	private float getAlpha()
	{
		return (float) Math.random();
	}
	
	private float filterValue(String Input)
	{
		try
		{
			Input = Input.substring(2);
			int Value = Integer.parseInt(Input);
			if(Value < 0 || Value > 100)
			{
				System.out.println("Zulaessiger Zahlenbereich 0-100!");
			}
			else
			{
				return (float) Value / 100.0F;
			}
		}
		catch(Exception e)
		{
			System.out.println("Keine Zahl angegeben!");
		}
		return 0.0F;
	}
	
	private Color getColor(float alpha)
	{
		if(changeColors && keepMoving)
		{
			return new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), alpha);
		}
		else
		{
			return new Color(colorR, colorG, colorB, alpha);
		}
	}
	
	private class Renderer extends Thread
	{
		public void run()
		{
			while(keepRunning)
			{
				bufferGraphics.setColor(bgColor);
				bufferGraphics.fillRect(0, 0, sizeX, sizeY);
				
				for(int i = 0; i < NUM_CHANGE_CHARS; i++)
				{
					int in = getInt(0, MAX_LINES - 1);
					if(lines[in] != null)
					{
						lines[in].changeChar();
					}
				}
				
				
				for(int i = 0; i < MAX_LINES; i++)
				{
					if(lines[i] == null)
					{
						if(showMatrix && getBoolean(CHANCE_NEW_LINE) && keepMoving)
						{
							lines[i] = new Line();
						}
					}	
					else if(lines[i].isOutOfRange())
					{
						lines[i] = null;
					}
					else
					{
						lines[i].move();
						lines[i].paint(bufferGraphics);
					}
				}
				
				if(showClock)
				{
					paintClock(bufferGraphics);
				}
				
				mainGraphics.drawImage(bufferImage, 0, 0, null);
			}
		}
		
		private void paintClock(Graphics g)
		{
			if(System.currentTimeMillis() - lastTime > CLOCK_COLOR_DELAY)
			{
				lastTime = System.currentTimeMillis();
				clockColor = getColor(CLOCK_ALPHA);
				if(clockTick && keepMoving)
				{
					clockVisible = !clockVisible;
				}
				else
				{
					clockVisible = true;
				}
			}
			
			if(clockVisible)
			{
				char z1, z2, z3, z4, z5;
				
				String time = sdf.format(new Date());
				
				z1 = time.charAt(0);
				z2 = time.charAt(1);
				z3 = time.charAt(2);
				z4 = time.charAt(3);
				z5 = time.charAt(4);
				
				int x1, x2, x3, x4, x5;
				
				x3 = sizeX / 2 - CLOCK_SIZE / 12;
				x2 = x3 - CLOCK_SIZE / 2 - 35;
				x1 = x2 - CLOCK_SIZE / 2 - 35;
				x4 = x3 + CLOCK_SIZE / 12 + 35;
				x5 = x4 + CLOCK_SIZE / 2 + 35;
				
				paintChar(x1, sizeY / 2 - CLOCK_SIZE / 2, CLOCK_SIZE, z1, clockColor, g);
				paintChar(x2, sizeY / 2 - CLOCK_SIZE / 2, CLOCK_SIZE, z2, clockColor, g);
				paintChar(x3, sizeY / 2- CLOCK_SIZE / 2, CLOCK_SIZE, z3, clockColor, g);
				paintChar(x4, sizeY / 2 - CLOCK_SIZE / 2, CLOCK_SIZE, z4, clockColor, g);
				paintChar(x5, sizeY / 2 - CLOCK_SIZE / 2, CLOCK_SIZE, z5, clockColor, g);
			}
		}
		
		private void paintChar(int x, int y, int size, char value, Color col, Graphics g)
		{
			double pX, pY, sX, sY;
			
			g.setColor(col);
			if(value == ':')
			{
				sX = size / 6.0;
				sY = sX;
				pX = x;
				pY = y + 1.5 * sX;
				g.fillRect((int) pX, (int) pY, (int) sX, (int) sY);
				pY += 2.5 * sX;
				g.fillRect((int) pX, (int) pY, (int) sX, (int) sY);
			}
			if(value == '4' || value == '5' || value == '6' || value == '8' || value == '9' || value == '0') //Links Oben
			{
				sX = size / 10.0;
				sY = size / 2.0;
				pX = (double) x;
				pY = (double) y + sX;
				sY -= sX;
				g.fillRect((int) pX, (int) pY, (int) sX, (int) sY);
			}
			if(value == '2'|| value == '6' || value == '8' || value == '0') //Links Unten
			{
				sX = size / 10.0;
				sY = size / 2.0;
				pX = (double) x;
				pY = (double) y + sY + sX;
				sY -= sX;
				g.fillRect((int) pX, (int) pY, (int) sX, (int) sY);				
			}
			if(value == '2' || value == '3' || value == '5' || value == '6' || value == '7' || value == '8' || value == '9' || value == '0') //Mitte Oben
			{
				sX = size / 2.0;
				sY = size / 10.0;
				pX = (double) x + sY;
				pY = (double) y;
				sX -= sY;
				g.fillRect((int) pX, (int) pY, (int) sX, (int) sY);
			}
			if(value == '2' || value == '3' || value == '4' || value == '5' || value == '6' || value == '8' || value == '9') //Mitte Mitte
			{
				sX = size / 2.0;
				sY = size / 10.0;
				pX = (double) x + sY;
				pY = (double) y + sX;
				sX -= sY;
				g.fillRect((int) pX, (int) pY, (int) sX, (int) sY);
			}
			if(value == '2' || value == '3' || value == '5' || value == '6' || value == '8' || value == '9' || value == '0') //Mitte Unten
			{
				sX = size / 2.0;
				sY = size / 10.0;
				pX = (double) x + sY;
				pY = (double) y + sX * 2.0;
				sX -= sY;
				g.fillRect((int) pX, (int) pY, (int) sX, (int) sY);
			}
			if(value == '1' || value == '2' || value == '3' || value == '4' || value == '7' || value == '8' || value == '9' || value == '0') //Rechts Oben
			{
				sX = size / 10.0;
				sY = size / 2.0;
				pX = (double) x + sY;
				pY = (double) y + sX;
				sY -= sX;
				g.fillRect((int) pX, (int) pY, (int) sX, (int) sY);
			}
			if(value == '1' || value == '3' || value == '4' || value == '5' || value == '6' || value == '7' || value == '8' || value == '9' || value == '0') //Rechts Unten
			{
				sX = size / 10.0;
				sY = size / 2.0;
				pX = (double) x + sY;
				pY = (double) y + sX + sY;
				sY -= sX;
				g.fillRect((int) pX, (int) pY, (int) sX, (int) sY);
			}
		}		
	}
	
	private class Line
	{
		private int posX, posY, speed, textSize, length;
		private char[] chars;
		private Color[] colors;
		
		public Line()
		{
			speed = getInt(MIN_SPEED, MAX_SPEED);
			textSize = getInt(MIN_SIZE, MAX_SIZE);
			length = getInt(MIN_LENGTH, MAX_LENGTH);			
			posX = getInt(-10, sizeX + 10);
			posY = -10 - (length * textSize);

			chars = new char[length];
			colors = new Color[length];
			
			Color col = getColor(getAlpha());
			
			for(int i = 0; i < length; i++)
			{
				chars[i] = getChar();
				colors[i] = col;
			}
		}
		
		public void move()
		{
			if(keepMoving)
			{
				posY += speed;
			}
		}
		
		public void changeChar()
		{
			if(keepMoving)
			{
				int i = getInt(0, length - 1);
				
				if(chars[i] == '1')
				{
					chars[i] = '0';
				}
				else
				{
					chars[i] = '1';
				}
			}
		}
		
		public boolean isOutOfRange()
		{
			if(posY > (sizeY + 20))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		public void paint(Graphics g)
		{
			g.setFont(new Font(Font.SERIF, Font.BOLD, textSize));
			for(int i = 0; i < length; i++)
			{
				g.setColor(colors[i]);
				g.drawString(chars[i] + "", posX, posY + i * textSize);
			}
		}
	}
}