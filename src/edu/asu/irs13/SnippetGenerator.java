package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class SnippetGenerator 
{
	//public void generateSnippet(int docID) throws CorruptIndexException, IOException
	public HashMap<Integer,String> snipGenerate(int docList[],String query) throws CorruptIndexException, IOException
	{

		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		HashMap<Integer,String> snippet = new HashMap<Integer,String>(); 
		
		for(int dcount =0; dcount<docList.length;dcount++)
		{
			int docid = docList[dcount];
			Document di = r.document(docList[dcount]);
			String urldi = "C:\\Users\\Sumanth\\Downloads\\Projectclass\\result3"+"\\"+ di.getFieldable("path").stringValue();
			System.out.println(urldi);
			File input = new File(urldi);
			org.jsoup.nodes.Document doc = Jsoup.parse(input, "UTF-8");
			String [] finalSnippets = new String [3];
			finalSnippets[0]=doc.title().toString();
			System.out.println("1."+finalSnippets[0]);
			StringBuilder a = new StringBuilder();
			Elements aele = doc.select("a");
			for(Element s : aele)
			{
				if(s.text().length()>0)
				{
					a.append(s.text());
					a.append(' ');
				}
				
			}
			Elements ele = doc.select("p");
			for(Element s : ele)
			{
				a.append(s.text());
				a.append(' ');
			}
			
			//System.out.println(a.toString());
			String terms[] = a.toString().split("\\s+");
			for (String x : terms)
			{
				x.toLowerCase();
				if(x.length()>1)
				System.out.println(x);
			}
			
			int[] index = new int[3];
			String []m = query.split("//s+");
			String q = m[0];
			
			int len = q.length();
			//System.out.println("LEN:"+len +"TL"+ terms.length);
			
			int counter = 0;		
			for(int i=0;i<terms.length;i++)
			{
				if((terms[i].regionMatches(true, 0, q.toLowerCase(), 0, len)&& counter < 3))
				{
					index[counter++] = i;
				}
			}
			
			//for(int i=0;i<3;i++)
			//	System.out.println(index[i]);
			
		//System.out.println("SNIPPET");
			StringBuilder str = new StringBuilder();
			for(int x =0;x<3;x++)
			{
				for(int i = index[x]-5; i<index[x]+5 ; i++)
				{
					if(i<0)
						continue; 
					str.append(terms[i]);
					str.append(" ");
				}
			}
			String snip = str.toString();
			snippet.put(docid, snip);
		}
		return snippet;
	}
}
