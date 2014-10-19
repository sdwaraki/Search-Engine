package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class TFIDFcalculation {

	HashMap<Integer,Double> h= new HashMap<Integer,Double>();
	
	TFIDFcalculation() throws IOException
	{
		// TODO Auto-generated method stub
			
		//long stime=System.currentTimeMillis();
		IndexReader r = IndexReader.open(FSDirectory.open(new File("C:/Users/Sumanth/workspace/IRProject/index")));
		TermEnum t = r.terms();
		HashMap <Integer,Double> g = new HashMap<Integer,Double>();
		int maxdoc=r.maxDoc();
		double idf = 0.0;
		while(t.next())
		{
			String a=t.term().text();
			Term tx=new Term("contents",a);
			TermDocs td = r.termDocs(tx);
			int ntermdoc=r.docFreq(tx);
			if(ntermdoc!= 0)
			{
				double ratio=(double)maxdoc/ntermdoc;
				idf=Math.log(ratio);
			} else {
				idf = 0.0;
			}
			    
				while(td.next())
				{
					if(g.containsKey(td.doc()))
					{
						double c=g.get(td.doc());
						g.put(td.doc(), c+Math.pow(td.freq()*idf, 2));
					}
					else
					{
						g.put(td.doc(), Math.pow(td.freq()*idf, 2));
					}
					
				}
		}
		
		for(Map.Entry<Integer, Double> entry: g.entrySet())
		{
		
			int docid=entry.getKey();
			double freqsq=entry.getValue();
			h.put(docid,Math.sqrt(freqsq));
		
		}
		
		//double min=20;
		//String tterm=null;
		//TermEnum x = r.terms();
		
	//	while(x.next())
		//{
			//	String a=x.term().text();
				//System.out.println(a);
				//Term tx=new Term("contents",a);
				//int ntermdoc=r.docFreq(tx);
				//if(ntermdoc!=0)
				//{
				//	double ratio=maxdoc/ntermdoc;
					//double idf=Math.log(ratio);
					//System.out.print("\t" +idf);
					//if(idf<min)
					//{
						//min=idf;
						//tterm = a;
					//}
				//}
			//}
		
		
		//System.out.println("Term with the lowest IDF is" + tterm);
	}
		
	}
			
		
		/**int count=0;
		for(Map.Entry<String, Double> entry:h.entrySet())
		{
			count++;
			System.out.println(entry.getKey()+"  "+entry.getValue());
			if(count%100==0)
				Thread.sleep(10000);
		} **/
		
		//long etime=System.currentTimeMillis();
		//long duration=etime-stime;
		//long durationinseconds=TimeUnit.MILLISECONDS.toSeconds(duration);
		//System.out.println("Duration for TF-IDF computation is " +durationinseconds);
		



