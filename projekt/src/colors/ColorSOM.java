package colors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import som.net.SOMNet;

public class ColorSOM {
	public int imgSize = 50;
	public SOMNet net;
	
	private JFrame frame;
	private JTextField textIter;
	private JTextField textMinNi;

	public BufferedImage image;
	public JPanel display;
	public Graphics displayGraphics;
	private JTextField textImgSize;
	private JTextField textMinD;
	private JTextField textMaxD;
	private JTextField textTrainset;
	private JTextField textMaxNi;
        private JCheckBox checkFunction;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ColorSOM window = new ColorSOM();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ColorSOM() {
		initialize();
	}

	private void initialize() {
		image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);

		frame = new JFrame();
		frame.setBounds(100, 100, 800, 460);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		display = new DisplayPanel();
		display.setBounds(10, 11, 400, 400);
		panel.add(display);

		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imgSize = Integer.parseInt(textImgSize.getText());
				net = new SOMNet(imgSize, imgSize, 3);
				
				image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
				
				net.randomizeWeights();	
				double[][][] map = net.getWeightsMap();

				for (int i = 0; i < imgSize; i++) {
					for (int j = 0; j < imgSize; j++) {
						image.setRGB(i, j, new Color((float) map[i][j][0], (float) map[i][j][1], (float) map[i][j][2], 0f).getRGB());
					}
				}
				display.repaint();
			}
		});
		btnReset.setBounds(420, 11, 91, 23);
		panel.add(btnReset);

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double trainset[][] = new double[Integer.parseInt(textTrainset.getText())][3];
				

				Random rnd = new Random();
				for (int i = 0; i < trainset.length; i++)
					for (int j = 0; j < 3; j++)
						trainset[i][j] = rnd.nextInt(255);

				double[][] meaus = net.teach(trainset, Double.parseDouble(textMinNi.getText()), Double.parseDouble(textMaxNi.getText()), Integer.parseInt(textMinD.getText()), Integer.parseInt(textMaxD.getText()), Integer.parseInt(textIter.getText()), checkFunction.isSelected());

                                for(int i=0;i<meaus.length;i++){
                                    System.out.print(meaus[i][0]+" ");
                                }System.out.print("\n");
                                for(int i=0;i<meaus.length;i++){
                                    System.out.print(meaus[i][1]+" ");
                                }System.out.print("\n");
                                
				double[][][] map = net.getWeightsMap();
				BufferedImage newImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);

				for (int i = 0; i < imgSize; i++) {
					for (int j = 0; j < imgSize; j++) {
						newImage.setRGB(i, j, new Color((float) map[i][j][0], (float) map[i][j][1], (float) map[i][j][2], 0f).getRGB());
					}
				}

				image = newImage;
				display.repaint();
			}
		});
		btnStart.setBounds(420, 45, 91, 23);
		panel.add(btnStart);

		JLabel lblIteracji = new JLabel("Iteracji:");
		lblIteracji.setHorizontalAlignment(SwingConstants.RIGHT);
		lblIteracji.setBounds(510, 15, 85, 14);
		panel.add(lblIteracji);

		JLabel lblMinNi = new JLabel("Min ni:");
		lblMinNi.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMinNi.setBounds(510, 79, 85, 14);
		panel.add(lblMinNi);
		
		JLabel lblMaxNi = new JLabel("Max ni:");
		lblMaxNi.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMaxNi.setBounds(510, 107, 85, 14);
		panel.add(lblMaxNi);
		
		JLabel lblBokObrazka = new JLabel("Bok obrazka:");
		lblBokObrazka.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBokObrazka.setBounds(510, 45, 85, 14);
		panel.add(lblBokObrazka);
		
		JLabel lblMaxD = new JLabel("Min d:");
		lblMaxD.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMaxD.setBounds(510, 138, 85, 14);
		panel.add(lblMaxD);
		
		JLabel lblMinD = new JLabel("Max d:");
		lblMinD.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMinD.setBounds(510, 169, 85, 14);
		panel.add(lblMinD);

		textIter = new JTextField();
		textIter.setText("300");
		textIter.setBounds(600, 12, 86, 20);
		panel.add(textIter);
		textIter.setColumns(10);

		textMinNi = new JTextField();
		textMinNi.setText("0.01");
		textMinNi.setBounds(600, 74, 86, 20);
		panel.add(textMinNi);
		textMinNi.setColumns(10);
                
		textMaxNi = new JTextField();
		textMaxNi.setText("0.1");
		textMaxNi.setColumns(10);
		textMaxNi.setBounds(600, 104, 86, 20);
		panel.add(textMaxNi);
		
		textImgSize = new JTextField();
		textImgSize.setText("50");
		textImgSize.setColumns(10);
		textImgSize.setBounds(600, 43, 86, 20);
		panel.add(textImgSize);
		
		textMinD = new JTextField();
		textMinD.setText("4");
		textMinD.setColumns(10);
		textMinD.setBounds(600, 135, 86, 20);
		panel.add(textMinD);
		
		textMaxD = new JTextField();
		textMaxD.setText("20");
		textMaxD.setColumns(10);
		textMaxD.setBounds(600, 166, 86, 20);
		panel.add(textMaxD);
		
		textTrainset = new JTextField();
		textTrainset.setText("10");
		textTrainset.setColumns(10);
		textTrainset.setBounds(600, 197, 86, 20);
		panel.add(textTrainset);
                
                checkFunction = new JCheckBox();
		checkFunction.setBounds(620, 230, 86, 20);
		panel.add(checkFunction);
		
		JLabel lblTrainset = new JLabel("Trainset:");
		lblTrainset.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTrainset.setBounds(510, 200, 85, 14);
		panel.add(lblTrainset);
		
		JLabel lblFunction = new JLabel("Gauss Function:");
		lblFunction.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFunction.setBounds(510, 230, 105, 14);
		panel.add(lblFunction);
	}

	class DisplayPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(image, 0, 0, 400, 400, null);
			repaint();
		}
	}
}
