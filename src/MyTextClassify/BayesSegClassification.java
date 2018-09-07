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
		}while(i<j);//如果两边扫描的下标交错，完成一次排序
		if(left<j)
			sort(pData,left,j); //递归调用
		if(right>i)
			sort(pData,i,right); //递归调用
		return pData;
	}
	 Vector<String> readFile(String fileName) throws IOException, FileNotFoundException{
		 //返回一篇文章中分词后的字符串列表
		File f=new File(fileName);
		InputStreamReader isr=new InputStreamReader(new FileInputStream(f),"GBK");
		char[] cbuf=new char[(int) f.length()];
		isr.read(cbuf);
		Analyzer analyzer=new MMAnalyzer();//采用正向最大匹配的中文分词算法，相当于分词粒度等于1
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
		labelsName=folder.list();//存放所有类别的String[]
		labels=new Vector<Label>();//labels存放所有类别label下单词的信息
		for(int i=0;i<labelsName.length;i++){
			labels.add(new Label());//
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();//flies数组存放当前类别所有子文件名
			System.out.println("Processing:"+labelsName[i]);
		//	GUI.setTextArea("Processing:"+labelsName[i]);
			Vector<String> v=new Vector<String>();//v用来临时存放该类下的所有单词
			for(int j=0;j<files.length;j++){
				//System.out.print(files[j]+" ");
				try {
					v.addAll(readFile(trainPath+"\\"+labelsName[i]+"\\"+files[j]));
					//addall函数用于将集合中的所有元素添加到list,
					//故v是这个类别中所有单词的集合
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//把当前类别标签下的所有文档的所有单词都放入Set集合中，
			//目的是为了获得vocabulary的大小
			vocabulary.addAll(v);
			//对当前类别标签下的所有文档的所有单词进行排序，
			//目的是为了使相同的词连在一起
			String[] allWords=new String[v.size()];
			for(int j=0;j<v.size();j++)
				allWords[j]=v.elementAt(j);
			sort(allWords,0,v.size()-1);
			//统计各个单词的信息
			String previous=allWords[0];
			double count=1;
			Map<String,WordItem> m=new HashMap<String, WordItem>();
			//临时的赋值Map变量
			for(int j=1;j<allWords.length;j++){
				if(allWords[j].equals(previous))
					count++;
				else{
					m.put(previous, new WordItem(count));
					//将上一个单词和出现次数放入Map中
					previous=allWords[j];
					count=1;
				}
			}
			labels.elementAt(i).set(m, v.size(),files.length);
			//输入第i个类别的所有单词统计信息map和
			//总单词个数和该类别下训练的文本个数
			long endTime=System.currentTimeMillis();
			trainTime=endTime-startTime;
		}
		//获得了vocabulary的大小后，下面才开始计算词频
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
						v = readFile(testPath+"\\"+labelsName[m]+"\\"+testfiles[k]);//v是该文章中的所有单词
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					double values[]=new double[labelsName.length];//存放每一个类别算出的概率					
					for(int i=0;i<labels.size();i++){//算该文章属于每一个类别的概率
						double tempValue=0;//labels.elementAt(i).documentCount;
						for(int j=0;j<v.size();j++){//连乘每一个单词出现的概率
							if(labels.elementAt(i).m.containsKey(v.elementAt(j))){	//第i个类别如果含有这个字							
								tempValue+=labels.elementAt(i).m.get(v.elementAt(j)).frequency; 
								
							}else{//这里注意第i个类别如果不含这个字的话无法找到frequency
								tempValue+=Math.log10(1/(double)(labels.elementAt(i).wordCount+vocabulary.size()));
							}
						}
						values[i]=tempValue;						
					}
					
					int maxIndex=findMax(values);					
					label=labelsName[maxIndex];//label是分类结果
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
class Label{//类别标签
	//m中用来存放每个单词及其统计信息
	Map<String,WordItem> m=new HashMap<String,WordItem>();
	double wordCount;//某个类别标签下的所有单词个数
	double documentCount;//某个类别标签下的所有文档个数
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
class WordItem{//单词的统计信息包括单词的个数和词频
	double count;//单词的个数
	double frequency;//词频，它需要在得出vocabulary的大小之后才能计算
	public WordItem(double count) {
		this.count=count;
		this.frequency=-1;
	}
	public void setFrequency(double frequency){
		this.frequency=frequency;
	}
}
