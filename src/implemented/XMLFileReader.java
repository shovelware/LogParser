package implemented;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import interfaces.FileReader;
import interfaces.LogEntry;
import interfaces.ReportSummary;
import interfaces.TestCase;

public class XMLFileReader implements FileReader {

	List<interfaces.TestCase> testEntries_;
	
	public XMLFileReader() {
	}

	@Override
	public void readDir(String path) {
		List<String> fileList = new ArrayList<String>();
		List<String> cleanFileList = new ArrayList<String>();
		File f = new File(path);
		testEntries_ = new ArrayList<interfaces.TestCase>();
		
		//Get files in path
		fileList = new ArrayList<String>(Arrays.asList(f.list()));
		
		ArrayList<Integer> indices = new ArrayList<Integer>();
		int index = 0;
		
		if(fileList.size() == 0) {
			return;
		}
		
		//debug
		for (String s : fileList)
		{
			System.out.println(path + s);
		}
		//
		
		//Filter for .xml files
		for (String s : fileList) {
			if (new File("" + path + s).isDirectory())
			{
				//skip dirs
			}
			
			else if (s.split("\\.(?=[^\\.]+$)")[1].equals("xml") == true) {
				indices.add(index);
			}
			
			index++;
		}
		
		//Create the proper list
		for (Integer i : indices) {
			cleanFileList.add(fileList.get(i));
		}
		
		//Parse each file
		String lastTest = "";
		for (String n : cleanFileList) {
			String testName = n.split("_")[0];
			
			if (lastTest.equals(testName) == false) {
				lastTest = testName;
				
				interfaces.TestCase newTest = parseDocument(path + n);
				newTest.setName(testName);
				
				testEntries_.add(newTest);
			}		
		}
	}

	@Override
	public TestCase[] getEntries() {
		TestCase[] ret = new TestCase[testEntries_.size()];
		return testEntries_.toArray(ret);
	}
	
	private TestCase parseDocument(String filePath) {
		TestCase test = new TestCaseImpl();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(filePath);
			doc.getDocumentElement().normalize();
		}
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NodeList nList = doc.getElementsByTagName("LogEntry");
		
		for (int i = 0; i < nList.getLength(); i++) {
	    	LogEntry l = processLine(nList.item(i));
	    	
	    	updateStatus(test, l);
	    	
	    	test.addLog((interfaces.LogEntry) l);
		}
		
		return test;		
	}

	private LogEntry processLine(Node n) {
		LogEntry l = new LogEntryImpl();
		
		
		//p(n.getNodeName() + "|" + n.getTextContent());
		
		
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			Element el = (Element)n;
			
			NamedNodeMap nnm = el.getAttributes();
			
			for (int i = 0; i < nnm.getLength(); ++i) {
				l.addData(nnm.item(i).getNodeName(), nnm.item(i).getTextContent());
				//p(nnm.item(i).getNodeName() + ": "+ nnm.item(i).getTextContent());
			}
			l.addData("Message", n.getTextContent());
		}
		return l;
	}
	
	private void updateStatus(interfaces.TestCase entry, interfaces.LogEntry log) {
		if (log.getData().containsKey("Severity"))
		{
			if (log.getData().get("Severity").equals("Succes"))
			{
				entry.setStatus("Success");
			}

			else if (log.getData().get("Severity").equals("Fail"))
			{
				entry.setStatus("Failure");
			}
			
		}
	}
	
	//TODO: DELET THIS
	private void p(String s) { System.out.println(s); }

	@Override
	public ReportSummary getReport() {
		// TODO Auto-generated method stub
		return null;
	}
}
