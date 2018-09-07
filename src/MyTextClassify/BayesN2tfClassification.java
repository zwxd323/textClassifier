package MyTextClassify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import jeasy.analysis.MMAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

	
public class BayesN2tfClassification{
	private String label=null;
	private long trainTime=0;
	public String[] labelsName=null;
	public Vector<Labeltfdf> labels=null;
	public Set<String> vocabulary=new HashSet<String>();
	public String trainPath=null;
	public String testPath=null;
	public char []a=new char[20000];	
	
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
	static void showVector(Vector<String> v) {
		Enumeration vEnum = v.elements();
		 while(vEnum.hasMoreElements())
	         System.out.print(vEnum.nextElement() + "  ");
		
	}
	Vector<String> read2Bigram(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-1;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]"))
				if(String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]"))
					vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1]));			
				//vtemp.add(String.valueOf(a[i]));
		}	
		//System.out.println(vtemp.size());
		return vtemp;
	}
	Vector<String> read2Unigram(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-1;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]"))
						
				vtemp.add(String.valueOf(a[i]));
		}	
		return vtemp;
	}
	 static Vector<String> readFile(String fileName) throws IOException, FileNotFoundException{
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
		BayesN2tfClassification nc=new BayesN2tfClassification();
		//showVector(nc.readFile("C:\\train\\C11-Space\\C11-Space0431.txt"));
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
	public void train() throws IOException {
		long startTime=System.currentTimeMillis();
		File folder=new File(trainPath);
		labelsName=folder.list();//�����������String[]
		labels=new Vector<Labeltfdf>();//labels����������label�µ��ʵ���Ϣ
		for(int i=0;i<labelsName.length;i++){
			labels.add(new Labeltfdf());//
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();//flies�����ŵ�ǰ����������ļ���
			System.out.println("Processing:"+labelsName[i]);
			Vector<String> v=new Vector<String>();//v������ʱ��Ÿ�����һƪ���µ����е���
			Vector<String> vtotal=new Vector<String>();
			v=readFile(trainPath+"\\"+labelsName[i]+"\\"+files[0]);
			vtotal=v;
			String[] firstWords=new String[v.size()];
			String[] otherWords;
			for(int j=0;j<v.size();j++)
				firstWords[j]=v.elementAt(j);
			sort(firstWords,0,v.size()-1);
			String previous=firstWords[0];
			double count=1;
			Map<String,WordItemtfdf> m=new HashMap<String, WordItemtfdf>();//��Ÿ����µ�ngram���ִ���
			for(int j=1;j<firstWords.length;j++){
				if(firstWords[j].equals(previous))
					count++;
				else{
					if(count>=2)
						m.put(previous, new WordItemtfdf(count));//Ƶ�ȴ��ڵ���2
					//����һ�����ʺͳ��ִ�������Map��
					previous=firstWords[j];
					count=1;
				}
			}
			for(int j=1;j<files.length;j++){
				try {
					v=readFile(trainPath+"\\"+labelsName[i]+"\\"+files[j]);
					vtotal.addAll(v);
					otherWords=new String[v.size()];
					for(int k=0;k<v.size();k++)
						otherWords[k]=v.elementAt(k);
					sort(otherWords,0,v.size()-1);//�Ե�ǰ����ǩ�µ������ĵ������е��ʽ�������
					//Ŀ����Ϊ��ʹ��ͬ�Ĵ�����һ��
					previous=otherWords[0];
					count=1;
					for(int k=1;k<otherWords.length;k++){
						if(otherWords[k].equals(previous))
							count++;
						else{
							if(count>=2) {//Ƶ�ȴ��ڵ���2
								if (m.containsKey(previous))
									m.put(previous, new WordItemtfdf(m.get(previous).count+count,true));//��ɢ�ȴ��ڵ���2
								else
									m.put(previous, new WordItemtfdf(count));
							}
							previous=otherWords[k];
							count=1;
						}
					}
					
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
			vocabulary.addAll(vtotal);	
			//System.out.println(m.size());
			(labels.elementAt(i)).setTfdf(m, vtotal.size(),files.length);//��i����ͳ�����
			//�����i���������е���ͳ����Ϣmap��
			//�ܵ��ʸ����͸������ѵ�����ı�����
			long endTime=System.currentTimeMillis();
			trainTime=endTime-startTime;
		}
		//�����vocabulary�Ĵ�С������ſ�ʼ�����Ƶ
		for(int i=0;i<labels.size();i++){
			
			Iterator iter=labels.elementAt(i).mtfdf.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, WordItemtfdf> entry=
						(Map.Entry<String, WordItemtfdf>)iter.next();
				WordItemtfdf item=entry.getValue();			
				item.setFrequency(Math.log10((item.count+1)/(labels.
						elementAt(i).wordCount+vocabulary.size())));
			}
		}
	}
	//	static void process(String folderPath) throws IOException{
	//		
	//	}
		public void test(){
			int rnumsum=0;
			int totalnumsum=0;
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
							if(labels.elementAt(i).mtfdf.containsKey(v.elementAt(j))){	//��i�����������������							
								tempValue+=labels.elementAt(i).mtfdf.get(v.elementAt(j)).frequency; 
								
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
				rnumsum+=rnum;
				totalnumsum+=totalnum;
				
			}
			
			System.out.println(rnumsum+" "+totalnumsum+" "+" Recall Rate :" +(double)rnumsum/totalnumsum);
		}
		public String getLabelName(){
			return label;
		}
		public long getTrainingTime(){
			return trainTime;
		}
}
