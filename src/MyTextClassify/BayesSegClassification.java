package MyTextClassify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jeasy.analysis.MMAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

public class BayesSegClassification{
	private String label=null;
	private long trainTime=0;
	public String[] labelsName=null;
	public Vector<Label> labels=null;
	public Set<String> vocabulary=new HashSet<String>();
	public String trainPath=null;
	public String testPath=null;
	public int findMax(double[] values){
		double max=values[0];
		int mark=0;
		for(int i=0;i<values.length;i++){
			if(values[i]>max){
				max=values[i];
				mark=i;
			}
		}
		return mark;
	}
	public String[] sort(String[] pData, int left, int right){
		String middle,strTemp;
		int i = left;
		int j = right;
		middle = pData[(left+right)/2];
		do{
			while((pData[i].compareTo(middle)<0) && (i<right))
				i++;
			while((pData[j].compareTo(middle)>0) && (j>left))
				j--;
			if(i<=j){
				strTemp = pData[i];
				pData[i] = pData[j];
				pData[j] = strTemp;
				i++;
				j--;
			}
		}while(i<j);//�������ɨ����±꽻�����һ������
		if(left<j)
			sort(pData,left,j); //�ݹ����
		if(right>i)
			sort(pData,i,right); //�ݹ����
		return pData;
	}
	 Vector<String> readFile(String fileName) throws IOException, FileNotFoundException{
		 //����һƪ�����зִʺ���ַ����б�
		File f=new File(fileName);
		InputStreamReader isr=new InputStreamReader(new FileInputStream(f),"GBK");
		char[] cbuf=new char[(int) f.length()];
		isr.read(cbuf);
		Analyzer analyzer=new MMAnalyzer();//�����������ƥ������ķִ��㷨���൱�ڷִ����ȵ���1
		TokenStream tokens=analyzer.tokenStream("Contents", new StringReader(new String(cbuf)));
		Token token=null;
		Vector<String> v=new Vector<String>();
		while((token=tokens.next(new Token()))!=null){
			v.add(token.term());
		}
		return v;
	}
	public static void main(String[] args) throws IOException{
		long startTrain=System.currentTimeMillis();
		BayesSegClassification nc=new BayesSegClassification();
		nc.trainPath="C:\\train";
		nc.train();
		long endTrain=System.currentTimeMillis();
		System.out.println("Training costs "+(endTrain-startTrain)/1000+"s");
		long startTest=System.currentTimeMillis();
		nc.testPath="C:\\test";
		nc.test();
		long endTest=System.currentTimeMillis();
		System.out.println("Testing costs "+(endTest-startTest)/1000+"s");
	}
	public void setTrainPath(String folderPat){
		trainPath=folderPat;
	}
	public void setTestPath(String testPat){
		testPath=testPat;
	}
	public void train() {
		long startTime=System.currentTimeMillis();
		File folder=new File(trainPath);
		labelsName=folder.list();//�����������String[]
		labels=new Vector<Label>();//labels����������label�µ��ʵ���Ϣ
		for(int i=0;i<labelsName.length;i++){
			labels.add(new Label());//
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();//flies�����ŵ�ǰ����������ļ���
			System.out.println("Processing:"+labelsName[i]);
		//	GUI.setTextArea("Processing:"+labelsName[i]);
			Vector<String> v=new Vector<String>();//v������ʱ��Ÿ����µ����е���
			for(int j=0;j<files.length;j++){
				//System.out.print(files[j]+" ");
				try {
					v.addAll(readFile(trainPath+"\\"+labelsName[i]+"\\"+files[j]));
					//addall�������ڽ������е�����Ԫ����ӵ�list,
					//��v�������������е��ʵļ���
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//�ѵ�ǰ����ǩ�µ������ĵ������е��ʶ�����Set�����У�
			//Ŀ����Ϊ�˻��vocabulary�Ĵ�С
			vocabulary.addAll(v);
			//�Ե�ǰ����ǩ�µ������ĵ������е��ʽ�������
			//Ŀ����Ϊ��ʹ��ͬ�Ĵ�����һ��
			String[] allWords=new String[v.size()];
			for(int j=0;j<v.size();j++)
				allWords[j]=v.elementAt(j);
			sort(allWords,0,v.size()-1);
			//ͳ�Ƹ������ʵ���Ϣ
			String previous=allWords[0];
			double count=1;
			Map<String,WordItem> m=new HashMap<String, WordItem>();
			//��ʱ�ĸ�ֵMap����
			for(int j=1;j<allWords.length;j++){
				if(allWords[j].equals(previous))
					count++;
				else{
					m.put(previous, new WordItem(count));
					//����һ�����ʺͳ��ִ�������Map��
					previous=allWords[j];
					count=1;
				}
			}
			labels.elementAt(i).set(m, v.size(),files.length);
			//�����i���������е���ͳ����Ϣmap��
			//�ܵ��ʸ����͸������ѵ�����ı�����
			long endTime=System.currentTimeMillis();
			trainTime=endTime-startTime;
		}
		//�����vocabulary�Ĵ�С������ſ�ʼ�����Ƶ
		for(int i=0;i<labels.size();i++){
			
			Iterator iter=labels.elementAt(i).m.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, WordItem> entry=
						(Map.Entry<String, WordItem>)iter.next();
				WordItem item=entry.getValue();			
				item.setFrequency(Math.log10((item.count+1)/(labels.
						elementAt(i).wordCount+vocabulary.size())));
			}
		}
	}
	//	static void process(String folderPath) throws IOException{
	//		
	//	}
		public void test(){
			for(int m=0;m<labelsName.length;m++) {
				File testsubFolder=new File(testPath+"\\"+labelsName[m]);
				String[] testfiles=testsubFolder.list();
				int rnum=0;
				double totalnum = testfiles.length;
				double recall;
				for(int k=0;k<testfiles.length;k++) {
					Vector<String> v=null;
					try {
						v = readFile(testPath+"\\"+labelsName[m]+"\\"+testfiles[k]);//v�Ǹ������е����е���
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					double values[]=new double[labelsName.length];//���ÿһ���������ĸ���					
					for(int i=0;i<labels.size();i++){//�����������ÿһ�����ĸ���
						double tempValue=0;//labels.elementAt(i).documentCount;
						for(int j=0;j<v.size();j++){//����ÿһ�����ʳ��ֵĸ���
							if(labels.elementAt(i).m.containsKey(v.elementAt(j))){	//��i�����������������							
								tempValue+=labels.elementAt(i).m.get(v.elementAt(j)).frequency; 
								
							}else{//����ע���i����������������ֵĻ��޷��ҵ�frequency
								tempValue+=Math.log10(1/(double)(labels.elementAt(i).wordCount+vocabulary.size()));
							}
						}
						values[i]=tempValue;						
					}
					
					int maxIndex=findMax(values);					
					label=labelsName[maxIndex];//label�Ƿ�����
					if (labelsName[maxIndex].trim().equals(labelsName[m]))
						rnum++;
				
				}
				recall=rnum/totalnum;
				System.out.println(rnum+" "+totalnum+" "+labelsName[m]+" Recall Rate :" +recall);
			}	
		}
		public String getLabelName(){
			return label;
		}
		public long getTrainingTime(){
			return trainTime;
		}
}
class Label{//����ǩ
	//m���������ÿ�����ʼ���ͳ����Ϣ
	Map<String,WordItem> m=new HashMap<String,WordItem>();
	double wordCount;//ĳ������ǩ�µ����е��ʸ���
	double documentCount;//ĳ������ǩ�µ������ĵ�����
	public Label() {
		this.m=null;
		this.wordCount=-1;
		this.documentCount=-1;
	}
	public void set(Map<String,WordItem> m,double wordCount,double documentCount) {
		this.m=m;
		this.wordCount=wordCount;
		this.documentCount=Math.log10(documentCount);
	}
}
class WordItem{//���ʵ�ͳ����Ϣ�������ʵĸ����ʹ�Ƶ
	double count;//���ʵĸ���
	double frequency;//��Ƶ������Ҫ�ڵó�vocabulary�Ĵ�С֮����ܼ���
	public WordItem(double count) {
		this.count=count;
		this.frequency=-1;
	}
	public void setFrequency(double frequency){
		this.frequency=frequency;
	}
}
