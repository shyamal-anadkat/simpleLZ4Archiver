package zostale.utilities.lz4Archiver;


/*Author: Shyamal Anadkat
 * Summer 2016 
 */
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.*;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

public class Lz4Archiver extends JFrame implements ActionListener, WindowListener {

	private static final long serialVersionUID = 1L;
	//CONFIGURE DESTINATION AND FILE NAME 
	static File lz4file = null;
	static File outputCSV = null;
	static File outZipped = null;
	static File selectedFile = null;
	static boolean toggleZip = true; 
	static boolean inFileIsLZ = false; 
	//true if you want to ZIP.
	//CONFIG 

	static ProcessProgressBar pb = new ProcessProgressBar();
	static JFileChooser fileChooser = new JFileChooser();
	static JFrame frame;
	static JTextArea jTextArea; 
	static Font font = new Font("Verdana", Font.PLAIN, 18);
	static boolean fileSelected = false; 

	public Lz4Archiver() {}

	public static void main( String[] args )
	{

		//SETUP J FRAME 
		frame = new JFrame("Zostale - Simple LZ4 Archiver");
		frame.setBackground(new Color(0, 204, 255));
		frame.setFont(font);
		frame.setLocation(10,200); // default is 0,0 (top left corner)
		frame.setPreferredSize(new Dimension(1020,600));
		frame.setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);


		//REDIRECT OUTPUT STREAM
		JTextArea textArea = new JTextArea(50, 90);
		PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
		System.setOut(printStream);
		System.setErr(printStream);

