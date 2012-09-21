package ast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import impact.Resources;
 
public class FolderTraverser
{
	private static class JavaFilter implements FileFilter
	{
		private String[] extension = {"java", "jar"};

		@Override
		public boolean accept(File pathname)
		{
			if (pathname.isDirectory())
			{
				return true;
			}

			String name = pathname.getName().toLowerCase();
			for (String anExt : extension) {
				if (name.endsWith(anExt)) {
					return true;
				}
			}
			return false;
		}
	}

	private File originalFileObject;
	private File fileObject;
	private List<String> filePaths;
 
	public FolderTraverser(File fileObject) 
	{
		this.originalFileObject = fileObject;
		this.fileObject = fileObject;
		filePaths = new ArrayList<String>();

	}
 
	public void traverse()
	{
		recursiveTraversal(fileObject);
		Set<String> javaDirs = new HashSet<String>();
		Set<String> jarDirs  = new HashSet<String>();

		for(String filePath : filePaths)
		{
			// Need relative path to the root
			String root = originalFileObject.getAbsolutePath();
			String relativePath = filePath.substring(root.length(),	filePath.length());

			String[] allfilePaths = relativePath.split(Pattern.quote(File.separator));
			String dir ="";

			// Java needs all possible directory packages
			if(filePath.endsWith("java"))
			{
				// "." for root dir
				if(allfilePaths.length == 0)
				{
					String rootDir = "." + File.separator;
					javaDirs.add(rootDir);
				}

				for(String dirname : allfilePaths)
				{
					if(!dir.isEmpty())
					{
						String fullPath = Resources.repository + File.separator + dir;
						javaDirs.add(fullPath);
						dir += File.separator + dirname;
					}
					else
					{
						dir = dirname;
					}
				}
			}
			else // Class files only need absolute path to the jar because ast doesn't care 
			if(filePath.endsWith("jar"))
			{
				// "." for root dir
				if(allfilePaths.length == 0)
				{
					String rootDir = "." + File.separator;
					jarDirs.add(rootDir);
				}

				int lastSlashIndex = filePath.lastIndexOf(File.separator);
				if(lastSlashIndex != -1)
				{
					String directory = filePath.substring(0, lastSlashIndex);
					jarDirs.add(directory);
				}
			}
		}

		exportToConfigFile(javaDirs, jarDirs);
	}

	public void exportToConfigFile(Set<String> javaDirs, Set<String> jarDirs)
	{
		 try
		 {
			// erase the file
			 String configFile = Resources.configFile;
			 File file = new File(configFile);
			 file.createNewFile();
			 FileWriter fstream = new FileWriter(Resources.configFile);
			 BufferedWriter out = new BufferedWriter(fstream);

			 // rewrite file
			 out.write(Resources.classpath);
			 for(String jarDir : jarDirs)
			 {
				 out.newLine();
				 out.write(jarDir);
			 }

			 out.newLine();
			 out.newLine();
			 out.write(Resources.sourcepath);
			 for(String javadir : javaDirs)
			 {
				 out.newLine();
				 out.write(javadir);
			 }

			 out.close();
		 }
		 catch (Exception e)
		 {
			  System.err.println("Error: " + e.getMessage());
		  }
	}
 
	/**
	 * Get a list of package pack that contains java file
	 * @param fileObject
	 */
	public void recursiveTraversal(File fileObject)
	{
		if (fileObject.isDirectory())
		{
			File allFiles[] = fileObject.listFiles(new JavaFilter());

			for (File aFile : allFiles)
			{
				recursiveTraversal(aFile);
			}
		}
		else
		if (fileObject.isFile())
		{
			filePaths.add(fileObject.getAbsolutePath());
		}
	}
}