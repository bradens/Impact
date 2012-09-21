package models;

import java.util.ArrayList;
import java.util.List;

public class Method
{
	private String 			file;
	private String 			pkg;
	private String 			clazz;
	private String 			name;
	private List<String> 	parameters;
	private int 			start = -1;
	private int 			end = -1;

	private int				id;

	public Method() {
		parameters = new ArrayList<String>();
	}

	public Method(String file, String pkg, String clazz, String name,
			List<String> parameters, int start, int end)
	{
		super();
		this.file = file;
		this.pkg = pkg;
		this.clazz = clazz;
		this.name = name;
		this.parameters = parameters;
		this.start = start;
		this.end = end;
	}

	public Method(String file, String pkg, String clazz, String name,
			List<String> parameters, int start, int end, int id)
	{
		super();
		this.file = file;
		this.pkg = pkg;
		this.clazz = clazz;
		this.name = name;
		this.parameters = parameters;
		this.start = start;
		this.end = end;
		this.id = id;
	}

	public String getFile()
	{
		return file;
	}

	public void setFile(String file)
	{
		this.file = file;
	}

	public String getPkg()
	{
		return pkg;
	}

	public void setPkg(String pkg)
	{
		this.pkg = pkg;
	}

	public String getClazz()
	{
		return clazz;
	}

	public void setClazz(String clazz)
	{
		this.clazz = clazz;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void addParameter(String param) {
		parameters.add(param);
	}

	public List<String> getParameters()
	{
		return parameters;
	}

	public void setParameters(List<String> parameters)
	{
		this.parameters = parameters;
	}

	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public int getEnd()
	{
		return end;
	}

	public void setEnd(int end)
	{
		this.end = end;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	@Override
	public String toString() {
		String val = pkg + "." + clazz + "." + name + 
				"(" + parameters.toString() + ")";
		return val;
	}
}