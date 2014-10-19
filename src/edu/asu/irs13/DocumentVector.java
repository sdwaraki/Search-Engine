package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class DocumentVector 
{
	HashMap <Integer, HashMap<String,Double>> main_map = new HashMap<Integer,HashMap<String,Double>>();
	DocumentVector() throws CorruptIndexException, IOException
	{
		// TODO Auto-generated method stub
		IndexReader r = IndexReader.open(FSDirectory.open(new File("C:/Users/Sumanth/workspace/IRProject/index")));
		TermEnum t = r.terms();
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
				double weight = td.freq()*idf;
				if(main_map.containsKey(td.doc()))
				{
					HashMap<String,Double> inner_map = main_map.get(td.doc());
					inner_map.put(t.term().text(), weight);
					main_map.put(td.doc(), inner_map);
				}
				else
				{
					HashMap<String,Double> inner_map = new HashMap<String,Double>();
					inner_map.put(t.term().text(), weight);
					main_map.put(td.doc(), inner_map);
				}
			}
		}
	}
}
