package edu.asu.irs13;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class SearchBean 
{
	String query;
	int choice;
	Map<Integer, String> result = new HashMap<Integer,String>();
	HashMap<List<Integer>,String> clusterResult = new HashMap<List<Integer>,String>();
	boolean initial = true;
	double w;
	Set<String> suggestionList = new HashSet<String>();
	String [] suggestion= new String[4];
	HashMap<Integer,String> snippet = new HashMap<Integer,String>();
	private String suggest;
	
	
	@PostConstruct
	void initializeSuggestion()
	{
		suggestionList.add("transcripts");
		suggestionList.add("medic care");
		suggestionList.add("src");
		suggestionList.add("stimulant web");
		suggestionList.add("employee benefits");
		suggestionList.add("parking decal");
	}
	
	public List<String> complete(String query)
	{
		ArrayList<String> s = new ArrayList<String>(suggestionList);
		
		if(!s.contains(query))
			s.add(query);
		
		return s;
	}
	
	public String getQuery()
	{
		return query;
	}
	
	public void setQuery(String query)
	{
		this.query=query;
	}
	
	public Map<Integer,String> getResult()
	{
		return result;
	}
	
	public boolean getInitial()
	{
		return initial;
	}
	
	public int getChoice()
	{
		return choice;
	}
	
	public void setChoice(int choice)
	{
		this.choice=choice;
	}
	
	public double getW()
	{
		return w;
	}
	public void setW(double w)
	{
		this.w=w;
	}
			
	public HashMap<List<Integer>,String> getclusterResult()
	{
		return clusterResult;
	}
	
	public void setclusterResult(HashMap<List<Integer>,String> clusterResult)
	{
		this.clusterResult = clusterResult;
	}
	
	public void setSuggestion(String [] suggestion)
	{
		this.suggestion=suggestion;
	}
	
	public String[] getSuggestion()
	{
		return suggestion;
	}
	
	public HashMap<Integer,String>  getSnippet()
	{
		return snippet;
	}
	
	public void setSnippet(HashMap<Integer,String> snippet)
	{
		this.snippet = snippet;
	}
		
	public String makeSearch() throws Exception
	{
		initial=false;
		if(choice==1) //choice = 1 tf/idf
		{
			result.clear();
			int docList[] = new int[10];
			System.out.println("Calling SearchFilesIDF.java");
			SearchFilesIDF s=new SearchFilesIDF();
			result=s.searchByIDF(query);
			append_result();
			//suggestion = s.suggestion;
			//suggest = "";
			//for(int y =0;y<suggestion.length;y++)
			//{
			//	suggest=suggest+" "+suggestion[y];
			//}
			int i=0;
			for(Map.Entry<Integer, String> entry : result.entrySet())
			{ 
				System.out.println(entry.getKey() + "  "+entry.getValue());
				docList[i++] = entry.getKey();
			}
			SnippetGenerator sn = new SnippetGenerator();
			snippet = sn.snipGenerate(docList, query); 
			
		}
		else if(choice==2)   //choice = 2 is authority 
		{
			result.clear();
			System.out.println("Entering authority hub with authority");
			int decision=1;
			int ch=1;
			IDFSearch i=new IDFSearch();
			result=i.searchByVector(query, decision,ch,0);
			append_result();
			for(Map.Entry<Integer, String> entry : result.entrySet())
			{ 
				System.out.println(entry.getKey() + "  "+entry.getValue());
			}
		}
		else if(choice==3)  //choice =3 is hub
		{
			result.clear();
			System.out.println("Entering authority hub with hubs");
			int decision=2;
			int ch=1;
			IDFSearch i=new IDFSearch();
			result=i.searchByVector(query, decision,ch,0);
			append_result();
			for(Map.Entry<Integer, String> entry : result.entrySet())
			{ 
				System.out.println(entry.getKey() + "  "+entry.getValue());
			}
		}
		else if(choice==4)
		{
			result.clear();
			System.out.println("Entering PR.java");
			int decision=0;
			int ch=2;
			IDFSearch x = new IDFSearch();
			result=x.searchByVector(query, decision, ch, w);
			append_result();
			for(Map.Entry<Integer, String> entry : result.entrySet())
			{ 
				System.out.println(entry.getKey() + "  "+entry.getValue());
			}
		}
		else
		{
			result.clear();
			ClusterSearchIDF s = new ClusterSearchIDF();
			result = s.searchByIDF(query);
			String[] terms = query.split("\\s+");
			DocumentVector d = new DocumentVector();
			int docList[] = new int[result.size()];
			//HashMap<List<Integer>,String> finalResult = new HashMap<List<Integer>,String>(); 
			int count =0;
			
			for(Map.Entry<Integer, String> entry : result.entrySet())
			{
				docList[count++] = entry.getKey();
			}
			
			Cluster c = new Cluster(docList);
			clusterResult = c.compute_cluster(docList, d.main_map, terms);
		}
			
		return "Done";
	}
	
	public void append_result()
	{
		for(Map.Entry<Integer, String> entry : result.entrySet())
		{ 
			String a = "http://"+entry.getValue();
			 result.put(entry.getKey(), a);
		}
	}

	public String getSuggest() {
		return suggest;
	}
	
	
	
}
