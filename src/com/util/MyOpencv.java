package com.util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.frame.MyFrame;

public class MyOpencv {

	String bodyImg;	//С��ͼƬ������ƥ��
	String sourceImg;	//ÿһ������ǰ�Ľ�ͼ
	String whiteDotImg;	//С�׵㣬��ƥ�䵽�����յ�
	Point bodyPoint; 	//С�˵����Ͻ�����
	Point startPoint;	//��㣬��С�˵�����
	Point endPoint;		//�յ㣬��һ����������
	long distance;	//��㵽�յ�ľ���
	int presstime;		//��ѹ��ʱ�䣬
	
	private Mat body;
	private Mat source;
	private Mat result;
	private Mat imgCanny; //��Ե�������ͼ���Ҷ�ͼ
	private MyFrame frame;
	
	public MyOpencv(String bodyImg,String sourceImg,String whiteDotImg,MyFrame frame) {
		
		this.bodyImg = bodyImg;
		this.sourceImg = sourceImg;
		this.whiteDotImg = whiteDotImg;
		this.startPoint = new Point(0,0);
		this.endPoint = new Point(0,0);
		this.bodyPoint = new Point(0,0);
		this.distance = 0;
		this.presstime = 0;
			
		this.frame = frame;
	}
	
	//����ͼ��ģ��ƥ�䣬ģ��ΪС�ˣ�ƥ��С���ڽ�ͼ�е�λ�ã��Ӷ��õ��������
	public void matchBody() {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);  	
		//���ؾ����ʼ��
		this.body = Imgcodecs.imread(this.bodyImg);
		this.source = Imgcodecs.imread(this.sourceImg);
		this.result = Mat.zeros(source.rows(), source.cols(),source.type());//��ʼ��ȫΪ0
    	Imgproc.matchTemplate(source, body, result, Imgproc.TM_CCOEFF_NORMED);
    	
