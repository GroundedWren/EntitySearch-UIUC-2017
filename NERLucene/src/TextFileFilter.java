import java.io.File;

import java.io.FileFilter;

public class TextFileFilter implements FileFilter
{

	// https://www.tutorialspoint.com/lucene/lucene_indexing_process.htm
	@Override
	public boolean accept(File f)
	{
		return f.getName().toLowerCase().endsWith(".txt");
	}

}
