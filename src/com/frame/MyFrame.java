package com.frame;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Point;

import com.util.MyOpencv;

public class MyFrame extends JFrame{
	
	private Label label;
	private TextField textField;
	private Button startButton;
	private Button stopButton;
	private JPanel panel;
	private TextArea textArea;
	
	//��ͼ���ϴ�ͼƬ��adb����
	private String cmdScreen = new String("adb shell screencap /sdcard/1.png");
	private String cmdPull = new String("adb pull /sdcard/1.png D:\\opencv\\WeixinJump\\images");
	
	private Point p1,p2;
	MyOpencv myOpencv = new MyOpencv("images/body.jpg", "images/1.png","images/whiteDot.jpg",this);
	private Timer timer;
	
	public MyFrame() {
		
		this.setTitle("΢����һ��");
		this.setSize(300,300);
		
		this.setLocationRelativeTo(null);
		
		panel = new JPanel(new GridLayout(2, 2));
		this.add(panel,BorderLayout.CENTER);
		
		this.timer = new Timer();
		this.label = new Label("����ϵ����");
		this.textField = new TextField("1.35");
		this.textArea = new TextArea("�ֱ���1080*1920����ϵ����1.35");
		this.startButton = new Button("��ʼ");
		this.stopButton = new Button("ֹͣ");
		this.panel.add(label);
		this.panel.add(textField);
		this.panel.add(startButton);
		this.panel.add(stopButton);
		this.add(textArea,BorderLayout.SOUTH);
		
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
			
		});
		
		this.startButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				timer = new Timer();
				//������һ����ʱ�����������1000��5000����˼����ʱ�ӳ�1��ִ�У�ÿ5��ִ��һ�Σ�����ʵ��һֱ����
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						execAdbCmd(cmdScreen); //��ȡ��ǰ�ֻ���Ļ���������ֻ���
						execAdbCmd(cmdPull); //���ֻ��еĽ�ͼ�ϴ������Ե��ļ�����
						
						//����opencv��װ�õķ���
						myOpencv.go();
						//�����ȡ�������꣬��������긽�����������һ�����꣬��֤ÿ�δ������궼��һ�����������������ǻᱻ���������쳣
						p1 = myOpencv.getStartPoint();
						p2 = new Point(p1.x+Math.random()*10,p1.y+Math.random()*15);
								
						System.out.println("��������Ϊ"+p1+" "+p2);
						//��ѹ��Ļ��adbָ��
						String cmd = 
								String.format("adb shell input swipe %d %d %d %d %d ",
										(int)p1.x,(int)p1.y,(int)p2.x,(int)p2.y,myOpencv.getPresstime());
						execAdbCmd(cmd);
						System.out.println("ok");
					}
				}, 1000,5000);
			}
			
		});
		
		this.stopButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					timer.cancel();
					System.out.println("��ʱ��ȡ��");
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				
			}
		});
	}
	
	public void execAdbCmd(String cmd) {
    	try {
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			System.out.println("execute...");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public TextField getTextField() {
		return textField;
	}

	public void setTextField(TextField textField) {
		this.textField = textField;
	}
	
}
