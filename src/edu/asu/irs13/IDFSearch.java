package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;

public class IDFSearch extends TFIDFcalculation
{

	IDFSearch() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	public static int[] documentID=new int[10];
	
	public Map<Integer,String> searchByVector(String query, int decision,int ch,double w) throws Exception
	{
		
		IndexReader r = IndexReader.open(FSDirectory.open(new File("C:/Users/Sumanth/workspace/IRProject/index")));
		double dotproduct=0;  	// To compute q.d
		double qmod=0; 		//qmod is the |q|
		double idf=0.0;
		TFIDFcalculation tfidf = new TFIDFcalculation(); // tfidf is the object used to get hashmap values frm TFIDFcalculation.java
		
		
		
			String[] terms = query.split("\\s+");
			
			HashMap<String,Integer> querymap=new HashMap<String,Integer>();  //querymap will contain the query vector
			for(String word : terms)
			{
				if(!word.isEmpty())
				{
					
					Integer freq=querymap.get(word);
					if(freq==null)
					{
						freq=0;
					}
					
					++freq;
					
					querymap.put(word, freq);
				
				}
				
			}
			
			for(Map.Entry<String, Integer> entry: querymap.entrySet()) //adding the frequencies of the terms to get |q|
			{
				int fr=(int) Math.pow(entry.getValue(), 2);
				qmod+=fr;
			}
			
			qmod=Math.sqrt(qmod);
			
			
			/** To get the dotproduct  **/
			
			HashMap <Integer,Double> dotprod=new HashMap <Integer,Double>();
			
			for(String word : terms)
			{
				Term term = new Term("contents", word);//creating a term out of every query word
			    
				TermDocs tdocs = r.termDocs(term);   //tdocs contains all the documents that contain the word term.
				
				
				int ntermdoc=r.docFreq(term);
				if(ntermdoc!= 0)
				{
					double ratio=(double)r.maxDoc()/ntermdoc;
					idf=Math.log(ratio);
				} else {
					idf = 0.0;
				}
				
				while(tdocs.next())
				{
					String qword = term.text();    
					int qwordfreq=querymap.get(qword);
														
					dotproduct = (qwordfreq * tdocs.freq()*idf);
											
						if(!(dotprod.containsKey(tdocs.doc())))
						{
							
							dotprod.put(tdocs.doc(), dotproduct);
						}
						else
						{
							double getval=dotprod.get(tdocs.doc());
							dotprod.put(tdocs.doc(), getval+dotproduct);
					
						}
						
						
					
				}		
			}		
			
			
			
			
			//find out cosine similarity
			HashMap<Integer,Double> cosine=new HashMap<Integer,Double>();
			
			
			for(Map.Entry<Integer, Double> entry:dotprod.entrySet())
			{
					double numerator=entry.getValue();
					double norm;
					if(tfidf.h.get(entry.getKey())!=null)
					{
						norm=tfidf.h.get(entry.getKey());
					double denominator=qmod*norm;
					double sim=(double)numerator/denominator;
					cosine.put(entry.getKey(), sim);
					}
			
			}		
			
					
			//Sort the cosine similarity hashmap
			//1.Put it into a Linked List and sort it
			List<Map.Entry<Integer,Double>> list = new LinkedList<Map.Entry<Integer,Double>>(cosine.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>(){
					
					
					@Override
					public int compare(Entry<Integer, Double> o1,
							Entry<Integer, Double> o2) {
						// TODO Auto-generated method stub
							
						return o2.getValue().compareTo(o1.getValue());
					}
			});
			
			//2.Then put it into a LinkedHashMap
			Map <Integer, Double> sortedcosine=new LinkedHashMap<Integer,Double>();
			
			for(Map.Entry<Integer, Double> entry : list)
			{
				sortedcosine.put(entry.getKey(), entry.getValue());
			
			}
//--------------------------------------------------------------------------------------------------------------------------------		
		int count=0;
		for(Map.Entry<Integer,Double> entry: sortedcosine.entrySet())
		{
			if(count<10)
			{
				int xw=entry.getKey();
				documentID[count++]=xw;
			}
		}
		
		Map<Integer,String> resultdocs=new LinkedHashMap<Integer,String>();
		if(ch==1)
		{
			//call authority and hubs
			LinkAnalysis l=new LinkAnalysis();
			ArrayList<Integer> ahsorted=new ArrayList<Integer>();
			ahsorted=l.link_main(documentID,decision);
			System.out.println("Getting back ahsorted");
			
			int did;
			String urldi;
			for(int i=0;i<ahsorted.size();i++)
			{
				did=ahsorted.get(i);
				Document di=r.document(did);
				urldi = di.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
				urldi=urldi.replace("%%", "/");
				resultdocs.put(did,urldi);
			}
			System.out.println("Returning resultdocs");
			return resultdocs;
		}
		
		else
		{
			//Call PageRank
			PR p=new PR();
			resultdocs=p.precompute(sortedcosine, w);
			return resultdocs;
		}
			
	
	}

}