		//ADD ZIP BUTTON AND LISTENER 
		JButton zipButton = new JButton("ZIP");
		zipButton.setFont(font);
		zipButton.setForeground(Color.WHITE);
		zipButton.setPreferredSize(new Dimension(120,50));
		zipButton.setBackground( new Color(153, 0, 0));
		jTextArea = new JTextArea();
		zipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if(!fileSelected) {
					JOptionPane.showMessageDialog(null, "No file selected ! Try again.","Message", JOptionPane.ERROR_MESSAGE);
					System.out.println("NULL FILE");
				}
				else {
					toggleZip = true;
					process();
				}
			}

		});

		//ADD UNZIP BUTTON AND LISTENER 
		JButton unzipButton = new JButton("UNZIP");
		unzipButton.setFont(font);
		unzipButton.setForeground(Color.WHITE);
		unzipButton.setPreferredSize(new Dimension(120,50));
		unzipButton.setBackground( new Color(0, 0, 153));
		jTextArea = new JTextArea();
		unzipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if(!fileSelected || !inFileIsLZ) {
					JOptionPane.showMessageDialog(null, "No file selected / Invalid Format. Try again.","Message", JOptionPane.ERROR_MESSAGE);
					System.out.println("No file selected");
				}
				else {
					System.out.println("Unzip Selected");
					toggleZip = false;
					process();
				}
			}

		});

		//ADD INPUT FILE SELECTOR BUTTON
		JButton inbutton = new JButton("Select Input File");
		inbutton.setFont(font);
		inbutton.setPreferredSize(new Dimension(180,50));
		inbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser fileChooser = new JFileChooser();
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					if(fileChooser.getSelectedFile().getName().contains("lz4")) {
						lz4file = fileChooser.getSelectedFile();
						inFileIsLZ= true;
						fileSelected = true;
					}
					else {
						selectedFile = fileChooser.getSelectedFile();
						fileSelected = true;
						inFileIsLZ= false;
					}
				}
			}
		});

		//ADD INPUT FILE SELECTOR BUTTON
		JButton outButton = new JButton("Select Output Directory");
		outButton.setFont(font);
		outButton.setPreferredSize(new Dimension(250,50));
		outButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser  = new JFileChooser(); 
				fileChooser .setCurrentDirectory(new java.io.File("."));
				fileChooser .setDialogTitle("Please choose output directory");
				fileChooser .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser .setAcceptAllFileFilterUsed(false);
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 

					System.out.println("\nOutput : " 
							+  fileChooser.getSelectedFile());
					if(!inFileIsLZ) {
						outZipped = new File(fileChooser.getSelectedFile()+"\\"+selectedFile.getName().split("\\.")[0]+".lz4");
						System.out.println(outZipped.getAbsolutePath());
					}
					else {
						outputCSV =  new File(fileChooser.getSelectedFile()+"\\"+lz4file.getName().split("\\.")[0]+".csv");
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "No output directory selected ! Try again.", "Message", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		//ADD ELEMENTS TO FRAME 
		frame.add(inbutton);
		frame.add(outButton);
		frame.add(zipButton);
		frame.add(unzipButton);
		frame.add(jTextArea);
		frame.add(textArea);
		frame.pack();

	}

	public static void process() {
		//GETTING FILE SIZE 
		final long startTime = System.nanoTime();

		if (toggleZip) {
			long fileSizeInBytes = selectedFile.length();
			long fileSizeInKB = fileSizeInBytes / 1024;
			long fileSizeInMB = fileSizeInKB / 1024;
			System.out.println("File Size (MB): " + fileSizeInMB);
			System.out.println("Zipping File: "+ selectedFile.getPath());
			//pb.start();
			lz4Zip(selectedFile, outZipped);
			//pb.show = false;
			System.out.println("Zipped file size (MB): "+(outZipped.length()/1024)/1024);
		}

		else {
			System.out.println("Unzipping File: "+ lz4file);
			//pb.start();
			lz4Unzip(lz4file,outputCSV);
			//pb.show = false;
			System.out.println("Unzipped file size (MB): "+(outputCSV.length()/1024)/1024);
		}

		//Prints out the duration 
		final long duration = NANOSECONDS.toSeconds(System.nanoTime() - startTime);
		System.out.println("\nTime taken for the process : " + duration +" seconds.");
		System.out.println("Method: LZ4 Compression Algorithm");
		System.out.println("*******************************************");
		System.out.println("\n#####@author:SANADKAT#####");
		String doneWhat = toggleZip ? "ZIPPING":"UNZIPPING";
		JOptionPane.showMessageDialog(null, "Done "+doneWhat+" the file requested.");
		postProcessCleanup();
	}


	/**
	 * 
	 * @param input
	 * @param output
	 */
	public static void lz4Zip(File input, File output) {

		byte[] buf = new byte[2048];
		try {
			LZ4BlockOutputStream out = new LZ4BlockOutputStream(new FileOutputStream(output), 32*1024*1024);
			FileInputStream in = new FileInputStream(input);
			int len;
			while((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void postProcessCleanup() {
		//Make selected file null and selected as false after process 
		selectedFile = null;
		lz4file = null;
		outZipped = null;
		outputCSV = null;
		fileSelected = false;
		inFileIsLZ = false;
	}

	/**
	 * 
	 * @param lz4file
	 * @param outputFile
	 */
	public static void lz4Unzip(File lz4file, File outputFile){
		byte[] buf = new byte[2048];
		try {
			LZ4BlockInputStream in = new LZ4BlockInputStream(new FileInputStream(lz4file));
			FileOutputStream out = new FileOutputStream(outputFile);
			int len;
			while((len = in.read(buf)) > 0){
				out.write(buf, 0, len);

			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void windowOpened(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void actionPerformed(ActionEvent e) {}
}


/**
 * Generate Progress Bar 
 * @author SAnadkat
 *
 */
class ProcessProgressBar extends Thread {
	boolean show = true; 
	@Override 
	public void run() {
		String animation = "============================\r";
		int counter = 0;
		while(show) {
			System.out.print(animation.substring(0, counter++ % animation.length())); 
			try { Thread.sleep(1000); }
			catch (Exception e) {};
		}
	}
}


/**
 * redirect output to a JTextArrea
 *
 */
class CustomOutputStream extends OutputStream {
	public JTextArea textArea;

	public CustomOutputStream(JTextArea textArea) {
		this.textArea = textArea;
	}
	@Override
	public void write(int b) throws IOException {
		textArea.append(String.valueOf((char)b));
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}
}