    	//�ڸ����ľ�����Ѱ��������Сֵ(�������ǵ�λ��),minMaxLocResult ���ص� maxLocΪƥ���ģ���ڽ�ͼ�е�����λ��
    	MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result);
    	//bodyPoint��ʾС���ڽ�ͼ�е����Ͻ�����
    	this.bodyPoint = new Point(minMaxLocResult.maxLoc.x, minMaxLocResult.maxLoc.y);
    	System.out.println("С��������㣺"+this.bodyPoint);
    	
    	//����С�˵ĸ߶ȺͿ���Լ��ڽ�ͼ�е�λ�ã����Եõ�С�˵����ĵ㣬�����
    	Point point = 
    			new Point(this.bodyPoint.x + body.width()/2, this.bodyPoint.y + 0.8*body.height());
    	this.setStartPoint(point);
    	System.out.println("������꣺"+this.startPoint);
    	
    	//����һ�ݽ�ͼ�����С�˵ı�Ǻ������
    	Mat mark = this.source.clone();	
    	//����һ�����ΰ�С�˿�����
    	Imgproc.rectangle(mark, bodyPoint, 
    			new Point((int)(this.bodyPoint.x+this.body.cols()), (int)(this.bodyPoint.y+this.body.rows())),new Scalar(0,255,0),5); 
    	//����һ���㣬��ʾ����������
    	Imgproc.circle(mark,startPoint , 
    			10,new Scalar(255,0,0), -1);
    	//����Ǻõ�ͼд��������mark.jpg
    	Imgcodecs.imwrite("images/mark.jpg", mark);   	
	}

	//��Ե���
	public void edgeDetection() {
		
		//��ʼ������Ϊ0����ߺ�ԭ��ͼһ��
		Mat imgBlur = Mat.zeros(source.rows(), source.cols(),CvType.CV_32FC1);
		//��ͼƬ���и�˹ģ������ȥ�����
    	Imgproc.GaussianBlur(source, imgBlur, new Size(5,5), 0);
    	//��������˹ģ�������ͼƬд�����ļ���
    	Imgcodecs.imwrite("images/imgBlur.jpg", imgBlur);
    	//��ʼ������Ϊ0����ߺ�ԭ��ͼһ��
    	this.imgCanny = Mat.zeros(this.source.rows(), this.source.cols(),CvType.CV_32FC1);
    	//��˹ģ��������ͼƬ���б�Ե��⴦��
    	Imgproc.Canny(imgBlur, this.imgCanny, 1, 10);
    	
    	//�Ȱ�ͼƬ�е�С��Ĩ��������ü������С��Ӱ�����
    	for(int i = (int)this.bodyPoint.x; i <= (int)(this.bodyPoint.x+this.body.width()); i++) {
    		for(int j = (int)this.bodyPoint.y; j <= (int)(this.bodyPoint.y+this.body.height()); j++) {
    			this.imgCanny.put(j, i, 0);//ע�������i��j��x����������ͼ����������y������
    		}
    	}
    	//��Ե�����ͼƬ������ļ���
    	Imgcodecs.imwrite("images/imgCanny.jpg", this.imgCanny);
    	  	
	}
	
	public void findEndPoint() {
		
		//�ȶԱ�Ե�����ͼƬ���м���
		//��������Ϊ��ص�ֻ����ͼƬ���ϰ벿�֣�
		//�������ڷ������±ߣ����Կ��Ի��ֳ�һ����ŵ�����(�����꣨0��ͼƬ�߶ȵ�1/4������ʼ�Ŀ�ΪԭͼƬ�Ŀ�����ΪԭͼƬ��1/4�ľ���)
		Mat imgCut = new Mat(this.imgCanny, 
				new Rect(0, (int)Math.round(source.height()*0.25), source.width(), (int)Math.round(source.height()*0.25)));
		//����һ�ݣ�������ļ���
    	Mat imgCopy = imgCut.clone();
    	Imgcodecs.imwrite("images/imgCut.jpg", imgCopy);
    	
    	imgCopy.convertTo(imgCopy, CvType.CV_64FC3);	//������һ������ļ���ᱨ��
    	int size = (int) (imgCopy.total() * imgCopy.channels());	//���ص�����*ͨ�������Ҷ�ͼͨ����Ϊ1
    	System.out.println("�ü����ͼƬ��ߣ�"+imgCopy.rows()+","+imgCopy.cols());
    	System.out.println("���ص�������"+size);
    	System.out.println("ͨ������"+imgCopy.channels());
    	double data[] = new double[size];	//�洢���ص��һά����
    	imgCopy.get(0, 0, data);	//����(0,0)���ص��ֵ��ŵ�������,ֻ������ֵ��0Ϊ��ɫ��255Ϊ��ɫ
    	
    	//������Ļ��ü����ͼ����ֻʣ�յ����ڵķ��飬�������£���������
    	//�ҵ���һ����ɫ��A(x1,y1),�Լ����������İ�ɫ��B(x2,y2),�յ�����꼴Ϊ(x1,y2)
    	Point point = new Point(0,0);
    	int maxX = 0; //�洢���x����
    	for(int i = 0; i < imgCopy.rows();i++) {
    		for(int j = 0; j < imgCopy.cols(); j++) {
    			//������һά������±�ת��(i,j)��[i*ÿ�е�����+��ǰ���ڵ�����j]
    			if((int)data[i*imgCopy.cols()+j] == 255&&point.x == 0) {
    				point.x = j;
    			}
    			if((int)data[i*imgCopy.cols()+j] == 255 && j>maxX && point.x!=0) {
    				point.y = i;
    				maxX = j;
    			}
    		}
    	}
    	//����ڲü����ͼƬ�е��յ㣬��ʱ�õ������겢�����������յ����꣬��Ϊ���Բü����ͼƬ���Ͻ�Ϊ���
    	Imgproc.circle(imgCopy, endPoint,10,new Scalar(255,0,255), -1);
    	Imgcodecs.imwrite("images/imgCut.jpg", imgCopy);
    	point.y += this.source.height()*0.25; //y����Ӧ���ϲü���1/4,�����겻��
    	this.setEndPoint(point);
    	Imgproc.circle(this.imgCanny, this.endPoint,10,new Scalar(255,0,255), -1);   	
    	Imgcodecs.imwrite("images/imgCanny.jpg", imgCanny);
	}

	public void setDistance() {
		
		this.distance = 
				Math.round(Math.sqrt(Math.pow(this.startPoint.x-this.endPoint.x, 2)
						+Math.pow(this.startPoint.y-this.endPoint.y, 2)));
	}
	
	public boolean findWhiteDot() {
		
		Mat whiteDot = Imgcodecs.imread(this.whiteDotImg);
    	source = Imgcodecs.imread(this.sourceImg);
    	Mat dotResult = Mat.zeros(this.source.rows(), this.source.cols(),this.source.type());
    	Imgproc.matchTemplate(this.source, whiteDot, dotResult, Imgproc.TM_CCOEFF_NORMED);
    	MinMaxLocResult dotLocResult = Core.minMaxLoc(dotResult);
    	
    	if(dotLocResult.maxVal > 0.8) {
    		System.out.println("С�׵�ƥ��ֵ��"+dotLocResult.maxVal);
    		System.out.println("�ҵ�С�׵�");
    		this.endPoint.x = dotLocResult.maxLoc.x + whiteDot.width()/2;
    		this.endPoint.y = dotLocResult.maxLoc.y + whiteDot.height()/2;
    		
    		Mat imgResult = source.clone();
    		Imgproc.circle(imgResult, endPoint, 10, new Scalar(255, 255, 0),-1);
    		Imgcodecs.imwrite("images/dotLocResult.jpg", imgResult);
    		
    		return true;
    	}
    	return false;
	}
	public void setPretime(double x) {
		this.presstime = (int)(this.distance*x);
	}
	public Point getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(Point endPoint) {
		this.endPoint = endPoint;
	}

	public int getPresstime() {
		return presstime;
	}

	public void go() {
		this.matchBody();
		this.edgeDetection();
		if(!findWhiteDot()) {
			this.findEndPoint();
		}
		this.setDistance();
		this.setPretime(Double.parseDouble(this.frame.getTextField().getText()));
	}
	
}
