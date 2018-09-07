package MyTextClassify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
interface ConstantInterface {    
    //int[] ATTRCOUNT= {10000};
	int[] ATTRCOUNT= {30};
    int KNN_K=100;
}    
class CHIWordItem{
	String word;
	double CHI;
	int position;
	double d;//log(N/ni)
	public CHIWordItem(String s,double d1) {
		word=s;
		CHI=d1;
		position=0;
		d=0;
	}
}

class documentItem{
	double[] weight;//向量表示，TFIDF权值
	String cl;
	public documentItem(int i) {
		weight=new double[ConstantInterface.ATTRCOUNT[i]];
		cl=null;
	}
	void setW (int i,double d) {
		weight[i]=d;
	}
	void showW() {
		for(int i=0;i<weight.length;i++) {
			System.out.print(weight[i]+"  ");
			if(i%100==99)
				System.out.println(" ");
		}
		System.out.println("The document belongs to "+cl);
	}
}
class similarityItem{
	double sim;
	String cl;
	public similarityItem() {
		sim=0;
		cl=null;
	}
}
class PrecisionCountItem{
	int rnum;//判断属于该类的文档中确实属于该类的文档数
	int totalNum;//判断属于该类的文档数
	double p;
	public PrecisionCountItem() {
		totalNum=0;
		rnum=0;
		p=-1;
	}
	public void wrong() {
		totalNum++;
	}
	public void right() {
		totalNum++;
		rnum++;
	}
	public double getP() {
		p=(double)(rnum)/totalNum;
		return p;
	}
}
class EvaluationItem {
	private double a,b,c,d;
	public double getA() {
		return a;
	}
	public double getB() {
		return b;
	}
	public double getC() {
		return c;
	}
	public double getD() {
		return d;
	}
	public void setA(double i) {
		a=i;
	}
	public void setB(double i) {
		b=i;
	}
	public void setC(double i) {
		c=i;
	}
	public void setD(double i) {
		d=i;
	}
	public EvaluationItem() {
		a=b=c=d=0;
	}
	public double getP() {
		if(a==0)
			return 0;
		return a/(a+b);
	}
	public double getR() {
		return a/(a+c);
	}
	public void show() {
		System.out.println("                                       真正属于该类的文档数     "+"    真正不属于该类的文档数");
		System.out.println("判断为属于该类的文档数             "+a+"                     "+b);
		System.out.println("判断为不属于该类的文档数          "+c+"                     "+d);
	}
}
class F1{
	double macro_avr;
	double micro_avr;
	public F1(double d1,double d2) {
		macro_avr=d1;
		micro_avr=d2;
	}
}
class Macro_avr{
	public static double Macro_r(Map<String,EvaluationItem> e) {
		Iterator<Entry<String,EvaluationItem>> i = e.entrySet().iterator();
		String key;
		double r=0;
		while(i.hasNext()) {
		    Map.Entry<String,EvaluationItem> entry = (Map.Entry<String,EvaluationItem>)i.next();
		    // 获取key
		    key = (String)entry.getKey();		  
		    r+=entry.getValue().getR();
		}
		return r/e.size();
	}
	public static double Macro_p(Map<String,EvaluationItem> e) {
		Iterator<Entry<String,EvaluationItem>> i = e.entrySet().iterator();
		String key;
		double p=0;
		while(i.hasNext()) {
		    Map.Entry<String,EvaluationItem> entry = (Map.Entry<String,EvaluationItem>)i.next();
		    // 获取key
		    key = (String)entry.getKey();		  
		    p+=entry.getValue().getP();
		}
		return p/e.size();
	}
	public static double Macro_F1(Map<String,EvaluationItem> e) {
		Iterator<Entry<String,EvaluationItem>> i = e.entrySet().iterator();
		String key;
		double p,r,f1=0;
		while(i.hasNext()) {
		    Map.Entry<String,EvaluationItem> entry = (Map.Entry<String,EvaluationItem>)i.next();
		    // 获取key
		    key = (String)entry.getKey();
		    r=entry.getValue().getR();
		    p=entry.getValue().getP();
		    if(!(p==0||r==0))	    	
		    	f1+=2*p*r/(p+r);
		}
		return f1/e.size();
	}
	public static void show(Map<String,EvaluationItem> e) {
		System.out.println("Macro_r="+Macro_r(e));
		System.out.println("Macro_p="+Macro_p(e));
		System.out.println("Macro_F1="+Macro_F1(e));
	}
}
class Micro_avr{
	public static double Micro_r(Map<String,EvaluationItem> e) {
		Iterator<Entry<String,EvaluationItem>> i = e.entrySet().iterator();
		String key;
		double a=0;
		double c=0;
		while(i.hasNext()) {
		    Map.Entry<String,EvaluationItem> entry = (Map.Entry<String,EvaluationItem>)i.next();
		    // 获取key
		    key = (String)entry.getKey();		  
		    a+=entry.getValue().getA();
		    c+=entry.getValue().getC();
		}
		return a/(a+c);
	}
	public static double Micro_p(Map<String,EvaluationItem> e) {
		Iterator<Entry<String,EvaluationItem>> i = e.entrySet().iterator();
		String key;
		double a=0;
		double b=0;
		while(i.hasNext()) {
		    Map.Entry<String,EvaluationItem> entry = (Map.Entry<String,EvaluationItem>)i.next();
		    // 获取key
		    key = (String)entry.getKey();		  
		    a+=entry.getValue().getA();
		    b+=entry.getValue().getB();
		}
		return a/(a+b);
	}
	public static double Micro_F1(Map<String,EvaluationItem> e) {
		Iterator<Entry<String,EvaluationItem>> i = e.entrySet().iterator();
		String key;
		double p,r,f1;
		p=Micro_p(e);
		r=Micro_r(e);
		f1=2*p*r/(p+r);
		return f1;
	}
	public static void show(Map<String,EvaluationItem> e) {
		System.out.println("Micro_r="+Micro_r(e));
		System.out.println("Micro_p="+Micro_p(e));
		System.out.println("Micro_F1="+Micro_F1(e));
	}
}
public class VSMKnnN2tfdfClassification {
	class WordItem{	
		private int[] cf;//cf储存在每一个类中出现该单词的文档数
		private boolean dfgt2;
		private double CHI;//X2统计的权值
		private int sumCf;
		public void computeCHI() {
			double A,B,C,D;
			CHI=0;
			for(int i=0;i<cf.length;i++) {
				A=cf[i];
				B=sumCf-A;
				C=documentCnt[i]-A;
				D=N-A-B-C;
				CHI+=(((A+C)/N)*(A*D-C*B)*(A*D-C*B)*N)/((A+C)*(B+D)*(A+B)*(C+D));
				
				//System.out.println(String.valueOf(A)+' '+String.valueOf(B)+' '+String.valueOf(C)+' '+String.valueOf(D)+' '+String.format("%.5f", t));								
			}
		}
		public int getSumCf() {
			return sumCf;
		}
		public void setSumCf(int i) {
			sumCf=i;
		}
		public double getCHI() {
			return CHI;
		}
		public void initCf() {
			for(int i=0;i<cf.length;i++) {
				cf[i]=0;
			}
		}
		public WordItem() {
			cf=new int[CLASSCOUNT];
			initCf();
			dfgt2=false;			
		}
		public WordItem(boolean b) {
			cf=new int[CLASSCOUNT];
			initCf();
			dfgt2=b;
		}
		public boolean getDfgt2() {
			return dfgt2;
		}
		public void addCf(int i) {
			this.cf[i]++;
		}
		public int getCf(int i) {
			return cf[i];
		}
	}
	public static F1[] f1_result=new F1[ConstantInterface.ATTRCOUNT.length];
	public static final int CLASSCOUNT=7;
	private int[] documentCnt=new int[CLASSCOUNT];
	private int N;
	private Map<String,WordItem> allWords;
	private static FileOutputStream fos;
	private PrintStream ps;
	String testPath;
	String trainPath;
	private char []a=new char[20000];
	private String[] labelsName=null;
	private CHIWordItem[] allCHI;
	private Vector<documentItem> trainDocuments;//存放所有训练文档向量
	public static void show_f1_result() {
		for(int i=0;i<f1_result.length;i++) {
			System.out.println(ConstantInterface.ATTRCOUNT[i]+"     "+f1_result[i].macro_avr+"  "+f1_result[i].micro_avr);
		}
	}
	public void setN(int i) {
		N=i;
	}
	public int getN() {
		return N;
	}
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
	public double computeNorm(double[] d) {
		double norm=0;
		for(int i=0;i<d.length;i++) {
			norm+=d[i]*d[i];
		}
		norm=Math.pow(norm, 0.5);
		return norm;		
	}
	public double computeSim(double[] d1,double[] d2) {
		double n1,n2,sim=0;
		n1=computeNorm(d1);
		n2=computeNorm(d2);
		for(int i=0;i<d1.length;i++) {
			sim+=d1[i]*d2[i];
		}
		sim=sim/(n1*n2);
		return sim;				
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
	public CHIWordItem[] sort(CHIWordItem[] pData, int left, int right){
		CHIWordItem middle,strTemp;
		int i = left;
		int j = right;
		middle = pData[(left+right)/2];
		do{
			while((pData[i].CHI>middle.CHI) && (i<right))
				i++;
			while((pData[j].CHI<middle.CHI) && (j>left))
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
	public similarityItem[] sort(similarityItem[] pData, int left, int right){
		similarityItem middle,strTemp;
		int i = left;
		int j = right;
		middle = pData[(left+right)/2];
		do{
			while((pData[i].sim>middle.sim) && (i<right))
				i++;
			while((pData[j].sim<middle.sim) && (j>left))
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
	public similarityItem[] sortByCl(similarityItem[] pData, int left, int right){
		similarityItem strTemp,middle;
		int i = left;
		int j = right;
		middle = pData[(left+right)/2];
		do{
			while((pData[i].cl.compareTo(middle.cl)<0) && (i<right))
				i++;
			while((pData[j].cl.compareTo(middle.cl)>0) && (j>left))
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
			sortByCl(pData,left,j); //递归调用
		if(right>i)
			sortByCl(pData,i,right); //递归调用
		return pData;
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
	Vector<String> read2trigram(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-2;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]"))
				if(String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]"))
					if(String.valueOf(a[i+2]).matches("[\u4e00-\u9fa5]"))
						vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2]));			
				//vtemp.add(String.valueOf(a[i]));
		}	
		//System.out.println(vtemp.size());
		return vtemp;
	}
	Vector<String> read2quadrigram(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-3;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]"))
				if(String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]"))
					if(String.valueOf(a[i+2]).matches("[\u4e00-\u9fa5]"))
						if(String.valueOf(a[i+2]).matches("[\u4e00-\u9fa5]"))
							vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2])+String.valueOf(a[i+3]));			
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
	Vector<String> read2gram12(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-1;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]")) {						
				vtemp.add(String.valueOf(a[i]));
				if (String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[i+1]));
					vtemp.add(String.valueOf(a[i+1])+String.valueOf(a[i]));
				}
			}	
		}	
		if (String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")) 
			vtemp.add(String.valueOf(a[a.length-1]));
		return vtemp;
	}
	Vector<String> read2gram1234(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-3;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]")) {
				vtemp.add(String.valueOf(a[i]));
				if(String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1]));
					if(String.valueOf(a[i+2]).matches("[\u4e00-\u9fa5]")) {
						vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2]));
						if(String.valueOf(a[i+3]).matches("[\u4e00-\u9fa5]"))
							vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2])+String.valueOf(a[i+3]));
					}
				}
			}
		}
		if (String.valueOf(a[a.length-3]).matches("[\u4e00-\u9fa5]")) {//处理最后三个字
			vtemp.add(String.valueOf(a[a.length-3]));
			if (String.valueOf(a[a.length-2]).matches("[\u4e00-\u9fa5]")) {
				vtemp.add(String.valueOf(a[a.length-2]));
				vtemp.add(String.valueOf(a[a.length-3])+String.valueOf(a[a.length-2]));
				if (String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[a.length-1]));
					vtemp.add(String.valueOf(a[a.length-2])+String.valueOf(a[a.length-1]));
					vtemp.add(String.valueOf(a[a.length-3])+String.valueOf(a[a.length-2])+String.valueOf(a[a.length-1]));
				}
			}else if(String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")){
				vtemp.add(String.valueOf(a[a.length-1]));
			}
		}else {
			if (String.valueOf(a[a.length-2]).matches("[\u4e00-\u9fa5]")) {
				vtemp.add(String.valueOf(a[a.length-2]));
				if (String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[a.length-1]));
					vtemp.add(String.valueOf(a[a.length-2])+String.valueOf(a[a.length-1]));
				}
			}else if(String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")){
				vtemp.add(String.valueOf(a[a.length-1]));
			}			
		}
		//System.out.println(vtemp.size());
		return vtemp;
	}
	Vector<String> read2gram234(String fileName) throws IOException{					
		File f=new File(fileName);
		FileReader r=new FileReader(f);
		Vector<String> vtemp=new Vector<String>();
		r.read(a);
		for(int i=0;i<a.length-3;i++) {			
			if (String.valueOf(a[i]).matches("[\u4e00-\u9fa5]")) {
				if(String.valueOf(a[i+1]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1]));
					if(String.valueOf(a[i+2]).matches("[\u4e00-\u9fa5]")) {
						vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2]));
						if(String.valueOf(a[i+3]).matches("[\u4e00-\u9fa5]"))
							vtemp.add(String.valueOf(a[i])+String.valueOf(a[i+1])+String.valueOf(a[i+2])+String.valueOf(a[i+3]));
					}
				}
			}
		}
		
		if (String.valueOf(a[a.length-2]).matches("[\u4e00-\u9fa5]")) {
			if (String.valueOf(a[a.length-1]).matches("[\u4e00-\u9fa5]")) {
				vtemp.add(String.valueOf(a[a.length-2])+String.valueOf(a[a.length-1]));
				if (String.valueOf(a[a.length-3]).matches("[\u4e00-\u9fa5]")) {
					vtemp.add(String.valueOf(a[a.length-2])+String.valueOf(a[a.length-3]));
					vtemp.add(String.valueOf(a[a.length-3])+String.valueOf(a[a.length-2])+String.valueOf(a[a.length-1]));
				}
			}else if (String.valueOf(a[a.length-3]).matches("[\u4e00-\u9fa5]")) {
				vtemp.add(String.valueOf(a[a.length-2])+String.valueOf(a[a.length-3]));
			}
		}
		//System.out.println(vtemp.size());
		return vtemp;
	}
	public void setTrainPath(String folderPat){
		trainPath=folderPat;
	}
	public void setTestPath(String testPat){
		testPath=testPat;
	}
	public Vector<documentItem> train(int attrcount) throws IOException {
		trainDocuments=new Vector<documentItem>();
		allWords=new HashMap<String,WordItem>();
		File folder=new File(trainPath);
		labelsName=folder.list();//存放所有类别的String[]
		System.out.println("Collecting attributes...");
		for(int i=0;i<labelsName.length;i++){//第一遍遍历把每个类中tf，df大于2的2gram放入allWords
			Map<String,WordItem> m=new HashMap<String, WordItem>();//存放该类下的2gram出现次数
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();//flies数组存放当前类别所有子文件名
			//System.out.println("Processing:"+labelsName[i]);
			Vector<String> v=new Vector<String>();//v用来临时存放该类下一篇文章的所有单词
			v=read2Bigram(trainPath+"\\"+labelsName[i]+"\\"+files[0]);
			String[] firstWords=new String[v.size()];
			String[] otherWords;
			for(int j=0;j<v.size();j++)
				firstWords[j]=v.elementAt(j);
			sort(firstWords,0,v.size()-1);
			String previous=firstWords[0];
			double count=1;
			for(int j=1;j<firstWords.length;j++){
				if(firstWords[j].equals(previous))
					count++;
				else{
					if(count>=2)
						m.put(previous, new WordItem());//频度大于等于2
					//将上一个单词和出现次数放入Map中
					previous=firstWords[j];
					count=1;
				}
			}
			for(int j=1;j<files.length;j++){
				try {
					v=read2Bigram(trainPath+"\\"+labelsName[i]+"\\"+files[j]);
					otherWords=new String[v.size()];
					for(int k=0;k<v.size();k++)
						otherWords[k]=v.elementAt(k);
					sort(otherWords,0,v.size()-1);//对当前类别标签下的所有文档的所有单词进行排序，
					//目的是为了使相同的词连在一起
					previous=otherWords[0];
					count=1;
					for(int k=1;k<otherWords.length;k++){
						if(otherWords[k].equals(previous))
							count++;
						else{
							if(count>=2) {//频度大于等于2
								if (m.containsKey(previous))
									m.put(previous, new WordItem(true));//分散度大于等于2
								else
									m.put(previous, new WordItem());
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
			Iterator<Entry<String, WordItem>> iter = m.entrySet().iterator();
			String key;
			while(iter.hasNext()) {
			    Map.Entry<String,WordItem> entry = (Map.Entry<String,WordItem>)iter.next();
			    // 获取key
			    key = (String)entry.getKey();
			        // 获取value
			    if (entry.getValue().getDfgt2()&&!allWords.containsKey(key)) {
			    	allWords.put(key, new WordItem(true));
			    }
			}
			//System.out.println(allWords.size());
			documentCnt[i]=files.length;//该类别下训练的文本个数
			
		}
		setN(0);
		for (int i=0;i<documentCnt.length;i++) {//N-总训练文档数
			setN(getN()+documentCnt[i]);
		}
		System.out.println("N="+N);
		System.out.println("Computing CHI...");
		for(int i=0;i<labelsName.length;i++){//第二遍遍历把allWords填完
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();//flies数组存放当前类别所有子文件名
			//System.out.println("Processing:"+labelsName[i]);
			Vector<String> v=new Vector<String>();//v用来临时存放该类下一篇文章的所有单词
			String[] tempWords;
			String previous;
			for(int j=0;j<files.length;j++){
				try {
					v=read2Bigram(trainPath+"\\"+labelsName[i]+"\\"+files[j]);
					tempWords=new String[v.size()];
					for(int k=0;k<v.size();k++)
						tempWords[k]=v.elementAt(k);
					sort(tempWords,0,v.size()-1);//对当前类别标签下的所有文档的所有单词进行排序，
					//目的是为了使相同的词连在一起
					previous=tempWords[0];
					for(int k=1;k<tempWords.length;k++){
						if(!tempWords[k].equals(previous)) {							
							if (allWords.containsKey(previous))
								allWords.get(previous).addCf(i);
							previous=tempWords[k];	
						}
					}					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}			
		}
		
		allCHI=new CHIWordItem[allWords.size()];
		Iterator<Entry<String, WordItem>> iter = allWords.entrySet().iterator();
		String key;
		int n=0;
		while(iter.hasNext()) {
		    Map.Entry<String,WordItem> entry = (Map.Entry<String,WordItem>)iter.next();
		    // 获取key
		    key = (String)entry.getKey();
		        // 获取value
		    entry.getValue().setSumCf(0);
		    for(int i=0;i<entry.getValue().cf.length;i++) {
		    	entry.getValue().setSumCf(entry.getValue().getSumCf()+entry.getValue().getCf(i));
		    }
		    entry.getValue().computeCHI();
		    //System.out.println(entry.getValue().getCHI()); 
		    allCHI[n]=new CHIWordItem(key,entry.getValue().getCHI());
		    n++;
		}
		System.out.println("Filtering attributes...");
		sort(allCHI,0,allCHI.length-1);//按CHI排序，从大到小
		double temp,Nt;
		Nt=N;
		for(int i=0;i<ConstantInterface.ATTRCOUNT[attrcount];i++) {//计算前2000个2gram的d
			temp=Math.log10(Nt/allWords.get(allCHI[i].word).getSumCf()+0.01);
			allCHI[i].position=i;
			allCHI[i].d=temp;
		    //System.out.println(allCHI[i].position+"   "+String.format("%.5f", allCHI[i].CHI)+"   "+allCHI[i].word+"   "+allCHI[i].d);
		}
		double thresholdCHI=allCHI[ConstantInterface.ATTRCOUNT[attrcount]].CHI;
		iter = allWords.entrySet().iterator();
		while(iter.hasNext()) {//从allWords里删掉CHI排2000后的
		    Map.Entry<String,WordItem> entry = (Map.Entry<String,WordItem>)iter.next();
		    // 获取key
		    key = (String)entry.getKey();
		        // 获取value
		    if (entry.getValue().getCHI()<=thresholdCHI) {
		    	iter.remove();
		    	/*在遍历Map过程中,不能用map.put(key,newVal),map.remove(key)来修改和删除元素， 
		    	     会引发 并发修改异常,可以通过迭代器的remove()： 
		    	    从迭代器指向的 collection 中移除当前迭代元素 
		    	    来达到删除访问中的元素的目的。   */
		    }
		}
		/*iter = allWords.entrySet().iterator();
		while(iter.hasNext()) {
		    Map.Entry<String,WordItem> entry = (Map.Entry<String,WordItem>)iter.next();
		    // 获取key
		    key = (String)entry.getKey();
		    if(entry.getValue().getCHI()>820)
		        System.out.println(key+"   "+entry.getValue().getCHI());
		}*/
		System.out.println(ConstantInterface.ATTRCOUNT[attrcount]);
		System.out.println("Computing feature Vector...");
		double[] tfTemp=new double[ConstantInterface.ATTRCOUNT[attrcount]];//临时存放当前文章中属性单词的tf
		Map<String,Double> tf=new HashMap<String, Double>();//临时存放当前文章的单词词频
		for(int i=0;i<labelsName.length;i++){//第三遍遍历计算每个训练文档的向量
			File subFolder=new File(trainPath+"\\"+labelsName[i]);
			String[] files=subFolder.list();//flies数组存放当前类别所有子文件名
			//System.out.println("Processing:"+labelsName[i]);
			Vector<String> v=new Vector<String>();//v用来临时存放该类下一篇文章的所有单词
			String[] tempWords;
			String previous;
			int count;
			double denominator=0;
			documentItem tempDocItem;
			for(int j=0;j<files.length;j++){//读每一篇文章
				count=1;
				try {
					v=read2Bigram(trainPath+"\\"+labelsName[i]+"\\"+files[j]);
					tempWords=new String[v.size()];
					for(int k=0;k<v.size();k++)
						tempWords[k]=v.elementAt(k);
					sort(tempWords,0,v.size()-1);//对所有单词进行排序，
					//目的是为了使相同的词连在一起
					previous=tempWords[0];
					for(int k=1;k<tempWords.length;k++){
						if(tempWords[k].equals(previous))
							count++;
						else {
							tf.put(previous, (double) count);//读取当前文章的单词词频
							previous=tempWords[k];
							count=1;
						}
					}					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {					
					e.printStackTrace();
				}
				for(int k=0;k<tfTemp.length;k++) {//统计属性单词的词频
					if(tf.containsKey(allCHI[k].word))
						tfTemp[k]=tf.get(allCHI[k].word);
					else {
						tfTemp[k]=0;
					}
				}
				for(int k=0;k<tfTemp.length;k++) {//计算TFIDF的分母
					denominator+=Math.pow(tfTemp[k]*allCHI[k].d, 2);
				}
				denominator=Math.pow(denominator, 0.5);
				tempDocItem=new documentItem(attrcount);
				for(int k=0;k<tfTemp.length;k++) {//计算训练文档向量
					tempDocItem.setW(k,tfTemp[k]*allCHI[k].d/denominator);
					tempDocItem.cl=labelsName[i];
				}
				trainDocuments.add(tempDocItem);
				tf.clear();
			}			
		}
		System.out.println(trainDocuments.size());
		/*trainDocuments.elementAt(1).showW();
		trainDocuments.elementAt(2000).showW();
		trainDocuments.elementAt(5478).showW();*/
		allWords.clear();
		return trainDocuments;
	}
	public void test(Vector<documentItem> trainDocuments,int attrcount){
		System.out.println("Testing...");
		double[] tfTemp=new double[ConstantInterface.ATTRCOUNT[attrcount]];//临时存放当前文章中属性单词的tf
		Map<String,Double> tf=new HashMap<String, Double>();//临时存放当前文章的单词词频
		Map<String,PrecisionCountItem> ac=new HashMap<String,PrecisionCountItem>();//用于计算准确率
		Map<String,EvaluationItem> eval=new HashMap<String,EvaluationItem>();//用于计算宏观平均和微观平均
		similarityItem[] trainDocSim=new similarityItem[N];
		similarityItem[] trainDocSimKnn=new similarityItem[ConstantInterface.KNN_K];
		int rnumAll=0;
		int totalNumAll=0;
		for(int m=0;m<labelsName.length;m++) {
			eval.put(labelsName[m], new EvaluationItem());
		}
		for(int m=0;m<labelsName.length;m++) {
			File testsubFolder=new File(testPath+"\\"+labelsName[m]);
			String[] testfiles=testsubFolder.list();
			int rnum=0;			
			double totalNum = testfiles.length;
			double recall;
			Vector<String> v=new Vector<String>();//v用来临时存放该类下一篇文章的所有单词
			String[] tempWords;
			String previous=null;
			int count;
			double denominator=0;
			double mostSum;
			double curSum;
			String outcome = null;
			documentItem tempDocItem;//该测试文档的向量
			for(int j=0;j<testfiles.length;j++){//读每一篇文章
				count=1;
				try {
					v=read2Bigram(testPath+"\\"+labelsName[m]+"\\"+testfiles[j]);
					tempWords=new String[v.size()];
					for(int k=0;k<v.size();k++)
						tempWords[k]=v.elementAt(k);
					sort(tempWords,0,v.size()-1);//对所有单词进行排序，
					//目的是为了使相同的词连在一起
					previous=tempWords[0];
					for(int k=1;k<tempWords.length;k++){
						if(tempWords[k].equals(previous))
							count++;
						else {
							tf.put(previous, (double) count);//读取当前文章的单词词频
							previous=tempWords[k];
							count=1;
						}
					}					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {					
					e.printStackTrace();
				}
				for(int k=0;k<tfTemp.length;k++) {//统计属性单词的词频
					if(tf.containsKey(allCHI[k].word))
						tfTemp[k]=tf.get(allCHI[k].word);
					else {
						tfTemp[k]=0;
					}
				}
				for(int k=0;k<tfTemp.length;k++) {//计算TFIDF的分母
					denominator+=Math.pow(tfTemp[k]*allCHI[k].d, 2);
				}
				denominator=Math.pow(denominator, 0.5);
				tempDocItem=new documentItem(attrcount);
				for(int k=0;k<tfTemp.length;k++) {//计算该测试文档向量
					tempDocItem.setW(k,tfTemp[k]*allCHI[k].d/denominator);
					tempDocItem.cl=labelsName[m];//该测试文档的真实类别
				}
				tf.clear();
				for(int k=0;k<N;k++) {//计算该测试文档和所有训练文档的相似度
					trainDocSim[k]=new similarityItem();
					trainDocSim[k].sim=computeSim(tempDocItem.weight,trainDocuments.get(k).weight);
					trainDocSim[k].cl=trainDocuments.get(k).cl;
				}
				sort(trainDocSim,0,trainDocSim.length-1);
				for(int k=0;k<ConstantInterface.KNN_K;k++) {
					trainDocSimKnn[k]=trainDocSim[k];
				}
				sortByCl(trainDocSimKnn,0,ConstantInterface.KNN_K-1);
				mostSum=curSum=trainDocSimKnn[0].sim;	
				outcome=trainDocSimKnn[0].cl;
				previous=trainDocSimKnn[0].cl;
				for(int k=1;k<ConstantInterface.KNN_K;k++) {
					if(trainDocSimKnn[k].cl.equals(previous))
						curSum+=trainDocSimKnn[k].sim;
					else {
						if(curSum>mostSum) {
							mostSum=curSum;
							outcome=trainDocSimKnn[k-1].cl;
						}
						previous=trainDocSimKnn[k].cl;
						curSum=trainDocSimKnn[k].sim;
					}
				}		
				if(curSum>mostSum)
					outcome=trainDocSimKnn[ConstantInterface.KNN_K-1].cl;
				if(!ac.containsKey(outcome))
					ac.put(outcome,new PrecisionCountItem());
				if(outcome.trim().equals(tempDocItem.cl.trim())) {
					rnum++;
					rnumAll++;
					ac.get(outcome).right();
				}else {
					ac.get(outcome).wrong(); 
				}
			}			
			recall=rnum/totalNum;
			totalNumAll+=totalNum;
			eval.get(labelsName[m]).setA(rnum);
			eval.get(labelsName[m]).setC(totalNum-rnum);
			System.out.println(rnum+" "+totalNum+" "+labelsName[m]+" Recall Rate :" +recall);//输出召回率
		}
		System.out.println(rnumAll+" "+totalNumAll+" "+" Recall Rate :" +(double)rnumAll/totalNumAll);
		Iterator<Entry<String, PrecisionCountItem>> iter = ac.entrySet().iterator();
		String key;
		while(iter.hasNext()) {
		    Map.Entry<String,PrecisionCountItem> entry = (Map.Entry<String,PrecisionCountItem>)iter.next();
		    // 获取key
		    key = (String)entry.getKey();
		    eval.get(key).setB(entry.getValue().totalNum-entry.getValue().rnum);
		    eval.get(key).setD(totalNumAll-eval.get(key).getA()-eval.get(key).getB()-eval.get(key).getC());
		    System.out.println(entry.getValue().rnum+"  "+entry.getValue().totalNum+"  "+key+" Precision: "+entry.getValue().getP());//输出准确率
		}
		/*Iterator<Entry<String,EvaluationItem>> i = eval.entrySet().iterator();
		while(i.hasNext()) {
		    Map.Entry<String,EvaluationItem> entry = (Map.Entry<String,EvaluationItem>)i.next();
		    // 获取key
		    key = (String)entry.getKey();
		    System.out.println(key);
		    entry.getValue().show();
		}
		*/
		f1_result[attrcount]=new F1(Macro_avr.Macro_F1(eval),Micro_avr.Micro_F1(eval));		
		ps=new PrintStream(fos);
		ps.println(ConstantInterface.ATTRCOUNT[attrcount]+"     Macro_F1:"+String.format("%.5f", f1_result[attrcount].macro_avr)
				+"      Micro_F1:"+String.format("%.5f", f1_result[attrcount].micro_avr));
		Macro_avr.show(eval);
		Micro_avr.show(eval);
		
	}
	public static void main(String[] args) throws IOException {
		VSMKnnN2tfdfClassification vc=new VSMKnnN2tfdfClassification();
		long startTrain,endTrain,startTest,endTest;
		vc.trainPath="C:\\train";
		vc.testPath="C:\\test";
		try {
			fos=new FileOutputStream("D:\\directory\\CHI4t.txt");	
			for(int i=0;i<ConstantInterface.ATTRCOUNT.length;i++) {
				startTrain=System.currentTimeMillis();				
				Vector<documentItem> trainDocuments=vc.train(i);
				endTrain=System.currentTimeMillis();
				System.out.println("Training costs "+(endTrain-startTrain)/1000+"s");
				startTest=System.currentTimeMillis();		
				vc.test(trainDocuments,i);
				endTest=System.currentTimeMillis();
				System.out.println("Test costs "+(endTest-startTest)/1000+"s");
			}	
			show_f1_result();		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			fos.close();
		}
		
				
	}
}
