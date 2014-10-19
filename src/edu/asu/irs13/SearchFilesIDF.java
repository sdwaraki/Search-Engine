package edu.asu.irs13;


import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;

public class SearchFilesIDF extends TFIDFcalculation {
	
	SearchFilesIDF() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}
	public static int[] documentID=new int[10];
	public static String [] suggestion = null;
	
	public Map<Integer,String> searchByIDF(String query) throws Exception {
		
		
		IndexReader r = IndexReader.open(FSDirectory.open(new File("C:/Users/Sumanth/workspace/IRProject/index")));
		
		// You can figure out the number of documents using the maxDoc() function
		//System.out.println("The number of documents in this index is: " + r.maxDoc());
		
		
	/**	int i = 0;
		// You can find out all the terms that have been indexed using the terms() function
		TermEnum t = r.terms();
		while(t.next())
		{
			// Since there are so many terms, let us try printing only term #100000-#100010
			if (i > 100000) System.out.println("["+i+"] " + t.term().text());
			if (++i > 100010) break;
			
		
		}
		
		// You can create your own query terms by calling the Term constructor, with the field 'contents'
		// In the following example, the query term is 'brute'
		Term te = new Term("contents", "brute");
		
		// You can also quickly find out the number of documents that have term t
		System.out.println("Number of documents with the word 'brute' is: " + r.docFreq(te));
		
		// You can use the inverted index to find out all the documents that contain the term 'brute'
		//  by using the termDocs function
		TermDocs td = r.termDocs(te);
		
		while(td.next())
		{
			System.out.println("Document number ["+td.doc()+"] contains the term 'brute' " + td.freq() + " time(s).");
		}
		
		// You can find the URL of the a specific document number using the document() function
		// For example, the URL for document number 14191 is:
		Document d = r.document(1731);
		String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
		System.out.println(url.replace("%%", "/"));
		

		// -------- Now let us use all of the functions above to make something useful --------
		// The following bit of code is a worked out example of how to get a bunch of documents
		// in response to a query and show them (without ranking them according to TF/IDF) **/
		
		//@SuppressWarnings("resource")
		//Scanner sc = new Scanner(System.in);
		//String str = "";
		//System.out.print("query> ");
		double dotproduct=0;  	// To compute q.d
		double qmod=0; 		//qmod is the |q|
		double idf=0.0;
		//TFcalculation tf=new TFcalculation();		//tf is the object used to get the hashmap values from the TFcalculation.java
		TFIDFcalculation tfidf = new TFIDFcalculation(); // tfidf is the object used to get hashmap values frm TFIDFcalculation.java
		
		
		
		//while(!(str = sc.nextLine()).equals("quit"))
		//{
		//str=sc.nextLine();
		//sc.close();
		
		//if(sc.equals("\\n"))
			//continue;
			//long starttime= System.currentTimeMillis();
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
					//System.out.println("putting the term" +word+ "and its frequency "+freq);
					querymap.put(word, freq);
				
				}
				
			}
			
			for(Map.Entry<String, Integer> entry: querymap.entrySet()) //adding the frequencies of the terms to get |q|
			{
				int fr=(int) Math.pow(entry.getValue(), 2);
				qmod+=fr;
			}
			
			qmod=Math.sqrt(qmod);
			//System.out.println("qmod is "+qmod);
			
			/** To get the dotproduct  **/
			
			HashMap <Integer,Double> dotprod=new HashMap <Integer,Double>();
			
			for(String word : terms)
			{
				Term term = new Term("contents", word);//creating a term out of every query word
			    //System.out.println("Created the term "+term.text());
				TermDocs tdocs = r.termDocs(term);   //tdocs contains all the documents that contain the word term.
				//System.out.println(tdocs);
				
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
					//System.out.println("Frequency of "+term+" in the document "+tdocs.doc()+ " is " + qwordfreq);
					
					
					dotproduct = (qwordfreq * tdocs.freq()*idf);
					//System.out.println("Dotproduct of document" + tdocs.doc() + " for term " +term.text()+ " is "+dotproduct);
					
					//int tfweightvalue=tf.h.get(tdocs.doc());
					
					//int idfweightvalue= tfidf.h.get(term);
					//for(Map.Entry<Integer, Double> entry:dotprod.entrySet()) //why did you do this???
					//{
					//	if(tdocs.doc()>10000)
					//	 continue;
						
						if(!(dotprod.containsKey(tdocs.doc())))
						{
							//System.out.println("doc id" + tdocs.doc() + " dot product " + dotproduct);
							dotprod.put(tdocs.doc(), dotproduct);
						}
						else
						{
							double getval=dotprod.get(tdocs.doc());
							dotprod.put(tdocs.doc(), getval+dotproduct);
					//System.out.println("doc id" + tdocs.doc() + " dot product " + (dotproduct+getval));
						}
						
						
					//}
				}		
			}		
			
			
			//System.out.println("This is the dotproduct hashmap" +dotprod);
			
			//find out cosine similarity
			HashMap<Integer,Double> cosine=new HashMap<Integer,Double>();
			//HashMap<Integer,Double> idfcosine=new HashMap<Integer,Double>();
			
			for(Map.Entry<Integer, Double> entry:dotprod.entrySet())
			{
					double numerator=entry.getValue();
					double norm;
					//System.out.println("This is the numerator" + numerator);
					//System.out.println("This is the doc ID" +entry.getKey());
					if(tfidf.h.get(entry.getKey())!=null)
					{
						norm=tfidf.h.get(entry.getKey());
				//	double idfweightvalue=tfidf.h.get(entry.getKey());
					//System.out.println("This is the doc norm" + tfidfweightvalue);
					double denominator=qmod*norm;
					//double denominatoridf=qmod*idfweightvalue;
					//System.out.println("This is the denominator" + denominator);
					double sim=(double)numerator/denominator;
					//double idfsim=numerator/denominatoridf;
					//System.out.println("This will be the ratio" +sim);
					cosine.put(entry.getKey(), sim);
					}
					//idfcosine.put(entry.getKey(), idfsim);
			}		
			
		//System.out.println("This is the cosine similarity" +cosine);
			
			
			//Sort the cosine similarity hashmap
			//1.Put it into a Linked List and sort it
			//long start_sort_time=System.currentTimeMillis();
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
			
			//long end_sort_time=System.currentTimeMillis();
			//long sort_time=end_sort_time - start_sort_time;
			//System.out.println("Time taken for sorting is " + sort_time);
			
			int count=0;
			
			Map<Integer,String> resultdocs=new LinkedHashMap<Integer,String>();
			
			for(Map.Entry<Integer, Double> entry:sortedcosine.entrySet())
			{
				if(count++<10)
				{
					int did=entry.getKey();
					Document di=r.document(did);
					String urldi = di.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
					urldi= urldi.replace("%%", "/");
					System.out.println(did+"----"+urldi);
					resultdocs.put(did, urldi);
				}
				else
					break;
				
			}
					
			
			/**System.out.println("The top 10 results using TF-IDF are");
			System.out.println("-----------------------------------------------------------------------------------------------------------");
			for(Map.Entry<Integer,Double> entry: sortedcosine.entrySet())
			{
				
				if(count<10)
				{
					int did=entry.getKey();
					//System.out.print("\n Doc ID " + did);
					Document di=r.document(did);
					String urldi = di.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
					System.out.println("Doc ID " + did +"------ " + urldi.replace("%%", "/"));
					count++;
				}
				else
					break;
			
			}
			count=0;
			for(Map.Entry<Integer,Double> entry: sortedcosine.entrySet())
			{
				if(count<10)
				{
					int xw=entry.getKey();
					documentID[count++]=xw;
				}
			}
			
			//System.out.println("\n This is the count "+count);
			//for(int i=0;i<documentID.length;i++)
			//	System.out.println(documentID[i]);
			
			//System.out.println("\n Going to Link Analysis of Java");	
		    //Thread.sleep(5000);
			LinkAnalysis l=new LinkAnalysis();
			l.link_main(documentID);
			System.out.println("-----PageRank-----");
			//PageRank page=new PageRank();
			//page.prcompute(l,sortedcosine);
			
			//Prank p=new Prank();
			//p.precompute(l);
			
			PR pagerank=new PR();
			pagerank.precompute(l,sortedcosine);
			
			//print time taken to search using TF calculation
			//long etime=System.currentTimeMillis();
			//long durationtf=etime-starttime;
		//	System.out.println(" \n Duration taken to search using TF - IDF is " + durationtf + " milliseconds");
		//	System.out.println("The total number of results is " +count);
			// To find the cosine similiarity taking the idf into consideration : 
			
			 System.out.print("query> ");**/
			
			
			count = 0;
			
			int cluster_input[] = new int[50];
			
			for(Map.Entry<Integer,Double> entry: sortedcosine.entrySet())
			{
				if(count<50)
				{
					int xw=entry.getKey();
					cluster_input[count]=xw;
					count++;
				}
			}
			
			DocumentVector d = new DocumentVector();
			
			ScalarCluster s = new ScalarCluster();
			suggestion= s.suggestion(terms, d.main_map, cluster_input);
			
			return resultdocs;
		}
}
	

