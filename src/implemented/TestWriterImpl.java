package implemented;

import interfaces.HTMLWriter;
import interfaces.ReportSummary;
import interfaces.TestCase;

public class TestWriterImpl implements HTMLWriter {

	TestCase[] entries_;
	
	public TestWriterImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setData(TestCase[] tests) {
		entries_ = tests.clone();
	}

	@Override
	public boolean writeReport(String path) {
		
		System.out.println("tcl" + entries_.length + "\n");
		
		for (TestCase t : entries_)
		{
			p(t.getName());
			
				for (String s : t.getPics())
				{
					p(s);
				}
			p("====end testcase====\n");
		}
		return true;
	}

	public void p(String p) {
		System.out.println(p);
	}

	@Override
	public void setSummary(ReportSummary data) {
		// TODO Auto-generated method stub
		
	}
}
